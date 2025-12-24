package com.hims.entity.repository;

import com.hims.entity.EmployeeAward;
import com.hims.entity.MasEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EmployeeAwardRepository extends JpaRepository<EmployeeAward, Long> {
    List<EmployeeAward> findByEmployee(MasEmployee employee);
}
