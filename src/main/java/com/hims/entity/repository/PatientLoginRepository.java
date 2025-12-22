package com.hims.entity.repository;

import com.hims.entity.PatientLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientLoginRepository extends JpaRepository<PatientLogin, Long> {

  //  boolean existsByPatientId(Long patientId);

    Optional<PatientLogin> findByPatientId(Long patientId);

   // PatientLogin findTopByMobileNoOrderByPatientLoginIdDesc(String mobileNo);

   // List<PatientLogin> findTopByMobileNoInOrderByPatientLoginIdDesc(String mobileNo);

    List<PatientLogin> findByMobileNoOrderByPatientLoginIdDesc(String mobileNo);
}