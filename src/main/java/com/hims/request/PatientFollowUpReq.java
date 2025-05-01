package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientFollowUpReq {
    private boolean appointmentFlag;
    private PatientRegistrationReq patientDetails;
}
