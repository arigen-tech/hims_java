package com.hims.request;

import com.hims.entity.Visit;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PatientFollowUpReq {
    private boolean appointmentFlag;
    private PatientRegistrationReq patientDetails;
}
