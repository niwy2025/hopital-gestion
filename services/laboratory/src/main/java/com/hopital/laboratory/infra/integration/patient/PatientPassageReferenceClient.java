package com.hopital.laboratory.infra.integration.patient;

import com.hopital.laboratory.application.exception.LaboratoryResourceNotFoundException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Resolves the source-of-truth patient passage on the private service network. */
@Component
public class PatientPassageReferenceClient {

    private final RestClient patientClient;

    public PatientPassageReferenceClient(
            RestClient.Builder builder,
            @Value("${hospital.patient-service.base-url}") String patientServiceBaseUrl) {
        this.patientClient = builder.baseUrl(patientServiceBaseUrl).build();
    }

    public PatientPassageReference resolve(UUID passageId) {
        try {
            PatientPassageReference reference = patientClient.get()
                    .uri("/internal/patients/passages/{passageId}/laboratory-reference", passageId)
                    .retrieve()
                    .body(PatientPassageReference.class);
            if (reference == null) {
                throw new LaboratoryResourceNotFoundException("Le passage patient", passageId.toString());
            }
            return reference;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                throw new LaboratoryResourceNotFoundException("Le passage patient", passageId.toString());
            }
            throw exception;
        }
    }

    public record PatientPassageReference(
            UUID passageId,
            String passageCode,
            UUID patientId,
            String patientCode,
            String patientName,
            UUID hospitalId,
            String hospitalCode,
            String serviceName,
            String status) {
    }
}
