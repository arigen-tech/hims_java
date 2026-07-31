package com.hims.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface BloodDonorDetailsProjection {

    Long getDonorId();

    String getDonorCode();
    String getFirstName();

    String getLastName();
    String getGender();
    LocalDate getDateOfBirth();
    String getMobileNo();
    Long getBloodGroupId();
    String getBloodGroup();
    Long getDonationType();
    String getRelation();
    String getDonorScreeningStatus();
    String getCurrentDeferralReason();
    LocalDate getDeferralUpToDate();
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
    LocalDateTime getCreatedDate();
    String getCreatedBy();
}
