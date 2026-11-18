package com.hims.projection;

import java.time.LocalDate;

public interface BloodDonorDeferralProjection {

    Long getDonorId();

    String getDonorCode();

    String getDonorName();

    String getDeferralType();

    String getDeferralReason();

    LocalDate getStartDate();

    LocalDate getEndDate();

    String getScreenedBy();
}