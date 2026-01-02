package com.hims.entity.repository;

import com.hims.entity.ObMasCervixConsistency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObMasCervixConsistencyRepository
        extends JpaRepository<ObMasCervixConsistency, Long> {

    List<ObMasCervixConsistency>
    findByStatusIgnoreCaseOrderByCervixConsistencyAsc(String status);

    List<ObMasCervixConsistency>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
