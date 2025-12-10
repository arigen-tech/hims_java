package com.hims.entity.repository;

import com.hims.entity.MasProcedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasProcedureRepository extends JpaRepository<MasProcedure, Integer> {
    List<MasProcedure> findByStatusIgnoreCaseOrderByLastChangedDateDesc(String y);

    List<MasProcedure> findByStatusInOrderByLastChangedDateDesc(List<String> statuses);

    // Optional query methods:
    // List<MasProcedure> findByDepartment_DepartmentId(Long departmentId);
    // List<MasProcedure> findByProcedureType_ProcedureTypeId(Long procedureTypeId);
}
