package com.hims.entity.repository;

import com.hims.entity.StoreInternalIndentT;
import com.hims.entity.StoreIssueM;
import com.hims.entity.StoreIssueT;
import com.hims.response.IndentDetailsResponseForReceiving;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreIssueTRepository extends JpaRepository<StoreIssueT,Long>{


    List<StoreIssueT> findByStoreIssueMId(StoreIssueM issueM);

    List<StoreIssueT> findByIndentTId(StoreInternalIndentT indentT);

    List<StoreIssueT> findByIndentTIdAndBatchNo(StoreInternalIndentT indentT, String batchNo);

    @Query("""
SELECT new com.hims.response.IndentDetailsResponseForReceiving(
    m.indentMId,
    t.indentTId,

    i.itemId,
    i.nomenclature,
    i.pvmsNo,

    u.unitId,
    u.unitName,

    s.batchNo,
    s.manufactureDate,
    s.expiryDate,

    t.requestedQty,
    it.issuedQty,
    t.receivedQty,

    mf.manufacturerName,
    br.brandName
)
FROM StoreInternalIndentT t
JOIN t.indentM m
JOIN t.itemId i
LEFT JOIN i.unitAU u
LEFT JOIN StoreIssueT it ON it.indentTId.indentTId = t.indentTId
LEFT JOIN it.stockId s
LEFT JOIN s.manufacturerId mf
LEFT JOIN s.brandId br
WHERE m.indentMId = :indentMId
""")
    List<IndentDetailsResponseForReceiving> findIndentDetailsForReceiving(Long indentMId);
}
