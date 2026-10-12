package com.hims.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class OpdPatientDetailCreateRequest {

    // ======================== Mapping IDs ======================
    private Long patientId;
    private Long visitId;
    private Long departmentId;
    private Long hospitalId;
    private Long doctorId;
    private Long opdPatientDetailId;
    private Long topicId;
    private List<OpdPsychiatricDetailsRequest> psychiatricDetailsRequests;


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
    @Size(max = 12)
    private String rr;
    private String bmi;
    @Size(max = 120)
    private String spo2;
    @Size(max = 3)
    private String bpSystolic;
    @Size(max = 3)
    private String bpDiastolic;
    @Size(max = 1)
    private String mlcFlag;

    // ======================== Clinical History =================
    private String pastMedicalHistory;
    private String familyHistory;
    private String presentComplaints;
    private String patientSignsSymptoms;
    private String clinicalExamination;

    // ======================== Diagnosis ========================
    private String workingDiagnosis;
    private List<IcdDiagnosis> icdDiagnosis;

    // ======================== Investigation ====================
    @Size(max = 1)
    private String labFlag;
    @Size(max = 1)
    private String radioFlag;
    private List<Investigation> investigation;


    // ============================== Treatment ======================
    private List<Treatment> treatment;
    private String treatmentAdvice;


    // ============================== final medicine advice =============================
    private String doctorRemarks;

    // ============================== Procedure Care =============================
    private List<ProcedureCare> procedureCare;


    // ========================= Admission Advice =====================================
    private String admissionFlag;
    private Instant admissionAdvisedDate;
    private String admissionRemarks;
    private Long admissionCareLevel;
    private Long admissionWardCategory;
    private Long admissionWard;
    private String admissionPriority;

    //  =========================== referral ==============================
    private String referralFlag;
    private String referTo;
    private String referredHospitalName;
    private String referralRemarks;
    private Instant referralDate;

    // =================== follow up =========

    private String followUpFlag;
    private Instant followUpDate;
    private Long followUpDays;

    private OpdOpthDetailsRequest ophthalmologyExaminationDetails;
    private OpdObgDetailsRequest opdObgDetailsRequest;
    private OpdEntDetailsRequest entExaminationDetails;
    private PregnancyDetails pregnancyDetails;
    private SurgeryAdviceRequestDTO surgeryAdvice;
    private DentalDetailsRequest dentalDetails;




    // ======================== Inner Class ======================

    @Getter
    @Setter
    public static class IcdDiagnosis{
        private Long icdId;
        private String icdDiagnosisName;
    }

    @Getter
    @Setter
    public static class Investigation {
        private Long investigationId;
        private String investigationName;
        private LocalDate investigationDate;
        private String CategoryCode;
    }

    @Getter
    @Setter
    public static class Treatment{
        private Long itemId;
        private String dosage;
        private String frequency;
        private Integer days;
        private BigDecimal total;
        private String instraction;
        private Integer flag;
    }

    @Getter
    @Setter
    public static class PregnancyDetails{
        private Boolean isPregnant;
        private LocalDate lmpDate;
        private LocalDate edd;
        private LocalDate currentEdd;
        private String gestationPeriod;

    }

//    @Getter
//    @Setter
//    public static class ProcedureCare{
//        private Long procedureId;
//        private String procedureName;
//        private Long frequencyId;
//        private Long noOfDays;
//        private String remarks;
//        private Long patientId;
//        private Long visitId;
//        private Long departmentId;
//        private Long hospitalId;
//        private Long doctorId;
//        private String diagnosis;
//    }

    @Getter
    @Setter
    public static class ProcedureCare {
        private Long procedureId;
        private String procedureName;
        private Long frequencyId;
        private Long noOfDays;
        private String remarks;
        private Long patientId;
        private Long visitId;
        private Long departmentId;
        private Long hospitalId;
        private Long doctorId;
        private String diagnosis;
    }


}
