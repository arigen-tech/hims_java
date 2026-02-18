package com.hims.request;

import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Patient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor  // ADD THIS
@AllArgsConstructor
public class PatientRegistrationReq {
     PatientRequest patient;
     OpdPatientDetailRequest opdPatientDetail;
     List<VisitRequest> visits;
}
