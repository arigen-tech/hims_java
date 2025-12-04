package com.hims.entity.repository;

import com.hims.entity.MasProcedureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasProcedureTypeRepository extends JpaRepository<MasProcedureType, Long> {
}
