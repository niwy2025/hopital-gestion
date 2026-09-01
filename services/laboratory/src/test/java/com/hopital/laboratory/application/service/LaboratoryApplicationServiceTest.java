package com.hopital.laboratory.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.hopital.laboratory.application.domain.AnalysisRequestStatus;
import com.hopital.laboratory.application.domain.AnalysisResultStatus;
import com.hopital.laboratory.application.domain.LaboratoryType;
import com.hopital.laboratory.application.domain.SpecimenType;
import com.hopital.laboratory.application.dto.CreateAnalysisRequestRequest;
import com.hopital.laboratory.application.dto.CreateAnalysisResultRequest;
import com.hopital.laboratory.application.dto.CreatePatientPassageAnalysisRequest;
import com.hopital.laboratory.application.dto.CreateSpecimenRequest;
import com.hopital.laboratory.application.dto.ValidateAnalysisResultRequest;
import com.hopital.laboratory.application.exception.InvalidLaboratoryWorkflowException;
import com.hopital.laboratory.infra.persistence.entity.AnalysisRequestEntity;
import com.hopital.laboratory.infra.persistence.entity.AnalysisResultEntity;
import com.hopital.laboratory.infra.persistence.entity.SpecimenEntity;
import com.hopital.laboratory.infra.persistence.repository.AnalysisRequestRepository;
import com.hopital.laboratory.infra.persistence.repository.AnalysisResultRepository;
import com.hopital.laboratory.infra.persistence.repository.SpecimenRepository;
import com.hopital.laboratory.infra.integration.organization.HospitalLaboratoryReferenceClient;
import com.hopital.laboratory.infra.integration.patient.PatientPassageReferenceClient;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LaboratoryApplicationServiceTest {

    @Mock
    private AnalysisRequestRepository analysisRequestRepository;

    @Mock
    private SpecimenRepository specimenRepository;

    @Mock
    private AnalysisResultRepository analysisResultRepository;

    @Mock
    private PatientPassageReferenceClient patientPassageReferenceClient;

    @Mock
    private HospitalLaboratoryReferenceClient hospitalLaboratoryReferenceClient;

    @InjectMocks
    private LaboratoryApplicationService laboratoryApplicationService;

    @Test
    void progressesFromRequestToValidatedResult() {
        AnalysisRequestEntity analysisRequest = new AnalysisRequestEntity(
                UUID.randomUUID(),
                "REQ-001",
                LaboratoryType.HOSPITAL,
                "LAB-HGR-001",
                "PAT-001",
                "Patient de test",
                "NFS",
                "Numération formule sanguine",
                "Dr. Mbala",
                Instant.now());
        when(analysisRequestRepository.existsByCodeIgnoreCase(anyString())).thenReturn(false);
        when(analysisRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(analysisRequestRepository.findByCodeIgnoreCase("REQ-001")).thenReturn(Optional.of(analysisRequest));
        when(specimenRepository.existsByCodeIgnoreCase(anyString())).thenReturn(false);
        when(specimenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(analysisResultRepository.existsByCodeIgnoreCase(anyString())).thenReturn(false);
        when(analysisResultRepository.existsByAnalysisRequest_Id(analysisRequest.getId())).thenReturn(false);
        when(analysisResultRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var createdRequest = laboratoryApplicationService.createAnalysisRequest(new CreateAnalysisRequestRequest(
                LaboratoryType.HOSPITAL, "lab-hgr-001", "PAT-001", "Patient de test", "Numération formule sanguine", "Dr. Mbala"));
        var specimen = laboratoryApplicationService.receiveSpecimen(new CreateSpecimenRequest(
                "req-001", SpecimenType.BLOOD, Instant.now()));
        var result = laboratoryApplicationService.enterAnalysisResult(new CreateAnalysisResultRequest(
                "req-001", "12.4", "g/dL", "12 - 16", null));
        AnalysisResultEntity analysisResult = new AnalysisResultEntity(
                UUID.randomUUID(),
                "RES-001",
                analysisRequest,
                "12.4",
                "g/dL",
                "12 - 16",
                null,
                Instant.now());
        when(analysisResultRepository.findByCodeIgnoreCase("RES-001")).thenReturn(Optional.of(analysisResult));
        var validatedResult = laboratoryApplicationService.validateAnalysisResult(
                "res-001", new ValidateAnalysisResultRequest("biologiste"));

        assertThat(createdRequest.status()).isEqualTo(AnalysisRequestStatus.REQUESTED);
        assertThat(createdRequest.code()).startsWith("LAB-");
        assertThat(createdRequest.analysisCode()).startsWith("ANL-");
        assertThat(createdRequest.laboratoryType()).isEqualTo(LaboratoryType.HOSPITAL);
        assertThat(createdRequest.laboratoryCode()).isEqualTo("LAB-HGR-001");
        assertThat(specimen.analysisRequestCode()).isEqualTo("REQ-001");
        assertThat(specimen.code()).startsWith("ECH-");
        assertThat(result.status()).isEqualTo(AnalysisResultStatus.ENTERED);
        assertThat(result.code()).startsWith("RES-");
        assertThat(validatedResult.status()).isEqualTo(AnalysisResultStatus.VALIDATED);
        assertThat(analysisRequest.getStatus()).isEqualTo(AnalysisRequestStatus.VALIDATED);
    }

    @Test
    void rejectsAResultBeforeASpecimenIsReceived() {
        AnalysisRequestEntity analysisRequest = new AnalysisRequestEntity(
                UUID.randomUUID(), "REQ-001", LaboratoryType.REFERENCE, "LRP-KIN", "PAT-001", "Patient", "NFS", "NFS", null, Instant.now());
        when(analysisRequestRepository.findByCodeIgnoreCase("REQ-001")).thenReturn(Optional.of(analysisRequest));

        assertThatThrownBy(() -> laboratoryApplicationService.enterAnalysisResult(new CreateAnalysisResultRequest(
                "req-001", "12.4", null, null, null)))
                .isInstanceOf(InvalidLaboratoryWorkflowException.class)
                .hasMessageContaining("après la réception");
    }

    @Test
    void returnsTheSampleWithItsRequestAndWorkflow() {
        AnalysisRequestEntity analysisRequest = new AnalysisRequestEntity(
                UUID.randomUUID(),
                "LAB-001",
                LaboratoryType.HOSPITAL,
                "LAB-HGR-001",
                "PAT-001",
                "Patient de test",
                "ANL-001",
                "Numération formule sanguine",
                "Dr. Mbala",
                Instant.now());
        SpecimenEntity specimen = new SpecimenEntity(
                UUID.randomUUID(),
                "ECH-001",
                analysisRequest,
                SpecimenType.BLOOD,
                Instant.now().minusSeconds(600),
                Instant.now());
        when(specimenRepository.findByCodeIgnoreCase("ECH-001")).thenReturn(Optional.of(specimen));
        when(specimenRepository.findAllByAnalysisRequest_IdInOrderByReceivedAtDesc(List.of(analysisRequest.getId())))
                .thenReturn(List.of(specimen));
        when(analysisResultRepository.findAllByAnalysisRequest_IdIn(List.of(analysisRequest.getId())))
                .thenReturn(List.of());

        var detail = laboratoryApplicationService.getSpecimenDetail("ech-001");

        assertThat(detail.specimen().code()).isEqualTo("ECH-001");
        assertThat(detail.request().code()).isEqualTo("LAB-001");
        assertThat(detail.specimens()).singleElement().extracting(item -> item.code()).isEqualTo("ECH-001");
        assertThat(detail.result()).isNull();
    }

    @Test
    void bindsAPassageRequestToItsActiveHospitalLaboratory() {
        UUID passageId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        when(patientPassageReferenceClient.resolve(passageId)).thenReturn(
                new PatientPassageReferenceClient.PatientPassageReference(
                        passageId,
                        "PASS-001",
                        UUID.randomUUID(),
                        "PAT-001",
                        "Patient de test",
                        hospitalId,
                        "HGR-001",
                        "Médecine interne",
                        "OPEN"));
        when(hospitalLaboratoryReferenceClient.resolveActiveHospital(hospitalId)).thenReturn(
                new HospitalLaboratoryReferenceClient.HospitalReference(
                        hospitalId,
                        "HGR-001",
                        true,
                        java.util.List.of("LAB-HGR-001"),
                        java.util.List.of(new HospitalLaboratoryReferenceClient.HospitalLaboratoryReference(
                                "LAB-HGR-001", "Laboratoire HGR"))));
        when(analysisRequestRepository.existsByCodeIgnoreCase(anyString())).thenReturn(false);
        when(analysisRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = laboratoryApplicationService.createPatientPassageAnalysisRequest(
                passageId,
                new CreatePatientPassageAnalysisRequest("lab-hgr-001", "Numération formule sanguine"),
                "dr.mbala");

        assertThat(response.patientPassageId()).isEqualTo(passageId);
        assertThat(response.patientReference()).isEqualTo("PAT-001");
        assertThat(response.patientName()).isEqualTo("Patient de test");
        assertThat(response.laboratoryType()).isEqualTo(LaboratoryType.HOSPITAL);
        assertThat(response.laboratoryCode()).isEqualTo("LAB-HGR-001");
        assertThat(response.requesterName()).isEqualTo("dr.mbala");
        assertThat(response.code()).startsWith("LAB-");
        assertThat(response.analysisCode()).startsWith("ANL-");
    }
}
