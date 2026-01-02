package com.hims.entity.repository;

import com.hims.entity.EmployeeWorkExperience;
import com.hims.entity.MasEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EmployeeWorkExperienceRepository extends JpaRepository<EmployeeWorkExperience, Long> {

    List<EmployeeWorkExperience> findByEmployee(MasEmployee employee);
}
