package com.hims.projection;
public interface IpDischargeMedicationProjection {

    Long getMedicationId();

    String getMedicineName();

    String getDosage();

    String getFrequency();

    Integer getTotalDoses();

    String getRoute();

    String getInstruction();
}