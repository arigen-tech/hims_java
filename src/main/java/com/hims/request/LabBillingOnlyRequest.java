package com.hims.request;

import lombok.Data;

import java.util.List;

@Data

public class LabBillingOnlyRequest {

    private Long patientId;
    private Long orderhdid; // existing orderhdid from Pending Billing

    private List<LabInvestigationReq> labInvestigationReq;  // you already have this class

}
