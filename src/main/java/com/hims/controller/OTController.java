package com.hims.controller;

import com.hims.response.ActiveAdmissionOtResponse;
import com.hims.response.ActiveAdmissionResponse;
import com.hims.response.ApiResponse;
import com.hims.service.OtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "OTController")
@RequestMapping("/otController")
@Slf4j
@AllArgsConstructor
public class OTController {

    private final OtService otService;


    @GetMapping("/activeAdmissionList")
    public ResponseEntity<ApiResponse<Page<ActiveAdmissionOtResponse>>> activeAdmissionList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) String admissionNo,
            @RequestParam(required = false) Long wardId)
          {
              log.info("Active admission OT list request. page={}, size={}, patientName={}, mobileNo={}, admissionNo={}, wardId={}", page, size, patientName, mobileNo,
                      admissionNo,
                      wardId);
              ApiResponse<Page<ActiveAdmissionOtResponse>> response = otService.activeAdmissionList(page, size,
                patientName,
                mobileNo,
                admissionNo,
                wardId);
        return ResponseEntity.ok(response);
    }



}
