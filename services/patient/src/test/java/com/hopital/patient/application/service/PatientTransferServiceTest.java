package com.hopital.patient.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.application.domain.Gender;
import com.hopital.patient.application.domain.PatientPassageStatus;
import com.hopital.patient.application.domain.PatientPassageType;
import com.hopital.patient.application.domain.PatientTransferStatus;
import com.hopital.patient.application.dto.CreatePatientTransferRequest;
import com.hopital.patient.application.dto.ReceivePatientTransferRequest;
import com.hopital.patient.application.exception.DataAccessDeniedException;
import com.hopital.patient.application.exception.InvalidPatientPassageStateException;
import com.hopital.patient.infra.integration.organization.HospitalReferenceClient;
import com.hopital.patient.infra.persistence.entity.PatientEntity;
import com.hopital.patient.infra.persistence.entity.PatientPassageEntity;
import com.hopital.patient.infra.persistence.entity.PatientTransferEntity;
import com.hopital.patient.infra.persistence.repository.PatientPassageRepository;
import com.hopital.patient.infra.persistence.repository.PatientTransferRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientTransferServiceTest {
    @Mock PatientTransferRepository transfers;
    @Mock PatientPassageRepository passages;
    @Mock HospitalReferenceClient hospitals;
    @InjectMocks PatientTransferService service;
    private final UUID origin = UUID.randomUUID();
    private final UUID destination = UUID.randomUUID();
    private final UUID doctorId = UUID.randomUUID();
    private final AuditActor actor = new AuditActor("doctor-id", "medecin.test");

    private PatientPassageEntity source() {
        var patient = new PatientEntity(UUID.randomUUID(), "PAT-TEST", "Marie", "Test", null,
                LocalDate.of(1990, 1, 1), Gender.FEMALE, null, null, null, "NAT-TEST", origin, "ORIGIN", Instant.now());
        patient.recordCreation(actor, Instant.now());
        var passage = new PatientPassageEntity(UUID.randomUUID(), "PAS-TEST", patient, origin, "ORIGIN",
                PatientPassageType.CONSULTATION, "Médecine", null, actor, Instant.now());
        passage.assignResponsiblePersonnel(doctorId, "EMP-001", "Docteur Test", "Médecin", actor, Instant.now());
        return passage;
    }

    private DataAccessScope sender() {
        return new DataAccessScope(false, false, Set.of("DOCTOR"), doctorId, origin, "ORIGIN");
    }

    private DataAccessScope receiver() {
        return new DataAccessScope(false, false, Set.of("RECEPTIONIST"), null, destination, "DEST");
    }

    private CreatePatientTransferRequest request(boolean internal) {
        return new CreatePatientTransferRequest(internal ? destination : null, internal ? null : "Clinique externe",
                internal ? null : "Matadi", "Contact service", "Soins spécialisés", "Synthèse de test",
                "Consignes de test", "Ambulance", true);
    }

    @Test void preparesInternalTransferWithoutMovingOrDuplicatingPatient() {
        var source = source();
        when(passages.findForUpdate(source.getId())).thenReturn(Optional.of(source));
        when(hospitals.resolveActiveHospital(destination)).thenReturn(new HospitalReferenceClient.HospitalReference(destination, "DEST", true));
        when(hospitals.resolveActiveHospital(origin)).thenReturn(new HospitalReferenceClient.HospitalReference(origin, "ORIGIN", true));
        var response = service.create(source.getId(), request(true), sender(), actor);
        assertThat(response.status()).isEqualTo(PatientTransferStatus.REQUESTED);
        assertThat(response.patientId()).isEqualTo(source.getPatient().getId());
        assertThat(response.destinationPassageId()).isNull();
        assertThat(response.code()).startsWith("TRF-");
        assertThat(source.getStatus()).isEqualTo(PatientPassageStatus.OPEN);
        assertThat(source.getPatient().getRegistrationHospitalId()).isEqualTo(origin);
    }

    @Test void rejectsUnassignedDoctorAndOtherHospital() {
        var source = source();
        when(passages.findForUpdate(source.getId())).thenReturn(Optional.of(source));
        assertThatThrownBy(() -> service.create(source.getId(), request(true),
                new DataAccessScope(false, false, Set.of("DOCTOR"), UUID.randomUUID(), origin, "ORIGIN"), actor))
                .isInstanceOf(DataAccessDeniedException.class);
        assertThatThrownBy(() -> service.create(source.getId(), request(true), receiver(), actor))
                .isInstanceOf(DataAccessDeniedException.class);
        verifyNoInteractions(hospitals);
    }

    @Test void rejectsDuplicateAndSameHospitalTransfers() {
        var source = source();
        when(passages.findForUpdate(source.getId())).thenReturn(Optional.of(source));
        var sameHospital = new CreatePatientTransferRequest(origin, null, null, null, "Motif", "Synthèse", null, null, false);
        assertThatThrownBy(() -> service.create(source.getId(), sameHospital, sender(), actor))
                .isInstanceOf(InvalidPatientPassageStateException.class);
        when(transfers.existsBySourcePassage_IdAndStatusNot(source.getId(), PatientTransferStatus.CANCELLED)).thenReturn(true);
        assertThatThrownBy(() -> service.create(source.getId(), request(true), sender(), actor))
                .isInstanceOf(InvalidPatientPassageStateException.class);
        verifyNoInteractions(hospitals);
    }

    @Test void recordsExternalDestinationAndFinishesOnlyAtDeparture() {
        var source = source();
        var transfer = new PatientTransferEntity(source, request(false), null, null, actor);
        when(transfers.findForUpdate(transfer.getId())).thenReturn(Optional.of(transfer));
        when(passages.findForUpdate(source.getId())).thenReturn(Optional.of(source));
        var result = service.dispatch(transfer.getId(), sender(), actor);
        assertThat(result.status()).isEqualTo(PatientTransferStatus.EXTERNAL_COMPLETED);
        assertThat(result.externalFacilityName()).isEqualTo("Clinique externe");
        assertThat(result.destinationPassageId()).isNull();
        assertThat(source.getStatus()).isEqualTo(PatientPassageStatus.TRANSFERRED);
        assertThat(service.dispatch(transfer.getId(), sender(), actor)).isEqualTo(result);
        verifyNoInteractions(hospitals);
    }

    @Test void receivesOnceAndStartsNormalCareAtDestination() {
        var source = source();
        var transfer = new PatientTransferEntity(source, request(true), destination, "DEST", actor);
        transfer.dispatch(actor, Instant.now());
        source.changeStatus(PatientPassageStatus.TRANSFERRED, actor, Instant.now());
        when(transfers.findForUpdate(transfer.getId())).thenReturn(Optional.of(transfer));
        when(hospitals.resolveActiveHospital(destination)).thenReturn(new HospitalReferenceClient.HospitalReference(destination, "DEST", true));
        var request = new ReceivePatientTransferRequest(PatientPassageType.EMERGENCY, "Urgences", "Patient arrivé");
        var result = service.receive(transfer.getId(), request, receiver(), actor);
        assertThat(result.status()).isEqualTo(PatientTransferStatus.RECEIVED);
        assertThat(result.patientId()).isEqualTo(source.getPatient().getId());
        assertThat(result.destinationPassageId()).isNotEqualTo(source.getId());
        assertThat(transfer.getDestinationPassage().getStatus()).isEqualTo(PatientPassageStatus.OPEN);
        assertThat(transfer.getDestinationPassage().getHospitalId()).isEqualTo(destination);
        assertThat(transfer.getDestinationPassage().getResponsiblePersonnelId()).isNull();
        assertThat(service.receive(transfer.getId(), request, receiver(), actor).destinationPassageId()).isEqualTo(result.destinationPassageId());
        verify(passages, times(1)).saveAndFlush(any(PatientPassageEntity.class));
    }

    @Test void cannotReceiveBeforeDepartureOrAtWrongHospital() {
        var transfer = new PatientTransferEntity(source(), request(true), destination, "DEST", actor);
        when(transfers.findForUpdate(transfer.getId())).thenReturn(Optional.of(transfer));
        var request = new ReceivePatientTransferRequest(PatientPassageType.CONSULTATION, null, null);
        assertThatThrownBy(() -> service.receive(transfer.getId(), request, receiver(), actor))
                .isInstanceOf(InvalidPatientPassageStateException.class);
        transfer.dispatch(actor, Instant.now());
        assertThatThrownBy(() -> service.receive(transfer.getId(), request, sender(), actor))
                .isInstanceOf(DataAccessDeniedException.class);
        verifyNoInteractions(passages, hospitals);
    }

    @Test void unrelatedHospitalCannotReadTransfer() {
        var transfer = new PatientTransferEntity(source(), request(true), destination, "DEST", actor);
        when(transfers.findById(transfer.getId())).thenReturn(Optional.of(transfer));
        var thirdHospital = new DataAccessScope(false, false, Set.of("DOCTOR"), UUID.randomUUID(), UUID.randomUUID(), "THIRD");
        assertThatThrownBy(() -> service.get(transfer.getId(), thirdHospital)).isInstanceOf(DataAccessDeniedException.class);
        assertThat(service.get(transfer.getId(), receiver()).clinicalSummary()).isEqualTo("Synthèse de test");
    }

    @Test void cancellationPreservesSourceAndIsForbiddenAfterDeparture() {
        var source = source();
        var transfer = new PatientTransferEntity(source, request(true), destination, "DEST", actor);
        when(transfers.findForUpdate(transfer.getId())).thenReturn(Optional.of(transfer));
        assertThat(service.cancel(transfer.getId(), "Réorientation", sender(), actor).status()).isEqualTo(PatientTransferStatus.CANCELLED);
        assertThat(source.getStatus()).isEqualTo(PatientPassageStatus.OPEN);
        var departed = new PatientTransferEntity(source, request(true), destination, "DEST", actor);
        departed.dispatch(actor, Instant.now());
        when(transfers.findForUpdate(departed.getId())).thenReturn(Optional.of(departed));
        assertThatThrownBy(() -> service.cancel(departed.getId(), "Annulation", sender(), actor))
                .isInstanceOf(InvalidPatientPassageStateException.class);
    }
}
