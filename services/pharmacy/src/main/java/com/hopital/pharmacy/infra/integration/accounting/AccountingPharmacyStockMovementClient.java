package com.hopital.pharmacy.infra.integration.accounting;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Private transport for stock-out accounting; it is never exposed to a browser. */
@Component
public class AccountingPharmacyStockMovementClient {

    private final RestClient restClient;

    public AccountingPharmacyStockMovementClient(
            RestClient.Builder builder,
            @Value("${hospital.accounting-service.base-url}") String accountingServiceBaseUrl,
            @Value("${hospital.accounting-service.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${hospital.accounting-service.read-timeout-ms:5000}") long readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(Math.max(1, connectTimeoutMs)));
        requestFactory.setReadTimeout(Duration.ofMillis(Math.max(1, readTimeoutMs)));
        this.restClient = builder.baseUrl(accountingServiceBaseUrl).requestFactory(requestFactory).build();
    }

    /** The accounting side is idempotent by immutable stock movement code. */
    public AccountingPostingAcknowledgement postStockMovement(String stockMovementCode) {
        AccountingPostingAcknowledgement response = restClient.post()
                .uri("/internal/accounting/pharmacy-stock-movements")
                .body(new PharmacyStockMovementAccountingRequest(stockMovementCode))
                .retrieve()
                .body(AccountingPostingAcknowledgement.class);
        if (response == null) {
            throw new IllegalStateException("Le service Comptabilité n'a pas accusé réception de la sortie de stock.");
        }
        if (!response.ignored() && (response.accountingEntryReference() == null
                || response.accountingEntryReference().isBlank())) {
            throw new IllegalStateException("Le service Comptabilité n'a pas renvoyé la référence de l'écriture de sortie de stock.");
        }
        return response;
    }

    private record PharmacyStockMovementAccountingRequest(String stockMovementCode) {
    }

    public record AccountingPostingAcknowledgement(
            @JsonAlias({"accountingEntryCode", "entryCode", "reference", "code"}) String accountingEntryReference,
            boolean ignored,
            @JsonAlias({"ignoredReason", "reason", "message"}) String ignoredReason) {
    }
}
