package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for patient prescription header with details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionHeaderResponse {
    private Long prescriptionHdId;
    private Long patientId;
    private String doctorName;
    private Long departmentId;
    private LocalDateTime prescriptionDate;
    private String status;
    private String billingStatus;
    private String createdBy;
    private String issuedBy;
    private LocalDateTime issuedDate;
    private BigDecimal totalCost;
    private BigDecimal totalGst;
    private BigDecimal totalDiscount;
    private BigDecimal netAmount;
    private List<PrescriptionDetailResponse> prescriptionDetails;
}

