package com.hims.request;

import com.hims.entity.MasCareLevel;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasWardCategory;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class OpdPatientDetailFinalRequest {

    // ======================== Vital Details ====================
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

    // ======================== Diagnosis ========================
    private String workingDiag;
    private List<IcdDiagnosis> icdDiag;

    // ======================== Clinical History =================
    private String pastMedicalHistory;
    private String familyHistory;
    private String presentComplaints;
    private String patientSignsSymptoms;
    private String clinicalExamination;

    // ======================== Investigation ====================
    @Size(max = 1)
    private String labFlag;

    @Size(max = 1)
    private String radioFlag;

    private List<Investigation> investigation;


    // ============================== Treatment ======================
    private List<Treatment> treatment;

    private String treatmentAdvice;


    // ============================== Procedure Care =============================
//    private List<ProcedureCare> procedureCare;


    // ============================== final medicine advice =============================
    private String doctorRemarks;

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


    // =================== follow up =========

    private String followUpFlag;
    private Instant followUpDate;
    private Long followUpDays;

    // ======================== Mapping IDs ======================
    private Long patientId;
    private Long visitId;
    private Long departmentId;
    private Long hospitalId;
    private Long doctorId;
    private Long opdPatientDetailId;

    // ======================== Inner Class ======================

    @Getter
    @Setter
    public static class IcdDiagnosis{
        private Long icdId;
        private String icdDiagName;
    }

    @Getter
    @Setter
    public static class Investigation {
        private Long id;
        private String investigationName;
        private LocalDate investigationDate;
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

    }

    @Getter
    @Setter
    public static class ProcedureCare{
        private Long procedureId;
        private String procedureName;
        private Long frequencyId;
        private Long noOfDays;
        private String remarks;
    }
}
