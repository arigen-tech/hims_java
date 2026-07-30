package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class DischargeSummaryResponse {
    private Long dischargeSummaryId;
    private Long inpatientId;
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
    private String conditionName;
    private Long dischargeReasonId;
    private String dischargeReasonName;
    private String dischargedTo;
    private String referredHospitalName;
    private String dischargeAdvice;
    private String followUpAdvice;
    private String status;
    private Long billStatusId;
    private String billStatus;
    private Long paymentStatusId;
    private String paymentStatus;

    private List<IpDischargeMedicationResponse> medications;
}
