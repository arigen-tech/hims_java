package com.hims.controller;

import com.hims.request.DonorRegistrationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.BloodDonorResponse;
import com.hims.service.BloodBankService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "BloodBankController", description = "This controller is used for Blood Bank Related task.")
@RequestMapping("/bloodBank")
@Slf4j
public class BloodBankController {

    @Autowired
    private BloodBankService bloodBankService;

    @PostMapping("/registerDonor")
    public ResponseEntity<ApiResponse<BloodDonorResponse>> registerDonor(@RequestBody DonorRegistrationRequest donorRegistrationRequest){
        ApiResponse<BloodDonorResponse> response = bloodBankService.registerDonor(donorRegistrationRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/updateDonor/{id}")
    public ResponseEntity<ApiResponse<BloodDonorResponse>> updateDonor(@PathVariable Long id, @RequestBody DonorRegistrationRequest donorRegistrationRequest){
        ApiResponse<BloodDonorResponse> response = bloodBankService.updateDonor(id, donorRegistrationRequest);
        return ResponseEntity.ok(response);
    }
}
