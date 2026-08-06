package com.hims.entity.repository;

import com.hims.entity.MasStoreItem;
import com.hims.projection.*;
import com.hims.response.ItemStockLedgerWithBatchResponse;
import com.hims.response.NonDrugStoreItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
    m.dosageUnit as dosageUnit,

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
    m.reOrderLevelDispensary, m.reOrderLevelStore, m.dosageUnit
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
    LEFT JOIN m.sectionId s
    WHERE m.status = 'y'
      AND s.sectionCode = :sectionCode
      AND (
            LOWER(m.nomenclature) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(m.pvmsNo) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    ORDER BY m.nomenclature ASC
""")
    Page<ItemStockLedgerWithBatchResponse> searchItems(
            @Param("sectionCode") String sectionCode,
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
    LEFT JOIN m.sectionId s
    LEFT JOIN m.itemTypeId t
    WHERE m.status = 'y'
      AND s.sectionCode != :sectionCode
      AND t.code IN (:medicalConsumablesAndNonConsumables)
      AND (
            LOWER(m.nomenclature) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(m.pvmsNo) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    ORDER BY m.nomenclature ASC
""")
    Page<ItemStockLedgerWithBatchResponse> searchNonDrugItems(
            @Param("sectionCode") String sectionCode,
            @Param("keyword") String keyword,
            @Param("medicalConsumablesAndNonConsumables") List<String> medicalConsumablesAndNonConsumables,
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
    List<NonDrugStoreItemProjection> getAllNonDrugItems(@Param("sectionId") Integer sectionId);

    List<MasStoreItem> findBySectionIdSectionIdAndStatusIgnoreCaseOrderByLastChgDateDescLastChgTimeDesc(Integer sectionId, String y);

    List<MasStoreItem> findBySectionIdSectionIdAndStatusInIgnoreCaseOrderByLastChgDateDescLastChgTimeDesc(Integer sectionId, List<String> y);

    @Query("""
    SELECT
        m.itemId AS itemId,
        m.pvmsNo AS pvmsNo,
        m.nomenclature AS nomenclature,
        m.status AS status,
        m.lastChgBy AS lastChgBy,
        m.lastChgDate AS lastChgDate,
        m.lastChgTime AS lastChgTime,
        m.adispQty AS adispQty,

        du.unitId AS dispUnit,
        du.unitName AS dispUnitName,

        au.unitId AS unitAU,
        au.unitName AS unitAuName,

        sec.sectionId AS sectionId,
        sec.sectionName AS sectionName,

        it.id AS itemTypeId,
        it.name AS itemTypeName,

        grp.id AS groupId,
        grp.groupName AS groupName,

        cls.itemClassId AS itemClassId,
        cls.itemClassName AS itemClassName,

        cat.itemCategoryId AS masItemCategoryid,
        cat.itemCategoryName AS masItemCategoryName,

        hsn.hsnCode AS hsnCode,
        hsn.gstRate AS hsnGstPercent,

        m.reOrderLevelDispensary AS reOrderLevelDispensary,
        m.reOrderLevelStore AS reOrderLevelStore,

        m.dangerousDrug AS dangerousDrug,
        m.isGeneric AS isGeneric,
        m.drugSchedule AS drugSchedule,
        m.highValueDrug AS highValueDrug,
        m.availableInOpd AS availableInOpd,
        m.availableInIpd AS availableInIpd,
        m.availableInEmergency AS availableInEmergency,
        m.availableInOt AS availableInOt,
        m.dosageUnit AS dosageUnit

    FROM MasStoreItem m
    LEFT JOIN m.dispUnit du
    LEFT JOIN m.unitAU au
    LEFT JOIN m.sectionId sec
    LEFT JOIN m.itemTypeId it
    LEFT JOIN m.groupId grp
    LEFT JOIN m.itemClassId cls
    LEFT JOIN m.masItemCategory cat
    LEFT JOIN m.hsnCode hsn

    WHERE 
      LOWER(m.status) = LOWER(:status)
ORDER BY m.lastChgDate DESC, m.lastChgTime DESC
""")
    List<MasStoreItemsProjection> findActiveItemsBySectionId(

            @Param("status") String status
    );


    @Query("""
    SELECT
        m.itemId AS itemId,
        m.pvmsNo AS pvmsNo,
        m.nomenclature AS nomenclature,
        m.status AS status,
        m.lastChgBy AS lastChgBy,
        m.lastChgDate AS lastChgDate,
        m.lastChgTime AS lastChgTime,
        m.adispQty AS adispQty,

        du.unitId AS dispUnit,
        du.unitName AS dispUnitName,

        au.unitId AS unitAU,
        au.unitName AS unitAuName,

        sec.sectionId AS sectionId,
        sec.sectionName AS sectionName,

        it.id AS itemTypeId,
        it.name AS itemTypeName,

        grp.id AS groupId,
        grp.groupName AS groupName,

        cls.itemClassId AS itemClassId,
        cls.itemClassName AS itemClassName,

        cat.itemCategoryId AS masItemCategoryid,
        cat.itemCategoryName AS masItemCategoryName,

        hsn.hsnCode AS hsnCode,
        hsn.gstRate AS hsnGstPercent,

        m.reOrderLevelDispensary AS reOrderLevelDispensary,
        m.reOrderLevelStore AS reOrderLevelStore,

        m.dangerousDrug AS dangerousDrug,
        m.isGeneric AS isGeneric,
        m.drugSchedule AS drugSchedule,
        m.highValueDrug AS highValueDrug,
        m.availableInOpd AS availableInOpd,
        m.availableInIpd AS availableInIpd,
        m.availableInEmergency AS availableInEmergency,
        m.availableInOt AS availableInOt,
        m.dosageUnit AS dosageUnit

    FROM MasStoreItem m
    LEFT JOIN m.dispUnit du
    LEFT JOIN m.unitAU au
    LEFT JOIN m.sectionId sec
    LEFT JOIN m.itemTypeId it
    LEFT JOIN m.groupId grp
    LEFT JOIN m.itemClassId cls
    LEFT JOIN m.masItemCategory cat
    LEFT JOIN m.hsnCode hsn

    WHERE sec.sectionId = :sectionId
      AND LOWER(m.status) IN :statusList

    ORDER BY m.status DESC, m.lastChgDate DESC, m.lastChgTime DESC
""")
    List<MasStoreItemsProjection> findAllItemsBySectionIdAndStatusIn(
            @Param("sectionId") Integer sectionId,
            @Param("statusList") List<String> statusList
    );
    @Query(value = """
SELECT
    i.item_id AS itemId,
    i.pvms_no AS pvmsNo,
    i.nomenclature AS nomenclature,

    g.group_id AS groupId,
    g.group_name AS groupName,

    it.item_type_id AS itemTypeId,
    it.item_type_name AS itemTypeName,

    s.section_id AS sectionId,
    s.section_name AS sectionName,

    ic.item_class_id AS itemClassId,
    ic.item_class_name AS itemClassName,

    c.item_category_id AS masItemCategoryId,
    c.item_category_name AS masItemCategoryName,

    u.unit_id AS unitAU,
    u.unit_name AS unitAuName,

    i.status AS status

FROM mas_store_item i

INNER JOIN mas_store_section s
        ON s.section_id = i.section_id

INNER JOIN mas_item_type it
        ON it.item_type_id = i.item_type_id

INNER JOIN mas_store_group g
        ON g.group_id = i.group_id

LEFT JOIN mas_item_class ic
       ON ic.item_class_id = i.item_class_id

LEFT JOIN mas_item_category c
       ON c.item_category_id = i.item_category_id

LEFT JOIN mas_store_unit u
       ON u.unit_id = i.unit_au

WHERE
      s.section_code <> :drugSectionCode
  AND it.item_type_code = :medicalConsumableItemTypeCode
  AND g.group_code = :groupCode
AND (
        :itemName IS NULL
        OR :itemName = ''
        OR LOWER(i.nomenclature) LIKE LOWER(CONCAT('%', :itemName, '%'))
    )

AND (
        :itemClassId IS NULL
        OR ic.item_class_id = :itemClassId
    )
    
AND (
        :sectionId IS NULL
        OR s.section_id = :sectionId
    )


ORDER BY i.nomenclature
""",
            countQuery = """
SELECT COUNT(*)
FROM mas_store_item i

INNER JOIN mas_store_section s
        ON s.section_id = i.section_id

INNER JOIN mas_item_type it
        ON it.item_type_id = i.item_type_id

INNER JOIN mas_store_group g
        ON g.group_id = i.group_id

LEFT JOIN mas_item_class ic
       ON ic.item_class_id = i.item_class_id

WHERE
      s.section_code <> :drugSectionCode
  AND it.item_type_code = :medicalConsumableItemTypeCode
  AND g.group_code = :groupCode
       AND (
          :itemName IS NULL
            OR :itemName = ''
          OR LOWER(i.nomenclature) LIKE LOWER(CONCAT('%', :itemName, '%'))
          )
       AND (
        :itemClassId IS NULL
        OR ic.item_class_id = :itemClassId
    )
      AND (
        :sectionId IS NULL
        OR s.section_id = :sectionId
    )

""",
            nativeQuery = true)
    Page<MedicalConsumableItemProjection> medicalConsumableItem(
            @Param("drugSectionCode") String drugSectionCode,
            @Param("medicalConsumableItemTypeCode") String medicalConsumableItemTypeCode,
            @Param("groupCode") String groupCode,
            @Param("itemName") String itemName,
            @Param("sectionId") Integer sectionId,
            @Param("itemClassId") Integer itemClassId,
            Pageable pageable);
    @Query(value = """
SELECT
    i.item_id AS itemId,
    i.pvms_no AS pvmsNo,
    i.nomenclature AS nomenclature,

    g.group_id AS groupId,
    g.group_name AS groupName,

    it.item_type_id AS itemTypeId,
    it.item_type_name AS itemTypeName,

    s.section_id AS sectionId,
    s.section_name AS sectionName,

    ic.item_class_id AS itemClassId,
    ic.item_class_name AS itemClassName,

    c.item_category_id AS masItemCategoryId,
    c.item_category_name AS masItemCategoryName,

    u.unit_id AS unitAU,
    u.unit_name AS unitAuName,

    i.status AS status

FROM mas_store_item i

INNER JOIN mas_store_section s
        ON s.section_id = i.section_id

INNER JOIN mas_item_type it
        ON it.item_type_id = i.item_type_id

INNER JOIN mas_store_group g
        ON g.group_id = i.group_id

LEFT JOIN mas_item_class ic
       ON ic.item_class_id = i.item_class_id

LEFT JOIN mas_item_category c
       ON c.item_category_id = i.item_category_id

LEFT JOIN mas_store_unit u
       ON u.unit_id = i.unit_au

WHERE
     it.item_type_code = :medicalNonConsumableItemTypeCode
  AND g.group_code = :groupCode
AND (
        :itemName IS NULL
        OR :itemName = ''
        OR LOWER(i.nomenclature) LIKE LOWER(CONCAT('%', :itemName, '%'))
    )

AND (
        :itemClassId IS NULL
        OR ic.item_class_id = :itemClassId
    )
       
AND (
        :sectionId IS NULL
        OR s.section_id = :sectionId
    )


ORDER BY i.nomenclature
""",
            countQuery = """
SELECT COUNT(*)
FROM mas_store_item i

INNER JOIN mas_store_section s
        ON s.section_id = i.section_id

INNER JOIN mas_item_type it
        ON it.item_type_id = i.item_type_id

INNER JOIN mas_store_group g
        ON g.group_id = i.group_id

LEFT JOIN mas_item_class ic
       ON ic.item_class_id = i.item_class_id

WHERE
      it.item_type_code = :medicalNonConsumableItemTypeCode
  AND g.group_code = :groupCode
       AND (
          :itemName IS NULL
            OR :itemName = ''
          OR LOWER(i.nomenclature) LIKE LOWER(CONCAT('%', :itemName, '%'))
          )
       AND (
        :itemClassId IS NULL
        OR ic.item_class_id = :itemClassId
    )
""",
            nativeQuery = true)
    Page<MedicalConsumableItemProjection> nonMedicalConsumableItem(
            @Param("medicalNonConsumableItemTypeCode") String medicalNonConsumableItemTypeCode,
            @Param("groupCode") String groupCode,
            @Param("itemName") String itemName,
            @Param("sectionId") Integer sectionId,
            @Param("itemClassId") Integer itemClassId,
            Pageable pageable);

    @Query(value = """
    SELECT
        m.itemId AS itemId,
        m.pvmsNo AS pvmsNo,
        m.nomenclature AS nomenclature,
        m.status AS status,
        m.lastChgBy AS lastChgBy,
        m.lastChgDate AS lastChgDate,
        m.lastChgTime AS lastChgTime,
        m.adispQty AS adispQty,

        du.unitId AS dispUnit,
        du.unitName AS dispUnitName,

        au.unitId AS unitAU,
        au.unitName AS unitAuName,

        sec.sectionId AS sectionId,
        sec.sectionName AS sectionName,

        it.id AS itemTypeId,
        it.name AS itemTypeName,

        grp.id AS groupId,
        grp.groupName AS groupName,

        cls.itemClassId AS itemClassId,
        cls.itemClassName AS itemClassName,

        cat.itemCategoryId AS masItemCategoryid,
        cat.itemCategoryName AS masItemCategoryName,

        hsn.hsnCode AS hsnCode,
        hsn.gstRate AS hsnGstPercent,

        m.reOrderLevelDispensary AS reOrderLevelDispensary,
        m.reOrderLevelStore AS reOrderLevelStore,

        m.dangerousDrug AS dangerousDrug,
        m.isGeneric AS isGeneric,
        m.drugSchedule AS drugSchedule,
        m.highValueDrug AS highValueDrug,
        m.availableInOpd AS availableInOpd,
        m.availableInIpd AS availableInIpd,
        m.availableInEmergency AS availableInEmergency,
        m.availableInOt AS availableInOt,
        m.dosageUnit AS dosageUnit

    FROM MasStoreItem m
    LEFT JOIN m.dispUnit du
    LEFT JOIN m.unitAU au
    LEFT JOIN m.sectionId sec
    LEFT JOIN m.itemTypeId it
    LEFT JOIN m.groupId grp
    LEFT JOIN m.itemClassId cls
    LEFT JOIN m.masItemCategory cat
    LEFT JOIN m.hsnCode hsn

    WHERE 
      ((:flag = 1 AND LOWER(m.status) = 'y') OR (:flag = 0 AND LOWER(m.status) IN ('y', 'n')))
      AND (:sectionId IS NULL OR sec.sectionId = :sectionId)
      AND (:nomenclature IS NULL OR :nomenclature = '' OR LOWER(m.nomenclature) LIKE LOWER(CONCAT('%', :nomenclature, '%')))
      AND (:itemClassId IS NULL OR cls.itemClassId = :itemClassId)
      AND (:masItemCategoryid IS NULL OR cat.itemCategoryId = :masItemCategoryid)

    ORDER BY m.status DESC, m.lastChgDate DESC, m.lastChgTime DESC
    """,
    countQuery = """
    SELECT COUNT(m)
    FROM MasStoreItem m
    LEFT JOIN m.sectionId sec
    LEFT JOIN m.itemClassId cls
    LEFT JOIN m.masItemCategory cat
    WHERE 
      ((:flag = 1 AND LOWER(m.status) = 'y') OR (:flag = 0 AND LOWER(m.status) IN ('y', 'n')))
      AND (:sectionId IS NULL OR sec.sectionId = :sectionId)
      AND (:nomenclature IS NULL OR :nomenclature = '' OR LOWER(m.nomenclature) LIKE LOWER(CONCAT('%', :nomenclature, '%')))
      AND (:itemClassId IS NULL OR cls.itemClassId = :itemClassId)
      AND (:masItemCategoryid IS NULL OR cat.itemCategoryId = :masItemCategoryid)
    """)
    Page<MasStoreItemsProjection> findItemsWithOutStockPaginated(
            @Param("flag") int flag,
            @Param("sectionId") Integer sectionId,
            @Param("nomenclature") String nomenclature,
            @Param("itemClassId") Integer itemClassId,
            @Param("masItemCategoryid") Integer masItemCategoryid,
            Pageable pageable
    );
}