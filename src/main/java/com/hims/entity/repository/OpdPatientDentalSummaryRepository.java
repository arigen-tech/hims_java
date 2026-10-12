package com.hims.entity.repository;

import com.hims.entity.OpdPatientDentalSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OpdPatientDentalSummaryRepository extends JpaRepository<OpdPatientDentalSummary, Long> {

    Optional<OpdPatientDentalSummary> findByPatientIdAndVisitId(Long patientId, Long visitId);
}