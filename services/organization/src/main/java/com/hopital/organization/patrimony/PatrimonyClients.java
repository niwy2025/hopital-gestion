package com.hopital.organization.patrimony;

import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Component
public class PatrimonyClients {
    private final RestClient auth;
    private final RestClient patients;
    private final RestClient personnel;
    private final RestClient accounting;
    public PatrimonyClients(RestClient.Builder builder,
            @Value("${hospital.auth-service.base-url:http://auth-service:8081}") String authUrl,
            @Value("${hospital.patient-service.base-url:http://patient-service:8086}") String patientUrl,
            @Value("${hospital.personnel-service.base-url:http://personnel-service:8087}") String personnelUrl,
            @Value("${hospital.accounting-service.base-url:http://accounting-service:8090}") String accountingUrl) {
        var factory=new SimpleClientHttpRequestFactory(); factory.setConnectTimeout(2000); factory.setReadTimeout(5000);
        builder=builder.clone().requestFactory(factory);
        auth = builder.clone().baseUrl(authUrl).build(); patients = builder.clone().baseUrl(patientUrl).build();
        personnel = builder.clone().baseUrl(personnelUrl).build(); accounting = builder.clone().baseUrl(accountingUrl).build();
    }
    public PatrimonyScope scope(Jwt jwt) {
        var response = auth.get().uri("/internal/auth/access-scopes/{username}", jwt.getClaimAsString("preferred_username"))
                .retrieve().body(Scope.class);
        if (response == null) throw new IllegalStateException("Périmètre indisponible");
        return new PatrimonyScope(response.administrator(), response.laboratoryCode()==null?response.hospitalId():null, response.roleCodes(), jwt.getSubject(), jwt.getClaimAsString("preferred_username"));
    }
    public Passage passage(UUID id) {
        var response = patients.get().uri("/internal/patients/passages/{id}/laboratory-reference", id).retrieve().body(Passage.class);
        if (response == null) throw new IllegalStateException("Passage indisponible");
        return response;
    }
    public void sendAsset(UUID id) {
        accounting.post().uri("/internal/accounting/patrimony-assets/{id}", id).retrieve().toBodilessEntity();
    }
    public Personnel personnel(UUID id, UUID hospital) {
        var response = personnel.get().uri("/internal/personnel/{id}/hospitals/{hospital}/care-reference", id, hospital)
                .retrieve().body(Personnel.class);
        if (response == null) throw new IllegalStateException("Personnel indisponible");
        return response;
    }
    public record Personnel(UUID id, String firstName, String lastName, String middleName) { }
    public record Scope(boolean administrator, UUID hospitalId, Set<String> roleCodes, String laboratoryCode) { }
    public record Passage(UUID passageId, String passageCode, String patientCode, UUID hospitalId, String status) { }
}
