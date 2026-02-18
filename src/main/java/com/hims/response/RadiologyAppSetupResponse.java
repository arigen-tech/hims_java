package com.hims.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RadiologyAppSetupResponse {
    Long patientId;
    private String msg;
    private String billinghdId;
}
