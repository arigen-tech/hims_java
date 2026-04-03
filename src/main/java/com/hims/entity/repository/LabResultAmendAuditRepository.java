package com.hims.entity.repository;

import com.hims.entity.LabResultAmendAudit;
import com.hims.response.LabAmenedAuditReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LabResultAmendAuditRepository extends JpaRepository<LabResultAmendAudit,Long>, JpaSpecificationExecutor<LabResultAmendAudit> {

    @Query("""
SELECT new com.hims.response.LabAmenedAuditReportResponse(
    a.amendmentId,
    a.generatedSampleId,
    TRIM(CONCAT(
        COALESCE(p.patientFn, ''), ' ',
        COALESCE(p.patientMn, ''), ' ',
        COALESCE(p.patientLn, '')
    )),
    inv.investigationName,
    uom.name,
    a.oldResult,
    a.newResult,
    a.reasonForChange,
    a.amendedBy,
    a.amendedDatetime
)
FROM LabResultAmendAudit a
LEFT JOIN a.patient p
LEFT JOIN a.investigation inv
LEFT JOIN inv.uomId uom
LEFT JOIN inv.subChargeCodeId sub

WHERE (:phnNum IS NULL OR p.patientMobileNumber LIKE :phnNum)

AND (
    :patientName IS NULL OR
    LOWER(TRIM(CONCAT(
        COALESCE(p.patientFn, ''), ' ',
        COALESCE(p.patientMn, ''), ' ',
        COALESCE(p.patientLn, '')
    ))) LIKE :patientName
)

AND (:investigationId IS NULL OR inv.investigationId = :investigationId)

AND (:subChargeCodeId IS NULL OR sub.subId = :subChargeCodeId)

AND a.amendedDatetime >= COALESCE(:fromDate, a.amendedDatetime)
AND a.amendedDatetime <= COALESCE(:toDate, a.amendedDatetime)
""")
    Page<LabAmenedAuditReportResponse> getAmendAuditReport(
            String phnNum,
            String patientName,
            Long investigationId,
            Long subChargeCodeId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    );
}
