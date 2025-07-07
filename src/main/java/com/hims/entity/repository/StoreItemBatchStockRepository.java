package com.hims.entity.repository;

import com.hims.entity.MasManufacturer;
import com.hims.entity.MasStoreItem;
import com.hims.entity.StoreItemBatchStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StoreItemBatchStockRepository extends JpaRepository<StoreItemBatchStock,Long> {

    Optional<StoreItemBatchStock> findByItemIdAndBatchNoAndManufacturerIdAndExpiryDate(MasStoreItem itemId, String batchNo, LocalDate manufacturerDate, LocalDate expiryDate);
}
