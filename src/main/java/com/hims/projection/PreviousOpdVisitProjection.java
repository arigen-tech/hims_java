package com.hims.projection;

import java.time.LocalDate;

public interface PreviousOpdVisitProjection {
    LocalDate getVisitDate();
    Long getVisitId();
    String getDoctorName();
    String getDepartment();
    String getIcdDiag();
    String getWorkingDiag();
}
