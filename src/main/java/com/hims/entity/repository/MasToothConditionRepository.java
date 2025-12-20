package com.hims.entity.repository;

import com.hims.entity.MasToothCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasToothConditionRepository   extends JpaRepository<MasToothCondition, Long> {
    List<MasToothCondition> findByStatusIgnoreCaseOrderByConditionNameAsc(String y);

    List<MasToothCondition> findAllByOrderByStatusDescLastUpdateDateDesc();
}
