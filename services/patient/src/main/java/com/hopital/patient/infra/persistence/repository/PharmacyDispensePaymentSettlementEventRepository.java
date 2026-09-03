package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.PharmacyDispensePaymentSettlementEventEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyDispensePaymentSettlementEventRepository
        extends JpaRepository<PharmacyDispensePaymentSettlementEventEntity, UUID> {

    Optional<PharmacyDispensePaymentSettlementEventEntity> findByPaymentId(UUID paymentId);
}
