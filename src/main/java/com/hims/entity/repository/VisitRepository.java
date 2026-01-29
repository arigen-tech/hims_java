package com.hims.entity.repository;

import com.hims.entity.*;
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

    @Query(value = """
    SELECT v.* FROM visit v
    JOIN patient p ON p.patient_id = v.patient_id
    WHERE v.visit_status = 'c'
      AND (
            CAST(:visitDate AS DATE) IS NULL
            OR DATE(v.visit_date) = CAST(:visitDate AS DATE)
      )
      AND (
            :mobile = '' 
            OR p.p_mobile_number = :mobile
      )
      AND (
            :name = '' 
            OR LOWER(p.p_fn) LIKE '%' || LOWER(:name) || '%'
            OR LOWER(p.p_mn) LIKE '%' || LOWER(:name) || '%'
            OR LOWER(p.p_ln) LIKE '%' || LOWER(:name) || '%'
      )
""", nativeQuery = true)
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




    @Query(value = """
SELECT v.* FROM visit v
JOIN patient p ON p.patient_id = v.patient_id
WHERE v.visit_status = 'n'
  AND v.billing_status = 'y'
  AND DATE(v.visit_date) = :visitDate   -- match only the date part
  AND (:doctorId IS NULL OR v.doctor_id = :doctorId)
  AND (:sessionId IS NULL OR v.session_id = :sessionId)
  AND (:employeeNo IS NULL OR p.uhid_no = :employeeNo)
  AND (
        :patientName IS NULL OR
        LOWER(
            CAST(p.p_fn AS VARCHAR) || ' ' ||
            CAST(p.p_mn AS VARCHAR) || ' ' ||
            CAST(p.p_ln AS VARCHAR)
        ) LIKE LOWER(CONCAT('%', :patientName, '%'))
      )
""", nativeQuery = true)
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
            "visit_id != :currentVisitId " +  // <-- Current visit ko exclude karo
            "ORDER BY visit_date DESC " +
            "LIMIT 1",
            nativeQuery = true)
    Optional<Visit> findPreviousVisit(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("departmentId") Long departmentId,
            @Param("hospitalId") Long hospitalId,
            @Param("currentVisitId") Long currentVisitId);


    boolean existsByDepartment_IdAndDoctor_UserIdAndVisitDateAndSession_IdAndTokenNo(
            Long departmentId,
            Long doctorId,
            Instant visitDate,
            Long sessionId,
            Long tokenNo
    );


}
