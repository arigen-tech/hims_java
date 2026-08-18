package com.hims.entity.repository;


import com.hims.entity.DgOrderDt;
import com.hims.entity.DgOrderHd;
import com.hims.response.LabIncompleteInvestigationsReportResponse;
import com.hims.response.OrderTrackingReportResponse;
import com.hims.response.PendingSampleDetailResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LabDtRepository extends JpaRepository<DgOrderDt,Integer> , JpaSpecificationExecutor<DgOrderDt> {
 List<DgOrderDt> findByOrderHd(DgOrderHd hdObj);

    @Modifying
    @Query("UPDATE DgOrderDt b SET b.billingStatus = :billing_status WHERE b.investigation.id = :investigationId AND b.billingHd.id = :billHdId AND b.investigationPackage IS NULL" )
    void updatePaymentStatusInvestigationDt(@Param("billing_status") String billing_status,
                                          @Param("investigationId") int investigationId,
                                          @Param("billHdId") int billHdId);


    @Modifying
    @Query("UPDATE DgOrderDt b SET b.billingStatus = :billing_status WHERE b.investigationPackage.packId = :pkgId AND b.billingHd.id = :billHdId")
    void updatePaymentStatusPackageDt(@Param("billing_status") String billing_status,
                                      @Param("pkgId") int pkgId,
                                      @Param("billHdId") int billHdId);

   @Modifying
   @Query("select b from DgOrderDt b  WHERE  b.billingHd.id = :billHdId AND  b.billingStatus = 'n'")
   List<DgOrderDt> findByStatus(@Param("billHdId") long billHdId);

    List<DgOrderDt> findByOrderHdAndBillingStatusAndOrderStatus(DgOrderHd orderHd, String billingStatus, String orderStatus);

    List<DgOrderDt> findByOrderHd_Id(Long orderHdId);

    List<DgOrderDt> findByOrderHdAndBillingStatus(DgOrderHd orderHd, String n);

    @Transactional
    @Modifying
    @Query("UPDATE DgOrderDt d SET d.orderStatus = :status WHERE d.id = :id")
    void updateOrderStatus(Long id, String status);

    @Query("SELECT d.orderStatus FROM DgOrderDt d WHERE d.orderHd.id = :orderHdId")
    List<String> getOrderStatusesOfOrderHd(Long orderHdId);



    DgOrderDt findByOrderHd_IdAndInvestigation_InvestigationId(long id, Long investigationId);

    @Query("""
SELECT new com.hims.response.PendingSampleDetailResponse(
    dt.id,
    inv.investigationId,
    inv.investigationName,
    s.id,
    s.sampleDescription,
    c.collectionId,
    c.collectionName,
    ms.subId,
    mm.chargecodeId
)
FROM DgOrderDt dt
LEFT JOIN dt.investigation inv
LEFT JOIN inv.subChargeCodeId ms
LEFT JOIN inv.mainChargeCodeId mm
LEFT JOIN inv.sampleId s
LEFT JOIN inv.collectionId c
WHERE dt.orderHd.id = :orderHdId
AND LOWER(dt.billingStatus) = :billingStatus
AND LOWER(dt.orderStatus) = :orderStatus
ORDER BY dt.createdOn
""")
    List<PendingSampleDetailResponse> findPendingDetailsForCollectionByOrderHdId(
            @Param("orderHdId") Long orderHdId,
            @Param("billingStatus") String billingStatus,
            @Param("orderStatus") String orderStatus
    );


    @Query("""
SELECT new com.hims.response.OrderTrackingReportResponse(
    oh.id,
    oh.orderNo,
    TRIM(CONCAT(
        COALESCE(p.patientFn, ''), ' ',
        COALESCE(p.patientMn, ''), ' ',
        COALESCE(p.patientLn, '')
    )),
    p.patientMobileNumber,
    p.patientAge,
    g.genderName,
    CASE
        WHEN ots.orderStatusId = 1 THEN 'N/A'
        ELSE (
            SELECT scd.sampleGeneratedId
            FROM DgSampleCollectionDetails scd
            WHERE scd.sampleCollectionDetailsId = (
                SELECT MAX(scd2.sampleCollectionDetailsId)
                FROM DgSampleCollectionDetails scd2
                WHERE scd2.investigationId.investigationId = inv.investigationId
                  AND scd2.sampleCollectionHeader.dgOrderHd.id = oh.id
            )
        )
    END,
    inv.investigationName,
    ots.orderStatusId,
    ots.orderStatusName,
    oh.orderDate
)
FROM DgOrderDt od
JOIN od.orderHd oh
JOIN oh.patientId p
LEFT JOIN p.patientGender g
LEFT JOIN od.investigation inv
LEFT JOIN od.orderTrackingStatus ots
WHERE oh.hospitalId=:hospitalId
AND (:mobileNo IS NULL OR p.patientMobileNumber LIKE :mobileNo)
AND (:patientName IS NULL OR LOWER(TRIM(CONCAT(
        COALESCE(p.patientFn, ''), ' ',
        COALESCE(p.patientMn, ''), ' ',
        COALESCE(p.patientLn, '')
    ))) LIKE :patientName)
AND oh.orderDate >= COALESCE(:fromDate, oh.orderDate)
AND oh.orderDate <= COALESCE(:toDate, oh.orderDate)
""")
    Page<OrderTrackingReportResponse> getOrderTrackingReport(
            Long hospitalId,
            String patientName,
            String mobileNo,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    );


    @Query("""
SELECT new com.hims.response.OrderTrackingReportResponse(
    oh.id,
    oh.orderNo,
    TRIM(CONCAT(
        COALESCE(p.patientFn, ''), ' ',
        COALESCE(p.patientMn, ''), ' ',
        COALESCE(p.patientLn, '')
    )),
    p.patientMobileNumber,
    p.patientAge,
    g.genderName,
    CASE
        WHEN ots.orderStatusId = 1 THEN 'N/A'
        ELSE (
            SELECT scd.sampleGeneratedId
            FROM DgSampleCollectionDetails scd
            WHERE scd.sampleCollectionDetailsId = (
                SELECT MAX(scd2.sampleCollectionDetailsId)
                FROM DgSampleCollectionDetails scd2
                WHERE scd2.investigationId.investigationId = inv.investigationId
                  AND scd2.sampleCollectionHeader.dgOrderHd.id= oh.id
            )
        )
    END,
    inv.investigationName,
    ots.orderStatusId,
    ots.orderStatusName,
    oh.orderDate
)
FROM DgOrderDt od
JOIN od.orderHd oh
JOIN oh.patientId p
LEFT JOIN p.patientGender g
LEFT JOIN od.investigation inv
LEFT JOIN od.orderTrackingStatus ots
WHERE oh.hospitalId=:hospitalId
AND p.id=:patientId
""")
    Page<OrderTrackingReportResponse> getOrderTrackingDetailsByPatientId(
            Long hospitalId,
            Long patientId,
            Pageable pageable
    );

    @Query("""
SELECT new com.hims.response.LabIncompleteInvestigationsReportResponse(
    oh.orderNo,
    oh.orderDate,
    TRIM(CONCAT(
        COALESCE(p.patientFn,''),' ',
        COALESCE(p.patientMn,''),' ',
        COALESCE(p.patientLn,'')
    )),
    p.patientMobileNumber,
    p.patientAge,
    g.genderName,
    (
        SELECT scd.sampleGeneratedId
            FROM DgSampleCollectionDetails scd
            WHERE scd.sampleCollectionDetailsId = (
                SELECT MAX(scd2.sampleCollectionDetailsId)
                FROM DgSampleCollectionDetails scd2
                WHERE scd2.investigationId.investigationId = inv.investigationId
                  AND scd2.sampleCollectionHeader.dgOrderHd.id = oh.id
            )
    ),
    inv.investigationName,
    ots.orderStatusName
)
FROM DgOrderDt od
JOIN od.orderHd oh
JOIN oh.patientId p
LEFT JOIN p.patientGender g
LEFT JOIN od.investigation inv
LEFT JOIN od.orderTrackingStatus ots
WHERE oh.hospitalId=:hospitalId
AND oh.orderDate BETWEEN :fromDate AND :toDate
AND (:subChargeCodeId IS NULL OR od.subChargeCodeId = :subChargeCodeId)
AND ots.orderStatusId IN :statuses
""")
    Page<LabIncompleteInvestigationsReportResponse> getIncompleteInvestigations(
            Long hospitalId,
            Long subChargeCodeId,
            LocalDate fromDate,
            LocalDate toDate,
            List<Long> statuses,
            Pageable pageable
    );

}
