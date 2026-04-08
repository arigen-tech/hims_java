package com.hims.service;

import com.hims.request.*;
import com.hims.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BloodBankService {

   ApiResponse<String> registerDonor(DonorRegistrationRequest donorRegistrationRequest);

   @Transactional
   ApiResponse<String> updateDonor(Long donorId, DonorRegistrationRequest donorRegistrationRequest);

   ApiResponse<Page<DonorResponse>> getAllDonor(Pageable pageable, String donorName, String mobileNo);

   ApiResponse<BloodDonorScreeningDetailsResponse> getDonorScreeningDetails(Long donorId);

   ApiResponse<List<BloodDonorCollectionResponse>> pendingBloodCollection();

   ApiResponse<BloodDonorCollectionDetailsResponse> pendingBloodCollectionDetails(Long donorId);

   ApiResponse<String> saveBloodCollection(BloodCollectionRequest bloodCollectionRequest);

   ApiResponse<List<PendingComponentGenerationResponse>> pendingComponentGenerationList();

   ApiResponse<String> failComponentGeneration(Long donationId, Long componentFailureReasonId);

   ApiResponse<String> saveComponentGeneration(SaveComponentGenerationRequest request);

   ApiResponse<List<PendingForMandatoryTestingResponse>> pendingForMandatoryTestingList();

   ApiResponse<String> mandatoryTestingTestEntry(MandatoryTestingSaveRequest mandatoryTestingSaveRequest, List<MultipartFile> files);

   ApiResponse<?> getBloodStock(BloodStockFilterRequest request);
}
