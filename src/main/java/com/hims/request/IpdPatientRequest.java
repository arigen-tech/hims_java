package com.hims.request;

import jakarta.validation.Valid;
import lombok.Data;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


import org.springframework.format.annotation.DateTimeFormat;


@Getter
@Setter
public class IpdPatientRequest {



    // ==========================
    // Admission Details
    // ==========================

    @NotNull(message = "Patient id is required")
    private Long patientId;

    private Long visitId;

    @NotNull(message = "Admission date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate admissionDate;

    @NotNull(message = "Admission time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime admissionTime;

    private Long admissionTypeId;

    private Long admissionCategoryId;

    private Long admissionSourceId;

    private Long patientConditionId;

    private Long careLevelId;

    private Long wardCategoryId;

    @Size(max = 500, message = "Condition notes should not exceed 500 characters")
    private String conditionNotes;

    private String admissionConsentTaken;
    private String consentTakenBy;
    private String mlcCase;
    private String policeIntimationRequired;
    private String admissionAdvisedFrom;
    private Long dietPreferenceId;



    // ==========================
    // NOK Details
    // ==========================

    @NotBlank(message = "NOK name is required")
    @Size(max = 100, message = "NOK name should not exceed 100 characters")
    private String nokName;

    private Long nokRelationId;

    @Size(max = 20, message = "Contact number should not exceed 20 characters")
    private String contactNo;

    @Size(max = 250, message = "Address line should not exceed 250 characters")
    private String addressLine;

    @Size(max = 50, message = "City should not exceed 50 characters")
    private String city;

    @Size(max = 50, message = "State should not exceed 50 characters")
    private String state;

    @Size(max = 10, message = "Pincode should not exceed 10 characters")
    private String pincode;

    // ==========================
    // Documents
    // ==========================

    @Valid
    private List<IpDocumentRequest> documents;

    // ==========================
    // Ward Details
    // ==========================

    @NotNull(message = "Ward id is required")
    private Long wardId;

    @NotNull(message = "Room id is required")
    private Long roomId;

    @NotNull(message = "Bed id is required")
    private Long bedId;

    // ==========================
    // Inner Class
    // ==========================

    @Getter
    @Setter
    public static class IpDocumentRequest {

        @NotBlank(message = "Document type is required")
        @Size(max = 100, message = "Document type should not exceed 100 characters")
        private String documentType;

        @NotNull(message = "Document file is required")
        private MultipartFile ipDocumentUploads;
    }


    private String patientName;
    private String uhid;


    // Doctor & Diagnosis
    private Long departmentId;
    private Long treatingDoctor;
    private String workingDiagnosis;
    List<PaymentRequest> paymentRequests;

    @Getter
    @Setter
    public static class PaymentRequest {
        // Financial Details
        private Long paymentType;
        private String advanceCollected;
        private BigDecimal advanceAmount;
        private Long paymentMode;
    }
    private BigDecimal estimationCost;

// Financial Details

}