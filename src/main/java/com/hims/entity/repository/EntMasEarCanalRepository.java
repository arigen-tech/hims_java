package com.hims.entity.repository;

import com.hims.entity.EntMasEarCanal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntMasEarCanalRepository
        extends JpaRepository<EntMasEarCanal, Long> {

    List<EntMasEarCanal>
    findByStatusIgnoreCaseOrderByEarCanalConditionAsc(String status);

    List<EntMasEarCanal>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
