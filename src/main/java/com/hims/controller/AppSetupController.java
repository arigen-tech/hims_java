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
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "AppSetup", description = "This controller is used for any AppSetup Related task.")
@RequestMapping("/app")
@Slf4j
public class AppSetupController {

    @Autowired
    AppSetupServices appSetupServices;
    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<AppsetupResponse>> appSetupResponse(@RequestBody AppointmentReq request) {
        return new ResponseEntity<>(appSetupServices.appSetup(request), HttpStatus.OK);

    }


    @GetMapping("/getappsetup/{departmentId}/{doctorId}/{sessionId}/")
    public ResponseEntity<ApiResponse<AppsetupResponse>> getappsetupData(@PathVariable("departmentId") Long departmentId,@PathVariable(value = "doctorId") Long doctorId
            ,@PathVariable(value = "sessionId") Long sessionId) {
        return new ResponseEntity<>(appSetupServices.getappsetupData(departmentId,doctorId,sessionId), HttpStatus.OK);
    }
}
