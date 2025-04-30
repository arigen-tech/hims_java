package com.hims.response;

import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Patient;
import com.hims.entity.Visit;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PatientRegFollowUpResp {
    private Patient patient;
    private OpdPatientDetail opdPatientDetail;
    private Visit visit;
}
