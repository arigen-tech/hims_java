package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasDepartmentTypeRequest {
    private String departmentTypeCode;
    private String departmentTypeName;
    private String status;
    private String lastChgBy;
}
