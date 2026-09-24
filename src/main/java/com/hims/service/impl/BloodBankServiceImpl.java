package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.projection.BloodAllocatedProjection;
import com.hims.entity.projection.BloodIssueProjection;
import com.hims.entity.repository.*;
import com.hims.exception.RecordNotFoundException;
import com.hims.exception.SDDException;
import com.hims.exception.bloodBankException.DonorSaveException;
import com.hims.projection.*;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.BloodBankService;
import com.hims.service.TransactionSequenceService;
import com.hims.service.UserContextService;
import com.hims.utils.AuthUtil;
import com.hims.utils.DateTimeUtil;
import com.hims.utils.HMISTransaction;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BloodBankServiceImpl implements BloodBankService {
    @Autowired
    BloodDonationDtRepository bloodDonationDtRepository;
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
    private UserContextService userContextService;
    @Autowired
    private BloodDonorScreeningRepository bloodDonorScreeningRepository;
    @Autowired
    private MasRelationRepository masRelationRepository;
    @Autowired
    private BloodDonationHdrRepository bloodDonationHdrRepository;
    @Autowired
    private MasBloodCollectionTypeRepository bloodCollectionTypeRepository;
    @Autowired
    private MasBloodBagTypeRepository masBloodBagTypeRepository;
    @Autowired
    private MasBloodDonationStatusRepository masBloodDonationStatusRepository;
    @Autowired
    private MasBloodInventoryStatusRepository masBloodInventoryStatusRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private InpatientRepository inpatientRepository;
    @Autowired
    private MasDepartmentRepository masDepartmentRepository;
    @Autowired
    private TransactionSequenceService transactionSequenceService;
    @Autowired
    private BloodRequestDtAllocationRepository bloodRequestDtAllocationRepository;
    @Autowired
    private MasWardRepository masWardRepository;
    @Value("${bloodDonationStatusCollected}")
    private Long bloodDonationStatusCollected;
    @Value("${bloodDonationStatusComponent_Failed}")
    private Long bloodDonationStatusComponent_Failed;
    @Value("${bloodDonationStatusComponent_Generated}")
    private Long bloodDonationStatusComponent_Generated;
    @Value("${bloodDonationStatus_TEST_FAILED}")
    private Long bloodDonationStatus_TEST_FAILED;
    @Value("${bloodDonationStatus_AVAILABLE}")
    private Long bloodDonationStatus_AVAILABLE;
    @Value("${donor.screening.temp-fail.cooldown-days}")
    private int tempFailDays;
    @Value("${donor.screening.pass.cooldown-days}")
    private int passDays;
    @Value("${inventoryStatusAvailable}")
    private Long inventoryStatusAvailable;
    @Value(("${inventoryStatusAllocated}"))
    private Long inventoryStatusAllocated;
    @Value("${blood.request.status.requested}")
    private Long requestedStatusId;
    @Value("${blood.request.status.allocated}")
    private Long allocatedStatusId;
    @Value("${blood.request.status.partial.allocated}")
    private Long partiallyAllocatedStatusId;
    @Value("${blood.request.status.component.reserved}")
    private Long componentReservedStatusId;
    @Value("${blood.request.status.crossmatch.failed}")
    private Long crossmatchFailedStatusId;
    @Value("${inventoryStatusReserved}")
    private Long inventoryStatusReserved;

    @Value("${inventoryStatusIssued}")
    private Long inventoryStatusIssued;

    @Value("${blood.request.status.rejected}")
    private Long bloodRequestStatusRejected;

    @Value("${blood.request.status.issued}")
    private Long bloodRequestStatusIssued;

    @Autowired
    private BloodComponentInventoryRepository bloodComponentInventoryRepository;
    @Autowired
    private MasComponentFailureReasonRepository masComponentFailureReasonRepository;
    @Autowired
    private MasBloodComponentRepository masBloodComponentRepository;
    @Autowired
    private BloodDonationTestResultRepository bloodDonationTestResultRepository;
    @Autowired
    private MasBloodTestRepository masBloodTestRepository;
    @Autowired
    private BloodDonationInvestigationDocRepository bloodDonationInvestigationDocRepository;
    @Autowired
    private BloodRequestHdRepository bloodRequestHdRepository;
    @Autowired
    private BloodRequestDtRepository bloodRequestDtRepository;
    @Autowired
    private MasBloodComponentRepository bloodComponentRepository;
    @Autowired
    private MasHospitalRepository masHospitalRepository;

    @Autowired
    private BloodTrackingStatusMasterRepository bloodTrackingStatusMasterRepository;

    @Autowired
    private MasCrossMatchTypeRepository  masCrossMatchTypeRepository;

    @Autowired
    private BloodCrossmatchHdRepository  bloodCrossmatchHdRepository;

    @Autowired
    private BloodCrossmatchDtRepository  bloodCrossmatchDtRepository;

    @Autowired
    private BloodCrossmatchFailedHistoryRepository bloodCrossmatchFailedHistoryRepository;


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
    public ApiResponse<String> registerDonor(DonorRegistrationRequest request) {
        log.info("Starting donor registration process");

        BloodDonorPersonalDetailsRequest pd = request.getBloodDonorPersonalDetailsRequest();
        boolean exists = bloodDonorRepository.existsDonorByDetails(
                pd.getMobileNo(),
                pd.getFirstName(),
                pd.getDateOfBirth(),
                pd.getRelationId(),
                pd.getBloodGroupId());

        if (exists) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
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
        return ResponseUtils.createSuccessResponse(AppConstants.DONOR_REGISTRATION_SUCCESS_MSG, new TypeReference<>() {
        });

    }


    @Transactional
    @Override
    public ApiResponse<String> updateDonor(Long donorId, DonorRegistrationRequest request) {
        BloodDonor donor = bloodDonorRepository.findById(donorId).orElseThrow(() -> new DonorSaveException(AppConstants.DONOR_NOT_FOUND_ERR_MSG));

        updateDonorDetails(donor, request.getBloodDonorPersonalDetailsRequest());

        BloodDonorScreening screening = saveDonorScreeningDetails(request.getBloodDonorScreeningRequest(), donor);

        BloodDonorScreeningDetailsResponse response = mapToResponse(donor, screening);

        return ResponseUtils.createSuccessResponse(AppConstants.DONOR_UPDATE_AND_SCREENING_SUCCESS_MSG, new TypeReference<>() {
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<DonorResponse>> getAllDonor(Long hospitalId, Pageable pageable, String donorName, String mobileNo) {
        try {

            Page<DonorProjection> projections = bloodDonorRepository.getAllDonor(hospitalId, pageable, donorName, mobileNo);

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

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error occurred while fetching donor list. donorName: {}, mobileNo: {}", donorName, mobileNo, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<BloodDonorScreeningDetailsResponse> getDonorScreeningDetails(Long donorId, Long hospitalId) {
        log.info("Fetching donor screening details for donorId: {}", donorId);

        try {
            BloodDonorDetailsProjection donor = bloodDonorScreeningRepository.getDonorBasicDetails(donorId, hospitalId);

            List<BloodDonorPreviousScreeningProjection> screeningProjections = bloodDonorScreeningRepository.getDonorPreviousScreenings(donorId, hospitalId);

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
                } else if (AppConstants.DONOR_SCREENING_STATUS_FAIL
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

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error while fetching donor screening details for donorId: {}", donorId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<BloodDonorCollectionResponse>> pendingBloodCollection(Long hospitalId) {
        log.info("Fetching pending blood collection donors");

        try {
            List<BloodDonorCollectionProjection> projections = bloodDonorRepository.findPendingBloodCollection(AppConstants.DONOR_SCREENING_STATUS_PASS, hospitalId);

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
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error while fetching pending blood collection donors", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INSUFFICIENT_STORAGE.value()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<BloodDonorCollectionDetailsResponse> pendingBloodCollectionDetails(Long donorId, Long hospitalId) {
        log.info("Fetching pending blood collection details for donorId: {}", donorId);

        try {
            Optional<BloodDonorCollectionDetailsProjection> optional = bloodDonorRepository.findPendingBloodCollectionDetails(donorId, hospitalId);

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

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error while fetching pending blood collection details for donorId: {}", donorId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
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

            BloodDonationHdr bloodDonationHdr = new BloodDonationHdr();
            bloodDonationHdr.setDonorId(bloodDonorRepository.findById(bloodCollectionRequest.getDonorId()).orElseThrow(() -> new RecordNotFoundException(AppConstants.DONOR_ID_NOT_FOUND_ERR_MSG)));
            bloodDonationHdr.setScreeningId(bloodDonorScreeningRepository.findById(bloodCollectionRequest.getScreeningId()).orElseThrow(() -> new RecordNotFoundException(AppConstants.SCREENING_ID_NOT_FOUND_ERR_MSG)));
            bloodDonationHdr.setDonationTypeId(masBloodDonationTypeRepository.findById(bloodCollectionRequest.getDonationTypeId()).orElseThrow(() -> new RecordNotFoundException(AppConstants.DONATION_TYPE_NOT_FOUND_ERR_MSG)));
            bloodDonationHdr.setBagNumber(generateBagNumber());
            bloodDonationHdr.setCollectionTypeId(bloodCollectionTypeRepository.findById(bloodCollectionRequest.getCollectionTypeId()).orElseThrow(() -> new RecordNotFoundException(AppConstants.COLLECTION_TYPE_NOT_FOUND_ERR_MSG)));
            bloodDonationHdr.setBagTypeId(masBloodBagTypeRepository.findById(bloodCollectionRequest.getBagTypeId()).orElseThrow(() -> new RecordNotFoundException(AppConstants.BAG_TYPE_NOT_FOUND_ERR_MSG)));
            bloodDonationHdr.setTotalCollectedVolumeMl(bloodCollectionRequest.getTotalCollectedVolume());
            bloodDonationHdr.setCreatedDate(LocalDate.now());
            bloodDonationHdr.setCreatedBy(userContextService.getCurrentUserContext().getUserFullName());
            bloodDonationHdr.setDonationDatetime(LocalDateTime.now());
            bloodDonationHdr.setHospital(masHospitalRepository.findById(userContextService.getCurrentUserContext().getHospitalId()).orElseThrow(() -> new RecordNotFoundException("Hospital Not Found")));
            bloodDonationHdr.setDonationStatusId(masBloodDonationStatusRepository.findById(bloodDonationStatusCollected).orElseThrow());

            bloodDonationHdrRepository.save(bloodDonationHdr);

            log.info("Blood collection saved successfully with bagNumber: {}", bloodDonationHdr.getBagNumber());

            return ResponseUtils.createSuccessResponse(AppConstants.BLOOD_COLLECTION_SAVE_SUCCESS_MSG, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Unexpected exception occurred while saving blood collection", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
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
            List<PendingComponentGenerationResponse> pendingComponentGenerationList = bloodDonationHdrRepository.pendingComponentGenerationList(bloodDonationStatusCollected, hospitalId);

            log.info("Pending component generation list fetched successfully. Total records: {}",
                    pendingComponentGenerationList != null ? pendingComponentGenerationList.size() : 0);


            return ResponseUtils.createSuccessResponse(pendingComponentGenerationList, new TypeReference<>() {
                    }
            );

        } catch (Exception e) {
            log.error("Exception occurred while fetching pending component generation list", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
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

            return ResponseUtils.createSuccessResponse(AppConstants.COMPONENT_FAILURE_REASON_UPDATE_SUCCESS_MSG, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error while failing component generation for donationId: {}", donationId, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, AppConstants.INTERNAL_SERVER_ERR_MSG,
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
                dt.setCreatedBy(userContextService.getCurrentUserContext().getUserFullName());
                dt.setHospital(masHospitalRepository.findById(userContextService.getCurrentUserContext().getHospitalId()).orElseThrow(() -> new RecordNotFoundException("Hospital Not Found")));
                donationDtList.add(dt);
            }

            bloodDonationDtRepository.saveAll(donationDtList);

            donationHdr.setComponentGenerationDatetime(LocalDateTime.now());
            donationHdr.setDonationStatusId(masBloodDonationStatusRepository.findById(bloodDonationStatusComponent_Generated).orElseThrow());
            bloodDonationHdrRepository.save(donationHdr);
            log.info("Component generation saved successfully for donationId: {}", request.getDonationId());
            return ResponseUtils.createSuccessResponse(AppConstants.COMPONENT_GENERATION_SAVE_SUCCESS_MSG, new TypeReference<>() {
                    }
            );

        } catch (Exception e) {
            log.error("Error while saving component generation for donationId: {}", request.getDonationId(), e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<PendingForMandatoryTestingResponse>> pendingForMandatoryTestingList(Long hospitalId) {
        try {
            List<PendingForMandatoryTestingProjection> projectionList =
                    bloodDonationHdrRepository
                            .pendingForMandatoryTestingList(bloodDonationStatusComponent_Generated, hospitalId);

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

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error while fetching pending mandatory testing list", e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {
                    },
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
            entity.setCreatedBy(userContextService.getCurrentUserContext().getUserFullName());
            entity.setHospital(masHospitalRepository.findById(userContextService.getCurrentUserContext().getHospitalId()).orElseThrow(() -> new RecordNotFoundException("Hospital Not Found")));

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
                AppConstants.MANDATORY_TEST_ENTRY_SUCCESS_MSG, new TypeReference<>() {
                });
    }

    @Override
    public ApiResponse<?> getBloodStock(BloodStockFilterRequest req, Pageable pageable) {

        if (AppConstants.SUMMARY.equalsIgnoreCase(req.getViewType())) {

            Page<BloodStockSummaryProjection> page =
                    bloodComponentInventoryRepository.getSummary(
                            req.getBloodGroupId(),
                            req.getComponentId(),
                            req.getInventoryStatus(),
                            req.getCollectionType(),
                            req.getExpiryFilter(),
                            req.getHospitalId(),
                            AppConstants.COMPONENT_CRYO.toLowerCase(),
                            AppConstants.COMPONENT_PLASMA.toLowerCase(),
                            AppConstants.COMPONENT_PLT.toLowerCase(),
                            AppConstants.COMPONENT_PRBC.toLowerCase(),
                            pageable
                    );

            return ResponseUtils.createSuccessResponse(
                    page,
                    new TypeReference<>() {}
            );

        } else {
            Page<BloodStockDetailedProjection> list = bloodComponentInventoryRepository.getDetailed(
                    req.getBloodGroupId(),
                    req.getComponentId(),
                    req.getInventoryStatus(),
                    req.getExpiryFilter(),
                    userContextService.getCurrentUserContext().getHospitalId(),
                    pageable);
            return ResponseUtils.createSuccessResponse(list, new TypeReference<>() {
            });
        }
    }

    @Transactional
    public BloodDonor saveDonorDetails(BloodDonorPersonalDetailsRequest personalDetailsRequest) {
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
            donor.setCreatedBy(userContextService.getCurrentUserContext().getUserFullName());
            donor.setHospital(masHospitalRepository.findById(userContextService.getCurrentUserContext().getHospitalId()).orElseThrow(() -> new RecordNotFoundException("Hospital Not Found")));

            return bloodDonorRepository.save(donor);
        } catch (Exception ex) {
            throw new DonorSaveException(AppConstants.DONOR_SAVE_FAILED_ERR_MSG, ex);
        }
    }

    @Transactional
    public BloodDonorScreening saveDonorScreeningDetails(BloodDonorScreeningRequest donorScreeningRequest, BloodDonor donor) {
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
            if (donorScreeningRequest.getScreeningResult().equalsIgnoreCase(AppConstants.DONOR_SCREENING_STATUS_PASS)) {
                screening.setScreeningResult(donorScreeningRequest.getScreeningResult().toLowerCase());
                donor.setDonorScreeningStatus(donorScreeningRequest.getScreeningResult().toLowerCase());
                screening.setDeferralType(null);
                screening.setDeferralReason(null);
                donor.setCurrentDeferralReason(null);
                donor.setDeferralUptoDate(null);


            } else if (donorScreeningRequest.getScreeningResult().equalsIgnoreCase(AppConstants.DONOR_SCREENING_STATUS_FAIL)) {

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
            screening.setCreatedBy(userContextService.getCurrentUserContext().getUserFullName());
            screening.setHospital(masHospitalRepository.findById(userContextService.getCurrentUserContext().getHospitalId()).orElseThrow(() -> new RecordNotFoundException("Hospital Not Found")));
            return bloodDonorScreeningRepository.save(screening);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    private BloodDonorScreeningDetailsResponse mapToResponse(BloodDonor donor, BloodDonorScreening screening) {
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
            inventory.setCreatedBy(userContextService.getCurrentUserContext().getUserFullName());
            inventory.setHospital(masHospitalRepository.findById(userContextService.getCurrentUserContext().getHospitalId()).orElseThrow(() -> new RecordNotFoundException("Hospital Not Found")));

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
                doc.setUploadedBy(userContextService.getCurrentUserContext().getUserFullName());

                bloodDonationInvestigationDocRepository.save(doc);

            } catch (IOException e) {
                throw new RuntimeException(AppConstants.FILE_UPLOAD_FAILED_ERR_MSG, e);
            }
        }
    }


    @Override
    @Transactional
    public ApiResponse<String> createBloodRequest(BloodRequestRequest request) {
        try {
            String currentUser = userContextService.getCurrentUserContext().getUserFullName();

            BloodRequestHd bloodRequestHd = new BloodRequestHd();
            bloodRequestHd.setRequestNo(transactionSequenceService.generateTransactionNumber
                    (HMISTransaction.BLOOD_REQUEST_NO, userContextService.getCurrentUserContext().getHospitalId()));
            bloodRequestHd.setInpatient(inpatientRepository.findById(request.getInpatientId())
                    .orElseThrow(() -> new RecordNotFoundException("Inpatient not found")));
            bloodRequestHd.setPatient(patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RecordNotFoundException("Patient not found")));
            bloodRequestHd.setMasWard(masWardRepository.findById(request.getWardId())
                    .orElseThrow(() -> new RecordNotFoundException("Ward not found")));
            bloodRequestHd.setBloodGroup(masBloodGroupRepository.findById(request.getBloodGroupId())
                    .orElseThrow(() -> new RecordNotFoundException("Blood group not found")));
            bloodRequestHd.setRequestDatetime(LocalDateTime.now());
            bloodRequestHd.setRequestedBy(currentUser);
            bloodRequestHd.setOverallStatus(AppConstants.STATUS_N.toLowerCase());
            bloodRequestHd.setCreatedDate(LocalDateTime.now());
            bloodRequestHd.setCreatedBy(currentUser);

            BloodRequestHd savedHeader = bloodRequestHdRepository.save(bloodRequestHd);

            List<BloodRequestDt> details = new ArrayList<>();

            for (BloodRequirementDetailRequest detailRequest : request.getBloodRequirementDetails()) {
                MasBloodComponent component = bloodComponentRepository.findById(detailRequest.getComponentId())
                        .orElseThrow(() -> new RecordNotFoundException(
                                "Blood component not found: " + detailRequest.getComponentId()));

                BloodRequestDt detail = new BloodRequestDt();
                detail.setBloodRequestHd(savedHeader);
                detail.setComponent(component);
                detail.setUnitsRequired(detailRequest.getUnitsRequired());
                detail.setRequiredByDatetime(detailRequest.getRequiredDateTime());
                detail.setUrgency(detailRequest.getUrgency());
                detail.setClinicalIndication(detailRequest.getIndication());
                detail.setRemarks(detailRequest.getRemarks());
                detail.setFulfilledUnits(0);
                detail.setDetailStatus(AppConstants.STATUS_N.toLowerCase());
                detail.setCreatedDate(LocalDateTime.now());
                detail.setCreatedBy(currentUser);
                detail.setTrackingStatus(bloodTrackingStatusMasterRepository.findById(requestedStatusId).orElseThrow(() -> new RecordNotFoundException("Tracking status not found")));
                details.add(detail);
            }

            bloodRequestDtRepository.saveAll(details);

            return ResponseUtils.createSuccessResponse(
                    null,
                    new TypeReference<String>() {
                    },
                    "Blood request created successfully"
            );

        } catch (RecordNotFoundException e) {
            log.error("Blood request creation failed: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            return ResponseUtils.createFailureResponse(
                    null,
                    "Blood request creation failed: " + e.getMessage(),
                    HttpStatus.NOT_FOUND.value()
            );

        } catch (Exception e) {
            log.error("Unexpected error while creating blood request", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            return ResponseUtils.createFailureResponse(
                    null,
                    "Failed to create blood request. Please try again.",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<Page<BloodTrackingResponse>> getBloodRequestTrackingList(
            int page,
            int size,
            String inpatientNo,
            String patientName) {

        Pageable pageable = PageRequest.of(page, size);

        Page<BloodTrackingProjection> projectionPage =
                bloodRequestDtRepository.getBloodRequestTrackingList(
                        inpatientNo,
                        patientName,
                        pageable);

        Page<BloodTrackingResponse> responsePage = projectionPage.map(p -> {
            BloodTrackingResponse response = new BloodTrackingResponse();

            response.setRequestDtId(p.getRequestDtId());
            response.setRequestNo(p.getRequestNo());
            response.setInpatientId(p.getInpatientId());
            response.setInpatientNo(p.getInpatientNo());
            response.setPatientId(p.getPatientId());
            response.setPatientName(p.getPatientName());
            response.setBloodGroup(p.getBloodGroup());
            response.setComponent(p.getComponent());
            response.setBloodGroupId(p.getBloodGroupId());
            response.setComponentId(p.getComponentId());
            response.setUnits(p.getUnits());
            response.setUrgency(p.getUrgency());
            response.setRequestedDateTime(p.getRequestedDateTime());
            response.setRequiredByDateTime(p.getRequiredByDateTime());
            response.setRequestedWard(p.getRequestedWard());
            response.setTrackingStatus(p.getTrackingStatus());

            return response;
        });

        return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {
        });
    }


    @Override
    public ApiResponse<List<BloodInventoryResponse>> getAvailableInventory(BloodInventoryRequest request) {

        List<BloodInventoryProjection> inventoryList = bloodComponentInventoryRepository.findAvailableBloodInventory(
                request.getPatientBloodGroupId(),
                request.getComponentId(),
                AppConstants.STATUS_Y.toLowerCase(),
                inventoryStatusAvailable
        );

        List<BloodInventoryResponse> responseList = inventoryList.stream()
                .map(this::mapToBloodInventoryResponse)
                .toList();

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Available blood inventory fetched successfully",
                responseList
        );
    }

    private BloodInventoryResponse mapToBloodInventoryResponse(BloodInventoryProjection projection) {
        BloodInventoryResponse response = new BloodInventoryResponse();
        response.setInventoryId(projection.getInventoryId());
        response.setUnitNo(projection.getUnitNo());
        response.setBloodGroupId(projection.getBloodGroupId());
        response.setVolumeMl(projection.getVolumeMl());
        response.setExpiryDate(projection.getExpiryDate());
        response.setComponentId(projection.getComponentId());
        response.setCompatibility("Compatible");
        response.setStatus("Available");
        response.setPreferred(AppConstants.STATUS_Y.equalsIgnoreCase(projection.getPreferred()));

        return response;
    }


    @Override
    @Transactional
    public ApiResponse<String> allocateBloodUnits(BloodRequestAllocationRequest request) {

        try {
            String currentUser = userContextService.getCurrentUserContext().getUserFullName();
            int totalAllocated = 0;
            for (BloodRequestDetailAllocationRequest detailRequest : request.getDetails()) {

                BloodRequestDt requestDt = bloodRequestDtRepository.findById(detailRequest.getRequestDtId()
                ).orElseThrow(() ->
                        new RecordNotFoundException(
                                "Blood request detail not found: "
                                        + detailRequest.getRequestDtId()
                        ));

                int allocatedForDetail = 0;

                for (Long inventoryId : detailRequest.getInventoryIds()) {

                    BloodComponentInventory inventory =
                            bloodComponentInventoryRepository.findById(inventoryId)
                                    .orElseThrow(() ->
                                            new RecordNotFoundException(
                                                    "Blood inventory not found: "
                                                            + inventoryId
                                            ));

                    // Prevent duplicate allocation
                    boolean alreadyAllocated =
                            bloodRequestDtAllocationRepository
                                    .existsByBloodRequestDtAndInventory(
                                            requestDt,
                                            inventory
                                    );

                    if (alreadyAllocated) {
                        continue;
                    }

                    BloodRequestDtAllocation allocation = new BloodRequestDtAllocation();
                    allocation.setBloodRequestDt(requestDt);
                    allocation.setInventory(inventory);
                    allocation.setAllocatedUnits(1);
                    allocation.setAllocatedDate(LocalDateTime.now());
                    allocation.setCreatedBy(currentUser);
                    bloodRequestDtAllocationRepository.save(allocation);


                    inventory.setInventoryStatus(
                            masBloodInventoryStatusRepository
                                    .findById(inventoryStatusAllocated)
                                    .orElseThrow(() ->
                                            new RecordNotFoundException(
                                                    "Allocated inventory status not found"))
                    );
                    inventory.setReservationDatetime(LocalDateTime.now());

                    bloodComponentInventoryRepository.save(inventory);


                    allocatedForDetail++;
                    totalAllocated++;
                }

                // Update fulfilled units for this particular request detail
                int currentFulfilled = requestDt.getFulfilledUnits() == null ? 0 : requestDt.getFulfilledUnits();
                requestDt.setFulfilledUnits(currentFulfilled + allocatedForDetail);

                // Update detail status
                if (requestDt.getFulfilledUnits() >= requestDt.getUnitsRequired()) {
                    requestDt.setTrackingStatus(
                            bloodTrackingStatusMasterRepository
                                    .findById(allocatedStatusId)
                                    .orElseThrow(() ->
                                            new RecordNotFoundException(
                                                    "Allocated tracking status not found"))
                    );
                } else if (requestDt.getFulfilledUnits() > 0) {
                    requestDt.setTrackingStatus(
                            bloodTrackingStatusMasterRepository
                                    .findById(partiallyAllocatedStatusId)
                                    .orElseThrow(() ->
                                            new RecordNotFoundException(
                                                    "Partially allocated tracking status not found"))
                    );
                }

                bloodRequestDtRepository.save(requestDt);
            }

            return ResponseUtils.createSuccessResponse(
                    totalAllocated + " blood unit(s) allocated successfully",
                    new TypeReference<>() {
                    }
            );
        } catch (Exception e) {
            log.error("Error while allocating blood units", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseUtils.createFailureResponse(
                    null,
                    "Failed to allocate blood units",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }


    @Override
    public ApiResponse<Page<BloodTrackingResponse>> getAllPendingBloodRequest(
            int page,
            int size,
            String patientName,
            Long wardId) {

        Pageable pageable = PageRequest.of(page, size);

        Page<BloodTrackingProjection> projectionPage =
                bloodRequestDtRepository.getBloodRequestTrackingList(
                        patientName,
                        wardId,
                        requestedStatusId,
                        partiallyAllocatedStatusId,
                        pageable);

        Page<BloodTrackingResponse> responsePage = projectionPage.map(p -> {

            BloodTrackingResponse response = new BloodTrackingResponse();

            response.setRequestDtId(p.getRequestDtId());
            response.setRequestNo(p.getRequestNo());
            response.setInpatientId(p.getInpatientId());
            response.setInpatientNo(p.getInpatientNo());
            response.setPatientId(p.getPatientId());
            response.setPatientName(p.getPatientName());
            response.setBloodGroup(p.getBloodGroup());
            response.setComponent(p.getComponent());
            response.setBloodGroupId(p.getBloodGroupId());
            response.setComponentId(p.getComponentId());
            response.setUnits(p.getUnits());
            response.setUrgency(p.getUrgency());
            response.setRequestedDateTime(p.getRequestedDateTime());
            response.setRequiredByDateTime(p.getRequiredByDateTime());
            response.setRequestedWard(p.getRequestedWard());
            response.setTrackingStatus(p.getTrackingStatus());

            return response;
        });

        return ResponseUtils.createSuccessResponse(
                responsePage,
                new TypeReference<>() {
                });
    }
    @Override
    public ApiResponse<Page<BloodAllocatedResponse>> getAllocatedBloodRequestList(
            int page,
            int size,
            String patientName,
            Long wardId) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "requestDtId"));

        Page<BloodAllocatedProjection> projectionPage =
                bloodRequestDtAllocationRepository.getAllocatedBloodRequestList(
                        patientName,
                        wardId,
                        allocatedStatusId,
                        partiallyAllocatedStatusId,
                        pageable);
        Page<BloodAllocatedResponse> responsePage = projectionPage.map(projection -> {
                    BloodAllocatedResponse response = new BloodAllocatedResponse();
                    response.setRequestHdId(projection.getRequestHdId());
                    response.setRequestDtId(projection.getRequestDtId());
                    response.setRequestNo(projection.getRequestNo());
                    response.setInpatientId(projection.getInpatientId());
                    response.setInpatientNo(projection.getInpatientNo());
                    response.setPatientId(projection.getPatientId());
                    response.setPatientName(projection.getPatientName());
                    response.setBloodGroup(projection.getBloodGroup());
                    response.setComponent(projection.getComponent());
                    response.setUnitsRequired(projection.getUnitsRequired());
                    response.setUnitsAllocated(projection.getUnitsAllocated());
                    response.setWard(projection.getWard());
                    response.setUrgency(projection.getUrgency());
                    response.setRequestedOn(DateTimeUtil.formatDateTime(projection.getRequestedOn()));
                    response.setRequiredBy(DateTimeUtil.formatDateTime(projection.getRequiredBy()));
                    response.setTrackingStatusId(projection.getTrackingStatusId());
                    response.setAge(Period.between(projection.getDob(), LocalDate.now()).getYears());
                    response.setGender(projection.getGender());
                    response.setUnitExpiryDate(DateTimeUtil.formatDate(projection.getUnitExpiry()));
                    response.setUnitVolume(projection.getUnitVolume().toString());
                    response.setUnitNumber(projection.getUnitNumber());
                    response.setInventoryId(projection.getInventoryId());
                    return response;
                });

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Allocated blood request list fetched successfully",
                responsePage
        );
    }


    @Override
    @Transactional
    public ApiResponse<String> saveCrossmatch(BloodCrossmatchRequest request) {
        String currentUser=userContextService.getCurrentUserContext().getUserFullName();
        Long currentUserId=userContextService.getCurrentUserContext().getUserId();

        BloodRequestHd bloodRequest=bloodRequestHdRepository.findById(request.getRequestHdId())
                .orElseThrow(()->new SDDException(HttpStatus.NOT_FOUND.value(),"Blood request not found"));
        BloodRequestDt bloodRequestDt=bloodRequestDtRepository.findById(request.getRequestDtId())
                .orElseThrow(()->new SDDException(HttpStatus.NOT_FOUND.value(),"Blood request detail not found"));
        MasCrossMatchType crossmatchType=masCrossMatchTypeRepository.findById(request.getCrossmatchTypeId())
                .orElseThrow(()->new SDDException(HttpStatus.NOT_FOUND.value(),"Crossmatch type not found"));

        BloodCrossmatchHd crossmatchHd=new BloodCrossmatchHd();
        crossmatchHd.setBloodRequestHd(bloodRequest);
        crossmatchHd.setInpatient(bloodRequest.getInpatient());
        crossmatchHd.setPatient(bloodRequest.getPatient());
        crossmatchHd.setCrossmatchType(crossmatchType);
        crossmatchHd.setSampleReceivedDatetime(request.getSampleReceivedDatetime());
        crossmatchHd.setCrossmatchDatetime(request.getCrossmatchDatetime());
        crossmatchHd.setOverallResult(request.getOverallResult());
        crossmatchHd.setRemarks(request.getRemarks());
        crossmatchHd.setCreatedDate(LocalDateTime.now());
        crossmatchHd.setCreatedBy(currentUser);

        BloodCrossmatchHd savedHeader=bloodCrossmatchHdRepository.save(crossmatchHd);
        int compatibleCount=0;
        int incompatibleCount=0;

        for(BloodCrossmatchUnitRequest unit:request.getUnits()){
            BloodComponentInventory inventory=bloodComponentInventoryRepository.findById(unit.getInventoryId())
                    .orElseThrow(()->new SDDException(HttpStatus.NOT_FOUND.value(),"Inventory not found for ID: "+unit.getInventoryId()));

            BloodCrossmatchDt crossmatchDt=new BloodCrossmatchDt();
            crossmatchDt.setCrossmatchHd(savedHeader);
            crossmatchDt.setInventory(inventory);
            crossmatchDt.setUnitNo(unit.getUnitNo());
            crossmatchDt.setCompatibilityResult(unit.getCompatibilityResult());
            crossmatchDt.setTestDate(unit.getTestDate());
            crossmatchDt.setRemarks(unit.getRemarks());
            crossmatchDt.setCreatedDate(LocalDateTime.now());
            crossmatchDt.setCreatedBy(currentUser);
            bloodCrossmatchDtRepository.save(crossmatchDt);

            if(AppConstants.COMPATIBLE.equalsIgnoreCase(unit.getCompatibilityResult())){
                inventory.setInventoryStatus(masBloodInventoryStatusRepository.findById(inventoryStatusReserved)
                        .orElseThrow(()->new SDDException(HttpStatus.NOT_FOUND.value(),"Reserved inventory status not found")));
                inventory.setReservedForPatientId(bloodRequest.getPatient().getId());
                inventory.setReservedForInpatientId(bloodRequest.getInpatient().getInpatientId());
                inventory.setReservationDatetime(LocalDateTime.now());
                compatibleCount++;
            }else if(AppConstants.INCOMPATIBLE.equalsIgnoreCase(unit.getCompatibilityResult())){
                inventory.setInventoryStatus(masBloodInventoryStatusRepository.findById(inventoryStatusAvailable)
                        .orElseThrow(()->new SDDException(HttpStatus.NOT_FOUND.value(),"Available inventory status not found")));
                inventory.setReservedForPatientId(null);
                inventory.setReservedForInpatientId(null);
                inventory.setReservationDatetime(null);

                BloodCrossmatchFailedHistory failedHistory=new BloodCrossmatchFailedHistory();
                failedHistory.setBloodRequestDt(bloodRequestDt);
                failedHistory.setInpatient(bloodRequest.getInpatient());
                failedHistory.setInventory(inventory);
                failedHistory.setFailedDate(LocalDateTime.now());
                failedHistory.setSubmittedBy(currentUserId);
                failedHistory.setRemarks(unit.getRemarks()!=null?unit.getRemarks():request.getRemarks());
                bloodCrossmatchFailedHistoryRepository.save(failedHistory);
                incompatibleCount++;
            }
            bloodComponentInventoryRepository.save(inventory);
        }

        int totalUnits=request.getUnits().size();
        if(incompatibleCount>0){
            bloodRequestDt.setTrackingStatus(
                    bloodTrackingStatusMasterRepository.findById(crossmatchFailedStatusId)
                            .orElseThrow(()->new SDDException(
                                    HttpStatus.NOT_FOUND.value(),
                                    "Crossmatch failed tracking status not found"
                            ))
            );
        }else if(compatibleCount==totalUnits&&totalUnits>0){
            bloodRequestDt.setTrackingStatus(bloodTrackingStatusMasterRepository.findById(componentReservedStatusId)
                    .orElseThrow(()->new SDDException(HttpStatus.NOT_FOUND.value(),"Component reserved tracking status not found")));
        }else if(compatibleCount>0&&incompatibleCount>0){
            bloodRequestDt.setTrackingStatus(bloodTrackingStatusMasterRepository.findById(partiallyAllocatedStatusId)
                    .orElseThrow(()->new SDDException(HttpStatus.NOT_FOUND.value(),"Partially allocated tracking status not found")));
        }else if(incompatibleCount==totalUnits&&totalUnits>0){
            bloodRequestDt.setTrackingStatus(bloodTrackingStatusMasterRepository.findById(allocatedStatusId)
                    .orElseThrow(()->new SDDException(HttpStatus.NOT_FOUND.value(),"Allocated tracking status not found")));
        }

        bloodRequestDtRepository.save(bloodRequestDt);
        return new ApiResponse<>(HttpStatus.OK.value(),"Cross-match saved successfully",null);
    }



    @Override
    public ApiResponse<Page<BloodIssueResponse>> getPendingBloodIssue(
            int page,
            int size,
            String requestNo,
            String patientName,
            Long wardId) {

        Pageable pageable = PageRequest.of(page, size);

        Page<BloodIssueProjection> projectionPage =
                bloodRequestHdRepository.getPendingBloodIssue(
                        requestNo,
                        patientName,
                        wardId,
                        pageable);

        Page<BloodIssueResponse> responsePage =
                projectionPage.map(projection -> {

                    BloodIssueResponse response =
                            new BloodIssueResponse();

                    response.setRequestHdId(projection.getRequestHdId());
                    response.setRequestDtId(projection.getRequestDtId());
                    response.setRequestNo(projection.getRequestNo());
                    response.setInpatientNo(projection.getInpatientNo());
                    response.setPatientName(projection.getPatientName());
                    response.setBloodGroup(projection.getBloodGroup());
                    response.setComponent(projection.getComponent());
                    response.setUnitsReserved(projection.getUnitsReserved());
                    response.setRequestDept(projection.getRequestDept());
                    response.setUrgency(projection.getUrgency());
                    response.setInventoryId(projection.getInventoryId());
                    response.setRequiredBy(
                            DateTimeUtil.formatDateTime(
                                    projection.getRequiredBy()));
                    response.setReservedOn(
                            DateTimeUtil.formatDateTime(
                                    projection.getReservedOn()));

                    return response;
                });

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Pending blood issue data fetched successfully",
                responsePage);
    }


    @Override
    @Transactional
    public ApiResponse<String> updateBloodIssueAndTrackingStatus(BloodIssueStatusRequest request) {

        String currentUser = userContextService.getCurrentUserContext().getUserFullName();

        BloodRequestDt bloodRequestDt = bloodRequestDtRepository
                .findById(request.getRequestDtId())
                .orElseThrow(() -> new SDDException(
                        HttpStatus.NOT_FOUND.value(),
                        "Blood request detail not found"));

        BloodComponentInventory inventory = bloodComponentInventoryRepository
                .findById(request.getInventoryId())
                .orElseThrow(() -> new SDDException(
                        HttpStatus.NOT_FOUND.value(),
                        "Blood inventory not found"));

        if (Boolean.TRUE.equals(request.getIsIssued())) {

            MasBloodInventoryStatus issuedStatus =
                    masBloodInventoryStatusRepository.findById(inventoryStatusIssued)
                            .orElseThrow(() -> new SDDException(
                                    HttpStatus.NOT_FOUND.value(),
                                    "Issued inventory status not found"));

            bloodRequestDt.setTrackingStatus(bloodTrackingStatusMasterRepository.findById(bloodRequestStatusIssued)
                    .orElseThrow(() -> new SDDException(
                            HttpStatus.NOT_FOUND.value(),
                            "Issued tracking status not found")));
            bloodRequestDt.setIssuedBy(currentUser);
            bloodRequestDt.setIssuedDate(LocalDateTime.now());

            inventory.setInventoryStatus(issuedStatus);

        } else if (Boolean.TRUE.equals(request.getIsRejected())) {

            MasBloodInventoryStatus availableStatus =
                    masBloodInventoryStatusRepository.findById(inventoryStatusAvailable)
                            .orElseThrow(() -> new SDDException(
                                    HttpStatus.NOT_FOUND.value(),
                                    "Available inventory status not found"));

            if (request.getRejectedReason() == null ||
                    request.getRejectedReason().trim().isEmpty()) {
                throw new SDDException(
                        HttpStatus.BAD_REQUEST.value(),
                        "Rejected reason is required");
            }

            bloodRequestDt.setTrackingStatus(bloodTrackingStatusMasterRepository.findById(bloodRequestStatusRejected)
                    .orElseThrow(() -> new SDDException(
                            HttpStatus.NOT_FOUND.value(),
                            "Rejected tracking status not found")));
            bloodRequestDt.setRejectedBy(currentUser);
            bloodRequestDt.setRejectedDate(LocalDateTime.now());
            bloodRequestDt.setRejectedReason(request.getRejectedReason());

            inventory.setInventoryStatus(availableStatus);

        } else {
            throw new SDDException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Either isIssued or isRejected must be true");
        }

        bloodRequestDtRepository.save(bloodRequestDt);
        bloodComponentInventoryRepository.save(inventory);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Blood request status updated successfully",
                null);
    }

}
