package com.hims.entity.repository;

import com.hims.entity.*;
import com.hims.projection.AppointmentHistoryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.hims.constants.AppConstants.*;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query("SELECT MAX(v.tokenNo) FROM Visit v " +
                "WHERE v.doctor.userId = :doctorId " +
                "AND v.hospital.id = :hospitalId " +
                "AND v.session.id = :sessionId " +
                "AND FUNCTION('DATE', v.visitDate) = CURRENT_DATE")
    Long findMaxTokenForSessionToday(@Param("doctorId") Long doctorId,
                                     @Param("hospitalId") Long hospitalId,
                                     @Param("sessionId") Long sessionId);

    @Query("SELECT v.tokenNo FROM Visit v " +
            "WHERE v.doctor.userId = :doctorId " +
            "AND v.hospital.id = :hospitalId " +
            "AND v.session.id = :sessionId " +
            "AND FUNCTION('DATE', v.visitDate) = CURRENT_DATE " +
            "ORDER BY v.tokenNo ASC")
    List<Long> findAllTokensForSessionToday(@Param("doctorId") Long doctorId,
                                            @Param("hospitalId") Long hospitalId,
                                            @Param("sessionId") Long sessionId);


//    List<Visit> findByHospitalAndPreConsultationAndBillingStatus(MasHospital hospital,String preConsultation, String billingStatus);

    @Query("SELECT v FROM Visit v WHERE v.hospital = :hospital AND v.preConsultation = :preConsultation AND v.billingStatus = :billingStatus")
    List<Visit> findByHospitalAndPreConsultationAndBillingStatus(@Param("hospital") MasHospital hospital,
                                                                 @Param("preConsultation") String preConsultation,
                                                                 @Param("billingStatus") String billingStatus);



    @Query(value = "SELECT COUNT(v.token_no) FROM visit v " +
            "WHERE v.hospital_id = :hospitalId " +
            "AND v.department_id = :departmentId " +
            "AND DATE(v.visit_date) = CURRENT_DATE",
            nativeQuery = true)
    Long countTokensForToday(@Param("hospitalId") Long hospitalId,
                             @Param("departmentId") Long departmentId);
