package com.hims.controller;


import com.hims.request.IpNursingMedicalAssessmentRequest;
import com.hims.request.IpdPatientRequest;
import com.hims.request.SampleCollectionRequest;
import com.hims.response.*;
import com.hims.service.IPDPatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ipd")
@AllArgsConstructor
@Slf4j

public class IPDPatientController {

    private final IPDPatientService ipdPatientService;

    @GetMapping("ipdPatientWaitingList")
    public ResponseEntity<ApiResponse<Page<IPDPatientWaitingListResponse>>> ipdPatientWaitingList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam Long hospitalId,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String mobileNo) {

        ApiResponse<Page<IPDPatientWaitingListResponse>> response = ipdPatientService.ipdPatientWaitingList(page, size,
                hospitalId,
                        patientName,
                        mobileNo
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Save IPD Patient Details",
            description = "This API is used to save IPD patient details including admission, bed allocation, NOK details, and document details.")
    @PostMapping(value = "/saveIpdPatientDetails", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> saveIpdPatientDetails(@Valid @ModelAttribute IpdPatientRequest request) {

        log.info("Request received to save IPD patient details for patientId: {}", request.getPatientId());

        return ipdPatientService.saveIpdPatientDetails(request);
    }

    @GetMapping("getWardByDepartment")
    public ResponseEntity<ApiResponse<List<IpdWardResponse>>> getWardByDepartment(@RequestParam Long departmentId ){

        ApiResponse<List<IpdWardResponse>> response = ipdPatientService.getWardByDepartment(departmentId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("getRoomByWard/{wardId}")
    public ResponseEntity<ApiResponse<List<IpdRoomResponse>>> getRoomByWard(@PathVariable Long wardId ){

        ApiResponse<List<IpdRoomResponse>> response = ipdPatientService.getRoomByWard(wardId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getWardByCategory/{wardCategoryId}")
    public ResponseEntity<ApiResponse<List<WardResponse>>> getWardsByCategory(@PathVariable Long wardCategoryId) {
        log.info("GET /getWardByCategory/{} called", wardCategoryId);
        ApiResponse<List<WardResponse>> response = ipdPatientService.getWardByCategory(wardCategoryId);
        HttpStatus status = (response.getStatus() == 200) ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(response, status);
    }

    @GetMapping("getBedByRoom/{roomId}")
    public ResponseEntity<ApiResponse<List<BedResponse>>> getBedByRoom(@PathVariable Long roomId ){

        log.info("Request received to fetch beds for roomId: {}", roomId);

        ApiResponse<List<BedResponse>> response = ipdPatientService.getBedByRoom(roomId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("getWardWiseDetails/{departmentId}")
    public ResponseEntity<ApiResponse<List<WardWiseDetailsResponse>>> getWardWiseDetails(@PathVariable Long departmentId ){

        log.info("Request received to getWardWiseDetails");

        ApiResponse<List<WardWiseDetailsResponse>> response = ipdPatientService.getWardWiseDetails(departmentId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("getTotalBedCount/{departmentId}")
    public ResponseEntity<ApiResponse<TotalBedCountResponse>> getTotalBedCount(@PathVariable Long departmentId ){

        log.info("Request received to fetch beds for departmentId: {}", departmentId);

        ApiResponse<TotalBedCountResponse> response = ipdPatientService.getTotalBedCount(departmentId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("SaveIpNursingMedicalAssessment")
    public ApiResponse<String> SaveIpNursingMedicalAssessment(@RequestBody IpNursingMedicalAssessmentRequest request) {
        log.info(
                "Request received to save IP nursing medical assessment. inpatientId: {}, hospitalId: {}",
                request.getInpatientId(),
                request.getHospitalId()
        );
        return ipdPatientService.SaveIpNursingMedicalAssessment(request);
    }














}
