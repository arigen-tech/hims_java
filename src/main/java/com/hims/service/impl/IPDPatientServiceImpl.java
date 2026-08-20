package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import java.time.temporal.ChronoUnit;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.helperUtil.HelperUtils;
import com.hims.projection.*;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.IPDPatientService;
import com.hims.mapper.IpMarDetailsMapper;
import com.hims.mapper.IpProcedureTxnMapper;
import com.hims.service.TransactionSequenceService;
import com.hims.utils.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private final MasIpdBillingTypeRepository masIpdBillingTypeRepository;
    private final IpdBillingHeaderRepository ipdBillingHeaderRepository;
    private final MasPaymentModeRepository masPaymentModeRepository;
    private final IpPaymentDetailRepository ipPaymentDetailRepository;
    private final IpMarDetailsRepository ipMarDetailsRepository;
    private final IpMarDetailsMapper ipMarDetailsMapper;
    private final IpProcedureTxnRepository ipProcedureTxnRepository;
    private final IpProcedureTxnMapper ipProcedureTxnMapper;
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
    private LabHdRepository labHdRepository;
    @Autowired
    private LabDtRepository labDtRepository;
    @Autowired
    private RadOrderHdRepository radOrderHdRepository;
    @Autowired
    private RadOrderDtRepository radOrderDtRepository;
    @Autowired
    private DgMasInvestigationRepository dgMasInvestigationRepository;
    @Autowired
    private MasSubChargeCodeRepository subChargeCodeRepository;
    @Autowired
    private LabOrderTrackingStatusRepository labOrderTrackingStatusRepository;
    @Autowired
    private com.hims.utils.RandomNumGenerator randomNumGenerator;
    @Autowired
    MasIpdTransferReasonRepository masIpdTransferReasonRepository;
    @Autowired
    IpTransferRequestRepository ipTransferRequestRepository;
    @Autowired
    MasIpdPaymentStatusRepository masIpdPaymentStatusRepository;
    @Autowired
    MasIpdBillStatusRepository masIpdBillStatusRepository;

    @Autowired
    HelperUtils helperUtils;
    @Autowired
    IpDischargeMedicationRepository ipDischargeMedicationRepository;
    @Autowired
    IpDischargeSummaryRepository ipDischargeSummaryRepository;
    @Autowired
    MasPatientDischargeConditionRepository masPatientDischargeConditionRepository;
    @Autowired
    MasDischargeReasonRepository masDischargeReasonRepository;
    @Autowired
    IpdBlReceiptHdRepository ipdBlReceiptHdRepository;
    @Autowired
    IpdBlReceiptDtRepository ipdBlReceiptDtRepository;
    @Autowired
    MasReceiptTypeRepository masReceiptTypeRepository;
    @Autowired
    TransactionSequenceService transactionSequenceService;
    @Autowired
    IpMedicinePrescriptionRepository ipMedicinePrescriptionRepository;
    @Autowired
    MasStoreItemRepository masStoreItemRepository;
    @Autowired
    MasFrequencyRepository masFrequencyRepository;
    @Autowired
    MasMainChargeCodeRepository masMainChargeCodeRepository;
    @Autowired
    MasIpdServiceSubcategoryRepository masIpdServiceSubcategoryRepository;
    @Autowired
    MasInvestigationPriceDetailsRepository masInvestigationPriceDetailsRepository;
    @Autowired
    StoreItemBatchStockRepository storeItemBatchStockRepository;
    @Autowired
    StoreIssueMRepository storeIssueMRepository;
    @Autowired
    StoreIssueTRepository storeIssueTRepository;
    @Autowired
    StoreStockLedgerRepository storeStockLedgerRepository;
    @Autowired
    IpMedicineIssueRepository ipMedicineIssueRepository;
    @Autowired
    MasProcedureRepository masProcedureRepository;
    @Autowired
    MasProcedureConsumableTemplateRepository masProcedureConsumableTemplateRepository;
    @Autowired
    MasProcedureConsumableTemplateDetailRepository masProcedureConsumableTemplateDetailRepository;
    @Autowired
    IpConsumableTxnRepository ipConsumableTxnRepository;
    @Autowired
    InventoryUtils inventoryUtils;
    @Autowired
    ItemClassBillSubcategoryMappingRepository itemClassBillSubcategoryMappingRepository;
    @Autowired
    IpAdverseEventRepository ipAdverseEventRepository;


    @Value("${ipd.admission.status.admitted}")
    Long admitAdmissionStatusId;

    @Value("${bed.status.available.id}")
    Long bedStatusId;
    @Value("${bed.status.Occupied.id}")
    Long bedStatusOccupiedId;
    @Value("${ip.internal.status.nrw.id}")
    Long ipInternalStatusNrwId;
    @Value("${ip.internal.status.rw.id}")
    Long ipInternalStatusRwId;

    @Value("${ipd.service.category.ipd.consultation.id}")
    Long ipdServiceCategoryId;

    @Value("${ipd.investigation.service.category.id}")
    Long ipdInvestigationServiceCategoryId;

    @Value("${app.laboratoryDepartment}")
    Long laboratoryDepartment;
    @Value("${app.radiologyDepartment}")
    Long radiologyDepartment;
    @Value("${lab.track-order-status-reg.ordered}")
    Long labOrderedStatusId;
    @Value("${bed.status.transfer.request.id}")
    Long bedStatusTransferRequestId;
    @Value("${ip.internal.status.transfer.pending.id}")
    Long ipInternalStatusTransferPendingId;

    @Value("${ip.payment.status.partial}")
    Long ipPaymentStatusPartial;
    @Value("${ip.bill.status.open}")
    Long ipBillStatusOpen;
    @Value("${ip.bill.status.Interim}")
    Long ipBillStatusInterim;

    @Value("${ipd.admission.status.readyForDischarge}")
    Long readyForDischargeId;

    @Value("${ip.bill.status.final}")
    Long ipBillStatusFinal;
    @Value("${ip.payment.status.paid}")
    Long ipPaymentStatusPaid;

    @Value("${ipd.admission.status.discharge}")
    Long ipdDischargeStatusDischarge;

    @Value("${mas.receipt_type.advance.collection}")
    Long masReceiptTypeAdvanceCollection;

    @Value("${ip.payment.status.pending}")
    Long ipPaymentStatusPending;
    @Value("${ip.bill.status.close}")
    Long ipBillStatusClose;

    @Value("${ipd.service.category.drug}")
    Long IPDServiceCategoryDrug;
    @Value("${IPD.Service.Category.Medical.Consumables}")
    Long IPDServiceCategoryMedicalConsumables;

    @Value("${upload.image.path}")
    String filePath;



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

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<Page<IPDPatientWaitingListResponse>>() {
                    }
            );

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<Page<IPDPatientWaitingListResponse>>() {
                    }, e.getMessage(),
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

            saveDoctorDiagnosis(request, inpatient, patient);

            saveIpdBillingAndPaymentDetails(request, inpatient);

            log.info("Saving IPD patient details completed for patientId: {}, inpatientId: {}", patient.getId(), inpatient.getInpatientId());

            return ResponseUtils.createSuccessResponse("IPD patient details saved successfully", new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error while saving IPD patient details for patientId: {}. Error: {}", request != null ? request.getPatientId() : null, e.getMessage(),
                    e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, e.getMessage(),
                    400
            );
        }
    }

    @Override
    public ApiResponse<List<IpdWardResponse>> getWardDetailsByDepartment(Long departmentId) {
        try {
            List<IpdWardResponse> wardList = masWardRepository.getWardByDepartment(departmentId, AppConstants.STATUS_Y.toLowerCase());

            return ResponseUtils.createSuccessResponse(wardList, new TypeReference<List<IpdWardResponse>>() {
            });

        } catch (Exception e) {
            log.error("Error while fetching wards for departmentId: {}", departmentId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<List<IpdRoomResponse>> getRoomDetailsByWard(Long wardId) {
        try {
            List<IpdRoomResponse> roomList = masRoomRepository.getRoomByWard(wardId, bedStatusId, AppConstants.STATUS_Y.toLowerCase());

            return ResponseUtils.createSuccessResponse(roomList, new TypeReference<List<IpdRoomResponse>>() {
            });

        } catch (Exception e) {
            log.error("Error while fetching wards for departmentId: {}", wardId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    @Override
    public ApiResponse<List<WardResponse>> getWardDetailsByCategory(Long wardCategoryId) {

        try {
            log.info("Fetching wards and available bed count for wardCategoryId: {}", wardCategoryId);

            List<WardResponse> wardList = masWardRepository.getWardsByCategory(wardCategoryId, bedStatusId, AppConstants.STATUS_Y.toLowerCase());

            return ResponseUtils.createSuccessResponse(wardList, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error while fetching wards for wardCategoryId: {}", wardCategoryId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, AppConstants.INTERNAL_SERVER_ERR_MSG, 500
            );
        }
    }

    @Override
    public ApiResponse<List<BedResponse>> getBedDetailsByRoom(Long roomId) {
        try {
            log.info("Fetching rooms and available bed count for roomId: {}", roomId);

            List<MasBed> beds = masBedRepository.findAllActiveBedsByRoomId(roomId, bedStatusId, AppConstants.STATUS_Y.toLowerCase());

            List<BedResponse> bedResponses = beds.stream()
                    .map(bed -> new BedResponse(
                            bed.getBedId(),
                            bed.getBedNumber()
                    ))
                    .toList();

            return ResponseUtils.createSuccessResponse(bedResponses, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error while fetching wards for wardCategoryId: {}", roomId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, AppConstants.INTERNAL_SERVER_ERR_MSG, 500
            );
        }
    }

    @Override
    public ApiResponse<List<WardWiseDetailsResponse>> getNursingDashboardByWard(Long wardId) {
        try {

            List<WardWiseDetailsProjection> projections = ipBedAllocationRepository.getWardWiseDetails(wardId, admitAdmissionStatusId);

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
                            item.getDoctor(),
                            item.getDoctorId(),
                            item.getDiagnosisId(),
                            item.getDiagnosisType(),
                            item.getDiagnosis()

                    ))
                    .toList();

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error while fetching getWardWiseDetails", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, AppConstants.INTERNAL_SERVER_ERR_MSG, 500
            );
        }

    }

    @Override
    public ApiResponse<TotalBedCountResponse> getTotalBedCountByWard(Long wardId) {

        log.info("Fetching total bed count for wardId: {}", wardId);

        TotalBedCountResponse response = masBedRepository.getTotalBedCountByDepartmentId(wardId, bedStatusId, bedStatusOccupiedId);

        if (response == null) {
            response = new TotalBedCountResponse(0L, 0L, 0L, null);
        }

        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
        });

    }

    @Override
    @Transactional
    public ApiResponse<String> saveNursingMedicalAssessment(IpNursingMedicalAssessmentRequest request) {

        log.info("Saving IP nursing medical assessment. inpatientId: {}, hospitalId: {}",
                request.getInpatientId(),
                request.getHospitalId()
        );

        try {
            User user = authUtil.getCurrentUser();

            Optional<Inpatient> inpatient = inpatientRepository.findById(request.getInpatientId());
            if (inpatient.isEmpty()) {

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Inpatient not found", HttpStatus.NOT_FOUND.value());

            }

            Optional<MasHospital> hospital = masHospitalRepository.findById(request.getHospitalId());
            if (hospital.isEmpty()) {

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Hospital not found", HttpStatus.NOT_FOUND.value());

            }
            Optional<IpNursingMedicalAssessment> existingAssessment = ipNursingMedicalAssessmentRepository
                    .findTopByInpatientInpatientIdOrderByAssessmentIdDesc(request.getInpatientId());
            IpNursingMedicalAssessment assessment = existingAssessment.orElseGet(IpNursingMedicalAssessment::new);
            boolean isNewAssessment = existingAssessment.isEmpty();

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
            if (isNewAssessment) {
                assessment.setCreatedBy(user.getFullName());
                assessment.setCreatedDate(LocalDateTime.now());
            }
            assessment.setUpdatedBy(user.getFullName());
            assessment.setUpdatedDate(LocalDateTime.now());

            IpNursingMedicalAssessment savedAssessment = ipNursingMedicalAssessmentRepository.save(assessment);

            // Save an entry in ip_vitals
            saveIpVitals(request, inpatient.get(), user);


            log.info("IP nursing medical assessment saved successfully. assessmentId: {}, inpatientId: {}",
                    savedAssessment.getAssessmentId(),
                    request.getInpatientId());

            return ResponseUtils.createSuccessResponse("IP nursing medical assessment saved successfully. Assessment ID: "
                    + savedAssessment.getAssessmentId(), new TypeReference<>() {
            });

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
    @Transactional(readOnly = true)
    public ApiResponse<IpNursingMedicalAssessmentResponse> getNursingMedicalAssessment(Long inpatientId) {

        log.info("Fetching nursing medical assessment details for inpatientId: {}", inpatientId);

        try {
            if (!inpatientRepository.existsById(inpatientId)) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Inpatient not found", HttpStatus.NOT_FOUND.value());
            }

            Optional<IpNursingMedicalAssessmentProjection> assessment = ipNursingMedicalAssessmentRepository
                    .getNursingMedicalAssessmentByInpatientId(inpatientId);

            if (assessment.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "No nursing medical assessment details found for inpatient ID: " + inpatientId, HttpStatus.NOT_FOUND.value());
            }

            IpNursingMedicalAssessmentResponse response = mapNursingMedicalAssessmentToResponse(assessment.get());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
            });

        } catch (Exception exception) {
            log.error("Error while fetching nursing medical assessment details for inpatientId: {}", inpatientId, exception);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private IpNursingMedicalAssessmentResponse mapNursingMedicalAssessmentToResponse(IpNursingMedicalAssessmentProjection assessment) {
        IpNursingMedicalAssessmentResponse response = new IpNursingMedicalAssessmentResponse();

        response.setAssessmentId(assessment.getAssessmentId());
        response.setInpatientId(assessment.getInpatientId());
        response.setHospitalId(assessment.getHospitalId());
        response.setHospitalName(assessment.getHospitalName());
        response.setConsciousness(assessment.getConsciousness());
        response.setGcsScore(assessment.getGcsScore());
        response.setPainScore(assessment.getPainScore());
        response.setMobilityStatus(assessment.getMobilityStatus());
        response.setFallRisk(assessment.getFallRisk());
        response.setPressureSoreRisk(assessment.getPressureSoreRisk());
        response.setSkinCondition(assessment.getSkinCondition());
        response.setSkinRemarks(assessment.getSkinRemarks());
        response.setIvLinePresent(assessment.getIvLinePresent());
        response.setIvSite(assessment.getIvSite());
        response.setCatheterPresent(assessment.getCatheterPresent());
        response.setCatheterType(assessment.getCatheterType());
        response.setDrainPresent(assessment.getDrainPresent());
        response.setDrainType(assessment.getDrainType());
        response.setNutritionRisk(assessment.getNutritionRisk());
        response.setNutritionRemarks(assessment.getNutritionRemarks());
        response.setInfectionRisk(assessment.getInfectionRisk());
        response.setInfectionRemarks(assessment.getInfectionRemarks());
        response.setPatientOrientationDone(assessment.getPatientOrientationDone());
        response.setRelativeOrientationDone(assessment.getRelativeOrientationDone());
        response.setNursingCarePlan(assessment.getNursingCarePlan());
        response.setChiefComplaint(assessment.getChiefComplaint());
        response.setHistoryPresentIllness(assessment.getHistoryPresentIllness());
        response.setFamilyHistory(assessment.getFamilyHistory());
        response.setMedicationHistory(assessment.getMedicationHistory());
        response.setAllergies(assessment.getAllergies());
        response.setPulse(assessment.getPulse());
        response.setSystolicBp(assessment.getSystolicBp());
        response.setDiastolicBp(assessment.getDiastolicBp());
        response.setTemperature(assessment.getTemperature());
        response.setTemperatureUnit(assessment.getTemperatureUnit());
        response.setRespiratoryRate(assessment.getRespiratoryRate());
        response.setSpo2(assessment.getSpo2());
        response.setGeneralExaminationNotes(assessment.getGeneralExaminationNotes());
        response.setRsExamination(assessment.getRsExamination());
        response.setCvsExamination(assessment.getCvsExamination());
        response.setPaExamination(assessment.getPaExamination());
        response.setCnsExamination(assessment.getCnsExamination());
        response.setProvisionalDiagnosis(assessment.getProvisionalDiagnosis());
        response.setStatus(assessment.getStatus());
        response.setCreatedBy(assessment.getCreatedBy());
        response.setCreatedDate(assessment.getCreatedDate());
        response.setUpdatedBy(assessment.getUpdatedBy());
        response.setUpdatedDate(assessment.getUpdatedDate());

        return response;
    }

    @Override
    public ApiResponse<String> updateAdmissionInternalStatus(Long inpatientId, Long internalStatusId) {
        try {
            User user = authUtil.getCurrentUser();

            Inpatient inpatient = inpatientRepository.findById(inpatientId).orElseThrow(() -> new RuntimeException("Inpatient not found with id: "
                    + inpatientId));
            inpatient.setMasIpdInternalStatus(masIpdInternalStatusRepository.findById(internalStatusId).orElseThrow());
            inpatient.setLastUpdateDate(LocalDateTime.now());
            inpatient.setLastUpdatedBy(user.getFullName());
            inpatientRepository.save(inpatient);

            return ResponseUtils.createSuccessResponse("Ip internal status change successfully", new TypeReference<>() {
            });

        } catch (Exception exception) {
            log.error("Error while saving Ip internal status change", exception);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, AppConstants.INTERNAL_SERVER_ERR_MSG, 500
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

                return ResponseUtils.createFailureResponse(Collections.emptyList(), new TypeReference<>() {
                }, "No vitals details found for inpatient ID: " + inpatientId, 404);
            }

            List<IpVitalsResponse> responseList = vitalsList.stream()
                    .map(this::mapVitalsProjectionToResponse)
                    .toList();

            log.info("Successfully fetched {} vitals records for inpatientId: {}", responseList.size(), inpatientId);

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
            });

        } catch (Exception e) {

            log.error("Error while fetching vitals history for inpatientId: {}", inpatientId, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
        }
    }

    @Override
    public ApiResponse<String> saveVitalsDetails(IpVitalsRequest request) {
        log.info("Saving vitals details started for inpatientId: {}", request.getInpatientId());

        try {
            User user = authUtil.getCurrentUser();
            Inpatient inpatient = inpatientRepository.findById(request.getInpatientId()).orElse(null);

            if (inpatient == null) {
                log.warn("Inpatient not found for inpatientId: {}", request.getInpatientId());

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Inpatient not found with ID: " + request.getInpatientId(), 404);
            }

            IpVitals ipVitals = new IpVitals();

            ipVitals.setInpatient(inpatient);
            ipVitals.setObservationDatetime(LocalDateTime.now());
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

            return ResponseUtils.createSuccessResponse("Vitals details saved successfully", new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error while saving vitals details for inpatientId: {}", request.getInpatientId(), e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
        }
    }

    @Override
    public ApiResponse<String> saveIntakeOutputDetails(IpIntakeOutputSaveRequest request) {

        log.info("Saving intake/output details started for inpatientId: {}", request.getInpatientId());

        try {
            if (request.getEntries() == null || request.getEntries().isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "At least one intake/output entry is required", 400);
            }

            Inpatient inpatient = inpatientRepository.findById(request.getInpatientId()).orElse(null);

            if (inpatient == null) {
                log.warn("Inpatient not found with inpatientId: {}", request.getInpatientId());

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Inpatient not found with ID: " + request.getInpatientId(), 404);
            }

            Patient patient = inpatient.getPatient();

            if (patient == null) {
                log.warn("Patient is not associated with inpatientId: {}", request.getInpatientId());

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Patient is not associated with this inpatient", 400);
            }

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

            return ResponseUtils.createSuccessResponse(" intake/output entries saved successfully", new TypeReference<>() {
            });

        } catch (Exception e) {

            log.error("Error while saving intake/output details for inpatientId: {}", request.getInpatientId(), e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
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
                            "Consultation fee is not defined for this doctor"));


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
                            .visitType(visitType)
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
            String name = consultationTariff.getDoctor().getFullName();

            /*
             * Create billing using the matched tariff.
             *
             */
            saveIpdBillingDetails.saveInpatientBillingDetails(inpatient, rate, quantity, gstPercent, discountAmount, amount, gstAmount, netAmount, billingCategory, null, name);

            log.info("Daily case sheet entry saved successfully. " + "caseSheetEntryId: {}, inpatientId: {}, tariffId: {}",
                    savedCaseSheetEntry.getCaseSheetEntryId(),
                    inpatient.getInpatientId(),
                    consultationTariff.getTariffId()
            );

            return ResponseUtils.createSuccessResponse("Daily case sheet entry saved successfully", new TypeReference<>() {
            });

        } catch (Exception e) {

            log.error("Error while saving daily case sheet entry. inpatientId: {}", request.getInpatientId(), e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, e.getMessage(), 404);
        }
    }

    @Override
    public ApiResponse<List<DailyCaseSheetEntryResponse>> getDailyCaseSheetEntry(Long inpatientId) {

        log.info("Fetching daily case sheet entries for inpatientId: {}", inpatientId);

        try {
            List<DailyCaseSheetEntryProjectionResponse> projections = ipDailyCaseSheetEntryRepository.findDailyCaseSheetEntries(inpatientId);

            List<DailyCaseSheetEntryResponse> responseList =
                    projections.stream().map(this::mapToDailyCaseSheetResponse).toList();

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
            });

        } catch (Exception exception) {

            log.error("Error while fetching daily case sheet entries. " + "inpatientId: {}", inpatientId, exception);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, AppConstants.INTERNAL_SERVER_ERR_MSG + exception.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<List<BedDetailsByWardResponse>> getBedDetailsByWard(Long wardId) {
        log.info("Fetching bed details for wardId: {}", wardId);

        try {
            List<BedDetailsByWardResponse> bedDetails = masBedRepository.getBedDetailsByWard(wardId, bedStatusId);

            log.info("Bed details fetched successfully for wardId: {}, totalBeds: {}", bedDetails.size());

            return ResponseUtils.createSuccessResponse(bedDetails, new TypeReference<>() {
            });

        } catch (Exception exception) {

            log.error("Error while fetching bed details for wardId: {}", wardId, exception);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, "Unable to fetch bed details: " + exception.getMessage(),
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
                    .transferNo(generateTransferNumber())
                    .transferReason(transferReason)
                    .priority(request.getPriority())
                    .clinicalNotes(request.getClinicalNotes())
                    .requestDatetime(LocalDateTime.now())
                    .requestedBy(currentUser.getFullName())
                    .transferStatus(AppConstants.IPD_BED_TRANSFER_REQUEST)
                    .createdBy(currentUser.getFullName())
                    .createdDate(LocalDateTime.now())
                    .lastUpdatedBy(currentUser.getFullName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            IpTransferRequest savedRequest = ipTransferRequestRepository.save(transferRequest);
            inpatient.setMasIpdInternalStatus(masIpdInternalStatusRepository.findById(ipInternalStatusTransferPendingId).orElseThrow());
            inpatientRepository.save(inpatient);
            toBed.setBedStatusId(masBedStatusRepo.findById(bedStatusTransferRequestId).orElseThrow());
            masBedRepository.save(toBed);


            log.info("Bed transfer request saved successfully. transferId: {}, transferNo: {}",
                    savedRequest.getTransferId(),
                    savedRequest.getTransferNo());

            return ResponseUtils.createSuccessResponse("Bed transfer request saved successfully. Transfer No: "
                            + savedRequest.getTransferNo(), new TypeReference<>() {
                    }
            );

        } catch (Exception e) {

            log.error("Error while saving bed transfer request. inpatientId: {}", request.getInpatientId(), e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, e.getMessage(),
                    500);
        }
    }

    @Override
    public ApiResponse<List<PendingToTransferResponse>> wardPendingToTransferRequest(List<Long> wardIds) {

        log.info(
                "Fetching pending transfer requests for wardIds: {}",
                wardIds
        );

        try {
            List<PendingToTransferProjectionResponse> projectionList = ipTransferRequestRepository
                    .findPendingTransferRequestsByWardId(wardIds, AppConstants.IPD_BED_TRANSFER_REQUEST.toLowerCase());

            List<PendingToTransferResponse> responseList = projectionList.stream()
                    .map(this::mapToPendingTransferResponse)
                    .toList();

            log.info("Found {} pending transfer requests for destination wardId: {}", responseList.size(), wardIds);

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error while fetching pending transfer requests for wardId: {}", wardIds, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
        }
    }

    //    @Override
//    @Transactional
//    public ApiResponse<String> wardPendingToTransferRequestStatusCompleteAndReject(Long inpatientId, String transferStatus) {
//        User user=authUtil.getCurrentUser();
//        Optional<IpTransferRequest> ipTransferRequest = ipTransferRequestRepository.findById(inpatientId);
//        IpTransferRequest ipTransferRequest1= ipTransferRequest.get();
//        Optional<Inpatient> inpatient=inpatientRepository.findById(inpatientId);
//        Inpatient inpatient1=inpatient.get();
//        Optional<IpBedAllocation> bedAllocation=ipBedAllocationRepository.findById(inpatientId);
//        IpBedAllocation allocation=bedAllocation.get();
//        MasBed masBed=ipTransferRequest.get().getToBed();
//        if (transferStatus.equals("C")){
//
//            masBed.setBedStatusId(masBedStatusRepo.findById(bedStatusOccupiedId).orElseThrow());
//
//            ipTransferRequest1.setTransferStatus("C");
//            ipTransferRequest1.setTransferDatetime(LocalDateTime.now());
//            ipTransferRequest1.setAcceptanceDatetime(LocalDateTime.now());
//
//            inpatient1.setAdmittingWardId(ipTransferRequest1.getToWard());
//            inpatient1.setMasIpdInternalStatus(masIpdInternalStatusRepository.findById(ipInternalStatusId).orElseThrow());
//
//            allocation.setInpatient(inpatient1);
//            allocation.setPatient(inpatient1.getPatient());
//            allocation.setWard(ipTransferRequest1.getToWard());
//            allocation.setBed(ipTransferRequest1.getToBed());
//            allocation.setRoom(masBed.getRoomId());
//            allocation.setAllocationStartDate(LocalDateTime.now());
//            allocation.setCreatedBy(user.getFullName());
//            allocation.setLastUpdatedBy(user.getFullName());
//            allocation.setLastUpdateDate(LocalDateTime.now());
//
//        }else{
//            ipTransferRequest1.setTransferStatus("R");
//            ipTransferRequest1.setLastUpdatedBy(user.getFullName());
//            ipTransferRequest1.setLastUpdateDate(LocalDateTime.now());
//
//            masBed.setBedStatusId(masBedStatusRepo.findById(bedStatusId).orElseThrow());
//
//
//    }
//        return ResponseUtils.createSuccessResponse("Status change successfully", new TypeReference<>() {});
//
//    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> wardPendingToTransferRequestStatusCompleteAndReject(Long inpatientId, String transferStatus) {

        log.info("Updating transfer request status for inpatientId: {}, transferStatus: {}", inpatientId, transferStatus);

        try {
            // Convert status to uppercase to support values such as c, C, r and R.
            String normalizedTransferStatus = transferStatus.trim().toUpperCase();

            if (!normalizedTransferStatus.equals(AppConstants.IPD_BED_TRANSFER_STATUS_COMPLETE) && !normalizedTransferStatus.equals(AppConstants.IPD_BED_TRANSFER_STATUS_REJECT)) {
                return ResponseUtils.createFailureResponse("Invalid transfer status. Allowed values are C and R", new TypeReference<>() {
                }.toString(), HttpStatus.BAD_REQUEST.value());
            }

            // Get the currently logged-in user.
            User user = authUtil.getCurrentUser();

            if (user == null) {
                return ResponseUtils.createFailureResponse("current user not found", new TypeReference<>() {
                }.toString(), HttpStatus.NOT_FOUND.value());
            }

            String updatedBy = user.getFullName();
            LocalDateTime currentDateTime = LocalDateTime.now();

            /*
             * Fetch the transfer request.
             */
            Optional<IpTransferRequest> ipTransferRequest = ipTransferRequestRepository.findByInpatient_InpatientIdAndTransferStatusIgnoreCase(inpatientId,
                    AppConstants.IPD_BED_TRANSFER_REQUEST);

            if (ipTransferRequest.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Inpatient transfer request is not available",
                        HttpStatus.NOT_FOUND.value()
                );
            }
            IpTransferRequest ipTransferRequest1 = ipTransferRequest.get();

            // Fetch inpatient details.
            Inpatient inpatient = inpatientRepository.findById(inpatientId).orElseThrow(() -> new RuntimeException("Inpatient not found with ID: " + inpatientId));

            /*
             * Fetch the patient's current bed allocation
             *
             */

            // Get the destination bed selected in the transfer request.
            MasBed destinationBed = ipTransferRequest1.getToBed();

            MasBed fromBed = ipTransferRequest1.getFromBed();

            // new entry Ipd Bed Allocation
            IpBedAllocation bedAllocation = new IpBedAllocation();

            // Fetch the vacant/available bed status.
            MasBedStatus availableBedStatus = masBedStatusRepo.findById(bedStatusId)
                    .orElseThrow(() -> new RuntimeException("Available bed status not found with ID: " + bedStatusId));

            Optional<IpBedAllocation> previousAllocation = ipBedAllocationRepository.findFirstByInpatient_InpatientIdAndAllocationEndDateIsNullOrderByAllocationStartDateDesc(inpatientId);
            IpBedAllocation ipBedAllocation = previousAllocation.get();

            /*
             * Complete the transfer request.
             */
            if (AppConstants.IPD_BED_TRANSFER_STATUS_COMPLETE.equals(normalizedTransferStatus)) {

                // Fetch the occupied bed status.
                MasBedStatus occupiedBedStatus = masBedStatusRepo.findById(bedStatusOccupiedId)
                        .orElseThrow(() -> new RuntimeException("Occupied bed status not found with ID: " + bedStatusOccupiedId));

                // Mark the destination bed as occupied.
                destinationBed.setBedStatusId(occupiedBedStatus);
                // Mark the destination bed as
                fromBed.setBedStatusId(availableBedStatus);

                // Update the transfer-request details.
                ipTransferRequest1.setTransferStatus(AppConstants.IPD_BED_TRANSFER_STATUS_COMPLETE);
                ipTransferRequest1.setTransferDatetime(currentDateTime);
                ipTransferRequest1.setAcceptanceDatetime(currentDateTime);
                ipTransferRequest1.setAcceptedBy(updatedBy);
                ipTransferRequest1.setLastUpdatedBy(updatedBy);
                ipTransferRequest1.setLastUpdateDate(currentDateTime);

                // Update the inpatient's current ward.
                inpatient.setAdmittingWardId(ipTransferRequest1.getToWard());
                inpatient.setRoom(ipTransferRequest1.getToBed().getRoomId());
                inpatient.setBed(ipTransferRequest1.getToBed());

                // Update the inpatient's internal status.
                inpatient.setMasIpdInternalStatus(masIpdInternalStatusRepository.findById(ipInternalStatusNrwId).orElseThrow(() -> new RuntimeException(
                        "IPD internal status not found with ID: " + ipInternalStatusNrwId)));
                inpatient.setLastUpdatedBy(updatedBy);
                inpatient.setLastUpdateDate(currentDateTime);

                // Update the bed-allocation details with the destination ward and bed.
                bedAllocation.setInpatient(inpatient);
                bedAllocation.setPatient(inpatient.getPatient());
                bedAllocation.setWard(ipTransferRequest1.getToWard());
                bedAllocation.setBed(destinationBed);
                bedAllocation.setRoom(destinationBed.getRoomId());
                bedAllocation.setAllocationStartDate(currentDateTime);
                bedAllocation.setCreatedBy(updatedBy);
                bedAllocation.setLastUpdatedBy(updatedBy);
                bedAllocation.setLastUpdateDate(currentDateTime);

                ipBedAllocation.setAllocationEndDate(LocalDateTime.now());

                ipBedAllocationRepository.save(bedAllocation);
                log.info("Transfer request completed successfully for inpatientId: {}", inpatientId);

            } else {
                /*
                 * Reject the transfer request.
                 */

                // Update the transfer-request status as rejected.
                ipTransferRequest1.setTransferStatus(AppConstants.IPD_BED_TRANSFER_STATUS_REJECT);
                ipTransferRequest1.setLastUpdatedBy(updatedBy);
                ipTransferRequest1.setLastUpdateDate(currentDateTime);

                // Release the destination bed because the transfer was rejected.
                destinationBed.setBedStatusId(availableBedStatus);

                log.info("Transfer request rejected successfully for inpatientId: {}", inpatientId);
            }

            masBedRepository.save(destinationBed);
            ipTransferRequestRepository.save(ipTransferRequest1);
            inpatientRepository.save(inpatient);
            ipBedAllocationRepository.save(ipBedAllocation);

            String message = AppConstants.IPD_BED_TRANSFER_STATUS_COMPLETE.equals(normalizedTransferStatus) ? "Transfer completed successfully" : "Transfer rejected successfully";

            return ResponseUtils.createSuccessResponse(message, new TypeReference<>() {
            });


        } catch (Exception e) {
            log.error("Error while updating transfer status for inpatientId: {}", inpatientId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
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
                .visitTypeName(projection.getVisitTypeName())
                .visitTypeId(projection.getVisitTypeId())
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
        entity.setObservationDatetime(currentDateTime);
        entity.setIoType(ioType);
        entity.setLastUpdateDate(currentDateTime);
        entity.setCreatedBy(userName.getFullName());
        entity.setLastUpdatedBy(userName.getFullName());

        if (AppConstants.IO_TYPE_I.equals(ioType)) {

            MasIntakeType intakeType = masIntakeTypeRepository.getReferenceById(request.getIntakeTypeId());

            MasIntakeItem intakeItem = masIntakeItemRepository.getReferenceById(request.getIntakeItemId());

            entity.setIntakeType(intakeType);
            entity.setIntakeItem(intakeItem);
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

        IpDiagnosisEntry ipDiagnosisEntry = new IpDiagnosisEntry();
        ipDiagnosisEntry.setDiagnosisDatetime(LocalDateTime.now());
        ipDiagnosisEntry.setInpatient(inpatient);
        ipDiagnosisEntry.setPatient(patient);
        ipDiagnosisEntry.setDepartment(masDepartmentRepository.findById(request.getDepartmentId()).orElseThrow());
        ipDiagnosisEntry.setRecordedBy(userRepo.findById(request.getTreatingDoctor()).orElseThrow());
        ipDiagnosisEntry.setDiagnosisType(AppConstants.WORKING_DIAGNOSIS_TYPE);
        ipDiagnosisEntry.setDiagnosisText(request.getWorkingDiagnosis());
        ipDiagnosisEntry.setCreatedBy(user.getFullName());
        ipDiagnosisEntry.setLastUpdateDate(LocalDateTime.now());
        ipDiagnosisEntry.setLastUpdatedBy(user.getFullName());


        ipDiagnosisEntryRepository.save(ipDiagnosisEntry);
        log.info("IpDiagnosisEntry saved successfully for inpatientId: {}", inpatient.getInpatientId());
    }

    private void saveIpdBillingAndPaymentDetails(IpdPatientRequest request, Inpatient inpatient) {

        User user = authUtil.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        // ====================== Calculate Total Advance ======================
        BigDecimal totalAdvance = BigDecimal.ZERO;

        if (request.getPaymentRequests() != null && !request.getPaymentRequests().isEmpty()) {
            totalAdvance = request.getPaymentRequests().stream()
                    .map(IpdPatientRequest.PaymentRequest::getAdvanceAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // ====================== Billing Type ======================
        MasIpdBillingType billingType = null;

        if (request.getPaymentRequests() != null && !request.getPaymentRequests().isEmpty()) {
            Long billingTypeId = request.getPaymentRequests().get(0).getPaymentType();

            if (billingTypeId != null) {
                billingType = masIpdBillingTypeRepository.findById(billingTypeId)
                        .orElseThrow(() -> new RuntimeException("Invalid Billing Type Id : " + billingTypeId));
            }
        }
        // ====================== Billing Header ======================
        IpdBillingHeader billingHeader = new IpdBillingHeader();

        billingHeader.setUhid(request.getUhid());
        billingHeader.setInpatientId(inpatientRepository.findById(inpatient.getInpatientId()).orElseThrow());
        billingHeader.setPatientName(request.getPatientName());
        billingHeader.setEstimationCost(request.getEstimationCost());
        billingHeader.setBillingType(billingType);
        billingHeader.setPatientPaidAmount(totalAdvance);


        // ====================== Outstanding Amount ======================

        BigDecimal netAmount = billingHeader.getNetAmount() != null
                ? billingHeader.getNetAmount()
                : BigDecimal.ZERO;

        BigDecimal outstandingAmount = netAmount.subtract(totalAdvance);

        billingHeader.setOutstandingAmount(outstandingAmount);
        billingHeader.setBillStatus(masIpdBillStatusRepository.findById(ipBillStatusInterim).orElseThrow());
        billingHeader.setPaymentStatus(masIpdPaymentStatusRepository.findById(ipPaymentStatusPending).orElseThrow());
        billingHeader.setCreatedBy(user.getFullName());
        billingHeader.setUpdatedBy(user.getFullName());
        billingHeader.setCreatedAt(now);
        billingHeader.setUpdatedAt(now);

        IpdBillingHeader savedBillingHeader = ipdBillingHeaderRepository.save(billingHeader);

        log.info("Billing Header Saved Successfully. Bill Id : {}", savedBillingHeader.getBillId());

        // ====================== No Advance Paid ======================
        if (totalAdvance.compareTo(BigDecimal.ZERO) <= 0) {

            log.info("No advance payment collected. Only Billing Header created for InpatientId : {}",
                    inpatient.getInpatientId());

            return;
        }

        // ====================== Receipt Header ======================
        IpdBlReceiptHd receiptHd = new IpdBlReceiptHd();

        receiptHd.setReceiptNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.RECEIPT_NO, inpatient.getPatient().getPatientHospital().getId()));

        receiptHd.setReceiptDate(now);
        receiptHd.setInpatient(inpatient);
        receiptHd.setBill(savedBillingHeader);

        receiptHd.setReceiptType(masReceiptTypeRepository.findById(masReceiptTypeAdvanceCollection)
                .orElseThrow(() -> new RuntimeException("Invalid Receipt Type")));

        receiptHd.setTotalAmount(totalAdvance);
        receiptHd.setReceiptStatus(AppConstants.IP_RECEIPT_STATUS.toLowerCase());
        receiptHd.setCreatedBy(user.getFullName());
        receiptHd.setCreatedDate(now);
        receiptHd.setLastChgBy(user.getFullName());
        receiptHd.setLastChgDate(now);

        IpdBlReceiptHd savedReceiptHd = ipdBlReceiptHdRepository.save(receiptHd);

        log.info("Receipt Header Saved Successfully. Receipt Id : {}",
                savedReceiptHd.getReceiptId());

        // ====================== Payment Details & Receipt Details ======================
        for (IpdPatientRequest.PaymentRequest payment : request.getPaymentRequests()) {

            if (payment.getAdvanceAmount() == null
                    || payment.getAdvanceAmount().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            MasPaymentMode paymentMode = masPaymentModeRepository.findById(payment.getPaymentMode())
                    .orElseThrow(() -> new RuntimeException(
                            "Invalid Payment Mode : " + payment.getPaymentMode()));

            // -------- Payment Detail --------
            IpPaymentDetail paymentDetail = new IpPaymentDetail();

            paymentDetail.setInpatient(inpatient);
            paymentDetail.setBill(savedBillingHeader);
            paymentDetail.setAmount(payment.getAdvanceAmount());
            paymentDetail.setPaymentDate(now);
            paymentDetail.setPaymentStatus(masIpdPaymentStatusRepository.findById(ipPaymentStatusPaid).orElseThrow());
            paymentDetail.setReceipt(savedReceiptHd);
            paymentDetail.setReceiptAmount(payment.getAdvanceAmount());
            paymentDetail.setLastChgBy(user.getFullName());
            paymentDetail.setLastChgDate(now);

            ipPaymentDetailRepository.save(paymentDetail);

            // -------- Receipt Detail --------
            IpdBlReceiptDt receiptDt = new IpdBlReceiptDt();

            receiptDt.setReceipt(savedReceiptHd);
            receiptDt.setPaymentMode(paymentMode);
            receiptDt.setAmount(payment.getAdvanceAmount());
            receiptDt.setCreatedBy(user.getFullName());
            receiptDt.setCreatedDate(now);
            receiptDt.setLastChgBy(user.getFullName());
            receiptDt.setLastChgDate(now);

            ipdBlReceiptDtRepository.save(receiptDt);

            log.info("Payment Detail & Receipt Detail saved. Mode : {}, Amount : {}",
                    paymentMode.getModeName(),
                    payment.getAdvanceAmount());
        }

        log.info(
                "IPD Billing & Payment Process Completed Successfully. InpatientId : {}, BillId : {}, ReceiptId : {}",
                inpatient.getInpatientId(),
                savedBillingHeader.getBillId(),
                savedReceiptHd.getReceiptId());
    }

    private Inpatient saveInpatientDetails(IpdPatientRequest request, Patient patient, Visit visit) {
        User user = authUtil.getCurrentUser();
        // find doctor
        User user1= userRepo.findById(request.getTreatingDoctor()).orElseThrow();
        Inpatient inpatient = new Inpatient();

        inpatient.setPatient(patient);
        inpatient.setVisit(visit);
        inpatient.setAdmissionDate(request.getAdmissionDate());
        inpatient.setAdmissionTime(request.getAdmissionTime());
        inpatient.setAdmissionNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.ADMISSION_NO, patient.getPatientHospital().getId()));
        inpatient.setConsentTakenBy(request.getConsentTakenBy());
        inpatient.setMlcCase(request.getMlcCase());
        inpatient.setPoliceIntimationRequired(request.getPoliceIntimationRequired());
        inpatient.setAdmissionAdvisedFrom(request.getAdmissionAdvisedFrom());
        inpatient.setAdmissionConsentTaken(request.getAdmissionConsentTaken());
        inpatient.setAdmissionStatus(masAdmissionStatusRepository.findById(admitAdmissionStatusId).orElseThrow());
        inpatient.setDietPreference(masDietPreferenceRepository.findById(request.getDietPreferenceId()).orElseThrow());
        inpatient.setMasIpdInternalStatus(masIpdInternalStatusRepository.findById(ipInternalStatusNrwId).orElseThrow());
        inpatient.setRoom(masRoomRepository.findById(request.getRoomId()).orElseThrow());
        inpatient.setBed(masBedRepository.findById(request.getBedId()).orElseThrow());
        inpatient.setInitialDiagnosis(request.getWorkingDiagnosis());
        inpatient.setDoctor(user1);
        inpatient.setDoctorName(user1.getFullName());


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


        String uploadDir = filePath;

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

                // Normalize path separators to forward slashes for consistent storage
                String normalizedFilePath = finalFilePath.toString().replace("\\", "/");

                IpDocument document = new IpDocument();

                document.setInpatient(inpatient);
                document.setPatient(patient);
                document.setDocumentDatetime(LocalDateTime.now());
                document.setDocumentType(docReq.getDocumentType());
                document.setFileName(originalFileName);
                document.setFilePath(normalizedFilePath);
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

    private synchronized String generateAdmissionNo() {

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

    private PendingToTransferResponse mapToPendingTransferResponse(
            PendingToTransferProjectionResponse projection) {

        return PendingToTransferResponse.builder()
                .inpatientId(projection.getInpatientId())
                .patientId(projection.getPatientId())
                .transferNo(projection.getTransferNo())
                .transferDateTime(projection.getTransferDateTime())
                .patientName(projection.getPatientName())
                .gender(projection.getGender())
                .age(projection.getAge())
                .admissionNo(projection.getAdmissionNo())
                .admissionDate(projection.getAdmissionDate())
                .fromWardId(projection.getFromWardId())
                .fromWardName(projection.getFromWardName())
                .fromBedId(projection.getFromBedId())
                .fromBedName(projection.getFromBedName())
                .toWardId(projection.getToWardId())
                .toWardName(projection.getToWardName())
                .toBedId(projection.getToBedId())
                .toBedName(projection.getToBedName())
                .transferReasonId(projection.getTransferReasonId())
                .transferReason(projection.getTransferReason())
                .transferStatus(projection.getTransferStatus())
                .clinicalNotes(projection.getClinicalNotes())
                .doctorId(projection.getDoctorId())
                .doctorName(projection.getDoctorName())
                .uhidNO(projection.getUhidNo())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> saveInpatientBookingInvestigation(@Valid InpatientBookingInvestigationRequest request) {
        try {
            if (request == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Request body is required", HttpStatus.BAD_REQUEST.value());
            }

            if (request.getPatientId() == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "patientId is required", HttpStatus.BAD_REQUEST.value());
            }

            if (request.getInpatientId() == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "inpatientId is required", HttpStatus.BAD_REQUEST.value());
            }

            Inpatient inpatient = inpatientRepository.findById(request.getInpatientId())
                    .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(), "Inpatient not found with id: " + request.getInpatientId()));

            Patient patient = inpatient.getPatient();
            if (patient == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Inpatient is not linked to a patient", HttpStatus.BAD_REQUEST.value());
            }

            if (request.getPatientId() != null && !Objects.equals(request.getPatientId(), patient.getId())) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "patientId does not match the inpatient record", HttpStatus.BAD_REQUEST.value());
            }

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            List<LabRadioInvestigationRequest> investigations = resolveInvestigations(request);
            if (investigations.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "At least one investigation is required", HttpStatus.BAD_REQUEST.value());
            }

            Map<Long, DgMasInvestigation> masterMap = dgMasInvestigationRepository.findAllById(
                            investigations.stream()
                                    .map(LabRadioInvestigationRequest::getId)
                                    .filter(Objects::nonNull)
                                    .distinct()
                                    .toList()
                    )
                    .stream()
                    .collect(Collectors.toMap(DgMasInvestigation::getInvestigationId, investigation -> investigation));

            if (masterMap.size() != investigations.stream().map(LabRadioInvestigationRequest::getId).filter(Objects::nonNull).distinct().count()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "One or more investigation IDs are invalid", HttpStatus.BAD_REQUEST.value());
            }

            Map<Long, Map<LocalDate, List<LabRadioInvestigationRequest>>> grouped =
                    investigations.stream()
                            .filter(item -> item.getId() != null)
                            .collect(Collectors.groupingBy(
                                    item -> helperUtils.getDepartmentFromInvestigation(item.getId()),
                                    Collectors.groupingBy(item -> item.getAppointmentDate() != null ? item.getAppointmentDate() : LocalDate.now())
                            ));

            LocalTime now = LocalTime.now();
            String userName = currentUser.getFullName();
            List<Long> createdLabOrderIds = new ArrayList<>();
            List<Long> createdRadOrderIds = new ArrayList<>();
            MasIpdServiceCategory billingCategory = masIpdServiceCategoryRepository.findById(ipdInvestigationServiceCategoryId)
                    .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(),
                            "IPD service category not found with id: " + ipdInvestigationServiceCategoryId));

            Map<LocalDate, List<LabRadioInvestigationRequest>> labGroups = grouped.getOrDefault(laboratoryDepartment, Collections.emptyMap());
            for (Map.Entry<LocalDate, List<LabRadioInvestigationRequest>> entry : labGroups.entrySet()) {
                LocalDate appointmentDate = entry.getKey();
                DgOrderHd orderHd = buildLabOrderHeader(inpatient, currentUser, appointmentDate, now);
                DgOrderHd savedHd = labHdRepository.save(orderHd);
                LabOrderTrackingStatus orderedStatus = labOrderTrackingStatusRepository.findById(labOrderedStatusId)
                        .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(), "Lab ordered status not found with id: " + labOrderedStatusId));

                for (LabRadioInvestigationRequest item : entry.getValue()) {
                    DgMasInvestigation master = masterMap.get(item.getId());
                    DgOrderDt orderDt = buildLabOrderDetail(
                            savedHd,
                            master,
                            currentUser,
                            appointmentDate,
                            now,
                            orderedStatus,
                            item.getRemarks()
                    );
                    labDtRepository.save(orderDt);
                    saveInvestigationBillingDetails(inpatient, billingCategory, master);

                }
                createdLabOrderIds.add((long) savedHd.getId());

            }

            Map<LocalDate, List<LabRadioInvestigationRequest>> radGroups = grouped.getOrDefault(radiologyDepartment, Collections.emptyMap());
            for (Map.Entry<LocalDate, List<LabRadioInvestigationRequest>> entry : radGroups.entrySet()) {
                LocalDate appointmentDate = entry.getKey();
                RadOrderHd orderHd = buildRadiologyOrderHeader(inpatient, currentUser, appointmentDate);
                RadOrderHd savedHd = radOrderHdRepository.save(orderHd);

                for (LabRadioInvestigationRequest item : entry.getValue()) {
                    DgMasInvestigation master = masterMap.get(item.getId());
                    RadOrderDt orderDt = buildRadiologyOrderDetail(
                            savedHd,
                            master,
                            currentUser,
                            appointmentDate,
                            item.getRemarks()
                    );
                    radOrderDtRepository.save(orderDt);
                    saveInvestigationBillingDetails(inpatient, billingCategory, master);
                }

                createdRadOrderIds.add(savedHd.getId());
            }

            log.info("Saved inpatient investigations successfully. inpatientId: {}, labOrders: {}, radOrders: {}",
                    request.getInpatientId(), createdLabOrderIds.size(), createdRadOrderIds.size());

            return ResponseUtils.createSuccessResponse(
                    "Investigations saved successfully. Lab orders: " + createdLabOrderIds.size() + ", Radiology orders: " + createdRadOrderIds.size(),
                    new TypeReference<>() {
                    }
            );

        } catch (Exception e) {
            log.error("Error while saving inpatient booking investigation. inpatientId: {}, patientId: {}",
                    request != null ? request.getInpatientId() : null,
                    request != null ? request.getPatientId() : null,
                    e);
            //manully rollback the whole transaction
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {
                    },
                    "Failed to save investigations: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<PendingToTransferResponse>> wardTransferList(List<Long> wardIds) {

        log.info(
                "Fetching pending transfer requests for wardId: {}", wardIds);

        try {
            List<PendingToTransferProjectionResponse> projectionList = ipTransferRequestRepository
                    .findTransferCompleteByWardId(wardIds, AppConstants.IPD_BED_TRANSFER_STATUS_COMPLETE);

            List<PendingToTransferResponse> responseList = projectionList.stream()
                    .map(this::mapToPendingTransferResponse)
                    .toList();

            log.info("Found {} pending transfer requests for destination wardIds: {}", responseList.size(), wardIds);

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error while fetching pending transfer requests for wardId: {}", wardIds, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
        }
    }

    @Override
    public ApiResponse<String> saveIpDiagnosisEntry(IpDiagnosisEntryRequest request) {

        log.info("Saving IP diagnosis entry. inpatientId: {}, diagnosisType: {}",
                request.getInpatientId(),
                request.getDiagnosisType());

        try {
            User currentUser = authUtil.getCurrentUser();
            LocalDateTime currentDateTime = LocalDateTime.now();

            String diagnosisType = request.getDiagnosisType() == null ? null : request.getDiagnosisType().trim().toUpperCase();

            /*
             * Only W and I are allowed.
             *
             * W = Working diagnosis
             * I = ICD diagnosis
             */
            if (!AppConstants.WORKING_DIAGNOSIS_TYPE.equals(diagnosisType) && !AppConstants.ICD_DIAGNOSIS_TYPE.equals(diagnosisType)) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Invalid diagnosis type. Allowed values are W and I",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            Inpatient inpatient = inpatientRepository.findById(request.getInpatientId()).orElse(null);

            if (inpatient == null) {
                return ResponseUtils.createNotFoundResponse("Inpatient not found with ID: " + request.getInpatientId(), HttpStatus.NOT_FOUND.value());
            }

            Patient patient = patientRepository.findById(request.getPatientId()).orElse(null);

            if (patient == null) {
                return ResponseUtils.createNotFoundResponse("Patient not found with ID: " + request.getPatientId(), HttpStatus.NOT_FOUND.value()
                );
            }

            MasDepartment department = masDepartmentRepository.findById(request.getDepartmentId()).orElse(null);

            if (department == null) {
                return ResponseUtils.createNotFoundResponse("Department not found with ID: " + request.getDepartmentId(), HttpStatus.NOT_FOUND.value());
            }

            MasIcd icd = null;

            if (AppConstants.WORKING_DIAGNOSIS_TYPE.equals(diagnosisType)) {
                icd = null;

            } else {
                icd = masIcdRepository.findById(request.getIcdId()).orElse(null);

                if (icd == null) {
                    return ResponseUtils.createNotFoundResponse("ICD not found with ID: " + request.getIcdId(), HttpStatus.NOT_FOUND.value());
                }
            }

            IpDiagnosisEntry diagnosisEntry = new IpDiagnosisEntry();

            diagnosisEntry.setInpatient(inpatient);
            diagnosisEntry.setPatient(patient);
            diagnosisEntry.setDepartment(department);
            diagnosisEntry.setDiagnosisType(diagnosisType);

            if (AppConstants.WORKING_DIAGNOSIS_TYPE.equalsIgnoreCase(diagnosisType)) {
                // Working diagnosis
                diagnosisEntry.setDiagnosisText(request.getDiagnosisText());
                diagnosisEntry.setIcd(null);
            } else if (AppConstants.ICD_DIAGNOSIS_TYPE.equalsIgnoreCase(diagnosisType)) {
                // ICD diagnosis
                diagnosisEntry.setDiagnosisText(icd.getIcdName());
                diagnosisEntry.setIcd(icd);
                inpatient.setIcd(icd.getIcdName());
            }

            diagnosisEntry.setStatus(request.getStatus().toUpperCase());
            diagnosisEntry.setDiagnosisDatetime(request.getDateTime());
            diagnosisEntry.setRecordedBy(currentUser);
            diagnosisEntry.setCreatedBy(currentUser.getFullName());
            diagnosisEntry.setLastUpdatedBy(currentUser.getFullName());
            diagnosisEntry.setLastUpdateDate(currentDateTime);

            inpatientRepository.save(inpatient);
            IpDiagnosisEntry savedDiagnosis = ipDiagnosisEntryRepository.save(diagnosisEntry);

            log.info(
                    "IP diagnosis entry saved successfully. diagnosisId: {}, inpatientId: {}, diagnosisType: {}, icdId: {}",
                    savedDiagnosis.getDiagnosisId(),
                    inpatient.getInpatientId(),
                    diagnosisType,
                    icd != null ? icd.getIcdId() : null
            );

            return ResponseUtils.createSuccessResponse("IP diagnosis entry saved successfully", new TypeReference<>() {
                    }
            );

        } catch (Exception e) {

            log.error("Error while saving IP diagnosis entry. inpatientId: {}, diagnosisType: {}",
                    request.getInpatientId(),
                    request.getDiagnosisType(),
                    e
            );

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<IpDiagnosisEntryResponse>> getIpDiagnosisEntry(Long inpatientId) {

        log.info("Fetching diagnosis entries for inpatientId: {}", inpatientId);

        try {

            List<IpDiagnosisEntryProjection> projections = ipDiagnosisEntryRepository.getIpDiagnosisEntry(inpatientId);

            List<IpDiagnosisEntryResponse> response = projections.stream().map(p -> {
                IpDiagnosisEntryResponse dto = new IpDiagnosisEntryResponse();
                dto.setInpatientId(p.getInpatientId());
                dto.setIcdId(p.getIcdId());
                dto.setIcdCode(p.getIcdCode());
                dto.setIcdName(p.getIcdName());
                dto.setRemark(p.getRemark());
                dto.setDiagnosisType(p.getDiagnosisType());
                dto.setStatus(p.getStatus());
                dto.setDiagnosis(p.getDiagnosis());
                dto.setDateTime(p.getDateTime());
                return dto;
            }).toList();

            log.info("Successfully fetched {} diagnosis entries for inpatientId: {}",
                    response.size(), inpatientId);

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
            });

        } catch (Exception e) {

            log.error("Error while fetching diagnosis entries for inpatientId: {}",
                    inpatientId, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<IntakeOutputResponse>> getIntakeOutputDetails(Long inpatientId) {

        log.info("Fetching Intake/Output details for inpatientId: {}", inpatientId);

        List<IntakeOutputProjection> projections = ipIntakeOutputEntryRepository.getIntakeOutputDetails(inpatientId);

        log.info("Total records fetched from database: {}", projections.size());

        if (projections.isEmpty()) {
            log.warn("No Intake/Output records found for inpatientId: {}", inpatientId);

            return ResponseUtils.createNotFoundResponse("No intake/output records found.", HttpStatus.NOT_FOUND.value());
        }

        List<IntakeOutputResponse> responses = projections.stream().map(p -> {

            IntakeOutputResponse response = new IntakeOutputResponse();

            response.setInpatientId(p.getInpatientId());
            response.setIoEntryId(p.getIoEntryId());
            response.setDateTime(p.getDateTime());
            response.setIoType(p.getIoType());

            if (AppConstants.IO_TYPE_I.equalsIgnoreCase(p.getIoType())) {

                log.debug("Mapping Intake record. ioEntryId: {}", p.getIoEntryId());

                response.setIntakeTypeId(p.getIntakeTypeId());
                response.setIntakeTypeName(p.getIntakeTypeName());
                response.setIntakeItemId(p.getIntakeItemId());
                response.setIntakeItemName(p.getIntakeItemName());
                response.setQuantity(p.getIntakeQuantity());

            } else if (AppConstants.IO_TYPE_O.equalsIgnoreCase(p.getIoType())) {

                log.debug("Mapping Output record. ioEntryId: {}", p.getIoEntryId());

                response.setOutputTypeId(p.getOutputTypeId());
                response.setOutputName(p.getOutputName());
                response.setQuantity(p.getIntakeQuantity());
            }

            return response;

        }).toList();

        log.info("Successfully mapped {} Intake/Output records for inpatientId: {}",
                responses.size(), inpatientId);

        return ResponseUtils.createSuccessResponse(
                responses,
                new TypeReference<List<IntakeOutputResponse>>() {
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> saveDischargeSummary(IpDischargeSummarySaveRequest request) {

        log.info("Saving discharge summary for inpatientId : {}", request.getInpatientId());

        try {
            User user = authUtil.getCurrentUser();
            //=========================
            // FIND EXISTING SUMMARY
            //=========================
            Optional<IpDischargeSummary> existingSummary = ipDischargeSummaryRepository.findByInpatient_InpatientId(request.getInpatientId());

            if (existingSummary.map(summary -> AppConstants.IP_DISCHARGE_SUMMARY_STATUS_SUMMIT
                    .equalsIgnoreCase(summary.getStatus())).orElse(false)) {

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        }, "Discharge summary already submitted",
                        HttpStatus.BAD_REQUEST.value());
            }

            Optional<Inpatient> inpatientOptional = inpatientRepository.findById(request.getInpatientId());

            if (inpatientOptional.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Inpatient not found", HttpStatus.NOT_FOUND.value());
            }

            Inpatient inpatient = inpatientOptional.get();

            Optional<IpdBillingHeader> ipdBillingHeader = ipdBillingHeaderRepository.findByInpatientId_InpatientId(request.getInpatientId());
            IpdBillingHeader ipdBillingHeader1 = ipdBillingHeader.get();
            //=========================
            // SUBMIT VALIDATION
            //=========================
            if (AppConstants.IP_DISCHARGE_SUMMARY_STATUS_SUMMIT.equalsIgnoreCase(request.getStatus())) {

                PaymentStatusResponse response = fetchPaymentStatus(request.getInpatientId());

                if (response.getBillStatusId() == null
                        || response.getPaymentStatusId() == null
                        || response.getOutstandingAmount() == null
                        || !ipBillStatusFinal.equals(response.getBillStatusId())
                        || !ipPaymentStatusPaid.equals(response.getPaymentStatusId())
                        || response.getOutstandingAmount().compareTo(BigDecimal.ZERO) != 0) {

                    return ResponseUtils.createFailureResponse(
                            null,
                            new TypeReference<>() {
                            },
                            "Bill must be FINAL, Payment must be PAID and Outstanding Amount must be 0 before submitting discharge summary.",
                            HttpStatus.BAD_REQUEST.value());
                }
                //=========================
                // UPDATE IPD STATUS ONLY ON SUBMIT
                //=========================
                inpatient.setAdmissionStatus(masAdmissionStatusRepository.findById(ipdDischargeStatusDischarge).orElseThrow());
                inpatient.setDischargeDate(LocalDate.now());
                inpatient.setDischargeTime(LocalTime.now());
                Optional<IpBedAllocation> bedAllocation = ipBedAllocationRepository.findTopByInpatient_InpatientIdOrderByAllocationStartDateDesc(inpatient.getInpatientId());
                IpBedAllocation ipBedAllocation = bedAllocation.get();
                ipBedAllocation.setAllocationEndDate(LocalDateTime.now());
                Optional<MasBed> masBed = masBedRepository.findById(bedAllocation.get().getBed().getBedId());
                MasBed masBed1 = masBed.get();
                masBed1.setBedStatusId(masBedStatusRepo.findById(bedStatusId).orElseThrow());
                ipdBillingHeader1.setPaymentStatus(masIpdPaymentStatusRepository.findById(ipBillStatusClose).orElseThrow());
                ipdBillingHeaderRepository.save(ipdBillingHeader1);
                ipBedAllocationRepository.save(ipBedAllocation);
                masBedRepository.save(masBed1);
            }
            IpDischargeSummary summary;

            if (existingSummary.isPresent()) {

                summary = existingSummary.get();
                summary.setLastUpdatedBy(user.getFullName());
                summary.setLastUpdateDate(LocalDateTime.now());

                log.info("Updating discharge summary.");

            } else {

                summary = new IpDischargeSummary();
                summary.setInpatient(inpatient);
                summary.setCreatedBy(user.getFullName());
                summary.setLastUpdatedBy(user.getFullName());
                summary.setLastUpdateDate(LocalDateTime.now());

                log.info("Creating discharge summary.");
            }

            //=========================
            // UPDATE FIELDS
            //=========================
            summary.setDischargeDate(request.getDischargeDate());
            summary.setPrimaryDiagnosis(request.getPrimaryDiagnosis());
            summary.setSecondaryDiagnosis(request.getSecondaryDiagnosis());
            summary.setPresentingComplaints(request.getPresentingComplaints());
            summary.setHistoryOfIllness(request.getHistoryOfIllness());
            summary.setPastHistory(request.getPastHistory());
            summary.setExaminationFindings(request.getExaminationFindings());
            summary.setProcedureDetails(request.getProcedureDetails());
            summary.setHospitalCourse(request.getHospitalCourse());
            summary.setDischargedTo(request.getDischargedTo());
            summary.setReferredHospitalName(request.getReferredHospitalName());
            summary.setDischargeAdvice(request.getDischargeAdvice());
            summary.setFollowUpAdvice(request.getFollowUpAdvice());
            summary.setStatus(request.getStatus());
            summary.setConsultantName(inpatient.getConsentTakenBy());
            summary.setCondition(masPatientDischargeConditionRepository.findById(request.getConditionId()).orElseThrow());
            summary.setDischargeReason(masDischargeReasonRepository.findById(request.getDischargeReasonId()).orElseThrow());
            // SAVE SUMMARY
            summary = ipDischargeSummaryRepository.save(summary);
            // change ip internal status
            inpatient.setMasIpdInternalStatus(masIpdInternalStatusRepository.findById(readyForDischargeId).orElseThrow());
            inpatientRepository.save(inpatient);
            //change bill status final
            ipdBillingHeader1.setBillStatus(masIpdBillStatusRepository.findById(ipBillStatusFinal).orElseThrow());
            ipdBillingHeaderRepository.save(ipdBillingHeader1);
            // DELETE SELECTED MEDICATIONS

            if (request.getDeleteMedicationIds() != null && !request.getDeleteMedicationIds().isEmpty()) {

                ipDischargeMedicationRepository.deleteSelectedMedications(request.getDeleteMedicationIds(), request.getInpatientId());

                log.info("Deleted {} medication(s).", request.getDeleteMedicationIds().size());
            }
            // ADD NEW MEDICATIONS

            if (request.getMedications() != null && !request.getMedications().isEmpty()) {

                List<IpDischargeMedication> medicationList = new ArrayList<>();

                for (IpDischargeMedicationRequest medicationRequest : request.getMedications()) {

                    IpDischargeMedication medication = new IpDischargeMedication();

                    medication.setInpatient(inpatient);
                    medication.setDischargeSummary(summary);
                    medication.setMedicineName(medicationRequest.getMedicineName());
                    medication.setDosage(medicationRequest.getDosage());
                    medication.setFrequency(medicationRequest.getFrequency());
                    medication.setDurationDays(medicationRequest.getDurationDays());
                    medication.setTotalDoses(medicationRequest.getTotalDoses());
                    medication.setRoute(medicationRequest.getRoute());
                    medication.setInstruction(medicationRequest.getInstruction());
                    medication.setCreatedBy(user.getFullName());
                    medication.setCreatedDate(LocalDateTime.now());
                    medication.setUpdatedBy(user.getFullName());
                    medication.setUpdatedDate(LocalDateTime.now());
                    medicationList.add(medication);
                }
                ipDischargeMedicationRepository.saveAll(medicationList);

                log.info("Added {} medication(s).", medicationList.size());
            }
            String message;

            if (AppConstants.IP_DISCHARGE_SUMMARY_STATUS_SUMMIT.equalsIgnoreCase(request.getStatus())) {

                message = existingSummary.isPresent() ? "Discharge summary submitted successfully." : "Discharge summary created and submitted successfully.";

            } else {

                message = existingSummary.isPresent() ? "Discharge summary draft updated successfully." : "Discharge summary draft saved successfully.";
            }

            return ResponseUtils.createSuccessResponse(message, new TypeReference<>() {
            });

        } catch (Exception e) {

            log.error("Error while saving discharge summary.", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<PaymentStatusResponse> getPaymentStatus(Long inpatientId) {

        log.info("Fetching payment status for inpatientId: {}", inpatientId);

        PaymentStatusResponse response = fetchPaymentStatus(inpatientId);

        log.info("Payment status fetched successfully for inpatientId: {}", inpatientId);

        return ResponseUtils.createSuccessResponse(response, new TypeReference<PaymentStatusResponse>() {
        });
    }

    private PaymentStatusResponse fetchPaymentStatus(Long inpatientId) {

        return ipdBillingHeaderRepository.getPaymentStatus(inpatientId);
    }


    @Override
    public ApiResponse<DischargeSummaryResponse> getDischargeSummary(Long inpatientId) {

        Optional<DischargeSummaryProjection> optionalProjection =
                ipDischargeSummaryRepository.getDischargeSummary(inpatientId);

        if (optionalProjection.isEmpty()) {
            return ResponseUtils.createNotFoundResponse(
                    "Discharge summary not found",
                    HttpStatus.NOT_FOUND.value());
        }

        DischargeSummaryProjection projection = optionalProjection.get();

        DischargeSummaryResponse response = new DischargeSummaryResponse();

        response.setDischargeSummaryId(projection.getDischargeSummaryId());
        response.setInpatientId(projection.getInpatientId());
        response.setDischargeDate(projection.getDischargeDate());
        response.setPrimaryDiagnosis(projection.getPrimaryDiagnosis());
        response.setSecondaryDiagnosis(projection.getSecondaryDiagnosis());
        response.setPresentingComplaints(projection.getPresentingComplaints());
        response.setHistoryOfIllness(projection.getHistoryOfIllness());
        response.setPastHistory(projection.getPastHistory());
        response.setExaminationFindings(projection.getExaminationFindings());
        response.setProcedureDetails(projection.getProcedureDetails());
        response.setHospitalCourse(projection.getHospitalCourse());
        response.setConditionId(projection.getConditionId());
        response.setConditionName(projection.getConditionName());
        response.setDischargeReasonId(projection.getDischargeReasonId());
        response.setDischargeReasonName(projection.getDischargeReasonName());
        response.setDischargedTo(projection.getDischargedTo());
        response.setReferredHospitalName(projection.getReferredHospitalName());
        response.setDischargeAdvice(projection.getDischargeAdvice());
        response.setFollowUpAdvice(projection.getFollowUpAdvice());
        response.setStatus(projection.getStatus());
        response.setBillStatusId(projection.getBillStatusId());
        response.setBillStatus(projection.getBillStatus());
        response.setPaymentStatusId(projection.getPaymentStatusId());
        response.setPaymentStatus(projection.getPaymentStatus());

        List<IpDischargeMedicationResponse> medicationResponses =
                ipDischargeMedicationRepository.getMedicationList(projection.getDischargeSummaryId())
                        .stream()
                        .map(item -> {
                            IpDischargeMedicationResponse dto = new IpDischargeMedicationResponse();
                            dto.setMedicationId(item.getMedicationId());
                            dto.setMedicineName(item.getMedicineName());
                            dto.setDosage(item.getDosage());
                            dto.setFrequency(item.getFrequency());
                            dto.setDurationDays(item.getDurationDays());
                            dto.setTotalDoses(item.getTotalDoses());
                            dto.setRoute(item.getRoute());
                            dto.setInstruction(item.getInstruction());
                            return dto;
                        })
                        .toList();

        response.setMedications(medicationResponses);

        return ResponseUtils.createSuccessResponse(
                response,
                new TypeReference<>() {
                },
                "Discharge summary fetched successfully");
    }

    @Override
    public ApiResponse<Page<InpatientAdvanceCollectionResponse>> getIpdAdvanceCollection(
            int page,
            int size,
            String patientName,
            String mobileNo,
            String admissionNo) {

        log.info("Fetching IPD advance collection. page: {}, size: {}, patientName: {}, mobileNo: {}, admissionNo: {}",
                page, size, patientName, mobileNo, admissionNo);

        try {
            Pageable pageable = PageRequest.of(page, size);

            Page<InpatientAdvanceCollectionProjection> projectionPage = inpatientRepository.getIpdAdvanceCollection(admitAdmissionStatusId,
                    patientName,
                    mobileNo,
                    admissionNo,
                    pageable);

            Page<InpatientAdvanceCollectionResponse> responsePage = projectionPage.map(this::mapResponse);

            log.info("Successfully fetched {} admitted patient(s).", responsePage.getTotalElements());

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {
            });

        } catch (Exception ex) {

            log.error("Error while fetching IPD advance collection.", ex);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        }
    }

    @Override
    public ApiResponse<Page<PendingTrackingIPDBillResponse>> getPendingTrackingIPDBillList(
            int page,
            int size,
            Long wardId,
            Long billType,
            BigDecimal outStandingAmount) {

        log.info("Fetching Pending Tracking IPD Bill List. page={}, size={}, wardId={}, billType={}, outStandingAmount={}",
                page, size, wardId, billType, outStandingAmount);

        try {

            Pageable pageable = PageRequest.of(page, size);

            Page<PendingTrackingIPDBillProjection> projectionPage =
                    ipdBillingHeaderRepository.getPendingTrackingIPDBillList(admitAdmissionStatusId,
                            wardId,
                            billType,
                            outStandingAmount,
                            pageable);

            Page<PendingTrackingIPDBillResponse> responsePage = projectionPage.map(this::convertToResponse);

            log.info("Successfully fetched {} Pending Tracking IPD Bill records.", responsePage.getTotalElements());
            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {
            });

        } catch (Exception e) {

            log.error("Error while fetching Pending Tracking IPD Bill List.", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, e.getMessage(), HttpStatus.BAD_REQUEST.value());

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> saveAdvanceCollection(AdvanceCollectionRequest request) {
        log.info("Saving advance collection for inpatientId : {}", request.getInpatientId());
        try {

            User user = authUtil.getCurrentUser();

            Optional<Inpatient> inpatient = inpatientRepository.findById(request.getInpatientId());
            if (inpatient.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Inpatient  not found", HttpStatus.NOT_FOUND.value());
            }
            Inpatient inpatient1 = inpatient.get();

            Optional<IpdBillingHeader> billingHeader = ipdBillingHeaderRepository.findByInpatientId_InpatientId(request.getInpatientId());
            if (billingHeader.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("IpdBillingHeader not found", HttpStatus.NOT_FOUND.value());
            }
            IpdBillingHeader ipdBillingHeader = billingHeader.get();

            BigDecimal totalAmount = request.getRequests().stream()
                    .map(AdvanceCollectionRequest.AdvanceCollectionDetailsRequest::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Receipt Header
            IpdBlReceiptHd receiptHd = new IpdBlReceiptHd();
            receiptHd.setReceiptNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.RECEIPT_NO, inpatient.get().getPatient().getPatientHospital().getId()));
            receiptHd.setReceiptDate(request.getCollectionDateTime());
            receiptHd.setInpatient(inpatient.get());
            receiptHd.setBill(billingHeader.get());
            receiptHd.setTotalAmount(totalAmount);
            receiptHd.setCreatedBy(user.getUsername());
            receiptHd.setCreatedDate(LocalDateTime.now());
            receiptHd.setReceiptStatus(AppConstants.IP_RECEIPT_STATUS.toLowerCase());
            receiptHd.setCreatedBy(user.getFullName());
            receiptHd.setLastChgBy(user.getFullName());
            receiptHd.setLastChgDate(LocalDateTime.now());
            receiptHd.setReceiptType(masReceiptTypeRepository.findById(request.getCollectionTypeId()).orElseThrow());
            receiptHd = ipdBlReceiptHdRepository.save(receiptHd);

            // Receipt Details + Payment Details
            for (AdvanceCollectionRequest.AdvanceCollectionDetailsRequest dto : request.getRequests()) {

                MasPaymentMode paymentMode = masPaymentModeRepository.findById(dto.getModeType())
                        .orElseThrow(() -> new RuntimeException("Payment mode not found"));

                // Receipt Detail
                IpdBlReceiptDt receiptDt = new IpdBlReceiptDt();
                receiptDt.setReceipt(receiptHd);
                receiptDt.setPaymentMode(paymentMode);
                receiptDt.setAmount(dto.getAmount());
                receiptDt.setCreatedBy(user.getUsername());
                receiptDt.setCreatedDate(LocalDateTime.now());
                receiptDt.setCreatedBy(user.getFullName());
                receiptDt.setLastChgBy(user.getFullName());
                receiptDt.setLastChgDate(LocalDateTime.now());
                ipdBlReceiptDtRepository.save(receiptDt);

                // Payment Detail
                IpPaymentDetail payment = new IpPaymentDetail();
                payment.setInpatient(inpatient.get());
                payment.setBill(billingHeader.get());
                payment.setReceipt(receiptHd);
                payment.setAmount(dto.getAmount());
                payment.setReceiptAmount(dto.getAmount());
                payment.setPaymentDate(request.getCollectionDateTime());
                payment.setPaymentStatus(masIpdPaymentStatusRepository.findById(ipPaymentStatusPaid).orElseThrow());
                payment.setLastChgBy(user.getUsername());
                payment.setLastChgDate(LocalDateTime.now());
                ipPaymentDetailRepository.save(payment);
            }
            BigDecimal netAmount = billingHeader.get().getNetAmount() != null
                    ? billingHeader.get().getNetAmount()
                    : BigDecimal.ZERO;

            BigDecimal existingPaidAmount = billingHeader.get().getPatientPaidAmount() != null
                    ? billingHeader.get().getPatientPaidAmount()
                    : BigDecimal.ZERO;

            // Add current advance to already paid amount
            BigDecimal patientPaidAmount = existingPaidAmount.add(totalAmount);

            // Calculate Outstanding Amount
            BigDecimal outstandingAmount = netAmount.subtract(patientPaidAmount);
            // Update Billing Header
            ipdBillingHeader.setPatientPaidAmount(patientPaidAmount);
            ipdBillingHeader.setOutstandingAmount(outstandingAmount);
            ipdBillingHeader.setUpdatedBy(user.getUsername());
            ipdBillingHeader.setUpdatedAt(LocalDateTime.now());

            // If Bill Final and Outstanding = 0 then Payment Status = Paid

            if (outstandingAmount.compareTo(BigDecimal.ZERO) == 0
                    && ipdBillingHeader.getBillStatus() != null
                    && ipdBillingHeader.getBillStatus().getBillStatusId().equals(ipBillStatusFinal)) {

                ipdBillingHeader.setPaymentStatus(masIpdPaymentStatusRepository.findById(ipPaymentStatusPaid)
                        .orElseThrow(() -> new RuntimeException("Paid status not found")));
            }
            ipdBillingHeaderRepository.save(ipdBillingHeader);

            return ResponseUtils.createSuccessResponse("Saving advance collection", new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error while fetching Pending Tracking IPD Bill List.", e);

            throw new RuntimeException("Failed to save advance collection.", e);
        }

    }

    @Override
    public ApiResponse<List<PreviousPaymentHistoryResponse>> previousPaymentHistory(Long billingHeaderId) {

        List<PreviousPaymentHistoryResponse> response = ipdBlReceiptDtRepository.previousPaymentHistory(billingHeaderId);

        return ResponseUtils.createSuccessResponse(
                response,
                new TypeReference<>() {
                },
                "Previous payment history fetched successfully");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> saveMedicationTreatment(IpMedicinePrescriptionRequest request) {
        log.info("Saving medication treatment. inpatientId: {}, itemId: {}",
                request.getInpatientId(),
                request.getItemId());

        try {
            if (request.getInpatientId() == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Inpatient ID is required", HttpStatus.BAD_REQUEST.value());
            }

            if (request.getItemId() == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Item ID is required", HttpStatus.BAD_REQUEST.value());
            }

            if (request.getRouteId() == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Route ID is required", HttpStatus.BAD_REQUEST.value());
            }

            if (request.getFrequencyId() == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Frequency ID is required", HttpStatus.BAD_REQUEST.value());
            }

            LocalDateTime currentDateTime = LocalDateTime.now();
            User currentUser = authUtil.getCurrentUser();

            Inpatient inpatient = inpatientRepository.findById(request.getInpatientId()).orElse(null);
            if (inpatient == null) {
                return ResponseUtils.createNotFoundResponse("Inpatient not found with ID: " + request.getInpatientId(), HttpStatus.NOT_FOUND.value());
            }

            if (inpatient.getAdmittingWardId() == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Ward is not associated with this inpatient", HttpStatus.BAD_REQUEST.value());
            }

            MasStoreItem item = masStoreItemRepository.findById(request.getItemId()).orElse(null);
            if (item == null) {
                return ResponseUtils.createNotFoundResponse("Item not found with ID: " + request.getItemId(), HttpStatus.NOT_FOUND.value());
            }

            // Check duplicate item for same inpatient ( active prescription — stop_date null — block)
            if (ipMedicinePrescriptionRepository.existsByInpatient_InpatientIdAndItem_ItemIdAndStopDateIsNull(
                    request.getInpatientId(), request.getItemId())) {

                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Item already added for this inpatient",
                        HttpStatus.BAD_REQUEST.value()
                );
            }


            MasRoute route = masRouteRepository.findById(request.getRouteId()).orElse(null);
            if (route == null) {
                return ResponseUtils.createNotFoundResponse("Route not found with ID: " + request.getRouteId(), HttpStatus.NOT_FOUND.value());
            }

            MasFrequency frequency = masFrequencyRepository.findById(request.getFrequencyId()).orElse(null);
            if (frequency == null) {
                return ResponseUtils.createNotFoundResponse("Frequency not found with ID: " + request.getFrequencyId(), HttpStatus.NOT_FOUND.value());
            }

            String userName = currentUser != null ? currentUser.getFullName() : null;

            IpMedicinePrescription prescription = new IpMedicinePrescription();
            prescription.setInpatient(inpatient);
            prescription.setWard(inpatient.getAdmittingWardId());
            prescription.setItem(item);
            prescription.setItemName(item.getNomenclature());
            prescription.setItemClass(item.getItemClassId());
            prescription.setRoute(route);
            prescription.setDose(request.getDose());
            prescription.setFrequency(frequency);
            prescription.setStartDate(request.getStartDate() != null ? request.getStartDate() : currentDateTime);
            prescription.setAdministratedBy(request.getAdministratedBy());
            prescription.setCreatedBy(userName);
            prescription.setLastUpdatedBy(userName);
            prescription.setTotalDays(request.getDay());
            prescription.setLastUpdateDate(currentDateTime);

            IpMedicinePrescription savedPrescription = ipMedicinePrescriptionRepository.save(prescription);

            log.info("Medication treatment saved successfully. prescriptionId: {}, inpatientId: {}",
                    savedPrescription.getPrescriptionId(),
                    request.getInpatientId());

            return ResponseUtils.createSuccessResponse("Medication treatment saved successfully", new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error while saving medication treatment. inpatientId: {}, itemId: {}", request.getInpatientId(), request.getItemId(), e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<IpMedicinePrescriptionResponse>> getMedicationTreatmentByInpatientId(Long inpatientId) {
        log.info("Fetching medication treatment for inpatientId: {}", inpatientId);

        try {
            if (inpatientId == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Inpatient ID is required", HttpStatus.BAD_REQUEST.value());
            }

            List<IpMedicinePrescriptionProjection> projections = ipMedicinePrescriptionRepository.getMedicationTreatmentByInpatientId(inpatientId);

            if (projections == null || projections.isEmpty()) {
                log.warn("No medication treatment found for inpatientId: {}", inpatientId);
                return ResponseUtils.createNotFoundResponse("No medication treatment found for inpatient ID: " + inpatientId, HttpStatus.NOT_FOUND.value());
            }

            List<IpMedicinePrescriptionResponse> responseList = projections.stream()
                    .map(this::mapToMedicinePrescriptionResponse)
                    .toList();

            log.info("Successfully fetched {} medication treatment records for inpatientId: {}", responseList.size(), inpatientId);

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error while fetching medication treatment for inpatientId: {}", inpatientId, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> stopMedicationTreatment(MedicinePrescriptionRequest request) {
        log.info("Stopping medication treatment. prescriptionId: {}", request.getPrescriptionId());

        try {
            if (request.getPrescriptionId() == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Prescription ID is required", HttpStatus.BAD_REQUEST.value());
            }

            IpMedicinePrescription prescription = ipMedicinePrescriptionRepository.findById(request.getPrescriptionId()).orElse(null);

            if (prescription == null) {
                return ResponseUtils.createNotFoundResponse("Medication treatment not found with prescription ID: " + request.getPrescriptionId(), HttpStatus.NOT_FOUND.value());
            }

            if (prescription.getStopDate() != null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Medication treatment is already stopped", HttpStatus.BAD_REQUEST.value());
            }

            LocalDateTime currentDateTime = LocalDateTime.now();
            User currentUser = authUtil.getCurrentUser();
            String userName = currentUser != null ? currentUser.getFullName() : null;

            prescription.setStopDate(currentDateTime);
            prescription.setStopReason(request.getStopReason());
            prescription.setLastUpdateDate(currentDateTime);
            prescription.setLastUpdatedBy(userName);

            ipMedicinePrescriptionRepository.save(prescription);

            log.info("Medication treatment stopped successfully. prescriptionId: {}", request.getPrescriptionId());

            return ResponseUtils.createSuccessResponse("Medication treatment stopped successfully", new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error while stopping medication treatment. prescriptionId: {}", request.getPrescriptionId(), e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> saveMarDetails(List<MarDetailsRequest> requests) {

        log.info("saveMarDetails started for {} record(s)", requests != null ? requests.size() : 0);

        User user = authUtil.getCurrentUser();

        try {
            for (MarDetailsRequest request : requests) {

                log.info("Processing MAR entry for prescriptionId={}, batchNo={}, qty={}", request.getPrescriptionId(), request.getBatchNo(), request.getRequestQty());

                // 1. Fetch Prescription
                IpMedicinePrescription prescription = ipMedicinePrescriptionRepository.findById(request.getPrescriptionId())
                        .orElseThrow(() -> {
                            return new RuntimeException("Prescription not found");
                        });

                // 2. Fetch Inpatient
                Inpatient inpatient = prescription.getInpatient();

                // MAR Entry

                IpMarDetails mar = new IpMarDetails();

                mar.setInpatient(inpatient);
                mar.setPrescription(prescription);
                mar.setAdministeredQty(request.getRequestQty());
                mar.setAdministrationTime(request.getDateTime());
                mar.setAdministeredBy(user.getUsername());
                mar.setBatchNo(request.getBatchNo());
                mar.setExpiryDate(request.getExpiryDate());
                mar.setRemarks(request.getRemark());
                mar.setCreatedBy(user.getFullName());
                mar.setLastUpdateDate(LocalDateTime.now());
                mar.setLastUpdatedBy(user.getFullName());
                ipMarDetailsRepository.save(mar);

                log.info("MAR entry saved with id={} for inpatientId={}", mar.getMarId(), inpatient.getInpatientId());

                // 3. Find Batch Stock (locked)
                StoreItemBatchStock stock = storeItemBatchStockRepository.findByItemIdAndBatchNoForUpdate(request.getItemId(), request.getBatchNo(), request.getDepartmentId())
                        .orElseThrow(() -> new RuntimeException("Batch not found"));

                BigDecimal currentQty = BigDecimal.valueOf(stock.getClosingStock());

                //---------------------------------------------------
                // Runtime stock check — available qty >= requested qty
                //---------------------------------------------------
                if (currentQty.compareTo(request.getRequestQty()) < 0) {
                    log.error("Insufficient stock for itemId={}, batchNo={}, available={}, requested={}",
                            request.getItemId(), request.getBatchNo(), currentQty, request.getRequestQty());
                    throw new RuntimeException("Insufficient Stock");
                }

                BigDecimal currentIpdIssueQty = stock.getIpdIssueQty() != null ? stock.getIpdIssueQty() : BigDecimal.ZERO;
                BigDecimal updatedIpdIssueQty = currentIpdIssueQty.add(request.getRequestQty());
                BigDecimal updatedQty = currentQty.subtract(request.getRequestQty());

                //---------------------------------------------------
                // Update Batch Stock
                //---------------------------------------------------

                stock.setClosingStock(updatedQty.longValue());
                stock.setIpdIssueQty(updatedIpdIssueQty);

                storeItemBatchStockRepository.save(stock);

                log.info("Stock updated for batchNo={}, before={}, after={}", request.getBatchNo(), currentQty, updatedQty);
                //---------------------------------------------------
                // Store IpMedicineIssue
                //---------------------------------------------------

                IpMedicineIssue ipMedicineIssue = new IpMedicineIssue();

                ipMedicineIssue.setInpatient(inpatient);
                ipMedicineIssue.setPrescription(prescription);
                ipMedicineIssue.setMarDetails(mar);
                ipMedicineIssue.setItem(stock.getItemId());
                ipMedicineIssue.setBatch(stock);
                ipMedicineIssue.setBatchNo(request.getBatchNo());
                ipMedicineIssue.setExpiryDate(request.getExpiryDate());
                ipMedicineIssue.setIssueQty(request.getRequestQty());
                ipMedicineIssue.setIssueDatetime(LocalDateTime.now());
                ipMedicineIssue.setIssuedBy(user.getUserId());
                ipMedicineIssue.setCreatedBy(user.getUserId());
                ipMedicineIssue.setCreatedOn(LocalDateTime.now());
                ipMedicineIssue.setLastChgBy(user.getUserId());
                ipMedicineIssue.setLastChgOn(LocalDateTime.now());
                ipMedicineIssue.setRemarks(request.getRemark());

                ipMedicineIssueRepository.save(ipMedicineIssue);

                log.info("IpMedicineIssue saved with id={} for inpatientId={}, prescriptionId={}, marId={}, batchNo={}, issueQty={}",
                        ipMedicineIssue.getIpMedicineIssueId(),
                        inpatient.getInpatientId(),
                        prescription.getPrescriptionId(),
                        mar.getMarId(),
                        request.getBatchNo(),
                        request.getRequestQty()
                );


                //---------------------------------------------------
                // Stock Ledger
                //---------------------------------------------------
                StoreStockLedgerRequest  storeStockLedgerRequest=new StoreStockLedgerRequest();
                storeStockLedgerRequest.setStockId(stock.getStockId());
                storeStockLedgerRequest.setTxnType(AppConstants.INPATIENT_ISSUE);
                storeStockLedgerRequest.setTxnReferenceId(ipMedicineIssue.getIpMedicineIssueId());
                storeStockLedgerRequest.setQtyBefore(currentQty);
                storeStockLedgerRequest.setQtyOut(request.getRequestQty());
                storeStockLedgerRequest.setQtyAfter(updatedQty);
                storeStockLedgerRequest.setTxnSource(AppConstants.INPATIENT_ISSUE);
                storeStockLedgerRequest.setCreatedBy(user.getUsername());
                storeStockLedgerRequest.setHospitalId(stock.getHospitalId().getId());
                storeStockLedgerRequest.setDepartmentId(stock.getDepartmentId().getId());
                inventoryUtils.updateStoreStockLedger(storeStockLedgerRequest);

                //---------------------------------------------------
                // Billing
                //---------------------------------------------------

                BigDecimal amount = calculateAmount(stock.getMrpPerUnit(), request.getRequestQty());
                BigDecimal gstAmount = calculateGST(stock.getMrpPerUnit(), request.getRequestQty(), stock.getGstPercent());
                BigDecimal netAmount = calculateNetAmount(stock.getMrpPerUnit(), request.getRequestQty(), stock.getGstPercent());
                Optional<MasIpdServiceCategory> masIpdServiceCategory = masIpdServiceCategoryRepository.findById(IPDServiceCategoryDrug);
                ItemClassBillSubcategoryMapping mapping = itemClassBillSubcategoryMappingRepository.findByItemClass_ItemClassId(stock.getItemId().getItemClassId().getItemClassId());
                MasIpdServiceSubcategory subcategory = null;
                if (mapping != null && mapping.getIpdBillSubcategoryId() != null) {
                    subcategory = masIpdServiceSubcategoryRepository.findById(mapping.getIpdBillSubcategoryId().getSubcategoryId()).orElse(null);
                }
                saveIpdBillingDetails.saveInpatientBillingDetails(inpatient,
                        stock.getMrpPerUnit(),
                        request.getRequestQty(),
                        stock.getGstPercent(),
                        BigDecimal.ZERO,
                        amount,
                        gstAmount,
                        netAmount,
                        masIpdServiceCategory.get(),
                        subcategory,
                        stock.getItemId().getNomenclature()
                );

                log.info("Billing entry saved for inpatientId={}, amount={}, netAmount={}", inpatient.getInpatientId(), amount, netAmount);
            }

            log.info("saveMarDetails completed successfully for {} record(s)", requests.size());
            return ResponseUtils.createSuccessResponse("mar details save successfully", new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("saveMarDetails failed, rolling back transaction. Reason: {}", e.getMessage(), e);
            throw e;
        }
    }

    private BigDecimal calculateGST(BigDecimal rate,
                                    BigDecimal qty,
                                    BigDecimal gstPercent) {

        if (rate == null || qty == null || gstPercent == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal amount = rate.multiply(qty);

        return amount.multiply(gstPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAmount(BigDecimal rate, BigDecimal qty) {

        if (rate == null || qty == null) {
            return BigDecimal.ZERO;
        }

        return rate.multiply(qty).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateNetAmount(BigDecimal rate,
                                          BigDecimal qty,
                                          BigDecimal gstPercent) {

        BigDecimal amount = calculateAmount(rate, qty);

        BigDecimal gst = calculateGST(rate, qty, gstPercent);

        return amount.add(gst).setScale(2, RoundingMode.HALF_UP);
    }

    private IpMedicinePrescriptionResponse mapToMedicinePrescriptionResponse(IpMedicinePrescriptionProjection projection) {
        IpMedicinePrescriptionResponse response = new IpMedicinePrescriptionResponse();

        response.setPrescriptionId(projection.getPrescriptionId());
        response.setInpatientId(projection.getInpatientId());
        response.setItemId(projection.getItemId());
        response.setItemName(projection.getItemName());
        response.setRouteId(projection.getRouteId());
        response.setRouteName(projection.getRouteName());
        response.setDose(projection.getDose());
        response.setFrequencyId(projection.getFrequencyId());
        response.setFrequencyName(projection.getFrequencyName());
        response.setStartDate(projection.getStartDate());
        response.setStopDate(projection.getStopDate());
        response.setAdministratedBy(projection.getAdministratedBy());

        return response;
    }

    private PendingTrackingIPDBillResponse convertToResponse(PendingTrackingIPDBillProjection p) {

        PendingTrackingIPDBillResponse response = new PendingTrackingIPDBillResponse();

        response.setInpatientId(p.getInpatientId());
        response.setBillingHeaderId(p.getBillingHeaderId());
        response.setUhid(p.getUhid());
        response.setPatientName(p.getPatientName());
        response.setAge(p.getAge());
        response.setGenderId(p.getGenderId());
        response.setGender(p.getGender());
        response.setMobileNo(p.getMobileNo());
        response.setAdmissionNo(p.getAdmissionNo());
        response.setWardId(p.getWardId());
        response.setWard(p.getWard());
        response.setRoomId(p.getRoomId());
        response.setRoom(p.getRoom());
        response.setBedId(p.getBedId());
        response.setBed(p.getBed());
        response.setAdmissionDateTime(p.getAdmissionDateTime());
        response.setBillingTypeId(p.getBillingTypeId());
        response.setBillingType(p.getBillingType());
        response.setTotalAmount(p.getTotalAmount());
        response.setEstimationCost(p.getEstimationCost());
        response.setPatientPaid(p.getPatientPaid());
        response.setOutStandingAmount(p.getOutStandingAmount());
        response.setBillStatusId(p.getBillStatusId());
        response.setBillStatus(p.getBillStatus());
        response.setPaymentStatusId(p.getPaymentStatusId());
        response.setPaymentStatus(p.getPaymentStatus());

        return response;
    }

    private InpatientAdvanceCollectionResponse mapResponse(
            InpatientAdvanceCollectionProjection p) {

        InpatientAdvanceCollectionResponse response =
                new InpatientAdvanceCollectionResponse();

        response.setInpatientId(p.getInpatientId());
        response.setBillingHeaderId(p.getBillingHeaderId());
        response.setUhid(p.getUhid());
        response.setPatientName(p.getPatientName());
        response.setAge(p.getAge());
        response.setGenderId(p.getGenderId());
        response.setGender(p.getGender());
        response.setMobileNo(p.getMobileNo());
        response.setAdmissionNo(p.getAdmissionNo());
        response.setWardId(p.getWardId());
        response.setWard(p.getWard());
        response.setRooId(p.getRoomId());   // rename to roomId in DTO if possible
        response.setRoom(p.getRoom());
        response.setBedId(p.getBedId());
        response.setBed(p.getBed());
        response.setAdmissionDateTime(p.getAdmissionDateTime());
        response.setBillingTypeId(p.getBillingTypeId());
        response.setBillingType(p.getBillingType());

        return response;
    }

    private List<LabRadioInvestigationRequest> resolveInvestigations(InpatientBookingInvestigationRequest request) {
        if (request.getInvestigationReq() != null && !request.getInvestigationReq().isEmpty()) {
            return request.getInvestigationReq();
        }

        if (request.getInvestigationId() != null) {
            LabRadioInvestigationRequest single = new LabRadioInvestigationRequest();
            single.setId(request.getInvestigationId());
            single.setAppointmentDate(LocalDate.now());
            return List.of(single);
        }

        return Collections.emptyList();
    }


    private DgOrderHd buildLabOrderHeader(Inpatient inpatient, User currentUser, LocalDate appointmentDate, LocalTime now) {
        DgOrderHd hd = new DgOrderHd();
        hd.setOrderDate(LocalDate.now());
        hd.setOrderTime(HMISUtil.getCurrentLocalDateTime());
        hd.setOrderNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.LAB_NO, currentUser.getHospital().getId()));
        hd.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
        hd.setCollectionStatus(AppConstants.STATUS_N.toLowerCase());
        hd.setPaymentStatus(AppConstants.STATUS_Y.toLowerCase());
        hd.setSource(AppConstants.SOURCE_TYPE_IPD);
        hd.setHospitalId(currentUser.getHospital().getId());
        hd.setPrescribedBy(currentUser.getUserId() != null ? currentUser.getUserId().intValue() : 0);
        hd.setDepartmentId(authUtil.getCurrentDepartmentId());
        hd.setInvestigationRequestNo(0);
        hd.setPatientId(inpatient.getPatient());
        hd.setDiscountId(null);
        hd.setAppointmentDate(appointmentDate);
        hd.setCreatedBy(currentUser.getFullName());
        hd.setLastChgBy(currentUser.getFullName());
        hd.setCreatedOn(LocalDate.now());
        hd.setLastChgDate(LocalDate.now());
        hd.setLastChgTime(now.toString());
        hd.setLabOrderStatus(AppConstants.STATUS_N.toLowerCase());
        hd.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
        hd.setInpatientId(inpatient);
        return hd;
    }

    private DgOrderDt buildLabOrderDetail(DgOrderHd orderHd, DgMasInvestigation investigation, User currentUser, LocalDate appointmentDate, LocalTime now, LabOrderTrackingStatus orderedStatus, String remarks) {
        DgOrderDt dt = new DgOrderDt();
        dt.setOrderHd(orderHd);
        dt.setInvestigation(investigation);
        dt.setAppointmentDate(appointmentDate);
        dt.setOrderQty(1);
        dt.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
        dt.setBillingStatus(AppConstants.STATUS_Y.toLowerCase());
        dt.setCreatedBy(currentUser.getFullName());
        dt.setLastChgBy(currentUser.getFullName());
        dt.setLastChgDate(LocalDate.now());
        dt.setLastChgTime(now.toString());
        dt.setMainChargeCodeId(investigation.getMainChargeCodeId() != null ? investigation.getMainChargeCodeId().getChargecodeId() : 0L);
        dt.setSubChargeCodeId(investigation.getSubChargeCodeId() != null ? investigation.getSubChargeCodeId().getSubId() : 0L);
        dt.setCreatedOn(HMISUtil.getCurrentLocalDateTime());
        dt.setMsgSent(AppConstants.STATUS_N.toLowerCase());
        dt.setOrderTrackingStatus(orderedStatus);
        dt.setRemarks(remarks);
        return dt;
    }

    private RadOrderHd buildRadiologyOrderHeader(Inpatient inpatient, User currentUser, LocalDate appointmentDate) {
        RadOrderHd hd = new RadOrderHd();
        hd.setOrderDate(LocalDate.now());
        hd.setOrderTime(HMISUtil.getCurrentLocalDateTime());
        hd.setAppointmentDate(appointmentDate);
        hd.setPatient(inpatient.getPatient());
        hd.setHospital(currentUser.getHospital());
        hd.setDepartment(masDepartmentRepository.findById(authUtil.getCurrentDepartmentId())
                .orElseThrow(() -> new SDDException(404, "Department not found with id: " + authUtil.getCurrentDepartmentId())));
        hd.setPrescribedBy(currentUser.getFullName());
        hd.setCreatedBy(currentUser.getFullName());
        hd.setLastChgBy(currentUser.getFullName());
        hd.setPaymentStatus(AppConstants.STATUS_Y.toLowerCase());
        hd.setInpatient(inpatient);
        return hd;
    }

    private RadOrderDt buildRadiologyOrderDetail(RadOrderHd orderHd, DgMasInvestigation investigation, User currentUser, LocalDate appointmentDate, String remarks) {
        RadOrderDt dt = new RadOrderDt();
        dt.setRadOrderhd(orderHd);
        dt.setInvestigation(investigation);
        dt.setSubChargecode(investigation.getSubChargeCodeId());
        dt.setOrderAccessionNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.RADIOLOGY_NO, currentUser.getHospital().getId()));
        dt.setAppointmentDate(appointmentDate);
        dt.setStudyStatus(AppConstants.STATUS_N.toLowerCase());
        dt.setReportStatus(AppConstants.STATUS_N.toLowerCase());
        dt.setHl7MwlStatus(AppConstants.STATUS_N.toLowerCase());
        dt.setPacsCompletionStatus(AppConstants.STATUS_N.toLowerCase());
        dt.setBillingStatus(AppConstants.STATUS_Y.toLowerCase());
        dt.setOrderStatus(AppConstants.STATUS_Y.toLowerCase());
        dt.setCreatedby(currentUser.getFullName());
        dt.setLastChgBy(currentUser.getFullName());
        dt.setRemarks(remarks);
        return dt;
    }

    private void saveInvestigationBillingDetails(Inpatient inpatient,
                                                 MasIpdServiceCategory billingCategory,
                                                 DgMasInvestigation investigation) {
        if (inpatient == null || investigation == null) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(),
                    "Invalid inpatient or investigation data for billing");
        }

        BigDecimal rate = resolveInvestigationRate(investigation);
        BigDecimal quantity = BigDecimal.ONE;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal amount = rate.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gstPercent = resolveIpdGstPercent(billingCategory);
        BigDecimal gstAmount = amount.multiply(gstPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netAmount = amount.add(gstAmount).subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
        MasMainChargeCode chargeCode = masMainChargeCodeRepository
                .findById(investigation.getMainChargeCodeId().getChargecodeId())
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(), "Charge Code not found"));

        MasIpdServiceSubcategory subcategory = masIpdServiceSubcategoryRepository
                .findBySubcategoryCode(chargeCode.getChargecodeCode())
                .orElseThrow(() -> new RuntimeException("Subcategory not found"));
        saveIpdBillingDetails.saveInpatientBillingDetails(
                inpatient,
                rate,
                quantity,
                gstPercent,
                discountAmount,
                amount,
                gstAmount,
                netAmount,
                billingCategory,
                subcategory,
                investigation.getInvestigationName()
        );
    }

    private BigDecimal resolveInvestigationRate(DgMasInvestigation investigation) {
        LocalDate today = LocalDate.now();

        Optional<MasInvestigationPriceDetails> activePrice = masInvestigationPriceDetailsRepository
                .findActivePriceByInvestigationAndDate(investigation, today);
        if (activePrice.isPresent() && activePrice.get().getPrice() != null) {
            return activePrice.get().getPrice().setScale(2, RoundingMode.HALF_UP);
        }

        Optional<MasInvestigationPriceDetails> latestPrice = masInvestigationPriceDetailsRepository
                .findTopByInvestigationOrderByFromDateDesc(investigation);
        if (latestPrice.isPresent() && latestPrice.get().getPrice() != null) {
            return latestPrice.get().getPrice().setScale(2, RoundingMode.HALF_UP);
        }

        if (investigation.getPrice() != null) {
            return BigDecimal.valueOf(investigation.getPrice()).setScale(2, RoundingMode.HALF_UP);
        }

        throw new SDDException(HttpStatus.BAD_REQUEST.value(),
                "Investigation price not configured for investigationId: " + investigation.getInvestigationId());
    }

//    private BigDecimal resolveInvestigationDiscount(DgMasInvestigation investigation) {
//        if (investigation.getDiscountApplicable() == null
//                || !("y".equalsIgnoreCase(investigation.getDiscountApplicable())
//                || "yes".equalsIgnoreCase(investigation.getDiscountApplicable())
//                || "1".equalsIgnoreCase(investigation.getDiscountApplicable()))) {
//            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
//        }
//
//        if (investigation.getDiscount() == null || investigation.getDiscount().trim().isEmpty()) {
//            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
//        }
//
//        try {
//            return new BigDecimal(investigation.getDiscount().trim()).setScale(2, RoundingMode.HALF_UP);
//        } catch (NumberFormatException ex) {
//            throw new SDDException(HttpStatus.BAD_REQUEST.value(),
//                    "Invalid discount configured for investigationId: " + investigation.getInvestigationId());
//        }
//    }

    private BigDecimal resolveIpdGstPercent(MasIpdServiceCategory billingCategory) {
        if (billingCategory == null || billingCategory.getGstPercentage() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return billingCategory.getGstPercentage().setScale(2, RoundingMode.HALF_UP);
    }

    private synchronized String generateTransferNumber() {

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

        String prefix = "TRF/" + financialYear;

        String lastAdmissionNo = ipTransferRequestRepository.findLastTransferNoByFinancialYear(prefix + "/%");

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

    // === Helper method to generate issue number ===
    private String generateIssueNumber() {
        // Option 1: Simple timestamp-based
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ISS-" + timestamp;

        // Option 2: Sequential number (you'd need to query the last issue number)
        // Long lastIssueNumber = issueRepository.findMaxIssueNumber();
        // return "ISS-" + String.format("%06d", (lastIssueNumber == null ? 1 : lastIssueNumber + 1));

        // Option 3: UUID
        // return "ISS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public ApiResponse<Page<IpMarDetailsResponse>> getMarAdministrationLog(Long inpatientId, Long itemId, Integer page, Integer size) {
        log.info("Request to fetch MAR Administration Log for inpatientId: {}, itemId: {}, page: {}, size: {}", inpatientId, itemId, page, size);
        try {
            if (inpatientId == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Inpatient ID is required", HttpStatus.BAD_REQUEST.value());
            }

            Pageable pageable;
            if (page != null && size != null) {
                pageable = PageRequest.of(page, size);
            } else {
                pageable = PageRequest.of(0, Integer.MAX_VALUE);
            }

            Page<IpMarDetailsProjection> projections = ipMarDetailsRepository.getMarAdministrationLog(inpatientId, itemId, pageable);
            Page<IpMarDetailsResponse> responsePage = projections.map(ipMarDetailsMapper::mapToMarDetailsResponse);

            log.info("Successfully fetched {} MAR Administration Log records for inpatientId: {}", responsePage.getTotalElements(), inpatientId);
            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error while fetching MAR Administration Log for inpatientId: {}", inpatientId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<MarMedicineResponse>> getMarMedicineList(Long inpatientId) {
        log.info("Request to fetch unique medicines in MAR log for inpatientId: {}", inpatientId);
        try {
            if (inpatientId == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Inpatient ID is required", HttpStatus.BAD_REQUEST.value());
            }

            List<MarMedicineProjection> projections = ipMarDetailsRepository.getUniqueMedicinesInMar(inpatientId);
            List<MarMedicineResponse> responseList = projections.stream()
                    .map(ipMarDetailsMapper::mapToMarMedicineResponse)
                    .toList();

            log.info("Successfully fetched {} unique medicines for inpatientId: {}", responseList.size(), inpatientId);
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error while fetching unique medicines in MAR log for inpatientId: {}", inpatientId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<String> saveInpatientProcedure(InpatientProcedureRequest request) {

        log.info("Saving inpatient procedure. inpatientId={}, procedureId={}", request.getInpatientId(), request.getProcedureId());

        try {
            // 2. Fetch inpatient
            Inpatient inpatient = inpatientRepository.findById(request.getInpatientId()).orElseThrow(() -> new RuntimeException(
                    "Inpatient not found with id: " + request.getInpatientId()));

            MasProcedure masProcedure = masProcedureRepository.findById(request.getProcedureId()).orElseThrow(() -> new RuntimeException(
                    "Procedure not found with id: " + request.getInpatientId()));

            // 3. Create procedure transaction
            IpProcedureTxn procedureTxn = new IpProcedureTxn();

            procedureTxn.setInpatient(inpatient);
            procedureTxn.setProcedureId(masProcedure);
            procedureTxn.setProcedureName(masProcedure.getProcedureName());
            procedureTxn.setProcedureDatetime(request.getProcedureDatetime());
            procedureTxn.setPerformedBy(request.getPerformedBy());
            procedureTxn.setRemarks(request.getRemarks());
            User user = authUtil.getCurrentUser();
            procedureTxn.setCreatedAt(LocalDateTime.now());
            procedureTxn.setCreatedBy(user.getFullName());
            procedureTxn.setUpdatedAt(LocalDateTime.now());
            procedureTxn.setUpdatedBy(user.getFullName());

            ipProcedureTxnRepository.save(procedureTxn);

            log.info("Inpatient procedure saved successfully. procedureTxnId={}", procedureTxn.getProcedureTxnId());

            return ResponseUtils.createSuccessResponse("Inpatient procedure saved successfully", new TypeReference<>() {
            });

        } catch (Exception e) {

            log.error("Error while saving inpatient procedure. inpatientId={}, procedureId={}", request.getInpatientId(), request.getProcedureId(), e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, e.getMessage(), HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    @Override
    public ApiResponse<List<IpProcedureTxnResponse>> getIpProcedureTxnByInpatientId(Long inpatientId) {
        log.info("Request to fetch IpProcedureTxn for inpatientId: {}", inpatientId);
        try {
            if (inpatientId == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Inpatient ID is required", HttpStatus.BAD_REQUEST.value());
            }

            List<IpProcedureTxn> entities = ipProcedureTxnRepository.findByInpatientInpatientId(inpatientId);
            List<IpProcedureTxnResponse> dtoList = entities.stream()
                    .map(ipProcedureTxnMapper::mapToDTO)
                    .toList();

            log.info("Successfully fetched {} IpProcedureTxn records for inpatientId: {}", dtoList.size(), inpatientId);
            return ResponseUtils.createSuccessResponse(dtoList, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error while fetching IpProcedureTxn for inpatientId: {}", inpatientId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @Transactional
    public ApiResponse<String> saveProcedureConsumableTemplate(ProcedureConsumableTemplateSaveRequest request) {
        try {
            // 1. Validate procedure
            MasProcedure procedure = masProcedureRepository.findById(request.getProcedureId())
                    .orElseThrow(() -> new RuntimeException("Procedure not found with id: " + request.getProcedureId()));

            // 2. Check duplicate template code
            if (masProcedureConsumableTemplateRepository.existsByTemplateCode(request.getTemplateCode())) {
                throw new RuntimeException("Template code already exists: " + request.getTemplateCode());
            }

            // 3. Check duplicate template name for same procedure
            if (masProcedureConsumableTemplateRepository.existsByProcedure_ProcedureIdAndTemplateName(
                    request.getProcedureId(),
                    request.getTemplateName())) {

                throw new RuntimeException("Template name already exists for this procedure: " + request.getTemplateName());
            }

            // 4. Create parent template
            MasProcedureConsumableTemplate template = new MasProcedureConsumableTemplate();

            template.setProcedure(procedure);
            template.setTemplateCode(request.getTemplateCode());
            template.setTemplateName(request.getTemplateName());
            template.setLastChgDate(LocalDateTime.now());
            User user = authUtil.getCurrentUser();
            template.setLastChgBy(user.getFullName());

            // 5. Save parent
            MasProcedureConsumableTemplate savedTemplate = masProcedureConsumableTemplateRepository.save(template);

            for (TemplateDetailRequest detailRequest : request.getDetails()) {

                MasStoreItem item = masStoreItemRepository.findById(detailRequest.getItemId()).orElseThrow(() ->
                        new RuntimeException("Item not found with id: " + detailRequest.getItemId()));

                MasProcedureConsumableTemplateDetail detail = new MasProcedureConsumableTemplateDetail();

                detail.setTemplate(savedTemplate);
                detail.setItem(item);
                detail.setDefaultQty(detailRequest.getDefaultQty());

                masProcedureConsumableTemplateDetailRepository.save(detail);
            }

            return ResponseUtils.createSuccessResponse("Template saved successfully", new TypeReference<>() {
            });

        } catch (Exception e) {

            log.error("Error while saving procedure consumable template. procedureId={}, templateCode={}",
                    request != null ? request.getProcedureId() : null, request != null ? request.getTemplateCode() : null, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<Page<ProcedureConsumableTemplateHeaderResponse>> getTemplates(
            String search,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "templateName"));

        String searchParam = (search == null || search.isBlank()) ? "" : search;

        Page<ProcedureConsumableTemplateHeaderResponse> result = masProcedureConsumableTemplateRepository.getTemplates(searchParam, pageable);

        return ResponseUtils.createSuccessResponse(result, new TypeReference<Page<ProcedureConsumableTemplateHeaderResponse>>() {
        }
        );
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ProcedureConsumableTemplateDetailsResponse>>
    getProcedureConsumableTemplateDetails(Long templateId) {

            List<ProcedureConsumableTemplateDetailsResponse> result = masProcedureConsumableTemplateDetailRepository.getTemplateDetails(templateId);

            return ResponseUtils.createSuccessResponse(
                    result,
                    new TypeReference<List<ProcedureConsumableTemplateDetailsResponse>>() {
                    }
            );

    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<NursingCareProcedure>> getNursingCareProcedure(Long inpatientId) {

        List<NursingCareProcedureProjection> projections =
                ipConsumableTxnRepository.getNursingCareProcedureByInpatientId(inpatientId);

        List<NursingCareProcedure> result = projections.stream().map(projection -> {
            NursingCareProcedure response = new NursingCareProcedure();
            response.setItemId(projection.getItemId());
            response.setItemName(projection.getItemName());
            response.setQty(projection.getQty());
            response.setProcedureTxnId(projection.getProcedureTxnId());
            response.setProcedureName(projection.getProcedureName());
            response.setDateTime(projection.getDateTime());
            response.setUsedBy(projection.getUsedBy());
            response.setBatchNo(projection.getBatchNo());
            response.setExpiryDate(projection.getExpiryDate());
            response.setRemark(projection.getRemark());
            return response;
        }).collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(result, new TypeReference<List<NursingCareProcedure>>() {});
    }


    @Override
    public ApiResponse<InpatientAdmissionDetailsResponse> getAdmissionDetailsByInpatient(Long inpatientId) {

        log.info("Fetching admission details for inpatientId={}", inpatientId);

        InpatientAdmissionProjection projection = inpatientRepository.findAdmissionDetailsByInpatientId(inpatientId)
                .orElseThrow(() -> new RuntimeException("Inpatient not found with ID: " + inpatientId));

        InpatientAdmissionDetailsResponse response = new InpatientAdmissionDetailsResponse();

        // Patient Information
        response.setPatientId(projection.getPatientId());
        response.setPatientName(String.join(" ",
                nullToEmpty(projection.getPatientFn()),
                nullToEmpty(projection.getPatientMn()),
                nullToEmpty(projection.getPatientLn())).trim());
        response.setUhid(projection.getUhidNo());
        response.setAge(projection.getPatientAge());
        response.setGenderId(projection.getGenderId());
        response.setGender(projection.getGenderName());
        response.setContactNo(projection.getPatientMobileNumber());
        response.setEmergencyContactNo(projection.getEmergencyContactNo());

        // Admission Information
        response.setAdmissionNo(projection.getAdmissionNo());
       response.setAdmissionTime(projection.getAdmissionTime());
       response.setAdmissionDate(projection.getAdmissionDate());
        response.setAdmissionCategory(projection.getAdmissionCategoryName());
        response.setAdmissionType(projection.getAdmissionTypeName());
        response.setAdmissionSource(projection.getAdmissionSourceName());
        response.setCurrentStatus(projection.getAdmissionStatusName());
        if (projection.getAdmissionDate() != null) {
            LocalDate endDate = projection.getDischargeDate() != null
                    ? projection.getDischargeDate()
                    : LocalDate.now();
            long days = ChronoUnit.DAYS.between(projection.getAdmissionDate(), endDate);
            response.setLos(days + " day(s)");
        }

        // Doctor & Location
        response.setAdmittingDoctor(projection.getDoctorName());
        response.setAdmittingWard(projection.getWardName());
        response.setCurrentWard(projection.getWardName());
        response.setRoom(projection.getRoomName());
        response.setBed(projection.getBedName());
        response.setCareLevel(projection.getCareLevelName());

        // Clinical Info
        response.setReasonForAdmission(projection.getConditionNotes());
        response.setInitialDiagnosis(projection.getInitialDiagnosis());
        response.setIcdDiagnosis(projection.getIcdName());
        response.setPatientCondition(projection.getPatientConditionName());
        response.setAdmissionPriority(projection.getAdmissionPriority());
        response.setRemark(projection.getConditionNotes());


        // NOK Details
        ipNokDetailsRepository.findNokDetailsByInpatientId(inpatientId)
                .ifPresent(nok -> {
                    response.setNokName(nok.getNokName());
                    response.setRelationship(nok.getRelationName());
                    response.setContact(nok.getContactNo());
                    response.setAddress(nok.getAddressLine());
                });

        // Document Details
        List<DocumentProjection> documents = ipDocumentRepository.findDocumentsByInpatientId(inpatientId);
        List<InpatientAdmissionDetailsResponse.DocumentList> documentList = documents.stream()
                .map(doc -> InpatientAdmissionDetailsResponse.DocumentList.builder()
                        .documentName(doc.getDocumentType())
                        .documentRemarks(doc.getDocumentNotes())
                        .fileName(doc.getFileName())
                        .filePath(doc.getFilePath())
                        .build())
                .collect(Collectors.toList());
        response.setDocumentListList(documentList);

        log.info("Admission details fetched successfully for inpatientId={}", inpatientId);

        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<byte[]> viewAdmissionDocument(String filePath) {
        try {
            String normalizedPath = filePath == null ? "" : filePath.trim();
            if (normalizedPath.startsWith("\"") && normalizedPath.endsWith("\"")) {
                normalizedPath = normalizedPath.substring(1, normalizedPath.length() - 1);
            }
            normalizedPath = normalizedPath.replace("\\\\", "\\");

            if (normalizedPath.isBlank()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "File path is required", HttpStatus.BAD_REQUEST.value()
                );
            }

            Path path = Paths.get(normalizedPath);

            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return ResponseUtils.createNotFoundResponse("File not found", HttpStatus.NOT_FOUND.value()
                );
            }

            byte[] data = Files.readAllBytes(path);
            return ResponseUtils.createSuccessResponse(data, new TypeReference<>() {}, "Document viewed successfully"
            );

        } catch (InvalidPathException e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid file path: " + filePath, HttpStatus.BAD_REQUEST.value()
            );
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Failed to read file: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> saveAdverseReaction(IpAdverseEventRequest request) {

        try {

            User currentUser = authUtil.getCurrentUser();

            // Validate Inpatient
            Inpatient inpatient = inpatientRepository.findById(request.getInpatientId())
                    .orElseThrow(() -> new RuntimeException("Inpatient not found with ID: " + request.getInpatientId()));

            // Validate Medication if provided
            MasStoreItem  medication = masStoreItemRepository.findById(request.getMedicationId())
                        .orElseThrow(() -> new RuntimeException("Medication not found with ID: " + request.getMedicationId()));

            User informedDoctor = null;
            if (AppConstants.STATUS_Y.equalsIgnoreCase(request.getDoctorInformed())) {
                if (request.getInformedDoctorId() == null) {
                    return new ApiResponse<>(null, "Informed doctor ID is required when doctor informed is 'Y'", 400);
                }

                informedDoctor = userRepo.findById(request.getInformedDoctorId())
                        .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + request.getInformedDoctorId()));
            }

            IpAdverseEvent adverseEvent = IpAdverseEvent.builder()
                    .inpatientId(inpatient)
                    .medicationId(medication)
                    .reaction(request.getReaction())
                    .severity(request.getSeverity())
                    .actionTaken(request.getActionTaken())
                    .reactionDatetime(request.getReactionDatetime())
                    .medicationStopped(request.getMedicationStopped().toLowerCase())
                    .doctorInformed(request.getDoctorInformed().toLowerCase())
                    .informedDoctorId(informedDoctor)
                    .patientConditionAfter(request.getPatientConditionAfter())
                    .recordedBy(currentUser.getUserId())
                    .recordedDatetime(LocalDateTime.now())
                    .build();

            ipAdverseEventRepository.save(adverseEvent);

            log.info("Adverse event saved successfully. adverseEventId={}", adverseEvent.getAdverseEventId());
            return ResponseUtils.createSuccessResponse("Adverse reaction saved successfully",new TypeReference<>(){});

        } catch (Exception e) {

            log.error("Error while saving adverse reaction", e);
            return new ApiResponse<>(null, "Failed to save adverse reaction: " + e.getMessage(),400);
        }
    }

    @Override
    public ApiResponse<List<IpAdverseEventResponse>> getAdverseReactionDetails(Long inpatientId) {

        List<IpAdverseEventResponse> result = ipAdverseEventRepository.findAdverseReactionDetailsByInpatientId(inpatientId);
        return ResponseUtils.createSuccessResponse(result,new TypeReference<>(){});

    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }


    @Override
    @Transactional
    public ApiResponse<String> saveNursingCareProcedure(List<ConsumableEntryRequest> requests) {

        log.info("saveNursingCareProcedure started for {} record(s)", requests != null ? requests.size() : 0);

        User user = authUtil.getCurrentUser();
        Long departmentId=authUtil.getCurrentDepartmentId();
        try {

            for (ConsumableEntryRequest request : requests) {
                // Validate Request

                if (request.getInpatientId() == null) {
                    throw new RuntimeException("Inpatient ID is required");
                }
                if (request.getItemId() == null) {
                    throw new RuntimeException("Item ID is required");
                }
                if (request.getRequestQty() == null || request.getRequestQty().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Request quantity must be greater than zero");
                }

                Inpatient inpatient = inpatientRepository.findById(request.getInpatientId()).orElseThrow(() -> new RuntimeException("Inpatient not found: " + request.getInpatientId()));

                // Find Batch Stock

                StoreItemBatchStock stock = storeItemBatchStockRepository.findByItemIdAndBatchNoForUpdate(request.getItemId(), request.getBatchNo(),departmentId)
                                .orElseThrow(() -> new RuntimeException("Batch not found for itemId=" + request.getItemId() + ", batchNo=" + request.getBatchNo()));

                BigDecimal currentQty = stock.getClosingStock() != null ? BigDecimal.valueOf(stock.getClosingStock()) : BigDecimal.ZERO;

                // Runtime Stock Check
                if (currentQty.compareTo(request.getRequestQty()) < 0) {
                    throw new RuntimeException("Insufficient Stock");
                }

                // Calculate Updated Stock
                BigDecimal currentIpdIssueQty = stock.getIpdIssueQty() != null ? stock.getIpdIssueQty() : BigDecimal.ZERO;
                BigDecimal updatedIpdIssueQty = currentIpdIssueQty.add(request.getRequestQty());
                BigDecimal updatedQty = currentQty.subtract(request.getRequestQty());
                // Update Batch Stock
                stock.setClosingStock(updatedQty.longValue());
                stock.setIpdIssueQty(updatedIpdIssueQty);
                storeItemBatchStockRepository.save(stock);

                log.info("Stock updated for batchNo={}, before={}, after={}", request.getBatchNo(), currentQty, updatedQty);

                // Store IpMedicineIssue

                IpMedicineIssue ipMedicineIssue = new IpMedicineIssue();

                ipMedicineIssue.setInpatient(inpatient);
                ipMedicineIssue.setItem(stock.getItemId());
                ipMedicineIssue.setBatch(stock);
                ipMedicineIssue.setBatchNo(request.getBatchNo());
                ipMedicineIssue.setExpiryDate(request.getExpiryDate());
                ipMedicineIssue.setIssueQty(request.getRequestQty());
                ipMedicineIssue.setIssueDatetime(LocalDateTime.now());
                ipMedicineIssue.setIssuedBy(user.getUserId());
                ipMedicineIssue.setCreatedBy(user.getUserId());
                ipMedicineIssue.setCreatedOn(LocalDateTime.now());
                ipMedicineIssue.setLastChgBy(user.getUserId());
                ipMedicineIssue.setLastChgOn(LocalDateTime.now());
                ipMedicineIssue.setRemarks(request.getRemark());

                ipMedicineIssueRepository.save(ipMedicineIssue);

                // Save IP Consumable Transaction
                IpConsumableTxn ipConsumableTxn = new IpConsumableTxn();

                ipConsumableTxn.setInpatientId(inpatient);
                ipConsumableTxn.setItemId(masStoreItemRepository.findById(request.getItemId()).orElseThrow());
                ipConsumableTxn.setItemName(stock.getItemId().getNomenclature());
                ipConsumableTxn.setQuantity(request.getRequestQty());
                ipConsumableTxn.setUom(stock.getItemId().getUnitAU().getUnitName());
                ipConsumableTxn.setBatchNo(request.getBatchNo());
                ipConsumableTxn.setExpiryDate(request.getExpiryDate());
                ipConsumableTxn.setUsageDatetime(request.getDateTime());
                ipConsumableTxn.setUsedBy(request.getGivenBy());
                if (request.getProcedureId() != null) {
                    IpProcedureTxn procedureTxn = ipProcedureTxnRepository.findById(request.getProcedureId()).orElseThrow(() ->
                                    new RuntimeException("Procedure transaction not found: " +request.getProcedureId()));
                    ipConsumableTxn.setProcedureTxnId(procedureTxn);
                } else {
                    ipConsumableTxn.setProcedureTxnId(null);
                }
                ipConsumableTxn.setRemarks(request.getRemark());
                ipConsumableTxn.setCreatedBy(user.getFullName());
                ipConsumableTxn.setCreatedAt(LocalDateTime.now());

                ipConsumableTxnRepository.save(ipConsumableTxn);

                log.info("IpConsumableTxn saved successfully. consumableTxnId={}, inpatientId={}, itemId={}, qty={}",
                        ipConsumableTxn.getConsumableTxnId(),
                        request.getInpatientId(),
                        request.getItemId(),
                        request.getRequestQty()
                );

                // Stock Ledger
                StoreStockLedgerRequest storeStockLedgerRequest=new StoreStockLedgerRequest();

                storeStockLedgerRequest.setStockId(stock.getStockId());
                storeStockLedgerRequest.setTxnType(AppConstants.INPATIENT_ISSUE);
                storeStockLedgerRequest.setTxnReferenceId(ipMedicineIssue.getIpMedicineIssueId());
                storeStockLedgerRequest.setQtyBefore(currentQty);
                storeStockLedgerRequest.setQtyOut(request.getRequestQty());
                storeStockLedgerRequest.setQtyAfter(updatedQty);
                storeStockLedgerRequest.setTxnSource(AppConstants.INPATIENT_ISSUE);
                storeStockLedgerRequest.setCreatedBy(user.getUsername());
                storeStockLedgerRequest.setHospitalId(stock.getHospitalId().getId());
                storeStockLedgerRequest.setDepartmentId(stock.getDepartmentId().getId());
                inventoryUtils.updateStoreStockLedger(storeStockLedgerRequest);

                // Billing
                BigDecimal amount = calculateAmount(stock.getMrpPerUnit(), request.getRequestQty());

                BigDecimal gstAmount = calculateGST(stock.getMrpPerUnit(), request.getRequestQty(), stock.getGstPercent());

                BigDecimal netAmount = calculateNetAmount(stock.getMrpPerUnit(), request.getRequestQty(), stock.getGstPercent());

                Optional<MasIpdServiceCategory> masIpdServiceCategory = masIpdServiceCategoryRepository.findById(IPDServiceCategoryMedicalConsumables);

                if (masIpdServiceCategory.isEmpty()) {
                    throw new RuntimeException("IPD Service Category not found: " + IPDServiceCategoryDrug);
                }
                ItemClassBillSubcategoryMapping mapping = itemClassBillSubcategoryMappingRepository.findByItemClass_ItemClassId(stock.getItemId().getItemClassId().getItemClassId());

                MasIpdServiceSubcategory subcategory = null;
                if (mapping != null && mapping.getIpdBillSubcategoryId() != null) {
                    subcategory = masIpdServiceSubcategoryRepository.findById(mapping.getIpdBillSubcategoryId().getSubcategoryId()).orElse(null);
                }

                saveIpdBillingDetails.saveInpatientBillingDetails(
                        inpatient,
                        stock.getMrpPerUnit(),
                        request.getRequestQty(),
                        stock.getGstPercent(),
                        BigDecimal.ZERO,
                        amount,
                        gstAmount,
                        netAmount,
                        masIpdServiceCategory.get(),
                        subcategory,
                        stock.getItemId().getNomenclature()
                );

                // Mark consumable transaction as billed
                ipConsumableTxn.setIsBilled(true);
                ipConsumableTxn.setUpdatedBy(user.getUsername());
                ipConsumableTxn.setUpdatedAt(LocalDateTime.now());

                ipConsumableTxnRepository.save(ipConsumableTxn);

                log.info("Billing entry saved for inpatientId={}, amount={}, netAmount={}", inpatient.getInpatientId(), amount, netAmount);
            }

            log.info("saveNursingCareProcedure completed successfully for {} record(s)", requests.size());

            return ResponseUtils.createSuccessResponse("Nursing care consumable details saved successfully", new TypeReference<>() {});

        } catch (Exception e) {

            log.error("saveNursingCareProcedure failed, rolling back transaction. Reason: {}", e.getMessage(), e);

            throw e;
        }
    }





}
