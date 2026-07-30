package com.hims.projection;
import java.time.LocalDateTime;

public interface DischargeSummaryProjection {

    Long getDischargeSummaryId();

    Long getInpatientId();

    LocalDateTime getDischargeDate();

    String getPrimaryDiagnosis();

    String getSecondaryDiagnosis();

    String getPresentingComplaints();

    String getHistoryOfIllness();

    String getPastHistory();

    String getExaminationFindings();

    String getProcedureDetails();

    String getHospitalCourse();

    Long getConditionId();

    String getConditionName();

    Long getDischargeReasonId();

    String getDischargeReasonName();

    String getDischargedTo();

    String getReferredHospitalName();

    String getDischargeAdvice();

    String getFollowUpAdvice();

    String getStatus();

    Long getBillStatusId();

    String getBillStatus();

    Long getPaymentStatusId();

    String getPaymentStatus();
}