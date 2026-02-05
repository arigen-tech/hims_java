package com.hims.response;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class IndentTrackingListReportResponse {

    private Long currentDeptId;
    private Long departmentId;
    private String deptName;
    private Long toDepartmentId;
    private String toDepartmentName;
    private Long indentMId;
    private LocalDate indentDate;
    private String indentNo;
    private LocalDate approvedDate;
    private LocalDate issueDate;
    private Long statusId;
    private String statusName;
    private List<IndentTResponseForIndentTracking> indentTResponses;


}
