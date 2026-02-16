package com.hims.entity.repository;

import com.hims.entity.BillingHeader;
import com.hims.entity.MasHospital;
import com.hims.entity.Visit;
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

}
