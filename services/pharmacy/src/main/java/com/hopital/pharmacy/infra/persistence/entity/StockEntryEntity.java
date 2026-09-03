package com.hopital.pharmacy.infra.persistence.entity;

import com.hopital.pharmacy.application.domain.AccountingPostingStatus;
import com.hopital.pharmacy.application.domain.AuditActor;
import com.hopital.pharmacy.application.domain.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "stock_entries")
public class StockEntryEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 30) private String code;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "stock_id", nullable = false) private HospitalStockEntity stock;
    @Column(name = "hospital_id", nullable = false) private UUID hospitalId;
    @Column(name = "hospital_code", nullable = false, length = 30) private String hospitalCode;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "medicine_id", nullable = false) private MedicineEntity medicine;
    @Column(nullable = false) private int quantity;
    @Column(name = "unit_cost", nullable = false, precision = 18, scale = 2) private BigDecimal unitCost;
    @Column(name = "unit_selling_price", nullable = false, precision = 18, scale = 2) private BigDecimal unitSellingPrice;
    @Column(name = "total_cost", nullable = false, precision = 18, scale = 2) private BigDecimal totalCost;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 3) private Currency currency;
    @Column(name = "expires_on") private LocalDate expiresOn;
    @Column(name = "supplier_name", length = 200) private String supplierName;
    @Column(length = 2000) private String notes;
    @Enumerated(EnumType.STRING) @Column(name = "accounting_status", nullable = false, length = 30) private AccountingPostingStatus accountingStatus;
    @Column(name = "accounting_entry_reference", length = 80) private String accountingEntryReference;
    @Column(name = "received_at", nullable = false) private Instant receivedAt;
    @Column(name = "received_by_user_id", nullable = false, length = 100) private String receivedByUserId;
    @Column(name = "received_by_username", nullable = false, length = 150) private String receivedByUsername;

    protected StockEntryEntity() { }

    public StockEntryEntity(UUID id, String code, HospitalStockEntity stock, int quantity, BigDecimal unitCost, BigDecimal unitSellingPrice,
            Currency currency, LocalDate expiresOn, String supplierName, String notes, AuditActor actor, Instant receivedAt) {
        this.id = id; this.code = code; this.stock = stock; this.hospitalId = stock.getHospitalId(); this.hospitalCode = stock.getHospitalCode();
        this.medicine = stock.getMedicine(); this.quantity = quantity; this.unitCost = unitCost; this.unitSellingPrice = unitSellingPrice; this.totalCost = unitCost.multiply(BigDecimal.valueOf(quantity));
        this.currency = currency; this.expiresOn = expiresOn; this.supplierName = supplierName; this.notes = notes;
        this.accountingStatus = AccountingPostingStatus.PENDING_ACCOUNTING; this.receivedAt = receivedAt;
        this.receivedByUserId = actor.userId(); this.receivedByUsername = actor.username();
    }

    public UUID getId() { return id; } public String getCode() { return code; } public UUID getHospitalId() { return hospitalId; }
    public String getHospitalCode() { return hospitalCode; } public MedicineEntity getMedicine() { return medicine; } public int getQuantity() { return quantity; }
    public BigDecimal getUnitCost() { return unitCost; } public BigDecimal getUnitSellingPrice() { return unitSellingPrice; } public BigDecimal getTotalCost() { return totalCost; } public Currency getCurrency() { return currency; }
    public LocalDate getExpiresOn() { return expiresOn; } public String getSupplierName() { return supplierName; } public String getNotes() { return notes; }
    public AccountingPostingStatus getAccountingStatus() { return accountingStatus; } public Instant getReceivedAt() { return receivedAt; }
    public String getReceivedByUsername() { return receivedByUsername; }
    public String getReceivedByUserId() { return receivedByUserId; }
    public String getAccountingEntryReference() { return accountingEntryReference; }
    public void markAccountingPosted(String entryReference) {
        this.accountingStatus = AccountingPostingStatus.POSTED;
        this.accountingEntryReference = entryReference;
    }
}
