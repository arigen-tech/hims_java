package com.hims.response;


import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class RecallOpdPatientDetailRequest {

    private String height;
    private String weight;
    private String temperature;
    private String systolicBP;
    private String diastolicBP;
    private String pulse;
    private String bmi;
    private String rr;
    private String spo2;

    private String patientSymptoms;
    private String clinicalExamination;
    private String pastHistory;
    private String familyHistory;

    private String mlcCase;
    private String workingDiagnosis;
    private String icdDiagnosis;   // Comma-separated values: "J06.9,Mh.01,srg,sdsf"

    private List<IcdDiagnosis> icdObj;
    private List<TreatmentRequest> treatments;
    private String treatmentAdvice;
    private List<InvestigationRequest> investigations;
    private String labFlag;
    private String radioFlag;
//    private List<ProcedureCare> procedureCare;
    private String doctorRemarks;
    private List<Long>removeIcdIds;
    private List<Long>removeprocedureCareIds;
    private List<Long> removedTreatmentIds;
    private List<Integer> removedInvestigationIds;


    private String admissionFlag;
    private Instant admissionAdvisedDate;
    private String admissionRemarks;
    private Long admissionCareLevel;
    private Long admissionWardCategory;
    private Long admissionWard;
    private String admissionPriority;
    private String referralFlag;
    private String referralRemarks;
    private Instant referralDate;
    private String followUpFlag;
    private Instant followUpDate;
    private Long followUpDays;


    private Long opdPatientId;
    private Long patientId;
    private Long visitId;
    private Long departmentId;
    private Long hospitalId;
    private Long doctorId;

    // -----------------------------
    // Treatment DTO
    // -----------------------------
    @Data
    public static class TreatmentRequest {
        private Long treatmentId;
        private Long drugId;
        private String drugName;
        private String dispUnit;
        private String dosage;
        private String frequency;
        private Integer days;
        private Integer total;
        private String instruction;
    }

    // -----------------------------
    // Investigation DTO
    // -----------------------------
    @Data
    public static class InvestigationRequest {
        private Long id;
        private String name;
        private LocalDate date;
        private Long investigationId;
        private String templateSource;
    }

    @Getter
    @Setter
    public static class IcdDiagnosis{
        private Long id;
        private Long icdId;
        private String icdDiagName;
    }

    // ============================== Procedure Care DTO=============================

    @Getter
    @Setter
    public static class ProcedureCare{
        private Long id;
        private Long procedureId;
        private String procedureName;
        private Long frequencyId;
        private Long noOfDays;
        private String remarks;
    }
}
