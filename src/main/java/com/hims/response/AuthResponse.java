package com.hims.response;

import lombok.Data;

import java.util.List;

@Data
public class AuthResponse {
    String token;
    String message;
    List<PatientIdResponse> patientIdResponseList;

}
