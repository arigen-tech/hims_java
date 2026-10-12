package com.hims.projection;

public interface UserContextProjection {

    Long getUserId();
    String getUserName();
    String getEmail();
    Long getHospitalId();
    Long getDepartmentId();
    String getUserFullName();
}