package com.hims.entity.repository;

import com.hims.entity.IpdBillingHeader;
import com.hims.projection.PendingTrackingIPDBillProjection;
import com.hims.response.PaymentStatusResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface IpdBillingHeaderRepository extends JpaRepository<IpdBillingHeader,Long> {

    Optional<IpdBillingHeader> findByInpatientId_InpatientId(Long inpatientId);

    @Query("""
    SELECT new com.hims.response.PaymentStatusResponse(
        i.inpatientId,
        b.billId,
        bs.billStatusId,
        bs.statusName,
        ps.paymentStatusId,
        ps.statusName,
        b.outstandingAmount
    )
    FROM IpdBillingHeader b
    JOIN b.inpatientId i
    LEFT JOIN b.billStatus bs
    LEFT JOIN b.paymentStatus ps
    WHERE i.inpatientId = :inpatientId
    """)
    PaymentStatusResponse getPaymentStatus(@Param("inpatientId") Long inpatientId);

    @Query(value = """
SELECT
    i.inpatient_id AS inpatientId,
    bh.bill_id AS billingHeaderId,
    p.uhid_no AS uhid,
    CONCAT(
        COALESCE(p.p_fn,''),' ',
        COALESCE(p.p_mn,''),' ',
        COALESCE(p.p_ln,'')
    ) AS patientName,
    p.p_age AS age,
    g.id AS genderId,
    g.gender_name AS gender,
    p.p_mobile_number AS mobileNo,
    i.admission_no AS admissionNo,
    w.ward_id AS wardId,
    w.ward_name AS ward,
    r.room_id AS roomId,
    r.room_name AS room,
    bd.bed_id AS bedId,
    bd.bed_number AS bed,
    (i.admission_date + i.admission_time) AS admissionDateTime,
    bt.billing_type_id AS billingTypeId,
    bt.billing_type_name AS billingType,
    bh.total_amount AS totalAmount,
    bh.estimation_cost AS estimationCost,
    bh.patient_paid_amount AS patientPaid,
    GREATEST( 0,
     COALESCE(bh.total_amount, 0) - COALESCE(bh.patient_paid_amount, 0)
 ) AS outStandingAmount,
    bs.bill_status_id AS billStatusId,
    bs.status_code AS billStatus,
    ps.payment_status_id AS paymentStatusId,
    ps.status_code AS paymentStatus
FROM inpatient i
INNER JOIN patient p
    ON p.patient_id = i.patient
LEFT JOIN mas_gender g
    ON g.id = p.p_gender_id
LEFT JOIN mas_ward w
    ON w.ward_id = i.admitting_ward_id
LEFT JOIN mas_room r
    ON r.room_id = i.room_id
LEFT JOIN mas_bed bd
    ON bd.bed_id = i.bed_id
LEFT JOIN ipd_billing_header bh
    ON bh.inpatient_id = i.inpatient_id
LEFT JOIN mas_ipd_billing_type bt
    ON bt.billing_type_id = bh.billing_type_id
LEFT JOIN mas_ipd_bill_status bs
    ON bs.bill_status_id = bh.bill_status_id
LEFT JOIN mas_ipd_payment_status ps
    ON ps.payment_status_id = bh.payment_status_id
WHERE i.admission_status = :admissionStatus
AND
 (:wardId IS NULL OR w.ward_id = :wardId)
AND (:billType IS NULL OR bt.billing_type_id = :billType)
AND (:outStandingAmount IS NULL OR bh.outstanding_amount >= :outStandingAmount)
ORDER BY i.inpatient_id DESC
""",
            countQuery = """
SELECT COUNT(*)
FROM inpatient i
LEFT JOIN mas_ward w
    ON w.ward_id = i.admitting_ward_id
LEFT JOIN ipd_billing_header bh
    ON bh.inpatient_id = i.inpatient_id
LEFT JOIN mas_ipd_billing_type bt
    ON bt.billing_type_id = bh.billing_type_id
WHERE
    (:wardId IS NULL OR w.ward_id = :wardId)
AND (:billType IS NULL OR bt.billing_type_id = :billType)
AND (:outStandingAmount IS NULL OR bh.outstanding_amount >= :outStandingAmount)
""",
            nativeQuery = true)
    Page<PendingTrackingIPDBillProjection> getPendingTrackingIPDBillList(
            @Param("admissionStatus") Long admissionStatus,
            @Param("wardId") Long wardId,
            @Param("billType") Long billType,
            @Param("outStandingAmount") BigDecimal outStandingAmount,
            Pageable pageable);
}
