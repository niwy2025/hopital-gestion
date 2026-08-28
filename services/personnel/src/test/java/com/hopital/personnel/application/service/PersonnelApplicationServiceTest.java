package com.hopital.personnel.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hopital.personnel.application.domain.Gender;
import com.hopital.personnel.application.domain.PersonnelCategory;
import com.hopital.personnel.application.domain.PersonnelAssignmentScope;
import com.hopital.personnel.application.domain.PersonnelDocumentType;
import com.hopital.personnel.application.dto.CreatePersonnelDocumentRequest;
import com.hopital.personnel.application.dto.CreatePersonnelAssignmentRequest;
import com.hopital.personnel.application.dto.CreatePersonnelRequest;
import com.hopital.personnel.application.dto.PersonnelDocumentResponse;
import com.hopital.personnel.application.dto.PersonnelAssignmentResponse;
import com.hopital.personnel.application.dto.PersonnelResponse;
import com.hopital.personnel.infra.integration.account.AccountReferenceClient;
import com.hopital.personnel.infra.persistence.entity.PersonnelDocumentEntity;
import com.hopital.personnel.infra.persistence.entity.PersonnelAssignmentEntity;
import com.hopital.personnel.infra.persistence.entity.PersonnelEntity;
import com.hopital.personnel.infra.persistence.repository.PersonnelDocumentRepository;
import com.hopital.personnel.infra.persistence.repository.PersonnelAssignmentRepository;
import com.hopital.personnel.infra.persistence.repository.PersonnelRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonnelApplicationServiceTest {

    @Mock
    private PersonnelRepository personnelRepository;

    @Mock
    private AccountReferenceClient accountReferenceClient;

    @Mock
    private PersonnelDocumentRepository personnelDocumentRepository;

    @Mock
    private PersonnelAssignmentRepository personnelAssignmentRepository;

    @InjectMocks
    private PersonnelApplicationService personnelApplicationService;

    @Test
    void createsPersonnelWithNormalizedMatriculeAndVerifiedAccount() {
        UUID accountId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        when(personnelRepository.existsByEmployeeNumberIgnoreCase("MED-001")).thenReturn(false);
        when(personnelRepository.existsByAccountId(accountId)).thenReturn(false);
        when(personnelRepository.save(any(PersonnelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PersonnelResponse response = personnelApplicationService.createPersonnel(new CreatePersonnelRequest(
                " med-001 ",
                "Amina",
                "Kasongo",
                "Mbuyi",
                LocalDate.of(1988, 4, 15),
                Gender.FEMALE,
                PersonnelCategory.DOCTOR,
                "Médecin chef",
                " +243 900 000 000 ",
                " amina@hopital.cd ",
                "Matadi",
                hospitalId.toString(),
                accountId.toString()));

        ArgumentCaptor<PersonnelEntity> personnelCaptor = ArgumentCaptor.forClass(PersonnelEntity.class);
        verify(personnelRepository).save(personnelCaptor.capture());
        verify(accountReferenceClient).assertAccountExists(eq(accountId));
        assertThat(personnelCaptor.getValue().getEmployeeNumber()).isEqualTo("MED-001");
        assertThat(personnelCaptor.getValue().getPhoneNumber()).isEqualTo("+243 900 000 000");
        assertThat(response.hospitalId()).isEqualTo(hospitalId);
        assertThat(response.accountId()).isEqualTo(accountId);
        assertThat(response.active()).isTrue();
    }

    @Test
    void addsProfilePhotoAndReplacesPreviousVersion() {
        UUID personnelId = UUID.randomUUID();
        PersonnelEntity personnel = new PersonnelEntity(
                personnelId,
                "MED-001",
                "Amina",
                "Kasongo",
                null,
                null,
                Gender.FEMALE,
                PersonnelCategory.DOCTOR,
                "Médecin chef",
                null,
                null,
                null,
                null,
                null,
                java.time.Instant.now());
        when(personnelRepository.findById(personnelId)).thenReturn(Optional.of(personnel));
        when(personnelDocumentRepository.save(any(PersonnelDocumentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PersonnelDocumentResponse response = personnelApplicationService.addDocument(personnelId, new CreatePersonnelDocumentRequest(
                PersonnelDocumentType.PROFILE_PHOTO,
                "amina.png",
                "image/png",
                "aGVsbG8="));

        ArgumentCaptor<PersonnelDocumentEntity> documentCaptor = ArgumentCaptor.forClass(PersonnelDocumentEntity.class);
        verify(personnelDocumentRepository).deleteByPersonnelIdAndDocumentType(personnelId, PersonnelDocumentType.PROFILE_PHOTO);
        verify(personnelDocumentRepository).save(documentCaptor.capture());
        assertThat(documentCaptor.getValue().getPersonnelId()).isEqualTo(personnelId);
        assertThat(documentCaptor.getValue().getSizeBytes()).isEqualTo(5);
        assertThat(response.fileName()).isEqualTo("amina.png");
        assertThat(response.contentBase64()).isEqualTo("aGVsbG8=");
    }

    @Test
    void createsHospitalAssignmentForActivePersonnel() {
        UUID personnelId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        when(personnelRepository.findById(personnelId)).thenReturn(Optional.of(activePersonnel(personnelId)));
        when(personnelAssignmentRepository.existsByPersonnelIdAndStatusAndPrimaryAssignmentTrue(any(), any())).thenReturn(false);
        when(personnelAssignmentRepository.save(any(PersonnelAssignmentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PersonnelAssignmentResponse response = personnelApplicationService.createAssignment(personnelId,
                new CreatePersonnelAssignmentRequest(
                        PersonnelAssignmentScope.HOSPITAL,
                        hospitalId.toString(),
                        "Médecine interne",
                        "Hospitalisation",
                        "Médecin traitant",
                        LocalDate.of(2026, 8, 28),
                        true,
                        null));

        assertThat(response.personnelId()).isEqualTo(personnelId);
        assertThat(response.hospitalId()).isEqualTo(hospitalId);
        assertThat(response.primaryAssignment()).isTrue();
        assertThat(response.status().name()).isEqualTo("ACTIVE");
    }

    private PersonnelEntity activePersonnel(UUID personnelId) {
        return new PersonnelEntity(
                personnelId, "MED-001", "Amina", "Kasongo", null, null,
                Gender.FEMALE, PersonnelCategory.DOCTOR, "Médecin chef", null, null,
                null, null, null, java.time.Instant.now());
    }
}
