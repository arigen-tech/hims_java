package com.hims.projection;

import java.time.LocalDateTime;

public interface PendingForMandatoryTestingProjection {
    Long getDonationId();
    Long getDonorId();
    String getBagNumber();
    String getDonorResNo();
    String getFullName();
    String getBloodGroup();
    LocalDateTime getCollectionDateTime();
    String getCollectionType();
    Long getNoOfComponent();
    String getCurrentStatus();
    String getBagType();
    LocalDateTime getComponentGenerationDateTime();
}
