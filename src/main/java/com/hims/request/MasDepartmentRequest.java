package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasDepartmentRequest {
    private String departmentCode;
    private String departmentName;
    private Long departmentTypeId;
    private Long hospitalId;
    private String departmentNo;
    private String status;
    private String lastChgBy;
    private Long wardCategoryId;
}