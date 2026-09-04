package com.hopital.patient.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hopital.patient.application.domain.AccountingSynchronizationStatus;
import com.hopital.patient.application.domain.PaymentCurrency;
import com.hopital.patient.application.domain.PharmacyDispenseAccountingInvoiceStatus;
import com.hopital.patient.application.domain.PrescriptionDispenseCompletion;
import com.hopital.patient.application.domain.PrescriptionPaymentMethod;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PrescriptionDispenseResponseContractTest {

    private final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @Test
    void serializesAccountingProjectionWithThePortalFieldNames() throws Exception {
        PrescriptionDispenseResponse response = new PrescriptionDispenseResponse(
                UUID.fromString("11111111-1111-4111-8111-111111111111"),
                "DSP-001",
                PrescriptionDispenseCompletion.COMPLETE,
                new BigDecimal("15000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("10000.00"),
                PaymentCurrency.CDF,
                PrescriptionPaymentMethod.CASH,
                AccountingSynchronizationStatus.SYNCHRONIZED,
                UUID.fromString("22222222-2222-4222-8222-222222222222"),
                "FAC-001",
                new BigDecimal("15000.00"),
                new BigDecimal("15000.00"),
                BigDecimal.ZERO.setScale(2),
                PaymentCurrency.CDF,
                PharmacyDispenseAccountingInvoiceStatus.PAID,
                2L,
                Instant.parse("2026-09-03T14:00:00Z"),
                "REC-001",
                null,
                Instant.parse("2026-09-03T13:00:00Z"),
                "pharmacien",
                List.of());

        JsonNode json = mapper.valueToTree(response);

        assertThat(json.path("accountingSyncStatus").asText()).isEqualTo("SYNCHRONIZED");
        assertThat(json.path("accountingInvoiceStatus").asText()).isEqualTo("PAID");
        assertThat(json.has("accountingSynchronizationStatus")).isFalse();
        assertThat(json.has("accountingStatus")).isFalse();
    }

    @Test
    void stillReadsThePreviousAccountingFieldNames() throws Exception {
        PrescriptionDispenseResponse response = mapper.readValue(
                """
                {
                  "id": "11111111-1111-4111-8111-111111111111",
                  "code": "DSP-001",
                  "completion": "COMPLETE",
                  "totalAmount": 15000.00,
                  "paidAmount": 5000.00,
                  "dueAmount": 10000.00,
                  "currency": "CDF",
                  "paymentMethod": "CASH",
                  "accountingSynchronizationStatus": "PENDING",
                  "accountingInvoiceId": null,
                  "accountingInvoiceCode": null,
                  "accountingTotalAmount": null,
                  "accountingPaidAmount": null,
                  "accountingDueAmount": null,
                  "accountingCurrency": null,
                  "accountingStatus": "ISSUED",
                  "accountingStateVersion": null,
                  "accountingSynchronizedAt": null,
                  "accountingLastPaymentReference": null,
                  "notes": null,
                  "dispensedAt": "2026-09-03T13:00:00Z",
                  "dispensedByUsername": "pharmacien",
                  "items": []
                }
                """,
                PrescriptionDispenseResponse.class);

        assertThat(response.accountingSyncStatus()).isEqualTo(AccountingSynchronizationStatus.PENDING);
        assertThat(response.accountingInvoiceStatus()).isEqualTo(PharmacyDispenseAccountingInvoiceStatus.ISSUED);
    }
}
