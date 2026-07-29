package com.hims.entity.repository;

import com.hims.entity.MasPatientDischargeCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasPatientDischargeConditionRepository extends JpaRepository<MasPatientDischargeCondition,Long> {
}
