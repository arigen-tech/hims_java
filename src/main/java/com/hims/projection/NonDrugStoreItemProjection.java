package com.hims.projection;
public interface NonDrugStoreItemProjection {

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

    String getStatus();
}