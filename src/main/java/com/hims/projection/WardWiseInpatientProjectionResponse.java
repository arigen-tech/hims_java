package com.hims.projection;

import java.time.LocalDateTime;

public interface WardWiseInpatientProjectionResponse {
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

        Long getRoomId();

        String getRoom();

        Long getBedId();

        String getBed();

        LocalDateTime getAdmissionDateTime();

}
