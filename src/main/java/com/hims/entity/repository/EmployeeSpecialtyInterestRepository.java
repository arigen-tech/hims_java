package com.hims.entity.repository;

import com.hims.entity.EmployeeSpecialtyInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeSpecialtyInterestRepository extends JpaRepository<EmployeeSpecialtyInterest, Long> {
}