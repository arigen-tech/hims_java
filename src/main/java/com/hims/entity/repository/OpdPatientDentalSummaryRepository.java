package com.hims.entity.repository;

import com.hims.entity.OpdPatientDentalSummary;
import com.hims.response.DentalSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpdPatientDentalSummaryRepository extends JpaRepository<OpdPatientDentalSummary, Long> {

    Optional<OpdPatientDentalSummary> findByPatientIdAndVisitId(Long patientId, Long visitId);

    @Query("""
    SELECT new com.hims.response.DentalSummaryResponse(
        d.visitId,
        d.summaryId,
        d.lastUpdateDate,
        d.dentalDiseaseScore,
        d.missingTeeth,
        d.unsalvageableTeeth,
        d.affectedTeeth,
        d.notes
    )
    FROM OpdPatientDentalSummary d
    WHERE d.patientId = :patientId
    ORDER BY d.lastUpdateDate DESC
""")
    Page<DentalSummaryResponse> findByPatientId(@Param("patientId") Long patientId, Pageable pageable);
}