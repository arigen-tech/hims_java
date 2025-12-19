package com.hims.response;

import lombok.Data;

@Data
public class MobileLoginResponce {

    String message;
    String mobileNo;
    String sessionId;
    Long patientId;
}
