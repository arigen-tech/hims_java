package com.hims.projection;

import java.time.LocalDate;

public interface PriviousHistoryByPatientProjectionResponse {
    LocalDate getVisitDate();
    String getDoctorName();
    String getDepartment();
    String getIcdDiag();
    String getWorkingDiag();
}
