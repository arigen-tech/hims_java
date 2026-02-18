package com.hims.projection;

import java.time.Instant;
import java.util.Date;

/**
 * Interface projection for appointment history queries.
 * Provides efficient read-only access to appointment data without entity loading overhead.
 */
public interface AppointmentHistoryProjection {

    Long getVisitId();

    Long getPatientId();

    String getPatientName();

    String getMobileNumber();

    String getPatientAge();

    Long getDoctorId();

    String getDoctorName();

    Long getDepartmentId();

    String getDepartmentName();

    Instant getAppointmentDate();

    Instant getAppointmentStartTime();

    Instant getAppointmentEndTime();

    String getVisitStatus();

    String getReason();

    String getPaymentStatus();

    Double getBilledAmount();

    Long getBillingHeaderId();
}

