package com.hims.entity.repository;


import com.hims.entity.MasGender;
import com.hims.entity.MasRelation;
import com.hims.entity.Patient;
//import com.hims.projection.CancellationReportProjection;
import com.hims.projection.PatientProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByPatientMobileNumberAndPatientRelation(String pMobileNumber, MasRelation pRelation);

    @Query("SELECT p FROM Patient p WHERE p.patientFn = :firstName AND p.patientLn = :lastName AND p.patientGender = :gender " +
            "AND (p.patientDob = :dob OR p.patientAge = :age) AND p.patientMobileNumber = :mobileNumber AND p.patientRelation = :relation")
    Optional<Patient> findByUniqueCombination(String firstName, String lastName, MasGender gender,
                                              LocalDate dob, String age, String mobileNumber, MasRelation relation);

    @Query("""
            SELECT 
            CONCAT(p.patientFn, ' ', 
                   COALESCE(p.patientMn, ''), ' ', 
                   COALESCE(p.patientLn, '')) AS fullName,
            p.patientMobileNumber AS patientMobileNumber,
            p.uhidNo AS uhidNo,
            p.patientAge AS patientAge,
            g.genderName AS gender,
            p.patientEmailId AS patientEmailId
            FROM Patient p
            LEFT JOIN p.patientGender g
            WHERE (:mobileNo IS NULL OR p.patientMobileNumber = :mobileNo)
            AND (:patientName IS NULL OR LOWER(CONCAT(p.patientFn,' ',p.patientMn,' ',p.patientLn))
                LIKE LOWER(CONCAT('%', :patientName, '%')))
            """)
    List<PatientProjection> searchPatients(@Param("mobileNo") String mobileNo,
                                           @Param("patientName") String patientName);

    @Query("""
            SELECT 
            CONCAT(p.patientFn, ' ', 
                   COALESCE(p.patientMn, ''), ' ', 
                   COALESCE(p.patientLn, '')) AS fullName,
            p.patientMobileNumber AS patientMobileNumber,
            p.uhidNo AS uhidNo,
            p.patientAge AS patientAge,
            g.genderName AS gender,
            p.patientEmailId AS patientEmailId
            FROM Patient p
            LEFT JOIN p.patientGender g
            WHERE (:mobileNo IS NULL OR p.patientMobileNumber = :mobileNo)
            AND (:patientName IS NULL OR LOWER(CONCAT(p.patientFn,' ',p.patientMn,' ',p.patientLn))
                LIKE LOWER(CONCAT('%', :patientName, '%')))
            """)
    Page<PatientProjection> searchPatients(@Param("mobileNo") String mobileNo,
                                           @Param("patientName") String patientName,
                                           Pageable pageable);


    @Query(value = """
            SELECT DISTINCT p.* FROM patient p 
            LEFT JOIN appointment a ON p.patient_id = a.patient_id
            WHERE (:mobileNo IS NULL OR CAST(p.p_mobile_number AS TEXT) ILIKE CONCAT('%', :mobileNo, '%'))
            AND (:patientName IS NULL OR 
                 CAST(p.p_fn AS TEXT) ILIKE CONCAT('%', :patientName, '%') OR 
                 CAST(p.p_mn AS TEXT) ILIKE CONCAT('%', :patientName, '%') OR 
                 CAST(p.p_ln AS TEXT) ILIKE CONCAT('%', :patientName, '%'))
            AND (:uhidNo IS NULL OR CAST(p.uhid_no AS TEXT) ILIKE CONCAT('%', :uhidNo, '%'))
            AND (:appointmentDate IS NULL OR DATE(a.appointment_date) = :appointmentDate)
            """, nativeQuery = true)
    List<Patient> searchPatients(@Param("mobileNo") String mobileNo,
                                 @Param("patientName") String patientName,
                                 @Param("uhidNo") String uhidNo,
                                 @Param("appointmentDate") LocalDate appointmentDate);

    @Query("""
            SELECT p FROM Patient p
            LEFT JOIN FETCH p.patientGender
            LEFT JOIN FETCH p.patientRelation
            WHERE (:mobileNo IS NULL OR p.patientMobileNumber LIKE %:mobileNo%)
            AND (:patientName IS NULL OR 
                 p.patientFn LIKE %:patientName% OR
                 p.patientMn LIKE %:patientName% OR
                 p.patientLn LIKE %:patientName%)
            AND (:uhidNo IS NULL OR p.uhidNo LIKE %:uhidNo%)
            """)
    List<Patient> searchPatients(@Param("mobileNo") String mobileNo,
                                 @Param("patientName") String patientName,
                                 @Param("uhidNo") String uhidNo);



    boolean existsByPatientFnAndPatientDobAndPatientGenderIdAndPatientMobileNumberAndPatientRelationId(String trim, LocalDate parse, Long gender, String trim1, Long relation);

}
