package com.hopital.patient.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.application.domain.EmergencyContactRelationship;
import com.hopital.patient.application.domain.Gender;
import com.hopital.patient.application.domain.PatientPassageStatus;
import com.hopital.patient.application.domain.PatientPassageType;
import com.hopital.patient.application.dto.CreatePatientRequest;
import com.hopital.patient.application.dto.CreatePatientPassageRequest;
import com.hopital.patient.application.dto.EmergencyContactRequest;
import com.hopital.patient.application.dto.PatientDuplicateCheckRequest;
import com.hopital.patient.application.dto.PatientResponse;
import com.hopital.patient.application.dto.PatientPassageResponse;
import com.hopital.patient.application.dto.UpdatePatientStatusRequest;
import com.hopital.patient.application.dto.UpdatePatientRequest;
import com.hopital.patient.infra.integration.organization.HospitalReferenceClient;
import com.hopital.patient.infra.persistence.entity.PatientEntity;
import com.hopital.patient.infra.persistence.entity.PatientPassageEntity;
import com.hopital.patient.infra.persistence.repository.PatientPassageRepository;
import com.hopital.patient.infra.persistence.repository.PatientRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class PatientApplicationServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientPassageRepository patientPassageRepository;

    @Mock
    private HospitalReferenceClient hospitalReferenceClient;

    @InjectMocks
    private PatientApplicationService patientApplicationService;

    @Test
    void createsPatientWithGeneratedDossierNumberAndHospitalReference() {
        UUID hospitalId = UUID.randomUUID();
        when(hospitalReferenceClient.resolveActiveHospital(hospitalId))
                .thenReturn(new HospitalReferenceClient.HospitalReference(hospitalId, "HP-GOMA", true));
        when(patientRepository.existsByCodeIgnoreCase(any())).thenReturn(false);
        when(patientRepository.findByIdentity(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(patientRepository.existsByNationalIdentifierIgnoreCase(any())).thenReturn(false);
        when(patientRepository.save(any(PatientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientResponse response = patientApplicationService.createPatient(new CreatePatientRequest(
                "Amina",
                "Kasongo",
                "Mbuyi",
                LocalDate.of(1992, 5, 4),
                Gender.FEMALE,
                " +243 900 000 000 ",
                "amina@example.cd",
                "Goma",
                "AB-12345",
                List.of(new EmergencyContactRequest(
                        "Jean Kasongo",
                        "+243 810 000 000",
                        EmergencyContactRelationship.SIBLING)),
                hospitalId), new DataAccessScope(true, null), auditActor());

        assertThat(response.code()).startsWith("PAT-");
        assertThat(response.registrationHospitalCode()).isEqualTo("HP-GOMA");
        assertThat(response.registrationHospitalId()).isEqualTo(hospitalId);
        assertThat(response.nationalIdentifier()).startsWith("NAT-");
        assertThat(response.emergencyContacts()).hasSize(1);
        assertThat(response.phoneNumber()).isEqualTo("+243 900 000 000");
        assertThat(response.active()).isTrue();
        assertThat(response.createdByUsername()).isEqualTo("operateur.accueil");
        assertThat(response.auditEvents()).singleElement().satisfies(event -> {
            assertThat(event.type()).isEqualTo(com.hopital.patient.application.domain.PatientAuditEventType.CREATED);
            assertThat(event.operatorUsername()).isEqualTo("operateur.accueil");
        });
    }

    @Test
    void updatesPatientStatus() {
        PatientEntity patient = new PatientEntity(
                UUID.randomUUID(),
                "PAT-0001",
                "Amina",
                "Kasongo",
                null,
                LocalDate.of(1992, 5, 4),
                Gender.FEMALE,
                null,
                null,
                null,
                null,
                UUID.randomUUID(),
                "HP-GOMA",
                Instant.now());
        when(patientRepository.findById(patient.getId())).thenReturn(Optional.of(patient));

        PatientResponse response = patientApplicationService.updateStatus(
                patient.getId(), new UpdatePatientStatusRequest(false), new DataAccessScope(true, null), auditActor());

        assertThat(response.active()).isFalse();
        assertThat(response.updatedByUsername()).isEqualTo("operateur.accueil");
        assertThat(response.auditEvents()).singleElement().satisfies(event ->
                assertThat(event.type()).isEqualTo(com.hopital.patient.application.domain.PatientAuditEventType.STATUS_CHANGED));
    }

    @Test
    void updatesPatientWithoutChangingTheRegistrationHospital() {
        PatientEntity patient = patient("HP-GOMA");
        UUID registrationHospitalId = patient.getRegistrationHospitalId();
        when(patientRepository.findById(patient.getId())).thenReturn(Optional.of(patient));
        when(patientRepository.findByIdentity(any(), any(), any(), any(), any())).thenReturn(List.of(patient));

        PatientResponse response = patientApplicationService.updatePatient(patient.getId(), new UpdatePatientRequest(
                "Aminata",
                "Kasongo",
                "Mbuyi",
                LocalDate.of(1992, 5, 4),
                Gender.FEMALE,
                "+243 900 000 001",
                "aminata@example.cd",
                        "Goma",
                        List.of(new EmergencyContactRequest(
                                "Jean Kasongo",
                                "+243 810 000 000",
                        EmergencyContactRelationship.SIBLING))), new DataAccessScope(false, "HP-GOMA"), auditActor());

        assertThat(response.firstName()).isEqualTo("Aminata");
        assertThat(response.registrationHospitalId()).isEqualTo(registrationHospitalId);
        assertThat(response.emergencyContacts()).hasSize(1);
        assertThat(response.updatedByUsername()).isEqualTo("operateur.accueil");
        verify(patientRepository).flush();
    }

    @Test
    void createsPassageInTheOperatorHospital() {
        PatientEntity patient = patient("HP-GOMA");
        UUID hospitalId = patient.getRegistrationHospitalId();
        when(patientRepository.findById(patient.getId())).thenReturn(Optional.of(patient));
        when(patientPassageRepository.existsByCodeIgnoreCase(any())).thenReturn(false);
        when(patientPassageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PatientPassageResponse response = patientApplicationService.createPassage(
                patient.getId(),
                new CreatePatientPassageRequest(null, PatientPassageType.CONSULTATION, "Accueil", "Consultation générale"),
                new DataAccessScope(false, hospitalId, "HP-GOMA"),
                auditActor());

        assertThat(response.code()).startsWith("PAS-");
        assertThat(response.hospitalId()).isEqualTo(hospitalId);
        assertThat(response.hospitalCode()).isEqualTo("HP-GOMA");
        assertThat(response.status()).isEqualTo(PatientPassageStatus.OPEN);
        assertThat(response.createdByUsername()).isEqualTo("operateur.accueil");
    }

    @Test
    void searchesOnlyPassagesFromTheOperatorHospital() {
        PatientEntity patient = patient("HP-GOMA");
        PatientPassageEntity passage = new PatientPassageEntity(
                UUID.randomUUID(),
                "PAS-20260831-ABCD1234",
                patient,
                patient.getRegistrationHospitalId(),
                "HP-GOMA",
                PatientPassageType.CONSULTATION,
                "Accueil",
                "Consultation générale",
                auditActor(),
                Instant.now());
        when(patientPassageRepository.searchRegistry(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(passage)));

        var response = patientApplicationService.searchPassageRegistry(
                0,
                20,
                "Amina",
                UUID.randomUUID(),
                PatientPassageType.CONSULTATION,
                PatientPassageStatus.OPEN,
                new DataAccessScope(false, patient.getRegistrationHospitalId(), "HP-GOMA"));

        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.patientId()).isEqualTo(patient.getId());
            assertThat(item.patientCode()).isEqualTo("PAT-0001");
            assertThat(item.patientLastName()).isEqualTo("Kasongo");
            assertThat(item.hospitalCode()).isEqualTo("HP-GOMA");
        });
        verify(patientPassageRepository).searchRegistry(
                org.mockito.ArgumentMatchers.eq("HP-GOMA"),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq("Amina"),
                org.mockito.ArgumentMatchers.eq(PatientPassageType.CONSULTATION),
                org.mockito.ArgumentMatchers.eq(PatientPassageStatus.OPEN),
                any());
    }

    @Test
    void returnsOnlyDuplicateCandidatesVisibleInTheCallerScope() {
        PatientEntity visiblePatient = patient("HP-GOMA");
        PatientEntity hiddenPatient = patient("HP-BUKAVU");
        when(patientRepository.findByIdentity(any(), any(), any(), any(), any()))
                .thenReturn(List.of(visiblePatient, hiddenPatient));

        var response = patientApplicationService.checkDuplicates(new PatientDuplicateCheckRequest(
                "Amina",
                "Kasongo",
                "Mbuyi",
                LocalDate.of(1992, 5, 4),
                Gender.FEMALE), new DataAccessScope(false, "HP-GOMA"));

        assertThat(response.matches()).extracting(match -> match.id()).containsExactly(visiblePatient.getId());
    }

    private PatientEntity patient(String hospitalCode) {
        return new PatientEntity(
                UUID.randomUUID(),
                "PAT-0001",
                "Amina",
                "Kasongo",
                "Mbuyi",
                LocalDate.of(1992, 5, 4),
                Gender.FEMALE,
                null,
                null,
                null,
                "NAT-20260831-ABCDEF1234",
                UUID.randomUUID(),
                hospitalCode,
                Instant.now());
    }

    private AuditActor auditActor() {
        return new AuditActor("b5695329-1d88-4bb5-b136-e609301c9c04", "operateur.accueil");
    }
}
