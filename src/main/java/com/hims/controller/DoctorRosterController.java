package com.hims.controller;

import com.hims.entity.DoctorRoaster;
import com.hims.request.AppointmentReq;
import com.hims.request.DoctorRosterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppSetupDTO;
import com.hims.response.AppsetupResponse;
import com.hims.response.DoctorRosterDTO;
import com.hims.service.AppSetupServices;
import com.hims.service.DoctorRosterServices;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@Tag(name = "Doctor", description = "This controller is used for any Doctor Roster Related task.")
@RequestMapping("/doctor")
@Slf4j
public class DoctorRosterController {


    @Autowired
    DoctorRosterServices doctorRosterServices;
    @PostMapping("/roster")
    public ResponseEntity<ApiResponse<AppsetupResponse>> doctorRosterResponse(@RequestBody DoctorRosterRequest request) {
        return new ResponseEntity<>(doctorRosterServices.doctorRoster(request), HttpStatus.OK);
    }



    @GetMapping("/rosterfind")
    public ResponseEntity<List<DoctorRosterDTO>> findDoctorRoster(
            @RequestParam Long deptId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam LocalDate rosterDate) {

        List<DoctorRosterDTO> doctorRosterList = doctorRosterServices.getDoctorRoster(deptId, doctorId, rosterDate);

        return ResponseEntity.ok(doctorRosterList);
    }
}