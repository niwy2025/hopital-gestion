package com.hopital.personnel.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hopital.personnel.application.domain.Gender;
import com.hopital.personnel.application.domain.PersonnelCategory;
import com.hopital.personnel.application.dto.CreatePersonnelRequest;
import com.hopital.personnel.application.dto.PersonnelResponse;
import com.hopital.personnel.infra.integration.account.AccountReferenceClient;
import com.hopital.personnel.infra.persistence.entity.PersonnelEntity;
import com.hopital.personnel.infra.persistence.repository.PersonnelRepository;
import java.time.LocalDate;
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
}
