package com.hims.entity.repository;

import com.hims.entity.EmployeeQualification;
import com.hims.entity.MasEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeQualificationRepository extends JpaRepository<EmployeeQualification, Long> {
    List<EmployeeQualification> findByEmployee(MasEmployee employee);
}
