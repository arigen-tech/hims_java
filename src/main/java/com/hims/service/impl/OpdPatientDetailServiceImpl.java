package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.projection.PrescriptionDetailProjection;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.helperUtil.HelperUtils;
import com.hims.mapper.InvestigationData;
import com.hims.mapper.OpdPatientDetailMapper;
import com.hims.mapper.TreatmentData;
import com.hims.projection.*;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.*;
import com.hims.utils.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hims.helperUtil.ConverterUtils.ageCalculator;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpdPatientDetailServiceImpl implements OpdPatientDetailService {


    private final OpdPatientDetailRepository opdPatientDetailRepository;

    private final DischargeIcdCodeRepository dischargeIcdCodeRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final MasDepartmentRepository departmentRepository;
    private final MasHospitalRepository hospitalRepository;
    private final UserRepo userRepository;
    private final MasIcdRepository masIcdRepository;
    private final StockFound stockFound;
    private final DgMasInvestigationRepository dgMasInvestigationRepository;
    private final LabHdRepository dgOrderHdRepo;
    private final MasInvestigationPriceDetailsRepository masInvestigationPriceDetailsRepository;
    private final LabDtRepository dgOrderDtRepo;
    private final PatientPrescriptionHdRepository patientPrescriptionHdRepository;
    private final PatientPrescriptionDtRepository patientPrescriptionDtRepository;
    private final AuthUtil authUtil;
    private final MasStoreItemRepository masStoreItemRepository;
    private final MasCareLevelRepo masCareLevelRepository;
    private final MasWardCategoryRepository masWardCategoryRepository;
    private final MasDepartmentRepository masDepartmentRepository;
    private final LabOrderTrackingStatusRepository labOrderTrackingStatusRepository;
    private final RadOrderHdRepository radOrderHdRepository;
    private final RadOrderDtRepository radOrderDtRepository;
    private final MasServiceCategoryRepository masServiceCategoryRepository;
    private final PatientPrescriptionDtRepository prescriptionDtRepository;
    private final OpdOpthDetailsService opdOpthDetailsService;
    private final OpdObgDetailsService opdObgDetailsService;
    private final OpdEntDetailsService opdEntDetailsService;
    private final OpdPatientPregnancyDetailsRepository opdPatientPregnancyDetailsRepository;
    private final OpdPsychiatryAssessmentDetailRepository opdPsychiatryAssessmentDetailRepository;
    private final OpdPsychiatryAssessmentHeaderRepository opdPsychiatryAssessmentHeaderRepository;
    private final MasQuestionHeadingRepository masQuestionHeadingRepository;
    private final OpdQuestionMasterRepository opdQuestionMasterRepository;
    private final MasQuestionOptionValueRepository masQuestionOptionValueRepository;
    private final TransactionSequenceService transactionSequenceService;
    private final MasFrequencyRepository masFrequencyRepository;
    private final BillingService billingService;
    private final OpdPatientDetailMapper opdPatientDetailMapper;

    @Value("${hos.define.storeDay}")
    private Integer hospDefinedDays;

    @Value("${hos.define.storeId}")
    private Integer deptIdStore;

    @Value("${lab.track-order-status-reg.ordered}")
    private Long orderedStatusId;

    @Value("${prescription.history.days}")
    private Integer prescriptionHistoryDays;

    @Value("${app.radiologyDepartment}")
    private Integer radiologyDepartment;

    @Value("${app.laboratoryDepartment}")
    private Integer laboratoryDepartment;

    @Value("${serviceCategoryLab}")
    private String serviceCategoryLab;

    @Value("${serviceCategoryRad}")
    private String serviceCategoryRad;

    @Autowired
    HelperUtils helperUtils;

    @Autowired
    BillingHeaderRepository  billingHeaderRepository;

    @Autowired
    BillingDetailRepository billingDetailRepository;



    @Override
    public ApiResponse<OpdPatientVitalResponse> getOpdPatientByVisit(Long visitId) {
        if (visitId == null) {
            throw new IllegalArgumentException("Visit ID must not be null");
        }
        OpdPatientDetail opdPObj = opdPatientDetailRepository.findByVisitId(visitId);
        if (opdPObj == null) {
            return ResponseUtils.createNotFoundResponse("OPD details not found for visitId: " + visitId, 404);
        }
        OpdPatientVitalResponse responseDto = opdPatientDetailMapper.mapToVitalResponse(opdPObj);
        return ResponseUtils.createSuccessResponse(responseDto, new TypeReference<>() {
        });
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<OpdPatientDetailResponseDTO> createOpdPatientDetail(OpdPatientDetailCreateRequest request) {
        validateCreateRequest(request);
        User user = getCurrentUser();
        if (user == null || user.getHospital() == null) {
            throw new SDDException("user", 401, "Authenticated user or hospital not found");
        }
        Patient patient = getPatient(request.getPatientId());
        Visit visit = getVisit(request.getVisitId());
        Long deptId = authUtil.getCurrentDepartmentId();
        OpdPatientDetail opd = opdPatientDetailRepository.findByVisit_Id(request.getVisitId()).orElseGet(() -> {
                    log.info("Creating new OPD Patient Detail for visit ID: {}", request.getVisitId());
                    return new OpdPatientDetail();
                });

        if ((request.getWorkingDiagnosis() == null || request.getWorkingDiagnosis().isBlank()) && (request.getIcdDiagnosis() == null || request.getIcdDiagnosis().isEmpty())) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.MANDATORY_DIAGNOSIS_MESSAGE, 400);
        }
        opdPatientDetailMapper.mapBasicVitalDetails(opd, request);
        opdPatientDetailMapper.mapClinicalDetails(opd, request);
        opdPatientDetailMapper.mapGeneralDetails(opd, patient, visit, user, deptId);
        handleAdmission(opd, request);
        handleFollowUp(opd, request);
        handleReferral(opd, request);
        opd.setLabFlag(AppConstants.STATUS_N.toLowerCase());
        opd.setRadioFlag(AppConstants.STATUS_N.toLowerCase());
        OpdPatientDetail saved = opdPatientDetailRepository.save(opd);
        log.info("Saved OPD detail with ID: {}", saved.getOpdPatientDetailsId());

        //saving diagnosis data for opd - Start
        List<Long> diagIdList = Optional.ofNullable(request.getIcdDiagnosis())
                .orElse(Collections.emptyList())
                .stream()
                .map(OpdPatientDetailCreateRequest.IcdDiagnosis::getIcdId)
                .collect(Collectors.toList());
        saveOrUpdateIcdDiagnosis(diagIdList, saved.getOpdPatientDetailsId(), request.getVisitId(), user.getUserId());
        //diagnosis - End


        if (request.getInvestigation() != null && !request.getInvestigation().isEmpty()) {
            if (request.getInvestigation().stream().anyMatch(i -> i == null || i.getInvestigationDate() == null)) {
                throw new SDDException("investigation", 400, "Investigation date cannot be null");
            }
            LabOrderTrackingStatus labOrderedStatus = labOrderTrackingStatusRepository.findById(orderedStatusId)
                    .orElseThrow(() -> new SDDException("status", 500, "Ordered status not found with id: " + orderedStatusId));

            List<InvestigationData> investigations =
                    opdPatientDetailMapper.mapInvestigations(
                            request.getInvestigation(),
                            item -> new InvestigationData(
                                    item.getInvestigationId(),
                                    item.getInvestigationName(),
                                    item.getInvestigationDate(),
                                    item.getInvestigationId(),
                                    null
                            ));

            // Group investigations by department
            Map<Long, Map<LocalDate, List<InvestigationData>>> grouped =
                    investigations.stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.groupingBy(
                                    inv -> helperUtils.getDepartmentFromInvestigation(
                                            inv.investigationId()
                                    ),
                                    Collectors.groupingBy(
                                            InvestigationData::investigationDate
                                    )));


            //if hospital flag
           // if( AppConstants.STATUS_N.equalsIgnoreCase(patient.getPatientHospital().getLabBilling())) {
                if (grouped.containsKey(Long.valueOf(laboratoryDepartment))) {
                    log.info("Processing LAB investigations");
                    Map<LocalDate, List<InvestigationData>> labInvestigations = grouped.get(Long.valueOf(laboratoryDepartment));

                    processLabInvestigations(labInvestigations, patient, visit, user, labOrderedStatus);

                    String labAdvised = labInvestigations.values().stream()
                            .flatMap(List::stream)
                            .map(InvestigationData::investigationName)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.joining(", "));

                    saved.setLabAdvised(labAdvised);
                    saved.setLabFlag(AppConstants.STATUS_Y.toLowerCase());

                }
           // }
//            if( AppConstants.STATUS_N.equalsIgnoreCase(patient.getPatientHospital().getRadioBilling())){
                if (grouped.containsKey(Long.valueOf(radiologyDepartment))) {
                    log.info("Processing RADIOLOGY investigations");
                    Map<LocalDate, List<InvestigationData>> radioInvestigations = grouped.get(Long.valueOf(radiologyDepartment));
                    String radioAdvised = radioInvestigations.values().stream()
                            .flatMap(List::stream)
                            .map(InvestigationData::investigationName)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.joining(", "));

                    processRadiologyInvestigations(radioInvestigations, patient, visit, user);

                    saved.setRadioAdvised(radioAdvised);
                    saved.setRadioFlag(AppConstants.STATUS_Y.toLowerCase());
                }
