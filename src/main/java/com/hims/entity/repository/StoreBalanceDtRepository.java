package com.hims.entity.repository;

import com.hims.entity.StoreBalanceDt;
import com.hims.entity.StoreBalanceHd;
import com.hims.projection.OpeningBalanceEntryDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreBalanceDtRepository extends JpaRepository<StoreBalanceDt,Long> {
    void deleteByBalanceMId(StoreBalanceHd updatedHd);

    List<StoreBalanceDt> findByBalanceMId(StoreBalanceHd hd);

    @Query("""
SELECT
    sbd.balanceTId AS balanceTId,
    sbd.balanceMId.balanceMId AS balanceMId,

    item.itemId AS itemId,
    item.nomenclature AS itemName,
    item.unitAU.unitName AS itemUnit,
    item.hsnCode.gstRate AS itemGst,
    item.pvmsNo AS itemCode,

    sbd.batchNo AS batchNo,
    sbd.manufactureDate AS manufactureDate,
    sbd.expiryDate AS expiryDate,

    sbd.qty AS qty,
    sbd.unitsPerPack AS unitsPerPack,

    sbd.purchaseRatePerUnit AS purchaseRatePerUnit,
    sbd.gstPercent AS gstPercent,
    sbd.mrpPerUnit AS mrpPerUnit,

    sbd.hsnCode.hsnCode AS hsnCode,

    sbd.baseRatePerUnit AS baseRatePerUnit,
    sbd.gstAmountPerUnit AS gstAmountPerUnit,
    sbd.totalPurchaseCost AS totalPurchaseCost,
    sbd.totalMrp AS totalMrpValue,

    sbd.brandId.brandId AS brandId,
    sbd.manufacturerId.manufacturerId AS manufacturerId,

    sbd.brandId.brandName AS brandName,
    sbd.manufacturerId.manufacturerName AS manufacturerName

FROM StoreBalanceDt sbd
LEFT JOIN sbd.itemId item
WHERE sbd.balanceMId.balanceMId = :balanceMId
""")
    List<OpeningBalanceEntryDetailProjection> findOpeningBalanceDetailsWrtHeader(@Param("balanceMId") Long balanceMId);
}
