package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface BloodDonorCollectionDetailsProjection {
    Long getDonorId();
    String getDonorCode();
    String getFirstName();
    String getLastName();
    String getGender();
    LocalDate getDateOfBirth();
    String getMobileNo();
    Long getBloodGroupId();
    String getBloodGroup();
    String getDonorScreeningStatus();
    String getAddressLine1();
    String getAddressLine2();
    Long getCountry();
    String getCountryName();
    Long getState();
    String getStateName();
    Long getDistrict();
    String getDistrictName();
    String getCity();
    String getPinCode();
    Long getScreeningId();
    LocalDate getScreeningDate();
    BigDecimal getHemoglobin();
    BigDecimal getWeight();
    BigDecimal getHeight();
    String getBp();
    Integer getPulse();
    BigDecimal getTemperature();
}
