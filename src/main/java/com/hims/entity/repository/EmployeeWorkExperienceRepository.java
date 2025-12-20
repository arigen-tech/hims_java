package com.hims.entity.repository;

import com.hims.entity.EmployeeWorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeWorkExperienceRepository extends JpaRepository<EmployeeWorkExperience, Long> {
}
