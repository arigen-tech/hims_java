package com.hims.projection;
import java.time.LocalDateTime;

public interface PendingToTransferProjectionResponse {

    Long getInpatientId();

    Long getPatientId();

    String getTransferNo();

    LocalDateTime getTransferDateTime();

    String getPatientName();

    String getGender();

    String getAge();

    String getAdmissionNo();

    String getAdmissionDate();

    Long getFromWardId();

    String getFromWardName();

    Long getFromBedId();

    String getFromBedName();

    Long getToWardId();

    String getToWardName();

    Long getToBedId();

    String getToBedName();

    Long getTransferReasonId();

    String getTransferReason();



    String getTransferStatus();

    String getClinicalNotes();

    Long getDoctorId();

    String getDoctorName();

    String getUhidNo();
}