//    @Query("SELECT COUNT(v.tokenNo) FROM Visit v " +
//            "WHERE v.hospital.id = :hospitalId " +
//            "AND v.department.id = :departmentId " +
//            "AND FUNCTION('DATE', v.visitDate) = CURRENT_DATE")
//    Long countTokensForToday(@Param("hospitalId") Long hospitalId,
//                             @Param("departmentId") Long departmentId);



    Visit findByBillingHd(BillingHeader obj);

    Optional<Visit> findById(Long id);

    List findByPatientId(Integer patient);

    List findByPatientId(Long patient);

    List<Visit> findByVisitStatusAndBillingStatus(String visitStatus, String billingStatus);

    List<Visit> findByVisitStatus(String visitStatus);

    @Query("""
    SELECT v FROM Visit v
    JOIN v.patient p
    WHERE v.visitStatus = 'c'
      AND (
            :visitDate IS NULL
            OR CAST(v.visitDate AS date) = :visitDate
      )
      AND (
            :mobile = '' 
            OR p.patientMobileNumber = :mobile
      )
      AND (
            :name = '' 
            OR LOWER(p.patientFn) LIKE '%' || LOWER(:name) || '%'
            OR LOWER(p.patientMn) LIKE '%' || LOWER(:name) || '%'
            OR LOWER(p.patientLn) LIKE '%' || LOWER(:name) || '%'
      )
    """)
    List<Visit> searchRecallVisits(
            @Param("visitDate") LocalDate visitDate,
            @Param("mobile") String mobile,
            @Param("name") String name
    );

    List<Visit> findByBillingStatusIn(List<String> billingStatus);

    @Query("SELECT COUNT(v) FROM Visit v WHERE v.id = :patientId")
    Long countByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(v) FROM Visit v WHERE v.patient.id = :patientId AND DATE(v.visitDate) = :visitDate")
    int countByPatientIdAndVisitDate(@Param("patientId") Long patientId,
                                     @Param("visitDate") Instant visitDate);




    @Query("""
    SELECT v FROM Visit v
    JOIN v.patient p
    WHERE v.visitStatus = 'n'
      AND v.billingStatus = 'y'
      AND CAST(v.visitDate AS date) = :visitDate
      AND (:doctorId IS NULL OR v.doctor.userId = :doctorId)
      AND (:sessionId IS NULL OR v.session.id = :sessionId)
      AND (:employeeNo IS NULL OR p.uhidNo = :employeeNo)
      AND (
            :patientName IS NULL OR
            LOWER(
                CONCAT(COALESCE(p.patientFn, ''), ' ', COALESCE(p.patientMn, ''), ' ', COALESCE(p.patientLn, ''))
            ) LIKE LOWER(CONCAT('%', :patientName, '%'))
          )
    """)
    List<Visit> findActiveVisitsWithFilters(
            @Param("doctorId") Long doctorId,
            @Param("sessionId") Long sessionId,
            @Param("employeeNo") String employeeNo,
            @Param("patientName") String patientName,
            @Param("visitDate") LocalDate visitDate
    );



    @Query("""
    SELECT v FROM Visit v 
    WHERE v.doctor.userId = :doctorId 
    AND CAST(v.visitDate AS date) = CAST(:visitDate AS date)
    AND v.displayPatientStatus = :status
""")
    Optional<Visit> findCpVisit(Long doctorId, Instant visitDate, String status);


    @Query("""
    SELECT v FROM Visit v 
    WHERE v.doctor.userId = :doctorId
    AND CAST(v.visitDate AS date) = CAST(:visitDate AS date)
    AND v.tokenNo > :tokenNo
    ORDER BY v.tokenNo ASC
""")
    List<Visit> findNextVisits(Long doctorId, Instant visitDate, Long tokenNo);

    Optional<Visit> findByPatientIdAndVisitDateAndSessionId(
            Long patientId,
            Instant visitDate,
            Long sessionId
    );

    @Query("SELECT v FROM Visit v WHERE v.patient.id = :patientId AND DATE(v.visitDate) = DATE(CURRENT_TIMESTAMP)")
    List<Visit> findTodayVisitsByPatientId(@Param("patientId") Long patientId);


    @Query("SELECT v.tokenNo FROM Visit v WHERE " +
            "v.department.id = :departmentId AND " +
            "v.doctor.id = :doctorId AND " +
            "v.session.id = :sessionId AND " +
            "v.visitDate >= :startOfDay AND v.visitDate < :endOfDay AND " +
            "v.visitStatus NOT IN ('c')")
    List<Long> findOccupiedTokens(
            @Param("departmentId") Long departmentId,
            @Param("doctorId") Long doctorId,
            @Param("sessionId") Long sessionId,
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay
    );

    @Query(value = "SELECT v.* FROM visit v WHERE v.patient_id = :patientId " +
            "AND ((DATE(v.visit_date) >= CURRENT_DATE AND v.visit_status = 'n') " +
            "OR (DATE(v.visit_date) = CURRENT_DATE AND v.visit_status = 'y' " +
            "AND v.start_time > CURRENT_TIMESTAMP)) " +  // TIMESTAMP compare
            "ORDER BY v.visit_date ASC, v.visit_status DESC",
            nativeQuery = true)
    List<Visit> findRelevantVisitsByPatientId(@Param("patientId") Long patientId);

    List<Visit> findByVisitStatusIgnoreCase(String n);

    List<Visit> findByVisitStatusInIgnoreCase(List<String> y);
    @Query("""
    SELECT v
    FROM Visit v
    WHERE LOWER(v.visitStatus) = 'n'
      AND v.visitDate >= :startDate
""")
    List<Visit> findNVisitsFromToday(@Param("startDate") Instant startDate);

    @Query(value = "SELECT * FROM visit WHERE " +
            "patient_id = :patientId AND " +
            "doctor_id = :doctorId AND " +
            "department_id = :departmentId AND " +
            "hospital_id = :hospitalId AND " +
            "visit_id != :currentVisitId " +  // <-- Current visit excluded
            "ORDER BY visit_date DESC " +
            "LIMIT 1",
            nativeQuery = true)
    Optional<Visit> findPreviousVisit(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("departmentId") Long departmentId,
            @Param("hospitalId") Long hospitalId,
            @Param("currentVisitId") Long currentVisitId);


    boolean existsByDepartment_IdAndDoctor_UserIdAndVisitDateBetweenAndSession_IdAndTokenNo(
            Long departmentId,
            Long doctorId,
            Instant startOfDay,
            Instant endOfDay,
            Long sessionId,
            Long tokenNo
    );


    @Query("""
        select v
        from Visit v
        where v.hospital.id = :hospitalId
          and v.patient.id  = :patientId
          and lower(v.visitStatus) in ('y','c','n')
        order by v.visitDate asc
    """)
    List<Visit> findHistoryByHospitalAndPatient(
            @Param("hospitalId") Long hospitalId,
            @Param("patientId") Long patientId
    );


    @Query("""
        select v
        from Visit v
        join v.patient p
        where (:hospitalId is null or v.hospital.id = :hospitalId)
          and lower(v.visitStatus) = 'n'
          and v.visitDate >= :fromDate
          and (
                :mobileNo is null or :mobileNo = ''
                or p.patientMobileNumber = :mobileNo
              )
        order by v.visitDate asc
    """)
    List<Visit> findUpcomingByHospitalAndMobile(
            @Param("hospitalId") Long hospitalId,
            @Param("fromDate") Instant fromDate,
            @Param("mobileNo") String mobileNo
    );

    @Query("""
        select v
        from Visit v
        where v.hospital.id = :hospitalId
          and v.patient.id  = :patientId
          and lower(v.visitStatus) in ('y','c','n')
        order by v.visitDate asc
    """)
    List<Visit> findAppointmentHistoryByHospitalAndPatient(
            @Param("hospitalId") Long hospitalId,
            @Param("patientId") Long patientId
    );

  /*
     This query is used to fetch appointment history for a patient based on hospital ID, patient ID or mobile number, and department IDs.
     It retrieves details such as visit ID, patient name, doctor name, department name, appointment date and time, visit status, reason for cancellation (if any), payment status, billed amount, and billing header ID.
   */
    @Query(value = """
        SELECT 
            v.visit_id AS visitId,
            v.patient_id AS patientId,
            CONCAT(
                COALESCE(p.p_fn, ''), ' ',
                COALESCE(p.p_mn, ''), ' ',
                COALESCE(p.p_ln, '')
            ) AS patientName,
            p.p_mobile_number AS mobileNumber,
            p.p_age AS patientAge,
            v.doctor_id AS doctorId,
            v.doctor_name AS doctorName,
            v.department_id AS departmentId,
            d.department_name AS departmentName,
            v.visit_date AS appointmentDate,
            v.start_time AS appointmentStartTime,
            v.end_time AS appointmentEndTime,
            v.visit_status AS visitStatus,
            r.reason_name AS reason,
            v.billing_status AS paymentStatus,
            bh.net_amount AS billedAmount,
            v.billing_hd_id AS billingHeaderId
        FROM visit v
        LEFT JOIN patient p ON p.patient_id = v.patient_id
        LEFT JOIN mas_department d ON d.department_id = v.department_id
        LEFT JOIN mas_appointment_change_reason r ON r.reason_id = v.cancelled_reason_id
        LEFT JOIN billing_header bh ON bh.bill_hd_id = v.billing_hd_id
        WHERE v.hospital_id = :hospitalId
                   
        AND (
                  (:patientId IS NOT NULL AND v.patient_id = :patientId)
        OR
                (:mobileNo IS NOT NULL AND :mobileNo <> '' AND p.p_mobile_number = :mobileNo)
            )
                  
        AND LOWER(v.visit_status) IN ('y', 'c', 'n')
        AND v.department_id IN (:departmentIds)
        ORDER BY v.visit_date ASC
    """, nativeQuery = true)
   List<AppointmentHistoryProjection> findAppointmentHistoryByHospitalPatientIdOrMobileAndDepartments(
            @Param("hospitalId") Long hospitalId,
            @Param("patientId") Long patientId,
            @Param("mobileNo") String mobileNo,
            @Param("departmentIds") List<Long> departmentIds
    );


}
