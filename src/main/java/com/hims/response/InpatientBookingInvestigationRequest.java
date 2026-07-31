package com.hims.response;

import com.hims.request.LabRadioInvestigationRequest;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InpatientBookingInvestigationRequest {
    private Long patientId;
    private Long inpatientId;
    private Long investigationId;
    private List<LabRadioInvestigationRequest> investigationReq;
    private String sample;
    private String container;
    private String resultUnit;
}
