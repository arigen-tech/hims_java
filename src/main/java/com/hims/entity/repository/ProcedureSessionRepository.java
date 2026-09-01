package com.hims.entity.repository;

import com.hims.entity.ProcedureSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcedureSessionRepository extends JpaRepository<ProcedureSession, Long> {

    List<ProcedureSession> findByProcedureDtProcedureDtId(Long procedureDtId);

}