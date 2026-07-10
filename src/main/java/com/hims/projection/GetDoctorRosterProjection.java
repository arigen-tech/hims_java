package com.hims.projection;

import java.time.LocalDate;
import java.util.Date;

public interface GetDoctorRosterProjection {
    Integer getId();
    Long getHospitalId();
    Long getDeptmentId();
    Long getChgBy();
    LocalDate getChgDate();
    String getChgTime();
    Long getDoctorId();
    String getRosterVal();
    Date getRoasterDate();
}
