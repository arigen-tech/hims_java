package com.hims.controller;


import com.hims.request.IpdPatientRequest;
import com.hims.request.SampleCollectionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.IPDPatientWaitingListResponse;
import com.hims.service.IPDPatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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



}
