package com.hims.projection;

import java.time.LocalDate;

public interface DonorProjection {
    Long getDonorId();
    Long getScreeningId();
    String getDonorCode();
    String getName();
    String getGender();
    String getMobileNo();
    String getBloodGroup();
    LocalDate getRegistrationDate();
    String getScreeningResult();
    String getDeferralType();
}
