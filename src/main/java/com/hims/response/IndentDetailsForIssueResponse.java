package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class IndentDetailsForIssueResponse {

    private Long indentTId;
    private Long itemId;
    private String itemName;
    private String pvmsNo;

    private BigDecimal requestedQty;
    private BigDecimal approvedQty;
//    private BigDecimal issuedQty;
//    private BigDecimal receivedQty;
    private BigDecimal availableStock;
    private String issueStatus;
    private String reason;
    private String unitAuName;
    private Long unitAUid;
    private Long stockId;
    private String batchNo;
    private BigDecimal batchAvailableStock;
    private Long manufacturerId;
    private LocalDate mfgDate;
    private LocalDate expDate;
}
