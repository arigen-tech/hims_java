package com.hims.projection;

import java.time.LocalDateTime;

public interface ActiveAdmissionProjectionResponse {


        Long getInpatientId();

        String getPatientName();

        String getUhid();

        String getAge();

        Long getGenderId();

        String getGender();

        String getMobileNo();
        String getEmergencyMobileNo();

        String getAdmissionNo();

        Long getWardId();

        String getWard();

        Long getRooId();

        String getRoom();

        Long getBedId();

        String getBed();

        LocalDateTime getAdmissionDateTime();

        LocalDateTime getDischargeDate();

        Long getCategoryId();

        String getCategoryName();

        String getDoctorName();

        String getLos();

        String getStatus();

        String getBillingType();
    }

