package com.hims.entity.repository;

import com.hims.entity.PatientPrescriptionDt;
import com.hims.entity.projection.PrescriptionDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PatientPrescriptionDtRepository extends JpaRepository<PatientPrescriptionDt, Long> {
    List<PatientPrescriptionDt> findByPrescriptionHdId(Long prescriptionHdId);

    void deleteByPrescriptionHdId(Long prescriptionHdId);

    @Query(value = """
        SELECT 
            ppdt.prescription_dt_id AS prescriptionDtId,
            ppdt.prescription_hd_id AS prescriptionHdId,
            ppdt.item_id AS itemId,
            ppdt.dosage AS dosage,
            ppdt.frequency AS frequency,
            ppdt.days AS days,
            ppdt.total AS total,
            ppdt.issued_qty AS issuedQty,
            ppdt.route AS route,
            ppdt.instruction AS instruction,
            ppdt.unit_price AS unitPrice,
            ppdt.discount AS discount,
            ppdt.gst_rate AS gstRate,
            ppdt.line_cost AS lineCost,
            ppdt.status AS status,
            ppdt.batch_no AS batchNo,
            ppdt.expiry_date AS expiryDate
        FROM patient_prescription_dt ppdt
        INNER JOIN patient_prescription_hd pph
            ON ppdt.prescription_hd_id = pph.prescription_hd_id
        WHERE pph.patient_id = :patientId
        AND pph.prescription_date >= 
            CURRENT_DATE - CAST(CONCAT(:prescriptionHistoryDays, ' days') AS INTERVAL)
        ORDER BY pph.prescription_date DESC
        """, nativeQuery = true)
    List<PrescriptionDetailProjection> findPrescriptionDetailsByPatientIdWithinLimitedDays(
            @Param("patientId") Long patientId,
            @Param("prescriptionHistoryDays") Integer prescriptionHistoryDays);

}
