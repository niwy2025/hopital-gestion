package com.hopital.organization.patrimony;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class PatrimonyRequests {
    private PatrimonyRequests() { }
    public record Location(UUID hospitalId, UUID parentId, @NotBlank String kind, @NotBlank @Size(max=200) String name,
            @Size(max=150) String serviceName) { }
    public record Asset(UUID hospitalId, @NotBlank String categoryCode, @NotBlank @Size(max=200) String name,
            @Size(max=100) String brand, @Size(max=100) String model, @Size(max=150) String serialNumber, UUID locationId,
            @NotBlank String acquisitionSource, @Size(max=200) String ownerName, @Size(max=200) String supplierName,
            @NotNull LocalDate receivedOn, LocalDate commissionedOn, @DecimalMin("0") @Digits(integer=17,fraction=2) BigDecimal purchaseCost,
            @NotBlank String currency, LocalDate warrantyUntil, LocalDate nextMaintenanceOn, @Size(max=4000) String notes,
            Integer version) { }
    public record Movement(@NotBlank String kind, UUID locationId, @Size(max=200) String recipient, LocalDate dueOn,
            @NotBlank @Size(max=2000) String note) { }
    public record Operation(@NotBlank String kind, @NotBlank @Size(max=2000) String reason, LocalDate plannedOn) { }
    public record CompleteOperation(@NotBlank String decision, @NotBlank @Size(max=4000) String note,
            @DecimalMin("0") @Digits(integer=17,fraction=2) BigDecimal cost, LocalDate nextMaintenanceOn, boolean operational) { }
    public record Document(@NotBlank String kind, @NotBlank @Size(max=200) String fileName,
            @NotBlank String contentType, @NotBlank @Size(max=4300000) String base64) { }
    public record Bed(@NotNull UUID passageId, boolean reserved, @Size(max=1000) String note) { }
    public record Note(@NotBlank @Size(max=1000) String note) { }
    public record Assignment(UUID personnelId, @NotBlank @Size(max=2000) String note) { }
    public record LocationStatus(boolean active) { }
}
