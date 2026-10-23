package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.PartnerCompanyMemberEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartnerCompanyMemberRepository extends JpaRepository<PartnerCompanyMemberEntity, UUID> {

    Optional<PartnerCompanyMemberEntity> findByIdAndPartnerCompanyId(UUID id, UUID partnerCompanyId);

    @Query("""
            SELECT member FROM PartnerCompanyMemberEntity member
            WHERE member.partnerCompanyId = :partnerCompanyId
              AND (:active IS NULL OR member.active = :active)
              AND (:sponsorMemberId IS NULL OR member.sponsorMemberId = :sponsorMemberId)
              AND (
                :query = ''
                OR LOWER(member.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(COALESCE(member.employeeNumber, '')) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY member.fullName ASC
            """)
    Page<PartnerCompanyMemberEntity> search(
            @Param("partnerCompanyId") UUID partnerCompanyId,
            @Param("query") String query,
            @Param("active") Boolean active,
            @Param("sponsorMemberId") UUID sponsorMemberId,
            Pageable pageable);
}
