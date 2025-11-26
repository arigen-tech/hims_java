package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StoreInternalIndentResponse {
    private Long indentMId;
    private String indentNo;
    private LocalDateTime indentDate;

    private Long fromDeptId;
    private String fromDeptName;

    private Long toDeptId;
    private String toDeptName;

    private BigDecimal totalCost;
    private String status;

    private String createdBy;
    private LocalDateTime createdDate;

    private String approvedBy;
    private LocalDateTime approvedDate;

    private String storeApprovedBy;
    private LocalDateTime storeApprovedDate;

    private String issuedBy;
    private LocalDateTime issuedDate;

    private String receivedBy;
    private LocalDateTime receivedDate;


    private String issueNo;

    private String remark;

    private List<StoreInternalIndentDetailResponse> items;
}
