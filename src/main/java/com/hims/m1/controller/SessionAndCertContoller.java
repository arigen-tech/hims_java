package com.hims.m1.controller;

import com.hims.m1.abdm_response.AbdmCertificateResponse;
import com.hims.m1.abdm_response.AbdmSessionApiResponse;
import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.service.ApiControllerLogService;
import com.hims.m1.service.SessionAndCertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/abdm/abha")
@CrossOrigin(origins = "*")
@Tag(name = "ABDM Session & Certificate", description = "APIs to generate ABDM session tokens and fetch public certificates required for secure communication")
public class SessionAndCertContoller {

    @Autowired
    private SessionAndCertService sessionAndCertService;

    @Autowired
    private ApiControllerLogService apiControllerLogService;

    @Operation(summary = "Generate ABDM Session Token", description = "This API generates a session token from ABDM which is required for subsequent secured API calls.")
    @GetMapping("/get-session")
    public ResponseEntity<Mono<ApiResponse<AbdmSessionApiResponse>>> getSessionToken() {
        Mono<ApiResponse<AbdmSessionApiResponse>> responseMono = apiControllerLogService.logMonoApi(
                "/api/v1/abdm/abha/get-session",
                null,
                sessionAndCertService.fetchTokenFromApi()
        );
        return new ResponseEntity<>(responseMono, HttpStatus.OK);
    }

    @Operation(summary = "Fetch ABDM Public Certificate", description = "This API fetches the ABDM public encryption certificate used for encrypting sensitive request payloads.")
    @GetMapping("/get-certificate")
    public ResponseEntity<Mono<ApiResponse<AbdmCertificateResponse>>> getAbdmCertificate() {
        Mono<ApiResponse<AbdmCertificateResponse>> responseMono = apiControllerLogService.logMonoApi(
                "/api/v1/abdm/abha/get-certificate",
                null,
                sessionAndCertService.getCertificateABDM()
        );
        return new ResponseEntity<>(responseMono, HttpStatus.OK);
    }
}
