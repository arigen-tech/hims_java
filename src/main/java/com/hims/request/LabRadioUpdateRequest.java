package com.hims.request;

import lombok.Data;

import java.util.List;

@Data
public class LabRadioUpdateRequest {
    PatientRequest patient;
    List<LabRadioInvestigationRequest> investigationReq;
}
