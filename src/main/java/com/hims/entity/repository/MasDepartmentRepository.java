package com.hims.entity.repository;

import com.hims.entity.MasDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasDepartmentRepository extends JpaRepository<MasDepartment, Long> {
   // MasDepartment findById(Long amountTypeId);
   List<MasDepartment> findByStatusIgnoreCase(String status);
   List<MasDepartment> findByStatusInIgnoreCase(List<String> statuses);

}
