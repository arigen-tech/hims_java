package com.hims.entity.repository;

import com.hims.entity.ProcedureDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcedureDetailsRepository extends JpaRepository<ProcedureDetails, Integer> {
}
