package com.hims.entity.repository;

import com.hims.entity.*;
import com.hims.projection.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Query(value = """
        SELECT 
            v.visit_id AS visitId,
            p.patient_id AS patientId,
            TRIM(CONCAT(
                COALESCE(p.p_fn, ''), ' ',
                COALESCE(p.p_mn, ''), ' ',
                COALESCE(p.p_ln, '')
            )) AS patientName,
            p.p_age AS patientAge,
            p.p_mobile_number AS mobileNumber,
            p.p_dob AS dob,
            g.gender_name AS gender,
            d.department_name AS departmentName,
            v.visit_date AS opdDate,
            mr.relation_name AS relation,
            
            CASE
                WHEN v.visit_type = 'F' THEN 'Follow Up'
                WHEN v.visit_type = 'N' THEN 'New'
                WHEN v.visit_type = 'W' THEN 'Walk In'
            END AS visitType,
            v.doctor_id AS doctorId,
            v.doctor_name AS doctorName,
            v.token_no AS tokenNo

        FROM visit v
        LEFT JOIN patient p ON p.patient_id = v.patient_id
        LEFT JOIN mas_gender g ON g.id = p.p_gender_id
        LEFT JOIN mas_relation mr ON mr.relation_id = p.p_relation_id
        LEFT JOIN mas_department d ON d.department_id = v.department_id

        WHERE v.hospital_id = :hospitalId
        AND v.department_id = :departmentId
        AND v.pre_consultation = :preConsultation
        AND v.billing_status = :billingStatus
        AND v.visit_status = :visitStatus

        
        AND (
            :patientName IS NULL OR :patientName = '' OR
            LOWER(CONCAT(
                COALESCE(p.p_fn, ''), ' ',
                COALESCE(p.p_mn, ''), ' ',
                COALESCE(p.p_ln, '')
            )) LIKE LOWER(CONCAT('%', :patientName, '%'))
        )


        AND (
            :mobileNumber IS NULL OR :mobileNumber = '' OR
            p.p_mobile_number LIKE CONCAT('%', :mobileNumber, '%')
        )

        ORDER BY v.visit_date ASC, v.start_time ASC
        """,
            countQuery = """
        SELECT COUNT(v.visit_id)
        FROM visit v
        LEFT JOIN patient p ON p.patient_id = v.patient_id

        WHERE v.hospital_id = :hospitalId
        AND v.department_id = :departmentId
        AND v.pre_consultation = :preConsultation
        AND v.billing_status = :billingStatus
        AND v.visit_status = :visitStatus

        AND (
            :patientName IS NULL OR :patientName = '' OR
            LOWER(CONCAT(
                COALESCE(p.p_fn, ''), ' ',
                COALESCE(p.p_mn, ''), ' ',
                COALESCE(p.p_ln, '')
            )) LIKE LOWER(CONCAT('%', :patientName, '%'))
        )

        AND (
            :mobileNumber IS NULL OR :mobileNumber = '' OR
            p.p_mobile_number LIKE CONCAT('%', :mobileNumber, '%')
        )
        """,
            nativeQuery = true)
    Page<PatientWaitingListProjection> findWaitingPatientsByHospital(
            @Param("hospitalId") Long hospitalId,
            @Param("departmentId") Long departmentId,
            @Param("preConsultation") String preConsultation,
            @Param("billingStatus") String billingStatus,
            @Param("visitStatus") String visitStatus,
            @Param("patientName") String patientName,
            @Param("mobileNumber") String mobileNumber,
            Pageable pageable
    );

    @Query(value = """
            SELECT
                v.visit_id AS visitId,
                p.patient_id AS patientId,
                CONCAT(
                    COALESCE(p.p_fn, ''), ' ',
                    COALESCE(p.p_mn, ''), ' ',
                    COALESCE(p.p_ln, '')
                ) AS patientName,
                p.p_age AS patientAge,
                COALESCE(g.gender_name, '') AS gender,
                d.department_id AS departmentId,
                d.department_name AS departmentName,
                p.p_mobile_number AS mobileNumber,
                CASE
                    WHEN v.visit_type = 'F' THEN 'Follow Up'
                    WHEN v.visit_type = 'N' THEN 'New'
                    ELSE 'Walk In'
                END AS visitType,
                v.doctor_id AS doctorId,
                v.doctor_name AS doctorName,
            
                CONCAT(
                    TO_CHAR(v.start_time, 'HH24:MI'),
                    ' - ',
                    TO_CHAR(v.end_time, 'HH24:MI')
                ) AS appointmentTime,
            
                v.token_no AS tokenNumber,
                CAST(v.visit_date AS DATE) AS appointmentDate
            
            FROM visit v
            LEFT JOIN patient p ON p.patient_id = v.patient_id
            LEFT JOIN mas_gender g ON g.id = p.p_gender_id
            LEFT JOIN mas_department d ON d.department_id = v.department_id
            
            WHERE v.hospital_id = :hospitalId
              AND v.pre_consultation = :preConsultation
              AND v.billing_status = :billingStatus
            
            ORDER BY v.visit_date ASC, v.start_time ASC
            """, nativeQuery = true)
    List<OpdPreConsultationProjection> findPendingPreConsultationsByHospital(
            @Param("hospitalId") Long hospitalId,
            @Param("preConsultation") String preConsultation,
            @Param("billingStatus") String billingStatus
    );
    @Query(value = """
        SELECT 
            v.visit_id AS visitId,
            p.patient_id AS patientId,
            TRIM(CONCAT(
                COALESCE(p.p_fn, ''), ' ',
                COALESCE(p.p_mn, ''), ' ',
                COALESCE(p.p_ln, '')
            )) AS patientName,
            p.p_age AS patientAge,
            g.gender_name AS gender,
            d.department_id AS departmentId,
            d.department_name AS departmentName,
            p.p_mobile_number AS mobileNumber,
            CASE
                WHEN v.visit_type = 'N' THEN 'New'
                WHEN v.visit_type = 'F' THEN 'Follow Up'
                ELSE 'Walk In'
            END AS visitType,
            v.doctor_id AS doctorId,
            v.doctor_name AS doctorName,

            CONCAT(
                TO_CHAR(v.start_time, 'HH24:MI'),
                ' - ',
                TO_CHAR(v.end_time, 'HH24:MI')
            ) AS appointmentTime,

            v.token_no AS tokenNumber,
            CAST(v.visit_date AS DATE) AS appointmentDate

        FROM visit v
        LEFT JOIN patient p ON p.patient_id = v.patient_id
        LEFT JOIN mas_gender g ON g.id = p.p_gender_id
        LEFT JOIN mas_department d ON d.department_id = v.department_id

        WHERE v.hospital_id = :hospitalId
        AND v.department_id = :departmentId
            AND DATE(v.visit_date) = CURRENT_DATE
        AND v.pre_consultation = :preConsultation
        AND v.billing_status = :billingStatus
        AND v.visit_status = :visitStatus

        
        AND (
            :patientName IS NULL OR :patientName = '' OR
            LOWER(CONCAT(
                COALESCE(p.p_fn, ''), ' ',
                COALESCE(p.p_mn, ''), ' ',
                COALESCE(p.p_ln, '')
            )) LIKE LOWER(CONCAT('%', :patientName, '%'))
        )

        AND (
            :mobileNumber IS NULL OR :mobileNumber = '' OR
            p.p_mobile_number LIKE CONCAT('%', :mobileNumber, '%')
        )

        ORDER BY v.visit_date ASC, v.start_time ASC
        """,
            countQuery = """
        SELECT COUNT(v.visit_id)
        FROM visit v
        LEFT JOIN patient p ON p.patient_id = v.patient_id

        WHERE v.hospital_id = :hospitalId
        AND v.department_id = :departmentId
        AND v.pre_consultation = :preConsultation
        AND v.billing_status = :billingStatus
        AND v.visit_status = :visitStatus

        AND (
            :patientName IS NULL OR :patientName = '' OR
            LOWER(CONCAT(
                COALESCE(p.p_fn, ''), ' ',
                COALESCE(p.p_mn, ''), ' ',
                COALESCE(p.p_ln, '')
            )) LIKE LOWER(CONCAT('%', :patientName, '%'))
        )

        AND (
            :mobileNumber IS NULL OR :mobileNumber = '' OR
            p.p_mobile_number LIKE CONCAT('%', :mobileNumber, '%')
        )
        """,
            nativeQuery = true)
    Page<OpdPreConsultationProjection> findPendingPreConsultationsByHospitalPaged(
            @Param("hospitalId") Long hospitalId,
            @Param("departmentId") Long departmentId,
            @Param("preConsultation") String preConsultation,
            @Param("billingStatus") String billingStatus,
            @Param("visitStatus") String visitStatus,
            @Param("patientName") String patientName,
            @Param("mobileNumber") String mobileNumber,
            Pageable pageable
    );

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

    Optional<Visit> findByBillingHd_Id(Long billHdId);

    Optional<Visit> findById(Long id);

    List findByPatientId(Integer patient);

    List findByPatientId(Long patient);

    List<Visit> findByVisitStatusAndBillingStatus(String visitStatus, String billingStatus);

    List<Visit> findByVisitStatus(String visitStatus);
    @Query("""
        SELECT
            p.id AS patientId,
            v.id AS visitId,

            p.patientFn AS patientFn,
            p.patientMn AS patientMn,
            p.patientLn AS patientLn,

            p.patientMobileNumber AS patientMobileNumber,
            p.patientDob AS patientDob,
            p.patientAge AS patientAge,

            g.genderName AS genderName,
            r.relationName AS relationName,

            d.id AS departmentId,
            d.departmentName AS departmentName,

            u.userId AS doctorId,
            u.firstName AS doctorFirstName,
            u.middleName AS doctorMiddleName,
            u.lastName AS doctorLastName,

            h.id AS hospitalId

        FROM Visit v

        LEFT JOIN v.patient p
        LEFT JOIN p.patientGender g
        LEFT JOIN p.patientRelation r
        LEFT JOIN v.department d
        LEFT JOIN v.doctor u
        LEFT JOIN p.patientHospital h

        WHERE v.id = :visitId
    """)
    RecallPatientProjection getRecallBasicDetails(
            @Param("visitId") Long visitId
    );

    List<Visit> findByBillingStatusIn(List<String> billingStatus);

    @Query("SELECT COUNT(v) FROM Visit v WHERE v.id = :patientId")
    Long countByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT COUNT(v) FROM Visit v WHERE v.patient.id = :patientId")
    int countByPatientIdAndVisitDate(@Param("patientId") Long patientI);


    @Query(value = """
    SELECT 
        v.visit_id AS visitId,
        v.token_no AS tokenNo,
        v.visit_date AS visitDate,
        v.display_patient_status AS displayPatientStatus,

        p.patient_id AS patientId,
        p.uhid_no AS employeeNo,
        p.p_mobile_number AS mobileNo,
        p.p_dob AS dob,
        p.p_age AS age,
        CONCAT(p.p_fn, ' ', p.p_mn, ' ', p.p_ln) AS patientName,

        g.gender_name AS gender,
        r.relation_name AS relation,

        d.department_id AS deptId,
        d.department_name AS deptName,

        doc.user_id AS docterId,
        CONCAT(doc.first_name, ' ', doc.middle_name, ' ', doc.last_name) AS docterName,

        v.hospital_id AS hospitalId,

        s.id AS sessionId,
        s.session_name AS sessionName

    FROM visit v
    LEFT JOIN patient p ON p.patient_id = v.patient_id
    LEFT JOIN mas_gender g ON g.id = p.p_gender_id
    LEFT JOIN mas_relation r ON r.relation_id = p.p_relation_id
    LEFT JOIN mas_department d ON d.department_id = v.department_id
    LEFT JOIN users doc ON doc.user_id = v.doctor_id
    LEFT JOIN mas_opd_session s ON s.id = v.session_id

    WHERE LOWER(v.visit_status) = :visitStatus
      AND LOWER(v.billing_status) = :billingStatus
      AND DATE(v.visit_date) = :visitDate
      AND (:doctorId IS NULL OR v.doctor_id = :doctorId)
      AND (:sessionId IS NULL OR v.session_id = :sessionId)
      AND (:employeeNo IS NULL OR p.uhid_no = :employeeNo)
      AND (
            :patientName IS NULL OR :patientName = '' OR
            LOWER(CONCAT(p.p_fn, ' ', p.p_mn, ' ', p.p_ln))
            LIKE LOWER(CONCAT('%', :patientName, '%'))
          )
    """, nativeQuery = true)
    List<OpdPatientDetailsWaitingProjection> findActiveVisitsWithFilters(
            @Param("doctorId") Long doctorId,
            @Param("sessionId") Long sessionId,
            @Param("employeeNo") String employeeNo,
            @Param("patientName") String patientName,
            @Param("visitDate") LocalDate visitDate,
            @Param("visitStatus") String visitStatus,
            @Param("billingStatus") String billingStatus

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
            "v.iniDoctor.userId = :doctorId AND " +
            "v.session.id = :sessionId AND " +
            "v.visitDate >= :startOfDay AND v.visitDate < :endOfDay AND " +
            "LOWER(v.visitStatus) IN (LOWER(:completedVisit), LOWER(:pendingVisit)) ")
    List<Long> findOccupiedTokens(
            @Param("departmentId") Long departmentId,
            @Param("doctorId") Long doctorId,
            @Param("sessionId") Long sessionId,
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay,
            @Param("pendingVisit") String pendingVisit,
            @Param("completedVisit") String completedVisit);

    @Query(value = "SELECT v.* FROM visit v WHERE v.patient_id = :patientId " +
            "AND ((DATE(v.visit_date) >= CURRENT_DATE AND v.visit_status = 'n') " +
            "OR (DATE(v.visit_date) = CURRENT_DATE AND v.visit_status = 'y' " +
            "AND v.start_time > CURRENT_TIMESTAMP)) " +
            "ORDER BY v.visit_date ASC, v.visit_status DESC",
            nativeQuery = true)
    List<Visit> findRelevantVisitsByPatientId(@Param("patientId") Long patientId);

    @Query(value = """
                SELECT 
                    v.visit_id AS appointmentId,
                    d.department_id AS specialityId,
                    d.department_name AS specialityName,
                    v.doctor_id AS doctorId,
                    v.doctor_name AS doctorName,
                    s.id AS sessionId,
                    s.session_name AS sessionName,
                    v.visit_date AS visitDate,
                    v.visit_type AS visitType,
                    v.token_no AS tokenNo,
                    v.visit_status AS visitStatus,
                    v.start_time AS startTime,
                    v.end_time AS endTime
                FROM visit v
                LEFT JOIN mas_department d ON v.department_id = d.department_id
                LEFT JOIN mas_opd_session s ON v.session_id = s.id
                WHERE v.patient_id = :patientId
            
                AND d.department_type_id = :opdDepartmentType
            
                AND (
                    (v.visit_date >= CURRENT_DATE AND v.visit_status = 'n')
                    OR 
                    (v.visit_date = CURRENT_DATE AND v.visit_status = 'y' 
                     AND v.start_time > CURRENT_TIMESTAMP)
                )
                ORDER BY v.visit_date ASC, v.visit_status DESC
            """, nativeQuery = true)
    List<AppointmentProjection> findAppointments(Long patientId, Integer opdDepartmentType);

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


    boolean existsByDepartment_IdAndDoctor_UserIdAndVisitDateBetweenAndSession_IdAndTokenNoAndVisitStatusNot(
            Long departmentId,
            Long doctorId,
            Instant startOfDay,
            Instant endOfDay,
            Long sessionId,
            Long tokenNo,
            String visitStatus
    );

    @Query("""
    SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END
    FROM Visit v
    WHERE v.patient.id = :patientId
      AND v.doctor.userId = :doctorId
      AND v.department.id = :departmentId
      AND v.visitDate BETWEEN :startOfDay AND :endOfDay
      AND LOWER(v.visitStatus) <> LOWER(:cancelledStatus)
      AND (:excludeVisitId IS NULL OR v.id <> :excludeVisitId)
    """)
    boolean existsDuplicatePatientAppointment(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("departmentId") Long departmentId,
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay,
            @Param("cancelledStatus") String cancelledStatus,
            @Param("excludeVisitId") Long excludeVisitId
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
        LEFT JOIN mas_appointment_change_reason r 
            ON r.reason_id = v.cancelled_reason_id
        LEFT JOIN billing_header bh 
            ON bh.bill_hd_id = v.billing_hd_id
        WHERE v.hospital_id = :hospitalId

        AND (
              (:patientId IS NOT NULL AND v.patient_id = :patientId)

              OR

              (:mobileNo IS NOT NULL 
               AND :mobileNo <> '' 
               AND p.p_mobile_number = :mobileNo)

              OR

                :patientName IS NULL
                OR LOWER(
                    CONCAT(
                        COALESCE(CAST(p.p_fn AS TEXT), ''), ' ',
                        COALESCE(CAST(p.p_mn AS TEXT), ''), ' ',
                        COALESCE(CAST(p.p_ln AS TEXT), '')
                    )
                ) LIKE LOWER(CONCAT('%', :patientName, '%'))
            )

        AND (:includeAllHistory = true 
             OR v.visit_date >= CURRENT_DATE)

        AND LOWER(v.visit_status) IN (:visitStatus)

        AND v.department_id IN (:departmentIds)

        ORDER BY v.visit_date ASC
        """, nativeQuery = true)
    List<AppointmentHistoryProjection> findAppointmentHistoryByHospitalPatientIdOrMobileAndDepartments(
            @Param("hospitalId") Long hospitalId,
            @Param("patientId") Long patientId,
            @Param("mobileNo") String mobileNo,
            @Param("patientName") String patientName,
            @Param("departmentIds") List<Long> departmentIds,
            @Param("includeAllHistory") Boolean includeAllHistory,
            @Param("visitStatus") String visitStatus
    );


    /**
     * Fetches cancelled appointments based on hospital, department, doctor, date range and cancellation reason
     *
     * @param hospitalId           Hospital ID (required)
     * @param departmentId         Department ID (optional)
     * @param doctorId             Doctor ID (optional)
     * @param fromDate             From date (optional)
     * @param toDate               To date (optional)
     * @param cancellationReasonId Cancellation reason ID (optional)
     * @return List of cancelled appointments
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
                    CASE 
                        WHEN g.gender_name IS NOT NULL THEN g.gender_name
                        ELSE ''
                    END AS gender,
                    v.doctor_id AS doctorId,
                    v.doctor_name AS doctorName,
                    v.department_id AS departmentId,
                    d.department_name AS departmentName,
                    DATE(v.visit_date) AS appointmentDate,
                    CONCAT(
                        TO_CHAR(v.start_time, 'HH24:MI'),
                        ' to ',
                        TO_CHAR(v.end_time, 'HH24:MI')
                    ) AS appointmentTime,
                    v.cancelled_datetime AS cancellationDateTime,
                    v.cancelled_by AS cancelledBy,
                    r.reason_name AS cancellationReason
                FROM visit v
                LEFT JOIN patient p ON p.patient_id = v.patient_id
                LEFT JOIN mas_gender g ON g.id = p.p_gender_id
                LEFT JOIN mas_department d ON d.department_id = v.department_id
                LEFT JOIN mas_appointment_change_reason r ON r.reason_id = v.cancelled_reason_id
                WHERE v.hospital_id = :hospitalId
                AND LOWER(v.visit_status) = 'c'
                AND (:departmentId IS NULL OR v.department_id = :departmentId)
                AND (:doctorId IS NULL OR v.doctor_id = :doctorId)
                AND (DATE(v.visit_date) BETWEEN :fromDate AND :toDate )
                AND (:cancellationReasonId IS NULL OR v.cancelled_reason_id = :cancellationReasonId)
                ORDER BY v.cancelled_datetime DESC
            """,
            nativeQuery = true)
    List<CancelledAppointmentProjection> findCancelledAppointments(
            @Param("hospitalId") Long hospitalId,
            @Param("departmentId") Long departmentId,
            @Param("doctorId") Long doctorId,
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate,
            @Param("cancellationReasonId") Long cancellationReasonId
    );

    @Query(value = """
                SELECT
                    v.visit_id AS visitId,
                    p.patient_id AS patientId,
                    CONCAT(
                        COALESCE(p.p_fn, ''), ' ',
                        COALESCE(p.p_mn, ''), ' ',
                        COALESCE(p.p_ln, '')
                    ) AS patientName,
                    p.p_mobile_number AS mobileNumber,
                    p.p_age AS patientAge,
                    CASE
                        WHEN g.gender_name IS NOT NULL THEN g.gender_name
                        ELSE ''
                    END AS gender,
                    d.department_name AS departmentName,
                    v.doctor_id AS doctorId,
                    v.doctor_name AS doctorName,
                    v.department_id AS departmentId,
                    DATE(v.visit_date) AS appointmentDate,
                    CONCAT(
                        TO_CHAR(v.start_time, 'HH24:MI'),
                        ' to ',
                        TO_CHAR(v.end_time, 'HH24:MI')
                    ) AS appointmentTime,
                    v.cancelled_datetime AS cancellationDateTime,
                    v.cancelled_by AS cancelledBy,
                    r.reason_name AS cancellationReason
                FROM visit v
                LEFT JOIN patient p ON p.patient_id = v.patient_id
                LEFT JOIN mas_gender g ON g.id = p.p_gender_id
                LEFT JOIN mas_department d ON d.department_id = v.department_id
                LEFT JOIN mas_appointment_change_reason r ON r.reason_id = v.cancelled_reason_id
                WHERE v.hospital_id = :hospitalId
                AND LOWER(v.visit_status) = 'c'
                AND v.department_id IN (:departmentIds)
                AND (:doctorId IS NULL OR v.doctor_id = :doctorId)
                AND (DATE(v.visit_date) BETWEEN :fromDate AND :toDate )
                AND (:cancellationReasonId IS NULL OR v.cancelled_reason_id = :cancellationReasonId)
                ORDER BY v.cancelled_datetime DESC
            """,
            nativeQuery = true)
    List<CancelledAppointmentProjection> findCancelledAppointmentsByDepartmentIds(
            @Param("hospitalId") Long hospitalId,
            @Param("departmentIds") List<Long> departmentIds,
            @Param("doctorId") Long doctorId,
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate,
            @Param("cancellationReasonId") Long cancellationReasonId
    );

    /**
     * Appointment Summary Report - Department-wise and Doctor-wise
     * Shows appointment statistics grouped by doctor and department
     *
     * @param hospitalId   Hospital ID (required)
     * @param departmentId Department ID (optional - null for all departments)
     *                     // * @param doctorId Doctor ID (optional - null for all doctors)
     * @param fromDate     Start date (optional)
     * @param toDate       End date (optional)
     * @return List of appointment summary statistics
     */
    @Query(value = """
                SELECT 
                     v.department_id AS departmentId,
                    d.department_name AS departmentName,
                    COUNT(v.visit_id) AS totalCount,
                    COUNT(CASE WHEN LOWER(v.visit_status) = lower(:statusCompete) THEN 1 END) AS completedCount,
                    COUNT(CASE WHEN LOWER(v.visit_status) = lower(:statusCancel) THEN 1 END) AS cancelledCount,
                    COUNT(CASE WHEN LOWER(v.visit_status) = lower(:statusClosed) THEN 1 END) AS noShowCount,
                    COUNT(CASE WHEN LOWER(v.visit_status) = lower(:statusPending) THEN 1 END) AS pendingCount
                FROM visit v
                LEFT JOIN mas_department d ON d.department_id = v.department_id
                WHERE v.hospital_id = :hospitalId
                AND (:departmentId IS NULL OR v.department_id = :departmentId)
                 AND (CAST(:fromDate AS DATE) IS NULL OR CAST(v.visit_date AS DATE) >= CAST(:fromDate AS DATE))
                  AND (CAST(:toDate AS DATE) IS NULL OR CAST(v.visit_date AS DATE) <= CAST(:toDate AS DATE))
                 AND LOWER(v.visit_type) IN (LOWER(:followUpStatus), LOWER(:newStatus))
                GROUP BY  v.department_id, d.department_name
                ORDER BY d.department_name
            """, nativeQuery = true)
    List<AppointmentSummaryDepartmentProjection> getAppointmentSummaryDepartmentWiseReport(
            @Param("hospitalId") Long hospitalId,
            @Param("departmentId") Long departmentId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("statusPending") String statusPending,
            @Param("statusCancel") String statusCancel,
            @Param("statusCompete") String statusCompete,
            @Param("statusClosed") String statusClosed,
            @Param("followUpStatus") String followUpStatus,
            @Param("newStatus") String newStatus

    );

    /**
     * Appointment Summary Report - Department-wise and Doctor-wise
     * Shows appointment statistics grouped by doctor and department
     *
     * @param hospitalId Hospital ID (required)
     *                   // * @param departmentId Department ID (optional - null for all departments)
     * @param doctorId   Doctor ID (optional - null for all doctors)
     * @param fromDate   Start date (optional)
     * @param toDate     End date (optional)
     * @return List of appointment summary statistics
     */
    @Query(value = """
                SELECT 
                    v.doctor_id AS doctorId,
                    v.doctor_name AS doctorName,
            
                    COUNT(v.visit_id) AS totalCount,
                   COUNT(CASE WHEN LOWER(v.visit_status) = lower(:statusCompete) THEN 1 END) AS completedCount,
                    COUNT(CASE WHEN LOWER(v.visit_status) = lower(:statusCancel) THEN 1 END) AS cancelledCount,
                    COUNT(CASE WHEN LOWER(v.visit_status) = lower(:statusClosed) THEN 1 END) AS noShowCount,
                    COUNT(CASE WHEN LOWER(v.visit_status) = lower(:statusPending) THEN 1 END) AS pendingCount
                FROM visit v
                LEFT JOIN mas_department d ON d.department_id = v.department_id
                WHERE v.hospital_id = :hospitalId
                AND (:departmentId IS NULL OR v.department_id = :departmentId)
                  AND (:doctorId IS NULL OR v.doctor_id = :doctorId)
                  AND (CAST(:fromDate AS DATE) IS NULL OR CAST(v.visit_date AS DATE) >= CAST(:fromDate AS DATE))
                  AND (CAST(:toDate AS DATE) IS NULL OR CAST(v.visit_date AS DATE) <= CAST(:toDate AS DATE))
                  AND LOWER(v.visit_type) IN (LOWER(:followUpStatus), LOWER(:newStatus))
                GROUP BY v.doctor_id, v.doctor_name
                ORDER BY  v.doctor_name
            """, nativeQuery = true)
    List<AppointmentSummaryDoctorProjection> getAppointmentSummaryDoctorWiseReport(
            @Param("hospitalId") Long hospitalId,
            @Param("departmentId") Long departmentId,
            @Param("doctorId") Long doctorId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("statusPending") String statusPending,
            @Param("statusCancel") String statusCancel,
            @Param("statusCompete") String statusCompete,
            @Param("statusClosed") String statusClosed,
            @Param("followUpStatus") String followUpStatus,
            @Param("newStatus") String newStatus

    );

    @Query("""
        SELECT 
            FUNCTION('DATE', v.visitDate) AS visitDate,
            v.doctorName AS doctorName,
            v.department.departmentName AS department,
            v.id AS visitId,
            opd.icdDiag AS icdDiag,
            opd.workingDiag AS workingDiag

        FROM Visit v
        INNER JOIN OpdPatientDetail opd ON opd.visit.id = v.id

        WHERE v.patient.id = :patientId
        AND v.hospital.id = :hospitalId
       AND v.visitStatus=:visitStatus

        ORDER BY v.visitDate DESC
    """)
    Page<PreviousOpdVisitProjection> getPreviousOpdVisit(
            @Param("patientId") Long patientId,
            @Param("hospitalId") Long hospitalId,
            @Param("visitStatus") String visitStatus,
              Pageable pageable
    );
    @Query("""
        SELECT 
            FUNCTION('DATE', v.visitDate) AS visitDate,
           opd.height AS height,
           opd.weight AS weight,
           opd.pulse AS pulse,
           opd.temperature AS temperature,
           opd.rr AS rr,
           opd.bmi AS bmi,
           opd.spo2 AS spo2,
           opd.bpSystolic AS bpSystolic,
           opd.bpDiastolic AS bpDiastolic 
           
            FROM Visit v
        INNER JOIN OpdPatientDetail opd ON opd.visit.id = v.id

        WHERE v.patient.id = :patientId
        AND v.hospital.id = :hospitalId
            AND v.visitStatus=:visitStatus
            

        ORDER BY v.visitDate DESC
    """)
    Page<PreviousOpdVitalsDetailsProjection> getPriviousOpdVitalsDetails(@Param("patientId") Long patientId,  @Param("hospitalId") Long hospitalId, @Param("visitStatus") String visitStatus, Pageable pageable);

    @Query(value = """
        SELECT 
            v.visit_id AS visitId,
            p.patient_id AS patientId,
            TRIM(CONCAT(
                COALESCE(p.p_fn, ''), ' ',
                COALESCE(p.p_mn, ''), ' ',
                COALESCE(p.p_ln, '')
            )) AS patientName,
            p.p_age AS patientAge,
            p.p_mobile_number AS mobileNumber,
            p.p_dob AS dob,
            g.gender_name AS gender,
            d.department_name AS departmentName,
            v.visit_date AS opdDate,
            mr.relation_name AS relation,
            
            CASE
                WHEN v.visit_type = 'F' THEN 'Follow Up'
                WHEN v.visit_type = 'N' THEN 'New'
                WHEN v.visit_type = 'W' THEN 'Walk In'
            END AS visitType,
            v.doctor_id AS doctorId,
            v.doctor_name AS doctorName,
            v.token_no AS tokenNo

        FROM visit v
        LEFT JOIN patient p ON p.patient_id = v.patient_id
        LEFT JOIN mas_gender g ON g.id = p.p_gender_id
        LEFT JOIN mas_relation mr ON mr.relation_id = p.p_relation_id
        LEFT JOIN mas_department d ON d.department_id = v.department_id

        WHERE v.hospital_id = :hospitalId
        AND v.department_id = :departmentId
          AND DATE(v.visit_date) = CURRENT_DATE
        AND v.billing_status = :billingStatus
        AND v.visit_status = :visitStatus

        AND (
            :patientName IS NULL OR :patientName = '' OR
            LOWER(CONCAT(
                COALESCE(p.p_fn, ''), ' ',
                COALESCE(p.p_mn, ''), ' ',
                COALESCE(p.p_ln, '')
            )) LIKE LOWER(CONCAT('%', :patientName, '%'))
        )

        AND (
            :mobileNumber IS NULL OR :mobileNumber = '' OR
            p.p_mobile_number LIKE CONCAT('%', :mobileNumber, '%')
        )

        AND (
            :doctorId IS NULL OR v.doctor_id = :doctorId
        )

        AND (
            :sessionId IS NULL OR v.session_id = :sessionId
        )

        ORDER BY v.visit_date ASC, v.start_time ASC
        """,
            countQuery = """
        SELECT COUNT(v.visit_id)
        FROM visit v
        LEFT JOIN patient p ON p.patient_id = v.patient_id

        WHERE v.hospital_id = :hospitalId
        AND v.department_id = :departmentId
        AND v.billing_status = :billingStatus
        AND v.visit_status = :visitStatus

        AND (
            :patientName IS NULL OR :patientName = '' OR
            LOWER(CONCAT(
                COALESCE(p.p_fn, ''), ' ',
                COALESCE(p.p_mn, ''), ' ',
                COALESCE(p.p_ln, '')
            )) LIKE LOWER(CONCAT('%', :patientName, '%'))
        )

        AND (
            :mobileNumber IS NULL OR :mobileNumber = '' OR
            p.p_mobile_number LIKE CONCAT('%', :mobileNumber, '%')
        )

        AND (
            :doctorId IS NULL OR v.doctor_id = :doctorId
        )

        AND (
            :sessionId IS NULL OR v.session_id = :sessionId
        )
        """,
            nativeQuery = true)
    Page<PatientWaitingListProjection> findWaitingPatientsByHospitalWithFilters(
            @Param("hospitalId") Long hospitalId,
            @Param("departmentId") Long departmentId,
            @Param("billingStatus") String billingStatus,
            @Param("visitStatus") String visitStatus,
            @Param("patientName") String patientName,
            @Param("mobileNumber") String mobileNumber,
            @Param("doctorId") Long doctorId,
            @Param("sessionId") Long sessionId,
            Pageable pageable
    );



    @Query("""
    SELECT v.id AS visitId,
          p.id AS patientId,

       CONCAT(
            COALESCE(p.patientFn, ''),
            ' ',
            COALESCE(p.patientMn, ''),
            ' ',
            COALESCE(p.patientLn, '')
        ) AS patientName,

        p.patientMobileNumber AS mobileNo,

        g.genderName AS gender,

        r.relationName AS relation,

        p.patientAge AS age,

        d.departmentName AS deptName,

        CONCAT(
            COALESCE(doc.firstName, ''),
            ' ',
            COALESCE(doc.middleName, ''),
            ' ',
            COALESCE(doc.lastName, '')
        ) AS doctorName

    FROM Visit v

    LEFT JOIN v.patient p
    LEFT JOIN p.patientGender g
    LEFT JOIN p.patientRelation r
    LEFT JOIN p.patientHospital h
    LEFT JOIN v.department d
    LEFT JOIN v.doctor doc

    WHERE

        d.id = :departmentId 
        AND v.visitStatus=:visitStatus

        AND (
            :dateFilter = false
            OR (
                v.visitDate >= :startDate
                AND v.visitDate < :endDate
            )
        )

        AND (
            :mobile = ''
            OR p.patientMobileNumber
                LIKE CONCAT('%', :mobile, '%')
        )

        AND (
            :name = ''
            OR LOWER(
                CONCAT(
                    COALESCE(p.patientFn, ''),
                    ' ',
                    COALESCE(p.patientMn, ''),
                    ' ',
                    COALESCE(p.patientLn, '')
                )
            )
            LIKE LOWER(CONCAT('%', :name, '%'))
        )

    ORDER BY v.id DESC
""")
    Page<OpdRecallVisitProjection> getOpdRecallVisit(

            @Param("name") String name,
            @Param("mobile") String mobile,
            @Param("dateFilter") Boolean dateFilter,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("departmentId") Long departmentId,
            @Param("visitStatus") String visitStatus,
            Pageable pageable);


    @Query(
            value = """
            SELECT
                v.visit_id AS visitId,
                v.patient_id AS patientId,
                v.billing_hd_id AS billingHeaderId,
                p.uhid_no AS registrationNo,

                TRIM(
                    CONCAT(
                        COALESCE(p.p_fn, ''), ' ',
                        COALESCE(p.p_mn, ''), ' ',
                        COALESCE(p.p_ln, '')
                    )
                ) AS patientName,

                p.p_mobile_number AS mobileNo,
                p.p_age AS age,
                g.gender_name AS gender,
                dt.department_type_name AS billingType,
                v.visit_date AS date,
                bh.net_amount AS billingAmount,
                v.cancelled_datetime AS cancelledDate,
                d.department_name AS departmentName,
                rd.refundDate AS refundDate,
                COALESCE(rd.refundStatus, 'PENDING') AS refundStatus

            FROM visit v

            INNER JOIN patient p
                ON p.patient_id = v.patient_id

            LEFT JOIN mas_gender g
                ON g.id = p.p_gender_id

            LEFT JOIN mas_department d
                ON d.department_id = v.department_id

            LEFT JOIN mas_department_type dt
                ON dt.department_type_id = d.department_type_id

            LEFT JOIN (
                SELECT
                    ord.billing_hd_id,
                    MAX(ord.refund_date) AS refundDate,
                    CASE
                        WHEN MAX(ord.processed_date) IS NOT NULL THEN :refundCompletedStatus
                        WHEN MAX(ord.refund_date) IS NOT NULL THEN :refundPendingStatus
                        ELSE :refundDefaultStatus
                    END AS refundStatusCode,
                    CASE
                        WHEN MAX(ord.processed_date) IS NOT NULL THEN :refundCompletedLabel
                        WHEN MAX(ord.refund_date) IS NOT NULL THEN :refundPendingLabel
                        ELSE :refundPendingLabel
                    END AS refundStatus
                FROM opd_refund_details ord
                GROUP BY ord.billing_hd_id
            ) rd
                ON rd.billing_hd_id = v.billing_hd_id

            INNER JOIN billing_header bh
                ON bh.bill_hd_id = v.billing_hd_id

            WHERE LOWER(v.visit_status) = 'c'
              AND LOWER(v.billing_status) = 'y'
              AND COALESCE(bh.net_amount, 0) > 0

              AND (
                  :patientName IS NULL
                  OR :patientName = ''
                  OR LOWER(
                      CONCAT(
                          COALESCE(p.p_fn, ''), ' ',
                          COALESCE(p.p_mn, ''), ' ',
                          COALESCE(p.p_ln, '')
                      )
                  ) LIKE LOWER(CONCAT('%', :patientName, '%'))
              )

              AND (
                  :mobileNo IS NULL
                  OR :mobileNo = ''
                  OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
              )

              AND (
                  :billingService IS NULL
                  OR :billingService = ''
                  OR LOWER(COALESCE(dt.department_type_code, '')) LIKE LOWER(CONCAT('%', :billingService, '%'))
              )

              AND (
                  :refundStatus IS NULL
                  OR :refundStatus = ''
                  OR COALESCE(rd.refundStatusCode, :refundDefaultStatus) = :refundStatus
              )

              AND (
                  :fromDate IS NULL
                  OR CAST(v.cancelled_datetime AS DATE) >= :fromDate
              )

              AND (
                  :toDate IS NULL
                  OR CAST(v.cancelled_datetime AS DATE) <= :toDate
              )

            ORDER BY v.cancelled_datetime DESC
            """,

            countQuery = """
            SELECT COUNT(v.visit_id)

            FROM visit v

            INNER JOIN patient p
                ON p.patient_id = v.patient_id

            LEFT JOIN mas_department d
                ON d.department_id = v.department_id

            LEFT JOIN mas_department_type dt
                ON dt.department_type_id = d.department_type_id

            INNER JOIN billing_header bh
                ON bh.bill_hd_id = v.billing_hd_id

            LEFT JOIN (
                SELECT
                    ord.billing_hd_id,
                    CASE
                        WHEN MAX(ord.processed_date) IS NOT NULL THEN :refundCompletedStatus
                        WHEN MAX(ord.refund_date) IS NOT NULL THEN :refundPendingStatus
                        ELSE :refundDefaultStatus
                    END AS refundStatusCode
                FROM opd_refund_details ord
                GROUP BY ord.billing_hd_id
            ) rd
                ON rd.billing_hd_id = v.billing_hd_id

            WHERE LOWER(v.visit_status) = 'c'
              AND LOWER(v.billing_status) = 'y'
              AND COALESCE(bh.net_amount, 0) > 0

              AND (
                  :patientName IS NULL
                  OR :patientName = ''
                  OR LOWER(
                      CONCAT(
                          COALESCE(p.p_fn, ''), ' ',
                          COALESCE(p.p_mn, ''), ' ',
                          COALESCE(p.p_ln, '')
                      )
                  ) LIKE LOWER(CONCAT('%', :patientName, '%'))
              )

              AND (
                  :mobileNo IS NULL
                  OR :mobileNo = ''
                  OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
              )

              AND (
                  :billingService IS NULL
                  OR :billingService = ''
                  OR LOWER(COALESCE(dt.department_type_code, '')) LIKE LOWER(CONCAT('%', :billingService, '%'))
              )

              AND (
                  :refundStatus IS NULL
                  OR :refundStatus = ''
                  OR COALESCE(rd.refundStatusCode, :refundDefaultStatus) = :refundStatus
              )

              AND (
                  :fromDate IS NULL
                  OR CAST(v.cancelled_datetime AS DATE) >= :fromDate
              )

              AND (
                  :toDate IS NULL
                  OR CAST(v.cancelled_datetime AS DATE) <= :toDate
              )
            """,
            nativeQuery = true
    )
    Page<PaidCancelledAppointmentProjection> getBillingRefundPatientList(
            @Param("patientName") String patientName,
            @Param("mobileNo") String mobileNo,
            @Param("billingService") String billingService,
            @Param("refundStatus") String refundStatus,
            @Param("refundCompletedStatus") String refundCompletedStatus,
            @Param("refundPendingStatus") String refundPendingStatus,
            @Param("refundDefaultStatus") String refundDefaultStatus,
            @Param("refundCompletedLabel") String refundCompletedLabel,
            @Param("refundPendingLabel") String refundPendingLabel,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    @Query("""
    SELECT
        v.id AS visitId,
        p.id AS patientId,
        TRIM(CONCAT(
            COALESCE(p.patientFn, ''), ' ',
            COALESCE(p.patientMn, ''), ' ',
            COALESCE(p.patientLn, '')
        )) AS patientName,
        p.patientMobileNumber AS mobileNumber,
        p.uhidNo AS uhid,
        pr.relationName AS relation,
        g.genderName AS gender,
        p.patientAge AS age,
        d.departmentName AS specialty,
        ph.prescriptionHdId AS prescriptionHdId,
        ph.status AS prescriptionStatus,
        ph.nisNo AS nisNo,
        TRIM(CONCAT(
            COALESCE(u.firstName, ''), ' ',
            COALESCE(u.middleName, ''), ' ',
            COALESCE(u.lastName, '')
        )) AS doctorName,
        CAST(v.visitDate AS string) AS visitDateTime
    FROM Visit v
    JOIN v.patient p
    LEFT JOIN v.iniDoctor u
    LEFT JOIN v.department d
    LEFT JOIN p.patientRelation pr
    LEFT JOIN p.patientGender g
    LEFT JOIN d.departmentType dt
    LEFT JOIN PatientPrescriptionHd ph ON ph.visit.id = v.id
    WHERE v.visitStatus = :visitStatus
      AND dt.departmentTypeCode = :departmentTypeCode
      AND (
          :mobileNo IS NULL
          OR :mobileNo = ''
          OR p.patientMobileNumber LIKE CONCAT('%', :mobileNo, '%')
      )
      AND (
          :patientName IS NULL
          OR :patientName = ''
          OR LOWER(
              TRIM(CONCAT(
                  COALESCE(p.patientFn, ''), ' ',
                  COALESCE(p.patientMn, ''), ' ',
                  COALESCE(p.patientLn, '')
              ))
          ) LIKE LOWER(CONCAT('%', :patientName, '%'))
      )
                AND (
                    :patientId IS NULL
                    OR p.id = :patientId
                )
    ORDER BY v.visitDate DESC
    """)
    Page<OpdReportListProjection> getOpdReportsList(
            @Param("visitStatus") String visitStatus,
            @Param("departmentTypeCode") String departmentTypeCode,
            @Param("mobileNo") String mobileNo,
            @Param("patientName") String patientName,
            @Param("patientId") Long patientId,
            Pageable pageable
    );


    }

