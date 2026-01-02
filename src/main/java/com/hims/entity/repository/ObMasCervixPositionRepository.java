package com.hims.entity.repository;

import com.hims.entity.ObMasCervixPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObMasCervixPositionRepository
        extends JpaRepository<ObMasCervixPosition, Long> {

    List<ObMasCervixPosition>
    findByStatusIgnoreCaseOrderByCervixPositionAsc(String status);

    List<ObMasCervixPosition>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
