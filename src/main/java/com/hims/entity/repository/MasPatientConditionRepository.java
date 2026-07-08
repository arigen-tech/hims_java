package com.hims.entity.repository;

import com.hims.entity.MasPatientCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasPatientConditionRepository extends JpaRepository<MasPatientCondition,Long> {
    List<MasPatientCondition> findByStatusIgnoreCaseOrderByPatientConditionNameAsc(String lowerCase);

    List<MasPatientCondition> findAllByOrderByStatusDescLastUpdateDateDesc();
}
