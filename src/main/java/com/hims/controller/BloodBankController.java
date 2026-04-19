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

    /**
     * Registers a new blood donor with personal, screening, and donation details.
     * Creates a donor profile in the blood bank system with comprehensive information.
     * 
     * @param donorRegistrationRequest Request object containing donor personal details, screening information, and medical history
     * @return ResponseEntity with HTTP status OK containing ApiResponse with confirmation message
     */
    @PostMapping("/registerDonor")
    public ResponseEntity<ApiResponse<String>> registerDonor(@RequestBody DonorRegistrationRequest donorRegistrationRequest) {
        log.info("Received request to register donor with mobileNo: {}, firstName: {}",
                donorRegistrationRequest.getBloodDonorPersonalDetailsRequest().getMobileNo(),
                donorRegistrationRequest.getBloodDonorPersonalDetailsRequest().getFirstName());
        ApiResponse<String> response = bloodBankService.registerDonor(donorRegistrationRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing donor's information and adds a new screening record.
     * Allows modification of donor details and creates a new screening entry for the donor.
     * 
     * @param donorId The unique identifier of the donor to update
     * @param donorRegistrationRequest Request object containing updated donor details and new screening information
     * @return ResponseEntity with HTTP status OK containing ApiResponse with confirmation message
     */
    @PutMapping("/updateDonorAndAddNewScreening")
    public ResponseEntity<ApiResponse<String>> updateDonor(
            @RequestParam Long donorId,
            @RequestBody DonorRegistrationRequest donorRegistrationRequest) {

        log.info("Received request to update donor with id: {}", donorId);
        ApiResponse<String> response = bloodBankService.updateDonor(donorId, donorRegistrationRequest);
        log.info("Successfully updated donor with id: {}", donorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a paginated list of all donors with their latest screening results.
     * Supports filtering by donor name and mobile number for advanced search capabilities.
     * 
     * @param page Zero-based page number (default: 0)
     * @param size Number of records per page (default: 5)
     * @param donorName Optional filter by donor's name (supports partial matching)
     * @param mobileNo Optional filter by donor's mobile number
     * @return ApiResponse containing a paginated Page of DonorResponse objects
     */
    @GetMapping("/getAllDonorScreeningResultList")
    public ApiResponse<Page<DonorResponse>> getAllDonor(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam Long hospitalId,
            @RequestParam(required = false) String donorName,
            @RequestParam(required = false) String mobileNo) {
        log.info("Received request to fetch donors. page: {}, size: {}, donorName: {}, mobileNo: {}", page, size, donorName, mobileNo);
        Pageable pageable = PageRequest.of(page, size);
        return bloodBankService.getAllDonor(hospitalId,pageable, donorName, mobileNo);
    }

    /**
     * Retrieves comprehensive screening and personal details for a specific blood donor.
     * Fetches complete donor information including medical history and screening results.
     * 
     * @param donorId The unique identifier of the donor
     * @return ApiResponse containing BloodDonorScreeningDetailsResponse with donor's complete information
     */
    @GetMapping("/getDonorAndScreeningDetails")
    public ApiResponse<BloodDonorScreeningDetailsResponse> getDonorScreeningDetails(@RequestParam Long donorId,
                                                                                    @RequestParam Long hospitalId ) {
        log.info("Received request to fetch donor screening details for donorId: {}", donorId);
        ApiResponse<BloodDonorScreeningDetailsResponse> response = bloodBankService.getDonorScreeningDetails(donorId,hospitalId);
        log.info("Successfully fetched donor screening details for donorId: {}", donorId);
        return response;
    }

    /**
     * Retrieves a list of all blood donors pending blood collection.
     * Fetches donors who have been approved for donation but collection is yet to be performed.
     * 
     * @return ApiResponse containing a List of BloodDonorCollectionResponse with pending collection donors
     */
    @GetMapping("/pendingBloodCollectionList")
    public ApiResponse<List<BloodDonorCollectionResponse>> pendingBloodCollection( @RequestParam Long hospitalId) {
        log.info("Received request to fetch pending blood collection");
        return bloodBankService.pendingBloodCollection(hospitalId);
    }

    /**
     * Retrieves detailed information for a specific blood donor pending collection.
     * Fetches comprehensive collection details including donor vitals and blood bank requirements.
     * 
     * @param donorId The unique identifier of the donor
     * @return ApiResponse containing BloodDonorCollectionDetailsResponse with donor's collection details
     */
    @GetMapping("/pendingBloodCollectionDetails")
    public ApiResponse<BloodDonorCollectionDetailsResponse> pendingBloodCollectionDetails(@RequestParam Long donorId,
                                                                                          @RequestParam Long hospitalId) {
        log.info("Received request to fetch pending blood collection details for donorId: {}", donorId);
        return bloodBankService.pendingBloodCollectionDetails(donorId,hospitalId);
    }

    /**
     * Records the successful collection of blood from a donor.
     * Saves blood collection details including collection time, quantity, and other collection parameters.
     * Updates the donor's collection status and blood inventory records.
     * 
     * @param bloodCollectionRequest Request object containing blood collection details and collection information
     * @return ResponseEntity with HTTP status OK containing ApiResponse with confirmation message
     */
    @PostMapping("/saveBloodCollection")
    public ResponseEntity<ApiResponse<String>> saveBloodCollection(@RequestBody BloodCollectionRequest bloodCollectionRequest) {
        log.info("Received request to save blood collection for request: {}", bloodCollectionRequest);
        ApiResponse<String> response = bloodBankService.saveBloodCollection(bloodCollectionRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a list of blood donations pending component generation/separation.
     * Fetches collected blood units that have not yet been processed for component separation.
     * Used to manage the blood component preparation workflow.
     * 
     * @return ApiResponse containing a List of PendingComponentGenerationResponse objects
     */
    @GetMapping("/pendingComponentGenerationList")
    public ApiResponse<List<PendingComponentGenerationResponse>> pendingComponentGenerationList(@RequestParam Long hospitalId) {
        log.info("Received request for pending component generation list");
        return bloodBankService.pendingComponentGenerationList(hospitalId);
    }

    /**
     * Marks a blood component generation as failed due to quality or processing issues.
     * Records the failure reason and updates the blood unit status accordingly.
     * Prevents defective components from entering the inventory.
     * 
     * @param donationId The unique identifier of the blood donation
     * @param ComponentFailureReasonId The unique identifier of the failure reason code
     * @return ResponseEntity with HTTP status OK containing ApiResponse with confirmation message
     */
    @PutMapping("/componentGenerationFail")
    public ResponseEntity<ApiResponse<String>> saveFailureComponentGeneration(@RequestParam Long donationId, @RequestParam Long ComponentFailureReasonId) {
        log.info("Received request to fail component generation for donationId: {} and componentFailureReasonId: {}",
                donationId, ComponentFailureReasonId);
        ApiResponse<String> response = bloodBankService.failComponentGeneration(donationId, ComponentFailureReasonId);
        return ResponseEntity.ok(response);
    }

    /**
     * Records successful blood component generation and separation from collected blood units.
     * Processes the blood through component separation workflow and adds separated components to inventory.
     * Includes detailed component information such as type, quantity, and testing results.
     * 
     * @param request Request object containing donation ID and component generation details
     * @return ResponseEntity with HTTP status OK containing ApiResponse with confirmation message
     */
    @PostMapping("/componentGenerationPass")
    public ResponseEntity<ApiResponse<String>> savePassComponentGeneration(@RequestBody SaveComponentGenerationRequest request) {
        log.info("Received request to save component generation for donationId: {}", request.getDonationId());
        ApiResponse<String> response = bloodBankService.saveComponentGeneration(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a list of blood donations pending mandatory disease testing.
     * Fetches blood units that require mandatory infectious disease testing before inventory placement.
     * Used to manage the mandatory testing workflow for blood safety compliance.
     * 
     * @return ApiResponse containing a List of PendingForMandatoryTestingResponse objects
     */
    @GetMapping("/pendingForMandatoryTestingList")
    public ApiResponse<List<PendingForMandatoryTestingResponse>> pendingForMandatoryTestingList(@RequestParam Long hospitalId) {
        log.info("Fetching pending mandatory testing list");
        return bloodBankService.pendingForMandatoryTestingList(hospitalId);
    }

    /**
     * Records mandatory disease testing results for blood donations.
     * Processes test data and associated test report files for mandatory infectious disease screening.
     * Manages the complete mandatory testing workflow including data validation and file storage.
     * 
     * @param data JSON string containing MandatoryTestingSaveRequest with test details and results
     * @param files Optional list of test report files (lab reports, certificates, etc.)
     * @return ResponseEntity with HTTP status OK containing ApiResponse with confirmation message
     * @throws JsonProcessingException if JSON data cannot be deserialized
     */
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
     * Fetches blood bank stock and availability information.
     * Retrieves detailed inventory data with support for filtering and multiple view types.
     * Can provide summary or detailed views of blood components with status, expiry, and type filtering.
     * 
     * @param bloodGroupId Optional filter by specific blood group (AO, B+, O-, AB+, etc.)
     * @param componentId Optional filter by blood component type (RBC, Plasma, Platelets, etc.)
     * @param inventoryStatus Optional filter by inventory status (Available, Reserved, Expired, etc.)
     * @param expiryFilter Optional filter by expiry timeframe (24HRS, 3DAYS, 7DAYS, etc.)
     * @param collectionType Optional filter by collection type
     * @param viewType Required view type - 'S' for summary view or 'D' for detailed view
     * @return ApiResponse containing blood stock data based on view type and filters applied
     */
    @GetMapping("/bloodBankStockAndAvailability")
    public ApiResponse<?> getBloodStock(@RequestParam(required = false) Long bloodGroupId,
            @RequestParam(required = false) Long componentId,
            @RequestParam(required = false) Long inventoryStatus,
            @RequestParam(required = false) String expiryFilter,
            @RequestParam(required = false) Long collectionType,
            @RequestParam Long hospitalId,
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
        req.setHospitalId(hospitalId);
        req.setViewType(viewType);
        return bloodBankService.getBloodStock(req);
    }
}
