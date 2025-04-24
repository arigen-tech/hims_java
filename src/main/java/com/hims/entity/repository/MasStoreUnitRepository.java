package com.hims.entity.repository;

import com.hims.entity.MasStoreUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasStoreUnitRepository extends JpaRepository<MasStoreUnit, Long> {
    List<MasStoreUnit> findByStatusIgnoreCase(String status);
    List<MasStoreUnit> findByStatusInIgnoreCase(List<String> statuses);
}
