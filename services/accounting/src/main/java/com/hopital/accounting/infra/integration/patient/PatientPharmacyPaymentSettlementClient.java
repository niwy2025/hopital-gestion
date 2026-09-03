package com.hopital.accounting.infra.integration.patient;

import com.hopital.accounting.infra.persistence.entity.PharmacyPaymentSettlementOutboxEventEntity;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Private accounting-to-patient transport. It is never exposed to a browser. */
@Component
public class PatientPharmacyPaymentSettlementClient {

    private final RestClient patientClient;

    public PatientPharmacyPaymentSettlementClient(
            RestClient.Builder builder,
            @Value("${hospital.patient-service.base-url}") String patientServiceBaseUrl,
            @Value("${hospital.patient-service.payment-settlement-outbox.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${hospital.patient-service.payment-settlement-outbox.read-timeout-ms:5000}") long readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(Math.max(1, connectTimeoutMs)));
        requestFactory.setReadTimeout(Duration.ofMillis(Math.max(1, readTimeoutMs)));
        patientClient = builder.baseUrl(patientServiceBaseUrl).requestFactory(requestFactory).build();
    }

    public void postSettlement(PharmacyPaymentSettlementOutboxEventEntity event) {
        patientClient.post()
                .uri("/internal/patients/pharmacy-dispensations/{dispenseCode}/payment-settlements", event.getDispenseCode())
                .body(new PharmacyPaymentSettlementRequest(
                        event.getId(),
                        event.getEventType().name(),
                        event.getPaymentId(),
                        event.getInvoiceId(),
                        event.getInvoiceCode(),
                        event.getTotalAmount(),
                        event.getPaidAmount(),
                        event.getDueAmount(),
                        event.getCurrency().name(),
                        event.getInvoiceStatus().name(),
                        event.getStateVersion(),
                        event.getPaidOn(),
                        event.getPaymentReference()))
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * `eventId` is the receiver's idempotency key. `stateVersion` protects
     * against an old retry arriving after a more recent payment projection.
     */
    public record PharmacyPaymentSettlementRequest(
            UUID eventId,
            String eventType,
            UUID paymentId,
            UUID invoiceId,
            String invoiceCode,
            java.math.BigDecimal totalAmount,
            java.math.BigDecimal paidAmount,
            java.math.BigDecimal dueAmount,
            String currency,
            String status,
            int stateVersion,
            LocalDate paidOn,
            String paymentReference) {
    }
}
