package com.hims.request;

import lombok.Data;

import java.util.List;

@Data
public class LabRadioRegistrationRequest {
    PatientRequest patient;
    List<LabRadioInvestigationRequest> investigationReq;
}
