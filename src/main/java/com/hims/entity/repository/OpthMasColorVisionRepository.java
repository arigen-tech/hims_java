package com.hims.entity.repository;

import com.hims.entity.OpthMasColorVision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpthMasColorVisionRepository extends JpaRepository<OpthMasColorVision, Long> {
    List<OpthMasColorVision> findByStatusIgnoreCaseOrderByColorValueAsc(String y);

    List<OpthMasColorVision> findAllByOrderByStatusDescLastUpdateDateDesc();
}
