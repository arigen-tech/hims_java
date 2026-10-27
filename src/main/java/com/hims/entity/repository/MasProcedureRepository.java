package com.hims.entity.repository;

import com.hims.entity.MasProcedure;
import com.hims.projection.MasProcedureProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasProcedureRepository extends JpaRepository<MasProcedure, Long> {
    @Query(value = "SELECT p.procedure_id AS procedureId, p.procedure_code AS procedureCode, " +
            "p.procedure_name AS procedureName, p.status AS status, p.last_chg_by AS lastChgBy, " +
            "p.last_chg_date AS lastChgDate, p.opd_allowed AS opdAllowed, p.ipd_allowed AS ipdAllowed, " +
            "p.is_nursing AS isNursing, p.procedure_type_id AS procedureTypeId, " +
            "pt.procedure_type_name AS procedureTypeName " +
            "FROM mas_procedure p LEFT JOIN mas_procedure_type pt ON pt.procedure_type_id = p.procedure_type_id " +
            "WHERE (:flag = 0 OR LOWER(p.status) = :status) ORDER BY p.last_chg_date DESC", nativeQuery = true)
    List<MasProcedureProjection> findAllMasProcedure(@Param("flag") int flag, @Param("status") String status);

    @Query(value = "SELECT p.procedure_id AS procedureId, p.procedure_code AS procedureCode, " +
            "p.procedure_name AS procedureName, p.status AS status, p.last_chg_by AS lastChgBy, " +
            "p.last_chg_date AS lastChgDate, p.opd_allowed AS opdAllowed, p.ipd_allowed AS ipdAllowed, " +
            "p.is_nursing AS isNursing, p.procedure_type_id AS procedureTypeId, " +
            "pt.procedure_type_name AS procedureTypeName " +
            "FROM mas_procedure p LEFT JOIN mas_procedure_type pt ON pt.procedure_type_id = p.procedure_type_id " +
            "WHERE p.procedure_id = :id", nativeQuery = true)
    java.util.Optional<MasProcedureProjection> findProcedureViewById(@Param("id") Long id);

    @Query(value = "SELECT p.procedure_id AS procedureId, p.procedure_code AS procedureCode, " +
            "p.procedure_name AS procedureName, p.status AS status, p.last_chg_by AS lastChgBy, " +
            "p.last_chg_date AS lastChgDate, p.opd_allowed AS opdAllowed, p.ipd_allowed AS ipdAllowed, " +
            "p.is_nursing AS isNursing, p.procedure_type_id AS procedureTypeId, " +
            "pt.procedure_type_name AS procedureTypeName " +
            "FROM mas_procedure p LEFT JOIN mas_procedure_type pt ON pt.procedure_type_id = p.procedure_type_id " +
            "WHERE (LOWER(p.status) IN (:statuses)) " +
            "AND (CAST(:nursingStatus AS text) IS NULL OR LOWER(p.is_nursing) = LOWER(:nursingStatus)) " +
            "AND (CAST(:opdStatus AS text) IS NULL OR LOWER(p.opd_allowed) = LOWER(:opdStatus)) " +
            "AND (CAST(:ipdStatus AS text) IS NULL OR LOWER(p.ipd_allowed) = LOWER(:ipdStatus)) " +
            "AND (CAST(:search AS text) IS NULL OR LOWER(p.procedure_code) LIKE :search OR LOWER(p.procedure_name) LIKE :search) " +
            "ORDER BY p.last_chg_date DESC",
            countQuery = "SELECT COUNT(*) FROM mas_procedure p WHERE (LOWER(p.status) IN (:statuses)) " +
                    "AND (CAST(:nursingStatus AS text) IS NULL OR LOWER(p.is_nursing) = LOWER(:nursingStatus)) " +
                    "AND (CAST(:opdStatus AS text) IS NULL OR LOWER(p.opd_allowed) = LOWER(:opdStatus)) " +
                    "AND (CAST(:ipdStatus AS text) IS NULL OR LOWER(p.ipd_allowed) = LOWER(:ipdStatus)) " +
                    "AND (CAST(:search AS text) IS NULL OR LOWER(p.procedure_code) LIKE :search OR LOWER(p.procedure_name) LIKE :search) " +
                    "", nativeQuery = true)
    Page<MasProcedureProjection> findProcedureViewsWithFilters(
            @Param("statuses") List<String> statuses,
            @Param("nursingStatus") String nursingStatus,
            @Param("opdStatus") String opdStatus,
            @Param("ipdStatus") String ipdStatus,
            @Param("search") String search,
            Pageable pageable);
   // List<MasProcedure> findByStatusIgnoreCaseOrderByLastChangedDateDesc(String y);

//    List<MasProcedure> findByStatusIgnoreCaseOrderByLastChangedDateDesc(String y);
//
//    List<MasProcedure> findByStatusInOrderByLastChangedDateDesc(List<String> statuses);
//
//    List<MasProcedure> findByStatusIgnoreCaseOrderByProcedureNameAsc(String y);
//
//    List<MasProcedure> findAllByOrderByLastChangedDateDesc();

    // Optional query methods:
    // List<MasProcedure> findByDepartment_DepartmentId(Long departmentId);
    // List<MasProcedure> findByProcedureType_ProcedureTypeId(Long procedureTypeId);


    @Query("""
    SELECT p FROM MasProcedure p
    WHERE p.status = :status

      AND (:nursingStatus IS NULL
           OR p.isNursing = :nursingStatus)

      AND (:opdStatus IS NULL
           OR p.opdAllowed = :opdStatus)

      AND (:ipdStatus IS NULL
           OR p.ipdAllowed = :ipdStatus)

      AND (
          LOWER(p.procedureCode) LIKE :search
          OR LOWER(p.procedureName) LIKE :search
      )
""")
    Page<MasProcedure> searchProcedure(
            @Param("status") String status,
            @Param("nursingStatus") String nursingStatus,
            @Param("opdStatus") String opdStatus,
            @Param("ipdStatus") String ipdStatus,
            @Param("search") String search,
            Pageable pageable
    );

    // Search when flag != 1 → include both Y and N
    @Query("""
    SELECT p FROM MasProcedure p
    WHERE p.status IN :statuses

      AND (:nursingStatus IS NULL
           OR p.isNursing = :nursingStatus)

      AND (:opdStatus IS NULL
           OR p.opdAllowed = :opdStatus)

      AND (:ipdStatus IS NULL
           OR p.ipdAllowed = :ipdStatus)

      AND (
          LOWER(p.procedureCode) LIKE :search
          OR LOWER(p.procedureName) LIKE :search
      )
""")
    Page<MasProcedure> searchProcedureIn(
            @Param("statuses") List<String> statuses,
            @Param("nursingStatus") String nursingStatus,
            @Param("opdStatus") String opdStatus,
            @Param("ipdStatus") String ipdStatus,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
    SELECT p FROM MasProcedure p
    WHERE p.status = :status

      AND (:nursingStatus IS NULL
           OR p.isNursing = :nursingStatus)

      AND (:opdStatus IS NULL
           OR p.opdAllowed = :opdStatus)

      AND (:ipdStatus IS NULL
           OR p.ipdAllowed = :ipdStatus)

      AND (:typeId IS NULL
           OR p.procedureType.procedureTypeId = :typeId)
""")
    Page<MasProcedure> findByStatusWithFilters(
            @Param("status") String status,
            @Param("nursingStatus") String nursingStatus,
            @Param("opdStatus") String opdStatus,
            @Param("ipdStatus") String ipdStatus,
            @Param("typeId") Long typeId,
            Pageable pageable
    );

    @Query("""
    SELECT p FROM MasProcedure p
    WHERE LOWER(p.status) IN :statuses

      AND (:nursingStatus IS NULL OR p.isNursing = :nursingStatus)
      AND (:opdStatus IS NULL OR p.opdAllowed = :opdStatus)
      AND (:ipdStatus IS NULL OR p.ipdAllowed = :ipdStatus)
""")
    Page<MasProcedure> findByStatusInWithFilters(
            @Param("statuses") List<String> statuses,
            @Param("nursingStatus") String nursingStatus,
            @Param("opdStatus") String opdStatus,
            @Param("ipdStatus") String ipdStatus,
            Pageable pageable
    );

    Page<MasProcedure> findByStatusIgnoreCase(String status, Pageable pageable);

    Page<MasProcedure> findByStatusInIgnoreCase(List<String> statusList, Pageable pageable);



    @Query("""
    SELECT p FROM MasProcedure p
     WHERE LOWER(p.status) IN :status
    AND (:search IS NULL OR 
         LOWER(p.procedureName) LIKE LOWER(CONCAT('%', :search, '%')))
""")
    Page<MasProcedure> searchMasProcedure( @Param("status") String status,
            @Param("search") String search, Pageable pageable);


}
