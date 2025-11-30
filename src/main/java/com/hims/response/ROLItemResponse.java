package com.hims.response;

import com.hims.entity.MasStoreItem;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ROLItemResponse {
    private Long itemId;
    private String itemName;
    private String pvmsNo;
    private Long availableQty;
    private Integer rolQty;
    private String unit;
    private BigDecimal storeROL;
    private BigDecimal dispROL;
    private BigDecimal wardROL;
    private Integer reOrderLevelStore;
    private Integer reOrderLevelDispensary;

    // Add these stock fields
    private Long storeStock;
    private Long wardStock;
    private Long dispStock;

    public ROLItemResponse() {}

    public ROLItemResponse(MasStoreItem item, Long availableQty, Integer rolQty, Long storeStock, Long wardStock, Long dispStock) {
        this.itemId = item.getItemId();
        this.itemName = item.getNomenclature();
        this.pvmsNo = item.getPvmsNo();
        this.availableQty = availableQty;
        this.rolQty = rolQty;
        this.unit = item.getUnitAU() != null ? item.getUnitAU().getUnitName() :
                (item.getDispUnit() != null ? item.getDispUnit().getUnitName() : "");
        this.storeROL = item.getStoreROL();
        this.dispROL = item.getDispROL();
        this.wardROL = item.getWardROL();
        this.reOrderLevelStore = item.getReOrderLevelStore();
        this.reOrderLevelDispensary = item.getReOrderLevelDispensary();
        this.wardROL = item.getWardROL();

        // Set the stock fields
        this.storeStock = storeStock;
        this.wardStock = wardStock;
        this.dispStock = dispStock;
    }
}