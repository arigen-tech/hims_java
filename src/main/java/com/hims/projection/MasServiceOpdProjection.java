package com.hims.projection;


import java.math.BigDecimal;
import java.time.LocalDate;

public interface MasServiceOpdProjection {

    Long getId();

    String getServiceName();

    BigDecimal getBaseTariff();

    String getServiceCategory();

    String getDepartmentName();

    String getDoctorFirstName();
    String getDoctorMiddleName();
    String getDoctorLastName();

    LocalDate getFromDate();

    LocalDate getToDate();

    String  getStatus();
}