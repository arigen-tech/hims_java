package com.hims.entity.repository;

import com.hims.entity.MasProcedure;
import com.hims.projection.MasProcedureProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasProcedureRepository extends JpaRepository<MasProcedure, Long> {
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
        AND (LOWER(p.procedureCode) LIKE :search 
             OR LOWER(p.procedureName) LIKE :search)
    """)
    Page<MasProcedure> searchProcedure(
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );

    // Search when flag != 1 → include both Y and N
    @Query("""
        SELECT p FROM MasProcedure p
        WHERE p.status IN :statusList
        AND (LOWER(p.procedureCode) LIKE :search
             OR LOWER(p.procedureName) LIKE :search)
    """)
    Page<MasProcedure> searchProcedureIn(
            @Param("statusList") List<String> statusList,
            @Param("search") String search,
            Pageable pageable
    );

    Page<MasProcedure> findByStatusIgnoreCase(String status, Pageable pageable);

    Page<MasProcedure> findByStatusInIgnoreCase(List<String> statusList, Pageable pageable);


    @Query("""
    SELECT 
        p.procedureId AS procedureId,
        p.procedureCode AS procedureCode,
        p.procedureName AS procedureName,
        p.status AS status,
        p.lastChgBy AS lastChgBy,
        p.lastChgDate AS lastChgDate,
        d.id AS departmentId,
        d.departmentName AS departmentName,
        p.opdAllowed AS opdAllowed,
        p.ipdAllowed AS ipdAllowed,
        p.isNursing AS isNursing,
        p.procedureLevel AS procedureLevel
    FROM MasProcedure p
    LEFT JOIN p.department d
    WHERE (:flag = 0 OR LOWER(p.status) = :status)
    ORDER BY  p.lastChgDate DESC
""")
    List<MasProcedureProjection> findAllMasProcedure(@Param("flag") int flag,
                                                     @Param("status") String status);


}
