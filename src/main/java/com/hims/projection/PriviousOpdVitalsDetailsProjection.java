package com.hims.projection;

import java.time.LocalDate;

public interface PriviousOpdVitalsDetailsProjection {
     LocalDate getVisitDate();
     String getHeight();
     String getPulse();
     String getWeight();
     String getTemperature();
     String getRr();
     String getBmi();
     String getSpo2();
     String getBpSystolic();
     String getBpDiastolic();
}
