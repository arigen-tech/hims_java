package com.hims.response;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OpdPatientRecallResponce {

    private String patientName;
    private String mobileNo;
    private String gender;
    private String relation;
    private LocalDate dob;
    private String age;
    private Long deptId;
    private String deptName;
    private Long docterId;
    private String docterName;
    private Long visitId;
    private Long patientId;
    private Long hospitalId;

    //============================== For OpdPatientDetails ==================================

    private Long opdPatientId;
    private Instant opdDate;

                //=============clinic===========
    private String pastMedicalHistory;
    private String familyHistory;
    private String patientSignsSymptoms;
    private String clinicalExamination;
               //=============Vital===============
    private String height;
    private String idealWeight;
    private String weight;
    private String pulse;
    private String temperature;
    private String rr;
    private String bmi;
    private String spo2;
    private String bpSystolic;
    private String bpDiastolic;
    private String mlcFlag;

    //=============diagnosis============
    private String workingDiag;
    private List<IcdDiagnosis> icdDiag;


    //==================== Investigation ==================
    private String labFlag;
    private String radioFlag;
    private List<LabOrderHd> labOrderHds;
    private List<RadOrderHd> radOrderHds;



    //======================== Treatment ====================
    private NewDPatientPrescriptionHd patientPrescriptionHd;
    private List<NewDPatientPrescriptionHd> patientPrescriptionHds;
    private List<NewDPatientPrescriptionDt> patientPrescriptionDts;


    // ============================== final medicine advice =============================
    private String doctorRemarks;

    // ========================= Admission Advice =====================================
    private String admissionFlag;
    private Instant admissionAdvisedDate;
    private String admissionRemarks;
    private Long admissionCareLevel;
    private Long admissionWardCategory;
    private Long admissionWard;
    private String admissionCareLevelName;
    private String admissionWardCategoryName;
    private String admissionWardName;
    private String admissionPriority;


    //  =========================== referral ==============================
    private String referralFlag;
    private String referralRemarks;
    private Instant referralDate;
    private String referTo;
    private String referredHospitalName;

    // =================== follow up =========

    private String followUpFlag;
    private Instant followUpDate;
    private Long followUpDays;

    private PregnancyDetails pregnancyDetails;

    //=========================== DgOrderHd =====================
    @Getter
    @Setter
    public static class LabOrderHd {
        private Long OrderHdId;
        private LocalDate orderDate;
        private String orderNo;
        private String orderStatus;
        private String collectionStatus;
        private String paymentStatus;
        private LocalDate appointmentDate ;
        private List<LabOrderDt> labOrderDts;

    }
    @Getter
    @Setter
    public static class RadOrderHd {
        private Integer OrderHdId;
        private LocalDate orderDate;
        private String paymentStatus;
        private LocalDate appointmentDate ;
        private List<RadOrderDt> radOrderDts;

    }

    //=========================== DgOrderDt =====================
    @Getter
    @Setter
    public static class LabOrderDt {
        private Integer OrderDtId;
        private int orderQty;
        private String orderStatus;
        private LocalDate appointmentDate;
        private Long investigationId;
        private String billingStatus;
        private Long packageId;
        private Integer billingHd;
        private String investigationName;
    }
    @Getter
    @Setter
    public static class RadOrderDt {
        private Integer OrderDtId;
        private String studyStatus;
        private LocalDate appointmentDate;
        private Long investigationId;
        private String billingStatus;
        private Long packageId;
        private Integer billingHd;
        private String investigationName;
    }

    //========================== PatientPrescriptionHd ===========================
    @Getter
    @Setter
    public static class NewDPatientPrescriptionHd {
        private Long prescriptionHdId;
        private String status;
        private LocalDateTime prescriptionDate;
    }

    private String treatmentAdvice;
    //========================== PatientPrescriptionDt ===========================
    @Getter
    @Setter
    public static class NewDPatientPrescriptionDt {
        private Long prescriptionDtId;
        private Long prescriptionHdId;
        private String status;
        private String dosage;
        private String frequency;
        private String frequencyId;
        private String depUnit;
        private Integer days;
        private BigDecimal total;
        private String instraction;
        private Long itemId;
        private String itemName;
        private Long stocks;
        private String dispUnit;
        private Integer itemClassId;
        private BigDecimal adispQty;
    }

    @Getter
    @Setter
    public static class IcdDiagnosis{
        private Long id;
        private Long icdId;
        private String icdDiagName;
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
