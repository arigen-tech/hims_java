package com.hims.entity.repository;

import com.hims.entity.GynMasFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GynMasFlowRepository
        extends JpaRepository<GynMasFlow, Long> {

    List<GynMasFlow>
    findByStatusIgnoreCaseOrderByFlowValueAsc(String status);

    List<GynMasFlow>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
