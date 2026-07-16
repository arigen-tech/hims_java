package com.hims.entity.repository;

import com.hims.entity.IpBedAllocation;
import com.hims.projection.WardWiseDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IpBedAllocationRepository extends JpaRepository<IpBedAllocation,Long> {
    @Query(value = """
    SELECT DISTINCT ON (b.bed_id)

        p.patient_id AS patientId,
        i.inpatient_id AS ipdPatientId,
        i.admission_no AS admissionNo,
        mis.status_code AS ipdInternalStatus,
        mds.status_code AS admissionStatus,
        p.p_age AS age,
        g.gender_code AS gender,

        CASE
            WHEN p.patient_id IS NOT NULL THEN
                TRIM(
                    CONCAT_WS(
                        ' ',
                        NULLIF(p.p_fn, ''),
                        NULLIF(p.p_mn, ''),
                        NULLIF(p.p_ln, '')
                    )
                )
            ELSE NULL
        END AS patientName,

       
        r.room_name AS roomName,
        b.bed_number AS bedNumber,
        i.admission_date AS admitDate,
                r.room_id AS roomId,
                b.bed_id AS bedId,
               TRIM(
                  CONCAT_WS(
                             ' ',
                         NULLIF(u.first_name, ''),
                         NULLIF(u.middle_name, ''),
                          NULLIF(u.last_name, '')
                         )
                    ) AS doctor,
               

        CASE
            WHEN i.admission_date IS NOT NULL THEN
                CAST(
                    GREATEST(
                        (CURRENT_DATE - i.admission_date) + 1,
                        0
                    ) AS BIGINT
                )
            ELSE NULL
        END AS days

    FROM mas_bed b

    INNER JOIN mas_room r
        ON r.room_id = b.room_id

    INNER JOIN mas_ward w
        ON w.ward_id = r.ward_id

    LEFT JOIN ip_bed_allocation iba
        ON iba.bed_id = b.bed_id
       
    LEFT JOIN inpatient i
        ON i.inpatient_id = iba.ip_admission_id

    LEFT JOIN patient p
        ON p.patient_id = i.patient

    LEFT JOIN mas_gender g
        ON g.id = p.p_gender_id

    LEFT JOIN mas_ipd_internal_status mis
        ON mis.ipd_internal_status_id = i.ip_internal_status_id

    LEFT JOIN mas_admission_status mds
        ON mds.admission_status_id = i.admission_status
              
                LEFT JOIN ip_diagnosis_entry ide
                    ON ide.inpatient_id = i.inpatient_id
                
                LEFT JOIN users u
                    ON u.user_id = ide.recorded_by
  

    WHERE w.ward_id = :wardId

    ORDER BY
        b.bed_id,
        iba.allocation_start_date DESC
    """, nativeQuery = true)
    List<WardWiseDetailsProjection> getWardWiseDetails(
            @Param("wardId") Long wardId
    );
}
