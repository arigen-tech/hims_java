package com.hims.entity.repository;

import com.hims.entity.MasEmployeeCenterMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeSpecialtyCenterRepository extends JpaRepository<MasEmployeeCenterMapping, Long> {
}
