package com.hims.entity.repository;

import com.hims.entity.StoreStockLedger;
import com.hims.response.StoreStockLedgerReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreStockLedgerRepository extends JpaRepository<StoreStockLedger,Long> {

    @Query("""
SELECT new com.hims.response.StoreStockLedgerReportResponse(
    l.ledgerId,
    l.createdDt,
    l.referenceNum,
    l.txnType,
    l.txnSource,
    l.qtyBefore,
    l.qtyIn,
    l.qtyOut,
    l.qtyReject,
    l.qtyAfter,
    l.remarks
)
FROM StoreStockLedger l
JOIN l.hospital h
JOIN l.dept d
JOIN l.stockId s
JOIN s.itemId i
WHERE h.id = :hospitalId
AND d.id = :deptId
AND i.itemId = :itemId
AND s.batchNo = :batchNo
""")
    Page<StoreStockLedgerReportResponse> findLedgerReport(
            @Param("hospitalId") Long hospitalId,
            @Param("deptId") Long deptId,
            @Param("itemId") Long itemId,
            @Param("batchNo") String batchNo,
            Pageable pageable
    );

}
