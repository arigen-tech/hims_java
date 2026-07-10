package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class RadRegInvReq {
    PatientRequest patient;
    List<LabInvestigationReq> radInvestigationReq;
}
