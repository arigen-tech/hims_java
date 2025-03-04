package com.hims.request;

import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Patient;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PatientRegistrationReq {
     PatientRequest patient;
     OpdPatientDetailRequest opdPatientDetail;
     VisitRequest visit;
}
