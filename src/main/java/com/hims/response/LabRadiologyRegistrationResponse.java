package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class LabRadiologyRegistrationResponse {
    Long patientId;
    private String msg;
    private Long billinghdId;
    private List<BillingDto> billingHdIds;

    @Getter
    @Setter
    public static class BillingDto {
        String billingHdId;
        String investigationId;
        Integer InvestigationAmount;
    }
}
