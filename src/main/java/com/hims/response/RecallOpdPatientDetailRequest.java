package com.hims.response;

import com.hims.request.*;
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
    private String bpSystolic;
    private String bpDiastolic;
    private String pulse;
    private String bmi;
    private String rr;
    private String spo2;
    private String patientSignsSymptoms;
    private String clinicalExamination;
    private String pastMedicalHistory;
    private String familyHistory;
    private String mlcFlag;
    private String workingDiagnosis;
    private List<IcdDiagnosis> icdDiagnosisList;
    private List<TreatmentRequest> treatments;
    private String treatmentAdvice;
    private List<InvestigationRequest> investigations;
    private String labFlag;
    private String radioFlag;
    private List<ProcedureCare> procedureCare;
    private String doctorRemarks;
    private List<Long> removeIcdIds;
    private List<Long> removeProcedureCareIds;
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
    private String referTo;
    private String referredHospitalName;
    private String currentPriorityNo;
    private String followUpFlag;
    private Instant followUpDate;
    private Long followUpDays;
    private PregnancyDetails pregnancyDetails;
    private Long opdPatientDetailId;
    private Long patientId;
    private Long visitId;
    private Long departmentId;
    private Long hospitalId;
    private Long doctorId;
    private Long topicId;
    private List<OpdPsychiatricDetailsRequest> psychiatricDetailsRequests;
    private OpdObgDetailsRequest opdObgDetailsRequest;
    private OpdEntDetailsRequest entExaminationDetails;
    private OpdOpthDetailsRequest ophthalmologyExaminationDetails;
    private SurgeryAdviceRequestDTO surgeryAdvice;

    @Data
    public static class TreatmentRequest {
        private Long prescriptionHdId;
        private Long prescriptionDtId;
        private Long itemId;
        private String itemName;
        private String dispUnit;
        private String dosage;
        private Long frequencyId;
        private String frequencyName;
        private Integer days;
        private Integer total;
        private String instruction;
        private Long itemClassId;
        private Integer adispQty;
        private Integer flag;
    }

    @Data
    public static class InvestigationRequest {
        private Long id;
        private String investigationName;
        private LocalDate investigationDate;
        private Long investigationId;
        private Integer flag;
    }

    @Getter
    @Setter
    public static class IcdDiagnosis {
        private Long id;
        private Long icdId;
        private String icdDiagnosisName;
        private Boolean communicableDisease;
        private Boolean infectiousDisease;
    }

    @Getter
    @Setter
    public static class ProcedureCare {
        private Long id;
        private Long procedureId;
        private String procedureName;
        private Long frequencyId;
        private Long noOfDays;
        private String remarks;
    }

    @Getter
    @Setter
    public static class PregnancyDetails {
        private Boolean isPregnant;
        private LocalDate lmpDate;
        private LocalDate edd;
        private LocalDate currentEdd;
        private String gestationPeriod;
    }
}
