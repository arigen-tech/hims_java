package com.hims.controller;
import com.hims.request.AppointmentReq;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.service.AppSetupServices;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "AppointmentController", description = "This controller is used for any Appointment Related task.")
@RequestMapping("/appointment")
@Slf4j
public class AppSetupController {

    @Autowired
    AppSetupServices appSetupServices;
    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<AppsetupResponse>> appSetupResponse(@RequestBody AppointmentReq request) {
        return new ResponseEntity<>(appSetupServices.appointmentSetup(request), HttpStatus.OK);

    }
}
