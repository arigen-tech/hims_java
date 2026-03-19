package com.hims.entity.repository;

import com.hims.entity.MasCrossMatchType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasCrossMatchTypeRepository extends JpaRepository<MasCrossMatchType,Long> {


    List<MasCrossMatchType> findAllByOrderByStatusDescCreatedDateDesc();

    List<MasCrossMatchType> findByStatusIgnoreCaseOrderByCrossMatchNameAsc(String y);
}
