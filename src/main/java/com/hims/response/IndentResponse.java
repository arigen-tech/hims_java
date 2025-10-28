package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class IndentResponse {

    private Long indentMId;


    private String indentNo;


    private LocalDateTime indentDate;


    private Long fromDeptId;

    private Long toDeptId;


    private BigDecimal totalCost;

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


    private Long storeIssueMId;

    private String status;

    List<IndentTResponse> indentTResponseList;
}
