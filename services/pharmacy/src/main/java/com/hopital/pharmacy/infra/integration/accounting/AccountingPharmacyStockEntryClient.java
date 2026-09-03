package com.hopital.pharmacy.infra.integration.accounting;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Private pharmacy-to-accounting transport; it is never called by a browser. */
@Component
public class AccountingPharmacyStockEntryClient {

    private final RestClient restClient;

    public AccountingPharmacyStockEntryClient(
            RestClient.Builder builder,
            @Value("${hospital.accounting-service.base-url}") String accountingServiceBaseUrl,
            @Value("${hospital.accounting-service.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${hospital.accounting-service.read-timeout-ms:5000}") long readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(Math.max(1, connectTimeoutMs)));
        requestFactory.setReadTimeout(Duration.ofMillis(Math.max(1, readTimeoutMs)));
        this.restClient = builder.baseUrl(accountingServiceBaseUrl).requestFactory(requestFactory).build();
    }

    /** Accounting is idempotent by stock entry code. */
    public String postStockEntry(String stockEntryCode) {
        AccountingEntryReferenceResponse response = restClient.post()
                .uri("/internal/accounting/pharmacy-stock-entries")
                .body(new PharmacyStockEntryAccountingRequest(stockEntryCode))
                .retrieve()
                .body(AccountingEntryReferenceResponse.class);
        if (response == null || response.accountingEntryReference() == null || response.accountingEntryReference().isBlank()) {
            throw new IllegalStateException("Le service Comptabilité n'a pas renvoyé la référence de l'écriture de stock.");
        }
        return response.accountingEntryReference().trim();
    }

    private record PharmacyStockEntryAccountingRequest(String stockEntryCode) {
    }

    private record AccountingEntryReferenceResponse(
            @JsonAlias({"accountingEntryCode", "entryCode", "reference", "code"}) String accountingEntryReference) {
    }
}
