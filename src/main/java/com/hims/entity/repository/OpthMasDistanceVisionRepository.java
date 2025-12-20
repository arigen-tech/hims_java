package com.hims.entity.repository;

import com.hims.entity.OpthMasDistanceVision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpthMasDistanceVisionRepository  extends JpaRepository<OpthMasDistanceVision, Long> {
    List<OpthMasDistanceVision> findByStatusIgnoreCaseOrderByVisionValueAsc(String y);

    List<OpthMasDistanceVision> findAllByOrderByStatusDescLastUpdateDateDesc();
}
