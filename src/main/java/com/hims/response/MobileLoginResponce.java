package com.hims.response;

import lombok.Data;

import java.util.List;

@Data
public class MobileLoginResponce {
    String message;
    String mobileNo;
    String sessionId;
    List<PatientIdResponse> patientIdResponseList;
}
