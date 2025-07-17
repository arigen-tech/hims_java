package com.hims.entity.repository;

import com.hims.entity.StoreStockTakingM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreStockTakingMRepository extends JpaRepository<StoreStockTakingM,Long> {
}
