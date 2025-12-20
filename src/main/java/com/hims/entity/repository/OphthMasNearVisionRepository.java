package com.hims.entity.repository;

import com.hims.entity.OphthMasNearVision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OphthMasNearVisionRepository extends JpaRepository<OphthMasNearVision, Long> {
    List<OphthMasNearVision> findByStatusIgnoreCaseOrderByNearValueAsc(String y);

    List<OphthMasNearVision> findAllByOrderByStatusDescLastUpdateDateDesc();
}
