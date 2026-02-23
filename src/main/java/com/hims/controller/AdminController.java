package com.hims.controller;

import com.hims.request.AppointmentReq;
import com.hims.request.DoctorRosterRequest;
import com.hims.response.*;
import com.hims.service.AdminService;
import com.hims.service.AppSetupServices;
import com.hims.service.DoctorRosterServices;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for administrative operations including appointment setup and doctor roster management
 */

@RestController
@Tag(name = "AdminController", description = "This controller is used for any AppSetup and Doctor Roster Related task.")
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Create or update appointment setup configuration
     */

    @PostMapping("/createAppointmentSetup")
    public ResponseEntity<ApiResponse<AppsetupResponse>> createAppointmentSetup(@RequestBody AppointmentReq request) {
        log.info("POST /adminController/createAppointmentSetup called");
        return new ResponseEntity<>(adminService.createAppointmentSetup(request), HttpStatus.OK);

    }
    /**
     * Retrieve appointment setup details
     */

    @GetMapping("/getAppointmentSetup")
    public ResponseEntity<ApiResponse<AppSetupDTO>> getAppointmentSetup(
            @RequestParam Long deptId,
            @RequestParam Long doctorId,
            @RequestParam Long sessionId) {
        log.info("GET /adminController/getAppointmentSetup called: deptId={}, doctorId={}, sessionId={}", deptId, doctorId, sessionId);
        return ResponseEntity.ok(adminService.getAppointmentSetup(deptId, doctorId, sessionId));
    }

    /**
     * Create or update doctor roster schedule
     */


    @PostMapping("/createDoctorRoster")
    public ResponseEntity<ApiResponse<AppsetupResponse>>  createDoctorRoster(@RequestBody DoctorRosterRequest request) {
        log.info("POST /adminController/createDoctorRoster called");
        return new ResponseEntity<>(adminService. createDoctorRoster(request), HttpStatus.OK);
    }


    /**
     * Retrieve doctor roster by filters
     */

    @GetMapping("/getDoctorRoster")
    public ResponseEntity<ApiResponse<List<DoctorRosterDTO>>> getDoctorRoster(
            @RequestParam Long deptId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam LocalDate rosterDate,
            @RequestParam(required = false) Long sessionId) {
        log.info("GET /adminController/getDoctorRoster called: deptId={}, doctorId={}, rosterDate={}, sessionId={}",
                deptId, doctorId, rosterDate, sessionId);
        ApiResponse<List<DoctorRosterDTO>> doctorRosterList = adminService.getDoctorRoster(deptId, doctorId, rosterDate,sessionId);
        return ResponseEntity.ok(doctorRosterList);

    }

    /**
     * Retrieve doctor roster with weekly schedule
     */

    @GetMapping("/getDoctorRosterWeekly")
    public ResponseEntity<ApiResponse<DoctorRosterResponseDTO>> getDoctorRosterWeekly(
            @RequestParam Long deptId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam String rosterDate) {
        log.info("GET /adminController/getDoctorRosterWeekly called: deptId={}, doctorId={}, rosterDate={}", deptId, doctorId, rosterDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedDate = LocalDate.parse(rosterDate, formatter);
        ApiResponse<DoctorRosterResponseDTO> apiResponse = adminService.getDoctorRosterWeekly(deptId, doctorId, parsedDate, false);
        return ResponseEntity.ok(apiResponse);
    }
}
