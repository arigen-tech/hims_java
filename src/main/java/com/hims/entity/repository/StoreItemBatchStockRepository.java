package com.hims.entity.repository;

import com.hims.entity.*;
import com.hims.projection.BatchNameForStockProjection;
import com.hims.response.BatchNameForStockResponse;
import com.hims.response.OpeningBalanceStockResponse;
import com.hims.response.OpeningBalanceStockResponseDto;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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
    s.stockId,
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
  AND (
        :minimumClosingStock IS NULL
         OR s.closingStock > :minimumClosingStock
      )
  AND COALESCE(:expDate, s.expiryDate) <= s.expiryDate
  ORDER BY s.expiryDate ASC
""")
    List<BatchNameForStockResponse> findBatchNameForStockWithOptionalExpiry(
            @Param("itemId") Long itemId,
            @Param("hospitalId") Long hospitalId,
            @Param("departmentId") Long departmentId,
            @Param("expDate") LocalDate expDate,
            @Param("minimumClosingStock") Long minimumCLosingStock
    );


    @Query(value = """
    SELECT
        s.stock_id AS stockId,
        s.batch_no AS batchName,
        s.manufacture_date AS dom,
        s.expiry_date AS doe,
        s.closing_stock AS batchStock,
        (
            SELECT COALESCE(SUM(s2.closing_stock), 0)
            FROM store_item_batch_stock s2
            WHERE s2.item_id = :itemId
              AND s2.hospital_id = :hospitalId
              AND s2.department_id = :departmentId
              AND COALESCE(:expDate, s2.expiry_date) <= s2.expiry_date
        ) AS availableStock,
        s.manufacturer_id AS manufacturerId
    FROM store_item_batch_stock s
    WHERE s.item_id = :itemId
      AND s.hospital_id = :hospitalId
      AND s.department_id = :departmentId
      AND s.closing_stock > :minimumClosingStock
    ORDER BY s.expiry_date ASC
    LIMIT 1
    """, nativeQuery = true)
    Optional<BatchNameForStockProjection> getNearlyExpiredBatchStockWrtItem(
            @Param("itemId") Long itemId,
            @Param("hospitalId") Long hospitalId,
            @Param("departmentId") Long departmentId,
            @Param("expDate") LocalDate expDate,
            @Param("minimumClosingStock") Long minimumClosingStock
    );

    @Query(value = """
    SELECT
        s.stock_id AS stockId,
        s.batch_no AS batchName,
        s.manufacture_date AS dom,
        s.expiry_date AS doe,
        s.closing_stock AS batchStock,
        (
            SELECT COALESCE(SUM(s2.closing_stock), 0)
            FROM store_item_batch_stock s2
            WHERE s2.item_id = :itemId
              AND s2.hospital_id = :hospitalId
              AND s2.department_id = :departmentId
              AND COALESCE(:expDate, s2.expiry_date) <= s2.expiry_date
        ) AS availableStock,
        s.manufacturer_id AS manufacturerId
    FROM store_item_batch_stock s
    WHERE s.item_id = :itemId
      AND s.hospital_id = :hospitalId
      AND s.department_id = :departmentId
      AND s.closing_stock > :minimumClosingStock
      AND s.stock_id NOT IN (:excludeStockIds)
    ORDER BY s.expiry_date ASC
    LIMIT 1
    """, nativeQuery = true)
    Optional<BatchNameForStockProjection> getAllStockBatchesWrtItemExceptGivenStock(
            @Param("itemId") Long itemId,
            @Param("hospitalId") Long hospitalId,
            @Param("departmentId") Long departmentId,
            @Param("expDate") LocalDate expDate,
            @Param("minimumClosingStock") Long minimumClosingStock,
            @Param("excludeStockIds") List<Long> excludeStockIds
    );

    @Query("""
