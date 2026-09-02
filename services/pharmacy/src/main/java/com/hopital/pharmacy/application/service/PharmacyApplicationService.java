package com.hopital.pharmacy.application.service;

import com.hopital.pharmacy.application.domain.AccountingPostingStatus;
import com.hopital.pharmacy.application.domain.AuditActor;
import com.hopital.pharmacy.application.domain.DataAccessScope;
import com.hopital.pharmacy.application.dto.CreateMedicineRequest;
import com.hopital.pharmacy.application.dto.CreateStockEntryRequest;
import com.hopital.pharmacy.application.dto.MedicineResponse;
import com.hopital.pharmacy.application.dto.PageResponse;
import com.hopital.pharmacy.application.dto.StockBalanceResponse;
import com.hopital.pharmacy.application.dto.StockEntryResponse;
import com.hopital.pharmacy.application.exception.DataAccessDeniedException;
import com.hopital.pharmacy.application.exception.InvalidStockEntryException;
import com.hopital.pharmacy.application.exception.PharmacyResourceNotFoundException;
import com.hopital.pharmacy.infra.integration.organization.HospitalReferenceClient;
import com.hopital.pharmacy.infra.persistence.entity.HospitalStockEntity;
import com.hopital.pharmacy.infra.persistence.entity.MedicineEntity;
import com.hopital.pharmacy.infra.persistence.entity.StockEntryEntity;
import com.hopital.pharmacy.infra.persistence.repository.HospitalStockRepository;
import com.hopital.pharmacy.infra.persistence.repository.MedicineRepository;
import com.hopital.pharmacy.infra.persistence.repository.StockEntryRepository;
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
    private final HospitalReferenceClient hospitalReferenceClient;

    public PharmacyApplicationService(MedicineRepository medicineRepository, HospitalStockRepository hospitalStockRepository,
            StockEntryRepository stockEntryRepository, HospitalReferenceClient hospitalReferenceClient) {
        this.medicineRepository = medicineRepository;
        this.hospitalStockRepository = hospitalStockRepository;
        this.stockEntryRepository = stockEntryRepository;
        this.hospitalReferenceClient = hospitalReferenceClient;
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

    @Transactional
    public StockEntryResponse receiveStock(CreateStockEntryRequest request, DataAccessScope scope, AuditActor actor) {
        MedicineEntity medicine = medicineRepository.findById(request.medicineId())
                .orElseThrow(() -> new PharmacyResourceNotFoundException("Le médicament"));
        if (!medicine.isActive()) throw new InvalidStockEntryException("Ce médicament est inactif et ne peut pas recevoir de stock.");
        if (request.expiresOn() != null && !request.expiresOn().isAfter(LocalDate.now())) {
            throw new InvalidStockEntryException("La date de péremption doit être future.");
        }
        HospitalReferenceClient.HospitalReference hospital = resolveHospital(request.hospitalId(), scope);
        Instant receivedAt = Instant.now();
        int reorderLevel = request.reorderLevel() == null ? 0 : request.reorderLevel();
        var existing = hospitalStockRepository.findByHospitalIdAndMedicine_Id(hospital.hospitalId(), medicine.getId());
        HospitalStockEntity stock;
        if (existing.isPresent()) {
            stock = existing.get();
            if (stock.getCurrency() != request.currency()) {
                throw new InvalidStockEntryException("La monnaie doit rester identique pour le stock de ce médicament dans cet hôpital.");
            }
            stock.receive(request.quantity(), request.unitCost(), reorderLevel, receivedAt);
        } else {
            stock = hospitalStockRepository.save(new HospitalStockEntity(UUID.randomUUID(), hospital.hospitalId(),
                    hospital.hospitalCode(), medicine, request.quantity(), reorderLevel, request.unitCost(), request.currency(), receivedAt));
        }
        StockEntryEntity entry = new StockEntryEntity(UUID.randomUUID(), nextStockEntryCode(), stock, request.quantity(),
                request.unitCost().setScale(2, RoundingMode.HALF_UP), request.currency(), request.expiresOn(),
                trimToNull(request.supplierName()), trimToNull(request.notes()), actor, receivedAt);
        return toEntry(stockEntryRepository.save(entry));
    }

    private HospitalReferenceClient.HospitalReference resolveHospital(UUID requestedHospitalId, DataAccessScope scope) {
        if (scope.provinceWide()) {
            if (requestedHospitalId == null) throw new IllegalArgumentException("Un hôpital est obligatoire pour cette entrée de stock.");
            return hospitalReferenceClient.resolveActive(requestedHospitalId);
        }
        if (scope.hospitalId() == null || scope.hospitalCode() == null) throw new DataAccessDeniedException();
        return new HospitalReferenceClient.HospitalReference(scope.hospitalId(), scope.hospitalCode(), true);
    }

    private String nextMedicineCode() { return nextCode("MED", medicineRepository::existsByCodeIgnoreCase); }
    private String nextStockEntryCode() { return nextCode("ENT", stockEntryRepository::existsByCodeIgnoreCase); }
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
        return new StockBalanceResponse(item.getId(), item.getHospitalId(), item.getHospitalCode(), medicine.getId(), medicine.getCode(),
                medicine.getGenericName(), medicine.getCommercialName(), medicine.getDosage(), medicine.getPharmaceuticalForm(), item.getQuantity(),
                item.getReorderLevel(), item.getAverageUnitCost(), item.getCurrency(), item.getQuantity() <= item.getReorderLevel(), item.getUpdatedAt());
    }
    private StockEntryResponse toEntry(StockEntryEntity item) {
        MedicineEntity medicine = item.getMedicine();
        return new StockEntryResponse(item.getId(), item.getCode(), item.getHospitalId(), item.getHospitalCode(), medicine.getId(), medicine.getCode(),
                medicine.getGenericName(), item.getQuantity(), item.getUnitCost(), item.getTotalCost(), item.getCurrency(), item.getExpiresOn(),
                item.getSupplierName(), item.getNotes(), item.getAccountingStatus(), item.getReceivedAt(), item.getReceivedByUsername());
    }
}
