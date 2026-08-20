package com.hims.entity.repository;

import com.hims.entity.StoreReturnT;
import com.hims.response.UnverifiedReturnDetailResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreReturnTRepository extends JpaRepository<StoreReturnT, Long> {
    @Query("""
    SELECT new com.hims.response.UnverifiedReturnDetailResponse(
        rt.returnTId,
        rt.storeReturnM.returnMId,

        item.itemId,
        item.nomenclature,

        stock.stockId,
        rt.batchNo,

        rt.expiryDate,
        rt.dom,

        rt.brandName,
        rt.manufacturerName,

        rt.rejectedQty,
        rt.usableQty,
        rt.damagedQty,

        rt.returnReason,
        rt.storeVerification
    )
    FROM StoreReturnT rt
    LEFT JOIN rt.masStoreItem item
    LEFT JOIN rt.storeItemBatchStock stock

    WHERE rt.storeReturnM.returnMId = :returnMId
    AND rt.isVerified='n'
    ORDER BY rt.returnTId
    """)
    List<UnverifiedReturnDetailResponse> getUnverifiedReturnDetails(
            @Param("returnMId") Long returnMId
    );

    @Query(value = """  
SELECT s.isVerified
FROM StoreReturnT s 
LEFT JOIN s.storeReturnM m
WHERE m.returnMId=:returnMId
""")
    List<String> getAllDetailsStatusByHeaderId(Long returnMId);
}
