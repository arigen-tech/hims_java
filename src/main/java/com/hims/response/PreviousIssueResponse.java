package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor

public class PreviousIssueResponse {
    private LocalDate issueDate;
    private String indentNo;
    private BigDecimal qtyIssued;
    private String batchNo;
    private String issueNo;
    private LocalDate expiryDate;

    // Constructor with 5 parameters (matching the query)
    public PreviousIssueResponse(LocalDate issueDate, String indentNo, BigDecimal qtyIssued, String batchNo, String issueNo) {
        this.issueDate = issueDate;
        this.indentNo = indentNo;
        this.qtyIssued = qtyIssued;
        this.batchNo = batchNo;
        this.issueNo = issueNo;
    }
}