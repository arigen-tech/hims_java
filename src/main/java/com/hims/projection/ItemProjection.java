package com.hims.projection;
import java.math.BigDecimal;

public interface ItemProjection {

    Long getItemId();
    String getPvmsNo();
    String getNomenclature();

    Integer getGroupId();
    String getGroupName();

    Integer getItemTypeId();
    String getItemTypeName();

    Integer getSectionId();
    String getSectionName();

    Integer getItemClassId();
    String getItemClassName();

    Integer getMasItemCategoryId();
    String getMasItemCategoryName();

    Long getUnitAU();
    String getUnitAuName();

    Long getDispUnit();
    String getDispUnitName();

    String getHsnCode();
    BigDecimal getHsnGstPercent();

    Integer getReOrderLevelDispensary();
    Integer getReOrderLevelStore();
    BigDecimal getAdispQty();
}