package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.RecordNotFoundException;
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
    @Autowired
    private MasBloodInventoryStatusRepository masBloodInventoryStatusRepository;

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

    @Value("${donor.screening.temp-fail.cooldown-days}")
    private int tempFailDays;

    @Value("${donor.screening.pass.cooldown-days}")
    private int passDays;

    @Value("${inventoryStatusAvailable}")
    private Long inventoryStatusAvailable;

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
                    AppConstants.DONOR_ALREADY_REGISTERED_MSG, HttpStatus.BAD_REQUEST.value()
            );
        }
        log.info("Saving donor personal details");
        BloodDonor donor = saveDonorDetails(request.getBloodDonorPersonalDetailsRequest());
        log.info("Donor personal details saved successfully with donorId: {}", donor.getDonorId());

        log.info("Saving donor screening details");
        BloodDonorScreening screening = saveDonorScreeningDetails(request.getBloodDonorScreeningRequest(), donor);
        log.info("Donor screening details saved successfully with screeningId: {}", screening.getScreeningId());

        log.info("Donor registration completed successfully");
        return ResponseUtils.createSuccessResponse(AppConstants.DONOR_REGISTRATION_SUCCESS_MSG, new TypeReference<>() {});

    }


    @Transactional
    @Override
    public ApiResponse<String> updateDonor(Long donorId, DonorRegistrationRequest request) {
        BloodDonor donor = bloodDonorRepository.findById(donorId).orElseThrow(() -> new DonorSaveException(AppConstants.DONOR_NOT_FOUND_ERR_MSG));

        updateDonorDetails(donor,request.getBloodDonorPersonalDetailsRequest());

        BloodDonorScreening screening = saveDonorScreeningDetails(request.getBloodDonorScreeningRequest(), donor);

        BloodDonorScreeningDetailsResponse response = mapToResponse(donor, screening);

        return ResponseUtils.createSuccessResponse(AppConstants.DONOR_UPDATE_AND_SCREENING_SUCCESS_MSG, new TypeReference<>() {});
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<DonorResponse>> getAllDonor(Long hospitalId,Pageable pageable, String donorName, String mobileNo) {
        try {

            Page<DonorProjection> projections = bloodDonorRepository.getAllDonor(hospitalId,pageable, donorName, mobileNo);

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

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error occurred while fetching donor list. donorName: {}, mobileNo: {}", donorName, mobileNo, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<BloodDonorScreeningDetailsResponse> getDonorScreeningDetails(Long donorId,Long hospitalId) {
        log.info("Fetching donor screening details for donorId: {}", donorId);

        try {
            BloodDonorDetailsProjection donor = bloodDonorScreeningRepository.getDonorBasicDetails(donorId,hospitalId);

            List<BloodDonorPreviousScreeningProjection> screeningProjections = bloodDonorScreeningRepository.getDonorPreviousScreenings(donorId,hospitalId);

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

            LocalDate nextEligibleDate = null;
            boolean eligible = true;

            if (!screeningList.isEmpty()) {

                BloodDonorPriviousScreening latest = screeningList.get(0);

                if (AppConstants.DONOR_SCREENING_STATUS_FAIL
                        .equalsIgnoreCase(latest.getScreeningResult())
                        && AppConstants.DONOR_SCREENING_PERMANENTLY_DEFERRED
                        .equalsIgnoreCase(latest.getDeferralType())) {

                    eligible = false;
                }
                else if (AppConstants.DONOR_SCREENING_STATUS_FAIL
                        .equalsIgnoreCase(latest.getScreeningResult())
                        && AppConstants.DONOR_SCREENING_TEMPORARILY_DEFERRED
                        .equalsIgnoreCase(latest.getDeferralType())) {

                    nextEligibleDate = latest.getScreeningDate().plusDays(tempFailDays);
                    eligible = nextEligibleDate.isBefore(LocalDate.now());

                } else if (AppConstants.DONOR_SCREENING_STATUS_PASS
                        .equalsIgnoreCase(latest.getScreeningResult())) {

                    nextEligibleDate = latest.getScreeningDate().plusDays(passDays);
                    eligible = nextEligibleDate.isBefore(LocalDate.now());
                }
            }

            response.setEligibleForDonation(eligible);
            response.setNextEligibleDonationDate(nextEligibleDate);
            response.setBloodDonorPreviousScreenings(screeningList);

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
    public ApiResponse<List<BloodDonorCollectionResponse>> pendingBloodCollection(Long hospitalId) {
        log.info("Fetching pending blood collection donors");

        try {
            List<BloodDonorCollectionProjection> projections = bloodDonorRepository.findPendingBloodCollection(AppConstants.DONOR_SCREENING_STATUS_PASS,hospitalId);

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
    public ApiResponse<BloodDonorCollectionDetailsResponse> pendingBloodCollectionDetails(Long donorId,Long hospitalId) {
        log.info("Fetching pending blood collection details for donorId: {}", donorId);

        try {
            Optional<BloodDonorCollectionDetailsProjection> optional = bloodDonorRepository.findPendingBloodCollectionDetails(donorId,hospitalId);

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
        bloodDonationHdr.setDonorId(bloodDonorRepository.findById(bloodCollectionRequest.getDonorId()).orElseThrow(()-> new RecordNotFoundException(AppConstants.DONOR_ID_NOT_FOUND_ERR_MSG)));
        bloodDonationHdr.setScreeningId(bloodDonorScreeningRepository.findById(bloodCollectionRequest.getScreeningId()).orElseThrow(()-> new RecordNotFoundException(AppConstants.SCREENING_ID_NOT_FOUND_ERR_MSG)));
        bloodDonationHdr.setDonationTypeId(masBloodDonationTypeRepository.findById(bloodCollectionRequest.getDonationTypeId()).orElseThrow(()-> new RecordNotFoundException(AppConstants.DONATION_TYPE_NOT_FOUND_ERR_MSG)));
        bloodDonationHdr.setBagNumber(generateBagNumber());
        bloodDonationHdr.setCollectionTypeId(bloodCollectionTypeRepository.findById(bloodCollectionRequest.getCollectionTypeId()).orElseThrow(()-> new RecordNotFoundException(AppConstants.COLLECTION_TYPE_NOT_FOUND_ERR_MSG)));
        bloodDonationHdr.setBagTypeId(masBloodBagTypeRepository.findById(bloodCollectionRequest.getBagTypeId()).orElseThrow(()-> new RecordNotFoundException(AppConstants.BAG_TYPE_NOT_FOUND_ERR_MSG)));
        bloodDonationHdr.setTotalCollectedVolumeMl(bloodCollectionRequest.getTotalCollectedVolume());
        bloodDonationHdr.setCreatedDate(LocalDate.now());
        bloodDonationHdr.setCreatedBy(authUtil.getCurrentUser().getFullName());
        bloodDonationHdr.setDonationDatetime(LocalDateTime.now());
        bloodDonationHdr.setHospital(authUtil.getCurrentUser().getHospital());
            bloodDonationHdr.setDonationStatusId(masBloodDonationStatusRepository.findById(bloodDonationStatusCollected).orElseThrow());

            bloodDonationHdrRepository.save(bloodDonationHdr);

            log.info("Blood collection saved successfully with bagNumber: {}", bloodDonationHdr.getBagNumber());

        return ResponseUtils.createSuccessResponse(AppConstants.BLOOD_COLLECTION_SAVE_SUCCESS_MSG, new TypeReference<>() {} );
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
    public ApiResponse<List<PendingComponentGenerationResponse>> pendingComponentGenerationList(Long hospitalId) {
        log.info("Fetching pending component generation list from repository");
        try {
            List<PendingComponentGenerationResponse> pendingComponentGenerationList = bloodDonationHdrRepository.pendingComponentGenerationList(bloodDonationStatusCollected,hospitalId);

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
                    .orElseThrow(() -> new RecordNotFoundException(AppConstants.BLOOD_DONATION_NOT_FOUND_ERR_MSG + donationId));

            MasComponentFailureReason failureReason = masComponentFailureReasonRepository.findById(componentFailureReasonId)
                    .orElseThrow(() -> new RecordNotFoundException(AppConstants.COMPONENT_FAILURE_REASON_NOT_FOUND_ERR_MSG + componentFailureReasonId));

            bloodDonationHdr.setComponentFailureReason(failureReason);
            bloodDonationHdr.setDonationStatusId(masBloodDonationStatusRepository.findById(bloodDonationStatusComponent_Failed).orElseThrow());
            bloodDonationHdrRepository.save(bloodDonationHdr);
            log.info("Component failure reason updated successfully for donationId: {}", donationId);

            return ResponseUtils.createSuccessResponse(AppConstants.COMPONENT_FAILURE_REASON_UPDATE_SUCCESS_MSG, new TypeReference<>() {});

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
                    .orElseThrow(() -> new RecordNotFoundException(AppConstants.BLOOD_DONATION_NOT_FOUND_ERR_MSG + request.getDonationId()));

            List<BloodDonationDt> donationDtList = new ArrayList<>();
            for (ComponentGenerationRequest row : request.getComponents()) {

                MasBloodComponent component = masBloodComponentRepository.findById(row.getComponentId())
                        .orElseThrow(() -> new RecordNotFoundException(AppConstants.COMPONENT_NOT_FOUND_ERR_MSG + row.getComponentId()));

                BloodDonationDt dt = new BloodDonationDt();
                dt.setDonationHdId(donationHdr);
                dt.setComponentId(component);
                dt.setUnitNo(row.getUnitNo().trim());
                dt.setVolumeMl(row.getVolumeMl());
                dt.setExpiryDate(row.getExpiryDate());
                dt.setCreatedDate(LocalDateTime.now());
                dt.setCreatedBy(authUtil.getCurrentUser().getFullName());
                dt.setHospital(authUtil.getCurrentUser().getHospital());
                donationDtList.add(dt);
            }

            bloodDonationDtRepository.saveAll(donationDtList);

            donationHdr.setComponentGenerationDatetime(LocalDateTime.now());
            donationHdr.setDonationStatusId(masBloodDonationStatusRepository.findById(bloodDonationStatusComponent_Generated).orElseThrow());
            bloodDonationHdrRepository.save(donationHdr);
            log.info("Component generation saved successfully for donationId: {}", request.getDonationId());
            return ResponseUtils.createSuccessResponse(AppConstants.COMPONENT_GENERATION_SAVE_SUCCESS_MSG, new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error while saving component generation for donationId: {}", request.getDonationId(), e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<PendingForMandatoryTestingResponse>> pendingForMandatoryTestingList(Long hospitalId) {
        try {
            List<PendingForMandatoryTestingProjection> projectionList =
                    bloodDonationHdrRepository
                            .pendingForMandatoryTestingList(bloodDonationStatusComponent_Generated,hospitalId);

            List<PendingForMandatoryTestingResponse> responseList =
                    projectionList.stream()
                            .map(p -> new PendingForMandatoryTestingResponse(
                                    p.getDonationId(),
                                    p.getDonorId(),
                                    p.getBagNumber(),
                                    p.getDonorResNo(),
                                    p.getFullName(),
                                    p.getBloodGroup(),
                                    p.getCollectionDateTime(),
                                    p.getCollectionType(),
                                    p.getNoOfComponent(),
                                    p.getCurrentStatus(),
                                    p.getBagType(),
                                    p.getComponentGenerationDateTime()
                            ))
                            .toList();

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while fetching pending mandatory testing list", e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
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
            entity.setHospital(authUtil.getCurrentUser().getHospital());

            bloodDonationTestResultRepository.save(entity);

            if (AppConstants.REACTIVE.equalsIgnoreCase(dto.getResult())) {
                isFailed = true;
            }
        }

        uploadMultipleDocs(bloodDonationHdr, files);

        if (isFailed) {
            var availableStatus = masBloodDonationStatusRepository.findById(bloodDonationStatus_TEST_FAILED).orElseThrow(() -> new RecordNotFoundException(AppConstants.STATUS_NOT_FOUND_ERR_MSG));
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
                AppConstants.MANDATORY_TEST_ENTRY_SUCCESS_MSG, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<?> getBloodStock(BloodStockFilterRequest req) {

        if (AppConstants.SUMMARY.equalsIgnoreCase(req.getViewType())) {
            List<BloodStockSummaryProjection> list = bloodComponentInventoryRepository.getSummary(
                            req.getBloodGroupId(),
                            req.getComponentId(),
                            req.getInventoryStatus(),
                            req.getCollectionType(),
                            req.getExpiryFilter(),
                    req.getHospitalId(),
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
            donor.setHospital(authUtil.getCurrentUser().getHospital());

            return bloodDonorRepository.save(donor);
        }catch (Exception ex){
            throw new DonorSaveException(AppConstants.DONOR_SAVE_FAILED_ERR_MSG, ex);
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
            screening.setHospital(authUtil.getCurrentUser().getHospital());
            return bloodDonorScreeningRepository.save(screening);
        }catch (Exception ex){
            ex.printStackTrace();
            throw ex;
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
        var availableStatus = masBloodDonationStatusRepository.findById(bloodDonationStatus_AVAILABLE).orElseThrow(() -> new RecordNotFoundException(AppConstants.STATUS_NOT_FOUND_ERR_MSG));

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
            inventory.setInventoryStatus(masBloodInventoryStatusRepository.findById(inventoryStatusAvailable).orElseThrow());
            inventory.setCreatedDate(LocalDateTime.now());
            inventory.setCreatedBy(authUtil.getCurrentUser().getFullName());
            inventory.setHospital(authUtil.getCurrentUser().getHospital());

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
                throw new RuntimeException(AppConstants.FILE_UPLOAD_FAILED_ERR_MSG, e);
            }
        }
    }
}
