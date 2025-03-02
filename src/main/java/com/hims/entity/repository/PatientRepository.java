package com.hims.entity.repository;

import com.hims.dto.MasGenderDto;
import com.hims.dto.MasRelationDto;
import com.hims.entity.MasGender;
import com.hims.entity.MasRelation;
import com.hims.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByPatientMobileNumberAndPatientRelation(String pMobileNumber,MasRelation pRelation);

    @Query("SELECT p FROM Patient p WHERE p.patientFn = :firstName AND p.patientLn = :lastName AND p.patientGender = :gender " +
            "AND (p.patientDob = :dob OR p.patientAge = :age) AND p.patientMobileNumber = :mobileNumber AND p.patientRelation = :relation")
    Optional<Patient> findByUniqueCombination(String firstName, String lastName, MasGender gender,
                                              String dob, String age, String mobileNumber, MasRelation relation);
}
