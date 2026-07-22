package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.projection.DailyCaseSheetEntryProjectionResponse;
import com.hims.projection.IPDPatientWaitingListProjection;
import com.hims.projection.IpVitalsProjection;
import com.hims.projection.WardWiseDetailsProjection;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.IPDPatientService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import com.hims.utils.SaveIpdBillingDetails;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service

@RequiredArgsConstructor
@Slf4j
public class IPDPatientServiceImpl implements IPDPatientService {

    private final AuthUtil authUtil;
    private final OpdPatientDetailRepository opdPatientDetailRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final InpatientRepository inpatientRepository;
    private final MasAdmissionTypeRepository masAdmissionTypeRepository;
    private final MasAdmissionCategoryRepository masAdmissionCategoryRepository;
    private final MasAdmissionSourceRepository masAdmissionSourceRepository;
    private final MasPatientConditionRepository masPatientConditionRepository;
    private final MasCareLevelRepo masCareLevelRepository;
    private final MasWardCategoryRepository masWardCategoryRepository;
    private final MasIcdRepository masIcdRepository;
    private final MasDietPreferenceRepository masDietPreferenceRepository;
    @Autowired
    MasAdmissionStatusRepository masAdmissionStatusRepository;
    private final MasRelationRepository masRelationRepository;
    private final IpNokDetailsRepository ipNokDetailsRepository;
    private final MasWardRepository masWardRepository;
    private final MasRoomRepo masRoomRepository;
    private final MasBedRepository masBedRepository;
    private final IpBedAllocationRepository ipBedAllocationRepository;
    private  final MasIpdBillingTypeRepository masIpdBillingTypeRepository;
    private final IpdBillingHeaderRepository ipdBillingHeaderRepository;
    private final MasPaymentModeRepository masPaymentModeRepository;
    private final IpPaymentDetailRepository ipPaymentDetailRepository;
    private final IpDocumentRepository ipDocumentRepository;
    private final UserRepo userRepo;
    private final IpDiagnosisEntryRepository ipDiagnosisEntryRepository;
    private final SaveIpdBillingDetails saveIpdBillingDetails;

    @Autowired
    MasGenderRepository masGenderRepository;
    @Autowired
    MasMaritalStatusRepository masMaritalStatusRepository;
    @Autowired
    MasReligionRepository masReligionRepository;
    @Autowired
    MasDistrictRepository masDistrictRepository;
    @Autowired
    MasStateRepository masStateRepository;
    @Autowired
    MasCountryRepository masCountryRepository;
    @Autowired
    MasHospitalRepository masHospitalRepository;
    @Autowired
    MasDepartmentRepository masDepartmentRepository;
    @Autowired
    MasBedStatusRepo masBedStatusRepo;
    @Autowired
    IpNursingMedicalAssessmentRepository ipNursingMedicalAssessmentRepository;
    @Autowired
    MasIpdInternalStatusRepository masIpdInternalStatusRepository;
    @Autowired
    IpVitalsRepository ipVitalsRepository;
    @Autowired
    IpIntakeOutputEntryRepository ipIntakeOutputEntryRepository;
    @Autowired
    MasIntakeItemRepository masIntakeItemRepository;
    @Autowired
    MasIntakeTypeRepository masIntakeTypeRepository;
    @Autowired
    MasOutputTypeRepository masOutputTypeRepository;
    @Autowired
    MasRouteRepository masRouteRepository;
    @Autowired
    IpDailyCaseSheetEntryRepository ipDailyCaseSheetEntryRepository;
    @Autowired
    MasIpdServiceCategoryRepository masIpdServiceCategoryRepository;
    @Autowired
    IpdBillingDetailsRepository ipdBillingDetailsRepository;
    @Autowired
    MasVisitTypeRepository masVisitTypeRepository;
    @Autowired
    IpdConsultationTariffRepository ipdConsultationTariffRepository;
    @Autowired
    MasIpdTransferReasonRepository masIpdTransferReasonRepository;
    @Autowired
    IpTransferRequestRepository ipTransferRequestRepository;



    @Value("${ipd.admission.status.active}")
     Long activeAdmissionStatusId;

    @Value("${bed.status.available.id}")
    Long bedStatusId;
    @Value("${bed.status.Occupied.id}")
    Long bedStatusOccupiedId;
    @Value("${ip.internal.status.id}")
    Long ipInternalStatusId;
    @Value("${ip.internal.status.rw.id}")
    Long ipInternalStatusRwId;

    @Value("${ipd.service.category.id}")
    Long ipdServiceCategoryId;

