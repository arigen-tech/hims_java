package com.hims.entity.repository;

import com.hims.entity.PatientPrescriptionDt;
import com.hims.entity.projection.PrescriptionDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PatientPrescriptionDtRepository extends JpaRepository<PatientPrescriptionDt, Long> {
    List<PatientPrescriptionDt> findByPrescriptionHdId(Long prescriptionHdId);

    void deleteByPrescriptionHdId(Long prescriptionHdId);

    @Query(value = """
            SELECT 
                ppdt.prescription_dt_id,
                ppdt.prescription_hd_id,
                ppdt.item_id,
                ppdt.dosage,
                ppdt.frequency,
                ppdt.days,
                ppdt.total,
                ppdt.issued_qty,
                ppdt.route,
                ppdt.instruction,
                ppdt.unit_price,
                ppdt.discount,
                ppdt.gst_rate,
                ppdt.line_cost,
                ppdt.status,
                ppdt.batch_no,
                ppdt.expiry_date
            FROM patient_prescription_dt ppdt
            INNER JOIN patient_prescription_hd pph 
                ON ppdt.prescription_hd_id = pph.prescription_hd_id
            WHERE pph.patient_id = :patientId
            AND pph.prescription_date >= DATE_SUB(CURRENT_DATE, INTERVAL :prescriptionHistoryDays DAY)
            ORDER BY pph.prescription_date DESC
            """, nativeQuery = true)
    List<PrescriptionDetailProjection> findPrescriptionDetailsByPatientIdWithinLimitedDays(
            @Param("patientId") Long patientId,
            @Param("prescriptionHistoryDays") Integer prescriptionHistoryDays);

}
