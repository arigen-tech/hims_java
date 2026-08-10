package com.hims.projection;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MasStoreItemsProjection {

    Long getItemId();

    String getPvmsNo();

    String getNomenclature();

    String getStatus();

    Long getLastChgBy();

    LocalDate getLastChgDate();

    String getLastChgTime();

    Long getStorestocks();

    Long getDispstocks();

    Long getWardstocks();

    BigDecimal getAdispQty();

    Long getHospitalId();

    Long getDepartmentId();

    Long getUnitAU();

    Long getDispUnit();

    Integer getSectionId();

    Integer getItemTypeId();

    Integer getGroupId();

    Integer getItemClassId();

    Integer getMasItemCategoryid();

    String getHsnCode();

    String getMasItemCategoryName();

    String getUnitAuName();

    String getDispUnitName();

    String getSectionName();

    String getItemTypeName();

    String getGroupName();

    String getItemClassName();

    BigDecimal getHsnGstPercent();

    BigDecimal getReOrderLevelDispensary();

    BigDecimal getReOrderLevelStore();

    String getIsGeneric();

    String getDangerousDrug();

    String getDrugSchedule();

    String getHighValueDrug();

    String getAvailableInOpd();

    String getAvailableInIpd();

    String getAvailableInEmergency();

    String getAvailableInOt();

    String getDosageUnit();

}