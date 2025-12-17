package com.hims.entity.repository;

import com.hims.entity.ProcedureDetails;
import com.hims.entity.ProcedureHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcedureDetailsRepository extends JpaRepository<ProcedureDetails, Long> {

    void deleteByProcedureHeader(ProcedureHeader header);

}
