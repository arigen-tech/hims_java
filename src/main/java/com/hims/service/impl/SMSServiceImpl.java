package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.constants.SMSTemplate;
import com.hims.response.ApiResponse;
import com.hims.service.SMSService;
import com.hims.utils.ResponseUtils;
import com.hims.utils.SMSUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class SMSServiceImpl implements SMSService {

    private  final SMSUtility smsUtility;


    @Override
    public ApiResponse<String> sendAppointmentCancellationInOtp(String mobileNumber) {
        try {

            log.info("getLoginOtp method started for mobile number {}",mobileNumber);
            String sessionId = smsUtility.sendOtp(mobileNumber, SMSTemplate.APPOINTMENT_CANCEL_OTP);
            log.info("getLoginOtp method ended for mobile number {}",mobileNumber);
            return ResponseUtils.createSuccessResponse(sessionId, new TypeReference<>() {});

        }catch (Exception e){
            log.error("getLoginOtp method error :: ",e);
           return ResponseUtils.createFailureResponse(null,
                   new TypeReference<>() {},
                   AppConstants.INTERNAL_SERVER_ERR_MSG,
                   HttpStatus.INTERNAL_SERVER_ERROR.value()
           );
        }
    }

    @Override
    public ApiResponse<Boolean> verifyOtp(String sessionId,String otp) {
        try {

            log.info("verifyOtp method started ...");
            boolean isVerified= smsUtility.verifyOtp(sessionId, otp);
            log.info("verifyOtp method ended...");
            return ResponseUtils.createSuccessResponse(isVerified, new TypeReference<>() {});

        }catch (Exception e){
            log.error("verifyOtp method error :: ",e);
            return ResponseUtils.createFailureResponse(null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
}
