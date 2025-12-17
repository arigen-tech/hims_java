package com.hims.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor  // ADD THIS
@AllArgsConstructor
public class OpdPatientDetailRequest {

    @Size(max = 40)
    private String height;

    @Size(max = 40)
    private String idealWeight;

    @Size(max = 40)
    private String weight;

    @Size(max = 40)
    private String pulse;

    @Size(max = 48)
    private String temperature;

    private Instant opdDate;

    @Size(max = 12)
    private String rr;

    private String bmi;

    @Size(max = 120)
    private String spo2;

    private Double varation;

    @Size(max = 3)
    private String bpSystolic;

    @Size(max = 3)
    private String bpDiastolic;

    private String icdDiag;
    private String workingDiag;
    private String followUpFlag;
    private Long followUpDays;
    private String pastMedicalHistory;
    private String presentComplaints;
    private String familyHistory;
    private String treatmentAdvice;
    private String sosFlag;

    @Size(max = 500)
    private String recmmdMedAdvice;

    @Size(max = 1)
    private String medicineFlag;

    @Size(max = 1)
    private String labFlag;

    @Size(max = 1)
    private String radioFlag;

    @Size(max = 1)
    private String referralFlag;

    @Size(max = 1)
    private String mlcFlag;

    @Size(max = 100)
    private String policeStation;

    @Size(max = 100)
    private String policeName;

    private Long patientId; // Only ID for Patient

    private Long visitId; // Only ID for Visit

    @NotNull
    private Long departmentId; // Only ID for Department

    @NotNull
    private Long hospitalId; // Only ID for Hospital

    private Long doctorId; // Optional ID for Doctor

    @Size(max = 200)
    private String lastChgBy;
}
