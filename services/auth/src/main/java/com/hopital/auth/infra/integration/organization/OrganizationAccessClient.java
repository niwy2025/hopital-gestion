package com.hopital.auth.infra.integration.organization;

import com.hopital.auth.application.config.AuthServiceProperties;
import com.hopital.auth.application.dto.HospitalAccessReferenceResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OrganizationAccessClient {

    private final RestClient organizationClient;

    public OrganizationAccessClient(RestClient.Builder builder, AuthServiceProperties properties) {
        this.organizationClient = builder.baseUrl(properties.organizationServiceBaseUrl()).build();
    }

    public HospitalAccessReferenceResponse resolveHospital(String hospitalId) {
        return organizationClient.get()
                .uri("/internal/organizations/hospitals/{hospitalId}/access-reference", hospitalId)
                .retrieve()
                .body(HospitalAccessReferenceResponse.class);
    }
}
