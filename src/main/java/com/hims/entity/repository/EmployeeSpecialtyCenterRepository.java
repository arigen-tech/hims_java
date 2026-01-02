package com.hims.entity.repository;

import com.hims.entity.EmployeeQualification;
import com.hims.entity.MasEmployee;
import com.hims.entity.MasEmployeeCenterMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeSpecialtyCenterRepository extends JpaRepository<MasEmployeeCenterMapping, Long> {
    List<MasEmployeeCenterMapping> findByEmpId(Long id);
    void deleteByEmpId(Long id);
}
