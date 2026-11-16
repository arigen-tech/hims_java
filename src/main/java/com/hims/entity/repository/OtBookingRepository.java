package com.hims.entity.repository;

import com.hims.entity.OtBooking;
import com.hims.projection.OtBookingHistoryProjection;
import com.hims.projection.OtDashboardProjection;
import com.hims.projection.OtDepartmentProjection;
import com.hims.projection.OtBookingScheduleProjection;
import com.hims.projection.PacPendingProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OtBookingRepository extends JpaRepository<OtBooking,Long> {

    @Query(value = """
            SELECT ob.ot_booking_id AS otBookingId,
                   ob.booking_no AS bookingNo,
                   obrh.ot_booking_request_id AS otBookingRequestId,
                   p.patient_id AS patientId,
                   CONCAT_WS(' ', NULLIF(TRIM(p.p_fn), ''), NULLIF(TRIM(p.p_mn), ''), NULLIF(TRIM(p.p_ln), '')) AS patientName,
                   CASE WHEN UPPER(COALESCE(obrh.request_source, '')) = 'IPD' THEN NULL ELSE p.uhid_no END AS uhidNo,
                   CASE WHEN UPPER(COALESCE(obrh.request_source, '')) = 'IPD' THEN ip.admission_no ELSE NULL END AS ipNo,
                   obrh.request_source AS patientType,
                   md.department_name AS departmentName,
                   CONCAT_WS(' ', NULLIF(TRIM(surg.first_name), ''), NULLIF(TRIM(surg.middle_name), ''), NULLIF(TRIM(surg.last_name), '')) AS primarySurgeonName,
                   obrh.diagnosis AS diagnosis,
                   ob.scheduled_date AS scheduledDate,
                   ob.scheduled_start_time AS scheduledStartTime,
                   ob.scheduled_end_time AS scheduledEndTime,
                   mot.ot_name AS otName,
                   CASE WHEN UPPER(COALESCE(obrh.request_source, '')) = 'IPD' THEN COALESCE(iba.ward_name, ipw.ward_name) ELSE NULL END AS wardName,
                   CASE WHEN UPPER(COALESCE(obrh.request_source, '')) = 'IPD' THEN COALESCE(iba.bed_number, ipb.bed_number) ELSE NULL END AS bedNumber,
                   (SELECT CAST(JSON_AGG(JSON_BUILD_OBJECT('surgeryId', surgery.surgery_id,
                                                           'surgeryName', surgery.surgery_name)
                                         ORDER BY surgery.surgery_name) AS text)
                    FROM (SELECT DISTINCT ms.surgery_id, ms.surgery_name
                          FROM ot_booking_request_dt dt
                          LEFT JOIN mas_surgery ms ON ms.surgery_id = dt.surgery_id
                          WHERE dt.ot_booking_request_id = obrh.ot_booking_request_id
                            AND ms.surgery_id IS NOT NULL) surgery) AS surgeries
            FROM ot_booking ob
            JOIN mas_ot_booking_status obs ON obs.booking_status_id = ob.booking_status_id
            JOIN ot_booking_request_hd obrh ON obrh.ot_booking_request_id = ob.ot_booking_request_id
            JOIN patient p ON p.patient_id = obrh.patient_id
            LEFT JOIN visit v ON v.visit_id = obrh.visit_id
            LEFT JOIN inpatient ip ON ip.inpatient_id = obrh.admission_id
            LEFT JOIN mas_department md ON md.department_id = obrh.department_id
            LEFT JOIN users surg ON surg.user_id = obrh.primary_surgeon_id
            LEFT JOIN mas_operation_theatre mot ON mot.ot_id = ob.ot_id
            LEFT JOIN LATERAL (
                SELECT w.ward_name, b.bed_number
                FROM ip_bed_allocation a
                LEFT JOIN mas_ward w ON w.ward_id = a.ward_id
                LEFT JOIN mas_bed b ON b.bed_id = a.bed_id
                WHERE a.ip_admission_id = ip.inpatient_id
                  AND a.allocation_end_date IS NULL
                ORDER BY a.allocation_start_date DESC NULLS LAST, a.bed_allocation_id DESC
                LIMIT 1
            ) iba ON TRUE
            LEFT JOIN mas_ward ipw ON ipw.ward_id = ip.admitting_ward_id
            LEFT JOIN mas_bed ipb ON ipb.bed_id = ip.bed_id
            WHERE UPPER(obs.status_code) = 'SCHEDULED'
              AND ob.is_pac_required = 'Y'
              AND ob.is_pac_done = 'N'
              AND UPPER(COALESCE(ob.status, 'N')) = 'Y'
              AND ob.cancelled_date IS NULL
              AND ob.cancelled_by IS NULL
              AND (NULLIF(TRIM(:patientType), '') IS NULL
                   OR UPPER(:patientType) = 'ALL'
                   OR UPPER(obrh.request_source) = UPPER(:patientType))
              AND (NULLIF(TRIM(:search), '') IS NULL
                   OR LOWER(COALESCE(p.uhid_no, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(COALESCE(ip.admission_no, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(CONCAT_WS(' ', NULLIF(TRIM(p.p_fn), ''), NULLIF(TRIM(p.p_mn), ''), NULLIF(TRIM(p.p_ln), ''))) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(CONCAT_WS(' ', NULLIF(TRIM(surg.first_name), ''), NULLIF(TRIM(surg.middle_name), ''), NULLIF(TRIM(surg.last_name), ''))) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR EXISTS (SELECT 1
                              FROM ot_booking_request_dt search_dt
                              LEFT JOIN mas_surgery search_s ON search_s.surgery_id = search_dt.surgery_id
                              WHERE search_dt.ot_booking_request_id = obrh.ot_booking_request_id
                                AND LOWER(COALESCE(search_s.surgery_name, '')) LIKE LOWER(CONCAT('%', :search, '%'))))
            ORDER BY ob.scheduled_date, ob.scheduled_start_time, ob.ot_booking_id
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM ot_booking ob
            JOIN mas_ot_booking_status obs ON obs.booking_status_id = ob.booking_status_id
            JOIN ot_booking_request_hd obrh ON obrh.ot_booking_request_id = ob.ot_booking_request_id
            JOIN patient p ON p.patient_id = obrh.patient_id
            LEFT JOIN inpatient ip ON ip.inpatient_id = obrh.admission_id
            LEFT JOIN users surg ON surg.user_id = obrh.primary_surgeon_id
            WHERE UPPER(obs.status_code) = 'SCHEDULED'
              AND ob.is_pac_required = 'Y'
              AND ob.is_pac_done = 'N'
              AND UPPER(COALESCE(ob.status, 'N')) = 'Y'
              AND ob.cancelled_date IS NULL
              AND ob.cancelled_by IS NULL
              AND (NULLIF(TRIM(:patientType), '') IS NULL
                   OR UPPER(:patientType) = 'ALL'
                   OR UPPER(obrh.request_source) = UPPER(:patientType))
              AND (NULLIF(TRIM(:search), '') IS NULL
                   OR LOWER(COALESCE(p.uhid_no, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(COALESCE(ip.admission_no, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(CONCAT_WS(' ', NULLIF(TRIM(p.p_fn), ''), NULLIF(TRIM(p.p_mn), ''), NULLIF(TRIM(p.p_ln), ''))) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(CONCAT_WS(' ', NULLIF(TRIM(surg.first_name), ''), NULLIF(TRIM(surg.middle_name), ''), NULLIF(TRIM(surg.last_name), ''))) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR EXISTS (SELECT 1
                              FROM ot_booking_request_dt search_dt
                              LEFT JOIN mas_surgery search_s ON search_s.surgery_id = search_dt.surgery_id
                              WHERE search_dt.ot_booking_request_id = obrh.ot_booking_request_id
                                AND LOWER(COALESCE(search_s.surgery_name, '')) LIKE LOWER(CONCAT('%', :search, '%'))))
            """, nativeQuery = true)
    Page<PacPendingProjection> findPacPendingList(@Param("search") String search,
                                                  @Param("patientType") String patientType,
                                                  Pageable pageable);

    @Query(value = """
            SELECT ob.ot_booking_id AS otBookingId,
                   ob.ot_booking_request_id AS otBookingRequestId,
                   ob.ot_id AS otId,
                   ob.scheduled_date AS scheduledDate,
                   ob.scheduled_start_time AS scheduledStartTime,
                   ob.scheduled_end_time AS scheduledEndTime,
                   ob.booking_status_id AS bookingStatusId,
                   s.status_code AS statusCode
            FROM ot_booking ob
            JOIN mas_ot_booking_status s ON s.booking_status_id = ob.booking_status_id
            WHERE ob.ot_booking_id = :otBookingId AND ob.status = 'Y'
            """, nativeQuery = true)
    java.util.Optional<OtBookingScheduleProjection> findScheduleForChange(@Param("otBookingId") Long otBookingId);

    @Query(value = "SELECT booking_status_id FROM mas_ot_booking_status WHERE status_code = :statusCode AND status = 'Y'", nativeQuery = true)
    java.util.Optional<Long> findActiveBookingStatusId(@Param("statusCode") String statusCode);

    @Query(value = """
            SELECT DISTINCT md.department_id AS id, md.department_name AS name
            FROM ot_booking ob
            JOIN ot_booking_request_hd obrh ON obrh.ot_booking_request_id = ob.ot_booking_request_id
            JOIN mas_department md ON md.department_id = obrh.department_id
            WHERE md.hospital_id = :hospitalId
            ORDER BY md.department_name
            """, nativeQuery = true)
    java.util.List<OtDepartmentProjection> findDepartmentsByHospitalId(@Param("hospitalId") Long hospitalId);

    @Query(value = """
            WITH filtered_bookings AS (
                SELECT ob.ot_booking_id, ob.scheduled_date, ob.booking_status_id,
                       mot.ot_name, ob.ot_id, obrh.department_id
                FROM ot_booking ob
                JOIN ot_booking_request_hd obrh ON obrh.ot_booking_request_id = ob.ot_booking_request_id
                LEFT JOIN mas_operation_theatre mot ON mot.ot_id = ob.ot_id
                WHERE EXTRACT(MONTH FROM ob.scheduled_date) = :month
                  AND EXTRACT(YEAR FROM ob.scheduled_date) = :year
                  AND (:otId IS NULL OR ob.ot_id = :otId)
                  AND (:departmentId IS NULL OR obrh.department_id = :departmentId)
            ), date_ot_names AS (
                SELECT scheduled_date,
                       STRING_AGG(DISTINCT ot_name, ', ' ORDER BY ot_name) AS ot_names
                FROM filtered_bookings
                GROUP BY scheduled_date
            )
            SELECT don.scheduled_date AS scheduledDate,
                   mobs.booking_status_id AS bookingStatusId,
                   mobs.status_name AS statusName,
                   COALESCE(COUNT(fb.ot_booking_id), 0) AS bookingCount,
                   don.ot_names AS otNames
            FROM date_ot_names don
            JOIN filtered_bookings fb ON fb.scheduled_date = don.scheduled_date
            LEFT JOIN mas_ot_booking_status mobs ON mobs.booking_status_id = fb.booking_status_id
            WHERE mobs.booking_status_id IS NOT NULL
            GROUP BY don.scheduled_date, mobs.booking_status_id, mobs.status_name, don.ot_names
            ORDER BY don.scheduled_date, mobs.booking_status_id
            """, nativeQuery = true)
    java.util.List<OtDashboardProjection> findOtDashboard(@Param("month") int month,
                                                           @Param("year") int year,
                                                           @Param("otId") Long otId,
                                                           @Param("departmentId") Long departmentId);

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
                   obrh.priority AS priority,
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
              AND (:patientId IS NULL OR p.patient_id = :patientId)
              AND (CAST(:scheduledDate AS date) IS NULL OR ob.scheduled_date = CAST(:scheduledDate AS date))
              AND (:ipd IS NULL OR upper(coalesce(obrh.request_source, '')) = CASE WHEN :ipd THEN 'IPD' ELSE 'OPD' END)
              AND (:otId IS NULL OR ob.ot_id = :otId)
              AND (:departmentId IS NULL OR obrh.department_id = :departmentId)
              AND (:patient IS NULL OR :patient = ''
                   OR LOWER(COALESCE(p.p_fn, '') || ' ' || COALESCE(p.p_ln, '')) LIKE LOWER(CONCAT('%', :patient, '%'))
                   OR LOWER(COALESCE(p.uhid_no, '')) LIKE LOWER(CONCAT('%', :patient, '%'))
                   OR LOWER(COALESCE(surg.first_name, '') || ' ' || COALESCE(surg.middle_name, '') || ' ' || COALESCE(surg.last_name, '')) LIKE LOWER(CONCAT('%', :patient, '%'))
                   OR EXISTS (SELECT 1 FROM ot_booking_request_dt search_dt
                              JOIN mas_surgery search_s ON search_s.surgery_id = search_dt.surgery_id
                              WHERE search_dt.ot_booking_request_id = obrh.ot_booking_request_id
                                AND LOWER(COALESCE(search_s.surgery_name, '')) LIKE LOWER(CONCAT('%', :patient, '%')))
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
              AND (:patientId IS NULL OR p.patient_id = :patientId)
              AND (CAST(:scheduledDate AS date) IS NULL OR ob.scheduled_date = CAST(:scheduledDate AS date))
              AND (:ipd IS NULL OR upper(coalesce(obrh.request_source, '')) = CASE WHEN :ipd THEN 'IPD' ELSE 'OPD' END)
              AND (:otId IS NULL OR ob.ot_id = :otId)
              AND (:departmentId IS NULL OR obrh.department_id = :departmentId)
              AND (:patient IS NULL OR :patient = ''
                   OR LOWER(COALESCE(p.p_fn, '') || ' ' || COALESCE(p.p_ln, '')) LIKE LOWER(CONCAT('%', :patient, '%'))
                   OR LOWER(COALESCE(p.uhid_no, '')) LIKE LOWER(CONCAT('%', :patient, '%'))
                   OR LOWER(COALESCE(surg.first_name, '') || ' ' || COALESCE(surg.middle_name, '') || ' ' || COALESCE(surg.last_name, '')) LIKE LOWER(CONCAT('%', :patient, '%'))
                   OR EXISTS (SELECT 1 FROM ot_booking_request_dt search_dt
                              JOIN mas_surgery search_s ON search_s.surgery_id = search_dt.surgery_id
                              WHERE search_dt.ot_booking_request_id = obrh.ot_booking_request_id
                                AND LOWER(COALESCE(search_s.surgery_name, '')) LIKE LOWER(CONCAT('%', :patient, '%')))
                   OR CAST(p.patient_id AS varchar) = :patient)
            """, nativeQuery = true)
    Page<OtBookingHistoryProjection> findBookingHistory(@Param("patient") String patient,
                                                        @Param("patientId") Long patientId,
                                                        @Param("scheduledDate") java.time.LocalDate scheduledDate,
                                                        @Param("ipd") Boolean ipd,
                                                        @Param("statusId") Long statusId,
                                                        @Param("otId") Long otId,
                                                        @Param("departmentId") Long departmentId,
                                                        Pageable pageable);
}