//            }
        }

        if (request.getTreatment() != null && !request.getTreatment().isEmpty()) {
            List<TreatmentData> treatments = request.getTreatment()
                    .stream()
                    .map(t -> new TreatmentData(
                            null,
                            null,
                            t.getItemId(),
                            t.getDosage(),
                            t.getFrequency(),
                            t.getDays(),
                            t.getTotal(),
                            t.getInstraction(),
                            null
                    ))
                    .toList();
            saveOrUpdateTreatments(treatments,patient,visit,user,deptId);
        }

        if (request.getOphthalmologyExaminationDetails() != null) {
            OpdOpthDetailsRequest opthRequest = request.getOphthalmologyExaminationDetails();
            opthRequest.setPatientId(patient.getId());
            opthRequest.setVisitId(visit.getId());
            ApiResponse<String> response = opdOpthDetailsService.opdVisionExaminationDetailsSaveOrUpdate(opthRequest);
            if (response == null || response.getStatus() != HttpStatus.OK.value()) {
                throw new SDDException("ophthalmology", 500, response != null ? response.getMessage() : "Failed to save ophthalmology details");
            }
        }
        if (request.getOpdObgDetailsRequest() != null) {
            request.getOpdObgDetailsRequest().setPatientId(patient.getId());
            request.getOpdObgDetailsRequest().setVisitId(visit.getId());
            ApiResponse<String> response = opdObgDetailsService.createOrUpdateObgDetails(request.getVisitId(), request.getOpdObgDetailsRequest());
            if (response == null || response.getStatus() != HttpStatus.OK.value()) {
                throw new SDDException("obg", 500, response != null ? response.getMessage() : "Failed to save OBG details");
            }
        }
        if (request.getEntExaminationDetails() != null) {
            ApiResponse<String> response = opdEntDetailsService.createOrUpdateEntDetails(request.getVisitId(), request.getEntExaminationDetails());
            if (response == null || response.getStatus() != HttpStatus.OK.value()) {
                throw new SDDException("ent", 500, response != null ? response.getMessage() : "Failed to save ENT details");
            }
        }
        if (request.getPregnancyDetails() != null) {
            handlePregnancyDetails(saved, request.getPregnancyDetails(), user);
        }
        if (request.getPsychiatricDetailsRequests() != null && !request.getPsychiatricDetailsRequests().isEmpty()) {
            log.info("Saving Psychiatric Assessment Header and Details for OPD ID: {}", saved.getOpdPatientDetailsId());
            saveOrUpdatePsychiatricAssessment(request.getPsychiatricDetailsRequests(),request.getTopicId(), visit, saved, authUtil.getCurrentUser());
        }
        opdPatientDetailRepository.save(saved);
        closeVisit(visit);
        log.info("Successfully completed OPD patient detail creation for visit ID: {}", visit.getId());
        return ResponseUtils.createSuccessResponse(null, new TypeReference<>() {
        });
    }


    private void validateCreateRequest(OpdPatientDetailCreateRequest request) {
        if (request == null) {
            throw new SDDException("request", 400, "Request cannot be null");
        }
        if (request.getPatientId() == null) {
            throw new SDDException("patient", 400, "Patient ID is required");
        }
        if (request.getVisitId() == null) {
            throw new SDDException("visit", 400, "Visit ID is required");
        }
    }


    private void validateUpdateRequest(RecallOpdPatientDetailRequest request) {
        if (request == null) {
            throw new SDDException("request", 400, "Request cannot be null");
        }
        Objects.requireNonNull(request.getPatientId(), "OPD ID required");
    }

    private Patient getPatient(Long patientId) {
        return patientRepository.findById(patientId).orElseThrow(() -> new SDDException("patient", 404, "Patient not found"));
    }

    private Visit getVisit(Long visitId) {
        return visitRepository.findById(visitId).orElseThrow(() -> new SDDException("visit", 404, "Visit not found"));
    }

    private OpdPatientDetail getOpdPatient(Long opdId) {
        return opdPatientDetailRepository.findById(opdId).orElseThrow(() -> new SDDException("opd", 404, "OPD detail not found"));
    }



    private PatientPrescriptionHd createPrescriptionHeader(Patient patient, Visit visit, User user, Long deptId) {
        PatientPrescriptionHd hd = new PatientPrescriptionHd();
        hd.setHospitalId(user.getHospital().getId());
        hd.setPatientId(patient.getId());
        hd.setDepartmentId(deptId);
        hd.setDoctorName(user.getFirstName());
        hd.setPrescriptionDate(LocalDateTime.now());
        hd.setStatus(AppConstants.STATUS_N.toLowerCase());
        hd.setCreatedBy(user.getFirstName());
        hd.setTotalCost(BigDecimal.ZERO);
        hd.setTotalGst(BigDecimal.ZERO);
        hd.setTotalDiscount(BigDecimal.ZERO);
        hd.setNetAmount(BigDecimal.ZERO);
        hd.setPrescriptionNumber(transactionSequenceService.generateTransactionNumber(HMISTransaction.PRESCRIPTION_NO, user.getHospital().getId()));
        hd.setVisit(visit);

        String medicineBilling = user.getHospital().getMedicineBilling();
        if (medicineBilling != null && AppConstants.PAYMENT_NOT_PAID.toLowerCase().equalsIgnoreCase(medicineBilling)) {
            hd.setBillingStatus(AppConstants.STATUS_N.toLowerCase());
        } else {
            hd.setBillingStatus(AppConstants.STATUS_Y.toLowerCase());
        }

        return patientPrescriptionHdRepository.save(hd);
    }

    private void closeVisit(Visit visit) {
        if (visit != null) {
            visit.setVisitStatus(AppConstants.VISIT_STATUS_COMPLETED.toLowerCase());
            visit.setDoctor(authUtil.getCurrentUser());
            visit.setDoctorName(authUtil.getCurrentUser().getFullName());
            visitRepository.save(visit);
            log.info("Closed visit with ID: {}", visit.getId());
        }
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public ApiResponse<String> updateRecallOpdPatientDetail(RecallOpdPatientDetailRequest request) {
        validateUpdateRequest(request);
        User user = getCurrentUser();
        if (user == null) {
            throw new SDDException("user", 401, "Current user not found or not authenticated");
        }
        Patient patient = getPatient(request.getPatientId());
        Visit visit = getVisit(request.getVisitId());
        OpdPatientDetail opd = getOpdPatient(request.getOpdPatientDetailId());
        mapRecallBasicDetails(opd, request);
        mapRecallClinicalDetails(opd, request);
        handleRecallAdmission(opd, request);
        handleRecallFollowUp(opd, request);
        handleRecallReferral(opd, request);
        OpdPatientDetail opdPatientDetail = opdPatientDetailRepository.save(opd);
        handleRecallPregnancyDetails(opd, request.getPregnancyDetails(), user);

        //ICD diagnosis details{
        List<Long> icdIds = Optional.ofNullable(request.getIcdDiagnosisList())
                .orElse(Collections.emptyList())
                .stream()
                .map(RecallOpdPatientDetailRequest.IcdDiagnosis::getIcdId)
                .collect(Collectors.toList());
        saveOrUpdateIcdDiagnosis(icdIds, opd.getOpdPatientDetailsId(), visit.getId(), user.getUserId());
        //}

        //investigation details{

        List<InvestigationData> investigations =
                opdPatientDetailMapper.mapInvestigations(
                        request.getInvestigations(),
                        item -> new InvestigationData(
                                item.getInvestigationId(),
                                item.getInvestigationName(),
                                item.getInvestigationDate(),
                                item.getInvestigationId(),
                                item.getFlag()
                        ));

        createOrDeleteInvestigation(investigations, patient, visit, user, opdPatientDetail);

        updateInvestigationAdvisedNames(
                investigations,
                opdPatientDetail
        );

        //    }
        //Treatment data {
        List<TreatmentData> treatments = request.getTreatments()
                .stream()
                .map(t -> new TreatmentData(
                        t.getPrescriptionHdId(),
                        t.getPrescriptionDtId(),
                        t.getItemId(),
                        t.getDosage(),
                        t.getFrequencyName() != null
                                ? t.getFrequencyName()
                                : null,
                        t.getDays(),
                        t.getTotal() != null
                                ? BigDecimal.valueOf(t.getTotal())
                                : null,
                        t.getInstruction(),
                        t.getFlag()
                ))
                .toList();

        saveOrUpdateTreatments(treatments,patient,visit,user,authUtil.getCurrentDepartmentId());
        //}

        replacePsychiatryAssessment(request, patient, visit, user);

        //OBG details
        if (request.getOpdObgDetailsRequest() != null) {
            handleRecallObgDetails(request, patient, visit, user);
        }
        //
        //EarExamination details save and update
        if (request.getEntExaminationDetails() != null) {
            ApiResponse<String> response = opdEntDetailsService.createOrUpdateEntDetails(request.getVisitId(), request.getEntExaminationDetails());
            if (response == null || response.getStatus() != HttpStatus.OK.value()) {
                throw new SDDException("ent", 500, response != null ? response.getMessage() : "Failed to save ENT details");
            }
        }
        // opthal details Save and update
        if (request.getOphthalmologyExaminationDetails() != null) {
            OpdOpthDetailsRequest opthRequest = request.getOphthalmologyExaminationDetails();
            opthRequest.setPatientId(patient.getId());
            opthRequest.setVisitId(visit.getId());
            ApiResponse<String> response = opdOpthDetailsService.opdVisionExaminationDetailsSaveOrUpdate(opthRequest);
            if (response == null || response.getStatus() != HttpStatus.OK.value()) {
                throw new SDDException("ophthalmology", 500, response != null ? response.getMessage() : "Failed to save ophthalmology details");
            }
        }
        return ResponseUtils.createSuccessResponse("Patient updated successfully", new TypeReference<>() {
        });
    }

    private void saveOrUpdateIcdDiagnosis(List<Long> icdIds, Long opdId,Long visitId,Long userId) {

        dischargeIcdCodeRepository.deleteByOpdPatientDetailsId(opdId);
        if (icdIds == null || icdIds.isEmpty()) {
            return;
        }
        List<DischargeIcdCode> entities = new ArrayList<>();
        for (Long icdId : icdIds) {
            if (icdId == null) {
                continue;
            }
            DischargeIcdCode entity = new DischargeIcdCode();
            entity.setIcdId(icdId);
            entity.setVisitId(visitId);
            entity.setOpdPatientDetailsId(opdId);
            entity.setAddEditById(userId);
            entity.setAddEditDate(LocalDate.now());
            entity.setAddEditTime(LocalTime.now().toString());
            entities.add(entity);
        }
        if (!entities.isEmpty()) {
            dischargeIcdCodeRepository.saveAll(entities);
        }
    }

    private void deleteTreatmentsByIds(List<Long> removedTreatmentIds) {
        if (removedTreatmentIds == null || removedTreatmentIds.isEmpty()) {
            return;
        }

        for (Long id : removedTreatmentIds) {
            patientPrescriptionDtRepository.findById(id).ifPresent(dt -> {
                patientPrescriptionDtRepository.delete(dt);
                log.info("Deleted treatment with ID: {}", id);
            });
        }
    }


    private void saveOrUpdateTreatments(
            List<TreatmentData> treatments,
            Patient patient,
            Visit visit,
            User user,
            Long deptId) {

        if (treatments == null || treatments.isEmpty()) {
            return;
        }
        List<PatientPrescriptionHd> existingHeaders = patientPrescriptionHdRepository.findAllByVisit_Id(visit.getId());

        Map<Long, PatientPrescriptionHd> headerMap =
                existingHeaders.stream()
                        .filter(Objects::nonNull)
                        .filter(h -> h.getPrescriptionHdId() != null)
                        .collect(Collectors.toMap(
                                PatientPrescriptionHd::getPrescriptionHdId,
                                Function.identity()
                        ));

        Map<Long, PatientPrescriptionDt> existingMap = new HashMap<>();
        for (PatientPrescriptionHd header : existingHeaders) {
            List<PatientPrescriptionDt> details = patientPrescriptionDtRepository.findByPrescriptionHdId(header.getPrescriptionHdId());
            for (PatientPrescriptionDt dt : details) {
                if (dt.getPrescriptionDtId() != null) {
                    existingMap.put(dt.getPrescriptionDtId(),dt);
                }
            }
        }
        List<TreatmentData> deleteList = treatments.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.itemId() != null)
                .filter(t -> t.flag() != null && t.flag() == -1)
                .toList();

        List<TreatmentData> updateList = treatments.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.itemId() != null)
                .filter(t -> t.flag() != null && t.flag() == 0)
                .toList();

        List<TreatmentData> createList = treatments.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.itemId() != null)
                .filter(t -> t.flag() != null && t.flag() == 1)
                .toList();

        Set<Long> headersToCheckForDelete = new HashSet<>();

        for (TreatmentData treatment : deleteList) {
            if (treatment.prescriptionDtId() == null) {
                log.warn("Cannot delete treatment. prescriptionDtId is null. itemId={}",treatment.itemId());
                continue;
            }
            PatientPrescriptionDt dt = existingMap.get(treatment.prescriptionDtId());

            if (dt == null) {
                log.warn("Treatment detail ID {} not found for deletion", treatment.prescriptionDtId());
                continue;
            }

            Long headerId = dt.getPrescriptionHdId();
            patientPrescriptionDtRepository.delete(dt);
            existingMap.remove(treatment.prescriptionDtId());
            if (headerId != null) {
                headersToCheckForDelete.add(headerId);
            }
            log.info("Deleted treatment detail ID: {} from header ID: {}",treatment.prescriptionDtId(),headerId);
        }

        for (TreatmentData treatment : updateList) {
            if (treatment.prescriptionDtId() == null) {
                log.warn("Cannot update treatment. prescriptionDtId is null. itemId={}",treatment.itemId());
                continue;
            }
            PatientPrescriptionDt dt =existingMap.get(treatment.prescriptionDtId());
            if (dt == null) {
                log.warn("Treatment detail ID {} not found for update",treatment.prescriptionDtId());
                continue;
            }
            dt.setItemId(treatment.itemId());
            dt.setDosage(treatment.dosage());
            dt.setFrequency(treatment.frequency());
            dt.setDays(treatment.days());
            dt.setTotal(treatment.total());
            dt.setInstruction(treatment.instruction());
            patientPrescriptionDtRepository.save(dt);
            log.info("Updated treatment detail ID: {} under header ID: {}",dt.getPrescriptionDtId(),dt.getPrescriptionHdId());
        }
        Map<Long, PatientPrescriptionHd> newHeaderMap = new HashMap<>();

        for (TreatmentData treatment : createList) {
            Long requestedHeaderId = treatment.prescrptionHdId();
            PatientPrescriptionHd headerToUse = null;
            if (requestedHeaderId != null) {
                headerToUse =  newHeaderMap.get(requestedHeaderId);
                if (headerToUse == null) {
                    PatientPrescriptionHd existingHeader = headerMap.get(requestedHeaderId);
                    if (existingHeader != null && AppConstants.STATUS_Y.equalsIgnoreCase(existingHeader.getStatus())) {
                        headerToUse = createPrescriptionHeader(patient, visit,user,deptId);
                        newHeaderMap.put(requestedHeaderId,headerToUse);
                        log.info("Existing header {} has status Y. " +"Created new header {}.",requestedHeaderId,headerToUse.getPrescriptionHdId());
                    }
                    else if (existingHeader != null&& AppConstants.STATUS_N.equalsIgnoreCase(existingHeader.getStatus())) {
                        headerToUse = existingHeader;
                        log.info("Using existing unbilled header {}.",requestedHeaderId);
                    }
                    else {
                        headerToUse = createPrescriptionHeader(patient,visit,user,deptId);
                        log.info("Header {} not found. Created new header {}.",requestedHeaderId,headerToUse.getPrescriptionHdId());
                    }
                }
            }

            if (headerToUse == null) {
                headerToUse = createPrescriptionHeader(patient,visit,user,deptId);
                log.info("No prescription header supplied. " +"Created new header {}.",headerToUse.getPrescriptionHdId());
            }

            PatientPrescriptionDt dt = new PatientPrescriptionDt();
            dt.setPrescriptionHdId(headerToUse.getPrescriptionHdId());
            dt.setItemId(treatment.itemId());
            dt.setDosage(treatment.dosage());
            dt.setFrequency(treatment.frequency());
            dt.setDays(treatment.days());
            dt.setTotal(treatment.total());
            dt.setInstruction(treatment.instruction());
            dt.setStatus(AppConstants.STATUS_N.toLowerCase());
            patientPrescriptionDtRepository.save(dt);
            log.info("Created treatment detail ID: {} under header ID: {}", dt.getPrescriptionDtId(),headerToUse.getPrescriptionHdId()
            );
        }
        for (Long headerId : headersToCheckForDelete) {
            List<PatientPrescriptionDt> remainingDetails = patientPrescriptionDtRepository.findByPrescriptionHdId(headerId);
            if (remainingDetails.isEmpty()) {
                patientPrescriptionHdRepository.deleteById(headerId);
                log.info(
                        "Deleted empty prescription header ID: {}",
                        headerId
                );
            }
        }
    }

    private void updateInvestigationAdvisedNames(
            List<InvestigationData> investigations,
            OpdPatientDetail opdPatientDetail) {

        if (investigations == null || investigations.isEmpty()) {
            opdPatientDetail.setLabAdvised(null);
            opdPatientDetail.setRadioAdvised(null);
            return;
        }

        Long labDepartmentId = Long.valueOf(laboratoryDepartment);
        Long radDepartmentId = Long.valueOf(radiologyDepartment);

        // LAB
        String labAdvised = investigations.stream()
                .filter(Objects::nonNull)
                .filter(inv -> inv.flag() != null && inv.flag() != -1)
                .filter(inv -> inv.investigationId() != null)
                .filter(inv -> labDepartmentId.equals(
                        helperUtils.getDepartmentFromInvestigation(
                                inv.investigationId()
                        )
                ))
                .map(InvestigationData::investigationName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(", "));

        // RADIOLOGY
        String radioAdvised = investigations.stream()
                .filter(Objects::nonNull)
                .filter(inv -> inv.flag() != null && inv.flag() != -1)
                .filter(inv -> inv.investigationId() != null)
                .filter(inv -> radDepartmentId.equals(
                        helperUtils.getDepartmentFromInvestigation(
                                inv.investigationId()
                        )
                ))
                .map(InvestigationData::investigationName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(", "));

        opdPatientDetail.setLabAdvised(
                labAdvised.isBlank() ? null : labAdvised
        );

        opdPatientDetail.setRadioAdvised(
                radioAdvised.isBlank() ? null : radioAdvised
        );
    }

    private void createOrDeleteInvestigation(List<InvestigationData> investigations, Patient patient, Visit visit, User user,OpdPatientDetail opdPatientDetail) {
        if (investigations == null || investigations.isEmpty()) {
            return;
        }
        record InvestigationKey(Long investigationId, LocalDate investigationDate) {
        }
        Long labDepartmentId = Long.valueOf(laboratoryDepartment);
        Long radDepartmentId = Long.valueOf(radiologyDepartment);

        List<DgOrderHd> labHeaders = dgOrderHdRepo.findAllByVisitId(visit);
        List<DgOrderDt> labDetails = new ArrayList<>();

        for (DgOrderHd header : labHeaders) {
            List<DgOrderDt> details = dgOrderDtRepo.findByOrderHd(header);
            if (details != null) {
                labDetails.addAll(details);
            }
        }

        Set<InvestigationKey> existingLabKeys = labDetails.stream()
                .filter(dt -> dt.getInvestigation() != null)
                .filter(dt -> dt.getAppointmentDate() != null)
                .map(dt -> new InvestigationKey(
                        dt.getInvestigation().getInvestigationId(),
                        dt.getAppointmentDate()
                ))
                .collect(Collectors.toSet());

        // 2. EXISTING RADIOLOGY ORDERS

        List<RadOrderHd> radHeaders = radOrderHdRepository.findAllByVisit_Id(visit.getId());
        List<RadOrderDt> radDetails = new ArrayList<>();
        for (RadOrderHd header : radHeaders) {
            List<RadOrderDt> details = radOrderDtRepository.findByRadOrderhd(header);

            if (details != null) {
                radDetails.addAll(details);
            }
        }
        Set<InvestigationKey> existingRadKeys = radDetails.stream()
                .filter(dt -> dt.getInvestigation() != null)
                .filter(dt -> dt.getAppointmentDate() != null)
                .map(dt -> new InvestigationKey(
                        dt.getInvestigation().getInvestigationId(),
                        dt.getAppointmentDate()
                ))
                .collect(Collectors.toSet());

        // 3. REQUESTED INVESTIGATIONS
        Set<InvestigationKey> requestedKeys = investigations.stream()
                .filter(Objects::nonNull)
                .filter(inv -> inv.investigationId() != null)
                .filter(inv -> inv.investigationDate() != null)
                .map(inv -> new InvestigationKey(
                        inv.investigationId(),
                        inv.investigationDate()
                ))
                .collect(Collectors.toSet());

        
        //validate the same date same investigation
        investigations.stream()
                .filter(Objects::nonNull)
                .filter(inv -> inv.flag() == 1)
                .filter(inv -> inv.investigationId() != null)
                .filter(inv -> inv.investigationDate() != null)
                .filter(inv -> labDepartmentId.equals(
                        helperUtils.getDepartmentFromInvestigation(
                                inv.investigationId()
                        )
                ))
                .filter(inv -> existingLabKeys.contains(
                        new InvestigationKey(
                                inv.investigationId(),
                                inv.investigationDate()
                        )
                ))
                .findFirst()
                .ifPresent(inv -> {
                    throw new SDDException("investigation",400,AppConstants.CANNOT_ADD_SAME_INVESTIGATION );
                });

        investigations.stream()
                .filter(Objects::nonNull)
                .filter(inv -> inv.flag() == 1)
                .filter(inv -> inv.investigationId() != null)
                .filter(inv -> inv.investigationDate() != null)
                .filter(inv -> radDepartmentId.equals(
                        helperUtils.getDepartmentFromInvestigation(
                                inv.investigationId()
                        )
                ))
                .filter(inv -> existingRadKeys.contains(
                        new InvestigationKey(
                                inv.investigationId(),
                                inv.investigationDate()
                        )
                ))
                .findFirst()
                .ifPresent(inv -> {
                    throw new SDDException(
                            "investigation",
                            400,
                            AppConstants.CANNOT_ADD_SAME_INVESTIGATION
                    );
                });


        // 4. LAB ADD LIST
        List<InvestigationData> addLabList = investigations.stream()
                .filter(Objects::nonNull)
                .filter(inv -> inv.flag() == 1)
                .filter(inv -> inv.investigationId() != null)
                .filter(inv -> inv.investigationDate() != null)
                .filter(inv ->
                        labDepartmentId.equals(
                                helperUtils.getDepartmentFromInvestigation(
                                        inv.investigationId()
                                )
                        )
                )
                .filter(inv ->
                        !existingLabKeys.contains(
                                new InvestigationKey(
                                        inv.investigationId(),
                                        inv.investigationDate()
                                )
                        )
                )
                .toList();

        // 5. RADIOLOGY ADD LIST
        List<InvestigationData> addRadList = investigations.stream()
                .filter(Objects::nonNull)
                .filter(inv -> inv.flag() == 1)
                .filter(inv -> inv.investigationId() != null)
                .filter(inv -> inv.investigationDate() != null)
                .filter(inv ->
                        radDepartmentId.equals(
                                helperUtils.getDepartmentFromInvestigation(
                                        inv.investigationId()
                                )
                        )
                )
                .filter(inv ->
                        !existingRadKeys.contains(
                                new InvestigationKey(
                                        inv.investigationId(),
                                        inv.investigationDate()
                                )
                        )
                )
                .toList();


        List<DgOrderDt> deleteLabList = labDetails.stream()
                .filter(dt -> dt.getInvestigation() != null)
                .filter(dt -> dt.getAppointmentDate() != null)
                .filter(dt -> investigations.stream()
                        .filter(Objects::nonNull)
                        .filter(inv -> inv.investigationId() != null)
                        .filter(inv -> inv.investigationDate() != null)
                        .anyMatch(inv ->
                                inv.investigationId().equals(
                                        dt.getInvestigation()
                                                .getInvestigationId()
                                )
                                        && inv.investigationDate().equals(
                                        dt.getAppointmentDate()
                                )
                                        && inv.flag() == -1
                        )
                        ||
                        !requestedKeys.contains(
                                new InvestigationKey(
                                        dt.getInvestigation()
                                                .getInvestigationId(),
                                        dt.getAppointmentDate()
                                )
                        )
                )
                .toList();
        // 7. RADIOLOGY DELETE LIST
        List<RadOrderDt> deleteRadList = radDetails.stream()
                .filter(dt -> dt.getInvestigation() != null)
                .filter(dt -> dt.getAppointmentDate() != null)
                .filter(dt -> investigations.stream()
                        .filter(Objects::nonNull)
                        .filter(inv -> inv.investigationId() != null)
                        .filter(inv -> inv.investigationDate() != null)
                        .anyMatch(inv ->
                                inv.investigationId().equals(
                                        dt.getInvestigation()
                                                .getInvestigationId()
                                )
                                        && inv.investigationDate().equals(
                                        dt.getAppointmentDate()
                                )
                                        && inv.flag() == -1
                        )
                        ||
                        !requestedKeys.contains(
                                new InvestigationKey(
                                        dt.getInvestigation()
                                                .getInvestigationId(),
                                        dt.getAppointmentDate()
                                )
                        )
                )
                .toList();

        if (!deleteLabList.isEmpty()) {
            boolean deleteLabHeader = deleteLabList.size() == labDetails.size();
            deleteInvestigation(deleteLabList,deleteLabHeader);
        }
        // 9. DELETE RADIOLOGY
        if (!deleteRadList.isEmpty()) {
            boolean deleteRadHeader = deleteRadList.size() == radDetails.size();
            deleteRadiologyInvestigation(deleteRadList,deleteRadHeader);
        }
        // 10. LAB ORDERED STATUS
        LabOrderTrackingStatus labOrderedStatus = labOrderTrackingStatusRepository.findById(orderedStatusId)
                        .orElseThrow(() -> new SDDException( "status", 500, "Ordered status not found with id: " + orderedStatusId));


        // 11. ADD LAB INVESTIGATIONS
        if (!addLabList.isEmpty()) {
            Map<LocalDate, List<InvestigationData>> labGrouped = addLabList.stream().collect(Collectors.groupingBy(
                                    InvestigationData::investigationDate
                            ));

            for (Map.Entry<LocalDate, List<InvestigationData>> entry: labGrouped.entrySet()) {
                LocalDate appointmentDate = entry.getKey();
                List<InvestigationData> dateInvestigations = entry.getValue();

                // Refresh after deletion
                labHeaders = dgOrderHdRepo.findAllByVisitId(visit);

                // Compare appointmentDate, NOT orderDate.
                DgOrderHd existingLabHeader = labHeaders.stream()
                                .filter(hd -> appointmentDate.equals(hd.getAppointmentDate()))
                                .filter(hd -> AppConstants.STATUS_N.equalsIgnoreCase(hd.getOrderStatus())
                                                ||AppConstants.STATUS_P.equalsIgnoreCase(hd.getOrderStatus()))
                                .findFirst()
                                .orElse(null);

                if (existingLabHeader != null) {
                    createLabOrderDetails(dateInvestigations,existingLabHeader,labOrderedStatus,null);
                } else {
                    // NEW LAB HEADER
                    Map<LocalDate, List<InvestigationData>> grouped = new HashMap<>();
                    grouped.put(appointmentDate, dateInvestigations);

                    String radioAdvised = grouped.values().stream()
                            .flatMap(List::stream)
                            .map(InvestigationData::investigationName)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.joining(", "));

                    processLabInvestigations(grouped,patient,visit,user,labOrderedStatus);



                }
            }
        }

        // 12. ADD RADIOLOGY INVESTIGATIONS
        if (!addRadList.isEmpty()) {
            Map<LocalDate, List<InvestigationData>> radGrouped = addRadList.stream().collect(Collectors.groupingBy(InvestigationData::investigationDate));

            for (Map.Entry<LocalDate, List<InvestigationData>> entry : radGrouped.entrySet()) {
                LocalDate appointmentDate = entry.getKey();
                List<InvestigationData> dateInvestigations = entry.getValue();
                // Refresh after deletion
                radHeaders = radOrderHdRepository.findAllByVisit_Id(visit.getId());
                // Find existing RAD header
                RadOrderHd existingRadHeader =
                        radHeaders.stream()
                                .filter(hd ->
                                        appointmentDate.equals(
                                                hd.getAppointmentDate()
                                        )
                                )
                                .findFirst()
                                .orElse(null);

                if (existingRadHeader != null) {
                    // EXISTING RADIOLOGY HEADER
                    BillingHeader billingHeader = findBillingHeaderFromRadDetails(existingRadHeader);

//                    if (billingHeader == null) {
//                        throw new SDDException(
//                                "billing",
//                                500,
//                                "Billing header not found for RADIOLOGY order: "
//                                        + existingRadHeader.getId()
//                        );
//                    }

                    createRadiologyOrderDetails(dateInvestigations,existingRadHeader,billingHeader);
                    if(billingHeader!=null){
                        updateBillingHeaderById(billingHeader.getId(),true,user);

                    }

                } else {
                    // NEW RADIOLOGY HEADER
                    Map<LocalDate, List<InvestigationData>> grouped = new HashMap<>();
                    grouped.put(appointmentDate,dateInvestigations);
                    processRadiologyInvestigations(grouped,patient,visit,user);
                }
            }
        }
    }

    private BillingHeader findBillingHeaderFromLabDetails(DgOrderHd orderHd) {
        List<DgOrderDt> details = dgOrderDtRepo.findByOrderHd(orderHd);
        if (details == null || details.isEmpty()) {
            return null;
        }

        return details.stream()
                .filter(dt -> dt.getBillingHd() != null)
                .map(DgOrderDt::getBillingHd)
                .findFirst()
                .orElse(null);
    }


    private BillingHeader findBillingHeaderFromRadDetails(RadOrderHd orderHd) {
        List<RadOrderDt> details = radOrderDtRepository.findByRadOrderhd(orderHd);
        if (details == null || details.isEmpty()) {
            return null;
        }

        return details.stream()
                .filter(dt -> dt.getBillingHd() != null)
                .map(RadOrderDt::getBillingHd)
                .findFirst()
                .map(billingHd ->
                        billingHeaderRepository
                                .findById(
                                        Math.toIntExact(
                                                billingHd.getId()
                                        )
                                )
                                .orElse(null)
                )
                .orElse(null);
    }

    private void deleteRadiologyInvestigation(List<RadOrderDt> deleteList, boolean deleteHeader) {
        if (deleteList == null || deleteList.isEmpty()) {
            return;
        }
        for (RadOrderDt dt : deleteList) {
            if (dt.getBillingHd() != null && dt.getInvestigation() != null) {

                BillingHeader billingHeader = billingHeaderRepository.findById(Math.toIntExact(dt.getBillingHd().getId())).orElse(null);
                if (billingHeader != null) {
                    Long investigationId = dt.getInvestigation().getInvestigationId();

                    List<BillingDetail> billingDetails = billingDetailRepository.findByBillingHd(billingHeader);
                    List<BillingDetail> billingDetailsToDelete = billingDetails.stream()
                                    .filter(detail -> detail.getInvestigation() != null)
                                    .filter(detail -> investigationId.equals(detail.getInvestigation().getInvestigationId()))
                                    .toList();

                    if (!billingDetailsToDelete.isEmpty()) {

                        BigDecimal totalAmount =
                                billingDetailsToDelete.stream()
                                        .map(BillingDetail::getBasePrice)
                                        .filter(Objects::nonNull)
                                        .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add
                                        );

                        BigDecimal discountAmount =
                                billingDetailsToDelete.stream()
                                        .map(BillingDetail::getDiscount)
                                        .filter(Objects::nonNull)
                                        .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add
                                        );

                        BigDecimal netAmount =
                                billingDetailsToDelete.stream()
                                        .map(BillingDetail::getNetAmount)
                                        .filter(Objects::nonNull)
                                        .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add
                                        );

                        billingDetailRepository.deleteAll(billingDetailsToDelete);
                        billingDetailRepository.flush();
                        List<BillingDetail> remainingDetails = billingDetailRepository.findByBillingHd(billingHeader);

                        if (remainingDetails.isEmpty()) {
                            billingHeaderRepository.delete(billingHeader);
                        } else {
                            billingHeader.setTotalAmount(billingHeader.getTotalAmount().subtract(totalAmount));
                            billingHeader.setDiscountAmount(billingHeader.getDiscountAmount().subtract(discountAmount));
                            billingHeader.setNetAmount(billingHeader.getNetAmount().subtract(netAmount));
                            billingHeaderRepository.save(billingHeader);
                        }
                    }
                }
            }
            RadOrderHd radOrderHd = dt.getRadOrderhd();
            radOrderDtRepository.delete(dt);
            radOrderDtRepository.flush();

            if (deleteHeader && radOrderHd != null) {
                List<RadOrderDt> remainingDetails = radOrderDtRepository.findByRadOrderhd(radOrderHd);
                if (remainingDetails == null || remainingDetails.isEmpty()) {
                    radOrderHdRepository.delete(radOrderHd);
                }
            }
        }
    }

    private void deleteInvestigation(List<DgOrderDt> deleteList, boolean deleteHeader) {
        if (deleteList == null || deleteList.isEmpty()) {
            return;
        }
        for (DgOrderDt dt : deleteList) {
            if (dt.getBillingHd() != null && dt.getInvestigation() != null) {
                BillingHeader billingHeader = billingHeaderRepository.findById(Math.toIntExact(dt.getBillingHd().getId())).orElse(null);
                if (billingHeader != null) {
                    Long investigationId = dt.getInvestigation().getInvestigationId();
                    List<BillingDetail> billingDetails = billingDetailRepository.findByBillingHd(billingHeader);
                    List<BillingDetail> billingDetailsToDelete =
                            billingDetails.stream()
                                    .filter(detail -> detail.getInvestigation() != null)
                                    .filter(detail ->
                                            investigationId.equals(
                                                    detail.getInvestigation()
                                                            .getInvestigationId()
                                            )
                                    )
                                    .toList();

                    if (!billingDetailsToDelete.isEmpty()) {
                        BigDecimal totalAmount = billingDetailsToDelete.stream()
                                .map(BillingDetail::getBasePrice)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal discountAmount = billingDetailsToDelete.stream()
                                .map(BillingDetail::getDiscount)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal netAmount = billingDetailsToDelete.stream()
                                .map(BillingDetail::getNetAmount)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        // Delete details FIRST
                        billingDetailRepository.deleteAll(billingDetailsToDelete);
                        billingDetailRepository.flush();

                        // Check whether other billing details still exist
                        List<BillingDetail> remainingDetails = billingDetailRepository.findByBillingHd(billingHeader);
                        if (remainingDetails.isEmpty()) {
                            billingHeaderRepository.delete(billingHeader);
                        } else {
                            // Other investigations still exist → update amount
                            billingHeader.setTotalAmount(billingHeader.getTotalAmount().subtract(totalAmount));
                            billingHeader.setDiscountAmount(billingHeader.getDiscountAmount().subtract(discountAmount));
                            billingHeader.setNetAmount(billingHeader.getNetAmount().subtract(netAmount));
                            billingHeaderRepository.save(billingHeader);
                        }
                    }
                }
            }
            // Delete investigation order detail
            if (dt.getOrderHd() != null) {
                dgOrderDtRepo.delete(dt);
            }
            // Delete order header only when requested
            if (deleteHeader && dt.getOrderHd() != null) {
                List<DgOrderDt> remaining = dgOrderDtRepo.findByOrderHd(dt.getOrderHd());
                if (remaining == null || remaining.isEmpty()) {
                    dgOrderHdRepo.delete(dt.getOrderHd());
                }
            }
        }
    }



    private void replacePsychiatryAssessment(RecallOpdPatientDetailRequest request, Patient patient, Visit visit, User user) {
        if (request.getPsychiatricDetailsRequests() != null && !request.getPsychiatricDetailsRequests().isEmpty()) {
            // Get or create OpdPatientDetail (needed for header)
            OpdPatientDetail opdPatientDetail = opdPatientDetailRepository
                    .findByVisit_Id(visit.getId())
                    .orElseThrow(() -> new SDDException("opdPatientDetail", 404,
                            "OPD patient detail not found for visit ID: " + visit.getId()));

            // Save or update using the improved method
            saveOrUpdatePsychiatricAssessment(
                    request.getPsychiatricDetailsRequests(),
                    request.getTopicId(),
                    visit,
                    opdPatientDetail,
                    user
            );

            log.info("Psychiatric assessment updated successfully for visit ID: {}", visit.getId());
        } else {
            // If details are null or empty, delete existing psychiatric assessment
            OpdPsychiatryAssessmentHeader existingHeader = opdPsychiatryAssessmentHeaderRepository
                    .findByVisit_Id(visit.getId())
                    .orElse(null);

            if (existingHeader != null) {
                deleteAllPsychiatricDetails(existingHeader);
                opdPsychiatryAssessmentHeaderRepository.delete(existingHeader);
                log.info("Deleted psychiatric assessment for visit ID: {}", visit.getId());
            }
        }
    }

    private void handleRecallObgDetails(RecallOpdPatientDetailRequest request, Patient patient, Visit visit, User user) {
        try {
            OpdObgDetailsRequest obgRequest = request.getOpdObgDetailsRequest();
            obgRequest.setPatientId(patient.getId());
            obgRequest.setVisitId(visit.getId());
            ApiResponse<String> response = opdObgDetailsService.createOrUpdateObgDetails(
                    request.getVisitId(),
                    obgRequest
            );
            if (response == null || response.getStatus() != HttpStatus.OK.value()) {
                throw new SDDException("obg", 500,
                        response != null ? response.getMessage() : "Failed to update OBG details");
            }
            log.info("OBG details updated successfully for visit ID: {}", visit.getId());
        } catch (Exception e) {
            log.error("Error updating OBG details for visit ID: {}", visit.getId(), e);
            throw new SDDException("obg", 500, "Failed to update OBG details: " + e.getMessage());
        }
    }


    private void mapRecallBasicDetails(OpdPatientDetail opd, RecallOpdPatientDetailRequest request) {
        opd.setHeight(request.getHeight());
        opd.setWeight(request.getWeight());
        opd.setTemperature(request.getTemperature());
        opd.setBpDiastolic(request.getBpDiastolic());
        opd.setBpSystolic(request.getBpSystolic());
        opd.setPulse(request.getPulse());
        opd.setBmi(request.getBmi());
        opd.setRr(request.getRr());
        opd.setSpo2(request.getSpo2());
    }

    private void mapRecallClinicalDetails(OpdPatientDetail opd, RecallOpdPatientDetailRequest request) {
        opd.setPatientSignsSymptoms(request.getPatientSignsSymptoms());
        opd.setClinicalExamination(request.getClinicalExamination());
        opd.setPastMedicalHistory(request.getPastMedicalHistory());
        opd.setFamilyHistory(request.getFamilyHistory());
        opd.setWorkingDiag(request.getWorkingDiagnosis());
        opd.setFinalMedicalAdvice(request.getDoctorRemarks());
        opd.setTreatmentAdvice(request.getTreatmentAdvice());
        if(request.getIcdDiagnosisList() != null && !request.getIcdDiagnosisList().isEmpty()) {
            String joinedNames = request.getIcdDiagnosisList().stream().filter(Objects::nonNull).map(RecallOpdPatientDetailRequest.IcdDiagnosis::getIcdDiagnosisName).filter(Objects::nonNull).collect(Collectors.joining(","));
            opd.setIcdDiag(joinedNames);
        } else {
            opd.setIcdDiag(null);
        }
    }

    private void handleAdmission(OpdPatientDetail opd, OpdPatientDetailCreateRequest request) {
        if (isYes(request.getAdmissionFlag())) {
            if (request.getAdmissionCareLevel() != null) {
                masCareLevelRepository.findById(request.getAdmissionCareLevel()).ifPresent(opd::setAdmissionCareLevel);
            }
            if (request.getAdmissionWardCategory() != null) {
                masWardCategoryRepository.findById(request.getAdmissionWardCategory()).ifPresent(opd::setAdmissionWardCategory);
            }
            if (request.getAdmissionWard() != null) {
                masDepartmentRepository.findById(request.getAdmissionWard()).ifPresent(opd::setAdmissionWard);
            }
            opd.setAdmissionFlag(AppConstants.STATUS_Y.toLowerCase());
            opd.setAdmissionAdvisedDate(request.getAdmissionAdvisedDate());
            opd.setAdmissionRemarks(request.getAdmissionRemarks());
            opd.setAdmissionPriority(request.getAdmissionPriority());
        } else {
            opd.setAdmissionFlag(AppConstants.STATUS_N.toLowerCase());
        }
    }

    private void handleFollowUp(OpdPatientDetail opd, OpdPatientDetailCreateRequest request) {
        if (isYes(request.getFollowUpFlag())) {
            opd.setFollowUpFlag(AppConstants.STATUS_Y.toLowerCase());
            opd.setFollowUpDays(request.getFollowUpDays());
            opd.setFollowUpDate(request.getFollowUpDate());
        } else {
            opd.setFollowUpFlag(AppConstants.STATUS_N.toLowerCase());
        }
    }

    private void handleReferral(OpdPatientDetail opd, OpdPatientDetailCreateRequest request) {
        opd.setReferralFlag(isYes(request.getReferralFlag()) ? AppConstants.STATUS_Y.toLowerCase() : AppConstants.STATUS_N.toLowerCase());
        opd.setReferralRemarks(request.getReferralRemarks());
        opd.setReferralDate(request.getReferralDate());
        opd.setReferTo(request.getReferTo());
        opd.setReferredHospitalName(request.getReferredHospitalName());
    }

    private void handleRecallAdmission(OpdPatientDetail opd, RecallOpdPatientDetailRequest request) {
        if (isYes(request.getAdmissionFlag())) {
            opd.setAdmissionFlag(AppConstants.STATUS_Y.toLowerCase());
            if (request.getAdmissionCareLevel() != null) {
                masCareLevelRepository.findById(request.getAdmissionCareLevel()).ifPresent(opd::setAdmissionCareLevel);
            }
            if (request.getAdmissionWardCategory() != null) {
                masWardCategoryRepository.findById(request.getAdmissionWardCategory()).ifPresent(opd::setAdmissionWardCategory);
            }
            if (request.getAdmissionWard() != null) {
                masDepartmentRepository.findById(request.getAdmissionWard()).ifPresent(opd::setAdmissionWard);
            }
            opd.setAdmissionAdvisedDate(request.getAdmissionAdvisedDate());
            opd.setAdmissionRemarks(request.getAdmissionRemarks());
            opd.setAdmissionPriority(request.getAdmissionPriority());
        } else {
            opd.setAdmissionFlag(AppConstants.STATUS_N.toLowerCase());
            opd.setAdmissionAdvisedDate(null);
            opd.setAdmissionRemarks(null);
            opd.setAdmissionPriority(null);
            opd.setAdmissionCareLevel(null);
            opd.setAdmissionWardCategory(null);
            opd.setAdmissionWard(null);
        }
    }

    private void handleRecallFollowUp(OpdPatientDetail opd, RecallOpdPatientDetailRequest request) {
        if (isYes(request.getFollowUpFlag())) {
            opd.setFollowUpFlag(AppConstants.STATUS_Y.toLowerCase());
            opd.setFollowUpDays(request.getFollowUpDays());
            opd.setFollowUpDate(request.getFollowUpDate());
        } else {
            opd.setFollowUpFlag(AppConstants.STATUS_N.toLowerCase());

        }
    }

    private void handleRecallReferral(OpdPatientDetail opd, RecallOpdPatientDetailRequest request) {
        boolean isReferral = isYes(request.getReferralFlag());
        opd.setReferralFlag(isReferral ? AppConstants.STATUS_Y.toLowerCase() : AppConstants.STATUS_N.toLowerCase());
        if (isYes(request.getReferralFlag())) {
            opd.setReferralFlag(AppConstants.STATUS_Y.toLowerCase());
            opd.setReferralRemarks(request.getReferralRemarks());
            opd.setReferralDate(request.getReferralDate());
            opd.setReferTo(request.getReferTo());
            opd.setReferredHospitalName(request.getReferredHospitalName());
        } else {
            opd.setReferralFlag(AppConstants.STATUS_N.toLowerCase());
            opd.setReferralRemarks(null);
            opd.setReferralDate(null);
            opd.setReferTo(null);
            opd.setReferredHospitalName(null);
        }
    }

    private void handleRecallPregnancyDetails(
            OpdPatientDetail opd,
            RecallOpdPatientDetailRequest.PregnancyDetails pregnancyDetails,
            User user
    ) {
        if (opd == null || pregnancyDetails == null || opd.getVisit() == null || opd.getPatient() == null) {
            return;
        }

        Long visitId = opd.getVisit().getId();
        OpdPatientPregnancyDetails pregnancyEntity = opdPatientPregnancyDetailsRepository
                .findByVisit_Id(visitId)
                .orElseGet(OpdPatientPregnancyDetails::new);

        pregnancyEntity.setVisit(opd.getVisit());
        pregnancyEntity.setPatient(opd.getPatient());
        pregnancyEntity.setIsPregnant(pregnancyDetails.getIsPregnant());
        pregnancyEntity.setLmpDate(pregnancyDetails.getLmpDate());
        pregnancyEntity.setEdd(pregnancyDetails.getEdd());
        pregnancyEntity.setCurrentEdd(pregnancyDetails.getCurrentEdd());
        pregnancyEntity.setGestationPeriod(pregnancyDetails.getGestationPeriod());
        pregnancyEntity.setLastChgDate(Instant.now());
        pregnancyEntity.setLastChgBy(user != null ? user.getFullName() : null);

        opdPatientPregnancyDetailsRepository.save(pregnancyEntity);
    }

    private boolean isYes(String flag) {
        return flag != null && flag.equalsIgnoreCase("y");
    }

    /**
     * Process LAB investigations for OPD patient
     * Creates DgOrderHd and DgOrderDt records
     */
    private void processLabInvestigations(Map<LocalDate, List<InvestigationData>> groupedByDate, Patient patient, Visit visit, User currentUser, LabOrderTrackingStatus labOrderedStatus) {
        log.info("Starting LAB investigation processing for patient ID: {}", patient.getId());

        for (Map.Entry<LocalDate, List<InvestigationData>> entry : groupedByDate.entrySet()) {
            LocalDate appointmentDate = entry.getKey();
            List<InvestigationData> investigations = entry.getValue();

            log.debug("Processing {} LAB investigations for date: {}", investigations.size(), appointmentDate);
            // Create lab order header
            DgOrderHd dgOrderHd = new DgOrderHd();
            dgOrderHd.setAppointmentDate(appointmentDate);
            dgOrderHd.setOrderDate(LocalDate.now());
            dgOrderHd.setOrderTime(LocalDateTime.now());
            dgOrderHd.setOrderNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.LAB_NO, currentUser.getHospital().getId()));
            dgOrderHd.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
            dgOrderHd.setCollectionStatus(AppConstants.STATUS_N.toLowerCase());
            dgOrderHd.setPaymentStatus(AppConstants.PAYMENT_PAID.equalsIgnoreCase(patient.getPatientHospital().getLabBilling()) ? AppConstants.PAYMENT_NOT_PAID.toLowerCase() : AppConstants.PAYMENT_PAID.toLowerCase());
            dgOrderHd.setSource("OPD PATIENT");
            dgOrderHd.setDiscountId(1);
            dgOrderHd.setPatientId(patient);
            dgOrderHd.setDepartmentId(authUtil.getCurrentDepartmentId());
            dgOrderHd.setHospitalId(currentUser.getHospital().getId());
            dgOrderHd.setVisitId(visit);
            dgOrderHd.setCreatedBy(currentUser.getFullName());
            dgOrderHd.setLastChgBy(currentUser.getFullName());
            dgOrderHd.setCreatedOn(LocalDate.now());
            dgOrderHd.setLastChgDate(LocalDate.now());
            dgOrderHd.setLastChgTime(LocalTime.now().toString());
            DgOrderHd savedOrderHd = dgOrderHdRepo.save(dgOrderHd);

            MasServiceCategory masServiceCategory= masServiceCategoryRepository.findByServiceCateCode(serviceCategoryLab);
