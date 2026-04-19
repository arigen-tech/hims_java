package com.hims.entity.repository;

import com.hims.entity.StoreInternalIndentM;
import com.hims.entity.StoreInternalIndentT;
import com.hims.projection.IndentDetailsForIssueProjection;
import com.hims.projection.IndentDetailsResponseForRequestDeptProjection;
import com.hims.response.IndentDetailsResponseForIndentTracking;
import com.hims.response.IndentDetailsResponseForRequestDept;
import com.hims.response.IndentDetailsWithAvlStock;
import com.hims.response.PreviousIssueResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface StoreInternalIndentTRepository extends JpaRepository<StoreInternalIndentT,Long> {

    List<StoreInternalIndentT> findByIndentM(StoreInternalIndentM indentM);
    List<StoreInternalIndentT> findByIndentM_IndentMId(Long indentMId);

    @Query(value = "SELECT DISTINCT d.item_id FROM store_internal_indent_t d " +
            "JOIN store_internal_indent_m m ON d.indent_m_id = m.indent_m_id " +
            "WHERE m.from_dept_id = :departmentId " +
            "AND m.status IN :statuses",
            nativeQuery = true)
    List<Long> findIndentedItemIds(
            @Param("departmentId") Long departmentId,
            @Param("statuses") List<String> statuses
    );


    // FIXED: Use native query for better control and accuracy
    @Query(value =
            "SELECT DISTINCT " +
                    "sibs.last_chg_date AS issueDate, " +
                    "sim.indent_no AS indentNo, " +
                    "sibs.indent_issue_qty AS qtyIssued, " +
                    "sibs.batch_no AS batchNo, " +
                    "sim.issue_no AS issueNo, " +
                    "sibs.expiry_date AS expiryDate " +
                    "FROM store_item_batch_stock sibs " +
                    "LEFT JOIN store_internal_indent_t sit ON sit.item_id = sibs.item_id " +
                    "LEFT JOIN store_internal_indent_m sim ON sim.indent_m_id = sit.indent_m_id " +
                    "LEFT JOIN store_issue_m sim2 ON sim2.store_issue_m_id = sim.store_issue_m_id " +
                    "WHERE sibs.item_id = :itemId " +
                    "AND sibs.indent_issue_qty > 0 " +
                    "ORDER BY sibs.last_chg_date DESC",
            nativeQuery = true)
    List<Map<String, Object>> findPreviousIssuesForItemAsMap(
            @Param("itemId") Long itemId
    );

    @Query("""
           SELECT t
           FROM StoreInternalIndentT t
           WHERE t.indentM.indentMId IN :indentMIds
           """)
    List<StoreInternalIndentT> findByIndentMIds(
            @Param("indentMIds") List<Long> indentMIds
    );

    @Query("""
    SELECT new com.hims.response.IndentDetailsResponseForIndentTracking(
        t.indentTId,
        i.nomenclature,
        u.unitName,
        t.requestedQty,
        t.approvedQty,
        t.receivedQty,
        t.reason,
        t.availableStock
    )
    FROM StoreInternalIndentT t
    JOIN t.itemId i
    LEFT JOIN i.unitAU u
    WHERE t.indentM.indentMId = :indentMId
""")
    List<IndentDetailsResponseForIndentTracking>
    findIndentDetailsForTracking(@Param("indentMId") Long indentMId);

    @Query("""
SELECT new com.hims.response.IndentDetailsWithAvlStock(
    t.indentTId,
    i.nomenclature,
    u.unitName,
    t.requestedQty,
    t.approvedQty,
    t.receivedQty,
    t.reason,
    COALESCE((
        SELECT SUM(s.closingStock)
        FROM StoreItemBatchStock s
        WHERE s.itemId.itemId = i.itemId
        AND s.departmentId.id = :departmentId
        AND s.expiryDate >= :expiryDate
    ), 0)
)
FROM StoreInternalIndentT t
LEFT JOIN t.itemId i
LEFT JOIN i.unitAU u
WHERE t.indentM.indentMId = :indentMId
""")
    List<IndentDetailsWithAvlStock> findIndentDetailsWithStock(
            @Param("indentMId") Long indentMId,
            @Param("departmentId") Long departmentId,
            @Param("expiryDate") LocalDate expiryDate
    );


    @Query(value = """
SELECT
    t.indent_t_id AS indentTId,

    i.item_id AS itemId,
    i.nomenclature AS itemName,
    i.pvms_no AS pvmsNo,

    t.requested_qty AS requestedQty,
    t.approved_qty AS approvedQty,

    (
        SELECT COALESCE(SUM(bs.closing_stock),0)
        FROM store_item_batch_stock bs
        WHERE bs.item_id = i.item_id
        AND bs.department_id = :deptId
        AND bs.expiry_date >= :expiryDate
    ) AS availableStock,

    t.issue_status AS issueStatus,
    t.reason AS reason,

    u.unit_name AS unitAuName,
    u.unit_id AS unitAUid,

    bs2.batch_no AS batchNo,
    bs2.closing_stock AS batchAvailableStock,
    mm.manufacturer_id AS manufacturerId,
    bs2.manufacture_date AS mfgDate,
    bs2.expiry_date AS expDate

FROM store_internal_indent_t t
JOIN mas_store_item i
    ON t.item_id = i.item_id

LEFT JOIN mas_store_unit u
    ON i.unit_au = u.unit_id

LEFT JOIN store_item_batch_stock bs2
    ON bs2.stock_id = (
        SELECT bs3.stock_id
        FROM store_item_batch_stock bs3
        WHERE bs3.item_id = i.item_id
        AND bs3.department_id = :deptId
        AND bs3.expiry_date >= :expiryDate
        AND bs3.closing_stock>0
        ORDER BY bs3.expiry_date
        LIMIT 1
    )
LEFT JOIN mas_manufacturer mm
ON bs2.manufacturer_id=mm.manufacturer_id
WHERE t.indent_m_id = :indentMId
""", nativeQuery = true)
    List<IndentDetailsForIssueProjection> findIndentDetailsForIssue(
            @Param("indentMId") Long indentMId,
            @Param("deptId") Long deptId,
            @Param("expiryDate") LocalDate expiryDate
    );



    @Query(value = """
SELECT
    t.indent_t_id AS indentTId,
    i.nomenclature AS itemName,
    u.unit_name AS itemUnitName,
    t.requested_qty AS qtyRequested,
    t.approved_qty AS qtyApproved,
    t.received_qty AS qtyReceived,
    t.reason AS reasonForIndent,

    COALESCE(store_stock.store_available_stock,0) AS storeAvailableStock,
    COALESCE(dept_stock.dept_available_stock,0) AS currentDeptAvailableStock

FROM store_internal_indent_t t
JOIN mas_store_item i ON i.item_id = t.item_id
LEFT JOIN mas_store_unit u ON u.unit_id = i.unit_au

LEFT JOIN (
    SELECT item_id, SUM(closing_stock) AS store_available_stock
    FROM store_item_batch_stock
    WHERE department_id = :requestedDeptId
    AND expiry_date >= :inventoryDrugExpDate
    GROUP BY item_id
) store_stock
ON store_stock.item_id = t.item_id

LEFT JOIN (
    SELECT item_id, SUM(closing_stock) AS dept_available_stock
    FROM store_item_batch_stock
    WHERE department_id = :currentDeptId
    AND expiry_date >= :inventoryDrugExpDate
    GROUP BY item_id
) dept_stock
ON dept_stock.item_id = t.item_id

WHERE t.indent_m_id = :indentMId
""", nativeQuery = true)
    List<IndentDetailsResponseForRequestDeptProjection> getIndentDetailsForRequestDept(
            Long indentMId,
            Long requestedDeptId,
            Long currentDeptId,
            LocalDate inventoryDrugExpDate

    );

    @Query(value = """
    SELECT DISTINCT s.indentM.indentType from StoreInternalIndentT s
    WHERE s.indentM.indentMId = :indentMId
    """)

    String getIndentTypeWrtIndentMId(Long indentMId);
}
