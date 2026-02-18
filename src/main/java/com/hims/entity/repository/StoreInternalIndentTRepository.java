package com.hims.entity.repository;

import com.hims.entity.StoreInternalIndentM;
import com.hims.entity.StoreInternalIndentT;
import com.hims.response.PreviousIssueResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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



}
