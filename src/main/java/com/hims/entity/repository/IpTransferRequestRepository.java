package com.hims.entity.repository;

import com.hims.entity.IpTransferRequest;
import com.hims.projection.PendingToTransferProjectionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IpTransferRequestRepository extends JpaRepository<IpTransferRequest,Long> {


        @Query(value = """
            SELECT
                itr.inpatient_id AS "inpatientId",
                itr.patient_id AS "patientId",
                itr.transfer_no AS "transferNo",
                itr.request_datetime AS "transferDateTime",

                TRIM(
                    CONCAT_WS(
                        ' ',
                        NULLIF(TRIM(p.p_fn), ''),
                        NULLIF(TRIM(p.p_mn), ''),
                        NULLIF(TRIM(p.p_ln), '')
                    )
                ) AS "patientName",

                mg.gender_code AS "gender",
                p.p_age AS "age",
                i.admission_no AS "admissionNo",
                TO_CHAR(i.admission_date, 'YYYY-MM-DD') AS "admissionDate",

                itr.from_ward_id AS "fromWardId",
                fw.ward_name AS "fromWardName",

                itr.from_bed_id AS "fromBedId",
                fb.bed_number AS "fromBedName",

                itr.to_ward_id AS "toWardId",
                tw.ward_name AS "toWardName",

                itr.to_bed_id AS "toBedId",
                tb.bed_number AS "toBedName",

                itr.transfer_reason_id AS "transferReasonId",
                mtr.transfer_reason_name AS "transferReason",

              
                itr.transfer_status AS "transferStatus",

                itr.clinical_notes AS "clinicalNotes",

                itr.doctor_id AS "doctorId",
               
                             TRIM(
                    CONCAT_WS(
                        ' ',
                        NULLIF(TRIM(usr.first_name), ''),
                        NULLIF(TRIM(usr.middle_name), ''),
                        NULLIF(TRIM(usr.last_name), '')
                    )
                ) AS "doctorName",

                p.uhid_no AS "uhidNo"

            FROM ip_transfer_request itr

            INNER JOIN inpatient i
                ON i.inpatient_id = itr.inpatient_id

            INNER JOIN patient p
                ON p.patient_id = itr.patient_id

            LEFT JOIN mas_gender mg
                ON mg.id = p.p_gender_id

            LEFT JOIN mas_ward fw
                ON fw.ward_id = itr.from_ward_id

            LEFT JOIN mas_bed fb
                ON fb.bed_id = itr.from_bed_id

            INNER JOIN mas_ward tw
                ON tw.ward_id = itr.to_ward_id

            INNER JOIN mas_bed tb
                ON tb.bed_id = itr.to_bed_id

            LEFT JOIN mas_ipd_transfer_reason mtr
                ON mtr.transfer_reason_id = itr.transfer_reason_id

            LEFT JOIN users usr
                ON usr.user_id = itr.doctor_id

          WHERE (
        itr.to_ward_id IN (:wardIds)
        OR itr.from_ward_id IN (:wardIds)
      )
  AND LOWER(itr.transfer_status) = LOWER(:transferStatus)
            ORDER BY itr.request_datetime DESC
            """, nativeQuery = true)
        List<PendingToTransferProjectionResponse>
        findPendingTransferRequestsByWardId(@Param("wardIds") List<Long> wardIds,
                                            @Param("transferStatus") String transferStatus
        );




    Optional<IpTransferRequest> findByInpatient_InpatientIdAndTransferStatusIgnoreCase(Long inpatientId, String ipdBedTransferStatus);
}
