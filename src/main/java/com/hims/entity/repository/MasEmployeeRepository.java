package com.hims.entity.repository;

import com.hims.entity.MasEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasEmployeeRepository extends JpaRepository<MasEmployee, Long> {
    List<MasEmployee> findByStatus(String status);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM MasEmployee e WHERE e.id = :employeeId AND e.idDocumentName IS NOT NULL AND e.profilePicName IS NOT NULL")
    boolean hasAllDocumentsUploaded(@Param("employeeId") Long employeeId);

}
