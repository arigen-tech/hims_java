package com.hims.entity.repository;

import com.hims.entity.MasProcedureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasProcedureTypeRepository extends JpaRepository<MasProcedureType, Long> {
    interface ProcedureTypeView {
        Long getProcedureTypeId();
        String getProcedureTypeName();
        String getDescription();
        String getStatus();
        java.time.LocalDateTime getLastUpdateDate();
        String getCreatedBy();
        String getLastUpdatedBy();
        String getProcedureTypeCode();
        Long getDepartmentId();
        String getDepartmentName();
    }

    @Query(value = "SELECT p.procedure_type_id AS procedureTypeId, p.procedure_type_name AS procedureTypeName, " +
            "p.description AS description, p.status AS status, p.last_update_date AS lastUpdateDate, " +
            "p.created_by AS createdBy, p.last_updated_by AS lastUpdatedBy, p.procedure_type_code AS procedureTypeCode, " +
            "d.department_id AS departmentId, d.department_name AS departmentName " +
            "FROM mas_procedure_type p LEFT JOIN mas_department d ON d.department_id = p.department_id " +
            "WHERE (:activeOnly = false OR LOWER(p.status) = 'y') " +
            "ORDER BY CASE WHEN :activeOnly = true THEN p.procedure_type_name END ASC, " +
            "CASE WHEN :activeOnly = false THEN p.status END DESC, " +
            "CASE WHEN :activeOnly = false THEN p.last_update_date END DESC", nativeQuery = true)
    List<ProcedureTypeView> findAllWithDepartment(@Param("activeOnly") boolean activeOnly);

    @Query(value = "SELECT p.procedure_type_id AS procedureTypeId, p.procedure_type_name AS procedureTypeName, " +
            "p.description AS description, p.status AS status, p.last_update_date AS lastUpdateDate, " +
            "p.created_by AS createdBy, p.last_updated_by AS lastUpdatedBy, p.procedure_type_code AS procedureTypeCode, " +
            "d.department_id AS departmentId, d.department_name AS departmentName " +
            "FROM mas_procedure_type p LEFT JOIN mas_department d ON d.department_id = p.department_id " +
            "WHERE p.procedure_type_id = :id", nativeQuery = true)
    Optional<ProcedureTypeView> findViewById(@Param("id") Long id);

   // List<MasProcedureType> findByStatusIgnoreCase(String y);

    List<MasProcedureType> findByStatusIgnoreCaseOrderByProcedureTypeNameAsc(String y);

    List<MasProcedureType> findAllByOrderByLastUpdateDateDesc();

    List<MasProcedureType> findAllByOrderByStatusDescLastUpdateDateDesc();

    Optional<MasProcedureType>findByProcedureTypeCode(String code);
}
