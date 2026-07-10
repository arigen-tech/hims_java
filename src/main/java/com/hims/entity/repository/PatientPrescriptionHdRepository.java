package com.hims.entity.repository;

import com.hims.entity.PatientPrescriptionHd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PatientPrescriptionHdRepository extends JpaRepository<PatientPrescriptionHd, Long> {
    PatientPrescriptionHd findByPatientId(Long id);
    @Query("""
       SELECT p
       FROM PatientPrescriptionHd p
       WHERE p.visit.id = :visitId
      
       """)
    PatientPrescriptionHd findByPatientIdAndVisitId(
            @Param("visitId") Long visitId);

    PatientPrescriptionHd findByVisit_Id(Long visitId);

    Optional<PatientPrescriptionHd> findLatestByPatientId(Long patientId);
}
