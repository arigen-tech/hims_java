package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for prescription detail line items
 * Contains medication-specific information from prescriptions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDetailResponse {
    private Long prescriptionDtId;
    private Long prescriptionHdId;
    private Long drugId;
    private String drugName;
    private String dosage;
    private String frequency;
    private Integer days;
    private BigDecimal total;
    private BigDecimal issuedQty;
    private String route;
    private String instruction;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal gstRate;
    private BigDecimal lineCost;
    private String status;
    private String batchNo;
    private LocalDate expiryDate;
    private String doctorName;
    private String departmentName;
    private LocalDateTime prescribedDate;
    private String dispUnit;
}

