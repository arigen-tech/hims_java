package com.hims.entity.repository;

import com.hims.entity.*;
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
      AND s.manufacturerId.manufacturerId = :manufacturerId
""")
    Optional<StoreItemBatchStock> findMatchingStock(
            @Param("itemId") MasStoreItem itemId,
            @Param("batchNo") String batchNo,
            @Param("manufactureDate") LocalDate manufactureDate,
            @Param("expiryDate") LocalDate expiryDate,
            @Param("manufacturerId") Long manufacturerId
    );


    @Query("SELECT s FROM StoreItemBatchStock s WHERE CONCAT(s.itemId, '_', s.batchNo, '_', s.manufactureDate, '_', s.expiryDate, '_' , s.manufacturerId.manufacturerId) IN :keys")
    List<StoreItemBatchStock> findAllByKeys(@Param("keys") Set<String> keys);


    @Query("SELECT s FROM StoreItemBatchStock s WHERE s.expiryDate BETWEEN :fromDate AND :toDate")
    List<StoreItemBatchStock> findByExpiryDateRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query("SELECT s FROM StoreItemBatchStock s " +
            "WHERE s.itemId.itemId = :itemId " +
            "AND s.expiryDate BETWEEN :fromDate AND :toDate")
    List<StoreItemBatchStock> findByItemIdAndExpiryDateRange(
            @Param("itemId") Long itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);





  //  List<StoreItemBatchStock> findByItemIdItemId(Long itemId);

    List<StoreItemBatchStock> findByHospitalIdIdAndDepartmentIdId(long hospitalId,long departmentId);

    List<StoreItemBatchStock> findByItemIdItemIdAndHospitalIdIdAndDepartmentIdId(Long itemId, Long hospitalId, Long departmentId);

    List<StoreItemBatchStock> findByItemIdItemIdAndExpiryDateBetweenAndHospitalIdIdAndDepartmentIdId(Long itemId, LocalDate fromDate, LocalDate toDate, Long hospitalId, Long departmentId);

    List<StoreItemBatchStock> findByExpiryDateBetweenAndHospitalIdIdAndDepartmentIdId(LocalDate fromDate, LocalDate toDate, Long hospitalId, Long departmentId);

    List<StoreItemBatchStock> findByHospitalIdIdAndDepartmentIdIdAndExpiryDateGreaterThanEqual(Long hospitalId, Long departmentId, LocalDate now);

    List<StoreItemBatchStock> findByItemId(MasStoreItem itemId);


    @Query("SELECT s FROM StoreItemBatchStock s WHERE s.itemId.itemId = :itemId AND s.departmentId.id = :departmentId AND s.expiryDate > :expiryThreshold")
    List<StoreItemBatchStock> findByItemId_ItemIdAndDepartmentId_IdAndExpiryDateAfter(
            @Param("itemId") Long itemId,
            @Param("departmentId") Long departmentId,
            @Param("expiryThreshold") LocalDate expiryThreshold
    );


}
