package com.hims.m1.controller;

import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.request.DemoAuthRequest;
import com.hims.m1.response.AUthDeamoResponse;
import com.hims.m1.service.ApiControllerLogService;
import com.hims.m1.service.AuthDemoAbhaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/abdm/auth")
@CrossOrigin(origins = "*")
@Tag(name = "ABHA Verification", description = "APIs for ABHA number verification using OTP-based authentication")
public class AuthDemoAbhaContoller {

    @Autowired
    private AuthDemoAbhaService authDemoAbhaService;

    @Autowired
    private ApiControllerLogService apiControllerLogService;

    @Operation(summary = "Send OTP for ABHA Verification", description = "This API sends an OTP to the registered mobile number linked with the provided ABHA number or input type.")
    @PostMapping("/demo")
    public Mono<ResponseEntity<ApiResponse<AUthDeamoResponse>>> authDemo(@Valid @RequestBody DemoAuthRequest demoAuthRequest) throws Exception {
        return apiControllerLogService.logMonoApi(
                "/api/v1/abdm/auth/demo",
                demoAuthRequest,
                authDemoAbhaService.authDemo(demoAuthRequest).map(ResponseEntity::ok)
        );
    }


}
