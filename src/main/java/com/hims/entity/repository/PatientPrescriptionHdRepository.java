package com.hims.entity.repository;

import com.hims.entity.PatientPrescriptionHd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientPrescriptionHdRepository extends JpaRepository<PatientPrescriptionHd, Long> {
//    PatientPrescriptionHd findByPatientIdAndPrescriptionDate(Long id, LocalDate prescriptionDate);
@Query("SELECT p FROM PatientPrescriptionHd p " +
        "WHERE p.patientId = :patientId " +
        "AND DATE(p.prescriptionDate) = :date")
PatientPrescriptionHd findByPatientIdAndDate(
        @Param("patientId") Long patientId,
        @Param("date") LocalDate date);

    Optional<PatientPrescriptionHd> findLatestByPatientId(Long patientId);
}
