package com.hopital.patient.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.ClinicalEntryType;
import com.hopital.patient.application.domain.ClinicalOrientation;
import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.application.domain.EmergencyContactRelationship;
import com.hopital.patient.application.domain.Gender;
import com.hopital.patient.application.domain.PatientPassageStatus;
import com.hopital.patient.application.domain.PatientPassageType;
import com.hopital.patient.application.domain.PatientDocumentType;
import com.hopital.patient.application.domain.PrescriptionSource;
import com.hopital.patient.application.domain.PrescriptionStatus;
import com.hopital.patient.application.dto.CreatePatientDocumentRequest;
import com.hopital.patient.application.dto.CreatePatientRequest;
import com.hopital.patient.application.dto.CreatePatientPassageRequest;
import com.hopital.patient.application.dto.AssignPatientPassageResponsiblePersonnelRequest;
import com.hopital.patient.application.dto.EmergencyContactRequest;
import com.hopital.patient.application.dto.PatientDuplicateCheckRequest;
import com.hopital.patient.application.dto.PatientResponse;
import com.hopital.patient.application.dto.PatientPassageResponse;
import com.hopital.patient.application.dto.UpdatePatientStatusRequest;
import com.hopital.patient.application.dto.UpdatePatientPassageStatusRequest;
import com.hopital.patient.application.dto.CreatePatientPassageClinicalEntryRequest;
import com.hopital.patient.application.dto.CreatePatientPassagePrescriptionRequest;
import com.hopital.patient.application.dto.CreatePrescriptionDispenseRequest;
import com.hopital.patient.application.dto.PrescriptionItemRequest;
import com.hopital.patient.application.dto.PrescriptionDispenseItemRequest;
import com.hopital.patient.application.exception.DataAccessDeniedException;
import com.hopital.patient.application.dto.UpdatePatientRequest;
import com.hopital.patient.infra.integration.organization.HospitalReferenceClient;
import com.hopital.patient.infra.integration.personnel.PersonnelReferenceClient;
import com.hopital.patient.infra.persistence.entity.PatientEntity;
import com.hopital.patient.infra.persistence.entity.PatientPassageEntity;
import com.hopital.patient.infra.persistence.entity.PatientPassageClinicalEntryEntity;
import com.hopital.patient.infra.persistence.entity.PatientDocumentEntity;
import com.hopital.patient.infra.persistence.entity.PatientPassagePrescriptionEntity;
import com.hopital.patient.infra.persistence.entity.PatientPassagePrescriptionItemEntity;
import com.hopital.patient.infra.persistence.repository.PatientPassageRepository;
import com.hopital.patient.infra.persistence.repository.PatientPassageClinicalEntryRepository;
import com.hopital.patient.infra.persistence.repository.PatientPassagePrescriptionItemRepository;
import com.hopital.patient.infra.persistence.repository.PatientPassagePrescriptionRepository;
import com.hopital.patient.infra.persistence.repository.PatientPassagePrescriptionDispenseItemRepository;
import com.hopital.patient.infra.persistence.repository.PatientPassagePrescriptionDispenseRepository;
import com.hopital.patient.infra.persistence.repository.PatientDocumentRepository;
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
    private PatientDocumentRepository patientDocumentRepository;

    @Mock
    private PatientPassageRepository patientPassageRepository;

    @Mock
    private PatientPassageClinicalEntryRepository patientPassageClinicalEntryRepository;

    @Mock
    private PatientPassagePrescriptionRepository patientPassagePrescriptionRepository;

    @Mock
    private PatientPassagePrescriptionItemRepository patientPassagePrescriptionItemRepository;

    @Mock
    private PatientPassagePrescriptionDispenseRepository patientPassagePrescriptionDispenseRepository;

    @Mock
    private PatientPassagePrescriptionDispenseItemRepository patientPassagePrescriptionDispenseItemRepository;

    @Mock
    private HospitalReferenceClient hospitalReferenceClient;

    @Mock
    private PersonnelReferenceClient personnelReferenceClient;

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
                new CreatePatientPassageRequest(null, PatientPassageType.CONSULTATION, "Accueil", "Consultation générale", null),
                new DataAccessScope(false, hospitalId, "HP-GOMA"),
                auditActor());

        assertThat(response.code()).startsWith("PAS-");
        assertThat(response.hospitalId()).isEqualTo(hospitalId);
        assertThat(response.hospitalCode()).isEqualTo("HP-GOMA");
        assertThat(response.status()).isEqualTo(PatientPassageStatus.OPEN);
        assertThat(response.createdByUsername()).isEqualTo("operateur.accueil");
    }

    @Test
    void assignsAnActiveHospitalPersonnelToAnOpenPassage() {
        PatientEntity patient = patient("HP-GOMA");
        PatientPassageEntity passage = new PatientPassageEntity(
                UUID.randomUUID(), "PAS-20260901-ABCD1234", patient, patient.getRegistrationHospitalId(), "HP-GOMA",
                PatientPassageType.CONSULTATION, "Consultations externes", null, auditActor(), Instant.now());
        UUID personnelId = UUID.randomUUID();
        when(patientPassageRepository.findById(passage.getId())).thenReturn(Optional.of(passage));
        when(personnelReferenceClient.resolveActivePersonnelForHospital(personnelId, patient.getRegistrationHospitalId()))
                .thenReturn(new PersonnelReferenceClient.PersonnelReference(
                        personnelId, "MED-001", "Amina", "Kasongo", "Mbuyi", "DOCTOR", "Médecin traitant"));

        PatientPassageResponse response = patientApplicationService.assignPassageResponsiblePersonnel(
                patient.getId(),
                passage.getId(),
                new AssignPatientPassageResponsiblePersonnelRequest(personnelId),
                new DataAccessScope(false, patient.getRegistrationHospitalId(), "HP-GOMA"),
                auditActor());

        assertThat(response.responsiblePersonnelId()).isEqualTo(personnelId);
        assertThat(response.responsiblePersonnelEmployeeNumber()).isEqualTo("MED-001");
        assertThat(response.responsiblePersonnelName()).isEqualTo("Kasongo Amina Mbuyi");
        assertThat(response.responsibleAssignedByUsername()).isEqualTo("operateur.accueil");
    }

    @Test
    void letsTheResponsiblePersonnelCloseTheirOwnPassage() {
        PatientEntity patient = patient("HP-GOMA");
        UUID responsiblePersonnelId = UUID.randomUUID();
        PatientPassageEntity passage = new PatientPassageEntity(
                UUID.randomUUID(), "PAS-20260901-ABCD1234", patient, patient.getRegistrationHospitalId(), "HP-GOMA",
                PatientPassageType.CONSULTATION, "Consultations externes", null, auditActor(), Instant.now());
        passage.assignResponsiblePersonnel(
                responsiblePersonnelId, "MED-001", "Kasongo Amina", "Médecin traitant", auditActor(), Instant.now());
        when(patientPassageRepository.findById(passage.getId())).thenReturn(Optional.of(passage));

        PatientPassageResponse response = patientApplicationService.updatePassageStatus(
                patient.getId(),
                passage.getId(),
                new UpdatePatientPassageStatusRequest(PatientPassageStatus.CLOSED),
                new DataAccessScope(false, false, responsiblePersonnelId, patient.getRegistrationHospitalId(), "HP-GOMA"),
                auditActor());

        assertThat(response.status()).isEqualTo(PatientPassageStatus.CLOSED);
        assertThat(response.closedByUsername()).isEqualTo("operateur.accueil");
    }

    @Test
    void preventsClosingAPassageWhileAPrescriptionIsStillAwaitingDispense() {
        PatientEntity patient = patient("HP-GOMA");
        UUID responsiblePersonnelId = UUID.randomUUID();
        PatientPassageEntity passage = new PatientPassageEntity(
                UUID.randomUUID(), "PAS-20260902-ABCD1234", patient, patient.getRegistrationHospitalId(), "HP-GOMA",
                PatientPassageType.CONSULTATION, "Consultations externes", null, auditActor(), Instant.now());
        passage.assignResponsiblePersonnel(
                responsiblePersonnelId, "MED-001", "Kasongo Amina", "Médecin traitant", auditActor(), Instant.now());
        when(patientPassageRepository.findById(passage.getId())).thenReturn(Optional.of(passage));
        when(patientPassagePrescriptionRepository.existsByPassage_IdAndStatusIn(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> patientApplicationService.updatePassageStatus(
                patient.getId(),
                passage.getId(),
                new UpdatePatientPassageStatusRequest(PatientPassageStatus.CLOSED),
                new DataAccessScope(false, false, responsiblePersonnelId, patient.getRegistrationHospitalId(), "HP-GOMA"),
                auditActor()))
                .isInstanceOf(com.hopital.patient.application.exception.InvalidPatientPassageStateException.class)
                .hasMessageContaining("ordonnances");
    }

    @Test
    void refusesStatusChangesFromPersonnelNotResponsibleForThePassage() {
        PatientEntity patient = patient("HP-GOMA");
        PatientPassageEntity passage = new PatientPassageEntity(
                UUID.randomUUID(), "PAS-20260901-ABCD1234", patient, patient.getRegistrationHospitalId(), "HP-GOMA",
                PatientPassageType.CONSULTATION, "Consultations externes", null, auditActor(), Instant.now());
        passage.assignResponsiblePersonnel(
                UUID.randomUUID(), "MED-001", "Kasongo Amina", "Médecin traitant", auditActor(), Instant.now());
        when(patientPassageRepository.findById(passage.getId())).thenReturn(Optional.of(passage));

        assertThatThrownBy(() -> patientApplicationService.updatePassageStatus(
                patient.getId(),
                passage.getId(),
                new UpdatePatientPassageStatusRequest(PatientPassageStatus.CANCELLED),
                new DataAccessScope(false, false, UUID.randomUUID(), patient.getRegistrationHospitalId(), "HP-GOMA"),
                auditActor()))
                .isInstanceOf(DataAccessDeniedException.class)
                .hasMessageContaining("personnel responsable");
    }

    @Test
    void appendsClinicalEntryOnlyForTheResponsiblePersonnelOnAnOpenPassage() {
        PatientEntity patient = patient("HP-GOMA");
        UUID responsiblePersonnelId = UUID.randomUUID();
        PatientPassageEntity passage = new PatientPassageEntity(
                UUID.randomUUID(), "PAS-20260901-ABCD1234", patient, patient.getRegistrationHospitalId(), "HP-GOMA",
                PatientPassageType.CONSULTATION, "Consultations externes", null, auditActor(), Instant.now());
        passage.assignResponsiblePersonnel(
                responsiblePersonnelId, "MED-001", "Kasongo Amina", "Médecin traitant", auditActor(), Instant.now());
        when(patientPassageRepository.findById(passage.getId())).thenReturn(Optional.of(passage));
        when(patientPassageClinicalEntryRepository.save(any(PatientPassageClinicalEntryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = patientApplicationService.addClinicalEntry(
                patient.getId(),
                passage.getId(),
                new CreatePatientPassageClinicalEntryRequest(
                        ClinicalEntryType.INITIAL_ASSESSMENT,
                        "Toux persistante, température à 38,5 °C.",
                        "Infection respiratoire à confirmer",
                        "Hydratation, surveillance et bilan complémentaire.",
                        ClinicalOrientation.LABORATORY,
                        LocalDate.of(2026, 9, 4)),
                new DataAccessScope(false, false, responsiblePersonnelId, patient.getRegistrationHospitalId(), "HP-GOMA"),
                auditActor());

        assertThat(response.passageId()).isEqualTo(passage.getId());
        assertThat(response.entryType()).isEqualTo(ClinicalEntryType.INITIAL_ASSESSMENT);
        assertThat(response.orientation()).isEqualTo(ClinicalOrientation.LABORATORY);
        assertThat(response.recordedByUsername()).isEqualTo("operateur.accueil");
        assertThat(patient.getAuditEvents()).singleElement().satisfies(event ->
                assertThat(event.getType()).isEqualTo(com.hopital.patient.application.domain.PatientAuditEventType.CLINICAL_ENTRY_ADDED));
    }

    @Test
    void searchesClinicalJournalWithServerSideFilters() {
        PatientEntity patient = patient("HP-GOMA");
        PatientPassageEntity passage = new PatientPassageEntity(
                UUID.randomUUID(), "PAS-20260901-ABCD1234", patient, patient.getRegistrationHospitalId(), "HP-GOMA",
                PatientPassageType.CONSULTATION, "Consultations externes", null, auditActor(), Instant.now());
        PatientPassageClinicalEntryEntity entry = new PatientPassageClinicalEntryEntity(
                UUID.randomUUID(),
                passage.getId(),
                ClinicalEntryType.CLINICAL_EVOLUTION,
                "La fièvre diminue après surveillance.",
                null,
                "Poursuivre l'hydratation.",
                ClinicalOrientation.FOLLOW_UP,
                null,
                auditActor(),
                Instant.now());
        when(patientPassageRepository.findById(passage.getId())).thenReturn(Optional.of(passage));
        when(patientPassageClinicalEntryRepository.search(
                org.mockito.ArgumentMatchers.eq(passage.getId()),
                org.mockito.ArgumentMatchers.eq("fièvre"),
                org.mockito.ArgumentMatchers.eq(ClinicalEntryType.CLINICAL_EVOLUTION),
                org.mockito.ArgumentMatchers.eq(ClinicalOrientation.FOLLOW_UP),
                any())).thenReturn(new PageImpl<>(List.of(entry)));

        var response = patientApplicationService.searchClinicalEntries(
                patient.getId(),
                passage.getId(),
                0,
                20,
                "fièvre",
                ClinicalEntryType.CLINICAL_EVOLUTION,
                ClinicalOrientation.FOLLOW_UP,
                new DataAccessScope(false, patient.getRegistrationHospitalId(), "HP-GOMA"));

        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(entry.getId());
            assertThat(item.entryType()).isEqualTo(ClinicalEntryType.CLINICAL_EVOLUTION);
            assertThat(item.orientation()).isEqualTo(ClinicalOrientation.FOLLOW_UP);
        });
    }

    @Test
    void recordsAnExternalPrescriptionWithoutImpersonatingAPlatformDoctor() {
        PatientEntity patient = patient("HP-GOMA");
        PatientPassageEntity passage = new PatientPassageEntity(
                UUID.randomUUID(), "PAS-20260901-ABCD1234", patient, patient.getRegistrationHospitalId(), "HP-GOMA",
                PatientPassageType.PHARMACY, "Pharmacie hospitalière", null, auditActor(), Instant.now());
        when(patientPassageRepository.findById(passage.getId())).thenReturn(Optional.of(passage));
        when(patientPassagePrescriptionRepository.existsByCodeIgnoreCase(any())).thenReturn(false);
        when(patientPassagePrescriptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(patientPassagePrescriptionItemRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = patientApplicationService.addPrescription(
                patient.getId(),
                passage.getId(),
                new CreatePatientPassagePrescriptionRequest(
                        PrescriptionSource.EXTERNAL_PAPER,
                        "Dr. Mavungu",
                        "ORD-PAPIER-54",
                        "Ordonnance apportée par le patient.",
                        List.of(new PrescriptionItemRequest(
                                "Amoxicilline", "500 mg", "Voie orale", "3 fois par jour", "7 jours", "21 gélules", null))),
                new DataAccessScope(false, patient.getRegistrationHospitalId(), "HP-GOMA"),
                auditActor());

        assertThat(response.code()).startsWith("ORD-");
        assertThat(response.source()).isEqualTo(PrescriptionSource.EXTERNAL_PAPER);
        assertThat(response.externalPrescriberName()).isEqualTo("Dr. Mavungu");
        assertThat(response.items()).singleElement().satisfies(item ->
                assertThat(item.medicineName()).isEqualTo("Amoxicilline"));
        assertThat(patient.getAuditEvents()).singleElement().satisfies(event ->
                assertThat(event.getType()).isEqualTo(com.hopital.patient.application.domain.PatientAuditEventType.PRESCRIPTION_ADDED));
    }

    @Test
    void recordsACompletePharmacyDispenseAndSettlesThePrescription() {
        PatientEntity patient = patient("HP-GOMA");
        PatientPassageEntity passage = new PatientPassageEntity(
                UUID.randomUUID(), "PAS-20260902-ABCD1234", patient, patient.getRegistrationHospitalId(), "HP-GOMA",
                PatientPassageType.PHARMACY, "Pharmacie hospitalière", null, auditActor(), Instant.now());
        PatientPassagePrescriptionEntity prescription = new PatientPassagePrescriptionEntity(
                UUID.randomUUID(), "ORD-20260902-ABCD1234", passage, PrescriptionSource.MEDICAL,
                null, null, null, auditActor(), Instant.now());
        PatientPassagePrescriptionItemEntity prescriptionItem = new PatientPassagePrescriptionItemEntity(
                UUID.randomUUID(), prescription, "Amoxicilline", "500 mg", "Voie orale",
                "3 fois par jour", "7 jours", "21 gélules", null, 0);
        when(patientPassagePrescriptionRepository.findById(prescription.getId())).thenReturn(Optional.of(prescription));
        when(patientPassagePrescriptionItemRepository.findAllByPrescription_IdInOrderByDisplayOrderAsc(any()))
                .thenReturn(List.of(prescriptionItem));
        when(patientPassagePrescriptionDispenseRepository.existsByCodeIgnoreCase(any())).thenReturn(false);
        when(patientPassagePrescriptionDispenseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(patientPassagePrescriptionDispenseItemRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = patientApplicationService.dispensePrescription(
                prescription.getId(),
                new CreatePrescriptionDispenseRequest(true, "Traitement remis au patient.", List.of(
                        new PrescriptionDispenseItemRequest(prescriptionItem.getId(), "21 gélules"))),
                new DataAccessScope(false, patient.getRegistrationHospitalId(), "HP-GOMA"),
                auditActor());

        assertThat(response.code()).startsWith("DSP-");
        assertThat(response.items()).singleElement().satisfies(item ->
                assertThat(item.medicineName()).isEqualTo("Amoxicilline"));
        assertThat(prescription.getStatus()).isEqualTo(PrescriptionStatus.DISPENSED);
        assertThat(patient.getAuditEvents()).singleElement().satisfies(event ->
                assertThat(event.getType()).isEqualTo(com.hopital.patient.application.domain.PatientAuditEventType.PRESCRIPTION_DISPENSED));
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
        when(patientPassageRepository.searchRegistry(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(passage)));

        var response = patientApplicationService.searchPassageRegistry(
                0,
                20,
                "Amina",
                UUID.randomUUID(),
                PatientPassageType.CONSULTATION,
                PatientPassageStatus.OPEN,
                false,
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
                org.mockito.ArgumentMatchers.isNull(),
                any());
    }

    @Test
    void returnsPassageDetailsOnlyInsideTheOperatorScope() {
        PatientEntity patient = patient("HP-GOMA");
        PatientPassageEntity passage = new PatientPassageEntity(
                UUID.randomUUID(),
                "PAS-20260901-ABCD1234",
                patient,
                patient.getRegistrationHospitalId(),
                "HP-GOMA",
                PatientPassageType.CONSULTATION,
                "Accueil et triage",
                "Consultation générale",
                auditActor(),
                Instant.now());
        when(patientPassageRepository.findById(passage.getId())).thenReturn(Optional.of(passage));

        var response = patientApplicationService.getPassage(
                passage.getId(), new DataAccessScope(false, patient.getRegistrationHospitalId(), "HP-GOMA"));

        assertThat(response.id()).isEqualTo(passage.getId());
        assertThat(response.code()).isEqualTo("PAS-20260901-ABCD1234");
        assertThat(response.patientId()).isEqualTo(patient.getId());
        assertThat(response.patientCode()).isEqualTo("PAT-0001");
        assertThat(response.serviceName()).isEqualTo("Accueil et triage");
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

    @Test
    void addsPatientDocumentAndRecordsItsOperator() {
        PatientEntity patient = patient("HP-GOMA");
        when(patientRepository.findById(patient.getId())).thenReturn(Optional.of(patient));
        when(patientDocumentRepository.save(any(PatientDocumentEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = patientApplicationService.addDocument(
                patient.getId(),
                new CreatePatientDocumentRequest(
                        PatientDocumentType.IDENTITY_CARD,
                        "carte-identite.jpg",
                        "image/jpeg",
                        "aGVsbG8="),
                new DataAccessScope(false, "HP-GOMA"),
                auditActor());

        assertThat(response.documentType()).isEqualTo(PatientDocumentType.IDENTITY_CARD);
        assertThat(response.sizeBytes()).isEqualTo(5);
        assertThat(response.createdByUsername()).isEqualTo("operateur.accueil");
        assertThat(patient.getAuditEvents()).singleElement().satisfies(event -> {
            assertThat(event.getType()).isEqualTo(com.hopital.patient.application.domain.PatientAuditEventType.DOCUMENT_ADDED);
            assertThat(event.getOperatorUsername()).isEqualTo("operateur.accueil");
        });
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
