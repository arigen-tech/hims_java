package com.hims.entity.repository;

import com.hims.entity.EmployeeDocument;
import com.hims.entity.MasEmployee;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM EmployeeDocument ed WHERE ed.employee = :employee")
    void deleteByEmployee(@Param("employee") MasEmployee employee);

    List<EmployeeDocument> findByEmployee(MasEmployee employee);
}
