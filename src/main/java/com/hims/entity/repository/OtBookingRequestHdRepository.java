package com.hims.entity.repository;

import com.hims.entity.OtBookingRequestHd;
import com.hims.projection.PendingForOtProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OtBookingRequestHdRepository extends JpaRepository<OtBookingRequestHd, Long> {

    Optional<OtBookingRequestHd> findByVisitId(Long visitId);

    @Query(value = """
            SELECT
                h.ot_booking_request_id AS otBookingRequestId,
                i.inpatient_id AS inpatientId,
                h.visit_id AS visitId,
             p.patient_id AS patientId,
               CONCAT(COALESCE(p.p_fn, ''), ' ', COALESCE(p.p_mn, ''), ' ',
                COALESCE(p.p_ln, '')  ) AS patientName,
                p.uhid_no AS uhid,
                p.p_age AS age,
                g.id AS genderId,
                g.gender_name AS gender,
                p.p_mobile_number AS mobileNo,
                i.admission_no AS admissionNo,
                u.user_id AS surgeonId,
              CONCAT(COALESCE(u.first_name, ''), ' ',  COALESCE(u.middle_name, ''),' ',  COALESCE(u.last_name, '') ) AS surgeonName,
                h.request_source AS patientType,
                ot.ot_id AS otId,
                ot.ot_name AS otName,
                CAST(h.requested_date AS date) AS requestedDate,
                CAST(h.requested_date AS time) AS requestedTime,
                h.requested_by AS requestedBy,
                h.request_no AS requestedNo
            
            FROM ot_booking_request_hd h
            LEFT JOIN patient p
                ON p.patient_id = h.patient_id           
            LEFT JOIN mas_gender g
                ON g.id = p.p_gender_id         
            LEFT JOIN inpatient i
                ON i.inpatient_id = h.admission_id           
            LEFT JOIN users u
                ON u.user_id = h.primary_surgeon_id           
            LEFT JOIN mas_operation_theatre ot
                ON ot.ot_id = h.preferred_ot_id          
            LEFT JOIN ot_booking_request_dt d
                ON d.ot_booking_request_id = h.ot_booking_request_id   
         WHERE h.booking_status_id =:surgeryBookingStatusRequested
               AND (
                    :patientName IS NULL
                    OR LOWER(CONCAT(
                        COALESCE(p.p_fn, ''),
                        ' ',
                        COALESCE(p.p_ln, '')
                    )) LIKE LOWER(CONCAT('%', :patientName, '%'))
                  )
              AND (
                    :mobileNo IS NULL
                    OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
                  )
              AND (
                   :patientType IS NULL
                 OR LOWER(h.request_source) LIKE LOWER(CONCAT('%', :patientType, '%'))
                  )
            ORDER BY h.ot_booking_request_id DESC
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT h.ot_booking_request_id)
                    FROM ot_booking_request_hd h
                    
                    LEFT JOIN patient p
                        ON p.patient_id = h.patient_id
                    
                    WHERE h.booking_status_id =:surgeryBookingStatusRequested
                      AND (
                            :patientName IS NULL
                            OR LOWER(CONCAT(
                                COALESCE(p.p_fn, ''),
                                ' ',
                                COALESCE(p.p_ln, '')
                            )) LIKE LOWER(CONCAT('%', :patientName, '%'))
                          )
                    
                      AND (
                            :mobileNo IS NULL
                            OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
                          )
                    
                      AND (
                           :patientType IS NULL
                           OR LOWER(h.request_source) LIKE LOWER(CONCAT('%', :patientType, '%'))
                          )
                    """,
            nativeQuery = true)
    Page<PendingForOtProjection> findPendingForOt(
           @Param("surgeryBookingStatusRequested") Long surgeryBookingStatusRequested,
            @Param("patientName") String patientName,
            @Param("mobileNo") String mobileNo,
            @Param("patientType") String patientType,
            Pageable pageable
    );
}