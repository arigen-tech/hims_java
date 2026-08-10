package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
public class MarDetailsRequest {
    private Long prescriptionId;
    private Long itemId;
    private Long InpatientId;
    private LocalDateTime dateTime;
    private BigDecimal requestQty;
    private String batchNo;
    private LocalDate expiryDate;
    private String givenBy;
    private String remark;
    private Long departmentId;



}
