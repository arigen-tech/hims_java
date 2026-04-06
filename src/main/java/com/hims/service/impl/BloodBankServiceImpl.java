package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.bloodBankException.DonorSaveException;
import com.hims.exception.bloodBankException.ScreeningSaveException;
import com.hims.projection.*;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.BloodBankService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class BloodBankServiceImpl implements BloodBankService{
    @Autowired
    private BloodDonorRepository bloodDonorRepository;
    @Autowired
    private MasGenderRepository masGenderRepository;
    @Autowired
    private MasBloodGroupRepository masBloodGroupRepository;
    @Autowired
    private MasBloodDonationTypeRepository masBloodDonationTypeRepository;
    @Autowired
    private MasCountryRepository masCountryRepository;
    @Autowired
    private MasStateRepository masStateRepository;
    @Autowired
    private MasDistrictRepository masDistrictRepository;
    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private BloodDonorScreeningRepository bloodDonorScreeningRepository;
    @Autowired
    private MasRelationRepository masRelationRepository;
    @Autowired
    private  BloodDonationHdrRepository bloodDonationHdrRepository;
    @Autowired
    private MasBloodCollectionTypeRepository bloodCollectionTypeRepository;
    @Autowired
    private MasBloodBagTypeRepository masBloodBagTypeRepository;
    @Autowired MasBloodDonationStatusRepository masBloodDonationStatusRepository;
    @Value("${bloodDonationStatusCollected}")
    private Long bloodDonationStatusCollected;
    @Value("${bloodDonationStatusComponent_Failed}")
    private Long bloodDonationStatusComponent_Failed;
    @Value("${bloodDonationStatusComponent_Generated}")
    private Long bloodDonationStatusComponent_Generated;
    @Value("${bloodDonationStatus_TEST_FAILED}")
    private  Long bloodDonationStatus_TEST_FAILED;
    @Value("${bloodDonationStatus_AVAILABLE}")
    private Long bloodDonationStatus_AVAILABLE;
    @Autowired
    private BloodComponentInventoryRepository bloodComponentInventoryRepository;

    @Autowired
    private MasComponentFailureReasonRepository masComponentFailureReasonRepository;
    @Autowired
    private MasBloodComponentRepository masBloodComponentRepository;
    @Autowired BloodDonationDtRepository bloodDonationDtRepository;
    @Autowired
    private BloodDonationTestResultRepository bloodDonationTestResultRepository;
    @Autowired
    private MasBloodTestRepository masBloodTestRepository;
    @Autowired
    private BloodDonationInvestigationDocRepository bloodDonationInvestigationDocRepository;



    private String generateDonorCode() {
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "DON-" + year + "-";
        String lastCode = bloodDonorRepository.findLastDonorCodeByPrefix(prefix);
        long nextNumber = 1;
        if (lastCode != null) {
            String numericPart = lastCode.substring(prefix.length());
            nextNumber = Long.parseLong(numericPart) + 1;
        }
        return prefix + String.format("%04d", nextNumber);
    }
    private String generateBagNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "BAG-" + year + "-";
        String lastCode = bloodDonationHdrRepository.findLastBagNumberPrefix(prefix);
        long nextNumber = 1;
        if (lastCode != null) {
            String numericPart = lastCode.substring(prefix.length());
            nextNumber = Long.parseLong(numericPart) + 1;
        }
        return prefix + String.format("%04d", nextNumber);
    }

    @Override
    @Transactional
    public ApiResponse<String> registerDonor(DonorRegistrationRequest request)  {
        log.info("Starting donor registration process");

        BloodDonorPersonalDetailsRequest pd = request.getBloodDonorPersonalDetailsRequest();
        boolean exists = bloodDonorRepository.existsDonorByDetails(
                pd.getMobileNo(),
                pd.getFirstName(),
                pd.getDateOfBirth(),
                pd.getRelationId(),
                pd.getBloodGroupId());

        if (exists) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Donor already registered with same details", HttpStatus.BAD_REQUEST.value()
            );
        }
        log.info("Saving donor personal details");
        BloodDonor donor = saveDonorDetails(request.getBloodDonorPersonalDetailsRequest());
        log.info("Donor personal details saved successfully with donorId: {}", donor.getDonorId());

        log.info("Saving donor screening details");
        BloodDonorScreening screening = saveDonorScreeningDetails(request.getBloodDonorScreeningRequest(), donor);
        log.info("Donor screening details saved successfully with screeningId: {}", screening.getScreeningId());

        log.info("Donor registration completed successfully");
        return ResponseUtils.createSuccessResponse("Donor Registration successfully", new TypeReference<>() {});

    }


    @Transactional
    @Override
    public ApiResponse<String> updateDonor(Long donorId, DonorRegistrationRequest request) {
        BloodDonor donor = bloodDonorRepository.findById(donorId).orElseThrow(() -> new DonorSaveException("Donor not found"));

        updateDonorDetails(donor,request.getBloodDonorPersonalDetailsRequest());

        BloodDonorScreening screening = saveDonorScreeningDetails(request.getBloodDonorScreeningRequest(), donor);

        BloodDonorScreeningDetailsResponse response = mapToResponse(donor, screening);

        return ResponseUtils.createSuccessResponse("Donor update successfully and add new screening", new TypeReference<>() {});
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<DonorResponse>> getAllDonor(Pageable pageable, String donorName, String mobileNo) {

        Page<DonorProjection> projections = bloodDonorRepository.getAllDonor(pageable, donorName, mobileNo);

        Page<DonorResponse> responsePage = projections.map(p -> {
            DonorResponse response = new DonorResponse();
            response.setDonorId(p.getDonorId());
            response.setScreeningId(p.getScreeningId());
            response.setDonorCode(p.getDonorCode());
            response.setName(p.getName());
            response.setGender(p.getGender());
            response.setMobileNo(p.getMobileNo());
            response.setBloodGroup(p.getBloodGroup());
            response.setRegistrationDate(p.getRegistrationDate());
            response.setScreeningResult(p.getScreeningResult());
            return response;
        });

        return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {}
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<BloodDonorScreeningDetailsResponse> getDonorScreeningDetails(Long donorId) {
        log.info("Fetching donor screening details for donorId: {}", donorId);

        try {
            BloodDonorDetailsProjection donor = bloodDonorScreeningRepository.getDonorBasicDetails(donorId);

            List<BloodDonorPreviousScreeningProjection> screeningProjections = bloodDonorScreeningRepository.getDonorPreviousScreenings(donorId);

            BloodDonorScreeningDetailsResponse response = new BloodDonorScreeningDetailsResponse();
            response.setDonorId(donor.getDonorId());
            response.setDonorCode(donor.getDonorCode());
            response.setFirstName(donor.getFirstName());
            response.setLastName(donor.getLastName());
            response.setGender(donor.getGender());
            response.setDateOfBirth(donor.getDateOfBirth());
            response.setMobileNo(donor.getMobileNo());
            response.setBloodGroupId(donor.getBloodGroupId());
            response.setBloodGroup(donor.getBloodGroup());
            response.setDonationType(donor.getDonationType());
            response.setCountryName(donor.getCountryName());
            response.setStateName(donor.getStateName());
            response.setDistrictName(donor.getDistrictName());
            response.setRelation(donor.getRelation());
            response.setDonorScreeningStatus(donor.getDonorScreeningStatus());
            response.setCurrentDeferralReason(donor.getCurrentDeferralReason());
            response.setDeferralUpToDate(donor.getDeferralUpToDate());
            response.setAddressLine1(donor.getAddressLine1());
            response.setAddressLine2(donor.getAddressLine2());
            response.setCountry(donor.getCountry());
            response.setState(donor.getState());
            response.setDistrict(donor.getDistrict());
            response.setCity(donor.getCity());
            response.setPinCode(donor.getPinCode());
            response.setCreatedDate(donor.getCreatedDate());
            response.setCreatedBy(donor.getCreatedBy());

            List<BloodDonorPriviousScreening> screeningList = screeningProjections.stream()
                    .map(p -> {
                        BloodDonorPriviousScreening s = new BloodDonorPriviousScreening();
                        s.setScreeningId(p.getScreeningId());
                        s.setScreeningDate(p.getScreeningDate());
                        s.setHemoglobin(p.getHemoglobin());
                        s.setWeight(p.getWeight());
                        s.setHeight(p.getHeight());
                        s.setBp(p.getBp());
                        s.setPulse(p.getPulse());
                        s.setTemperature(p.getTemperature());
                        s.setScreeningResult(p.getScreeningResult());
                        s.setDeferralType(p.getDeferralType());
                        s.setDeferralReason(p.getDeferralReason());
                        s.setConductedBy(p.getConductedBy());
                        return s;
                    })
                    .toList();

            response.setBloodDonorPriviousScreenings(screeningList);

            log.info("Successfully fetched donor screening details for donorId: {}", donorId);

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error while fetching donor screening details for donorId: {}", donorId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<BloodDonorCollectionResponse>> pendingBloodCollection() {
        log.info("Fetching pending blood collection donors");

        try {
            List<BloodDonorCollectionProjection> projections = bloodDonorRepository.findPendingBloodCollection(AppConstants.DONOR_SCREENING_STATUS_PASS);

            List<BloodDonorCollectionResponse> responseList = projections.stream().map(projection -> {
                BloodDonorCollectionResponse response = new BloodDonorCollectionResponse();
                response.setDonorId(projection.getDonorId());
                response.setDonorCode(projection.getDonorCode());
                response.setFirstName(projection.getFirstName());
                response.setLastName(projection.getLastName());
                response.setBloodGroupId(projection.getBloodGroupId());
                response.setBloodGroup(projection.getBloodGroup());
                response.setLastScreening(projection.getLastScreening());
                response.setHb(projection.getHb());
                response.setWeight(projection.getWeight());
                return response;
            }).toList();

            log.info("Fetched {} pending blood collection donors successfully", responseList.size());
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while fetching pending blood collection donors", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INSUFFICIENT_STORAGE.value()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<BloodDonorCollectionDetailsResponse> pendingBloodCollectionDetails(Long donorId) {
        log.info("Fetching pending blood collection details for donorId: {}", donorId);

        try {
            Optional<BloodDonorCollectionDetailsProjection> optional = bloodDonorRepository.findPendingBloodCollectionDetails(donorId);

            BloodDonorCollectionDetailsProjection p = optional.get();

            BloodDonorCollectionDetailsResponse response = new BloodDonorCollectionDetailsResponse();
            response.setDonorId(p.getDonorId());
            response.setDonorCode(p.getDonorCode());
            response.setFirstName(p.getFirstName());
            response.setLastName(p.getLastName());
            response.setGender(p.getGender());
            response.setDateOfBirth(p.getDateOfBirth());
            response.setMobileNo(p.getMobileNo());
            response.setBloodGroupId(p.getBloodGroupId());
            response.setBloodGroup(p.getBloodGroup());
            response.setDonorScreeningStatus(p.getDonorScreeningStatus());
            response.setAddressLine1(p.getAddressLine1());
            response.setAddressLine2(p.getAddressLine2());
            response.setCountry(p.getCountry());
            response.setCountryName(p.getCountryName());
            response.setState(p.getState());
            response.setStateName(p.getStateName());
            response.setDistrict(p.getDistrict());
            response.setDistrictName(p.getDistrictName());
            response.setCity(p.getCity());
            response.setPinCode(p.getPinCode());
            response.setScreeningId(p.getScreeningId());
            response.setScreeningDate(p.getScreeningDate());
            response.setHemoglobin(p.getHemoglobin());
            response.setWeight(p.getWeight());
            response.setHeight(p.getHeight());
            response.setBp(p.getBp());
            response.setPulse(p.getPulse());
            response.setTemperature(p.getTemperature());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {} );

        } catch (Exception e) {
            log.error("Error while fetching pending blood collection details for donorId: {}", donorId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<String> saveBloodCollection(BloodCollectionRequest bloodCollectionRequest) {
        try {
            log.info("Starting saveBloodCollection for donorId: {}, screeningId: {}",
                    bloodCollectionRequest.getDonorId(),
                    bloodCollectionRequest.getScreeningId());

        BloodDonationHdr bloodDonationHdr=new BloodDonationHdr();
        bloodDonationHdr.setDonorId(bloodDonorRepository.findById(bloodCollectionRequest.getDonorId()).orElseThrow(()-> new RuntimeException("donorId not found")));
        bloodDonationHdr.setScreeningId(bloodDonorScreeningRepository.findById(bloodCollectionRequest.getScreeningId()).orElseThrow(()-> new RuntimeException("screeningId not found")));
        bloodDonationHdr.setDonationTypeId(masBloodDonationTypeRepository.findById(bloodCollectionRequest.getDonationTypeId()).orElseThrow(()-> new RuntimeException("donationTypeId not found")));
        bloodDonationHdr.setBagNumber(generateBagNumber());
        bloodDonationHdr.setCollectionTypeId(bloodCollectionTypeRepository.findById(bloodCollectionRequest.getCollectionTypeId()).orElseThrow(()-> new RuntimeException("collectionTypeId not found")));
        bloodDonationHdr.setBagTypeId(masBloodBagTypeRepository.findById(bloodCollectionRequest.getBagTypeId()).orElseThrow(()-> new RuntimeException("bagTypeId not found")));
        bloodDonationHdr.setTotalCollectedVolumeMl(bloodCollectionRequest.getTotalCollectedVolume());
        bloodDonationHdr.setCreatedDate(LocalDate.now());
        bloodDonationHdr.setCreatedBy(authUtil.getCurrentUser().getFullName());
        bloodDonationHdr.setDonationDatetime(LocalDateTime.now());
        bloodDonationHdr.setDonationStatusId(masBloodDonationStatusRepository.findById(bloodDonationStatusCollected).orElseThrow());

        bloodDonationHdrRepository.save(bloodDonationHdr);

            log.info("Blood collection saved successfully with bagNumber: {}", bloodDonationHdr.getBagNumber());

        return ResponseUtils.createSuccessResponse("blood collection save successfully", new TypeReference<>() {} );
        } catch (Exception e) {
            log.error("Unexpected exception occurred while saving blood collection", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<PendingComponentGenerationResponse>> pendingComponentGenerationList() {
        log.info("Fetching pending component generation list from repository");
        try {
            List<PendingComponentGenerationResponse> pendingComponentGenerationList = bloodDonationHdrRepository.pendingComponentGenerationList(bloodDonationStatusCollected);

            log.info("Pending component generation list fetched successfully. Total records: {}",
                    pendingComponentGenerationList != null ? pendingComponentGenerationList.size() : 0);

            return ResponseUtils.createSuccessResponse(pendingComponentGenerationList, new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Exception occurred while fetching pending component generation list", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> failComponentGeneration(Long donationId, Long componentFailureReasonId) {
        log.info("Fail component generation request received for donationId: {} and componentFailureReasonId: {}",
                donationId, componentFailureReasonId);

        try {
            BloodDonationHdr bloodDonationHdr = bloodDonationHdrRepository.findById(donationId)
                    .orElseThrow(() -> new RuntimeException("Blood donation record not found with id: " + donationId));

            MasComponentFailureReason failureReason = masComponentFailureReasonRepository.findById(componentFailureReasonId)
                    .orElseThrow(() -> new RuntimeException("Component failure reason not found with id: " + componentFailureReasonId));

            bloodDonationHdr.setComponentFailureReason(failureReason);
            bloodDonationHdr.setDonationStatusId(masBloodDonationStatusRepository.findById(bloodDonationStatusComponent_Failed).orElseThrow());
            bloodDonationHdrRepository.save(bloodDonationHdr);
            log.info("Component failure reason updated successfully for donationId: {}", donationId);

            return ResponseUtils.createSuccessResponse("Component failure reason updated successfully", new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while failing component generation for donationId: {}", donationId, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG ,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<String> saveComponentGeneration(SaveComponentGenerationRequest request) {
        log.info("Starting component generation save for donationId: {}", request.getDonationId());

        try {
            BloodDonationHdr donationHdr = bloodDonationHdrRepository.findById(request.getDonationId())
                    .orElseThrow(() -> new RuntimeException("Blood donation record not found with id: " + request.getDonationId()));

            List<BloodDonationDt> donationDtList = new ArrayList<>();
            for (ComponentGenerationRequest row : request.getComponents()) {

                MasBloodComponent component = masBloodComponentRepository.findById(row.getComponentId())
                        .orElseThrow(() -> new RuntimeException("Component not found with id: " + row.getComponentId()));

                BloodDonationDt dt = new BloodDonationDt();
                dt.setDonationHdId(donationHdr);
                dt.setComponentId(component);
                dt.setUnitNo(row.getUnitNo().trim());
                dt.setVolumeMl(row.getVolumeMl());
                dt.setExpiryDate(row.getExpiryDate());
                dt.setCreatedDate(LocalDateTime.now());
                dt.setCreatedBy(authUtil.getCurrentUser().getFullName());
                donationDtList.add(dt);
            }

            bloodDonationDtRepository.saveAll(donationDtList);

            donationHdr.setComponentGenerationDatetime(LocalDateTime.now());
            donationHdr.setDonationStatusId(masBloodDonationStatusRepository.findById(bloodDonationStatusComponent_Generated).orElseThrow());
            bloodDonationHdrRepository.save(donationHdr);
            log.info("Component generation saved successfully for donationId: {}", request.getDonationId());
            return ResponseUtils.createSuccessResponse("Component generation saved successfully", new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error while saving component generation for donationId: {}", request.getDonationId(), e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<PendingForMandatoryTestingResponse>> pendingForMandatoryTestingList() {
        try {
            List<PendingForMandatoryTestingResponse> list =
                    bloodDonationHdrRepository.pendingForMandatoryTestingList(bloodDonationStatusComponent_Generated);

            return ResponseUtils.createSuccessResponse(list, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while fetching pending mandatory testing list", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> mandatoryTestingTestEntry(MandatoryTestingSaveRequest mandatoryTestingSaveRequest, List<MultipartFile> files) {

        boolean isFailed = false;

        BloodDonationHdr bloodDonationHdr = bloodDonationHdrRepository.findById(mandatoryTestingSaveRequest.getDonationId()).orElseThrow();

        for (TestResultRequest dto : mandatoryTestingSaveRequest.getTestResults()) {

            BloodDonationTestResult entity = new BloodDonationTestResult();
            entity.setDonation(bloodDonationHdr);
            entity.setResult(dto.getResult());
            entity.setTest(masBloodTestRepository.findById(dto.getTestId()).orElseThrow());
            entity.setTestDate(dto.getTestDate());
            entity.setRemarks(dto.getRemarks());
            entity.setCreatedDate(LocalDateTime.now());
            entity.setCreatedBy(authUtil.getCurrentUser().getFullName());

            bloodDonationTestResultRepository.save(entity);

            if ("REACTIVE".equalsIgnoreCase(dto.getResult())) {
                isFailed = true;
            }
        }

        uploadMultipleDocs(bloodDonationHdr, files);

        if (isFailed) {
            var availableStatus = masBloodDonationStatusRepository.findById(bloodDonationStatus_TEST_FAILED).orElseThrow(() -> new RuntimeException("Status not found"));
            bloodDonationHdr.setDonationStatusId(availableStatus);
            List<BloodDonationDt> components = bloodDonationDtRepository.findByDonationHdId(bloodDonationHdr);

            for (BloodDonationDt dt : components) {
                dt.setComponentStatus(availableStatus);
              
            }
            bloodDonationDtRepository.saveAll(components);
        } else {
            bloodDonationHdr.setDonationStatusId(masBloodDonationStatusRepository.findById(bloodDonationStatus_AVAILABLE).orElseThrow());
            bloodDonationHdr.setTestingDatetime(LocalDateTime.now());
            createInventoryEntries(bloodDonationHdr);
        }

        bloodDonationHdrRepository.save(bloodDonationHdr);

        return ResponseUtils.createSuccessResponse(
                "test entry create successfully", new TypeReference<>() {});
    }

    @Override
    public ApiResponse<?> getBloodStock(BloodStockFilterRequest req) {

        if ("S".equalsIgnoreCase(req.getViewType())) {
            List<BloodStockSummaryProjection> list = bloodComponentInventoryRepository.getSummary(
                            req.getBloodGroupId(),
                            req.getComponentId(),
                            req.getInventoryStatus(),
                            req.getCollectionType(),
                            req.getExpiryFilter(),
                    AppConstants.COMPONENT_CRYO,
                    AppConstants.COMPONENT_PLASMA,
                    AppConstants.COMPONENT_PLT,
                    AppConstants.COMPONENT_PRBC);
            return ResponseUtils.createSuccessResponse(list, new TypeReference<>() {});

        } else {
            List<BloodStockDetailedProjection> list = bloodComponentInventoryRepository.getDetailed(
                            req.getBloodGroupId(),
                            req.getComponentId(),
                            req.getInventoryStatus(),
                            req.getCollectionType(),
                            req.getExpiryFilter());
            return ResponseUtils.createSuccessResponse(list, new TypeReference<>() {});
        }
    }
    @Transactional
    public BloodDonor saveDonorDetails(BloodDonorPersonalDetailsRequest personalDetailsRequest){
        try {
            BloodDonor donor = new BloodDonor();
            donor.setDonorCode(generateDonorCode());
            donor.setFirstName(personalDetailsRequest.getFirstName());
            donor.setLastName(personalDetailsRequest.getLastName());
            if (personalDetailsRequest.getGenderId() != null) {
                MasGender gender = masGenderRepository.getReferenceById(personalDetailsRequest.getGenderId());
                donor.setGender(gender);
            }
            donor.setDateOfBirth(personalDetailsRequest.getDateOfBirth());
            donor.setMobileNo(personalDetailsRequest.getMobileNo());
            if (personalDetailsRequest.getBloodGroupId() != null) {
                MasBloodGroup group = masBloodGroupRepository.getReferenceById(personalDetailsRequest.getBloodGroupId());
                donor.setBloodGroup(group);
            }

//            if (personalDetailsRequest.getDonationTypeId() != null) {
//                MasBloodDonationType type = masBloodDonationTypeRepository.getReferenceById(personalDetailsRequest.getDonationTypeId());
//                donor.setDonationType(type);
//            }
            if (personalDetailsRequest.getRelationId() != null) {
                MasRelation masRelation = masRelationRepository.getReferenceById(personalDetailsRequest.getRelationId());
                donor.setRelation(masRelation);
            }
            donor.setAddressLine1(personalDetailsRequest.getAddressLine1());
            donor.setAddressLine2(personalDetailsRequest.getAddressLine2());

            if (personalDetailsRequest.getCountryId() != null) {
                MasCountry country = masCountryRepository.getReferenceById(personalDetailsRequest.getCountryId());
                donor.setCountry(country);
            }
            if (personalDetailsRequest.getStateId() != null) {
                MasState state = masStateRepository.getReferenceById(personalDetailsRequest.getStateId());
                donor.setState(state);
            }
            if (personalDetailsRequest.getDistrictId() != null) {
                MasDistrict district = masDistrictRepository.getReferenceById(personalDetailsRequest.getDistrictId());
                donor.setDistrict(district);
            }
            donor.setCity(personalDetailsRequest.getCity());
            donor.setPincode(personalDetailsRequest.getPinCode());
            donor.setCreatedDate(LocalDateTime.now());
            donor.setCreatedBy(authUtil.getCurrentUser().getFirstName());

            return bloodDonorRepository.save(donor);
        }catch (Exception ex){
            throw new DonorSaveException("Failed to save donor details", ex);
        }
    }
    @Transactional
    public BloodDonorScreening saveDonorScreeningDetails(BloodDonorScreeningRequest donorScreeningRequest,BloodDonor donor){
        try {
            BloodDonorScreening screening = new BloodDonorScreening();
            screening.setDonor(donor);
            screening.setScreeningDate(LocalDate.now());
            screening.setHemoglobin(donorScreeningRequest.getHemoglobin());
            screening.setWeightKg(donorScreeningRequest.getWeightKg());
            screening.setHeightCm(donorScreeningRequest.getHeightCm());
            screening.setBloodPressure(donorScreeningRequest.getBloodPressure());
            screening.setPulseRate(donorScreeningRequest.getPulseRate());
            screening.setTemperature(donorScreeningRequest.getTemperature());
            if(donorScreeningRequest.getScreeningResult().equalsIgnoreCase(AppConstants.DONOR_SCREENING_STATUS_PASS)){
                screening.setScreeningResult(donorScreeningRequest.getScreeningResult().toLowerCase());
                donor.setDonorScreeningStatus(donorScreeningRequest.getScreeningResult().toLowerCase());
                screening.setDeferralType(null);
                screening.setDeferralReason(null);
                donor.setCurrentDeferralReason(null);
                donor.setDeferralUptoDate(null);


            }else if(donorScreeningRequest.getScreeningResult().equalsIgnoreCase(AppConstants.DONOR_SCREENING_STATUS_FAIL)){

                screening.setScreeningResult(donorScreeningRequest.getScreeningResult().toLowerCase());
                donor.setDonorScreeningStatus(donorScreeningRequest.getScreeningResult().toLowerCase());
               donor.setCurrentDeferralReason(donorScreeningRequest.getDeferralReason());
               donor.setDeferralUptoDate(LocalDate.now());
                screening.setDeferralType(donorScreeningRequest.getDeferralType().toLowerCase());
                screening.setDeferralReason(donorScreeningRequest.getDeferralReason().toLowerCase());
                screening.setDeferralUptoDate(LocalDate.now());
                screening.setDeferralReason(donorScreeningRequest.getDeferralReason());


            }
            screening.setCreatedDate(LocalDateTime.now());
            screening.setCreatedBy(authUtil.getCurrentUser().getFirstName());
            return bloodDonorScreeningRepository.save(screening);
        }catch (Exception ex){
            throw new ScreeningSaveException("Failed to save screening details", ex);
        }
    }

    private BloodDonorScreeningDetailsResponse mapToResponse(BloodDonor donor , BloodDonorScreening screening) {
        BloodDonorScreeningDetailsResponse response = new BloodDonorScreeningDetailsResponse();
        response.setDonorId(donor.getDonorId());
        response.setDonorCode(donor.getDonorCode());
        response.setFirstName(donor.getFirstName());
        response.setLastName(donor.getLastName());
        response.setMobileNo(donor.getMobileNo());
        return response;
    }

    private void updateDonorDetails(BloodDonor donor, BloodDonorPersonalDetailsRequest pd) {

        donor.setFirstName(pd.getFirstName());
        donor.setLastName(pd.getLastName());
        donor.setDateOfBirth(pd.getDateOfBirth());
        donor.setMobileNo(pd.getMobileNo());
//        if (pd.getDonationTypeId() != null) {
//            MasBloodDonationType type = masBloodDonationTypeRepository.getReferenceById(pd.getDonationTypeId());
//            donor.setDonationType(type);
//        }
        if (pd.getRelationId() != null) {
            donor.setRelation(masRelationRepository.getReferenceById(pd.getRelationId()));

        }
        if (pd.getCountryId() != null) {
            donor.setCountry(masCountryRepository.getReferenceById(pd.getCountryId()));

        }
        if (pd.getStateId() != null) {
            donor.setState(masStateRepository.getReferenceById(pd.getStateId()));

        }
        if (pd.getDistrictId() != null) {
            donor.setDistrict(masDistrictRepository.getReferenceById(pd.getDistrictId()));
        }
        if (pd.getGenderId() != null) {
            donor.setGender(masGenderRepository.getReferenceById(pd.getGenderId()));
        }
        if (pd.getBloodGroupId() != null) {
            donor.setBloodGroup(masBloodGroupRepository.getReferenceById(pd.getBloodGroupId()));
        }
        donor.setAddressLine1(pd.getAddressLine1());
        donor.setAddressLine2(pd.getAddressLine2());
        donor.setCity(pd.getCity());
        donor.setPincode(pd.getPinCode());
        bloodDonorRepository.save(donor);
    }
    @Transactional
    private void createInventoryEntries(BloodDonationHdr hdr) {

        List<BloodDonationDt> components = bloodDonationDtRepository.findByDonationHdId(hdr);

        //Fetch status only once
        var availableStatus = masBloodDonationStatusRepository.findById(bloodDonationStatus_AVAILABLE).orElseThrow(() -> new RuntimeException("Status not found"));

        for (BloodDonationDt dt : components) {
            dt.setComponentStatus(availableStatus);

            BloodComponentInventory inventory = new BloodComponentInventory();

            inventory.setDonationDtId(dt);
            inventory.setUnitNo(dt.getUnitNo());
            inventory.setComponentId(dt.getComponentId());
            if (hdr.getDonorId() != null) {
                inventory.setBloodGroupId(hdr.getDonorId().getBloodGroup());
            }
            inventory.setVolumeMl(dt.getVolumeMl());
            inventory.setExpiryDate(dt.getExpiryDate());
            inventory.setInventoryStatus(availableStatus);
            inventory.setCreatedDate(LocalDateTime.now());
            inventory.setCreatedBy(authUtil.getCurrentUser().getFullName());

            bloodDonationDtRepository.save(dt);
            bloodComponentInventoryRepository.save(inventory);
        }
    }
    @Transactional
    public void uploadMultipleDocs(BloodDonationHdr bloodDonationHdr, List<MultipartFile> files) {

        BloodDonationHdr hdr = bloodDonationHdrRepository.findById(bloodDonationHdr.getDonationId()).orElseThrow();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            try {
                String uploadDir = "uploads/";
                Files.createDirectories(Paths.get(uploadDir));

                String originalName = file.getOriginalFilename();
                String filePath = uploadDir + originalName;

                Path path = Paths.get(filePath);
                Files.write(path, file.getBytes());
                String mimeType = file.getContentType();
                BloodDonationInvestigationDoc doc = new BloodDonationInvestigationDoc();
                doc.setDonation(hdr);
                doc.setFileName(originalName);
                doc.setFilePath(filePath);
                doc.setDocType(mimeType);
                doc.setUploadedDate(LocalDateTime.now());
                doc.setUploadedBy(authUtil.getCurrentUser().getFullName());

                bloodDonationInvestigationDocRepository.save(doc);

            } catch (IOException e) {
                throw new RuntimeException("File upload failed", e);
            }
        }
    }
}
