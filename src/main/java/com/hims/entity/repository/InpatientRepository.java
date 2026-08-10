package com.hims.entity.repository;

import com.hims.entity.Inpatient;
import com.hims.projection.InpatientAdvanceCollectionProjection;
import lombok.Locked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InpatientRepository extends JpaRepository<Inpatient,Long> {

    @Query(
            value = """
        SELECT admission_no
        FROM inpatient
        WHERE admission_no LIKE :financialYearPattern
        ORDER BY CAST(SPLIT_PART(admission_no, '/', 3) AS INTEGER) DESC
        LIMIT 1
        """,
            nativeQuery = true
    )
    String findLastAdmissionNoByFinancialYear(
            @Param("financialYearPattern") String financialYearPattern
    );

    Optional<Inpatient> findTopByPatient_IdOrderByInpatientIdDesc(Long patientId);

    @Query(value = """
SELECT
    i.inpatient_id   AS inpatientId,
    bh.bill_id  AS billingHeaderId,
    p.uhid_no    AS uhid,
    CONCAT(
        COALESCE(p.p_fn,''),' ',
        COALESCE(p.p_mn,''),' ',
        COALESCE(p.p_ln,'')
    )     AS patientName,
    p.p_age   AS age,
    g.id AS genderId,
    g.gender_name  AS gender,
    p.p_mobile_number AS mobileNo,
    i.admission_no AS admissionNo,
    w.ward_id  AS wardId,
    w.ward_name  AS ward,
    r.room_id  AS roomId,
    r.room_name  AS room,
    b.bed_id AS bedId,
    b.bed_number AS bed,
    (i.admission_date + i.admission_time) AS admissionDateTime,
    bt.billing_type_id AS billingTypeId,
    bt.billing_type_name AS billingType
FROM inpatient i
INNER JOIN patient p
        ON p.patient_id = i.patient
LEFT JOIN mas_gender g
       ON g.id = p.p_gender_id
LEFT JOIN mas_ward w
       ON w.ward_id = i.admitting_ward_id
LEFT JOIN mas_room r
       ON r.room_id = i.room_id
LEFT JOIN mas_bed b
       ON b.bed_id = i.bed_id
LEFT JOIN ipd_billing_header bh
       ON bh.inpatient_id = i.inpatient_id
LEFT JOIN mas_ipd_billing_type bt
       ON bt.billing_type_id = bh.billing_type_id
WHERE i.admission_status = :admissionStatus
AND (
        :patientName IS NULL
        OR LOWER(CONCAT(
                COALESCE(p.p_fn,''),' ',
                COALESCE(p.p_mn,''),' ',
                COALESCE(p.p_ln,'')
            )) LIKE LOWER(CONCAT('%', :patientName, '%'))
)
AND (
        :mobileNo IS NULL
        OR p.p_mobile_number ILIKE CONCAT('%', :mobileNo, '%')
)
AND (
        :admissionNo IS NULL
        OR i.admission_no ILIKE CONCAT('%', :admissionNo, '%')
)
ORDER BY i.inpatient_id DESC
""",
            countQuery = """
SELECT COUNT(*)
FROM inpatient i
INNER JOIN patient p
        ON p.patient_id = i.patient
WHERE i.admission_status = :admissionStatus
AND (
        :patientName IS NULL
        OR LOWER(CONCAT(
                COALESCE(p.p_fn,''),' ',
                COALESCE(p.p_mn,''),' ',
                COALESCE(p.p_ln,'')
            )) LIKE LOWER(CONCAT('%', :patientName, '%'))
)
AND (
        :mobileNo IS NULL
        OR p.p_mobile_number ILIKE CONCAT('%', :mobileNo, '%')
)
AND (
        :admissionNo IS NULL
        OR i.admission_no ILIKE CONCAT('%', :admissionNo, '%')
)
""",
            nativeQuery = true)
    Page<InpatientAdvanceCollectionProjection> getIpdAdvanceCollection(
            @Param("admissionStatus") Long admissionStatus,
            @Param("patientName") String patientName,
            @Param("mobileNo") String mobileNo,
            @Param("admissionNo") String admissionNo,
            Pageable pageable);
}
