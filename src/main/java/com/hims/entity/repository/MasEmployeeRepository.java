package com.hims.entity.repository;

import com.hims.entity.MasEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasEmployeeRepository extends JpaRepository<MasEmployee, Long> {
}
