package com.hims.request;

import lombok.Data;

@Data
public class MasOpdMedicalAdviseRequest {
    private String medicalAdviceName;
    private Long departmentId;
}
