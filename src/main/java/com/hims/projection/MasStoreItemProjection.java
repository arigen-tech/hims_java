package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MasStoreItemProjection {

    Long getItemId();
    String getPvmsNo();
    String getNomenclature();
    String getStatus();
    Long getLastChgBy();
    LocalDate getLastChgDate();
    String getLastChgTime();
    BigDecimal getAdispQty();

    Long getUnitAU();
    Long getDispUnit();
    Integer getSectionId();
    Integer getItemTypeId();
    Integer getGroupId();
    Integer getItemClassId();
    Integer getMasItemCategoryid();

    String getMasItemCategoryName();
    String getUnitAuName();
    String getDispUnitName();
    String getSectionName();
    String getItemTypeName();
    String getGroupName();
    String getItemClassName();
    String getHsnCode();
    BigDecimal getHsnGstPercent();



    Long getRequestedDeptStocks();
    Long getCurrentDeptStocks();

    Integer getReOrderLevelDispensary();
    Integer getReOrderLevelStore();

    String getDosageUnit();
}