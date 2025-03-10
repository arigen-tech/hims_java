package com.hims.entity.repository;

import com.hims.entity.EmployeeDocument;
import com.hims.entity.MasEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {
    List<EmployeeDocument> findByEmployee(MasEmployee employee);
}
