package com.hopital.patient.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.application.domain.Gender;
import com.hopital.patient.application.domain.PatientPassageStatus;
import com.hopital.patient.application.domain.PatientPassageType;
import com.hopital.patient.application.domain.PatientTransferStatus;
import com.hopital.patient.application.dto.CreatePatientTransferRequest;
import com.hopital.patient.application.dto.ReceivePatientTransferRequest;
import com.hopital.patient.infra.integration.organization.HospitalReferenceClient;
import com.hopital.patient.infra.persistence.entity.PatientEntity;
import com.hopital.patient.infra.persistence.entity.PatientPassageEntity;
import com.hopital.patient.infra.persistence.entity.PatientTransferEntity;
import com.hopital.patient.infra.persistence.repository.PatientRepository;
import com.hopital.patient.infra.persistence.repository.PatientPassageRepository;
import com.hopital.patient.infra.persistence.repository.PatientTransferRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

@DataJpaTest(properties = { "spring.jpa.hibernate.ddl-auto=validate", "spring.jpa.show-sql=false" })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PatientTransferService.class)
@EnabledIfEnvironmentVariable(named = "TRANSFER_POSTGRES_TEST", matches = "true")
class PatientTransferPersistenceTest {
    @Autowired PatientTransferService service;
    @Autowired PatientRepository patients;
    @Autowired PatientPassageRepository passages;
    @Autowired PatientTransferRepository transfers;
    @Autowired EntityManager entityManager;
    @MockBean HospitalReferenceClient hospitals;
    private final UUID origin = UUID.randomUUID();
    private final UUID destination = UUID.randomUUID();
    private final AuditActor actor = new AuditActor("test-actor", "medecin.test");
    private final DataAccessScope admin = new DataAccessScope(true, true, Set.of("ADMIN"), null, null, null);

    private PatientPassageEntity source() {
        var patient = new PatientEntity(UUID.randomUUID(), "PAT-" + UUID.randomUUID().toString().substring(0, 8),
                "Marie", "Test", null, LocalDate.of(1990, 1, 1), Gender.FEMALE, null, null, null,
                UUID.randomUUID().toString(), origin, "ORIGIN", Instant.now());
        patient.recordCreation(actor, Instant.now());
        patients.saveAndFlush(patient);
        return passages.saveAndFlush(new PatientPassageEntity(UUID.randomUUID(),
                "PAS-" + UUID.randomUUID().toString().substring(0, 8), patient, origin, "ORIGIN",
                PatientPassageType.CONSULTATION, null, null, actor, Instant.now()));
    }

    @Test void persistsTransferAndScopesBothSidesWithoutDuplicatingPatient() {
        var source = source();
        UUID patientId = source.getPatient().getId();
        when(hospitals.resolveActiveHospital(destination)).thenReturn(
                new HospitalReferenceClient.HospitalReference(destination, "DEST", true));
        when(hospitals.resolveActiveHospital(origin)).thenReturn(
                new HospitalReferenceClient.HospitalReference(origin, "ORIGIN", true, "Hôpital de départ"));
        var request = new CreatePatientTransferRequest(destination, null, null, null,
                "Orientation", "Synthèse", "Consignes", "Ambulance", false);
        var created = service.create(source.getId(), request, admin, actor);
        entityManager.flush();
        entityManager.clear();
        var receiver = new DataAccessScope(false, false, Set.of("RECEPTIONIST"), null, destination, "DEST");
        var sender = new DataAccessScope(false, false, Set.of("HOSPITAL_ADMIN"), null, origin, "ORIGIN");
        assertThat(created.sourceHospitalName()).isEqualTo("Hôpital de départ");
        assertThat(service.search(0, 20, "", null, "OUTGOING", null, source.getId(), null, sender).totalElements()).isEqualTo(1);
        assertThat(service.search(0, 20, "", null, "INCOMING", null, null, null, receiver).totalElements()).isEqualTo(1);
        assertThat(service.search(0, 20, "départ", null, "OUTGOING", origin, null, null, admin).totalElements()).isEqualTo(1);
        assertThat(service.search(0, 20, "", null, "INCOMING", origin, null, null, admin).totalElements()).isZero();
        assertThat(service.search(0, 20, "", null, "INCOMING", origin, null, null, receiver).totalElements()).isEqualTo(1);
        assertThat(patients.search("", "DEST", null, null, PageRequest.of(0, 20)).getTotalElements()).isZero();
        service.dispatch(created.id(), admin, actor);
        entityManager.flush();
        entityManager.clear();
        var received = service.receive(created.id(),
                new ReceivePatientTransferRequest(PatientPassageType.HOSPITALIZATION, "Médecine interne", "Arrivée constatée"),
                receiver, actor);
        entityManager.flush();
        entityManager.clear();
        assertThat(service.get(created.id(), receiver).status()).isEqualTo(PatientTransferStatus.RECEIVED);
        assertThat(passages.findById(source.getId()).orElseThrow().getStatus()).isEqualTo(PatientPassageStatus.TRANSFERRED);
        assertThat(passages.findById(received.destinationPassageId()).orElseThrow().getHospitalId()).isEqualTo(destination);
        assertThat(patients.count()).isEqualTo(1);
        assertThat(patients.findById(patientId).orElseThrow().getRegistrationHospitalId()).isEqualTo(origin);
        assertThat(patients.search("", "DEST", null, null, PageRequest.of(0, 20)).getTotalElements()).isEqualTo(1);
        assertThat(patients.findAllAccessibleToHospital("DEST")).hasSize(1);
        assertThat(passages.search(patientId, "DEST", "", null, null, PageRequest.of(0, 20)).getContent())
                .extracting(PatientPassageEntity::getId).containsExactly(received.destinationPassageId());
        assertThat(passages.existsByPatient_IdAndHospitalCodeIgnoreCase(patientId, "DEST")).isTrue();
        assertThat(patients.search("", "THIRD", null, null, PageRequest.of(0, 20)).getTotalElements()).isZero();
        assertThat(service.search(0, 20, "", null, "", null, null, patientId,
                new DataAccessScope(false, false, Set.of("NURSE"), null, UUID.randomUUID(), "THIRD")).totalElements()).isZero();
    }

    @Test void databaseRejectsDuplicateUncancelledTransfer() {
        var source = source();
        var request = new CreatePatientTransferRequest(null, "Structure externe", null, null,
                "Motif", "Synthèse", null, null, false);
        transfers.saveAndFlush(new PatientTransferEntity(source, request, null, null, actor));
        assertThatThrownBy(() -> transfers.saveAndFlush(new PatientTransferEntity(source, request, null, null, actor)))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
