package com.hims.entity.repository;

import com.hims.entity.MasProcedureStatus;
import com.hims.entity.ProcedureHd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcedureHdRepository extends JpaRepository<ProcedureHd, Long> {
    List<MasProcedureStatus> findByStatus(String status);

    //List<MasProcedureStatus> findByStatusOrderByDisplayOrderAsc(String status);
}