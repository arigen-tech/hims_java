package com.hims.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SampleHeaderForValidationProjection {

    Long getSampleCollectionHeaderId();
    LocalDateTime getCollectionTime();
    String getOrderNo();
    LocalDate getOrderDate();
    String getPatientName();
    String getMobileNumber();
    String getGenderName();
    String getAge();
    Long getSubId();
    String getSubName();
    String getDoctorName();
    String getRelationName();
    String getCollectionBy();
}
