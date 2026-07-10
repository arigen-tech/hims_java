package com.hims.request;

import com.hims.entity.Visit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientFollowUpReq {
    private boolean appointmentFlag;
    private PatientRegistrationReq patientDetails;
}
