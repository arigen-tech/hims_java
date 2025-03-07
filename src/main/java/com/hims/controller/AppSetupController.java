package com.hims.controller;
import com.hims.entity.AppSetup;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasOpdSession;
import com.hims.entity.User;
import com.hims.request.AppointmentReq;
import com.hims.response.ApiResponse;
import com.hims.response.AppSetupDTO;
import com.hims.response.AppsetupResponse;
import com.hims.service.AppSetupServices;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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


//    @GetMapping("/getappsetup/{departmentId}/{doctorId}/{sessionId}/")
//    public ResponseEntity<ApiResponse<AppsetupResponse>> getappsetupData(@PathVariable("departmentId") Long departmentId,@PathVariable(value = "doctorId") Long doctorId
//            ,@PathVariable(value = "sessionId") Long sessionId) {
//        return new ResponseEntity<>(appSetupServices.getappsetupData(departmentId,doctorId,sessionId), HttpStatus.OK);
//    }

    @GetMapping("/find")
    public ResponseEntity<ApiResponse<AppSetupDTO>> findAppSetups(
            @RequestParam Long deptId,
            @RequestParam Long doctorId,
            @RequestParam Long sessionId) {

        return ResponseEntity.ok(appSetupServices.getAppSetupDTO(deptId, doctorId, sessionId));
    }



}
