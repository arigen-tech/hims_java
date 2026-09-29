package com.hims.projection;

public interface PatientVisitStatusCountProjection {

    Long getPatientId();

    String getPatientName();

    String getMobileNo();

    Long getOpdPendingCount();

    Long getOpdCompleteCount();

    Long getLabPendingCount();

    Long getLabCompleteCount();

    Long getRadPendingCount();

    Long getRadCompleteCount();

    Long getTotalPendingCount();

    Long getTotalCompleteCount();

    Long getPrescriptionCount();
}