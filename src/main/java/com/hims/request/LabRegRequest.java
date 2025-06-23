package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LabRegRequest {
    List<LabInvestigationReq> labInvestigationReq;
    List<LabPackegReq> labPackegReqs;
    private Long patientId;

    private Long totalAmount;
    private Long discountAmount;

}
