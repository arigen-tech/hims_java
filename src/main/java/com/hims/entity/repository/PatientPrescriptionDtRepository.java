package com.hims.entity.repository;

import com.hims.entity.PatientPrescriptionDt;
import com.hims.entity.projection.PrescriptionDetailProjection;
import com.hims.response.PatientPrescriptionDetailsResponse;
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
        msi.nomenclature AS itemName,
        msi.dispensing_unit AS dispUnit,
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
        ppdt.expiry_date AS expiryDate,

        pph.doctor_name AS doctorName,
        pph.prescription_date AS prescribedDate,
      
        md.department_name AS departmentName

    FROM patient_prescription_dt ppdt

    INNER JOIN patient_prescription_hd pph
        ON ppdt.prescription_hd_id = pph.prescription_hd_id

    LEFT JOIN mas_department md
        ON pph.department_id = md.department_id
        LEFT JOIN mas_store_item msi
                        ON ppdt.item_id = msi.item_id

    WHERE pph.patient_id = :patientId
    AND pph.prescription_date >= 
        CURRENT_DATE - CAST(CONCAT(:prescriptionHistoryDays, ' days') AS INTERVAL)

    ORDER BY pph.prescription_date DESC
    """, nativeQuery = true)
    List<PrescriptionDetailProjection> findPrescriptionDetailsByPatientIdWithinLimitedDays(
            @Param("patientId") Long patientId,
            @Param("prescriptionHistoryDays") Integer prescriptionHistoryDays);

    @Query(value = """
    SELECT new com.hims.response.PatientPrescriptionDetailsResponse(
        ppdt.prescriptionHdId,
        ppdt.prescriptionDtId,
        ppdt.itemId,
        msi.nomenclature,
        ppdt.dosage,
        ppdt.frequency,
        ppdt.days,
        ppdt.total,
        ppdt.total,
        ppdt.status
        )
        FROM PatientPrescriptionDt ppdt
        LEFT JOIN ppdt.prescriptionHeader pph
        LEFT JOIN MasStoreItem msi ON ppdt.itemId = msi.itemId
    WHERE pph.prescriptionHdId = :prescriptionHeaderId
    AND ppdt.status = :pendingStatus
    """)
    List<PatientPrescriptionDetailsResponse> findPendingPrescriptionsDetailsWrtHeader(Long prescriptionHeaderId, String pendingStatus);

}
