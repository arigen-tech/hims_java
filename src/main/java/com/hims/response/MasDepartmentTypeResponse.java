package com.hims.response;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class MasDepartmentTypeResponse {
    private Long id;
    private String departmentTypeCode;
    private String departmentTypeName;
    private String status;
    private String lastChgBy;
    private Instant lastChgDate;
}