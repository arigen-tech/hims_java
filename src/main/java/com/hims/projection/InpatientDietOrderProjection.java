package com.hims.projection;

import java.time.LocalDateTime;

public interface InpatientDietOrderProjection {

    Long getInpatientId();

    String getPatientName();

    String getUhid();

    String getAge();

    Long getGenderId();

    String getGender();

    String getMobileNo();

    String getAdmissionNo();

    Long getWardId();

    String getWard();

    Long getRooId();

    String getRoom();

    Long getBedId();

    String getBed();

    String getDietStatus();

    Long getDietTypeId();

    String getDietTypeName();

    String getSpecialInstruction();

    LocalDateTime getAdmissionDateTime();

    String getDoctorName();
}
