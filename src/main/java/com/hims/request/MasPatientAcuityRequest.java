package com.hims.request;

import lombok.Data;

@Data
public class MasPatientAcuityRequest {
    private String acuityCode;
    private String acuityName;
    private String description;
}