SELECT new com.hims.response.OpeningBalanceStockResponseDto(
    s.stockId,
    i.itemId,
    i.nomenclature,
    i.pvmsNo,
    s.openingBalanceQty,
    u.unitName,
    s.batchNo,
    s.manufactureDate,
    s.expiryDate,
    m.manufacturerName,
    sec.sectionName,
    sec.sectionId,
    cls.itemClassId,
    cls.itemClassName,
    b.brandName,
    s.mrpPerUnit,
    s.closingStock
)
FROM StoreItemBatchStock s
JOIN s.itemId i
JOIN i.unitAU u
JOIN i.itemClassId cls
JOIN cls.masStoreSection sec
LEFT JOIN s.manufacturerId m
LEFT JOIN s.brandId b

WHERE s.hospitalId.id = :hospitalId
AND s.departmentId.id = :departmentId
AND s.expiryDate >= CURRENT_DATE

AND (:sectionId IS NULL OR sec.sectionId = :sectionId)
AND (:classId IS NULL OR cls.itemClassId = :classId)
AND (:itemId IS NULL OR i.itemId = :itemId)
ORDER BY i.nomenclature ASC
""")
    List<OpeningBalanceStockResponseDto> getStockDetails(
            Long hospitalId,
            Long departmentId,
            Long sectionId,
            Long classId,
            Long itemId
    );

    @Query("""
SELECT new com.hims.response.OpeningBalanceStockResponse(
    MIN(s.stockId),
    i.itemId,
    i.nomenclature,
    i.pvmsNo,
    COALESCE(SUM(s.openingBalanceQty),0),
    COALESCE(SUM(s.closingStock),0),
    u.unitName,
    sec.sectionId,
    sec.sectionName,
    cls.itemClassId,
    cls.itemClassName
)
FROM StoreItemBatchStock s
JOIN s.itemId i
JOIN i.unitAU u
JOIN i.itemClassId cls
JOIN cls.masStoreSection sec
WHERE s.hospitalId.id = :hospitalId
AND s.departmentId.id = :departmentId
AND s.expiryDate >= CURRENT_DATE

AND (:sectionId IS NULL OR sec.sectionId = :sectionId)
AND (:classId IS NULL OR cls.itemClassId = :classId)
AND (:itemId IS NULL OR i.itemId = :itemId)

GROUP BY
i.itemId, i.nomenclature, i.pvmsNo,
u.unitName,
sec.sectionId, sec.sectionName,
cls.itemClassId, cls.itemClassName
ORDER BY i.nomenclature ASC
""")
    List<OpeningBalanceStockResponse> getStockSummary(
            Long hospitalId,
            Long departmentId,
            Long sectionId,
            Long classId,
            Long itemId
    );

    Optional<Object> findByItemId_ItemIdAndBatchNo(Long itemId, String batchNo);

    // NEW: runtime row-level lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StoreItemBatchStock s " +
            "WHERE s.itemId.itemId = :itemId " +
            "AND s.batchNo = :batchNo " +
            "AND s.departmentId.id = :departmentId")
    Optional<StoreItemBatchStock> findByItemIdAndBatchNoForUpdate(
            @Param("itemId") Long itemId,
            @Param("batchNo") String batchNo,
            @Param("departmentId") Long departmentId);

    @Query(value = """
    SELECT *
    FROM store_item_batch_stock s
    WHERE s.hospital_id = :hospitalId
      AND s.department_id = :deptId
      AND s.item_id = :itemId
      AND s.batch_no = :batchName
      AND s.manufacture_date = :manufacturerDate
      AND s.manufacturer_id = :manufacturer
      AND (
            CAST(:expiryDate AS DATE) IS NULL
            OR s.expiry_date = CAST(:expiryDate AS DATE)
          )
    """,
            nativeQuery = true)
    List<StoreItemBatchStock> findStocksByUniqueCombination(
            @Param("hospitalId") Long hospitalId,
            @Param("deptId") Long deptId,
            @Param("itemId") Long itemId,
            @Param("batchName") String batchName,
            @Param("manufacturerDate") LocalDate manufacturerDate,
            @Param("expiryDate") LocalDate expiryDate,
            @Param("manufacturer") Long manufacturer
    );
}
