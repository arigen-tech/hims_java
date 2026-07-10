package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class StoreInternalIndentMResponse {

    private Long indentMId;
    private String indentNo;
    private LocalDateTime indentDate;

    private Long fromDeptId;
    private String fromDeptName;

    private Long toDeptId;
    private String toDeptName;

    private String status;

    private String createdBy;


    private String approvedBy;
    private LocalDateTime approvedDate;

    private String storeApprovedBy;
    private LocalDateTime storeApprovedDate;

    private String issuedBy;
    private LocalDateTime issuedDate;

    private String receivedBy;
    private LocalDateTime receivedDate;

//    private BigDecimal totalReceivedQty;
//    private BigDecimal totalIssuedQty;



//    private String issueNo;

    private String remark;
    private String indentType;
}
