package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentByDepartmentTypeCode {
    private Long departmentTypeId;
    private String departmentTypeName;
}
