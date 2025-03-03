package com.hims.entity.repository;

import com.hims.entity.MasDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MasDepartmentRepository extends JpaRepository<MasDepartment, Long> {
   // MasDepartment findById(Long amountTypeId);
}
