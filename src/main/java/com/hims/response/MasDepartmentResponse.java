package com.hims.response;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
public class MasDepartmentResponse {
    private Long id;
    private String departmentCode;
    private String departmentName;
    private String status;
    private String lastChgBy;
    private Instant lastChgDate;
    private String lastChgTime;
    private Long departmentTypeId;
    private String departmentTypeName;
    private Long hospitalId;
    private String hospitalName;
    private String departmentNo;
}