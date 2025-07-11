package com.hims.entity.repository;


import com.hims.entity.MasGender;
import com.hims.entity.MasRelation;
import com.hims.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByPatientMobileNumberAndPatientRelation(String pMobileNumber,MasRelation pRelation);

    @Query("SELECT p FROM Patient p WHERE p.patientFn = :firstName AND p.patientLn = :lastName AND p.patientGender = :gender " +
            "AND (p.patientDob = :dob OR p.patientAge = :age) AND p.patientMobileNumber = :mobileNumber AND p.patientRelation = :relation")
    Optional<Patient> findByUniqueCombination(String firstName, String lastName, MasGender gender,
                                              LocalDate dob, String age, String mobileNumber, MasRelation relation);
    @Query("SELECT p FROM Patient p " +
            "JOIN Visit v ON v.patient.id = p.id " +
            "WHERE " +
            "(:mobileNo IS NULL OR LOWER(p.patientMobileNumber) LIKE LOWER(CONCAT('%', :mobileNo, '%'))) AND " +
            "(:patientName IS NULL OR " +
            "LOWER(p.patientFn) LIKE LOWER(CONCAT('%', :patientName, '%')) OR " +
            "LOWER(p.patientMn) LIKE LOWER(CONCAT('%', :patientName, '%')) OR " +
            "LOWER(p.patientLn) LIKE LOWER(CONCAT('%', :patientName, '%'))" +
            ") AND " +
            "(:uhidNo IS NULL OR LOWER(p.uhidNo) LIKE LOWER(CONCAT('%', :uhidNo, '%'))) AND " +
            "(:appointmentDate IS NULL OR FUNCTION('DATE', v.visitDate) = :appointmentDate)"
    )
    List<Patient> searchPatients(@Param("mobileNo") String mobileNo,
                                 @Param("patientName") String patientName,
                                 @Param("uhidNo") String uhidNo,
                                 @Param("appointmentDate") LocalDate appointmentDate);
    @Query("SELECT p FROM Patient p " +
            "WHERE (:mobileNo IS NULL OR LOWER(p.patientMobileNumber) LIKE LOWER(CONCAT('%', :mobileNo, '%'))) " +
            "OR (:patientName IS NULL OR LOWER(p.patientFn) LIKE LOWER(CONCAT('%', :patientName, '%')) " +
            "     OR LOWER(p.patientMn) LIKE LOWER(CONCAT('%', :patientName, '%')) " +
            "     OR LOWER(p.patientLn) LIKE LOWER(CONCAT('%', :patientName, '%'))) " +
            "OR (:uhidNo IS NULL OR LOWER(p.uhidNo) LIKE LOWER(CONCAT('%', :uhidNo, '%')))")
    List<Patient> searchPatients(
            @Param("mobileNo") String mobileNo,
            @Param("patientName") String patientName,
            @Param("uhidNo") String uhidNo
    );

    boolean existsByPatientFnAndPatientDobAndPatientGenderIdAndPatientMobileNumberAndPatientRelationId(String trim, LocalDate parse, Long gender, String trim1, Long relation);

}
