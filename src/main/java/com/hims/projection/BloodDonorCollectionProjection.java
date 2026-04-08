package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface BloodDonorCollectionProjection {
    Long getDonorId();
    String getDonorCode();
    String getFirstName();
    String getLastName();
    Long getBloodGroupId();
    String getBloodGroup();
    LocalDate getLastScreening();
    String getHb();
    BigDecimal getWeight();
}
