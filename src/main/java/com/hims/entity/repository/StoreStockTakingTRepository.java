package com.hims.entity.repository;

import com.hims.entity.StoreStockTakingT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreStockTakingTRepository extends JpaRepository<StoreStockTakingT,Long> {
}
