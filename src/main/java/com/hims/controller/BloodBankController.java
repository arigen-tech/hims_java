package com.hims.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hims.constants.AppConstants;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.BloodBankService;
import com.hims.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Tag(name = "BloodBankController", description = "This controller is used for Blood Bank Related task.")
@RequestMapping("/bloodBank")
@Slf4j
public class BloodBankController {

    @Autowired
    private BloodBankService bloodBankService;

    @PostMapping("/registerDonor")
    public ResponseEntity<ApiResponse<String>> registerDonor(@RequestBody DonorRegistrationRequest donorRegistrationRequest) {
        log.info("Received request to register donor with mobileNo: {}, firstName: {}",
                donorRegistrationRequest.getBloodDonorPersonalDetailsRequest().getMobileNo(),
                donorRegistrationRequest.getBloodDonorPersonalDetailsRequest().getFirstName());
        ApiResponse<String> response = bloodBankService.registerDonor(donorRegistrationRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/updateDonorAndAddNewScreening")
    public ResponseEntity<ApiResponse<String>> updateDonor(
            @RequestParam Long donorId,
            @RequestBody DonorRegistrationRequest donorRegistrationRequest) {

        log.info("Received request to update donor with id: {}", donorId);
        ApiResponse<String> response = bloodBankService.updateDonor(donorId, donorRegistrationRequest);
        log.info("Successfully updated donor with id: {}", donorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getAllDonorScreeningResultList")
    public ApiResponse<Page<DonorResponse>> getAllDonor(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String donorName,
            @RequestParam(required = false) String mobileNo) {
        log.info("Received request to fetch donors. page: {}, size: {}, donorName: {}, mobileNo: {}", page, size, donorName, mobileNo);
        Pageable pageable = PageRequest.of(page, size);
        return bloodBankService.getAllDonor(pageable, donorName, mobileNo);
    }

    @GetMapping("/getDonorAndScreeningDetails")
    public ApiResponse<BloodDonorScreeningDetailsResponse> getDonorScreeningDetails(@RequestParam Long donorId) {
        log.info("Received request to fetch donor screening details for donorId: {}", donorId);
        ApiResponse<BloodDonorScreeningDetailsResponse> response = bloodBankService.getDonorScreeningDetails(donorId);
        log.info("Successfully fetched donor screening details for donorId: {}", donorId);
        return response;
    }

    @GetMapping("/pendingBloodCollectionList")
    public ApiResponse<List<BloodDonorCollectionResponse>> pendingBloodCollection() {
        log.info("Received request to fetch pending blood collection");
        return bloodBankService.pendingBloodCollection();
    }

    @GetMapping("/pendingBloodCollectionDetails")
    public ApiResponse<BloodDonorCollectionDetailsResponse> pendingBloodCollectionDetails(@RequestParam Long donorId) {
        log.info("Received request to fetch pending blood collection details for donorId: {}", donorId);
        return bloodBankService.pendingBloodCollectionDetails(donorId);
    }

    @PostMapping("/saveBloodCollection")
    public ResponseEntity<ApiResponse<String>> saveBloodCollection(@RequestBody BloodCollectionRequest bloodCollectionRequest) {
        ApiResponse<String> response = bloodBankService.saveBloodCollection(bloodCollectionRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pendingComponentGenerationList")
    public ApiResponse<List<PendingComponentGenerationResponse>> pendingComponentGenerationList() {
        log.info("Received request for pending component generation list");
        return bloodBankService.pendingComponentGenerationList();
    }

    @PutMapping("/componentGenerationFail")
    public ResponseEntity<ApiResponse<String>> saveComponentGeneration(@RequestParam Long donationId, @RequestParam Long ComponentFailureReasonId) {
        log.info("Received request to fail component generation for donationId: {} and componentFailureReasonId: {}",
                donationId, ComponentFailureReasonId);
        ApiResponse<String> response = bloodBankService.failComponentGeneration(donationId, ComponentFailureReasonId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/componentGenerationPass")
    public ResponseEntity<ApiResponse<String>> saveComponentGeneration(@RequestBody SaveComponentGenerationRequest request) {
        log.info("Received request to save component generation for donationId: {}", request.getDonationId());
        ApiResponse<String> response = bloodBankService.saveComponentGeneration(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pendingForMandatoryTestingList")
    public ApiResponse<List<PendingForMandatoryTestingResponse>> pendingForMandatoryTestingList() {
        log.info("Fetching pending mandatory testing list");
        return bloodBankService.pendingForMandatoryTestingList();
    }

    @PostMapping("/mandatoryTestingTestEntry")
    public ResponseEntity<ApiResponse<String>> mandatoryTestingTestEntry(@RequestParam("data") String data,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws JsonProcessingException {

            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            MandatoryTestingSaveRequest request = mapper.readValue(data, MandatoryTestingSaveRequest.class);
            log.info("Received mandatory testing request for donationId: {}", request.getDonationId());
            ApiResponse<String> response = bloodBankService.mandatoryTestingTestEntry(request, files);
            return ResponseEntity.ok(response);


    }
    /**
     * Fetch Blood Bank Stock and Availability
     *
     * @param bloodGroupId   optional blood group ID
     * @param componentId    optional component ID
     * @param inventoryStatus optional inventory status
     * @param expiryFilter   optional expiry filter (24HRS, 3DAYS, 7DAYS)
     * @param collectionType optional collection type
     * @param viewType       required view type (summery=S / details=D)
     * @return ApiResponse containing stock data
     */
    @GetMapping("/bloodBankStockAndAvailability")
    public ApiResponse<?> getBloodStock(@RequestParam(required = false) Long bloodGroupId,
            @RequestParam(required = false) Long componentId,
            @RequestParam(required = false) Long inventoryStatus,
            @RequestParam(required = false) String expiryFilter,
            @RequestParam(required = false) Long collectionType,
            @RequestParam String viewType)
    {
        log.info("Request Params -> bloodGroupId: {}, componentId: {}, inventoryStatus: {}, expiryFilter: {}, collectionType: {}, viewType: {}",
                bloodGroupId, componentId, inventoryStatus, expiryFilter, collectionType, viewType);

        BloodStockFilterRequest req = new BloodStockFilterRequest();
        req.setBloodGroupId(bloodGroupId);
        req.setComponentId(componentId);
        req.setInventoryStatus(inventoryStatus);
        req.setExpiryFilter(expiryFilter);
        req.setCollectionType(collectionType);
        req.setViewType(viewType);
        return bloodBankService.getBloodStock(req);
    }
}
