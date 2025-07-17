package com.hims.entity.repository;

import com.hims.entity.StoreStockTakingM;
import com.hims.entity.StoreStockTakingT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreStockTakingTRepository extends JpaRepository<StoreStockTakingT,Long> {
    List<StoreStockTakingT> findByTakingMId(StoreStockTakingM master);
}
