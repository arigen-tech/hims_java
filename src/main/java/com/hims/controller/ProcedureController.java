package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.ProcedureWorklistResponse;
import com.hims.service.ProcedureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/procedure")
@RequiredArgsConstructor
public class ProcedureController {

    private final ProcedureService procedureService;

    @GetMapping("/getProcedureWorkList")
    public ResponseEntity<ApiResponse<Page<ProcedureWorklistResponse>>> getProcedureWorklist(
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) String patientName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<ProcedureWorklistResponse> worklist =
                procedureService.getProcedureWorklist(
                        mobileNo,
                        patientName,
                        page,
                        size
                );

        return ResponseEntity.ok(
                new ApiResponse<>(200, "Procedure worklist fetched successfully", worklist)
        );
    }
}