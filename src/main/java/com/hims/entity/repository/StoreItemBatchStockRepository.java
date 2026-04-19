package com.hims.entity.repository;

import com.hims.entity.*;
import com.hims.response.BatchNameForStockResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    List<StoreItemBatchStock> findByItemIdAndHospitalId_IdAndDepartmentId_Id(MasStoreItem itemId,Long hospitalId, Long departmentId);


    @Query("""
       SELECT s
       FROM StoreItemBatchStock s
       WHERE s.itemId.itemId = :itemId
         AND s.departmentId.id = :departmentId
         AND (
                s.expiryDate IS NULL
                OR s.expiryDate >= :today
             )
       """)
    List<StoreItemBatchStock> findNonExpiredBatchesForROL(@Param("itemId") Long itemId,
                                                          @Param("departmentId") Long departmentId,
                                                          @Param("today") LocalDate today);


    List<StoreItemBatchStock> findByItemId_ItemId(Long itemId);


    @Query("SELECT s FROM StoreItemBatchStock s WHERE s.itemId IN :items")
    List<StoreItemBatchStock> findByItemIds(@Param("items") List<MasStoreItem> items);

    @Query("SELECT s FROM StoreItemBatchStock s WHERE s.itemId.itemId IN :itemIds")
    List<StoreItemBatchStock> findByItemId(@Param("itemIds") List<Long> itemIds);


        // Find all batch stocks for a specific department and item
        List<StoreItemBatchStock> findByDepartmentIdAndItemId(
                MasDepartment departmentId,
                MasStoreItem itemId
        );



    List<StoreItemBatchStock> findByItemIdItemId(Long itemId);

    @Query("""
SELECT COALESCE(SUM(s.closingStock),0)
FROM StoreItemBatchStock s
WHERE s.itemId.itemId = :itemId
  AND s.hospitalId.id = :hospitalId
  AND s.departmentId.id = :departmentId
  AND s.closingStock > 0
  AND (s.expiryDate IS NULL OR s.expiryDate >= :threshold)
""")
    Long getAvailableStock(
            @Param("itemId") Long itemId,
            @Param("hospitalId") Long hospitalId,
            @Param("departmentId") Long departmentId,
            @Param("threshold") LocalDate threshold
    );

    @Query("""
    SELECT s FROM StoreItemBatchStock s
    WHERE s.itemId = :itemId
        AND s.departmentId.id = :departmentId
        AND s.hospitalId.id = :hospitalId
        AND UPPER(TRIM(s.batchNo)) = UPPER(TRIM(:batchNo))
        AND s.manufacturerId.manufacturerId = :manufacturerId
        AND s.expiryDate = :expiryDate
      
""")
    Optional<StoreItemBatchStock> findExistingBatchStockForDrug(
            @Param("itemId") MasStoreItem itemId,
            @Param("departmentId") Long departmentId,
            @Param("hospitalId") Long hospitalId,
            @Param("batchNo") String batchNo,
            @Param("manufacturerId") Long manufacturerId,
            @Param("expiryDate") LocalDate expiryDate

    );

    @Query("""
    SELECT s FROM StoreItemBatchStock s
    WHERE s.itemId = :itemId
        AND s.departmentId.id = :departmentId
        AND s.hospitalId.id = :hospitalId
        AND UPPER(TRIM(s.batchNo)) = UPPER(TRIM(:batchNo))
        AND s.manufacturerId.manufacturerId = :manufacturerId
""")
    Optional<StoreItemBatchStock> findExistingBatchStockForNonDrug(
            @Param("itemId") MasStoreItem itemId,
            @Param("departmentId") Long departmentId,
            @Param("hospitalId") Long hospitalId,
            @Param("batchNo") String batchNo,
            @Param("manufacturerId") Long manufacturerId

    );

    @Query("""
SELECT new com.hims.response.BatchNameForStockResponse(
    s.batchNo,
    s.manufactureDate,
    s.expiryDate,
    s.closingStock,
    (
        SELECT COALESCE(SUM(s2.closingStock), 0)
        FROM StoreItemBatchStock s2
        WHERE s2.itemId.itemId = :itemId
          AND s2.hospitalId.id = :hospitalId
          AND s2.departmentId.id = :departmentId
          AND COALESCE(:expDate, s2.expiryDate) <= s2.expiryDate
    ),
    s.manufacturerId.manufacturerId
)
FROM StoreItemBatchStock s
WHERE s.itemId.itemId = :itemId
  AND s.hospitalId.id = :hospitalId
  AND s.departmentId.id = :departmentId
  AND COALESCE(:expDate, s.expiryDate) <= s.expiryDate
""")
    List<BatchNameForStockResponse> findBatchNameForStockWithOptionalExpiry(
            @Param("itemId") Long itemId,
            @Param("hospitalId") Long hospitalId,
            @Param("departmentId") Long departmentId,
            @Param("expDate") LocalDate expDate
    );
}
