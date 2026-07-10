package com.hims.projection;

import java.time.Instant;
import java.time.LocalDate;

public interface ResultEntryHeaderForResultValidation {

    Long getResultEntryId();

    String getPatientName();

    String getGenderName();

    String getPatientAge();

    String getPatientMobileNumber();

    String getRelationName();

    String getDoctorName();

    Integer getOrderhdId();

    String getOrderNo();

    LocalDate getOrderDate();

    Instant getOrderTime();
}
