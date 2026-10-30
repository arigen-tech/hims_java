package com.hims.entity.repository;

import com.hims.entity.OtBooking;
import com.hims.projection.OtBookingHistoryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OtBookingRepository extends JpaRepository<OtBooking,Long> {

    @Query(value = """
            SELECT ob.ot_booking_id AS id,
                   obrh.ot_booking_request_id AS headerid,
                   p.patient_id AS patientId, p.uhid_no AS uhidNo,
                   CONCAT_WS(' ', NULLIF(TRIM(p.p_fn), ''), NULLIF(TRIM(p.p_ln), '')) AS patientName,
                   md.department_id AS departmentId, md.department_name AS departmentName,
                   obrh.preferred_date AS preferredDate,
                   obrh.preferred_start_time AS preferredStartTime,
                   obrh.preferred_end_time AS preferredEndTime,
                   mot.ot_id AS otId, mot.ot_name AS otName,
                   ob.is_pac_required AS pacRequired, ob.is_pac_done AS pacDone,
                   ob.booked_date AS bookedDate, ob.booking_no AS bookingNo,
                   obrh.primary_surgeon_id AS primarySurgeonId,
                   CONCAT_WS(' ', NULLIF(TRIM(surg.first_name), ''), NULLIF(TRIM(surg.middle_name), ''),
                             NULLIF(TRIM(surg.last_name), '')) AS surgeonName,
                   ob.cancelled_date AS cancelledDate, ob.cancelled_by AS cancelledBy,
                   mobs.booking_status_id AS bookingStatusId,
                   mobs.status_name AS statusName
            FROM ot_booking ob
            JOIN mas_ot_booking_status mobs ON ob.booking_status_id = mobs.booking_status_id
            JOIN ot_booking_request_hd obrh ON ob.ot_booking_request_id = obrh.ot_booking_request_id
            JOIN patient p ON obrh.patient_id = p.patient_id
            JOIN mas_department md ON obrh.department_id = md.department_id
            LEFT JOIN users surg ON obrh.primary_surgeon_id = surg.user_id
            LEFT JOIN mas_operation_theatre mot ON ob.ot_id = mot.ot_id
            WHERE (:statusId IS NULL OR mobs.booking_status_id = :statusId)
              AND (:patient IS NULL OR :patient = ''
                   OR LOWER(COALESCE(p.p_fn, '') || ' ' || COALESCE(p.p_ln, '')) LIKE LOWER(CONCAT('%', :patient, '%'))
                   OR LOWER(COALESCE(p.uhid_no, '')) LIKE LOWER(CONCAT('%', :patient, '%'))
                   OR CAST(p.patient_id AS varchar) = :patient)
            ORDER BY ob.booked_date DESC, ob.ot_booking_id DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM ot_booking ob
            JOIN mas_ot_booking_status mobs ON ob.booking_status_id = mobs.booking_status_id
            JOIN ot_booking_request_hd obrh ON ob.ot_booking_request_id = obrh.ot_booking_request_id
            JOIN patient p ON obrh.patient_id = p.patient_id
            JOIN mas_department md ON obrh.department_id = md.department_id
            LEFT JOIN users surg ON obrh.primary_surgeon_id = surg.user_id
            LEFT JOIN mas_operation_theatre mot ON ob.ot_id = mot.ot_id
            WHERE (:statusId IS NULL OR mobs.booking_status_id = :statusId)
              AND (:patient IS NULL OR :patient = ''
                   OR LOWER(COALESCE(p.p_fn, '') || ' ' || COALESCE(p.p_ln, '')) LIKE LOWER(CONCAT('%', :patient, '%'))
                   OR LOWER(COALESCE(p.uhid_no, '')) LIKE LOWER(CONCAT('%', :patient, '%'))
                   OR CAST(p.patient_id AS varchar) = :patient)
            """, nativeQuery = true)
    Page<OtBookingHistoryProjection> findBookingHistory(@Param("patient") String patient,
                                                        @Param("statusId") Long statusId,
                                                        Pageable pageable);
}
