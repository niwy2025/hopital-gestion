package com.hopital.patient.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.hopital.patient.application.domain.Gender;
import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.application.dto.CreatePatientRequest;
import com.hopital.patient.application.dto.PatientResponse;
import com.hopital.patient.application.dto.UpdatePatientStatusRequest;
import com.hopital.patient.infra.persistence.entity.PatientEntity;
import com.hopital.patient.infra.persistence.repository.PatientRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientApplicationServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientApplicationService patientApplicationService;

    @Test
    void createsPatientWithNormalizedCodes() {
        when(patientRepository.existsByCodeIgnoreCase("PAT-0001")).thenReturn(false);
        when(patientRepository.save(any(PatientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientResponse response = patientApplicationService.createPatient(new CreatePatientRequest(
                " pat-0001 ",
                "Amina",
                "Kasongo",
                LocalDate.of(1992, 5, 4),
                Gender.FEMALE,
                " +243 900 000 000 ",
                "Goma",
                " hp-goma "), new DataAccessScope(true, null));

        assertThat(response.code()).isEqualTo("PAT-0001");
        assertThat(response.registrationHospitalCode()).isEqualTo("HP-GOMA");
        assertThat(response.phoneNumber()).isEqualTo("+243 900 000 000");
        assertThat(response.active()).isTrue();
    }

    @Test
    void updatesPatientStatus() {
        PatientEntity patient = new PatientEntity(
                UUID.randomUUID(),
                "PAT-0001",
                "Amina",
                "Kasongo",
                LocalDate.of(1992, 5, 4),
                Gender.FEMALE,
                null,
                null,
                "HP-GOMA",
                Instant.now());
        when(patientRepository.findByCodeIgnoreCase("PAT-0001")).thenReturn(Optional.of(patient));

        PatientResponse response = patientApplicationService.updateStatus(
                "pat-0001", new UpdatePatientStatusRequest(false), new DataAccessScope(true, null));

        assertThat(response.active()).isFalse();
    }
}
