package com.hims.entity.repository;

import com.hims.entity.BillingHeader;
import com.hims.entity.MasHospital;
import com.hims.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<Visit> findByVisitStatusAndBillingStatus(String visitStatus, String billingStatus);

    List<Visit> findByVisitStatus(String visitStatus);

    @Query(value = """
    SELECT v.* FROM visit v
    JOIN patient p ON p.patient_id = v.patient_id
    WHERE v.visit_status = 'c'
      AND (
            :visitDate IS NULL
            OR DATE(v.visit_date) = :visitDate
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

}
