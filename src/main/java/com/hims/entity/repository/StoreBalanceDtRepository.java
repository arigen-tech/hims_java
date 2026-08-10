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
    unit.unitName AS itemUnit,
    hsn.gstRate AS itemGst,
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

    brand.brandId AS brandId,
    manufacturer.manufacturerId AS manufacturerId,

    brand.brandName AS brandName,
    manufacturer.manufacturerName AS manufacturerName

FROM StoreBalanceDt sbd
LEFT JOIN sbd.itemId item
LEFT JOIN item.unitAU unit
LEFT JOIN item.hsnCode hsn
LEFT JOIN sbd.brandId brand
LEFT JOIN sbd.manufacturerId manufacturer
WHERE sbd.balanceMId.balanceMId = :balanceMId
""")
    List<OpeningBalanceEntryDetailProjection> findOpeningBalanceDetailsWrtHeader(@Param("balanceMId") Long balanceMId);
}
