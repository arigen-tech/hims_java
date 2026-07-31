package com.hims.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class IpDischargeSummarySaveRequest {

    @NotNull(message = "Inpatient Id is required")
    private Long inpatientId;
    @NotNull(message = "Discharge date is required")
    private LocalDateTime dischargeDate;
    private String primaryDiagnosis;
    private String secondaryDiagnosis;
    private String presentingComplaints;
    private String historyOfIllness;
    private String pastHistory;
    private String examinationFindings;
    private String procedureDetails;
    private String hospitalCourse;
    private Long conditionId;
    private Long dischargeReasonId;
    private String dischargedTo;
    private String referredHospitalName;
    private String dischargeAdvice;
    private String followUpAdvice;
    private String status;
    @Valid
    private List<IpDischargeMedicationRequest> medications;
    private List<Long> deleteMedicationIds;
}