    @Override
    public ApiResponse<Page<IPDPatientWaitingListResponse>> pendingAdmissionList(
            int page,
            int size,
            Long hospitalId,
            String patientName,
            String mobileNo
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            patientName = patientName != null && !patientName.trim().isEmpty() ? patientName.trim() : null;

            mobileNo = mobileNo != null && !mobileNo.trim().isEmpty() ? mobileNo.trim() : null;

            Page<IPDPatientWaitingListProjection> waitingListPage =
                    opdPatientDetailRepository.getIPDPatientWaitingList(AppConstants.IPD_ADMISSION_FLAG.toLowerCase(), hospitalId, patientName,
                            mobileNo,
                            pageable);

            Page<IPDPatientWaitingListResponse> responsePage = waitingListPage.map(this::mapToIPDPatientWaitingListResponse);

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<Page<IPDPatientWaitingListResponse>>() {}
            );

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<Page<IPDPatientWaitingListResponse>>() {}, e.getMessage(),
                    500
            );
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> saveAdmissionDetails(IpdPatientRequest request) {

        try {
            log.info("Saving IPD patient details started for patientId: {}", request.getPatientId());

            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + request.getPatientId()));
            Visit visit = null;

            if (request.getVisitId() != null) {
                visit = visitRepository.findById(request.getVisitId()).orElseThrow(() -> new RuntimeException(
                                        "Visit not found with id: " + request.getVisitId()));
            }

            Inpatient inpatient = saveInpatientDetails(request, patient, visit);

            saveNokDetails(request, inpatient, patient);

            saveBedAllocationDetails(request, inpatient, patient);

            saveIpDocumentDetails(request, inpatient, patient);

            saveDoctorDiagnosis(request,inpatient,patient);

            saveIpdBillingAndPaymentDetails(request, inpatient);

            log.info("Saving IPD patient details completed for patientId: {}, inpatientId: {}", patient.getId(), inpatient.getInpatientId());

            return ResponseUtils.createSuccessResponse("IPD patient details saved successfully", new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while saving IPD patient details for patientId: {}. Error: {}", request != null ? request.getPatientId() : null, e.getMessage(),
                    e);
            throw new RuntimeException("Error while saving IPD patient details: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<List<IpdWardResponse>> getWardDetailsByDepartment(Long departmentId) {
        try {
            List<IpdWardResponse> wardList = masWardRepository.getWardByDepartment(departmentId,AppConstants.STATUS_Y.toLowerCase());

            return ResponseUtils.createSuccessResponse(wardList, new TypeReference<List<IpdWardResponse>>() {});

        } catch (Exception e) {
            log.error("Error while fetching wards for departmentId: {}", departmentId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<List<IpdRoomResponse>> getRoomDetailsByWard(Long wardId) {
        try {
            List<IpdRoomResponse> roomList = masRoomRepository.getRoomByWard(wardId,bedStatusId,AppConstants.STATUS_Y.toLowerCase());

            return ResponseUtils.createSuccessResponse(roomList, new TypeReference<List<IpdRoomResponse>>() {});

        } catch (Exception e) {
            log.error("Error while fetching wards for departmentId: {}", wardId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<List<WardResponse>> getWardDetailsByCategory(Long wardCategoryId) {

        try {
            log.info("Fetching wards and available bed count for wardCategoryId: {}", wardCategoryId);

            List<WardResponse> wardList = masWardRepository.getWardsByCategory(wardCategoryId,bedStatusId,AppConstants.STATUS_Y.toLowerCase());

            return ResponseUtils.createSuccessResponse(wardList, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while fetching wards for wardCategoryId: {}", wardCategoryId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, 500
            );
        }
    }

    @Override
    public ApiResponse<List<BedResponse>> getBedDetailsByRoom(Long roomId) {
        try {
            log.info("Fetching rooms and available bed count for roomId: {}", roomId);

            List<MasBed> beds = masBedRepository.findAllActiveBedsByRoomId(roomId,bedStatusId,AppConstants.STATUS_Y.toLowerCase());

            List<BedResponse> bedResponses = beds.stream()
                    .map(bed -> new BedResponse(
                            bed.getBedId(),
                            bed.getBedNumber()
                    ))
                    .toList();

            return ResponseUtils.createSuccessResponse(bedResponses, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while fetching wards for wardCategoryId: {}", roomId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},AppConstants.INTERNAL_SERVER_ERR_MSG, 500
            );
        }
    }

    @Override
    public ApiResponse<List<WardWiseDetailsResponse>> getNursingDashboardByWard(Long wardId) {
        try {

            List<WardWiseDetailsProjection> projections = ipBedAllocationRepository.getWardWiseDetails(wardId,activeAdmissionStatusId);

        List<WardWiseDetailsResponse> responseList = projections.stream()
                        .map(item -> new WardWiseDetailsResponse(
                                item.getPatientId(),
                                item.getIpdPatientId(),
                                item.getPatientName(),
                                item.getRoomId(),
                                item.getRoomName(),
                                item.getBedId(),
                                item.getBedNumber(),
                                item.getAdmitDate(),
                                item.getDays(),
                                item.getAdmissionNo(),
                                item.getAdmissionStatus(),
                                item.getIpdInternalStatus(),
                                item.getAge(),
                                item.getGender(),
                                item.getDoctor()
                        ))
                        .toList();

        return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error while fetching getWardWiseDetails", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},AppConstants.INTERNAL_SERVER_ERR_MSG, 500
            );
        }

    }

    @Override
    public ApiResponse<TotalBedCountResponse> getTotalBedCountByWard(Long wardId) {

        log.info("Fetching total bed count for wardId: {}", wardId);

        TotalBedCountResponse response = masBedRepository.getTotalBedCountByDepartmentId(wardId,bedStatusId,bedStatusOccupiedId);

        if (response == null) {
            response = new TotalBedCountResponse(0L, 0L, 0L, null);
        }

        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

    }

    @Override
    @Transactional
    public ApiResponse<String> saveNursingMedicalAssessment(IpNursingMedicalAssessmentRequest request) {

        log.info("Saving IP nursing medical assessment. inpatientId: {}, hospitalId: {}",
                request.getInpatientId(),
                request.getHospitalId()
        );

        try {
            User user=authUtil.getCurrentUser();

            Optional<Inpatient> inpatient = inpatientRepository.findById(request.getInpatientId());
            if(inpatient.isEmpty()){

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Inpatient not found", HttpStatus.NOT_FOUND.value());

            }

            Optional<MasHospital> hospital = masHospitalRepository.findById(request.getHospitalId());
            if(hospital.isEmpty()){

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Hospital not found", HttpStatus.NOT_FOUND.value());

            }
            IpNursingMedicalAssessment assessment = new IpNursingMedicalAssessment();

            assessment.setInpatient(inpatient.get());
            assessment.setHospital(hospital.get());

            // Nursing assessment
            assessment.setConsciousness(request.getConsciousness());
            assessment.setGcsScore(request.getGcsScore());
            assessment.setPainScore(request.getPainScore());
            assessment.setMobilityStatus(request.getMobilityStatus());
            assessment.setFallRisk(request.getFallRisk());
            assessment.setPressureSoreRisk(request.getPressureSoreRisk());

            // Skin assessment
            assessment.setSkinCondition(request.getSkinCondition());
            assessment.setSkinRemarks(request.getSkinRemarks());

            // IV line details
            assessment.setIvLinePresent(request.getIvLinePresent());
            assessment.setIvSite(request.getIvSite());

            // Catheter details
            assessment.setCatheterPresent(request.getCatheterPresent());
            assessment.setCatheterType(request.getCatheterType());

            // Drain details
            assessment.setDrainPresent(request.getDrainPresent());
            assessment.setDrainType(request.getDrainType());

            // Nutrition assessment
            assessment.setNutritionRisk(request.getNutritionRisk());
            assessment.setNutritionRemarks(request.getNutritionRemarks());

            // Infection assessment
            assessment.setInfectionRisk(request.getInfectionRisk());
            assessment.setInfectionRemarks(request.getInfectionRemarks());

            // Orientation details
            assessment.setPatientOrientationDone(request.getPatientOrientationDone());
            assessment.setRelativeOrientationDone(request.getRelativeOrientationDone());

            assessment.setNursingCarePlan(request.getNursingCarePlan());

            // Medical history
            assessment.setChiefComplaint(request.getChiefComplaint());
            assessment.setHistoryPresentIllness(request.getHistoryPresentIllness());
            assessment.setFamilyHistory(request.getFamilyHistory());
            assessment.setMedicationHistory(request.getMedicationHistory());
            assessment.setAllergies(request.getAllergies());

            // Vitals
            assessment.setPulse(request.getPulse());
            assessment.setSystolicBp(request.getSystolicBp());
            assessment.setDiastolicBp(request.getDiastolicBp());
            assessment.setTemperature(request.getTemperature());

            assessment.setRespiratoryRate(request.getRespiratoryRate());
            assessment.setSpo2(request.getSpo2());

            // Examination
            assessment.setGeneralExaminationNotes(request.getGeneralExaminationNotes());
            assessment.setSystemRsExamination(request.getRsExamination());
            assessment.setSystemCvsExamination(request.getCvsExamination());
            assessment.setSystemPaExamination(request.getPaExamination());
            assessment.setSystemCnsExamination(request.getCnsExamination());
            assessment.setProvisionalDiagnosis(request.getProvisionalDiagnosis());
            assessment.setCreatedBy(user.getFullName());
            assessment.setCreatedDate(LocalDateTime.now());
            assessment.setUpdatedBy(user.getFullName());
            assessment.setUpdatedDate(LocalDateTime.now());

            IpNursingMedicalAssessment savedAssessment = ipNursingMedicalAssessmentRepository.save(assessment);

            // Save an entry in ip_vitals
             saveIpVitals(request, inpatient.get(), user);


            log.info("IP nursing medical assessment saved successfully. assessmentId: {}, inpatientId: {}",
                    savedAssessment.getAssessmentId(),
                    request.getInpatientId());

            return ResponseUtils.createSuccessResponse("IP nursing medical assessment saved successfully. Assessment ID: "
                            + savedAssessment.getAssessmentId(), new TypeReference<>() {});

        } catch (Exception exception) {
            log.error("Error while saving IP nursing medical assessment. inpatientId: {}, hospitalId: {}", request.getInpatientId(), request.getHospitalId(), exception);

            throw new RuntimeException("Unable to save IP nursing medical assessment: " + exception.getMessage(), exception);
        }
    }
    private void saveIpVitals(
            IpNursingMedicalAssessmentRequest request,
            Inpatient inpatient,
            User user
    ) {
        try {
            LocalDateTime currentDateTime = LocalDateTime.now();

            IpVitals ipVitals = new IpVitals();

            ipVitals.setInpatient(inpatient);
            ipVitals.setObservationDatetime(currentDateTime);
            ipVitals.setTemperature(request.getTemperature());
            ipVitals.setPulse(request.getPulse());
            ipVitals.setBpSystolic(request.getSystolicBp());
            ipVitals.setBpDiastolic(request.getDiastolicBp());
            ipVitals.setRespiration(request.getRespiratoryRate());
            ipVitals.setSpo2(request.getSpo2());
            ipVitals.setPainScore(request.getPainScore());
            ipVitals.setCreatedBy(user.getFullName());
            ipVitals.setLastUpdatedBy(user.getFullName());
            ipVitals.setLastUpdateDate(currentDateTime);

            IpVitals savedVitals = ipVitalsRepository.save(ipVitals);

            log.info("IP vitals saved successfully. ipVitalsId: {}, inpatientId: {}", savedVitals.getIpVitalsId(), inpatient.getInpatientId());

        } catch (Exception exception) {
            log.error("Error while saving IP vitals. inpatientId: {}", inpatient.getInpatientId(), exception);

            throw new RuntimeException("Unable to save IP vitals for inpatient ID: " + inpatient.getInpatientId() + ". Error: " + exception.getMessage(), exception);
        }
    }
    @Override
    public ApiResponse<String> updateAdmissionInternalStatus(Long inpatientId,Long internalStatusId) {
        try{
        User user=authUtil.getCurrentUser();

        Inpatient inpatient = inpatientRepository.findById(inpatientId).orElseThrow(() -> new RuntimeException("Inpatient not found with id: "
                + inpatientId));
        inpatient.setMasIpdInternalStatus(masIpdInternalStatusRepository.findById(internalStatusId).orElseThrow());
        inpatient.setLastUpdateDate(LocalDateTime.now());
        inpatient.setLastUpdatedBy(user.getFullName());
        inpatientRepository.save(inpatient);

        return ResponseUtils.createSuccessResponse( "Ip internal status change successfully", new TypeReference<>() {});

    } catch (Exception exception) {
        log.error("Error while saving Ip internal status change", exception);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},AppConstants.INTERNAL_SERVER_ERR_MSG, 500
            );
    }
    }

    @Override
    public ApiResponse<List<IpVitalsResponse>> getVitalsDetails(Long inpatientId) {

        log.info("Fetching all vitals history for inpatientId: {}", inpatientId);

        try {
            List<IpVitalsProjection> vitalsList = ipVitalsRepository.findAllVitalsByInpatientId(inpatientId);

            if (vitalsList == null || vitalsList.isEmpty()) {

                log.warn("No vitals history found for inpatientId: {}", inpatientId);

                return ResponseUtils.createFailureResponse(Collections.emptyList(), new TypeReference<>() {}, "No vitals details found for inpatient ID: " + inpatientId, 404);
            }

            List<IpVitalsResponse> responseList = vitalsList.stream()
                    .map(this::mapVitalsProjectionToResponse)
                    .toList();

            log.info("Successfully fetched {} vitals records for inpatientId: {}", responseList.size(), inpatientId);

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error while fetching vitals history for inpatientId: {}", inpatientId, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
        }
    }

    @Override
    public ApiResponse<String> saveVitalsDetails(IpVitalsRequest request) {
        log.info("Saving vitals details started for inpatientId: {}", request.getInpatientId());

        try {
            User user=authUtil.getCurrentUser();
            Inpatient inpatient = inpatientRepository.findById(request.getInpatientId()).orElse(null);

            if (inpatient == null) {
                log.warn("Inpatient not found for inpatientId: {}", request.getInpatientId());

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Inpatient not found with ID: " + request.getInpatientId(), 404);}

            IpVitals ipVitals = new IpVitals();

            ipVitals.setInpatient(inpatient);
            ipVitals.setObservationDatetime( LocalDateTime.now());
            ipVitals.setTemperature(request.getTemperature());
            ipVitals.setPulse(request.getPulse());
            ipVitals.setBpSystolic(request.getBpSystolic());
            ipVitals.setBpDiastolic(request.getBpDiastolic());
            ipVitals.setRespiration(request.getRespiration());
            ipVitals.setSpo2(request.getSpo2());
            ipVitals.setPainScore(request.getPainScore());
            ipVitals.setLastUpdateDate(LocalDateTime.now());
            ipVitals.setCreatedBy(user.getFullName());
            ipVitals.setLastUpdatedBy(user.getFullName());

            IpVitals savedVitals = ipVitalsRepository.save(ipVitals);

            log.info("Vitals details saved successfully for inpatientId: {}, vitalId: {}", request.getInpatientId(), savedVitals.getIpVitalsId());

            return ResponseUtils.createSuccessResponse("Vitals details saved successfully", new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while saving vitals details for inpatientId: {}", request.getInpatientId(), e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
        }
    }

    @Override
    public ApiResponse<String> saveIntakeOutputDetails(IpIntakeOutputSaveRequest request) {

        log.info("Saving intake/output details started for inpatientId: {}", request.getInpatientId());

        try {
            if (request.getEntries() == null || request.getEntries().isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "At least one intake/output entry is required", 400);
            }

            Inpatient inpatient = inpatientRepository.findById(request.getInpatientId()).orElse(null);

            if (inpatient == null) {
                log.warn("Inpatient not found with inpatientId: {}", request.getInpatientId());

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Inpatient not found with ID: " + request.getInpatientId(), 404);
            }

            Patient patient = inpatient.getPatient();

            if (patient == null) {
                log.warn("Patient is not associated with inpatientId: {}", request.getInpatientId());

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Patient is not associated with this inpatient", 400);}

            User currentUser = authUtil.getCurrentUser();
            LocalDateTime currentDateTime = LocalDateTime.now();

            List<IpIntakeOutputEntry> entities = new ArrayList<>();

            for (int index = 0; index < request.getEntries().size(); index++) {

                IpIntakeOutputEntryRequest entryRequest = request.getEntries().get(index);


                IpIntakeOutputEntry entity = buildIntakeOutputEntity(entryRequest, inpatient, patient, currentUser, currentDateTime);

                entities.add(entity);
            }

            List<IpIntakeOutputEntry> savedEntries = ipIntakeOutputEntryRepository.saveAll(entities);

            log.info("Successfully saved {} intake/output entries for inpatientId: {}", savedEntries.size(), request.getInpatientId());

            return ResponseUtils.createSuccessResponse( " intake/output entries saved successfully", new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error while saving intake/output details for inpatientId: {}",request.getInpatientId(), e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> saveDailyCaseSheetEntry(IpDailyCaseSheetEntryRequest request) {

        log.info("Saving daily case sheet entry. inpatientId: {}, doctorId: {}, " + "departmentId: {}, visitTypeId: {}",
                request.getInpatientId(),
                request.getDoctorId(),
                request.getVisitDepartmentId(),
                request.getVisitType());

        try {

            LocalDateTime currentDateTime = LocalDateTime.now();

            User loggedInUser = authUtil.getCurrentUser();

            Inpatient inpatient = inpatientRepository.findById(request.getInpatientId()).orElseThrow(() -> new RuntimeException("Inpatient not found with ID: "
                                    + request.getInpatientId()));

            Patient patient = inpatient.getPatient();

            if (patient == null) {
                throw new RuntimeException("Patient is not associated with inpatient ID: " + request.getInpatientId());
            }

            User doctor = userRepo.findById(request.getDoctorId()).orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + request.getDoctorId()));

            MasDepartment department = masDepartmentRepository.findById(request.getVisitDepartmentId()).orElseThrow(() -> new RuntimeException(
                            "Department not found with ID: " + request.getVisitDepartmentId()));

            MasVisitType visitType = masVisitTypeRepository.findById(request.getVisitType()).orElseThrow(() -> new RuntimeException(
                            "Visit type not found with ID: " + request.getVisitType()));

            /*
             * Check current active consultation tariff before saving
             * the case-sheet entry.
             */
            IpdConsultationTariff consultationTariff = ipdConsultationTariffRepository.findCurrentApplicableTariff(
                                    request.getVisitDepartmentId(),
                                    request.getDoctorId(),
                                    request.getVisitType(),
                                    currentDateTime
                            )
                            .orElseThrow(() -> new RuntimeException(
                                    "No active consultation tariff found for the current date. "
                                            + "Doctor ID: " + request.getDoctorId()
                                            + ", Department ID: "
                                            + request.getVisitDepartmentId()
                                            + ", Visit Type ID: "
                                            + request.getVisitType()
                            ));



            log.info("Consultation tariff found. tariffId: {}, baseTariff: {}", consultationTariff.getTariffId(), consultationTariff.getBaseTariff());

            IpDailyCaseSheetEntry caseSheetEntry =
                    IpDailyCaseSheetEntry.builder()
                            .inpatient(inpatient)
                            .patient(patient)
                            .visitDatetime(currentDateTime)
                            .doctor(doctor)
                            .doctorName(doctor.getFullName())
                            .doctorRole(null)
                            .visitDepartment(department)
                            .doctorNotes(request.getDoctorNotes())
                            .investigationSummary(request.getInvestigationSummary())
                            .medicineSummary(request.getMedicineSummary())
                            .procedureSummary(request.getProcedureSummary())
                            .carePlanChanges(request.getCarePlanChanges())
                            .nextFollowUpPlan(request.getNextFollowUpPlan())
                            .lastUpdateDate(currentDateTime)
                            .createdBy(loggedInUser.getFullName())
                            .lastUpdatedBy(loggedInUser.getFullName())
                            .build();

            MasIpdServiceCategory billingCategory = masIpdServiceCategoryRepository.findById(ipdServiceCategoryId)
                    .orElseThrow(() -> new RuntimeException("IPD service category not found with ID: 2"));


            IpDailyCaseSheetEntry savedCaseSheetEntry = ipDailyCaseSheetEntryRepository.save(caseSheetEntry);

            BigDecimal quantity = BigDecimal.ONE;

            BigDecimal rate = Optional.ofNullable(consultationTariff.getBaseTariff()).orElseThrow(() -> new RuntimeException("Base tariff is not configured for tariff ID: " + consultationTariff.getTariffId()));

            BigDecimal gstPercent = BigDecimal.ZERO;

            if (billingCategory != null && billingCategory.getGstPercentage() != null) {
                gstPercent = new BigDecimal(billingCategory.getGstPercentage().toString());
            }

            BigDecimal discountAmount = BigDecimal.ZERO;

            // amount = rate × quantity
            BigDecimal amount = rate.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

            // gstAmount = amount × gstPercent ÷ 100
            BigDecimal gstAmount = amount.multiply(gstPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            // netAmount = amount + GST - discount
            BigDecimal netAmount = amount.add(gstAmount).subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
            String name=consultationTariff.getDoctor().getFullName();

            /*
             * Create billing using the matched tariff.
             *
             */
            saveIpdBillingDetails.saveDailyCaseSheetBillingDetails(inpatient,rate,quantity,gstPercent,discountAmount,amount ,gstAmount,netAmount,billingCategory,name);

            log.info("Daily case sheet entry saved successfully. " + "caseSheetEntryId: {}, inpatientId: {}, tariffId: {}",
                    savedCaseSheetEntry.getCaseSheetEntryId(),
                    inpatient.getInpatientId(),
                    consultationTariff.getTariffId()
            );

            return ResponseUtils.createSuccessResponse("Daily case sheet entry saved successfully", new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error while saving daily case sheet entry. inpatientId: {}", request.getInpatientId(), e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},e.getMessage(), 404);
        }
    }
    @Override
    public ApiResponse<List<DailyCaseSheetEntryResponse>> getDailyCaseSheetEntry(Long inpatientId) {

        log.info("Fetching daily case sheet entries for inpatientId: {}", inpatientId);

        try {
            List<DailyCaseSheetEntryProjectionResponse> projections = ipDailyCaseSheetEntryRepository.findDailyCaseSheetEntries(inpatientId);

            List<DailyCaseSheetEntryResponse> responseList =
                    projections.stream().map(this::mapToDailyCaseSheetResponse).toList();

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

        } catch (Exception exception) {

            log.error("Error while fetching daily case sheet entries. " + "inpatientId: {}", inpatientId, exception);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG + exception.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<List<BedDetailsByWardResponse>> getBedDetailsByWard(Long wardId) {
        log.info("Fetching bed details for wardId: {}", wardId);

        try {
            List<BedDetailsByWardResponse> bedDetails = masBedRepository.getBedDetailsByWard(wardId,bedStatusId);

            log.info("Bed details fetched successfully for wardId: {}, totalBeds: {}",   bedDetails.size());

            return ResponseUtils.createSuccessResponse(bedDetails, new TypeReference<>() {});

        } catch (Exception exception) {

            log.error("Error while fetching bed details for wardId: {}", wardId, exception);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Unable to fetch bed details: " + exception.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<String> saveBedTransferRequest(BedTransferRequest request) {

        log.info("Saving bed transfer request started. inpatientId: {}", request.getInpatientId());

        try {

            User currentUser = authUtil.getCurrentUser();

            Inpatient inpatient = inpatientRepository.findById(request.getInpatientId()).orElseThrow(() -> new RuntimeException(
                            "Inpatient not found with ID: " + request.getInpatientId()));

            Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow(() -> new RuntimeException(
                            "Patient not found with ID: " + request.getPatientId()));

            MasWard fromWard = masWardRepository.findById(request.getFromWard()).orElseThrow(() -> new RuntimeException(
                    "From ward not found with ID: " + request.getFromWard()));

            MasBed fromBed = masBedRepository.findById(request.getFromBed())
                    .orElseThrow(() -> new RuntimeException("From bed not found with ID: " + request.getFromBed()));

            MasWard toWard = masWardRepository.findById(request.getToWard()).orElseThrow(() -> new RuntimeException("To ward not found with ID: " + request.getToWard()));

            MasBed toBed = masBedRepository.findById(request.getToBed()).orElseThrow(() -> new RuntimeException("To bed not found with ID: " + request.getToBed()));

            User doctor = userRepo.findById(request.getDoctorId()).orElseThrow(() -> new RuntimeException(
                            "Doctor not found with ID: " + request.getDoctorId()));

            MasIpdTransferReason transferReason = masIpdTransferReasonRepository.findById(request.getTransferReasonId())
                            .orElseThrow(() -> new RuntimeException("Transfer reason not found with ID: " + request.getTransferReasonId()));


            IpTransferRequest transferRequest = IpTransferRequest.builder()

                    .patient(patient)
                    .inpatient(inpatient)
                    .fromWard(fromWard)
                    .fromBed(fromBed)
                    .toWard(toWard)
                    .toBed(toBed)
                    .doctor(doctor)
                    .transferReason(transferReason)
                    .priority(request.getPriority())
                    .clinicalNotes(request.getClinicalNotes())
                    .requestDatetime(LocalDateTime.now())
                    .requestedBy(currentUser.getFullName())
                    .transferStatus(AppConstants.IPD_TRANSFER_STATUS)
                    .createdBy(currentUser.getFullName())
                    .createdDate(LocalDateTime.now())
                    .lastUpdatedBy(currentUser.getFullName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            IpTransferRequest savedRequest = ipTransferRequestRepository.save(transferRequest);

            log.info("Bed transfer request saved successfully. transferId: {}, transferNo: {}",
                    savedRequest.getTransferId(),
                    savedRequest.getTransferNo());

            return ResponseUtils.createSuccessResponse("Bed transfer request saved successfully. Transfer No: "
                            + savedRequest.getTransferNo(), new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error while saving bed transfer request. inpatientId: {}", request.getInpatientId(), e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, e.getMessage(),
                    500);
        }
    }

    private DailyCaseSheetEntryResponse mapToDailyCaseSheetResponse(
            DailyCaseSheetEntryProjectionResponse projection
    ) {

        return DailyCaseSheetEntryResponse.builder()
                .caseSheetEntryId(projection.getCaseSheetEntryId())
                .inpatient(projection.getInpatient())
                .notes(projection.getNotes())
                .investigation(projection.getInvestigation())
                .medicines(projection.getMedicines())
                .procedure(projection.getProcedure())
                .plan(projection.getPlan())
                .followUp(projection.getFollowUp())
                .visitDateTime(projection.getVisitDateTime())
                .doctorId(projection.getDoctorId())
                .doctorName(projection.getDoctorName())
                .departmentId(projection.getDepartmentId())
                .departmentName(projection.getDepartmentName())
                .build();
    }


    private IpIntakeOutputEntry buildIntakeOutputEntity(
            IpIntakeOutputEntryRequest request,
            Inpatient inpatient,
            Patient patient,
            User userName,
            LocalDateTime currentDateTime) {

        String ioType = request.getIoType().trim().toUpperCase();

        IpIntakeOutputEntry entity = new IpIntakeOutputEntry();

        entity.setInpatient(inpatient);
        entity.setPatient(patient);
        entity.setQuantity(request.getQuantity());
        entity.setUnit("ml");
        entity.setObservationDatetime( currentDateTime);
        entity.setRemarks(request.getRemarks());
        entity.setLastUpdateDate(currentDateTime);
        entity.setCreatedBy(userName.getFullName());
        entity.setLastUpdatedBy(userName.getFullName());

        if ("I".equals(ioType)) {

            MasIntakeType intakeType = masIntakeTypeRepository.getReferenceById(request.getIntakeTypeId());

            MasIntakeItem intakeItem = masIntakeItemRepository.getReferenceById(request.getIntakeItemId());

            entity.setIntakeType(intakeType);
            entity.setIntakeItem(intakeItem);
            if (request.getRouteId() != null)
            {
                entity.setRoute(masRouteRepository.getReferenceById(request.getRouteId()));
            }
            entity.setOutputType(null);

        } else {

            MasOutputType outputType = masOutputTypeRepository.getReferenceById(request.getOutputTypeId());

            entity.setOutputType(outputType);
            entity.setIntakeType(null);
            entity.setIntakeItem(null);
            entity.setRoute(null);
        }

        return entity;
    }

    private void saveDoctorDiagnosis(IpdPatientRequest request, Inpatient inpatient, Patient patient) {

        User user = authUtil.getCurrentUser();

        IpDiagnosisEntry ipDiagnosisEntry=new IpDiagnosisEntry();
        ipDiagnosisEntry.setDiagnosisDatetime(LocalDateTime.now());
        ipDiagnosisEntry.setInpatient(inpatient);
        ipDiagnosisEntry.setPatient(patient);
        ipDiagnosisEntry.setDepartment(masDepartmentRepository.findById(request.getDepartmentId()).orElseThrow());
        ipDiagnosisEntry.setRecordedBy(userRepo.findById(request.getTreatingDoctor()).orElseThrow());
        ipDiagnosisEntry.setDiagnosisText(request.getWorkingDiagnosis());
        ipDiagnosisEntry.setCreatedBy(user.getFullName());
        ipDiagnosisEntry.setLastUpdateDate(LocalDateTime.now());
        ipDiagnosisEntry.setLastUpdatedBy(user.getFullName());


        ipDiagnosisEntryRepository.save(ipDiagnosisEntry);
        log.info("IpDiagnosisEntry saved successfully for inpatientId: {}", inpatient.getInpatientId());
    }

    private void saveIpdBillingAndPaymentDetails(IpdPatientRequest request, Inpatient inpatient) {
        User user=authUtil.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        BigDecimal advanceAmount = request.getAdvanceAmount() != null ? request.getAdvanceAmount() : BigDecimal.ZERO;


        MasIpdBillingType billingType = masIpdBillingTypeRepository.findById(request.getPaymentType())
                .orElseThrow(() -> new RuntimeException("Invalid billing type id: " + request.getPaymentType()));

        IpdBillingHeader billingHeader = new IpdBillingHeader();

        billingHeader.setUhid(request.getUhid());
        billingHeader.setInpatientId(inpatientRepository.findById(inpatient.getInpatientId()).orElseThrow());
        billingHeader.setPatientName(request.getPatientName());
        billingHeader.setBillingType(billingType);
        billingHeader.setCreatedBy(user.getFullName());
        billingHeader.setUpdatedBy(user.getFullName());
        billingHeader.setCreatedAt(LocalDateTime.now());
        billingHeader.setUpdatedAt(LocalDateTime.now());

        IpdBillingHeader savedBillingHeader = ipdBillingHeaderRepository.save(billingHeader);

        log.info("IPD billing header saved successfully for inpatientId: {}, billId: {}",
                inpatient.getInpatientId(), savedBillingHeader.getBillId());

        IpPaymentDetail paymentDetail = new IpPaymentDetail();

            paymentDetail.setInpatient(inpatient);
            paymentDetail.setBill(savedBillingHeader);
            if (request.getPaymentMode() != null) {
                MasPaymentMode paymentMode = masPaymentModeRepository.findById(request.getPaymentMode())
                        .orElseThrow(() -> new RuntimeException("Invalid payment mode id: " + request.getPaymentMode()));
                paymentDetail.setPaymentMode(paymentMode);
            }
            paymentDetail.setAmount(advanceAmount);
            paymentDetail.setPaymentDate(now);
            paymentDetail.setLastChgBy(user.getFullName());
            paymentDetail.setLastChgDate(now);

            ipPaymentDetailRepository.save(paymentDetail);

            log.info("IPD advance payment saved successfully for inpatientId: {}, amount: {}", inpatient.getInpatientId(), advanceAmount);

    }

    private Inpatient saveInpatientDetails(IpdPatientRequest request, Patient patient, Visit visit) {
        User user = authUtil.getCurrentUser();

        Inpatient inpatient = new Inpatient();

        inpatient.setPatient(patient);
        inpatient.setVisit(visit);
        inpatient.setAdmissionDate(request.getAdmissionDate());
        inpatient.setAdmissionTime(request.getAdmissionTime());
        inpatient.setAdmissionNo(generateAdmissionNo());
        inpatient.setConsentTakenBy(request.getConsentTakenBy());
        inpatient.setMlcCase(request.getMlcCase());
        inpatient.setPoliceIntimationRequired(request.getPoliceIntimationRequired());
        inpatient.setAdmissionAdvisedFrom(request.getAdmissionAdvisedFrom());
        inpatient.setAdmissionConsentTaken(request.getAdmissionConsentTaken());
        inpatient.setAdmissionStatus(masAdmissionStatusRepository.findById(activeAdmissionStatusId).orElseThrow());
        inpatient.setDietPreference(masDietPreferenceRepository.findById(request.getDietPreferenceId()).orElseThrow());
        inpatient.setMasIpdInternalStatus(masIpdInternalStatusRepository.findById(ipInternalStatusId).orElseThrow());

        if (request.getAdmissionTypeId() != null) {
            inpatient.setAdmissionType(masAdmissionTypeRepository.getReferenceById(request.getAdmissionTypeId()));
        }
        if (request.getWardId() != null) {
            inpatient.setAdmittingWardId(masWardRepository.getReferenceById(request.getWardId()));
        }

        if (request.getAdmissionCategoryId() != null) {
            inpatient.setAdmissionCategory(masAdmissionCategoryRepository.getReferenceById(request.getAdmissionCategoryId()));
        }

        if (request.getAdmissionSourceId() != null) {
            inpatient.setAdmissionSource(masAdmissionSourceRepository.getReferenceById(request.getAdmissionSourceId()));
        }

        if (request.getPatientConditionId() != null) {
            inpatient.setPatientCondition(masPatientConditionRepository.getReferenceById(request.getPatientConditionId()));
        }

        if (request.getCareLevelId() != null) {
            inpatient.setCareLevel(masCareLevelRepository.getReferenceById(request.getCareLevelId()));
        }

        if (request.getWardCategoryId() != null) {
            inpatient.setWardCategory(masWardCategoryRepository.getReferenceById(request.getWardCategoryId()));
        }


        inpatient.setConditionNotes(request.getConditionNotes());
        inpatient.setLastUpdateDate(LocalDateTime.now());

        if (user != null) {
            inpatient.setCreatedBy(user.getFullName());
            inpatient.setLastUpdatedBy(user.getFullName());
        }

        Inpatient savedInpatient = inpatientRepository.save(inpatient);

        log.info("Inpatient admission details saved successfully. inpatientId: {}", savedInpatient.getInpatientId());
        return savedInpatient;
    }

    private void saveNokDetails(IpdPatientRequest request, Inpatient inpatient, Patient patient) {
        User user = authUtil.getCurrentUser();

        IpNokDetails nokDetails = new IpNokDetails();

        nokDetails.setInpatient(inpatient);
        nokDetails.setPatient(patient);
        nokDetails.setNokName(request.getNokName());

        if (request.getNokRelationId() != null) {
            nokDetails.setNokRelation(masRelationRepository.getReferenceById(request.getNokRelationId()));
        }
        nokDetails.setContactNo(request.getContactNo());
        nokDetails.setAddressLine(request.getAddressLine());
        nokDetails.setCity(request.getCity());
        nokDetails.setState(request.getState());
        nokDetails.setPincode(request.getPincode());
        nokDetails.setLastUpdateDate(LocalDateTime.now());

        if (user != null) {
            nokDetails.setCreatedBy(user.getFullName());
            nokDetails.setLastUpdatedBy(user.getFullName());
        }
        ipNokDetailsRepository.save(nokDetails);
        log.info("NOK details saved successfully for inpatientId: {}", inpatient.getInpatientId());
    }


    private void saveBedAllocationDetails(IpdPatientRequest request, Inpatient inpatient, Patient patient) {
        User user = authUtil.getCurrentUser();

        IpBedAllocation bedAllocation = new IpBedAllocation();

        bedAllocation.setInpatient(inpatient);

        bedAllocation.setPatient(patient);

        if (request.getWardId() != null) {
            bedAllocation.setWard(masWardRepository.getReferenceById(request.getWardId()));
        }

        if (request.getRoomId() != null) {
            bedAllocation.setRoom(masRoomRepository.getReferenceById(request.getRoomId()));
        }

        if (request.getBedId() != null) {
            bedAllocation.setBed(masBedRepository.getReferenceById(request.getBedId()));
            MasBed bed = masBedRepository.findById(request.getBedId()).orElseThrow(() -> new RuntimeException("Bed not found with id: " + request.getBedId()));
            bed.setBedStatusId(masBedStatusRepo.findById(bedStatusOccupiedId).orElseThrow());
        }

        bedAllocation.setAllocationStartDate(LocalDateTime.now());
        bedAllocation.setLastUpdateDate(LocalDateTime.now());

        if (user != null) {
            bedAllocation.setCreatedBy(user.getFullName());
            bedAllocation.setLastUpdatedBy(user.getFullName());
        }
        ipBedAllocationRepository.save(bedAllocation);
        log.info("Bed allocation details saved successfully for inpatientId: {}", inpatient.getInpatientId());
    }


    private void saveIpDocumentDetails(IpdPatientRequest request, Inpatient inpatient, Patient patient) {

        List<IpdPatientRequest.IpDocumentRequest> documents = request.getDocuments();

        if (documents == null || documents.isEmpty()) {
            log.info("No IPD documents uploaded for inpatientId: {}", inpatient.getInpatientId());
            return;
        }



        String uploadDir = "uploads/ipd/documents/";

        try {
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (IpdPatientRequest.IpDocumentRequest docReq : documents) {

                if (docReq == null) {
                    continue;
                }

                MultipartFile file = docReq.getIpDocumentUploads();

                if (file == null || file.isEmpty()) {
                    throw new RuntimeException(
                            "File is required for document type: " + docReq.getDocumentType()
                    );
                }

                String originalFileName = file.getOriginalFilename();

                if (originalFileName == null || originalFileName.trim().isEmpty()) {
                    throw new RuntimeException("Invalid file name");
                }

                String safeOriginalFileName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");

                String fileType = getFileExtension(safeOriginalFileName);

                Long fileSizeKb = file.getSize() / 1024;

                String newFileName = UUID.randomUUID() + "_" + safeOriginalFileName;

                Path finalFilePath = uploadPath.resolve(newFileName);

                Files.copy(
                        file.getInputStream(),
                        finalFilePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                IpDocument document = new IpDocument();

                document.setInpatient(inpatient);
                document.setPatient(patient);
                document.setDocumentDatetime(LocalDateTime.now());
                document.setDocumentType(docReq.getDocumentType());
                document.setFileName(originalFileName);
                document.setFilePath(finalFilePath.toString());
                document.setFileType(fileType);
                document.setFileSizeKb(fileSizeKb);
                document.setLastUpdateDate(LocalDateTime.now());

                ipDocumentRepository.save(document);
            }

            log.info("All IPD documents saved successfully for inpatientId: {}", inpatient.getInpatientId());

        } catch (Exception e) {
            log.error("Error while saving IPD document for inpatientId: {}", inpatient.getInpatientId(), e);
            throw new RuntimeException("Error while saving IPD document: " + e.getMessage(), e);
        }
    }


    private String getFileExtension(String fileName) {

        if (fileName == null || !fileName.contains(".")) {
            return null;
        }

        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }


    private IPDPatientWaitingListResponse mapToIPDPatientWaitingListResponse(
            IPDPatientWaitingListProjection projection) {

        IPDPatientWaitingListResponse response = new IPDPatientWaitingListResponse();

        response.setOpdPatientDetailsId(projection.getOpdPatientDetailsId());
        response.setVisitId(projection.getVisitId());
        response.setPatientId(projection.getPatientId());
        response.setPatientName(projection.getPatientName());
        response.setPatientMobileNo(projection.getPatientMobileNo());
        response.setAge(projection.getAge());
        response.setGender(projection.getGender());
        response.setAdmissionAdviseDate(projection.getAdmissionAdviseDate() != null ? projection.getAdmissionAdviseDate() : null);
        response.setDoctorName(projection.getDoctorName());
        response.setDepartment(projection.getDepartment());
        response.setWardId(projection.getWardId());
        response.setWardName(projection.getWardName());
        response.setCareLevelId(projection.getCareLevelId());
        response.setCareLevel(projection.getCareLevel());
        response.setUhid(projection.getUhid());
        response.setDepartmentId(projection.getDepartmentId());
        response.setAdmissionWardCategoryId(projection.getAdmissionWardCategoryId());
        response.setAdmissionWardCategoryName(projection.getAdmissionWardCategoryName());
        response.setAdmissionSource(null);
        return response;
    }
    private String generateAdmissionNo() {

        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();

        int financialYearStart;
        int financialYearEnd;

        if (currentMonth >= 4) {
            financialYearStart = currentYear;
            financialYearEnd = currentYear + 1;
        } else {
            financialYearStart = currentYear - 1;
            financialYearEnd = currentYear;
        }

        String financialYear = String.format("%02d-%02d", financialYearStart % 100, financialYearEnd % 100);

        String prefix = "IPD/" + financialYear;

        String lastAdmissionNo = inpatientRepository.findLastAdmissionNoByFinancialYear(prefix + "/%");

        int nextNumber = 1;

        if (lastAdmissionNo != null && !lastAdmissionNo.isBlank()) {
            try {
                // Example: IPD/26-27/1
                String[] parts = lastAdmissionNo.split("/");

                if (parts.length == 3) {
                    nextNumber = Integer.parseInt(parts[2]) + 1;
                }
            } catch (NumberFormatException e) {
                log.warn("Unable to parse last admission number: {}", lastAdmissionNo);
            }
        }

        return prefix + "/" + nextNumber;
    }
    private IpVitalsResponse mapVitalsProjectionToResponse(IpVitalsProjection projection) {
        return IpVitalsResponse.builder()
                .vitalId(projection.getVitalId())
                .inpatientId(projection.getInpatientId())
                .observationDatetime(projection.getObservationDatetime())
                .temperature(projection.getTemperature())
                .pulse(projection.getPulse())
                .bpSystolic(projection.getBpSystolic())
                .bpDiastolic(projection.getBpDiastolic())
                .respiration(projection.getRespiration())
                .spo2(projection.getSpo2())
                .painScore(projection.getPainScore())
                .build();
    }
}
