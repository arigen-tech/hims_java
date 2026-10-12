package com.hims.controller;


import com.hims.service.SMSService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "SMS", description = "SMS related operations")
@RequiredArgsConstructor
@RequestMapping("/sms")
public class SMSController {

    private  final SMSService smsService;


    @PostMapping("/send-cancellation-otp")
    public ResponseEntity<?> sendAppointmentCancellationInOtp(String mobileNumber){

        return  ResponseEntity.ok(smsService.sendAppointmentCancellationInOtp(mobileNumber));
    }

    @PostMapping("verify-otp")
    public ResponseEntity<?> verifyOtp(String sessionId,String otp){

        return  ResponseEntity.ok(smsService.verifyOtp(sessionId, otp));
    }
}
