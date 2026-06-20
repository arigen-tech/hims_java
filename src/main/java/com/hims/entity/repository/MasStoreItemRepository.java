package com.hims.entity.repository;

import com.hims.entity.MasItemCategory;
import com.hims.entity.MasStoreItem;
import com.hims.projection.ItemProjection;
import com.hims.projection.MasStoreItemProjection;
import com.hims.projection.NonDrugStoreItemProjection;
import com.hims.response.ItemStockLedgerWithBatchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MasStoreItemRepository extends JpaRepository<MasStoreItem,Long> {
    //  List<MasStoreItem> findByStatusIgnoreCaseAndHospitalIdAndDepartmentId(String y, Long hospitalId, Long departmentId);

    // List<MasStoreItem> findByStatusInIgnoreCaseAndHospitalIdAndDepartmentId(List<String> y,  Long hospitalId, Long departmentId);

    Optional<MasStoreItem> findByPvmsNo(String code);

    List<MasStoreItem> findByStatusIgnoreCaseAndSectionId_SectionId(String status, Integer sectionId);

    List<MasStoreItem> findByStatusInIgnoreCaseAndSectionId_SectionId(List<String> statuses, Integer sectionId);


    Optional<MasStoreItem> findFirstByPvmsNoOrNomenclature(String pvmsNo, String nomenclature);


    Optional<MasStoreItem> findByPvmsNoAndItemIdNot(String pvmsNo, Long id);

    Optional<MasStoreItem> findByNomenclatureAndItemIdNot(String nomenclature, Long id);

    List<MasStoreItem> findByStatus(String y);

    List<MasStoreItem> findByStatusIgnoreCase(String y);

    List<MasStoreItem> findByStatusInIgnoreCase(List<String> y);

    List<MasStoreItem> findByStatusIgnoreCaseOrderByLastChgDateDescLastChgTimeDesc(String status);

    List<MasStoreItem> findByStatusInIgnoreCaseOrderByLastChgDateDescLastChgTimeDesc(List<String> statuses);


    List<MasStoreItem> findByStatusIgnoreCaseOrderByNomenclatureAsc(String y);

    //  List<MasStoreItem> findByStatusIgnoreCaseInOrderByLastChgDateDesc(List<String> y);

    List<MasStoreItem> findAllByOrderByStatusDescLastChgDateDesc();

    List<MasStoreItem> findByStatusInIgnoreCaseOrderByStatusDescLastChgDateDescLastChgTimeDesc(List<String> y);

    @Query("""
            SELECT m FROM MasStoreItem m
            WHERE LOWER(m.status) IN :status
            ORDER BY m.status DESC,
                     m.lastChgDate DESC,
                     m.lastChgTime DESC
            """)
    List<MasStoreItem> findAllOrderByStatusDesc(
            @Param("status") List<String> status
    );


    Page<MasStoreItem> findByStatusIgnoreCase(String status, Pageable pageable);

    Page<MasStoreItem> findByStatusInIgnoreCase(List<String> status, Pageable pageable);

    @Query("""
            SELECT m FROM MasStoreItem m
            WHERE
              ((:flag = 0 AND LOWER(m.status) IN ('y','n')) OR (:flag = 1 AND LOWER(m.status) = 'y'))
              AND (:sectionId IS NULL OR m.sectionId.sectionId = :sectionId)
              AND (:search IS NULL OR LOWER(m.nomenclature) LIKE %:search% OR LOWER(m.pvmsNo) LIKE %:search%)
            """)
    Page<MasStoreItem> dynamicSearch(
            @Param("flag") int flag,
            @Param("sectionId") Long sectionId,
            @Param("search") String search,
            Pageable pageable
    );

    Page<MasStoreItem> findByNomenclatureContainingIgnoreCaseAndStatus(
            String nomenclature,
            String status,
            Pageable pageable
    );

    List<MasStoreItem> findByItemIdIn(List<Long> list);
    @Query("""
    select
        i.itemId as itemId,
        i.pvmsNo as pvmsNo,
        i.nomenclature as nomenclature,
        i.adispQty as adispQty,
        g.id as groupId,
        g.groupName as groupName,
        t.id as itemTypeId,
        t.name as itemTypeName,
        s.sectionId as sectionId,
        s.sectionName as sectionName,
        c.itemClassId as itemClassId,
        c.itemClassName as itemClassName,
        cat.itemCategoryId as masItemCategoryId,
        cat.itemCategoryName as masItemCategoryName,
        uau.unitId as unitAU,
        uau.unitName as unitAuName,
        du.unitId as dispUnit,
        du.unitName as dispUnitName,
        h.hsnCode as hsnCode,
        h.gstRate as hsnGstPercent,
        i.reOrderLevelDispensary as reOrderLevelDispensary,
        i.reOrderLevelStore as reOrderLevelStore
    from MasStoreItem i
    left join i.groupId g
    left join i.itemTypeId t
    left join i.sectionId s
    left join i.itemClassId c
    left join i.masItemCategory cat
    left join i.unitAU uau
    left join i.dispUnit du
    left join i.hsnCode h
    where
        (
            (:sectionId is not null and s.sectionId = :sectionId)
            or
            (:sectionId is null and s.sectionId <> 18)
        )
    order by i.nomenclature
""")
    List<ItemProjection> findDrugsBySection(@Param("sectionId") Integer sectionId);


    @Query("""
SELECT
    m.itemId as itemId,
    m.pvmsNo as pvmsNo,
    m.nomenclature as nomenclature,
    m.status as status,
    m.lastChgBy as lastChgBy,
    m.lastChgDate as lastChgDate,
    m.lastChgTime as lastChgTime,
    m.adispQty as adispQty,

    uau.unitId as unitAU,
    du.unitId as dispUnit,
    s.sectionId as sectionId,
    it.id as itemTypeId,
    g.id as groupId,
    ic.itemClassId as itemClassId,
    cat.itemCategoryId as masItemCategoryid,

    cat.itemCategoryName as masItemCategoryName,
    uau.unitName as unitAuName,
    du.unitName as dispUnitName,
    s.sectionName as sectionName,
    it.name as itemTypeName,
    g.groupName as groupName,
    ic.itemClassName as itemClassName,
    h.hsnCode as hsnCode,
    h.gstRate as hsnGstPercent,

    m.reOrderLevelDispensary as reOrderLevelDispensary,
    m.reOrderLevelStore as reOrderLevelStore,

    COALESCE(SUM(CASE
        WHEN sb.departmentId.id = :requestedDeptId
         AND sb.hospitalId.id = :hospitalId
         AND sb.closingStock > 0
         AND sb.expiryDate >= :drugExpDay
        THEN sb.closingStock ELSE 0 END),0) as requestedDeptStocks,

    COALESCE(SUM(CASE
        WHEN sb.departmentId.id = :currentDeptId
         AND sb.hospitalId.id = :hospitalId
         AND sb.closingStock > 0
         AND sb.expiryDate >= :drugExpDay
        THEN sb.closingStock ELSE 0 END),0) as currentDeptStocks
FROM MasStoreItem m
LEFT JOIN m.groupId g
LEFT JOIN m.itemClassId ic
LEFT JOIN m.itemTypeId it
LEFT JOIN m.sectionId s
LEFT JOIN m.unitAU uau
LEFT JOIN m.dispUnit du
LEFT JOIN m.masItemCategory cat
LEFT JOIN m.hsnCode h
LEFT JOIN StoreItemBatchStock sb ON sb.itemId.itemId = m.itemId

WHERE m.itemId = :itemId

GROUP BY
    m.itemId, m.pvmsNo, m.nomenclature, m.status,
    m.lastChgBy, m.lastChgDate, m.lastChgTime, m.adispQty,
    uau.unitId, du.unitId,
    s.sectionId, it.id, g.id, ic.itemClassId, cat.itemCategoryId,
    cat.itemCategoryName, uau.unitName, du.unitName,
    s.sectionName, it.name, g.groupName, ic.itemClassName,
    h.hsnCode, h.gstRate,
    m.reOrderLevelDispensary, m.reOrderLevelStore
""")
    Optional<MasStoreItemProjection> findItemWithStock(
            @Param("itemId") Long itemId,
            @Param("hospitalId") Long hospitalId,
            @Param("requestedDeptId") Long requestedDeptId,
            @Param("currentDeptId") Long currentDeptId,
            @Param(("drugExpDay")) LocalDate drugExpDay
//            @Param("storeExpiry") LocalDate storeExpiry,
//            @Param("dispExpiry") LocalDate dispExpiry,
//            @Param("wardExpiry") LocalDate wardExpiry
    );


    @Query("""
    SELECT new com.hims.response.ItemStockLedgerWithBatchResponse(
        m.itemId,
        m.pvmsNo,
        m.nomenclature
    )
    FROM MasStoreItem m
    WHERE m.status = 'y'
      AND m.sectionId.sectionId = :sectionId
      AND (
            LOWER(m.nomenclature) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(m.pvmsNo) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    ORDER BY m.nomenclature ASC
""")
    Page<ItemStockLedgerWithBatchResponse> searchItems(
            @Param("sectionId") Long sectionId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
    SELECT new com.hims.response.ItemStockLedgerWithBatchResponse(
        m.itemId,
        m.pvmsNo,
        m.nomenclature
    )
    FROM MasStoreItem m
    WHERE m.status = 'y'
      AND m.sectionId.sectionId != :sectionId
      AND (
            LOWER(m.nomenclature) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(m.pvmsNo) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    ORDER BY m.nomenclature ASC
""")
    Page<ItemStockLedgerWithBatchResponse> searchNonDrugItems(
            @Param("sectionId") Long sectionId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
    @Query("""
    SELECT new com.hims.response.ItemStockLedgerWithBatchResponse(
        m.itemId,
        m.pvmsNo,
        m.nomenclature
    )
    FROM MasStoreItem m
    WHERE m.status = 'y'
      
   AND (
            LOWER(m.nomenclature) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(m.pvmsNo) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    ORDER BY m.nomenclature ASC
""")
    Page<ItemStockLedgerWithBatchResponse> searchItems(
            @Param("keyword") String keyword,
            Pageable pageable
    );


    @Query("""
    SELECT m
    FROM MasStoreItem m
    LEFT JOIN FETCH m.dispUnit
    LEFT JOIN FETCH m.itemClassId
    WHERE m.itemId IN :itemIds
""")
    List<MasStoreItem> findAllByItemIds(
            @Param("itemIds") List<Long> itemIds
    );



    @Query("""
        SELECT
            m.itemId AS itemId,
            m.pvmsNo AS pvmsNo,
            m.nomenclature AS nomenclature,

            g.id AS groupId,
            g.groupName AS groupName,

            it.id AS itemTypeId,
            it.name AS itemTypeName,

            s.sectionId AS sectionId,
            s.sectionName AS sectionName,

            ic.itemClassId AS itemClassId,
            ic.itemClassName AS itemClassName,

            c.itemCategoryId AS masItemCategoryId,
            c.itemCategoryName AS masItemCategoryName,

            u.unitId AS unitAU,
            u.unitName AS unitAuName,

            m.status AS status

        FROM MasStoreItem m
        LEFT JOIN m.groupId g
        LEFT JOIN m.itemTypeId it
        LEFT JOIN m.sectionId s
        LEFT JOIN m.itemClassId ic
        LEFT JOIN m.masItemCategory c
        LEFT JOIN m.unitAU u

        WHERE  s.sectionId <> :sectionId

        ORDER BY m.lastChgDate DESC,  m.lastChgTime DESC
        """)
    List<NonDrugStoreItemProjection> getAllNonDrugItems( @Param("sectionId") Integer sectionId);
}