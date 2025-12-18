package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpRequest {
    String otp;
    String sessionId;
}
