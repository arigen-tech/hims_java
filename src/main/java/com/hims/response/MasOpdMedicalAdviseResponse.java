package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class MasOpdMedicalAdviseResponse {
    private Long medicalAdviseId;
    private String medicalAdviseName;
    private String status;
    private Long departmentId;
    private String departmentName;

    private LocalDateTime lastUpdateDate;

}
