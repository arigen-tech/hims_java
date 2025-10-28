package com.hims.response;

import com.hims.entity.MasBrand;
import com.hims.entity.MasManufacturer;
import com.hims.entity.MasStoreItem;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class OpeningBalanceDtResponse {

    private Long balanceTId;
    private Long balanceMId;
    private Long itemId;
    private String itemName;
    private String itemUnit;
    private BigDecimal itemGst;
    private String itemCode;
    private String batchNo;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private Long qty;
    private Long unitsPerPack;
    private BigDecimal purchaseRatePerUnit;
    private BigDecimal gstPercent;
    private BigDecimal mrpPerUnit;
    private String hsnCode;
    private BigDecimal baseRatePerUnit;
    private BigDecimal gstAmountPerUnit;
    private BigDecimal totalPurchaseCost;
    private BigDecimal totalMrpValue;
    private Long brandId;
    private Long manufacturerId;
    private String brandName;
    private String manufacturerName;


}
