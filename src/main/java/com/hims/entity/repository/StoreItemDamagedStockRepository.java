// StoreItemDamagedStockRepository.java
package com.hims.entity.repository;

import com.hims.entity.StoreItemDamagedStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreItemDamagedStockRepository extends JpaRepository<StoreItemDamagedStock, Long> {
}
