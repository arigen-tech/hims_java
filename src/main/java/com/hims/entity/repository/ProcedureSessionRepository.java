package com.hims.entity.repository;

import com.hims.entity.ProcedureSession;
import com.hims.response.ProcedureSessionHistoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcedureSessionRepository extends JpaRepository<ProcedureSession, Long> {

    List<ProcedureSession> findByProcedureDt_ProcedureDtIdAndStatusOrderBySessionNoAsc(Long procedureDtId, String status);

    @Query("""
    SELECT new com.hims.response.ProcedureSessionHistoryResponse(
        ps.procedureSessionId,
        ps.sessionNo,
        ps.scheduledDateTime,
        ps.sessionStatus,
        ps.isFinalSession,
        ps.performedBy,
        ps.performedDateTime,
        ps.procedureFindings,
        ps.procedureNotes,
        ps.complicationFlag,
        ps.complicationDetails,
        ps.postProcedureAdvice
    )
    FROM ProcedureSession ps
    WHERE ps.procedureDt.procedureDtId = :procedureDtId
    ORDER BY ps.sessionNo ASC
""")
    List<ProcedureSessionHistoryResponse> findSessionHistoryByProcedureDtId(
            @Param("procedureDtId") Long procedureDtId
    );

}