package com.hopital.auth.infra.integration.personnel;

import com.hopital.auth.application.config.AuthServiceProperties;
import com.hopital.auth.application.dto.PersonnelAccessScopeResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PersonnelAccessClient {

    private final RestClient personnelClient;

    public PersonnelAccessClient(RestClient.Builder builder, AuthServiceProperties properties) {
        this.personnelClient = builder.baseUrl(properties.personnelServiceBaseUrl()).build();
    }

    public PersonnelAccessScopeResponse resolveActiveScope(String accountId) {
        return personnelClient.get()
                .uri("/internal/personnel/accounts/{accountId}/access-scope", UUID.fromString(accountId))
                .retrieve()
                .body(PersonnelAccessScopeResponse.class);
    }
}
