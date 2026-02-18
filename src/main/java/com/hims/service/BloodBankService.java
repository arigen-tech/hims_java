package com.hims.service;

import com.hims.request.DonorRegistrationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.BloodDonorResponse;
import org.springframework.transaction.annotation.Transactional;

public interface BloodBankService {

   ApiResponse<BloodDonorResponse> registerDonor(DonorRegistrationRequest donorRegistrationRequest);

   @Transactional
   ApiResponse<BloodDonorResponse> updateDonor(Long donorId, DonorRegistrationRequest request);
}
