package com.hopital.laboratory.infra.persistence.repository;

import com.hopital.laboratory.infra.persistence.entity.SpecimenEntity;
import java.util.List;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpecimenRepository extends JpaRepository<SpecimenEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    List<SpecimenEntity> findAllByOrderByReceivedAtDesc();

    List<SpecimenEntity> findAllByAnalysisRequest_IdInOrderByReceivedAtDesc(Collection<UUID> analysisRequestIds);

    @Query("""
            SELECT specimen
            FROM SpecimenEntity specimen
            JOIN specimen.analysisRequest analysisRequest
            WHERE (:query = ''
                    OR LOWER(specimen.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.patientReference) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.patientName) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:provinceWide = true OR analysisRequest.laboratoryCode IN :laboratoryCodes)
            """)
    Page<SpecimenEntity> search(@Param("query") String query, @Param("provinceWide") boolean provinceWide,
            @Param("laboratoryCodes") List<String> laboratoryCodes, Pageable pageable);

    default Page<SpecimenEntity> search(String query, Pageable pageable) {
        return search(query, true, List.of("_"), pageable);
    }
}
