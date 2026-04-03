package com.hims.projection;

import java.time.LocalDate;

public interface PatientProjectionFollowUpDetails {
    Long getPatientId();
    String getFirstName();
    String getMiddleName();
    String getLastName();
    String getMobileNo();
    String getEmail();
    LocalDate getDob();
    String getAge();
    Long getGenderId();
    String getGenderName();
    Long getRelationId();
    String getRelationName();

    // Address
    String getAddress1();
    String getAddress2();
    String getCity();
    String getPinCode();
    Long getCountryId();
    String getCountryName();
    Long getStateId();
    String getStateName();
    Long getDistrictId();
    String getDistrictName();


    // NOK
    String getNokFirstName();
    String getNokLastName();
    String getNokEmail();
    String getNokMobile();
    String getNokAddress1();
    String getNokAddress2();
    String getNokCity();
    String getNokPinCode();
    Long getNokCountryId();
    String getNokCountryName();
    Long getNokStateId();
    String getNokStateName();
    Long getNokDistrictId();
    String getNokDistrictName();

    // Emergency
    String getEmerFirstName();
    String getEmerLastName();
    String getEmerMobile();
    String getPhotoUrl();
}
