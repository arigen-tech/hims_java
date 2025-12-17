package com.hims.entity.repository;

import com.hims.entity.ProcedureHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcedureHeaderRepository extends JpaRepository<ProcedureHeader, Long> {
    Optional<ProcedureHeader> findByVisitId(Long visitId);

}
