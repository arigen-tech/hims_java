package com.hims.projection;

import java.util.Date;

public interface DoctorRosterWeeklyProjection {
    Long getId();
    Date getRoasterDate();
    String getRoasterValue();
    Long getDoctorUserId();
}
