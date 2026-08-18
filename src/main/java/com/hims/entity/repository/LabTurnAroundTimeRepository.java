package com.hims.entity.repository;

import com.hims.entity.LabTurnAroundTime;
import com.hims.entity.Patient;
import com.hims.response.LabDetailedTATReportResponse;
import com.hims.response.LabSummaryTATReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface LabTurnAroundTimeRepository  extends JpaRepository<LabTurnAroundTime,Long>, JpaSpecificationExecutor<LabTurnAroundTime> {

    LabTurnAroundTime findByOrderHd_IdAndInvestigation_InvestigationIdAndPatient_IdAndIsReject(Long orderHdId, Long investigationId, Long patientId, Boolean s );

    LabTurnAroundTime findByOrderHd_IdAndInvestigation_InvestigationIdAndPatient_IdAndGeneratedSampleId(Long orderHdId, Long investigationId, Long id, String sampleGeneratedId);

    @Query("""
SELECT new com.hims.response.LabDetailedTATReportResponse(
    tat.turnAroundTimeId,
    oh.id,
    inv.investigationName,
    tat.generatedSampleId,
    tat.sampleCollectionDateTime,
    tat.resultValidationTime,
    inv.tatHours,
    FUNCTION('TIMESTAMPDIFF', HOUR, tat.sampleCollectionDateTime, tat.resultValidationTime),
    CASE 
        WHEN FUNCTION('TIMESTAMPDIFF', HOUR, tat.sampleCollectionDateTime, tat.resultValidationTime) > inv.tatHours 
        THEN FUNCTION('TIMESTAMPDIFF', HOUR, tat.sampleCollectionDateTime, tat.resultValidationTime) - inv.tatHours
        ELSE 0
    END,
    CASE 
        WHEN FUNCTION('TIMESTAMPDIFF', HOUR, tat.sampleCollectionDateTime, tat.resultValidationTime) > inv.tatHours 
        THEN 'Breached'
        ELSE 'Within'
    END,
    tat.resultValidatedBy
)
FROM LabTurnAroundTime tat
JOIN tat.orderHd oh
JOIN tat.investigation inv
LEFT JOIN inv.subChargeCodeId sub
WHERE oh.hospitalId=:hospitalId
AND tat.resultValidationTime IS NOT NULL
AND oh.orderDate BETWEEN :fromDate AND :toDate
AND (:investigationId IS NULL OR inv.investigationId = :investigationId)
AND (:subChargeCodeId IS NULL OR sub.subId = :subChargeCodeId)
""")
    Page<LabDetailedTATReportResponse> findTatReportWithPagination(
            @Param("hospitalId") Long hospitalId,
            @Param("investigationId") Long investigationId,
            @Param("subChargeCodeId") Long subChargeCodeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );


    @Query("""
SELECT new com.hims.response.LabSummaryTATReportResponse(
    inv.investigationId,
    inv.investigationName,
    inv.tatHours,
    COUNT(tat),
    AVG(FUNCTION('TIMESTAMPDIFF', HOUR, tat.sampleCollectionDateTime, tat.resultValidationTime)),
    MIN(FUNCTION('TIMESTAMPDIFF', HOUR, tat.sampleCollectionDateTime, tat.resultValidationTime)),
    MAX(FUNCTION('TIMESTAMPDIFF', HOUR, tat.sampleCollectionDateTime, tat.resultValidationTime)),
    SUM(CASE 
        WHEN FUNCTION('TIMESTAMPDIFF', HOUR, tat.sampleCollectionDateTime, tat.resultValidationTime) <= inv.tatHours 
        THEN 1 ELSE 0 END),
    SUM(CASE 
        WHEN FUNCTION('TIMESTAMPDIFF', HOUR, tat.sampleCollectionDateTime, tat.resultValidationTime) > inv.tatHours 
        THEN 1 ELSE 0 END)
)
FROM LabTurnAroundTime tat
JOIN tat.investigation inv
JOIN tat.orderHd oh
LEFT JOIN inv.subChargeCodeId sub
WHERE oh.hospitalId=:hospitalId
AND tat.resultValidationTime IS NOT NULL
AND oh.orderDate BETWEEN :fromDate AND :toDate
AND (:investigationId IS NULL OR inv.investigationId = :investigationId)
AND (:subChargeCodeId IS NULL OR sub.subId = :subChargeCodeId)
GROUP BY inv.investigationId, inv.investigationName, inv.tatHours
ORDER BY inv.investigationName
""")
    Page<LabSummaryTATReportResponse> getTatSummaryReport(
            @Param("hospitalId") Long hospitalId,
            @Param("investigationId") Long investigationId,
            @Param("subChargeCodeId") Long subChargeCodeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );
}
