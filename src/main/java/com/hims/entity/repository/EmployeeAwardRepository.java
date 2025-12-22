package com.hims.entity.repository;

import com.hims.entity.EmployeeAward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeAwardRepository extends JpaRepository<EmployeeAward, Long> {
}
