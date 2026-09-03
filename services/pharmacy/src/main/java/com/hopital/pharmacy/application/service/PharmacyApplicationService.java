package com.hopital.pharmacy.application.service;

import com.hopital.pharmacy.application.domain.AccountingPostingStatus;
import com.hopital.pharmacy.application.domain.AuditActor;
import com.hopital.pharmacy.application.domain.Currency;
import com.hopital.pharmacy.application.domain.DataAccessScope;
import com.hopital.pharmacy.application.domain.StockMovementType;
import com.hopital.pharmacy.application.domain.StockMovementSourceType;
import com.hopital.pharmacy.application.dto.CreateMedicineRequest;
import com.hopital.pharmacy.application.dto.CreateStockIssueRequest;
import com.hopital.pharmacy.application.dto.CreateStockEntryRequest;
import com.hopital.pharmacy.application.dto.ExpiryTreatmentResponse;
import com.hopital.pharmacy.application.dto.MedicineResponse;
import com.hopital.pharmacy.application.dto.PageResponse;
import com.hopital.pharmacy.application.dto.PrescriptionStockDispenseItemRequest;
import com.hopital.pharmacy.application.dto.PrescriptionDispenseAccountingLineResponse;
import com.hopital.pharmacy.application.dto.PrescriptionDispenseAccountingReferenceResponse;
import com.hopital.pharmacy.application.dto.PrescriptionDispenseValuationResponse;
import com.hopital.pharmacy.application.dto.StockAvailabilityResponse;
import com.hopital.pharmacy.application.dto.StockBalanceResponse;
import com.hopital.pharmacy.application.dto.StockEntryResponse;
import com.hopital.pharmacy.application.dto.StockEntryAccountingReferenceResponse;
import com.hopital.pharmacy.application.dto.StockMovementAccountingReferenceResponse;
import com.hopital.pharmacy.application.dto.StockMovementResponse;
import com.hopital.pharmacy.application.exception.DataAccessDeniedException;
import com.hopital.pharmacy.application.exception.InvalidStockEntryException;
import com.hopital.pharmacy.application.exception.PharmacyResourceNotFoundException;
import com.hopital.pharmacy.infra.integration.organization.HospitalReferenceClient;
import com.hopital.pharmacy.infra.persistence.entity.HospitalStockEntity;
import com.hopital.pharmacy.infra.persistence.entity.MedicineEntity;
import com.hopital.pharmacy.infra.persistence.entity.StockEntryEntity;
import com.hopital.pharmacy.infra.persistence.entity.StockLotEntity;
import com.hopital.pharmacy.infra.persistence.entity.StockMovementEntity;
import com.hopital.pharmacy.infra.persistence.repository.HospitalStockRepository;
import com.hopital.pharmacy.infra.persistence.repository.MedicineRepository;
import com.hopital.pharmacy.infra.persistence.repository.StockEntryRepository;
import com.hopital.pharmacy.infra.persistence.repository.StockLotRepository;
import com.hopital.pharmacy.infra.persistence.repository.StockMovementRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PharmacyApplicationService {
    private final MedicineRepository medicineRepository;
    private final HospitalStockRepository hospitalStockRepository;
    private final StockEntryRepository stockEntryRepository;
    private final StockLotRepository stockLotRepository;
    private final StockMovementRepository stockMovementRepository;
    private final HospitalReferenceClient hospitalReferenceClient;
    private final StockEntryAccountingOutboxService stockEntryAccountingOutboxService;
    private final StockMovementAccountingOutboxService stockMovementAccountingOutboxService;

    public PharmacyApplicationService(MedicineRepository medicineRepository, HospitalStockRepository hospitalStockRepository,
            StockEntryRepository stockEntryRepository, StockLotRepository stockLotRepository,
            StockMovementRepository stockMovementRepository, HospitalReferenceClient hospitalReferenceClient,
            StockEntryAccountingOutboxService stockEntryAccountingOutboxService,
            StockMovementAccountingOutboxService stockMovementAccountingOutboxService) {
        this.medicineRepository = medicineRepository;
        this.hospitalStockRepository = hospitalStockRepository;
        this.stockEntryRepository = stockEntryRepository;
        this.stockLotRepository = stockLotRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.hospitalReferenceClient = hospitalReferenceClient;
        this.stockEntryAccountingOutboxService = stockEntryAccountingOutboxService;
        this.stockMovementAccountingOutboxService = stockMovementAccountingOutboxService;
    }

    public PageResponse<MedicineResponse> searchMedicines(int page, int size, String query, Boolean active) {
        var results = medicineRepository.search(normalize(query), active, pageable(page, size, "genericName"));
        return page(results.map(this::toMedicine));
    }

    @Transactional
    public MedicineResponse createMedicine(CreateMedicineRequest request, AuditActor actor) {
        MedicineEntity medicine = new MedicineEntity(UUID.randomUUID(), nextMedicineCode(), request.genericName().trim(),
                trimToNull(request.commercialName()), trimToNull(request.dosage()), trimToNull(request.pharmaceuticalForm()),
                trimToNull(request.presentation()), actor, Instant.now());
        return toMedicine(medicineRepository.save(medicine));
    }

    public PageResponse<StockBalanceResponse> searchStocks(int page, int size, String query, boolean lowStock, DataAccessScope scope) {
        String hospitalCode = scope.provinceWide() ? "" : requiredHospitalCode(scope);
        var results = hospitalStockRepository.search(hospitalCode, normalize(query), lowStock, pageable(page, size, "updatedAt"));
        return page(results.map(this::toStock));
    }

    public PageResponse<StockEntryResponse> searchStockEntries(int page, int size, String query,
            AccountingPostingStatus accountingStatus, DataAccessScope scope) {
        String hospitalCode = scope.provinceWide() ? "" : requiredHospitalCode(scope);
        var results = stockEntryRepository.search(hospitalCode, normalize(query), accountingStatus, pageable(page, size, "receivedAt"));
        return page(results.map(this::toEntry));
    }

    public StockAvailabilityResponse currentStock(UUID medicineId, DataAccessScope scope) {
        HospitalReferenceClient.HospitalReference hospital = resolveHospital(scope);
        return hospitalStockRepository.findByHospitalIdAndMedicine_Id(hospital.hospitalId(), medicineId)
                .map(stock -> new StockAvailabilityResponse(medicineId, true, toStock(stock)))
                .orElseGet(() -> new StockAvailabilityResponse(medicineId, false, null));
    }

    public PageResponse<StockMovementResponse> searchStockMovements(int page, int size, String query,
            UUID medicineId, StockMovementType type, DataAccessScope scope) {
        String hospitalCode = scope.provinceWide() ? "" : requiredHospitalCode(scope);
        var results = stockMovementRepository.search(hospitalCode, medicineId, type, normalize(query), pageable(page, size, "occurredAt"));
        return page(results.map(this::toMovement));
    }

    @Transactional
    public StockEntryResponse receiveStock(CreateStockEntryRequest request, DataAccessScope scope, AuditActor actor) {
        MedicineEntity medicine = medicineRepository.findById(request.medicineId())
                .orElseThrow(() -> new PharmacyResourceNotFoundException("Le médicament"));
        if (!medicine.isActive()) throw new InvalidStockEntryException("Ce médicament est inactif et ne peut pas recevoir de stock.");
        if (request.expiresOn() != null && !request.expiresOn().isAfter(LocalDate.now())) {
            throw new InvalidStockEntryException("La date de péremption doit être future.");
        }
        HospitalReferenceClient.HospitalReference hospital = resolveHospital(scope);
        Instant receivedAt = Instant.now();
        int reorderLevel = request.reorderLevel() == null ? 0 : request.reorderLevel();
        var existing = hospitalStockRepository.findByHospitalIdAndMedicine_Id(hospital.hospitalId(), medicine.getId());
        HospitalStockEntity stock;
        if (existing.isPresent()) {
            stock = existing.get();
            if (stock.getCurrency() != request.currency()) {
                throw new InvalidStockEntryException("La monnaie doit rester identique pour le stock de ce médicament dans cet hôpital.");
            }
            stock.receive(request.quantity(), request.unitCost(), request.unitSellingPrice(), reorderLevel, receivedAt);
        } else {
            stock = hospitalStockRepository.save(new HospitalStockEntity(UUID.randomUUID(), hospital.hospitalId(),
                    hospital.hospitalCode(), medicine, request.quantity(), reorderLevel, request.unitCost(), request.unitSellingPrice(), request.currency(), receivedAt));
        }
        StockEntryEntity entry = new StockEntryEntity(UUID.randomUUID(), nextStockEntryCode(), stock, request.quantity(),
                request.unitCost().setScale(2, RoundingMode.HALF_UP), request.unitSellingPrice().setScale(2, RoundingMode.HALF_UP), request.currency(), request.expiresOn(),
                trimToNull(request.supplierName()), trimToNull(request.notes()), actor, receivedAt);
        StockEntryEntity savedEntry = stockEntryRepository.save(entry);
        StockLotEntity lot = stockLotRepository.save(new StockLotEntity(UUID.randomUUID(), "LOT-" + savedEntry.getCode(), stock,
                savedEntry, request.quantity(), savedEntry.getUnitCost(), request.currency(), request.expiresOn(), receivedAt));
        stockMovementRepository.save(new StockMovementEntity(UUID.randomUUID(), nextStockMovementCode(), stock, lot,
                StockMovementType.ENTRY, request.quantity(), savedEntry.getUnitCost(), request.currency(), savedEntry.getNotes(), actor, receivedAt));
        // Durable in the same transaction as the receipt. The scheduler, not
        // the pharmacy user's HTTP request, performs the accounting call.
        stockEntryAccountingOutboxService.enqueue(savedEntry.getId(), savedEntry.getCode());
        return toEntry(savedEntry);
    }

    /**
     * Returns the immutable purchase value of one stock reception. This is
     * internal-only: accounting obtains the amount from the source record,
     * never from an outbox payload or a browser request.
     */
    public StockEntryAccountingReferenceResponse resolveStockEntryAccountingReference(String rawStockEntryCode) {
        String stockEntryCode = rawStockEntryCode == null ? "" : rawStockEntryCode.trim().toUpperCase(Locale.ROOT);
        StockEntryEntity entry = stockEntryRepository.findByCodeIgnoreCase(stockEntryCode)
                .orElseThrow(() -> new PharmacyResourceNotFoundException("L'entrée de stock"));
        return new StockEntryAccountingReferenceResponse(
                entry.getId(),
                entry.getCode(),
                entry.getHospitalId(),
                entry.getHospitalCode(),
                entry.getSupplierName(),
                entry.getTotalCost(),
                entry.getCurrency(),
                entry.getReceivedAt(),
                entry.getReceivedByUserId(),
                entry.getReceivedByUsername());
    }

    /**
     * Returns the immutable valuation of one stock-out movement. Accounting
     * never trusts a browser amount or an outbox payload for this operation.
     */
    public StockMovementAccountingReferenceResponse resolveStockMovementAccountingReference(
            String rawStockMovementCode) {
        String stockMovementCode = rawStockMovementCode == null
                ? ""
                : rawStockMovementCode.trim().toUpperCase(Locale.ROOT);
        StockMovementEntity movement = stockMovementRepository.findByCodeIgnoreCase(stockMovementCode)
                .orElseThrow(() -> new PharmacyResourceNotFoundException("Le mouvement de stock"));
        BigDecimal totalCost = movement.getUnitCost()
                .multiply(BigDecimal.valueOf(movement.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        return new StockMovementAccountingReferenceResponse(
                movement.getId(),
                movement.getCode(),
                movement.getType(),
                movement.getSourceType(),
                movement.getSourceCode(),
                movement.getHospitalId(),
                movement.getHospitalCode(),
                movement.getQuantity(),
                movement.getUnitCost(),
                totalCost,
                movement.getCurrency(),
                movement.getNotes(),
                movement.getOccurredAt(),
                movement.getPerformedByUserId(),
                movement.getPerformedByUsername());
    }

    @Transactional
    public StockMovementResponse issueStock(CreateStockIssueRequest request, DataAccessScope scope, AuditActor actor) {
        if (request.type() == StockMovementType.ENTRY || request.type() == StockMovementType.EXPIRY) {
            throw new InvalidStockEntryException("Ce type de mouvement ne peut pas être saisi manuellement.");
        }
        HospitalReferenceClient.HospitalReference hospital = resolveHospital(scope);
        return issueStockFromHospital(request.medicineId(), request.quantity(), request.type(), trimToNull(request.notes()), hospital, actor);
    }

    /** Registers the stock movements generated by one already validated patient dispense. */
    @Transactional
    public PrescriptionDispenseValuationResponse recordPrescriptionDispensation(
            UUID hospitalId,
            String dispenseCode,
            AuditActor actor,
            BigDecimal paidAmount,
            String paymentCurrency,
            java.util.List<PrescriptionStockDispenseItemRequest> items) {
        // The patient service can safely retry an internal HTTP call without
        // deducting the same stock twice. One delivery code is immutable.
        var existingMovements = stockMovementRepository.findAllBySourceTypeAndSourceCodeOrderByOccurredAtAsc(
                StockMovementSourceType.PRESCRIPTION_DISPENSE,
                dispenseCode);
        if (!existingMovements.isEmpty()) {
            return valuationFromMovements(dispenseCode, existingMovements);
        }
        HospitalReferenceClient.HospitalReference hospital = hospitalReferenceClient.resolveActive(hospitalId);
        Currency expectedCurrency = Currency.valueOf(paymentCurrency);
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PrescriptionStockDispenseItemRequest item : items) {
            HospitalStockEntity stock = hospitalStockRepository.findByHospitalIdAndMedicine_Id(hospital.hospitalId(), item.medicineId())
                    .orElseThrow(() -> new InvalidStockEntryException("Ce médicament ne possède aucun stock dans votre hôpital."));
            if (stock.getCurrency() != expectedCurrency) {
                throw new InvalidStockEntryException(
                        "La devise du paiement doit correspondre à la devise du stock délivré.");
            }
            totalAmount = totalAmount.add(stock.getUnitSellingPrice().multiply(BigDecimal.valueOf(item.quantity())));
        }
        if (paidAmount.compareTo(totalAmount) > 0) {
            throw new InvalidStockEntryException(
                    "Le montant encaissé ne peut pas dépasser le montant facturé par le stock délivré.");
        }
        for (PrescriptionStockDispenseItemRequest item : items) {
            issueStockFromHospital(
                    item.medicineId(),
                    item.quantity(),
                    StockMovementType.DISPENSING,
                    "Délivrance pharmacie " + dispenseCode,
                    hospital,
                    actor,
                    StockMovementSourceType.PRESCRIPTION_DISPENSE,
                    dispenseCode);
        }
        return new PrescriptionDispenseValuationResponse(
                dispenseCode,
                totalAmount.setScale(2, RoundingMode.HALF_UP),
                expectedCurrency);
    }

    /**
     * Returns the immutable cost source of a prescription delivery for the
     * accounting service. This endpoint is internal-only and does not expose
     * the pharmacy catalogue to the browser.
     */
    public PrescriptionDispenseAccountingReferenceResponse resolvePrescriptionDispenseAccountingReference(
            String dispenseCode) {
        var movements = stockMovementRepository.findAllBySourceTypeAndSourceCodeOrderByOccurredAtAsc(
                StockMovementSourceType.PRESCRIPTION_DISPENSE,
                dispenseCode);
        if (movements.isEmpty()) {
            throw new PharmacyResourceNotFoundException("La délivrance de pharmacie");
        }
        StockMovementEntity firstMovement = movements.getFirst();
        BigDecimal totalCost = movements.stream()
                .map(movement -> movement.getUnitCost().multiply(BigDecimal.valueOf(movement.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var lines = movements.stream()
                .map(movement -> new PrescriptionDispenseAccountingLineResponse(
                        movement.getId(),
                        movement.getCode(),
                        movement.getStock().getMedicine().getId(),
                        movement.getStock().getMedicine().getCode(),
                        movement.getStock().getMedicine().getGenericName(),
                        movement.getQuantity(),
                        movement.getUnitCost(),
                        movement.getUnitCost().multiply(BigDecimal.valueOf(movement.getQuantity())),
                        movement.getCurrency(),
                        movement.getOccurredAt()))
                .toList();
        return new PrescriptionDispenseAccountingReferenceResponse(
                dispenseCode,
                firstMovement.getStock().getHospitalId(),
                firstMovement.getStock().getHospitalCode(),
                totalCost,
                firstMovement.getCurrency(),
                firstMovement.getOccurredAt(),
                lines);
    }

    private StockMovementResponse issueStockFromHospital(
            UUID medicineId,
            int quantity,
            StockMovementType type,
            String notes,
            HospitalReferenceClient.HospitalReference hospital,
            AuditActor actor) {
        return issueStockFromHospital(medicineId, quantity, type, notes, hospital, actor, null, null);
    }

    private StockMovementResponse issueStockFromHospital(
            UUID medicineId,
            int quantity,
            StockMovementType type,
            String notes,
            HospitalReferenceClient.HospitalReference hospital,
            AuditActor actor,
            StockMovementSourceType sourceType,
            String sourceCode) {
        HospitalStockEntity stock = hospitalStockRepository.findByHospitalIdAndMedicine_Id(hospital.hospitalId(), medicineId)
                .orElseThrow(() -> new InvalidStockEntryException("Ce médicament ne possède aucun stock dans votre hôpital."));
        Instant occurredAt = Instant.now();
        var usableLots = stockLotRepository.findUsableByStock(stock.getId(), LocalDate.now());
        int availableQuantity = usableLots.stream().mapToInt(StockLotEntity::getRemainingQuantity).sum();
        if (quantity > availableQuantity) {
            throw new InvalidStockEntryException("La quantité demandée dépasse le stock disponible hors produits périmés.");
        }
        int remainingToIssue = quantity;
        BigDecimal issuedValue = BigDecimal.ZERO;
        for (StockLotEntity lot : usableLots) {
            int consumed = Math.min(remainingToIssue, lot.getRemainingQuantity());
            if (consumed == 0) continue;
            lot.consume(consumed);
            issuedValue = issuedValue.add(lot.getUnitCost().multiply(BigDecimal.valueOf(consumed)));
            remainingToIssue -= consumed;
            if (remainingToIssue == 0) break;
        }
        stock.issue(quantity, occurredAt);
        BigDecimal unitCost = issuedValue.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);
        StockMovementEntity movement = new StockMovementEntity(UUID.randomUUID(), nextStockMovementCode(), stock, null,
                type, sourceType, sourceCode, quantity, unitCost,
                type == StockMovementType.DISPENSING ? stock.getUnitSellingPrice() : null,
                stock.getCurrency(), notes, actor, occurredAt);
        StockMovementEntity savedMovement = stockMovementRepository.save(movement);
        // Prescription deliveries are synchronised by the patient outbox. All
        // other irreversible stock-outs receive their own durable accounting
        // intent without delaying the pharmacy operation.
        stockMovementAccountingOutboxService.enqueueIfRequired(savedMovement);
        return toMovement(savedMovement);
    }

    private PrescriptionDispenseValuationResponse valuationFromMovements(
            String dispenseCode,
            java.util.List<StockMovementEntity> movements) {
        Currency currency = movements.getFirst().getCurrency();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (StockMovementEntity movement : movements) {
            if (movement.getCurrency() != currency || movement.getUnitSellingPrice() == null) {
                throw new InvalidStockEntryException(
                        "La valeur de vente historique de cette délivrance ne peut pas être déterminée.");
            }
            totalAmount = totalAmount.add(
                    movement.getUnitSellingPrice().multiply(BigDecimal.valueOf(movement.getQuantity())));
        }
        return new PrescriptionDispenseValuationResponse(
                dispenseCode,
                totalAmount.setScale(2, RoundingMode.HALF_UP),
                currency);
    }

    @Transactional
    public ExpiryTreatmentResponse processExpiredStock(DataAccessScope scope, AuditActor actor) {
        String hospitalCode = scope.provinceWide() ? "" : requiredHospitalCode(scope);
        int lotsProcessed = 0;
        int quantityProcessed = 0;
        Instant occurredAt = Instant.now();
        for (StockLotEntity lot : stockLotRepository.findExpired(hospitalCode, LocalDate.now())) {
            int expiredQuantity = lot.getRemainingQuantity();
            if (expiredQuantity == 0) continue;
            HospitalStockEntity stock = lot.getStock();
            lot.consume(expiredQuantity);
            stock.issue(expiredQuantity, occurredAt);
            StockMovementEntity movement = stockMovementRepository.save(new StockMovementEntity(UUID.randomUUID(), nextStockMovementCode(), stock, lot,
                    StockMovementType.EXPIRY, expiredQuantity, lot.getUnitCost(), lot.getCurrency(),
                    "Sortie automatique : lot périmé" + (lot.getExpiresOn() == null ? "" : " le " + lot.getExpiresOn()), actor, occurredAt));
            stockMovementAccountingOutboxService.enqueueIfRequired(movement);
            lotsProcessed++;
            quantityProcessed += expiredQuantity;
        }
        return new ExpiryTreatmentResponse(lotsProcessed, quantityProcessed);
    }

    private HospitalReferenceClient.HospitalReference resolveHospital(DataAccessScope scope) {
        if (scope.hospitalId() == null || scope.hospitalCode() == null) {
            throw new DataAccessDeniedException();
        }
        return hospitalReferenceClient.resolveActive(scope.hospitalId());
    }

    private String nextMedicineCode() { return nextCode("MED", medicineRepository::existsByCodeIgnoreCase); }
    private String nextStockEntryCode() { return nextCode("ENT", stockEntryRepository::existsByCodeIgnoreCase); }
    private String nextStockMovementCode() { return nextCode("MVT", stockMovementRepository::existsByCodeIgnoreCase); }
    private String nextCode(String prefix, java.util.function.Predicate<String> exists) {
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = prefix + "-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            if (!exists.test(code)) return code;
        }
        throw new IllegalStateException("Impossible de générer un code unique.");
    }
    private <T> PageResponse<T> page(org.springframework.data.domain.Page<T> source) {
        return new PageResponse<>(source.getContent(), source.getNumber(), source.getSize(), source.getTotalElements(), source.getTotalPages());
    }
    private PageRequest pageable(int page, int size, String sort) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(sort).descending());
    }
    private String normalize(String value) { return value == null ? "" : value.trim(); }
    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String requiredHospitalCode(DataAccessScope scope) {
        if (scope.hospitalCode() == null || scope.hospitalCode().isBlank()) throw new DataAccessDeniedException();
        return scope.hospitalCode();
    }
    private MedicineResponse toMedicine(MedicineEntity item) {
        return new MedicineResponse(item.getId(), item.getCode(), item.getGenericName(), item.getCommercialName(), item.getDosage(),
                item.getPharmaceuticalForm(), item.getPresentation(), item.isActive(), item.getCreatedAt(), item.getCreatedByUsername());
    }
    private StockBalanceResponse toStock(HospitalStockEntity item) {
        MedicineEntity medicine = item.getMedicine();
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(90);
        var lots = stockLotRepository.findByStock_IdAndRemainingQuantityGreaterThan(item.getId(), 0);
        int expiredQuantity = lots.stream().filter(lot -> lot.getExpiresOn() != null && lot.getExpiresOn().isBefore(today))
                .mapToInt(StockLotEntity::getRemainingQuantity).sum();
        int expiringQuantity = lots.stream().filter(lot -> lot.getExpiresOn() != null
                && !lot.getExpiresOn().isBefore(today) && !lot.getExpiresOn().isAfter(warningDate))
                .mapToInt(StockLotEntity::getRemainingQuantity).sum();
        LocalDate nearestExpiry = lots.stream().map(StockLotEntity::getExpiresOn).filter(java.util.Objects::nonNull)
                .min(LocalDate::compareTo).orElse(null);
        int availableQuantity = Math.max(0, item.getQuantity() - expiredQuantity);
        return new StockBalanceResponse(item.getId(), item.getHospitalId(), item.getHospitalCode(), medicine.getId(), medicine.getCode(),
                medicine.getGenericName(), medicine.getCommercialName(), medicine.getDosage(), medicine.getPharmaceuticalForm(), item.getQuantity(),
                availableQuantity, expiredQuantity, expiringQuantity, nearestExpiry, item.getReorderLevel(), item.getAverageUnitCost(),
                item.getUnitSellingPrice(),
                item.getCurrency(), availableQuantity <= item.getReorderLevel(), item.getUpdatedAt());
    }
    private StockEntryResponse toEntry(StockEntryEntity item) {
        MedicineEntity medicine = item.getMedicine();
        return new StockEntryResponse(item.getId(), item.getCode(), item.getHospitalId(), item.getHospitalCode(), medicine.getId(), medicine.getCode(),
                medicine.getGenericName(), item.getQuantity(), item.getUnitCost(), item.getUnitSellingPrice(), item.getTotalCost(), item.getCurrency(), item.getExpiresOn(),
                item.getSupplierName(), item.getNotes(), item.getAccountingStatus(), item.getAccountingEntryReference(), item.getReceivedAt(), item.getReceivedByUsername());
    }
    private StockMovementResponse toMovement(StockMovementEntity item) {
        MedicineEntity medicine = item.getStock().getMedicine();
        StockLotEntity lot = item.getStockLot();
        return new StockMovementResponse(item.getId(), item.getCode(), item.getType(), item.getStock().getId(), item.getStock().getHospitalId(),
                item.getStock().getHospitalCode(), medicine.getId(), medicine.getCode(), medicine.getGenericName(), lot == null ? null : lot.getCode(),
                item.getQuantity(), item.getUnitCost(), item.getCurrency(), lot == null ? null : lot.getExpiresOn(), item.getNotes(),
                item.getOccurredAt(), item.getPerformedByUsername());
    }
}
