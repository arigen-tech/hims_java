package com.hims.entity.repository;

import com.hims.entity.IpNursingMedicalAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IpNursingMedicalAssessmentRepository extends JpaRepository<IpNursingMedicalAssessment,Long> {
    Optional<IpNursingMedicalAssessment> findByInpatient_InpatientId(Long inpatientId);
}
