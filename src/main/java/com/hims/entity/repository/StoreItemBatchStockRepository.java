package com.hims.entity.repository;

import com.hims.entity.MasBrand;
import com.hims.entity.MasManufacturer;
import com.hims.entity.MasStoreItem;
import com.hims.entity.StoreItemBatchStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface StoreItemBatchStockRepository extends JpaRepository<StoreItemBatchStock,Long> {
    @Query("""
    SELECT s FROM StoreItemBatchStock s
    WHERE s.itemId = :itemId
      AND UPPER(TRIM(s.batchNo)) = UPPER(TRIM(:batchNo))
      AND s.manufactureDate = :manufactureDate
      AND s.expiryDate = :expiryDate
      AND s.brandId.brandId = :brandId
      AND s.manufacturerId.manufacturerId = :manufacturerId
""")
    Optional<StoreItemBatchStock> findMatchingStock(
            @Param("itemId") MasStoreItem itemId,
            @Param("batchNo") String batchNo,
            @Param("manufactureDate") LocalDate manufactureDate,
            @Param("expiryDate") LocalDate expiryDate,
            @Param("brandId") Long brandId,
            @Param("manufacturerId") Long manufacturerId
    );


    @Query("SELECT s FROM StoreItemBatchStock s WHERE CONCAT(s.itemId, '_', s.batchNo, '_', s.manufactureDate, '_', s.expiryDate, '_', s.brandId.brandId, '_', s.manufacturerId.manufacturerId) IN :keys")
    List<StoreItemBatchStock> findAllByKeys(@Param("keys") Set<String> keys);
}
