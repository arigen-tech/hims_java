package com.hims.projection;
import java.time.LocalDate;

public interface MasEmployeeProjection {

    Long getEmployeeId();

    String getFirstName();

    String getMiddleName();

    String getLastName();

    LocalDate getDob();

    Long getGenderId();

    String getGender();

    String getMobileNo();

    Long getEmploymentTypeId();

    String getEmploymentType();

    Long getEmployeeTypeId();

    String getEmployeeType();

    String getStatus();
}