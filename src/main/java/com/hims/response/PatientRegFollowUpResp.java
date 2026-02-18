package com.hims.response;

import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Patient;
import com.hims.entity.Visit;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PatientRegFollowUpResp {
    private Patient patient;
    private OpdPatientDetail opdPatientDetail;
    private List<Visit> visits;
    private OPDBillingPatientResponse opdBillingPatientResponse;
}
