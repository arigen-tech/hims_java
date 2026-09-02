package com.hims.entity.repository;

import com.hims.entity.DentalProcedureTooth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DentalProcedureToothRepository extends JpaRepository<DentalProcedureTooth, Long> {

    List<DentalProcedureTooth> findByProcedureDt_ProcedureDtIdAndStatus(Long procedureDtId, String status);
}