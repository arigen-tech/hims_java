package com.hims.entity.repository;

import com.hims.entity.OpdPatientPregnancyDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OpdPatientPregnancyDetailsRepository extends JpaRepository<OpdPatientPregnancyDetails, Long> {
    Optional<OpdPatientPregnancyDetails> findByVisit_Id(Long visitId);
}
