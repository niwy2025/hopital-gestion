package com.hopital.accounting.billing;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class BillingRequests {
    private BillingRequests() {}
    public record Service(UUID hospitalId,@NotBlank @Size(max=200) String name,@NotBlank String category,
        @Size(max=1000) String description) {}
    public record UpdateService(@NotBlank @Size(max=200) String name,@NotBlank String category,@Size(max=1000) String description,
        boolean active,@Min(0) int version,@NotBlank @Size(max=1000) String reason) {}
    public record Tariff(@NotNull LocalDate effectiveFrom,@NotBlank String currency,@NotBlank String unit,
        @NotNull @DecimalMin("0") @Digits(integer=14,fraction=2) BigDecimal unitPrice,@NotBlank @Size(max=1000) String reason) {}
    public record Charge(@NotNull UUID serviceId,@NotNull UUID tariffId,@NotNull LocalDate performedOn,
        @NotNull @DecimalMin(value="0",inclusive=false) @Digits(integer=6,fraction=3) BigDecimal quantity,
        @NotBlank String currency,@NotNull UUID requestId,@NotBlank @Size(max=1000) String note) {}
    public record Note(@NotBlank @Size(max=1000) String reason) {}
    public record InvoiceCharges(
            @NotEmpty @Size(max=100) List<@NotNull UUID> chargeIds,
            @NotNull UUID requestId,
            LocalDate issuedOn) {}
}
