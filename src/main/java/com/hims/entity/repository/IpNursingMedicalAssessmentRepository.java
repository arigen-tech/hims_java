package com.hims.entity.repository;

import com.hims.entity.IpNursingMedicalAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpNursingMedicalAssessmentRepository extends JpaRepository<IpNursingMedicalAssessment,Long> {
}
