package com.hims.entity.repository;

import com.hims.entity.DentalProcedureTooth;
import com.hims.response.DentalProcedureHistoryRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DentalProcedureToothRepository extends JpaRepository<DentalProcedureTooth, Long> {

    List<DentalProcedureTooth> findByProcedureDt_ProcedureDtIdAndStatus(Long procedureDtId, String status);

    @Query("""
    SELECT new com.hims.response.DentalProcedureHistoryRow(
           dt.procedureDtId,
           hd.procedureHdId,
           p.procedureName,
           CAST(tm.toothNumber AS string),
           dt.status,
           (SELECT MIN(s.scheduledDateTime)
              FROM ProcedureSession s
             WHERE s.procedureDt = dt)
    )
      FROM DentalProcedureTooth dpt
      JOIN dpt.procedureDt dt
      JOIN dt.procedureHd hd
      JOIN dt.procedure p
      JOIN dpt.tooth tm
     WHERE hd.patientId = :patientId
     ORDER BY dt.procedureDtId, tm.toothNumber
""")
    List<DentalProcedureHistoryRow> findDentalProcedureHistoryRows(@Param("patientId") Long patientId);

    @Query(value = """
    SELECT dt.procedureDtId
      FROM DentalProcedureTooth dpt
      JOIN dpt.procedureDt dt
      JOIN dt.procedureHd hd
     WHERE hd.patientId = :patientId
     GROUP BY dt.procedureDtId
     ORDER BY dt.procedureDtId DESC
""",
            countQuery = """
    SELECT COUNT(DISTINCT dt.procedureDtId)
      FROM DentalProcedureTooth dpt
      JOIN dpt.procedureDt dt
      JOIN dt.procedureHd hd
     WHERE hd.patientId = :patientId
""")
    Page<Long> findProcedureDtIdsByPatient(@Param("patientId") Long patientId, Pageable pageable);

    @Query("""
    SELECT new com.hims.response.DentalProcedureHistoryRow(
           dt.procedureDtId,
           hd.procedureHdId,
           p.procedureName,
           dt.createdBy,
           dt.createdDate,
           dt.plannedSessionCount,
           dt.completedSessionCount,
           CAST(tm.toothNumber AS string),
           dt.status,
           (SELECT MIN(s.scheduledDateTime)
              FROM ProcedureSession s
             WHERE s.procedureDt = dt)
    )
      FROM DentalProcedureTooth dpt
      JOIN dpt.procedureDt dt
      JOIN dt.procedureHd hd
      JOIN dt.procedure p
      JOIN dpt.tooth tm
     WHERE dt.procedureDtId IN :dtIds
     ORDER BY dt.procedureDtId DESC, tm.toothNumber
""")
    List<DentalProcedureHistoryRow> findDentalProcedureHistoryRowsByDtIds(@Param("dtIds") List<Long> dtIds);
}