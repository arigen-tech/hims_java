package com.hims.entity.repository;

import com.hims.entity.PatientPrescriptionHd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientPrescriptionHdRepository extends JpaRepository<PatientPrescriptionHd, Long> {
    PatientPrescriptionHd findByPatientId(Long id);

    Optional<PatientPrescriptionHd> findLatestByPatientId(Long patientId);
}
