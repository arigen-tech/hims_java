package com.hims.controller;

import com.hims.request.AppointmentReq;
import com.hims.request.LabRegRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.service.AppSetupServices;
import com.hims.service.LabRegistrationServices;
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
@Tag(name = "LabRegistration", description = "This controller is used for any LabRegistration & Investigation & Package booking Related task.")
@RequestMapping("/lab")
@Slf4j
public class LabRegistrationController {

    @Autowired
    LabRegistrationServices labRegistrationServices;
    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<AppsetupResponse>> appSetupResponse(@RequestBody LabRegRequest request) {
        return new ResponseEntity<>(labRegistrationServices.labReg(request), HttpStatus.OK);

    }
}