//            Lab Billing N -> means free then create OrderHd and OrderDt and billing Should be null
//                        Y -> means we dont need to create OrderHd and orderDt and Not the billing
//             IN both case we dont need to generate the billing
//            --------------------------------------------------------
            BillingHeader billingHeader = null;
//            if(AppConstants.STATUS_Y.equalsIgnoreCase(patient.getPatientHospital().getLabBilling())){
//                BigDecimal[] amounts = calculateBillingHdAmount(investigations, masServiceCategory);
//                billingHeader = billingService.saveBillingHeader(
//                        savedOrderHd, visit, currentUser,
//                        amounts[0], amounts[1], amounts[2],
//                        serviceCategoryLab, false
//                );
//                if (billingHeader == null) {
//                    throw new SDDException("billing", 500, "Failed to create billing");
//                }
//                visit.setBillingHd(billingHeader);
//            }
            visitRepository.save(visit);
            log.info("LAB Order Header saved - Order ID: {}", savedOrderHd.getId());
            createLabOrderDetails(investigations,savedOrderHd, labOrderedStatus, billingHeader);
        }
        log.info("LAB investigations processing completed");
    }

    //creating Lab order details
    private void createLabOrderDetails(List<InvestigationData> investigations, DgOrderHd savedOrderHd,
                                       LabOrderTrackingStatus labOrderedStatus, BillingHeader billingHeader){
        User currentUser = getCurrentUser();
        for (InvestigationData invObj : investigations) {
            if (invObj == null || invObj.investigationId() == null) {
                log.warn("Skipping null investigation object");
                continue;
            }
            DgMasInvestigation invEntity = dgMasInvestigationRepository.findById(invObj.investigationId()).orElseThrow(() -> new SDDException("investigation", 404, "Investigation not found with ID: " + invObj.investigationId()));
            BigDecimal investigationPrice = helperUtils.getInvestigationPrice(invEntity);
            BigDecimal discountAmount = BigDecimal.ZERO;

            if (invEntity.getMainChargeCodeId() == null || invEntity.getSubChargeCodeId() == null) {
                throw new SDDException("chargeCode", 400, "Charge codes not configured for investigation ID: " + invObj.investigationId());
            }
            DgOrderDt dgOrderDt = new DgOrderDt();
            dgOrderDt.setInvestigation(invEntity);
            dgOrderDt.setOrderHd(savedOrderHd);
            dgOrderDt.setAppointmentDate(invObj.investigationDate());
            dgOrderDt.setOrderQty(1);
            dgOrderDt.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
            dgOrderDt.setBillingStatus(savedOrderHd.getPaymentStatus());
            dgOrderDt.setCreatedBy(currentUser.getFullName());
            dgOrderDt.setLastChgBy(currentUser.getFullName());
            dgOrderDt.setCreatedOn(LocalDateTime.now());
            dgOrderDt.setLastChgDate(LocalDate.now());
            dgOrderDt.setMainChargeCodeId(invEntity.getMainChargeCodeId().getChargecodeId());
            dgOrderDt.setSubChargeCodeId(invEntity.getSubChargeCodeId().getSubId());
            dgOrderDt.setOrderTrackingStatus(labOrderedStatus);
            dgOrderDt.setLastChgTime(LocalTime.now().toString());
            DgOrderDt savedOrderDt = dgOrderDtRepo.save(dgOrderDt);
            if(billingHeader!=null){
                savedOrderDt.setBillingHd(billingHeader);
            }
            dgOrderDtRepo.save(dgOrderDt);

            if (billingHeader != null) {
                billingService.saveBillingDetail(billingHeader, savedOrderDt, investigationPrice, discountAmount, serviceCategoryLab, false);
            }

            log.debug("LAB Order Detail saved - Detail ID: {}", savedOrderDt.getId());
        }
    }

    public void updateBillingHeaderById(Long billingHeaderId, boolean isRadiology, User currentUser) {
        if (billingHeaderId == null) {
            return;
        }

        try {
            // Find the billing header
            BillingHeader billingHeader = billingHeaderRepository.findById(Math.toIntExact(billingHeaderId))
                    .orElseThrow(() -> new SDDException("billing", 404, "Billing header not found with ID: " + billingHeaderId));

            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalDiscount = BigDecimal.ZERO;
            BigDecimal totalTax = BigDecimal.ZERO;
            MasServiceCategory serviceCategory = billingHeader.getServiceCategory();

            if (isRadiology) {
                // Update from radiology details
                RadOrderHd radOrderHd = billingHeader.getRadOrderHd();
                if (radOrderHd == null) {
                    throw new SDDException("billing", 500, "No radiology order found for this billing header");
                }

                List<RadOrderDt> radDetails = radOrderDtRepository.findByRadOrderhd(radOrderHd);
                for (RadOrderDt detail : radDetails) {
                    if (detail.getInvestigation() != null) {
                        BigDecimal price = helperUtils.getInvestigationPrice(detail.getInvestigation());
                        totalAmount = totalAmount.add(price);
                    }
                }

                if (serviceCategory != null && serviceCategory.getGstApplicable()) {
                    totalTax = totalAmount.multiply(BigDecimal.valueOf(serviceCategory.getGstPercent()))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }

            } else {
                // Update from lab details
                DgOrderHd dgOrderHd = billingHeader.getHdorder();
                if (dgOrderHd == null) {
                    throw new SDDException("billing", 500, "No lab order found for this billing header");
                }

                List<DgOrderDt> labDetails = dgOrderDtRepo.findByOrderHd(dgOrderHd);
                for (DgOrderDt detail : labDetails) {
                    if (detail.getInvestigation() != null) {
                        BigDecimal price = helperUtils.getInvestigationPrice(detail.getInvestigation());
                        totalAmount = totalAmount.add(price);
                    }
                }

                if (serviceCategory != null && serviceCategory.getGstApplicable()) {
                    totalTax = totalAmount.multiply(BigDecimal.valueOf(serviceCategory.getGstPercent()))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }
            }

            // Update billing header
            billingHeader.setTotalAmount(totalAmount);
            billingHeader.setDiscountAmount(totalDiscount);
            BigDecimal netAmount = totalAmount.subtract(totalDiscount).add(totalTax);
            billingHeader.setNetAmount(netAmount);
            billingHeader.setTaxTotal(totalTax);
            billingHeader.setUpdatedDt(Instant.now());
            billingHeader.setUpdatedAt(OffsetDateTime.now());

            billingHeaderRepository.save(billingHeader);

            log.info("Updated billing header ID: {} with total: {}, tax: {}",
                    billingHeaderId, totalAmount, totalTax);

        } catch (Exception e) {
            log.error("Error updating billing header by ID: {}", e.getMessage(), e);
            throw new SDDException("billing", 500, "Failed to update billing header: " + e.getMessage());
        }
    }

    //dont remove this method as it is used in the processLabInvestigations method to calculate the billing header amount
    private BigDecimal[] calculateBillingHdAmount(List<InvestigationData> investigations, MasServiceCategory serviceCategoryLab) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (InvestigationData inv : investigations) {
            if (inv.investigationId() != null) {
                DgMasInvestigation invEntity = dgMasInvestigationRepository.findById(inv.investigationId()).orElse(null);
                if (invEntity != null) {
                    BigDecimal price = helperUtils.getInvestigationPrice(invEntity);
                    totalAmount = totalAmount.add(price);

                    if (serviceCategoryLab.getGstApplicable()) {
                        totalTax = totalTax.add(price.multiply(BigDecimal.valueOf(serviceCategoryLab.getGstPercent())).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                    }
                }
            }
        }

        return new BigDecimal[]{totalAmount, totalDiscount, totalTax};
    }

    /**
     * Process RADIOLOGY investigations for OPD patient
     * Creates RadOrderHd and RadOrderDt records with billing
     */
    private void processRadiologyInvestigations(Map<LocalDate, List<InvestigationData>> groupedByDate, Patient patient, Visit visit, User currentUser) {
        log.info("Starting RADIOLOGY investigation processing for patient ID: {}", patient.getId());
        MasServiceCategory radiologyServiceCategory = masServiceCategoryRepository.findByServiceCateCode("SC004");
        if (radiologyServiceCategory == null) {
            log.error("Radiology service category (SC004) not found");
            throw new SDDException("serviceCategory", 400, "Radiology service category not configured");
        }

        for (Map.Entry<LocalDate, List<InvestigationData>> entry : groupedByDate.entrySet()) {
            LocalDate appointmentDate = entry.getKey();
            List<InvestigationData> investigations = entry.getValue();

            log.debug("Processing {} RADIOLOGY investigations for date: {}", investigations.size(), appointmentDate);

            // Create radiology order header
            RadOrderHd radOrderHd = new RadOrderHd();
            radOrderHd.setAppointmentDate(appointmentDate);
            radOrderHd.setPaymentStatus(AppConstants.STATUS_Y.equalsIgnoreCase(patient.getPatientHospital().getRadioBilling()) ? AppConstants.STATUS_N.toLowerCase() : AppConstants.STATUS_Y.toLowerCase());
            radOrderHd.setOrderDate(LocalDate.now());
            radOrderHd.setOrderTime(LocalDateTime.now());
            radOrderHd.setPatient(patient);
            radOrderHd.setVisit(visit);
            radOrderHd.setDepartment(visit.getDepartment());
            radOrderHd.setHospital(visit.getHospital());
            radOrderHd.setLastChgBy(currentUser.getFirstName() + " " + currentUser.getLastName());
            radOrderHd.setLastChgDate(LocalDateTime.now());

            RadOrderHd savedRadOrderHd = radOrderHdRepository.save(radOrderHd);
            log.info("RADIOLOGY Order Header saved - Order ID: {}", savedRadOrderHd.getId());
            if (savedRadOrderHd == null) {
                throw new SDDException("RadOrderHeader",500,"Failed to create order header");
            }
            BillingHeader billingHeader = null;
//            if(AppConstants.STATUS_Y.equalsIgnoreCase(patient.getPatientHospital().getRadioBilling())) {
//                MasServiceCategory masServiceCategory = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryRad);
//                BigDecimal[] amounts = calculateBillingHdAmount(investigations, masServiceCategory);
//                billingHeader = billingService.saveBillingHeader(
//                        savedRadOrderHd, visit, currentUser,
//                        amounts[0], amounts[1], amounts[2],
//                        serviceCategoryRad, true
//                );
//                if (billingHeader == null) {
//                    throw new SDDException("billing", 500, "Failed to create billing");
//                }
//                visit.setBillingHd(billingHeader);
//            }
            visitRepository.save(visit);

            // Create radiology order and billing details
            createRadiologyOrderDetails(investigations, savedRadOrderHd, billingHeader);
        }
        visitRepository.save(visit);
        log.info("RADIOLOGY investigations processing completed");
    }

    private void createRadiologyOrderDetails(List<InvestigationData> investigations, RadOrderHd savedRadOrderHd,BillingHeader billingHeader) {
        for (InvestigationData invObj : investigations) {
            if (invObj == null || invObj.investigationId() == null) {
                log.warn("Skipping null radiology investigation object");
                continue;
            }

            DgMasInvestigation invEntity = dgMasInvestigationRepository.findById(invObj.investigationId()).orElseThrow(() -> new SDDException("investigation", 404, "Investigation not found with ID: " + invObj.investigationId()));
            BigDecimal investigationPrice = helperUtils.getInvestigationPrice(invEntity);
            BigDecimal discountAmount = BigDecimal.ZERO;
            User currentUser = getCurrentUser();

            // Create radiology order detail
            RadOrderDt radOrderDt = new RadOrderDt();
            radOrderDt.setRadOrderhd(savedRadOrderHd);
            radOrderDt.setInvestigation(invEntity);
            radOrderDt.setOrderAccessionNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.RADIOLOGY_NO, currentUser.getHospital().getId()));
            radOrderDt.setSubChargecode(invEntity.getSubChargeCodeId());
            radOrderDt.setAppointmentDate(invObj.investigationDate());
            radOrderDt.setLastChgBy(currentUser.getFirstName() + " " + currentUser.getLastName());
            radOrderDt.setCreatedby(currentUser.getFirstName() + " " + currentUser.getLastName());
            radOrderDt.setBillingStatus(savedRadOrderHd.getPaymentStatus());
            radOrderDt.setBillingHd(billingHeader);
            radOrderDt.setStudyStatus(AppConstants.STATUS_N.toLowerCase());
            radOrderDt.setReportStatus(AppConstants.STATUS_N.toLowerCase());
            radOrderDt.setHl7MwlStatus(AppConstants.STATUS_N.toLowerCase());
            radOrderDt.setPacsCompletionStatus(AppConstants.STATUS_N.toLowerCase());
            radOrderDt.setOrderStatus(AppConstants.STATUS_Y.toLowerCase());

            RadOrderDt savedRadOrderDt = radOrderDtRepository.save(radOrderDt);
            log.debug("RADIOLOGY Order Detail saved - Detail ID: {}", savedRadOrderDt.getId());
            if (billingHeader != null) {
                billingService.saveBillingDetail(billingHeader, savedRadOrderDt, investigationPrice, discountAmount, serviceCategoryRad, true);
            }
        }
    }



    @Override
    public ApiResponse<List<OpdPatientDetailsWaitingresponce>> getActiveVisits() {

        List<Visit> activeVisits = visitRepository.findByVisitStatusAndBillingStatus("n", "y");
        List<OpdPatientDetailsWaitingresponce> responseList = new ArrayList<>();

        for (Visit v : activeVisits) {

            OpdPatientDetailsWaitingresponce res = new OpdPatientDetailsWaitingresponce();

            // Patient
            if (v.getPatient() != null) {
                res.setPatientId(v.getPatient().getId());
                res.setEmployeeNo(v.getPatient().getUhidNo());
                res.setMobileNo(v.getPatient().getPatientMobileNumber());
                res.setDob(v.getPatient().getPatientDob());
                res.setAge(v.getPatient().getPatientAge());

                String fullName = buildFullName(v.getPatient().getPatientFn(), v.getPatient().getPatientMn(), v.getPatient().getPatientLn());
                res.setPatientName(fullName);

                res.setGender(v.getPatient().getPatientGender() != null ? v.getPatient().getPatientGender().getGenderName() : null);

                res.setRelation(v.getPatient().getPatientRelation() != null ? v.getPatient().getPatientRelation().getRelationName() : null);
            }

            // Visit
            res.setVisitId(v.getId());
            res.setTokenNo(v.getTokenNo() != null ? String.valueOf(v.getTokenNo()) : null);

            // Department
            if (v.getDepartment() != null) {
                res.setDeptId(v.getDepartment().getId());
                res.setDeptName(v.getDepartment().getDepartmentName());
            }

            // Doctor
            if (v.getDoctor() != null) {
                res.setDocterId(v.getDoctor().getUserId());

                String docFullName = buildFullName(v.getDoctor().getFirstName(), v.getDoctor().getMiddleName(), v.getDoctor().getLastName());
                res.setDocterName(docFullName);
            } else {
                res.setDocterId(null);
                res.setDocterName(null);
            }

            // Hospital
            if (v.getHospital() != null) {
                res.setHospitalId(v.getHospital().getId());
            }

            // Session
            if (v.getSession() != null) {
                res.setSessionId(v.getSession().getId());
                res.setSessionName(v.getSession().getSessionName());
            }

            responseList.add(res);
        }

        return ResponseUtils.createSuccessResponse(responseList, new TypeReference<List<OpdPatientDetailsWaitingresponce>>() {
        });
    }

    @Override
    public ApiResponse<List<OpdPatientDetailsWaitingresponce>> getActiveVisitsWithFilters(ActiveVisitSearchRequest req) {
        try {
            LocalDate visitDate = req.getDate() != null ? req.getDate().atZone(ZoneId.systemDefault()).toLocalDate() : LocalDate.now();

            List<OpdPatientDetailsWaitingProjection> projections = visitRepository.findActiveVisitsWithFilters(req.getDoctorId(), req.getSessionId(), req.getEmployeeNo(), req.getPatientName(), visitDate, AppConstants.STATUS_N.toLowerCase(), AppConstants.STATUS_Y.toLowerCase());

            List<OpdPatientDetailsWaitingresponce> responseList = projections.stream().map(p -> {
                OpdPatientDetailsWaitingresponce res = new OpdPatientDetailsWaitingresponce();
                res.setPatientId(p.getPatientId());
                res.setEmployeeNo(p.getEmployeeNo());
                res.setMobileNo(p.getMobileNo());
                res.setDob(p.getDob());
                res.setAge(p.getAge());
                res.setDisplayPatientStatus(p.getDisplayPatientStatus());
                res.setVisitDate(p.getVisitDate());
                res.setPatientName(p.getPatientName());
                res.setGender(p.getGender());
                res.setRelation(p.getRelation());
                res.setVisitId(p.getVisitId());
                res.setTokenNo(p.getTokenNo());
                res.setDeptId(p.getDeptId());
                res.setDeptName(p.getDeptName());
                res.setDocterId(p.getDocterId());
                res.setDocterName(p.getDocterName());
                res.setHospitalId(p.getHospitalId());
                res.setSessionId(p.getSessionId());
                res.setSessionName(p.getSessionName());
                return res;
            }).collect(Collectors.toList());

            // Sorting
            responseList.sort(Comparator.comparingInt(res -> {
                try {
                    return res.getTokenNo() != null ? Integer.parseInt(res.getTokenNo()) : Integer.MAX_VALUE;
                } catch (Exception e) {
                    return Integer.MAX_VALUE;
                }
            }));
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
            });

        } catch (Exception ex) {
            log.error("Error while fetching active visits. req={}", req, ex);
            return ResponseUtils.createFailureResponse(Collections.emptyList(), "Failed to fetch active visits", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OpdPatientRecallResponce> getRecallVisit(Long visitId) {
        try {

            OpdPatientRecallResponce response = new OpdPatientRecallResponce();
            RecallPatientProjection basicData = visitRepository.getRecallBasicDetails(visitId);
            mapBasicDetails(response, basicData);
            OpdPatientDetail opdPatientObj = opdPatientDetailRepository.findTopByVisit_IdOrderByOpdPatientDetailsIdDesc(visitId);
            List<DgOrderHd> dgOrderHdList = safeList(dgOrderHdRepo.findAllByVisitId_Id(visitId));
            List<RadOrderHd> orderHdList = safeList(radOrderHdRepository.findAllByVisit_Id(visitId));
            List<PatientPrescriptionHd> prescriptionHdList =
                    safeList(
                            patientPrescriptionHdRepository
                                    .findAllByVisit_Id(visitId)
                    );

            List<PatientPrescriptionDt> prescDtList = new ArrayList<>();

            for (PatientPrescriptionHd hd : prescriptionHdList) {

                List<PatientPrescriptionDt> details =
                        patientPrescriptionDtRepository
                                .findByPrescriptionHdId(
                                        hd.getPrescriptionHdId()
                                );

                if (details != null) {
                    prescDtList.addAll(details);
                }
            }            // ================= ITEM IDS =================
            List<Long> itemIds = prescDtList.stream().filter(Objects::nonNull).map(PatientPrescriptionDt::getItemId).filter(Objects::nonNull).distinct().toList();
            // ================= BULK FETCH ITEMS =================
            Map<Long, MasStoreItem> itemMap = itemIds.isEmpty() ? Collections.emptyMap() : masStoreItemRepository.findAllByItemIds(itemIds).stream().collect(Collectors.toMap(MasStoreItem::getItemId, Function.identity()));
            // ================= OPD DETAILS =================
            if (opdPatientObj != null) {

                mapOpdDetails(response, opdPatientObj);
                response.setTreatmentAdvice(opdPatientObj.getTreatmentAdvice());
            }

            if (basicData != null) {
                response.setPregnancyDetails(
                        mapPregnancyDetails(
                                opdPatientPregnancyDetailsRepository.findByVisit_Id(visitId).orElse(null)
                        )
                );
            } else {
                response.setPregnancyDetails(null);
            }

            // ================= DG / RADIO =================
            response.setLabOrderHds(buildDgOrderHdList(dgOrderHdList));

            response.setRadOrderHds(buildRadOrderHdList(orderHdList));

            // ================= PRESCRIPTION HD =================
            List<OpdPatientRecallResponce.NewDPatientPrescriptionHd> hdList =
                    new ArrayList<>();

            for (PatientPrescriptionHd prescriptionHd : prescriptionHdList) {

                if (prescriptionHd == null) {
                    continue;
                }

                OpdPatientRecallResponce.NewDPatientPrescriptionHd hd =
                        new OpdPatientRecallResponce.NewDPatientPrescriptionHd();

                hd.setPrescriptionHdId(
                        prescriptionHd.getPrescriptionHdId()
                );

                hd.setStatus(
                        prescriptionHd.getStatus()
                );

                hd.setPrescriptionDate(
                        prescriptionHd.getPrescriptionDate()
                );

                hdList.add(hd);
            }

            response.setPatientPrescriptionHds(hdList);
            // ================= PRESCRIPTION DT =================
            List<OpdPatientRecallResponce.NewDPatientPrescriptionDt> newDtList = new ArrayList<>();

            Long hospitalId = authUtil.getCurrentUser() != null && authUtil.getCurrentUser().getHospital() != null ? authUtil.getCurrentUser().getHospital().getId() : null;

            for (PatientPrescriptionDt dt : prescDtList) {

                if (dt == null) continue;

                OpdPatientRecallResponce.NewDPatientPrescriptionDt newDt = new OpdPatientRecallResponce.NewDPatientPrescriptionDt();
                newDt.setPrescriptionDtId(dt.getPrescriptionDtId());
                newDt.setPrescriptionHdId(dt.getPrescriptionHdId());
                newDt.setStatus(dt.getStatus());
                newDt.setDosage(dt.getDosage());
                newDt.setFrequency(dt.getFrequency());
                newDt.setDays(dt.getDays());
                newDt.setTotal(dt.getTotal());
                newDt.setInstraction(dt.getInstruction());
                newDt.setItemId(dt.getItemId());
                newDt.setFrequencyId(dt.getFrequency());

                // ================= ITEM DETAILS =================
                MasStoreItem item = itemMap.get(dt.getItemId());
                if (item != null) {
                    newDt.setItemName(item.getNomenclature());
                    newDt.setAdispQty(item.getAdispQty());

                    if (item.getDispUnit() != null) {
                        newDt.setDispUnit(item.getDispUnit().getUnitName());
                        newDt.setDepUnit(item.getDispUnit().getUnitName());
                    }
                    if (item.getItemClassId() != null) {
                        newDt.setItemClassId(item.getItemClassId().getItemClassId());
                    }
                }

                // ================= STOCK =================
                Long stocks = 0L;
                if (hospitalId != null && dt.getItemId() != null) {
                    Long stockVal = stockFound.getAvailableStocks(hospitalId, deptIdStore, dt.getItemId(), hospDefinedDays);

                    stocks = stockVal != null ? stockVal : 0L;
                }

                newDt.setStocks(stocks);
                newDtList.add(newDt);
            }

            response.setPatientPrescriptionDts(newDtList);

            // ================= FOLLOW UP =================
            if (opdPatientObj != null) {
                response.setFollowUpFlag(opdPatientObj.getFollowUpFlag());
                if (isYes(opdPatientObj.getFollowUpFlag())) {
                    response.setFollowUpDays(opdPatientObj.getFollowUpDays());
                    response.setFollowUpDate(opdPatientObj.getFollowUpDate());
                }

                // ================= REFERRAL =================
                response.setReferralFlag(opdPatientObj.getReferralFlag());

                if (isYes(opdPatientObj.getReferralFlag())) {
                    response.setReferralRemarks(opdPatientObj.getReferralRemarks());
                    response.setReferralDate(opdPatientObj.getReferralDate());
                }

                // ================= ADMISSION =================
                response.setAdmissionFlag(opdPatientObj.getAdmissionFlag());
                if (isYes(opdPatientObj.getAdmissionFlag())) {

                    response.setAdmissionRemarks(opdPatientObj.getAdmissionRemarks());
                    response.setAdmissionAdvisedDate(opdPatientObj.getAdmissionAdvisedDate());
                    response.setAdmissionPriority(opdPatientObj.getAdmissionPriority());

                    if (opdPatientObj.getAdmissionCareLevel() != null) {
                        response.setAdmissionCareLevel(opdPatientObj.getAdmissionCareLevel().getCareId());
                        response.setAdmissionCareLevelName(opdPatientObj.getAdmissionCareLevel().getCareLevelName());
                    }

                    if (opdPatientObj.getAdmissionWardCategory() != null) {
                        response.setAdmissionWardCategory(opdPatientObj.getAdmissionWardCategory().getId());
                        response.setAdmissionWardCategoryName(opdPatientObj.getAdmissionWardCategory().getCategoryName());
                    }

                    if (opdPatientObj.getAdmissionWard() != null) {
                        response.setAdmissionWard(opdPatientObj.getAdmissionWard().getId());
                        response.setAdmissionWardName(opdPatientObj.getAdmissionWard().getDepartmentName());
                    }
                }
            }
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
            });
        } catch (Exception ex) {
            log.error("Error while fetching data : ", ex);
            return ResponseUtils.createFailureResponse(null, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void mapBasicDetails(OpdPatientRecallResponce response, RecallPatientProjection data) {
        if (data == null) {
            return;
        }
        response.setPatientId(data.getPatientId());
        response.setVisitId(data.getVisitId());
        response.setPatientName(buildFullName(data.getPatientFn(), data.getPatientMn(), data.getPatientLn()));
        response.setMobileNo(data.getPatientMobileNumber());
        response.setGender(data.getGenderName());
        response.setRelation(data.getRelationName());
        response.setDob(data.getPatientDob());
        response.setAge(data.getPatientAge());
        response.setDeptId(data.getDepartmentId());
        response.setDeptName(data.getDepartmentName());
        response.setDocterId(data.getDoctorId());
        response.setDocterName(buildFullName(data.getDoctorFirstName(), data.getDoctorMiddleName(), data.getDoctorLastName()));
        response.setHospitalId(data.getHospitalId());
    }


    private <T> List<T> safeList(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }

    private void mapOpdDetails(OpdPatientRecallResponce response, OpdPatientDetail opd) {

        if (response == null || opd == null) {
            return;
        }

        // ---------------- BASIC OPD ----------------
        response.setOpdPatientId(opd.getOpdPatientDetailsId());
        response.setOpdDate(opd.getOpdDate());
        response.setPastMedicalHistory(opd.getPastMedicalHistory());
        response.setFamilyHistory(opd.getFamilyHistory());
        response.setPatientSignsSymptoms(opd.getPatientSignsSymptoms());
        response.setClinicalExamination(opd.getClinicalExamination());
        response.setHeight(opd.getHeight());
        response.setIdealWeight(opd.getIdealWeight());
        response.setWeight(opd.getWeight());
        response.setPulse(opd.getPulse());
        response.setTemperature(opd.getTemperature());
        response.setRr(opd.getRr());
        response.setBmi(opd.getBmi());
        response.setSpo2(opd.getSpo2());
        response.setBpSystolic(opd.getBpSystolic());
        response.setBpDiastolic(opd.getBpDiastolic());
        response.setMlcFlag(opd.getMlcFlag());
        response.setWorkingDiag(opd.getWorkingDiag());
        response.setReferTo(opd.getReferTo());
        response.setReferredHospitalName(opd.getReferredHospitalName());

        // ---------------- FINAL MEDICAL ADVICE ----------------
        response.setDoctorRemarks(opd.getFinalMedicalAdvice());

        // ---------------- ADMISSION ----------------
        response.setAdmissionFlag(opd.getAdmissionFlag());

        if (isYes(opd.getAdmissionFlag())) {

            response.setAdmissionAdvisedDate(opd.getAdmissionAdvisedDate());
            response.setAdmissionRemarks(opd.getAdmissionRemarks());
            response.setAdmissionPriority(opd.getAdmissionPriority());

            if (opd.getAdmissionCareLevel() != null) {
                response.setAdmissionCareLevel(opd.getAdmissionCareLevel().getCareId());
                response.setAdmissionCareLevelName(opd.getAdmissionCareLevel().getCareLevelName());
            }

            if (opd.getAdmissionWardCategory() != null) {
                response.setAdmissionWardCategory(opd.getAdmissionWardCategory().getId());
                response.setAdmissionWardCategoryName(opd.getAdmissionWardCategory().getCategoryName());
            }

            if (opd.getAdmissionWard() != null) {
                response.setAdmissionWard(opd.getAdmissionWard().getId());
                response.setAdmissionWardName(opd.getAdmissionWard().getDepartmentName());
            }
        }

        // ---------------- FOLLOW UP ----------------
        response.setFollowUpFlag(opd.getFollowUpFlag());

        if (isYes(opd.getFollowUpFlag())) {
            response.setFollowUpDate(opd.getFollowUpDate());
            response.setFollowUpDays(opd.getFollowUpDays());
        }

        // ---------------- REFERRAL ----------------
        response.setReferralFlag(opd.getReferralFlag());

        // ---------------- ICD DIAGNOSIS ----------------
        if (opd.getIcdDiag() != null && !opd.getIcdDiag().isEmpty() && opd.getVisit() != null) {

            List<DischargeIcdCode> icdList = safeList(dischargeIcdCodeRepository.findByOpdPatientDetailsIdAndVisitId(opd.getOpdPatientDetailsId(), opd.getVisit().getId()));

            List<OpdPatientRecallResponce.IcdDiagnosis> newList = new ArrayList<>();

            for (DischargeIcdCode dis : icdList) {

                if (dis == null) continue;

                OpdPatientRecallResponce.IcdDiagnosis d = new OpdPatientRecallResponce.IcdDiagnosis();

                d.setId(dis.getDischargeIcdCodeId());
                d.setIcdId(dis.getIcdId());

                if (dis.getIcdId() != null) {
                    masIcdRepository.findById(dis.getIcdId()).ifPresent(masIcd -> d.setIcdDiagName(masIcd.getIcdCode() + " - " + masIcd.getIcdName()));
                }

                newList.add(d);
            }

            response.setIcdDiag(newList);
        }

        // ---------------- FLAGS ----------------
        response.setLabFlag(opd.getLabFlag());
        response.setRadioFlag(opd.getRadioFlag());
    }

    private OpdPatientRecallResponce.PregnancyDetails mapPregnancyDetails(
            OpdPatientPregnancyDetails pregnancyDetails
    ) {
        if (pregnancyDetails == null) {
            return null;
        }

        OpdPatientRecallResponce.PregnancyDetails response =
                new OpdPatientRecallResponce.PregnancyDetails();
        response.setIsPregnant(pregnancyDetails.getIsPregnant());
        response.setLmpDate(pregnancyDetails.getLmpDate());
        response.setEdd(pregnancyDetails.getEdd());
        response.setCurrentEdd(pregnancyDetails.getCurrentEdd());
        response.setGestationPeriod(pregnancyDetails.getGestationPeriod());
        return response;
    }

    private List<OpdPatientRecallResponce.LabOrderHd> buildDgOrderHdList(List<DgOrderHd> hdList) {

        List<OpdPatientRecallResponce.LabOrderHd> newHdList = new ArrayList<>();

        for (DgOrderHd hdObj : safeList(hdList)) {

            if (hdObj == null) continue;

            OpdPatientRecallResponce.LabOrderHd hd = new OpdPatientRecallResponce.LabOrderHd();

            hd.setOrderHdId(hdObj.getId());
            hd.setOrderDate(hdObj.getOrderDate());
            hd.setOrderNo(hdObj.getOrderNo());
            hd.setOrderStatus(hdObj.getOrderStatus());
            hd.setCollectionStatus(hdObj.getCollectionStatus());
            hd.setPaymentStatus(hdObj.getPaymentStatus());
            hd.setAppointmentDate(hdObj.getAppointmentDate());

            List<DgOrderDt> dtList = safeList(dgOrderDtRepo.findByOrderHd(hdObj));

            List<OpdPatientRecallResponce.LabOrderDt> newDtList = new ArrayList<>();

            for (DgOrderDt dt : dtList) {

                if (dt == null) continue;

                OpdPatientRecallResponce.LabOrderDt nd = new OpdPatientRecallResponce.LabOrderDt();

                nd.setOrderDtId(dt.getId());
                nd.setOrderQty(dt.getOrderQty());
                nd.setOrderStatus(dt.getOrderStatus());
                nd.setAppointmentDate(dt.getAppointmentDate());
                nd.setBillingStatus(dt.getBillingStatus());

                // Investigation
                if (dt.getInvestigation() != null) {
                    nd.setInvestigationId(dt.getInvestigation().getInvestigationId());
                    nd.setInvestigationName(dt.getInvestigation().getInvestigationName());
                }

                // Package
                nd.setPackageId(dt.getInvestigationPackage() != null ? dt.getInvestigationPackage().getPackId() : null);

                // Billing
                nd.setBillingHd(dt.getBillingHd() != null ? dt.getBillingHd().getBillingHdId() : null);

                newDtList.add(nd);
            }

            hd.setLabOrderDts(newDtList);
            newHdList.add(hd);
        }

        return newHdList;
    }

    private List<OpdPatientRecallResponce.RadOrderHd> buildRadOrderHdList(List<RadOrderHd> hdList) {

        List<OpdPatientRecallResponce.RadOrderHd> newHdList = new ArrayList<>();

        for (RadOrderHd hdObj : safeList(hdList)) {

            if (hdObj == null) continue;

            OpdPatientRecallResponce.RadOrderHd hd = new OpdPatientRecallResponce.RadOrderHd();

            hd.setOrderHdId(Math.toIntExact(hdObj.getId()));
            hd.setOrderDate(hdObj.getOrderDate());

            hd.setPaymentStatus(hdObj.getPaymentStatus());
            hd.setAppointmentDate(hdObj.getAppointmentDate());

            List<RadOrderDt> dtList = safeList(radOrderDtRepository.findByRadOrderhd(hdObj));

            List<OpdPatientRecallResponce.RadOrderDt> newDtList = new ArrayList<>();

            for (RadOrderDt dt : dtList) {
                if (dt == null) continue;
                OpdPatientRecallResponce.RadOrderDt nd = new OpdPatientRecallResponce.RadOrderDt();
                nd.setOrderDtId(Math.toIntExact(dt.getId()));

                nd.setStudyStatus(dt.getStudyStatus());
                nd.setAppointmentDate(dt.getAppointmentDate());
                nd.setBillingStatus(dt.getBillingStatus());

                // Investigation
                if (dt.getInvestigation() != null) {
                    nd.setInvestigationId(dt.getInvestigation().getInvestigationId());
                    nd.setInvestigationName(dt.getInvestigation().getInvestigationName());
                }

                // Package
                nd.setPackageId(dt.getPackageId() != null ? dt.getPackageId().getPackId() : null);

                // Billing
                nd.setBillingHd(dt.getBillingHd() != null ? dt.getBillingHd().getBillingHdId() : null);

                newDtList.add(nd);
            }

            hd.setRadOrderDts(newDtList);
            newHdList.add(hd);
        }

        return newHdList;
    }


    //    update status
    @Override
    public ApiResponse<String> updateVisitStatus(Long visitId, String status) {

        Visit visit = visitRepository.findById(visitId).orElseThrow(() -> new RuntimeException("Visit not found with id: " + visitId));

        visit.setVisitStatus(status);

        visitRepository.save(visit);

        return ResponseUtils.createSuccessResponse("Status updated successfully", new TypeReference<>() {
        });
    }


    private String buildFullName(String firstName, String middleName, String lastName) {
        StringBuilder name = new StringBuilder();
        if (firstName != null && !firstName.trim().isEmpty()) {
            name.append(firstName.trim());
        }
        if (middleName != null && !middleName.trim().isEmpty()) {
            if (name.length() > 0) name.append(" ");
            name.append(middleName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (name.length() > 0) name.append(" ");
            name.append(lastName.trim());
        }
        return name.toString().trim();
    }


    @Transactional
    @Override
    public Visit updateVisitStatus(Long visitId, Instant visitDate, Long doctorId) {

        // Fetch current visit
        Visit currentVisit = visitRepository.findById(visitId).orElseThrow(() -> new RuntimeException("Visit not found"));

        // STEP 1 — Find previous CP visit
        Optional<Visit> cpVisitOpt = visitRepository.findCpVisit(doctorId, visitDate, "cp");

        if (cpVisitOpt.isPresent()) {
            Visit cpVisit = cpVisitOpt.get();

            // Case A: Completed → NP
            if ("c".equalsIgnoreCase(cpVisit.getVisitStatus())) {
                cpVisit.setDisplayPatientStatus("np");
                visitRepository.save(cpVisit);
            }
            // Case B: NOT completed → WP
            else {
                cpVisit.setDisplayPatientStatus("wp");
                visitRepository.save(cpVisit);
            }
        }

        // STEP 2 — Set CURRENT visit as CP
        currentVisit.setDisplayPatientStatus("cp");
        visitRepository.save(currentVisit);

        // STEP 3 — Fetch NEXT visits
        List<Visit> nextVisits = visitRepository.findNextVisits(doctorId, visitDate, currentVisit.getTokenNo());

        boolean rpAssigned = false;

        for (Visit next : nextVisits) {

            boolean validStatus = "n".equalsIgnoreCase(next.getVisitStatus());
            boolean validBilling = "y".equalsIgnoreCase(next.getBillingStatus());

            if (!rpAssigned && validStatus && validBilling) {
                // First valid next → RP
                next.setDisplayPatientStatus("rp");
                visitRepository.save(next);
                rpAssigned = true;
            } else {
                // Everything else → WP
                next.setDisplayPatientStatus("wp");
                visitRepository.save(next);
            }
        }

        return currentVisit;
    }


    @Override
    public ApiResponse<Page<OpdPreConsultationResponse>> getPendingPreConsultations(Pageable pageable, String patientName, String mobileNumber) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getHospital() == null) {
                Page<OpdPreConsultationResponse> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
                return ResponseUtils.createSuccessResponse(emptyPage, new TypeReference<>() {
                });
            }
            Long hospitalId = currentUser.getHospital().getId();
            Long departmentId = authUtil.getCurrentDepartmentId();
            Page<OpdPreConsultationProjection> projectionPage = visitRepository.findPendingPreConsultationsByHospitalPaged(hospitalId, departmentId, AppConstants.STATUS_N.toLowerCase(), AppConstants.STATUS_Y.toLowerCase(), AppConstants.STATUS_N.toLowerCase(), patientName, mobileNumber, pageable);
            Page<OpdPreConsultationResponse> responsePage = projectionPage.map(this::mapOpdPreConsultationProjectionToResponse);
            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error fetching pending pre-consultations: ", e);
            return ResponseUtils.createFailureResponse(new PageImpl<>(new ArrayList<>(), pageable, 0), new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * Retrieves the patient waiting list for the current hospital.
     *
     * @return ApiResponse containing list of patients in waiting list
     */
    @Override
    public ApiResponse<Page<PatientWaitingListResponse>> getWaitingList(Pageable pageable, String patientName, String mobileNumber, Long doctorId, Long sessionId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getHospital() == null) {
                return ResponseUtils.createFailureResponse(new PageImpl<>(new ArrayList<>(), pageable, 0), new TypeReference<>() {
                }, "User or hospital not found", 400);
            }

            Long hospitalId = currentUser.getHospital().getId();
            Long departmentId = authUtil.getCurrentDepartmentId();
            Page<PatientWaitingListProjection> projectionPage = visitRepository.findWaitingPatientsByHospitalWithFilters(hospitalId, departmentId, AppConstants.STATUS_Y.toLowerCase(), AppConstants.STATUS_N.toLowerCase(), patientName, mobileNumber, doctorId, sessionId, pageable);

            Page<PatientWaitingListResponse> responsePage = projectionPage.map(this::mapPatientWaitingListProjectionToResponse);
            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error fetching patient waiting list: ", e);
            return ResponseUtils.createFailureResponse(new PageImpl<>(new ArrayList<>(), pageable, 0), new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Page<PreviousOpdVisitResponse>> getPreviousOpdVisit(Long patientId, Long hospitalId, int page, int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("visitDate").descending());
            Page<PreviousOpdVisitProjection> projectionPage = visitRepository.getPreviousOpdVisit(patientId, hospitalId, AppConstants.STATUS_Y.toLowerCase(), pageable);

            //Projection → DTO
            Page<PreviousOpdVisitResponse> responsePage = projectionPage.map(p -> {
                PreviousOpdVisitResponse res = new PreviousOpdVisitResponse();
                res.setVisitDate(p.getVisitDate());
                res.setDoctorName(p.getDoctorName());
                res.setDepartment(p.getDepartment());
                res.setIcdDiag(p.getIcdDiag());
                res.setWorkingDiag(p.getWorkingDiag());
                res.setVisitId(p.getVisitId());
                return res;
            });

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<Page<PreviousOpdVisitResponse>>() {
            });

        } catch (Exception ex) {
            log.error("Error fetching getPriviousHistoryByPatient: ", ex);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Page<PreviousOpdVitalsDetailsResponse>> getPreviousOpdVitalsDetailsHistory(Long patientId, Long hospitalId, int page, int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("visitDate").descending());
            Page<PreviousOpdVitalsDetailsProjection> projectionPage = visitRepository.getPriviousOpdVitalsDetails(patientId, hospitalId, AppConstants.STATUS_Y.toLowerCase(), pageable);

            //Projection → DTO
            Page<PreviousOpdVitalsDetailsResponse> responsePage = projectionPage.map(p -> {
                PreviousOpdVitalsDetailsResponse res = new PreviousOpdVitalsDetailsResponse();
                res.setVisitDate(p.getVisitDate());
                res.setBmi(p.getBmi());
                res.setRr(p.getRr());
                res.setTemperature(p.getTemperature());
                res.setHeight(p.getHeight());
                res.setWeight(p.getWeight());
                res.setSpo2(p.getSpo2());
                res.setBpDiastolic(p.getBpDiastolic());
                res.setBpSystolic(p.getBpSystolic());
                res.setPulse(p.getPulse());
                return res;
            });

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<Page<PreviousOpdVitalsDetailsResponse>>() {
            });

        } catch (Exception ex) {
            log.error("Error fetching getPriviousHistoryByPatient: ", ex);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Page<PreviousOpdPsychiatryHistoryResponse>> getPreviousOpdPsychiatryDetailsHistory(
            Long patientId,
            Long hospitalId,
            int page,
            int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "assessmentDate"));

            Page<OpdPsychiatryAssessmentHeader> headerPage = opdPsychiatryAssessmentHeaderRepository.findByPatient_IdOrderByAssessmentDateDesc(patientId, pageable);

            List<Long> headerIds = headerPage.getContent().stream().map(OpdPsychiatryAssessmentHeader::getAssessmentHeaderId).filter(Objects::nonNull).toList();

            Map<Long, Map<Long, List<PreviousOpdPsychiatryHistoryResponse.AssessmentQuestionsResponse>>> detailsByHeaderId = new HashMap<>();
            Map<Long, Map<Long, String>> topicNamesByHeaderId = new HashMap<>();
            if (!headerIds.isEmpty()) {
                List<OpdPsychiatryAssessmentDetail> detailEntities = opdPsychiatryAssessmentDetailRepository.findByAssessmentHeaderId_AssessmentHeaderIdIn(headerIds);

                for (OpdPsychiatryAssessmentDetail detail : detailEntities) {
                    if (detail == null || detail.getAssessmentHeaderId() == null) continue;
                    PreviousOpdPsychiatryHistoryResponse.AssessmentQuestionsResponse qResponse = mapPsychiatryDetailToResponse(detail);
                    Long headerId = detail.getAssessmentHeaderId().getAssessmentHeaderId();
                    Long topicId = null;
                    String topicName = null;
                    if (detail.getQuestionId() != null && detail.getQuestionId().getQuestionHeading() != null) {
                        topicId = detail.getQuestionId().getQuestionHeading().getQuestionHeadingId();
                        topicName = detail.getQuestionId().getQuestionHeading().getQuestionHeadingName();
                    }
                    detailsByHeaderId.computeIfAbsent(headerId, k -> new HashMap<>()).computeIfAbsent(topicId, k -> new ArrayList<>()).add(qResponse);
                    if (topicName != null) {
                        topicNamesByHeaderId.computeIfAbsent(headerId, k -> new HashMap<>()).putIfAbsent(topicId, topicName);
                    }
                }
            }



            Page<PreviousOpdPsychiatryHistoryResponse> responsePage = headerPage.map(header -> {
                PreviousOpdPsychiatryHistoryResponse response = new PreviousOpdPsychiatryHistoryResponse();
                response.setAssessmentHeaderId(header.getAssessmentHeaderId());
                response.setPatientId(header.getPatient() != null ? header.getPatient().getId() : null);
                response.setVisitId(header.getVisit() != null ? header.getVisit().getId() : null);
                response.setDoctorName(extractDoctorName(header));
                response.setAssessmentDate(header.getAssessmentDate());
                response.setTotalScore(header.getTotalScore());
                response.setRemarks(header.getRemarks());

                Map<Long, List<PreviousOpdPsychiatryHistoryResponse.AssessmentQuestionsResponse>> topicsMap =
                        detailsByHeaderId.getOrDefault(header.getAssessmentHeaderId(), Collections.emptyMap());

                List<PreviousOpdPsychiatryHistoryResponse.PsychiatricAssessmentResponse> assessments = new ArrayList<>();
                for (Map.Entry<Long, List<PreviousOpdPsychiatryHistoryResponse.AssessmentQuestionsResponse>> entry : topicsMap.entrySet()) {
                    PreviousOpdPsychiatryHistoryResponse.PsychiatricAssessmentResponse pa =
                            new PreviousOpdPsychiatryHistoryResponse.PsychiatricAssessmentResponse();
                    Long tId = entry.getKey();
                    String tName = null;
                    Map<Long, String> topicNames = topicNamesByHeaderId.getOrDefault(header.getAssessmentHeaderId(), Collections.emptyMap());
                    if (tId != null) {
                        tName = topicNames.getOrDefault(tId, null);
                    }
                    if (tName == null) {
                        tName = header.getTopic() != null ? header.getTopic().getQuestionHeadingName() : null;
                    }
                    pa.setTopicName(tName);
                    pa.setQuestionsResponses(entry.getValue());
                    assessments.add(pa);
                }

                response.setAssessments(assessments);
                return response;
            });

            return ResponseUtils.createSuccessResponse(
                    responsePage,
                    new TypeReference<Page<PreviousOpdPsychiatryHistoryResponse>>() {
                    });

        } catch (Exception ex) {
            log.error("Error fetching previous psychiatry history for patientId={}, hospitalId={}", patientId, hospitalId, ex);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {
                    },
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<OpdRecallVisitResponse>> getRecallOpdVisit(String name, String mobile, LocalDate visitDate, int page, int size) {

        try {
            Long departmentId = authUtil.getCurrentDepartmentId();
            name = name == null ? "" : name.trim();
            mobile = mobile == null ? "" : mobile.trim();
            boolean dateFilter = visitDate != null;
            Instant startDate = null;
            Instant endDate = null;

            if (dateFilter) {
                startDate = visitDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                endDate = visitDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
            Page<OpdRecallVisitProjection> projectionPage = visitRepository.getOpdRecallVisit(name, mobile, dateFilter, startDate, endDate, departmentId, AppConstants.VISIT_STATUS_COMPLETED.toLowerCase(), pageable);
            Page<OpdRecallVisitResponse> responsePage = projectionPage.map(this::mapToResponse);
            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error while fetching OPD recall visit data : {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private String extractDoctorName(OpdPsychiatryAssessmentHeader header) {
        if (header == null || header.getVisit() == null) {
            return null;
        }

        if (header.getVisit().getDoctorName() != null && !header.getVisit().getDoctorName().isBlank()) {
            return header.getVisit().getDoctorName();
        }

        if (header.getVisit().getDoctor() != null) {
            return header.getVisit().getDoctor().getFullName();
        }

        return null;
    }

    private PreviousOpdPsychiatryHistoryResponse.AssessmentQuestionsResponse mapPsychiatryDetailToResponse(OpdPsychiatryAssessmentDetail detail) {
        PreviousOpdPsychiatryHistoryResponse.AssessmentQuestionsResponse response = new PreviousOpdPsychiatryHistoryResponse.AssessmentQuestionsResponse();
        if (detail == null) return response;
        response.setQuestionName(detail.getQuestionId() != null ? detail.getQuestionId().getQuestion() : null);
        response.setQuestionsAns(detail.getAnswerOptionId() != null ? detail.getAnswerOptionId().getOptionValue() : null);
        return response;
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return User object or null if not found
     */
    private User getCurrentUser() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUserName(username);
            if (user == null) {
                log.warn("User not found in database for username: {}", username);
            }
            return user;
        } catch (Exception e) {
            log.error("Error retrieving current user from security context", e);
            return null;
        }
    }

    /**
     * Maps OpdPreConsultationProjection to OpdPreConsultationResponse.
     *
     * @param projection the projection object to map
     * @return mapped response object
     */
    private OpdPreConsultationResponse mapOpdPreConsultationProjectionToResponse(OpdPreConsultationProjection projection) {
        OpdPreConsultationResponse response = new OpdPreConsultationResponse();
        response.setVisitId(projection.getVisitId());
        response.setPatientId(projection.getPatientId());
        response.setPatientName(projection.getPatientName());
        response.setAge(projection.getPatientAge());
        response.setGender(projection.getGender());
        response.setDepartmentId(String.valueOf(projection.getDepartmentId()));
        response.setDepartmentName(projection.getDepartmentName());
        response.setMobleNumber(projection.getMobileNumber());
        response.setVisitType(projection.getVisitType());
        response.setDoctorId(projection.getDoctorId());
        response.setDoctorName(projection.getDoctorName());
        response.setAppointmentDate(projection.getAppointmentDate() != null ? projection.getAppointmentDate().toString() : "");
        response.setAppointmentTime(projection.getAppointmentTime());
        response.setTokenNumber(String.valueOf(projection.getTokenNumber()));
        return response;
    }

    /**
     * Maps PatientWaitingListProjection to PatientWaitingListResponse.
     *
     * @param projection the projection object to map
     * @return mapped response object
     */
    private PatientWaitingListResponse mapPatientWaitingListProjectionToResponse(PatientWaitingListProjection projection) {
        PatientWaitingListResponse response = new PatientWaitingListResponse();
        response.setPatientId(projection.getPatientId());
        response.setVisitId(projection.getVisitId());
        response.setTokenNo(String.valueOf(projection.getTokenNo()));
        response.setMobileNo(projection.getMobileNumber());
        response.setPatientName(projection.getPatientName());
        response.setRelation(projection.getRelation());
        response.setAge(ageCalculator(projection.getDob()));
        response.setDob(projection.getDob());
        response.setGender(projection.getGender());
        response.setVisitType(projection.getVisitType());
        response.setDepartmentName(projection.getDepartmentName());
        response.setOpdDate(projection.getOpdDate());
        return response;
    }

    @Override
    public ApiResponse<List<PrescriptionDetailResponse>> getPrescriptionDetailsByPatientId(Long patientId) {
        try {
            log.info("Fetching prescription details for patient ID: {}", patientId);

            if (patientId == null || patientId <= 0) {
                log.warn("Invalid patient ID: {}", patientId);
                return ResponseUtils.createFailureResponse(new ArrayList<>(), new TypeReference<>() {
                }, "Patient ID is invalid", 400);
            }
            List<PrescriptionDetailProjection> prescriptionDetails = prescriptionDtRepository.findPrescriptionDetailsByPatientIdWithinLimitedDays(patientId, prescriptionHistoryDays);
            if (prescriptionDetails.isEmpty()) {
                log.info("No prescription details found for patient ID: {}", patientId);
                return ResponseUtils.createSuccessResponse(new ArrayList<>(), new TypeReference<>() {
                });
            }
            log.info("Found {} prescription detail items for patient ID: {}", prescriptionDetails.size(), patientId);

            List<PrescriptionDetailResponse> responses = prescriptionDetails.stream().map(this::mapPrescriptionDetailProjectionToResponse).collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error fetching prescription details for patient ID: {}", patientId, e);
            return ResponseUtils.createFailureResponse(new ArrayList<>(), new TypeReference<>() {
            }, "Error retrieving prescription details: " + e.getMessage(), 500);
        }
    }

    private PrescriptionDetailResponse mapPrescriptionDetailProjectionToResponse(PrescriptionDetailProjection projection) {
        return PrescriptionDetailResponse.builder().prescriptionDtId(projection.getPrescriptionDtId()).prescriptionHdId(projection.getPrescriptionHdId()).drugId(projection.getItemId()).drugName(projection.getItemName()).dosage(projection.getDosage()).frequency(projection.getFrequency()).days(projection.getDays()).total(projection.getTotal()).issuedQty(projection.getIssuedQty()).route(projection.getRoute()).instruction(projection.getInstruction()).unitPrice(projection.getUnitPrice()).discount(projection.getDiscount()).gstRate(projection.getGstRate()).lineCost(projection.getLineCost()).status(projection.getStatus()).batchNo(projection.getBatchNo()).expiryDate(projection.getExpiryDate()).doctorName(projection.getDoctorName()).departmentName(projection.getDepartmentName()).prescribedDate(projection.getPrescribedDate()).dispUnit(projection.getDispUnit()).build();
    }

    private OpdRecallVisitResponse mapToResponse(OpdRecallVisitProjection projection) {
        OpdRecallVisitResponse response = new OpdRecallVisitResponse();
        response.setVisitId(projection.getVisitId());
        response.setPatientId(projection.getPatientId());
        response.setPatientName(projection.getPatientName());
        response.setMobileNo(projection.getMobileNo());
        response.setGender(projection.getGender());
        response.setAge(projection.getAge());
        response.setDeptName(projection.getDeptName());
        response.setDoctorName(projection.getDoctorName());
        return response;
    }

    @Transactional
    public OpdPsychiatryAssessmentHeader saveOrUpdatePsychiatricAssessment(
            List<OpdPsychiatricDetailsRequest> details,
            Long topicId,
            Visit visit,
            OpdPatientDetail opdPatientDetail,
            User user) {

        // 1. Find or create header
        OpdPsychiatryAssessmentHeader header = opdPsychiatryAssessmentHeaderRepository
                .findByVisit_Id(visit.getId())
                .orElse(new OpdPsychiatryAssessmentHeader());

        boolean isNew = header.getAssessmentHeaderId() == null;

        // 2. Set header fields
        if (isNew) {
            header.setVisit(visit);
            header.setDepartment(visit.getDepartment());
            header.setPatient(visit.getPatient());
            header.setOpdPatientDetails(opdPatientDetail);
            header.setAssessmentDate(LocalDateTime.now());
//            header.setCreatedBy(user.getFullName());
//            header.setCreatedDate(LocalDateTime.now());
        } else {
            // Update existing header
//            header.setLastUpdatedBy(user.getFullName());
//            header.setLastUpdateDate(LocalDateTime.now());

            // Optionally update topic if changed
            if (topicId != null && !topicId.equals(header.getTopic().getQuestionHeadingId())) {
                header.setTopic(masQuestionHeadingRepository.findById(topicId)
                        .orElseThrow(() -> new EntityNotFoundException("Topic not found with id: " + topicId)));
            }
        }

        // 3. Save header first
        OpdPsychiatryAssessmentHeader savedHeader = opdPsychiatryAssessmentHeaderRepository.save(header);

        // 4. Handle details - Update or Create
        if (details != null && !details.isEmpty()) {
            handlePsychiatricDetails(details, savedHeader, isNew, user);
        } else if (!isNew) {
            // If no details provided and it's an existing record, delete all details
            deleteAllPsychiatricDetails(savedHeader);
        }

        // 5. Recalculate total score
        BigDecimal totalScore = calculateTotalScore(savedHeader);
        savedHeader.setTotalScore(totalScore);

        return opdPsychiatryAssessmentHeaderRepository.save(savedHeader);
    }

    private void handlePsychiatricDetails(
            List<OpdPsychiatricDetailsRequest> details,
            OpdPsychiatryAssessmentHeader header,
            boolean isNew,
            User user) {

        if (isNew) {
            // For new records, simply save all details
            for (OpdPsychiatricDetailsRequest detailReq : details) {
                OpdPsychiatryAssessmentDetail detail = createPsychiatricDetail(detailReq, header, user);
                opdPsychiatryAssessmentDetailRepository.save(detail);
            }
        } else {
            // For existing records, we need to handle updates more intelligently

            // Get existing details
            List<OpdPsychiatryAssessmentDetail> existingDetails = opdPsychiatryAssessmentDetailRepository
                    .findByAssessmentHeaderId_AssessmentHeaderId(header.getAssessmentHeaderId());

            // Create a map of existing details by question ID for quick lookup
            Map<Long, OpdPsychiatryAssessmentDetail> existingDetailMap = existingDetails.stream()
                    .collect(Collectors.toMap(
                            d -> d.getQuestionId().getId(),
                            d -> d
                    ));

            // Process each incoming detail
            for (OpdPsychiatricDetailsRequest detailReq : details) {
                OpdPsychiatryAssessmentDetail detail = existingDetailMap.get(detailReq.getQuestionId());

                if (detail == null) {
                    // Create new detail if it doesn't exist
                    detail = createPsychiatricDetail(detailReq, header, user);
                    opdPsychiatryAssessmentDetailRepository.save(detail);
                } else {
                    // Update existing detail
                    updatePsychiatricDetail(detail, detailReq, user);
                    opdPsychiatryAssessmentDetailRepository.save(detail);
                    // Remove from map to track which ones are still present
                    existingDetailMap.remove(detailReq.getQuestionId());
                }
            }

            // Delete details that are no longer present in the request
            if (!existingDetailMap.isEmpty()) {
                opdPsychiatryAssessmentDetailRepository.deleteAll(existingDetailMap.values());
                log.info("Deleted {} psychiatric assessment details for header ID: {}",
                        existingDetailMap.size(), header.getAssessmentHeaderId());
            }
        }
    }

    private OpdPsychiatryAssessmentDetail createPsychiatricDetail(
            OpdPsychiatricDetailsRequest detailReq,
            OpdPsychiatryAssessmentHeader header,
            User user) {

        OpdPsychiatryAssessmentDetail detail = new OpdPsychiatryAssessmentDetail();

        detail.setAssessmentHeaderId(header);

        // Set question
        if (detailReq.getQuestionId() != null) {
            detail.setQuestionId(opdQuestionMasterRepository.findById(detailReq.getQuestionId())
                    .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + detailReq.getQuestionId())));
        }

        // Set answer option
        BigDecimal score = BigDecimal.ZERO;
        if (detailReq.getAnswerOptionId() != null) {
            MasQuestionOptionValue optionValue = masQuestionOptionValueRepository
                    .findById(detailReq.getAnswerOptionId())
                    .orElseThrow(() -> new EntityNotFoundException("Option value not found with id: " + detailReq.getAnswerOptionId()));
            detail.setAnswerOptionId(optionValue);
            score = BigDecimal.valueOf(optionValue.getOptionScore());
        }

        detail.setScore(score);
//        detail.setCreatedBy(user.getFullName());
//        detail.setCreatedDate(LocalDateTime.now());

        return detail;
    }

    private void updatePsychiatricDetail(
            OpdPsychiatryAssessmentDetail detail,
            OpdPsychiatricDetailsRequest detailReq,
            User user) {

        // Update question if changed
        if (detailReq.getQuestionId() != null && !detailReq.getQuestionId().equals(detail.getQuestionId().getId())) {
            detail.setQuestionId(opdQuestionMasterRepository.findById(detailReq.getQuestionId())
                    .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + detailReq.getQuestionId())));
        }

        // Update answer option if changed
        BigDecimal score = BigDecimal.ZERO;
        if (detailReq.getAnswerOptionId() != null) {
            MasQuestionOptionValue optionValue = masQuestionOptionValueRepository
                    .findById(detailReq.getAnswerOptionId())
                    .orElseThrow(() -> new EntityNotFoundException("Option value not found with id: " + detailReq.getAnswerOptionId()));
            detail.setAnswerOptionId(optionValue);
            score = BigDecimal.valueOf(optionValue.getOptionScore());
        } else {
            detail.setAnswerOptionId(null);
        }

        detail.setScore(score);
    }

    private void deleteAllPsychiatricDetails(OpdPsychiatryAssessmentHeader header) {
        List<OpdPsychiatryAssessmentDetail> existingDetails = opdPsychiatryAssessmentDetailRepository
                .findByAssessmentHeaderId_AssessmentHeaderId(header.getAssessmentHeaderId());

        if (!existingDetails.isEmpty()) {
            opdPsychiatryAssessmentDetailRepository.deleteAll(existingDetails);
            log.info("Deleted all psychiatric assessment details for header ID: {}",
                    header.getAssessmentHeaderId());
        }
    }

    private BigDecimal calculateTotalScore(OpdPsychiatryAssessmentHeader header) {
        List<OpdPsychiatryAssessmentDetail> details = opdPsychiatryAssessmentDetailRepository
                .findByAssessmentHeaderId_AssessmentHeaderId(header.getAssessmentHeaderId());

        return details.stream()
                .map(OpdPsychiatryAssessmentDetail::getScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void handlePregnancyDetails(
            OpdPatientDetail opd,
            OpdPatientDetailCreateRequest.PregnancyDetails pregnancyDetails,
            User user
    ) {
        if (pregnancyDetails == null) {
            return;
        }

        if (opd.getVisit() == null) {
            throw new IllegalArgumentException(
                    "Visit is required to save pregnancy details"
            );
        }

        if (opd.getPatient() == null) {
            throw new IllegalArgumentException(
                    "Patient is required to save pregnancy details"
            );
        }

        Long visitId = opd.getVisit().getId();
        log.info(
                "Saving pregnancy details for visitId={}, isPregnant={}, lmpDate={}, edd={}, currentEdd={}, gestationPeriod={}",
                visitId,
                pregnancyDetails.getIsPregnant(),
                pregnancyDetails.getLmpDate(),
                pregnancyDetails.getEdd(),
                pregnancyDetails.getCurrentEdd(),
                pregnancyDetails.getGestationPeriod()
        );

        OpdPatientPregnancyDetails pregnancyEntity =
                opdPatientPregnancyDetailsRepository
                        .findByVisit_Id(visitId)
                        .orElseGet(OpdPatientPregnancyDetails::new);

        pregnancyEntity.setVisit(opd.getVisit());
        pregnancyEntity.setPatient(opd.getPatient());
        pregnancyEntity.setIsPregnant(pregnancyDetails.getIsPregnant());
        pregnancyEntity.setLmpDate(pregnancyDetails.getLmpDate());
        pregnancyEntity.setEdd(pregnancyDetails.getEdd());
        pregnancyEntity.setCurrentEdd(pregnancyDetails.getCurrentEdd());
        pregnancyEntity.setGestationPeriod(
                pregnancyDetails.getGestationPeriod()
        );
        pregnancyEntity.setLastChgDate(Instant.now());
        pregnancyEntity.setLastChgBy(
                user != null ? user.getFullName() : null
        );

        OpdPatientPregnancyDetails savedEntity =
                opdPatientPregnancyDetailsRepository.save(pregnancyEntity);

        log.info(
                "Pregnancy details saved successfully. pregnancyDetailsId: {}, OPD patient ID: {}, visit ID: {}",
                savedEntity.getPregnancyDetailsId(),
                opd.getOpdPatientDetailsId(),
                visitId
        );
    }

    @Override
    public ApiResponse<Page<OpdReportListResponse>> getOpdReportsList(Pageable pageable,String mobileNumber, String patientName, Long hospitalId ) {

        log.info("Fetching OPD reports for visitId: {}, page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());


            Page<OpdReportListProjection> projections =
                    visitRepository.getOpdReportsList(AppConstants.VISIT_STATUS_COMPLETED.toLowerCase(), AppConstants.OPDTYPE, mobileNumber, patientName, pageable);

            Page<OpdReportListResponse> responses = projections.map(projection -> {

                OpdReportListResponse response = new OpdReportListResponse();

                response.setVisitId(projection.getVisitId());
                response.setPatientId(projection.getPatientId());
                response.setPatientName(projection.getPatientName());
                response.setMobileNumber(projection.getMobileNumber());
                response.setUhid(projection.getUhid());
                response.setRelation(projection.getRelation());
                response.setGender(projection.getGender());
                response.setAge(projection.getAge());
                response.setSpecialty(projection.getSpecialty());
                response.setDoctorName(projection.getDoctorName());
                response.setVisitDateTime(projection.getVisitDateTime());
                response.setPrescriptionHdId(projection.getPrescriptionHdId());
                response.setPrescriptionStatus(projection.getPrescriptionStatus());

                return response;
            });

            return ResponseUtils.createSuccessResponse(
                    responses,
                    new TypeReference<>() {}
            );
    }
}

