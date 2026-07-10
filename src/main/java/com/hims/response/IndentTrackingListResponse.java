package com.hims.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IndentTrackingListResponse {

    private Long currentDeptId;
    private Long departmentId;
    private String deptName;
    private Long toDepartmentId;
    private String toDepartmentName;
    private Long indentMId;
    private LocalDateTime indentDate;
    private String indentNo;
    private LocalDateTime approvedDate;
    private LocalDateTime issueDate;
    private Long statusId;
    private String statusName;
    private String createdBy;
    private String indentType;
    private String isReturn;
}
