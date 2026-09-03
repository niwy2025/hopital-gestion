package com.hopital.patient.infra.integration.accounting;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Private patient-to-accounting transport; it is never called by a browser. */
@Component
public class AccountingPharmacyDispenseClient {

    private final RestClient restClient;

    public AccountingPharmacyDispenseClient(
            RestClient.Builder builder,
            @Value("${hospital.accounting-service.base-url}") String accountingServiceBaseUrl,
            @Value("${hospital.accounting-service.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${hospital.accounting-service.read-timeout-ms:5000}") long readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(Math.max(1, connectTimeoutMs)));
        requestFactory.setReadTimeout(Duration.ofMillis(Math.max(1, readTimeoutMs)));
        this.restClient = builder.baseUrl(accountingServiceBaseUrl).requestFactory(requestFactory).build();
    }

    /**
     * The accounting endpoint is idempotent by {@code dispenseCode}: a retry
     * must return the same invoice reference rather than creating another
     * accounting document.
     */
    public String postDispense(String dispenseCode) {
        AccountingInvoiceReferenceResponse response = restClient.post()
                .uri("/internal/accounting/pharmacy-dispensations")
                .body(new AccountingPharmacyDispenseRequest(dispenseCode))
                .retrieve()
                .body(AccountingInvoiceReferenceResponse.class);
        if (response == null || response.invoiceReference() == null || response.invoiceReference().isBlank()) {
            throw new IllegalStateException("Le service Comptabilité n'a pas renvoyé la référence de la facture.");
        }
        return response.invoiceReference().trim();
    }

    private record AccountingPharmacyDispenseRequest(String dispenseCode) {
    }

    /**
     * Accept common explicit field names while the two services are deployed
     * independently. The accounting API publishes `invoiceReference`.
     */
    private record AccountingInvoiceReferenceResponse(
            @JsonAlias({"invoiceCode", "invoiceNumber", "reference", "code"}) String invoiceReference) {
    }
}
