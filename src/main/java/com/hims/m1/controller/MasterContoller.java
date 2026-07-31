package com.hims.m1.controller;

import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.response.MasterResponse;
import com.hims.m1.service.ApiControllerLogService;
import com.hims.m1.service.MasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/abdm/master")
@CrossOrigin(origins = "*")
@Tag(name = "ABDM Master Data", description = "APIs to fetch master/reference data required for ABHA verification workflows")
public class MasterContoller {

    @Autowired
    private MasterService masterService;

    @Autowired
    private ApiControllerLogService apiControllerLogService;

    @Operation(summary = "Fetch ABHA Verification Types", description = "This API returns the list of available verification types such as ABHA number, Mobile number, or other supported identifiers.")
    @PostMapping("/get-list")
    public ResponseEntity<ApiResponse<List<MasterResponse>>> getVerificationTypeList() {
        try {
            ResponseEntity<ApiResponse<List<MasterResponse>>> response = ResponseEntity.ok(masterService.getVerificationType());
            apiControllerLogService.logSuccess("/api/v1/abdm/master/get-list", null, response);
            return response;
        } catch (RuntimeException exception) {
            apiControllerLogService.logFailure("/api/v1/abdm/master/get-list", null, exception);
            throw exception;
        }
    }
}
