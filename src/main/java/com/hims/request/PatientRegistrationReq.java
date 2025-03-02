package com.hims.request;

import com.hims.dto.OpdPatientDetailDto;
import com.hims.dto.PatientDto;
import lombok.Getter;


@Getter
public class PatientRegistrationReq {
     PatientDto patient;
     OpdPatientDetailDto opdPatientDetail;
}
