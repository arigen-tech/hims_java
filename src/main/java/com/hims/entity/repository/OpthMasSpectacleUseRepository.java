package com.hims.entity.repository;

import com.hims.entity.OpthMasSpectacleUse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpthMasSpectacleUseRepository  extends JpaRepository<OpthMasSpectacleUse, Long> {
    List<OpthMasSpectacleUse> findByStatusIgnoreCaseOrderByUseNameAsc(String y);

    List<OpthMasSpectacleUse> findAllByOrderByStatusDescLastUpdateDateDesc();
}
