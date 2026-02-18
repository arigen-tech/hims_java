package com.hims.entity.repository;

import com.hims.entity.MasBloodInventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasBloodInventoryStatusRepository
        extends JpaRepository<MasBloodInventoryStatus, Long> {

    List<MasBloodInventoryStatus>
    findByStatusIgnoreCaseOrderByStatusCodeAsc(String status);

    List<MasBloodInventoryStatus>
    findAllByOrderByStatusDescLastUpdateDateDesc();

    boolean existsByStatusCodeIgnoreCase(String statusCode);
}
