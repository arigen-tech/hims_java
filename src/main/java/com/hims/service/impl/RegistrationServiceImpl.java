package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.exception.patientRegistrationException.AppSetupNotFoundException;
import com.hims.exception.patientRegistrationException.InvalidDateException;
import com.hims.exception.patientRegistrationException.TokenAlreadyBookedException;
import com.hims.helperUtil.ConverterUtils;
import com.hims.helperUtil.HelperUtils;
import com.hims.mapper.OpdPatientDetailMapper;
import com.hims.mapper.PatientMapper;
import com.hims.mapper.VisitMapper;
import com.hims.projection.*;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.BillingService;
import com.hims.service.DoctorRosterServices;
import com.hims.service.PatientLoginService;
import com.hims.service.RegistrationService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import kong.unirest.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RegistrationServiceImpl implements RegistrationService {
    private static final String UPLOAD_DIR = "patientImage/";
    private static final Logger log = LoggerFactory.getLogger(PatientServiceImpl.class);
    @Value("${upload.image.path}")
    private String baseUrl;
    @Value("${serviceCategoryOPD}")
    private String serviceCategoryOPD;
    @Value("${app.opdDepartmentType}")
    private Integer opdDepartmentType;


    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AuthUtil authUtil;
    @Autowired
    BillingService billingService;
    @Autowired
    MasGenderRepository masGenderRepository;
    @Autowired
    MasRelationRepository masRelationRepository;
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
    UserRepo userRepository;
    @Autowired
    MasOpdSessionRepository masOpdSessionRepository;
    @Autowired
    AppSetupRepository appSetupRepository;

    @Autowired
    PaymentDetailRepository paymentDetailRepository;


    @Autowired
    OpdPatientDetailMapper opdPatientDetailMapper;

    @Autowired
    VisitMapper visitMapper;

    @Autowired
    DoctorRosterServices doctorRosterServices;
    @Autowired
    private HelperUtils helperUtils;


    @Autowired
    private OpdPatientDetailRepository opdPatientDetailRepository;
    @Autowired
    private MasServiceCategoryRepository masServiceCategoryRepository;

    @Autowired
    private BillingHeaderRepository billingHeaderRepository;
    @Autowired
    private BillingDetailRepository billingDetailRepository;


    @Autowired
    private PatientLoginService patientLoginService;

    @Autowired
    private MasAppointmentChangeReasonRepository changeReasonRepository;

    @Autowired
    private RescheduleHistoryRepository historyRepository;

    @Autowired
    VisitRepository visitRepository;

    @Autowired
    private LabHdRepository labHdRepository;

    @Autowired
    private LabDtRepository labDtRepository;

    @Autowired
    private RadOrderHdRepository radOrderHdRepository;

    @Autowired
    private RadOrderDtRepository radOrderDtRepository;

    @Autowired
    private InpatientRepository inpatientRepository;

    @Autowired
    private InpatientValidationService inpatientValidationService;




    @Override
    @Transactional
    public ApiResponse<PatientRegFollowUpResp> createPatient(PatientRequest patient, OpdPatientDetailRequest opdPatientDetail, List<VisitRequest> visit) {
        PatientRegFollowUpResp resp=new PatientRegFollowUpResp();
        Optional<Patient> existingPatient = patientRepository.findByUniqueCombination(
                patient.getPatientFn(),
                patient.getPatientLn(),
                (masGenderRepository.findById(patient.getPatientGenderId())).get(),
                patient.getPatientDob() != null ? patient.getPatientDob() : null,
                patient.getPatientAge(),
                patient.getPatientMobileNumber(),
                (masRelationRepository.findById(patient.getPatientRelationId())).get());
        if (existingPatient.isPresent()) {
            resp.setPatient(PatientMapper.mapToDTO(existingPatient.get()));
            return ResponseUtils.createFailureResponse(resp, new TypeReference<>() {
                    },
                    "Patient already Registered", 500);
        }
        Patient patientObj = savePatient(patient,false);
        patientLoginService.savePatientLogin(patientObj);
        resp.setPatient(PatientMapper.mapToDTO(patientObj));
        OpdPatientDetail newOpd=new OpdPatientDetail();
        if(visit!=null){
            List<Visit> savedVisits = new ArrayList<>();
            if (!visit.isEmpty()) {
                validateDuplicateAppointments(visit, patientObj.getId());
                for (VisitRequest v : visit) {
                    Instant today = v.getVisitDate();
                    String visitType = helperUtils.getVisitTypeForFollowUpOrNew(patientObj.getId());
                    v.setVisitType(visitType);
                    Visit saved = createSingleAppointment(v, patientObj);
                    savedVisits.add(saved);
                    if (saved.getHospital().getPreConsultationAvailable().equalsIgnoreCase(AppConstants.STATUS_N)) {
                        newOpd = addOpdDetails(saved, opdPatientDetail, patientObj);
                    }
                }
            }
            if(savedVisits.get(0).getBillingStatus().equalsIgnoreCase(AppConstants.STATUS_N)){
                List<OpdVisitResponseDTO> visitResponses = savedVisits.stream()
                        .map(visitMapper::mapToDTO)
                        .toList();
                resp.setVisits(visitResponses);
            }
            OPDBillingPatientResponse finalResponse =  buildFinalResponse(patientObj,savedVisits);
            resp.setOpdBillingPatientResponse(finalResponse);
        }
        resp.setOpdPatientDetail(opdPatientDetailMapper.mapToDTO(newOpd));
        return ResponseUtils.createSuccessResponse(resp, new TypeReference<>() {
        });
    }

    @Override
    @Transactional
    public ApiResponse<PatientRegFollowUpResp> updatePatient(PatientFollowUpReq followUpRequest) {
        if (followUpRequest == null || followUpRequest.getPatientDetails() == null) {
            throw new RuntimeException("Invalid request");
        }
        PatientRegistrationReq details = followUpRequest.getPatientDetails();
        PatientRegFollowUpResp resp = new PatientRegFollowUpResp();
        Patient patient;

        if (details.getPatient() != null && details.getPatient().getId() != null) {
            patient = updatePatientDetails(details.getPatient(), true);
        } else if (followUpRequest.isAppointmentFlag()
                && details.getVisits() != null
                && !details.getVisits().isEmpty()
                && details.getVisits().get(0).getPatientId() != null) {

            Long patientId = details.getVisits().get(0).getPatientId();

            patient = patientRepository.findById(patientId).orElseThrow(() -> new SDDException(500,"Patient not found"));

        } else {
            throw new SDDException(500,"Patient ID is required");
        }

        resp.setPatient(PatientMapper.mapToDTO(patient));

        if (followUpRequest.isAppointmentFlag()) {
            List<VisitRequest> visitList = details.getVisits();
            OpdPatientDetailRequest opdReq = details.getOpdPatientDetail();
            List<Visit> updatedVisits = new ArrayList<>();
            OpdPatientDetail opdDetails = null;

            if (visitList != null && !visitList.isEmpty()) {
                validateDuplicateAppointments(visitList, patient.getId());
                for (VisitRequest v : visitList) {
                    if (v.getPatientId() == null) {
                        v.setPatientId(patient.getId());
                    }
                    if (v.getHospitalId() == null && patient.getPatientHospital() != null) {
                        v.setHospitalId(patient.getPatientHospital().getId());
                    }
                    if (v.getVisitDate() == null) {
                        v.setVisitDate(Instant.now());
                    }
                    if (v.getVisitType() == null) {
                        v.setVisitType(AppConstants.VISIT_TYPE_FOLLOW_UP);
                    }
                    Visit visit;
                    if (v.getId() != null) {
                        visit = updateExistingVisitById(v, patient);
                    } else {
                        visit = createSingleAppointment(v, patient);
                    }
                    updatedVisits.add(visit);
                    if (visit.getHospital().getPreConsultationAvailable()
                            .equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())) {
                        opdDetails = addOpdDetails(visit, opdReq, patient);
                    }
                }
            }
            List<OpdVisitResponseDTO> visitResponses = updatedVisits.stream()
                    .map(visitMapper::mapToDTO)
                    .toList();
            resp.setVisits(visitResponses);
            if (opdDetails != null) {
                resp.setOpdPatientDetail(opdPatientDetailMapper.mapToDTO(opdDetails));
            }
            OPDBillingPatientResponse finalResponse =
                    buildFinalResponse(patient, updatedVisits);
            resp.setOpdBillingPatientResponse(finalResponse);
        }
        return ResponseUtils.createSuccessResponse(resp, new TypeReference<>() {});
    }


    @Override
    public ApiResponse<String> uploadPatientImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            // Ensure the upload directory exists
            File uploadDir = new File(baseUrl + UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Generate a unique filename
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(baseUrl + UPLOAD_DIR, filename);

            // Save file to the server
            Files.write(filePath, file.getBytes());
            return ResponseUtils.createSuccessResponse(baseUrl + UPLOAD_DIR + filename, new TypeReference<>() {
            });

        } catch (IOException e) {
            throw new RuntimeException("Error uploading file: " + e.getMessage());
        }
    }


    @Override
    public ApiResponse<Page<PatientProjection>> searchPatient(PatientSearchReq req, Pageable pageable) {

        String mobileNo = cleanStringParameter(req.getMobileNo());
        String patientName = cleanStringParameter(req.getPatientName());

        // Single unified method handles all scenarios:
        // - Both mobile and name provided
        // - Name only provided
        // - Mobile only provided
        // - Neither provided (will return empty results)
        Page<PatientProjection> patientList = patientRepository.searchPatients(mobileNo, patientName, pageable);

        return ResponseUtils.createSuccessResponse(patientList, new TypeReference<>() {});
    }

    public ApiResponse<FollowUpPatientResponseDetails> getPatientDetails(Long patientId, String serviceCategoryCode) {
        try {

            if (inpatientValidationService.isPatientCurrentlyAdmitted(patientId)) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        AppConstants.PATIENT_NOT_APPLICABLE_FOR_SERVICE_REGISTRATION,
                        400);
            }
            log.info("Fetching patient details for patientId: {}, serviceCategoryCode: {}", patientId, serviceCategoryCode);

            // Default to OPD if serviceCategoryCode is null or empty
            String categoryCode = (serviceCategoryCode == null || serviceCategoryCode.trim().isEmpty())
                    ? serviceCategoryOPD
                    : serviceCategoryCode;

            FollowUpPatientResponseDetails resp = new FollowUpPatientResponseDetails();
            PatientProjectionFollowUpDetails patientData = patientRepository.findPatientDetails(patientId);

            if (patientData == null) {
                log.warn("Patient not found for patientId: {}", patientId);
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Patient not found", 404);
            }

            // Map personal details
            resp.setPatientId(patientData.getPatientId());
            resp.setPersonal(mapPersonalDetails(patientData));
            resp.setAddress(mapAddressDetails(patientData));
            resp.setNok(mapNokDetails(patientData));
            resp.setEmergency(mapEmergencyDetails(patientData));
            resp.setPhotoUrl(patientData.getPhotoUrl());

            // Map OPD-specific details if serviceCategoryCode is OPD
            if (categoryCode.equalsIgnoreCase(serviceCategoryOPD)) {
                PatientVitalsProjection vitals = opdPatientDetailRepository.findLatestVitals(patientId);
                if (vitals != null) {
                    resp.setVitals(mapVitalDetails(vitals));
                }

                List<AppointmentProjection> appointments = visitRepository.findAppointments(patientId , opdDepartmentType);
                resp.setAppointments(mapAppointmentDetails(appointments));
            }

            log.info("Successfully fetched patient details for patientId: {}", patientId);
            return ResponseUtils.createSuccessResponse(resp, new TypeReference<FollowUpPatientResponseDetails>() {});

        } catch (Exception e) {
            log.error("Error fetching patient details for patientId: {}", patientId, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Error fetching patient details: " + e.getMessage(), 500);
        }
    }



    @Override
    @Transactional
    public ApiResponse<PaymentResponse> updatePaymentStatus(PaymentUpdateRequest request) {
        PaymentResponse res = new PaymentResponse();
        BillingHeader header;
        List<PaymentUpdateRequest.OpdBillPayment> opdPayments = request.getOpdBillPayments();
        if (opdPayments == null || opdPayments.isEmpty()) {
            throw new RuntimeException("OPD payment items missing in request.");
        }

        List<OpdPaymentItem> paymentItemList = new ArrayList<>();

        for (PaymentUpdateRequest.OpdBillPayment opd : opdPayments) {
            Integer billHeaderId = opd.getBillHeaderId();
            BigDecimal netAmount = opd.getNetAmount();

            Optional<BillingHeader> headerOpt = billingHeaderRepository.findById(billHeaderId);
            if (headerOpt.isPresent()) {
                header = headerOpt.get();
            } else {
                throw new RuntimeException("BillingHeader not found with id: " + billHeaderId);
            }
            List<BillingDetail> details = billingDetailRepository.findByBillHdId(Long.valueOf(billHeaderId));
            if (!details.isEmpty()) {
                for(BillingDetail bdt: details){
                    bdt.setChargeCost(bdt.getNetAmount());
                    bdt.setPaymentStatus("y");
                }
            }
            Visit visit = header.getVisit();
            if (visit == null) {
                throw new RuntimeException("Visit not linked with OPD Bill Header " + billHeaderId);
            }

            PaymentDetail paymentDetail = new PaymentDetail();
            paymentDetail.setPaymentMode(request.getMode());
            paymentDetail.setPaymentStatus("y");
            paymentDetail.setPaymentReferenceNo(request.getPaymentReferenceNo());
            paymentDetail.setPaymentDate(Instant.now());
            paymentDetail.setAmount(netAmount);
            paymentDetail.setCreatedBy(authUtil.getCurrentUser().getFirstName());
            paymentDetail.setCreatedAt(Instant.now());
            paymentDetail.setUpdatedAt(Instant.now());
            paymentDetail.setBillingHd(header);
            paymentDetailRepository.save(paymentDetail);

            BigDecimal oldPaid = header.getTotalPaid() == null ? BigDecimal.ZERO : header.getTotalPaid();
            header.setTotalPaid(oldPaid.add(netAmount));
            header.setPaymentStatus("y");
            billingHeaderRepository.save(header);

            visit.setBillingStatus("y");
            visit.setBillingHd(header);
            visitRepository.save(visit);

            OpdPaymentItem item = new OpdPaymentItem();
            item.setBillHeaderId(billHeaderId);
            item.setVisitId(visit.getId());
            item.setNetAmount(netAmount);
            item.setPatientName(visit.getPatient().getFullName());
            item.setTokenNo(visit.getTokenNo());
            item.setDoctorName(visit.getDoctorName());
            paymentItemList.add(item);
        }
        res.setMsg("Success");
        res.setPaymentStatus("y");
        res.setBillPayments(paymentItemList);
        return ResponseUtils.createSuccessResponse(res, new TypeReference<>() {});
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> cancelAppointment(CancelAppointmentRequest request) {
        // Check Visit exist
        Optional<Visit> optionalVisit = visitRepository.findById(request.getVisitId());
        if (optionalVisit.isEmpty()) {
            return new ApiResponse<>(HttpStatus.NOT_FOUND, "Appointment not found with ID: " + request.getVisitId());
        }
        Visit visit = optionalVisit.get();
        //Check billing Entry
        BillingHeader bill = billingHeaderRepository.findByVisit(visit);
        if (bill == null) {
            return new ApiResponse<>(HttpStatus.NOT_FOUND, "Billing not found for appointment ID: " + request.getVisitId());
        }
        // Get current user
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null || currentUser.getFirstName() == null) {
            throw new RuntimeException("User authentication failed or user has no first name");
        }
        // Update visit
        visit.setVisitStatus(AppConstants.VISIT_STATUS_CANCELLED.toLowerCase());
        visit.setCancelledBy(currentUser.getFirstName());
        visit.setCancelledDateTime(Instant.now());
        if (request.getCancelReasonId() != null) {
            MasAppointmentChangeReason reason = changeReasonRepository.findById(request.getCancelReasonId())
                    .orElseThrow(() -> new RuntimeException("Cancel reason not found with ID: " + request.getCancelReasonId()));
            visit.setReason(reason);
        }

        syncCancelledOrderStatus(visit);

      //  bill.setPaymentStatus("y");
      //  billingHeaderRepository.save(bill);
        Visit savedVisit = visitRepository.save(visit);
        return new ApiResponse<>(HttpStatus.OK, "Appointment cancelled successfully");
    }

    @Override
    @Transactional
    public ApiResponse<RescheduleAppointmentResponse> rescheduleAppointment(RescheduleAppointmentRequest request) {
        if (request == null || request.getVisitId() == null) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, "visitId is required");
        }

        Optional<Visit> optionalVisit = visitRepository.findById(request.getVisitId());
        if (optionalVisit.isEmpty()) {
            return new ApiResponse<>(HttpStatus.NOT_FOUND, "Appointment not found with ID: " + request.getVisitId());
        }

        Visit v = optionalVisit.get();
        String departmentTypeCode = null;
        if (v.getDepartment() != null && v.getDepartment().getDepartmentType() != null) {
            departmentTypeCode = v.getDepartment().getDepartmentType().getDepartmentTypeCode();
        }
        boolean isOpdAppointment = AppConstants.OPDTYPE.equalsIgnoreCase(departmentTypeCode);
        boolean isLabOrRadiologyAppointment =
                AppConstants.LABTYPE.equalsIgnoreCase(departmentTypeCode) || AppConstants.RADIOTYPE.equalsIgnoreCase(departmentTypeCode);

        if (request.getVisitDate() == null) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, "visitDate is required");
        }

        if (isOpdAppointment) {
            if (request.getAppointmentStartTime() == null ||
                    request.getAppointmentEndTime() == null ||
                    request.getTokenNumber() == null) {
                return new ApiResponse<>(
                        HttpStatus.BAD_REQUEST,
                        "appointmentStartTime, appointmentEndTime and tokenNumber are required for OPD reschedule"
                );
            }
        }

        Long resolvedTokenNumber = isOpdAppointment
                ? request.getTokenNumber()
                : v.getTokenNo();

        Instant resolvedStartTime = isOpdAppointment
                ? request.getAppointmentStartTime()
                : v.getStartTime();

        Instant resolvedEndTime = isOpdAppointment
                ? request.getAppointmentEndTime()
                : v.getEndTime();

        VisitRescheduleHistory history = new VisitRescheduleHistory();
        history.setVisitId(v);
        history.setRescheduleDatetime(request.getVisitDate());
        history.setRescheduleBy(authUtil.getCurrentUser().getFirstName());
        history.setNewTokenNo(resolvedTokenNumber);
        history.setOldTokenNo(v.getTokenNo());
        history.setNewVisitDatetime(
                isOpdAppointment ? request.getAppointmentStartTime() : request.getVisitDate()
        );
        history.setOldVisitDatetime(v.getVisitDate());
        history.setRescheduleDatetime(Instant.now());
        history.setRescheduleReason("");
        historyRepository.save(history);

        LocalDate updatedVisitDate = toLocalDate(request.getVisitDate());

        v.setVisitDate(request.getVisitDate());
        if (isOpdAppointment) {
            v.setStartTime(resolvedStartTime);
            v.setEndTime(resolvedEndTime);
            v.setTokenNo(resolvedTokenNumber);
        } else if (isLabOrRadiologyAppointment) {
            v.setTokenNo(resolvedTokenNumber);
            if (request.getAppointmentStartTime() != null) {
                v.setStartTime(request.getAppointmentStartTime());
            }
            if (request.getAppointmentEndTime() != null) {
                v.setEndTime(request.getAppointmentEndTime());
            }
            syncLabOrRadiologyReschedule(v, updatedVisitDate, departmentTypeCode);
        }
        v.setLastChgDate(Instant.now());

        visitRepository.save(v);
        return new ApiResponse<>(HttpStatus.OK, "Success");
    }

    private void syncLabOrRadiologyReschedule(Visit visit, LocalDate updatedVisitDate, String departmentTypeCode) {
        if (visit == null || updatedVisitDate == null) {
            return;
        }

        if (AppConstants.LABTYPE.equalsIgnoreCase(departmentTypeCode)) {
            List<DgOrderHd> labHeaders = labHdRepository.findAllByVisitId(visit);
            for (DgOrderHd header : labHeaders) {
                header.setOrderDate(updatedVisitDate);
                header.setAppointmentDate(updatedVisitDate);
                List<DgOrderDt> details = labDtRepository.findByOrderhdId(header);
                for (DgOrderDt detail : details) {
                    detail.setAppointmentDate(updatedVisitDate);
                }
                labDtRepository.saveAll(details);
            }
            labHdRepository.saveAll(labHeaders);
            return;
        }

        if (AppConstants.RADIOTYPE.equalsIgnoreCase(departmentTypeCode)) {
            List<RadOrderHd> radHeaders = radOrderHdRepository.findAllByVisit_Id(visit.getId());
            for (RadOrderHd header : radHeaders) {
                header.setOrderDate(updatedVisitDate);
                header.setAppointmentDate(updatedVisitDate);
                List<RadOrderDt> details = radOrderDtRepository.findByRadOrderhd(header);
                for (RadOrderDt detail : details) {
                    detail.setAppointmentDate(updatedVisitDate);
                }
                radOrderDtRepository.saveAll(details);
            }
            radOrderHdRepository.saveAll(radHeaders);
        }
    }

    private void syncCancelledOrderStatus(Visit visit) {
        String cancelledStatus = AppConstants.VISIT_STATUS_CANCELLED.toLowerCase();

        List<DgOrderHd> labHeaders = labHdRepository.findAllByVisitId(visit);
        for (DgOrderHd header : labHeaders) {
            header.setOrderStatus(cancelledStatus);
            List<DgOrderDt> details = labDtRepository.findByOrderhdId(header);
            for (DgOrderDt detail : details) {
                detail.setOrderStatus(cancelledStatus);
            }
            labDtRepository.saveAll(details);
        }
        labHdRepository.saveAll(labHeaders);

        List<RadOrderHd> radHeaders = radOrderHdRepository.findAllByVisit_Id(visit.getId());
        for (RadOrderHd header : radHeaders) {
            List<RadOrderDt> details = radOrderDtRepository.findByRadOrderhd(header);
            for (RadOrderDt detail : details) {
                detail.setOrderStatus(cancelledStatus);
            }
            radOrderDtRepository.saveAll(details);
        }
        radOrderHdRepository.saveAll(radHeaders);
    }

    private LocalDate toLocalDate(Instant instant) {
        return instant == null ? null : instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }


    @Override
    public ApiResponse<BookingAppointmentResponse> bookAppointment(Long patientId, VisitRequest visitReq) {
        Patient patient = null;
        BookingAppointmentResponse response = new BookingAppointmentResponse();

        if (patientId != null) {
            patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));

            if (visitReq!=null) {

                Instant date = visitReq.getVisitDate();
                String visitType = helperUtils.getVisitTypeForFollowUpOrNew(patient.getId());
                visitReq.setVisitType(visitType);
                Visit saved = createSingleAppointment(visitReq, patient);

                response.setPatientId(patient.getId());
                response.setVisitDate(saved.getVisitDate());
                response.setStartTime(saved.getStartTime());
                response.setEndTime(saved.getEndTime());
                response.setTokenNo(saved.getTokenNo());
            }
        } else {
            throw new RuntimeException("Patient Id cannot be null");
        }

        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {},"Appointment Booked");
    }

    @Override
    public ApiResponse<List<CancelledAppointmentResponse>> getCancelledAppointments(Long hospitalId,Long departmentId,Long doctorId,LocalDate fromDate,LocalDate toDate,Long cancellationReasonId
    ) {

        log.info("Fetching cancelled appointments: hospitalId={}, departmentId={}, doctorId={}, fromDate={}, toDate={}, cancellationReasonId={}",
                hospitalId, departmentId, doctorId, fromDate, toDate, cancellationReasonId);

        try {
            if (hospitalId == null || hospitalId <= 0) {
                log.warn("Hospital ID is required");
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Hospital ID is required",
                        org.springframework.http.HttpStatus.BAD_REQUEST.value()
                );
            }


            List<CancelledAppointmentProjection> projectionList = visitRepository.findCancelledAppointments(
                    hospitalId, departmentId, doctorId, fromDate, toDate, cancellationReasonId
            );

            // Map projections to response DTOs
            List<CancelledAppointmentResponse> responseList = projectionList.stream()
                    .map(this::mapCancelledAppointmentProjectionToDto)
                    .collect(Collectors.toList());

            log.info("Successfully fetched {} cancelled appointment(s)", responseList.size());
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

        } catch (Exception ex) {
            log.error("Error fetching cancelled appointments for hospitalId={}, departmentId={}, doctorId={}",
                    hospitalId, departmentId, doctorId, ex);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Failed to fetch cancelled appointments: " + ex.getMessage(),
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    public Patient savePatient(PatientRequest request, boolean followUp) {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null){
            log.info("current users not found");
        }

        Patient patient = new Patient();

        patient.setPatientFn(request.getPatientFn());
        patient.setPatientMn(request.getPatientMn());
        patient.setPatientLn(request.getPatientLn());
        patient.setPatientDob(request.getPatientDob());
        patient.setPatientAge(request.getPatientAge());
        patient.setPatientEmailId(request.getPatientEmailId());
        patient.setPatientMobileNumber(request.getPatientMobileNumber());
        patient.setPatientImage(request.getPatientImage());
        patient.setFileName(request.getFileName());
        patient.setPatientAddress1(request.getPatientAddress1());
        patient.setPatientAddress2(request.getPatientAddress2());
        patient.setPatientCity(request.getPatientCity());
        patient.setPatientPincode(request.getPatientPincode());
        patient.setPincode(request.getPincode());
        patient.setEmerFn(request.getEmerFn());
        patient.setEmerLn(request.getEmerLn());
        patient.setEmerMobile(request.getEmerMobile());
        patient.setNokFn(request.getNokFn());
        patient.setNokLn(request.getNokLn());
        patient.setNokEmail(request.getNokEmail());
        patient.setNokMobileNumber(request.getNokMobileNumber());
        patient.setNokAddress1(request.getNokAddress1());
        patient.setNokAddress2(request.getNokAddress2());
        patient.setNokCity(request.getNokCity());
        patient.setNokPincode(request.getNokPincode());
        patient.setPatientStatus(request.getPatientStatus());
        patient.setRegDate(request.getRegDate());
        patient.setCreatedOn(Instant.now());
        patient.setUpdatedOn(Instant.now());
        patient.setLastChgBy(currentUser.getFirstName()+" "+currentUser.getMiddleName()+" "+currentUser.getLastName());
        patient.setPatientHospital(currentUser.getHospital());
        patient.setPatientAbhaId(request.getPatientAbhaId());


        Optional.ofNullable(request.getPatientGenderId())
                .flatMap(masGenderRepository::findById)
                .ifPresent(patient::setPatientGender);

        Optional.ofNullable(request.getPatientRelationId())
                .flatMap(masRelationRepository::findById)
                .ifPresent(patient::setPatientRelation);

        Optional.ofNullable(request.getPatientMaritalStatusId())
                .flatMap(masMaritalStatusRepository::findById)
                .ifPresent(patient::setPatientMaritalStatus);


        Optional.ofNullable(request.getPatientReligionId())
                .flatMap(masReligionRepository::findById)
                .ifPresent(patient::setPatientReligion);

        Optional.ofNullable(request.getPatientDistrictId())
                .flatMap(masDistrictRepository::findById)
                .ifPresent(patient::setPatientDistrict);

        Optional.ofNullable(request.getPatientStateId())
                .flatMap(masStateRepository::findById)
                .ifPresent(patient::setPatientState);

        Optional.ofNullable(request.getPatientCountryId())
                .flatMap(masCountryRepository::findById)
                .ifPresent(patient::setPatientCountry);

        Optional.ofNullable(request.getNokDistrictId())
                .flatMap(masDistrictRepository::findById)
                .ifPresent(patient::setNokDistrict);

        Optional.ofNullable(request.getNokStateId())
                .flatMap(masStateRepository::findById)
                .ifPresent(patient::setNokState);

        Optional.ofNullable(request.getNokCountryId())
                .flatMap(masCountryRepository::findById)
                .ifPresent(patient::setNokCountry);

        Optional.ofNullable(request.getNokRelationId())
                .flatMap(masRelationRepository::findById)
                .ifPresent(patient::setNokRelation);

        Optional.ofNullable(request.getPatientHospitalId())
                .flatMap(masHospitalRepository::findById)
                .ifPresent(patient::setPatientHospital);
        if(followUp){
            patient.setUhidNo(request.getUhidNo());
            patient.setId(request.getId());
        }
        else{
            patient.setUhidNo(generateUhid(patient));
        }
        patient = patientRepository.save(patient);
        return patient;
    }

    private Visit createSingleAppointment(VisitRequest visit, Patient patient) {
        validateDuplicateAppointment(visit, patient.getId(), null);
        User currentLoggedInUser = authUtil.getCurrentUser();
        LocalDate visitDate = visit.getVisitDate().atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate tokenStartTime = visit.getTokenStartTime().atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate tokenEndTime = visit.getTokenEndTime().atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        if (visitDate.isBefore(today)||visitDate.isBefore(tokenStartTime)||visitDate.isBefore(tokenEndTime)) {
            throw new InvalidDateException("Past dates are not allowed. Please select today or a future date.");
        }
        Instant startOfDay = visitDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = visitDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).minusNanos(1).toInstant();
        boolean alreadyExists =
                visitRepository.existsByDepartment_IdAndDoctor_UserIdAndVisitDateBetweenAndSession_IdAndTokenNoAndVisitStatusNot(
                        visit.getDepartmentId(),
                        visit.getDoctorId(),
                        startOfDay,
                        endOfDay,
                        visit.getSessionId(),
                        visit.getTokenNo(),
                        AppConstants.VISIT_STATUS_CANCELLED.toLowerCase()   // "c"
                );

        if (alreadyExists) {
            throw new TokenAlreadyBookedException(
                    "This token has just been booked by another user. Please select a different slot."
            );
        }
        Visit newVisit = new Visit();
        String todayDayName = visit.getVisitDate().atZone(ZoneId.systemDefault()).getDayOfWeek().name();
        List<AppSetup> optionalSetup = appSetupRepository.findByDoctorHospitalSessionAndDayName(
                visit.getDoctorId(), visit.getDepartmentId(), visit.getSessionId(), todayDayName.toLowerCase());
        if (optionalSetup.isEmpty()) {
            throw new AppSetupNotFoundException(
                    "AppSetup not configured for today’s session."
            );
        }

        AppSetup setup = optionalSetup.stream()
                .filter(s -> s.getSession().getId().equals(visit.getSessionId()))
                .findFirst()
                .orElse(null);

        newVisit.setStartTime(visit.getTokenStartTime());
        newVisit.setEndTime(visit.getTokenEndTime());
        newVisit.setTokenNo(visit.getTokenNo());
        newVisit.setVisitDate(visit.getVisitDate());
        newVisit.setLastChgDate(Instant.now());
        newVisit.setVisitStatus(AppConstants.VISIT_STATUS_PENDING.toLowerCase()); // "n"
        newVisit.setDisplayPatientStatus(AppConstants.DISPLAY_PATIENT_STATUS.toLowerCase()); // "wp"
        newVisit.setPriority(visit.getPriority());
        newVisit.setDepartment(masDepartmentRepository.getReferenceById(visit.getDepartmentId()));
        newVisit.setDoctorName(userRepository.getReferenceById(visit.getDoctorId()).getFullName());
        assert setup != null;
        if (patient.getPatientHospital().getAppCostApplicable().equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())) {
            newVisit.setBillingStatus(AppConstants.PAYMENT_PAID.toLowerCase());
        } else {
            newVisit.setBillingStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
        }
        newVisit.setVisitType(helperUtils.getVisitTypeForFollowUpOrNew(patient.getId()));
        newVisit.setPatient(patient);

        if (visit.getIniDoctorId() != null) {
            userRepository.findById(visit.getDoctorId()).ifPresent(newVisit::setIniDoctor);
        }

        if (visit.getHospitalId() != null) {
            Optional<MasHospital> hospital = masHospitalRepository.findById(visit.getHospitalId());
            if (hospital.isPresent()) {
                newVisit.setHospital(hospital.get());
                if (hospital.get().getPreConsultationAvailable().equalsIgnoreCase(AppConstants.STATUS_Y.toLowerCase())) {
                    newVisit.setPreConsultation(AppConstants.STATUS_N.toLowerCase());
                } else if (hospital.get().getPreConsultationAvailable().equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())) {
                    newVisit.setPreConsultation(AppConstants.STATUS_Y.toLowerCase());
                }
            }
        }

        if (visit.getDoctorId() != null) {
            newVisit.setDoctor(userRepository.getReferenceById(visit.getDoctorId()));
        }

        if (visit.getSessionId() != null) {
            masOpdSessionRepository.findById(visit.getSessionId()).ifPresent(newVisit::setSession);
        }

        Visit savedVisit=visitRepository.save(newVisit);
        //create billing header and detail
        MasServiceCategory serviceCategory=masServiceCategoryRepository.findByServiceCateCode(serviceCategoryOPD);
        if(AppConstants.STATUS_Y.equalsIgnoreCase(savedVisit.getHospital().getAppCostApplicable())) {
            ApiResponse<OpdBillingPaymentResponse> resp = billingService.saveBillingForOpd(savedVisit, serviceCategory, null);
            Visit v = visitRepository.getReferenceById(newVisit.getId());
            newVisit.setBillingHd(resp.getResponse().getHeader());
            visitRepository.save(newVisit);
        }
        return savedVisit;
    }
    private OpdPatientDetail addOpdDetails(Visit savedVisit, OpdPatientDetailRequest opdPatientDetailRequest, Patient patient) {
        OpdPatientDetail opdPatientDetail = new OpdPatientDetail();
        opdPatientDetail.setHeight(opdPatientDetailRequest.getHeight());
        opdPatientDetail.setIdealWeight(opdPatientDetailRequest.getIdealWeight());
        opdPatientDetail.setWeight(opdPatientDetailRequest.getWeight());
        opdPatientDetail.setPulse(opdPatientDetailRequest.getPulse());
        opdPatientDetail.setTemperature(opdPatientDetailRequest.getTemperature());
        opdPatientDetail.setOpdDate(opdPatientDetailRequest.getOpdDate());
        opdPatientDetail.setRr(opdPatientDetailRequest.getRr());
        opdPatientDetail.setBmi(opdPatientDetailRequest.getBmi());
        opdPatientDetail.setSpo2(opdPatientDetailRequest.getSpo2());
        opdPatientDetail.setVaration(opdPatientDetailRequest.getVaration());
        opdPatientDetail.setBpSystolic(opdPatientDetailRequest.getBpSystolic());
        opdPatientDetail.setBpDiastolic(opdPatientDetailRequest.getBpDiastolic());
        opdPatientDetail.setIcdDiag(opdPatientDetailRequest.getIcdDiag());
        opdPatientDetail.setWorkingDiag(opdPatientDetailRequest.getWorkingDiag());
        opdPatientDetail.setFollowUpFlag(opdPatientDetailRequest.getFollowUpFlag());
        opdPatientDetail.setFollowUpDays(opdPatientDetailRequest.getFollowUpDays());
        opdPatientDetail.setPastMedicalHistory(opdPatientDetailRequest.getPastMedicalHistory());
        opdPatientDetail.setPresentComplaints(opdPatientDetailRequest.getPresentComplaints());
        opdPatientDetail.setFamilyHistory(opdPatientDetailRequest.getFamilyHistory());
        opdPatientDetail.setTreatmentAdvice(opdPatientDetailRequest.getTreatmentAdvice());
        opdPatientDetail.setSosFlag(opdPatientDetailRequest.getSosFlag());
        opdPatientDetail.setRecmmdMedAdvice(opdPatientDetailRequest.getRecmmdMedAdvice());
        opdPatientDetail.setMedicineFlag(opdPatientDetailRequest.getMedicineFlag());
        opdPatientDetail.setLabFlag(opdPatientDetailRequest.getLabFlag());
        opdPatientDetail.setRadioFlag(opdPatientDetailRequest.getRadioFlag());
        opdPatientDetail.setReferralFlag(opdPatientDetailRequest.getReferralFlag());
        opdPatientDetail.setMlcFlag(opdPatientDetailRequest.getMlcFlag());
        opdPatientDetail.setPoliceStation(opdPatientDetailRequest.getPoliceStation());
        opdPatientDetail.setPoliceName(opdPatientDetailRequest.getPoliceName());

        // Fetch related entities using IDs
        opdPatientDetail.setPatient(patient!=null?patient:patientRepository.findById(opdPatientDetailRequest.getPatientId()).get());
        opdPatientDetail.setVisit(savedVisit!=null?savedVisit:visitRepository.findById(opdPatientDetailRequest.getVisitId()).get());
        MasDepartment department = savedVisit!=null?savedVisit.getDepartment():masDepartmentRepository.findById(opdPatientDetailRequest.getDepartmentId()).get();
        opdPatientDetail.setDepartment(department);
        opdPatientDetail.setHospital(savedVisit!=null?savedVisit.getHospital():masHospitalRepository.findById(opdPatientDetailRequest.getHospitalId()).get());
        opdPatientDetail.setDoctor(savedVisit!=null?savedVisit.getDoctor():userRepository.findById(opdPatientDetailRequest.getDoctorId()).get());
        opdPatientDetail.setLastChgDate(Instant.now());
        opdPatientDetail.setLastChgBy(opdPatientDetailRequest.getLastChgBy());
        return opdPatientDetailRepository.save(opdPatientDetail);
    }
    public OPDBillingPatientResponse buildFinalResponse(Patient patient, List<Visit> savedVisits) {
        OPDBillingPatientResponse response = new OPDBillingPatientResponse();
        List<AppointmentBlock> blocks = new ArrayList<>();
        List<BillingDetailResponse> details = new ArrayList<>();
        response.setUhid(patient.getUhidNo());
        response.setPatientid(patient.getId());
        response.setPatientName(patient.getFullName());
        response.setMobileNo(patient.getPatientMobileNumber());
        response.setAge(patient.getPatientAge());
        response.setSex(patient.getPatientGender() != null ? patient.getPatientGender().getGenderName() : null);
        response.setRelation(patient.getPatientRelation() != null ? patient.getPatientRelation().getRelationName() : null);
        response.setAddress((patient.getPatientAddress1() == null ? "" : patient.getPatientAddress1())
                + " " + (patient.getPatientAddress2() == null ? "" : patient.getPatientAddress2()));

        for (Visit sVisit : savedVisits) {
            BillingHeader billingHeader = sVisit.getBillingHd();
            if (billingHeader == null) {
                continue;
            }

            // fetch billing detail safely
            List<BillingDetail> bDetails = billingDetailRepository.findByBillingHd(billingHeader);
            BillingDetail billingDetail=null;
            for(BillingDetail bdt : bDetails){
                if(bdt.getServiceCategory().getServiceCatName().equalsIgnoreCase("Registration Service")) {
                    response.setRegistrationCost(bdt.getServiceCategory().getRegistrationCost());
                }else{
                    billingDetail = bdt;
                }
            }

            AppointmentBlock appointmentBlock = new AppointmentBlock();
            appointmentBlock.setBillingHdId(billingHeader.getId());
            appointmentBlock.setBillingPolicyId(billingHeader.getBillingPolicy().getBillingPolicyId());
            appointmentBlock.setDepartment(sVisit.getDepartment() != null ? sVisit.getDepartment().getDepartmentName() : null);
            appointmentBlock.setVisitDate(sVisit.getVisitDate());
            appointmentBlock.setVisitId(sVisit.getId());
            appointmentBlock.setVisitType(sVisit.getVisitType());
            appointmentBlock.setTokenNo(sVisit.getTokenNo());
            appointmentBlock.setSessionName(sVisit.getSession() != null ? sVisit.getSession().getSessionName() : null);
            appointmentBlock.setConsultedDoctor(sVisit.getDoctorName());

            blocks.add(appointmentBlock);

            BillingDetailResponse billingDetailResponse = new BillingDetailResponse();
            if (billingDetail != null) {
                billingDetailResponse.setId(billingDetail.getId());
                billingDetailResponse.setDiscount(billingDetail.getDiscount());
                billingDetailResponse.setRegistrationCost(billingDetail.getRegistrationCost());
                billingDetailResponse.setBasePrice(billingDetail.getBasePrice());
                billingDetailResponse.setNetAmount(billingDetail.getNetAmount());
                billingDetailResponse.setTaxAmount(billingDetail.getTaxAmount());
            } else {
                billingDetailResponse.setId(null);
                billingDetailResponse.setDiscount(BigDecimal.ZERO);
                billingDetailResponse.setRegistrationCost(BigDecimal.ZERO);
                billingDetailResponse.setBasePrice(BigDecimal.ZERO);
                billingDetailResponse.setNetAmount(BigDecimal.ZERO);
                billingDetailResponse.setTaxAmount(BigDecimal.ZERO);
            }

            details.add(billingDetailResponse);
        }

        response.setAppointments(blocks);
        response.setDetails(details);

        return response;
    }

    public ApiResponse<List<AvailableTokenSlotResponse>> getAppointmentSlots(Long deptId, Long doctorId, String appointmentDate, Long sessionId,int flag) {
        int startToken,intervalToken,totalToken,totalOnlineTokens,timeTakenMin=0;
        String startTime,endTime="";

        LocalDate date = LocalDate.parse(appointmentDate);
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        List<AppSetup> optionalSetup = appSetupRepository.findByDoctorHospitalSessionAndDayName(doctorId, deptId, sessionId, dayName);

        ApiResponse<List<DoctorRosterDTO>> checkDoctorRoaster = doctorRosterServices.getDoctorRoster(deptId,doctorId,date,sessionId);
        if(!checkDoctorRoaster.getMessage().equalsIgnoreCase("success")){
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},checkDoctorRoaster.getMessage(),400);

        }

        AppSetup appSetup = optionalSetup.get(0);
        if (appSetup == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"App setup not defined for this day",400);
        }

        if (appSetup.getStartToken() == null ||
                appSetup.getTotalInterval() == null ||
                appSetup.getTotalToken() == null||
                appSetup.getStartTime()==null||
                appSetup.getEndTime()==null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"App setup not defined for this day (Missing Token/Interval data)",400);
        }else {
            startToken = appSetup.getStartToken();
            intervalToken = appSetup.getTotalInterval();
            totalToken = appSetup.getTotalToken();
            totalOnlineTokens = (appSetup.getTotalOnlineToken() != null) ? appSetup.getTotalOnlineToken() : 0;
            timeTakenMin = appSetup.getTimeTaken();
            startTime = appSetup.getStartTime();
            endTime = appSetup.getEndTime();
        }

        Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Set<Long> occupiedTokens = new HashSet<>();
        try {
            occupiedTokens = new HashSet<>(visitRepository.findOccupiedTokens(deptId, doctorId, sessionId, startOfDay, endOfDay,AppConstants.VISIT_STATUS_PENDING,AppConstants.VISIT_STATUS_COMPLETED));
        } catch (Exception e) {
            log.error("Error fetching occupied tokens", e);
        }

        List<AvailableTokenSlotResponse> list = generateSlotsWithAvailability(
                startToken, intervalToken, totalToken,
                startTime, endTime, timeTakenMin, occupiedTokens,flag);

        return ResponseUtils.createSuccessResponse(list, new TypeReference<List<AvailableTokenSlotResponse>>() {});
    }

    @Override
    public ApiResponse<List<?>> getAppointmentSummaryReport(Long hospitalId, Long departmentId, Long doctorId, LocalDate fromDate, LocalDate toDate, Integer flag
    ) {
        try {
            log.info("Processing appointment summary report with hospitalId: {}, departmentId: {}, doctorId: {}, fromDate: {}, toDate: {}, flag: {}",
                    hospitalId, departmentId, doctorId, fromDate, toDate, flag);
            if (flag == 0) {
                return getAppointmentSummaryDepartmentWiseReport(hospitalId, departmentId, fromDate, toDate);
            }
            if (flag == 1) {
                log.info("Fetching doctor wise appointment summary report for hospitalId: {}, departmentId: {}, doctorId: {}, fromDate: {}, toDate: {}",
                        hospitalId, departmentId, doctorId, fromDate, toDate);

                return getAppointmentSummaryDoctorWiseReport(hospitalId,departmentId, doctorId, fromDate, toDate);
            }

            return ResponseUtils.createFailureResponse(List.of(), new TypeReference<>() {}, "Invalid flag. Use 0 for department summary and 1 for doctor summary",
                    HttpStatus.BAD_REQUEST
            );

        } catch (Exception e) {
            log.error("Error while fetching appointment summary report", e);
            return ResponseUtils.createFailureResponse(
                    List.of(),
                    new TypeReference<>() {},
                    "Something went wrong while fetching appointment summary report",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private ApiResponse<List<?>> getAppointmentSummaryDepartmentWiseReport(Long hospitalId, Long departmentId, LocalDate fromDate, LocalDate toDate) {
        try {
            log.info("Fetching department wise appointment summary report for hospitalId: {}, departmentId: {}, fromDate: {}, toDate: {}",
                    hospitalId, departmentId, fromDate, toDate);
            if (hospitalId == null) {
                return ResponseUtils.createFailureResponse(List.of(), new TypeReference<>() {}, "hospitalId is required", 400
                );
            }
            List<AppointmentSummaryDepartmentProjection> list = visitRepository.getAppointmentSummaryDepartmentWiseReport(
                    hospitalId, departmentId, fromDate, toDate,
                    AppConstants.VISIT_STATUS_PENDING,
                    AppConstants.VISIT_STATUS_CANCELLED,
                    AppConstants.VISIT_STATUS_COMPLETED,
                    AppConstants.VISIT_STATUS_CLOSED,
                    AppConstants.VISIT_TYPE_FOLLOW_UP,
                    AppConstants.VISIT_TYPE_NEW);

            List<AppointmentSummaryDepartmentResponse> responseList = list.stream().map(item -> {
                AppointmentSummaryDepartmentResponse response = new AppointmentSummaryDepartmentResponse();
                response.setDepartmentId(item.getDepartmentId());
                response.setDepartmentName(item.getDepartmentName());
                response.setTotalCount(item.getTotalCount());
                response.setCompletedCount(item.getCompletedCount());
                response.setCancelledCount(item.getCancelledCount());
                response.setNoShowCount(item.getNoShowCount());
                response.setPendingCount(item.getPendingCount());
                return response;
            }).toList();
            log.info("Department wise appointment summary response prepared successfully. Response count: {}", responseList.size());
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while fetching doctor wise appointment summary report", e);
            return ResponseUtils.createFailureResponse(
                    List.of(),
                    new TypeReference<>() {},
                    "Something went wrong while fetching doctor wise appointment summary report",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    private ApiResponse<List<?>> getAppointmentSummaryDoctorWiseReport(Long hospitalId,Long departmentId, Long doctorId, LocalDate fromDate, LocalDate toDate) {
        try {
            log.info("Processing doctor wise appointment summary report with hospitalId: {}, doctorId: {}, fromDate: {}, toDate: {}",
                    hospitalId, doctorId, fromDate, toDate);

            if (hospitalId == null) {
                return ResponseUtils.createFailureResponse(List.of(), new TypeReference<>() {}, "hospitalId is required", 400
                );
            }

            List<AppointmentSummaryDoctorProjection> list = visitRepository.getAppointmentSummaryDoctorWiseReport(hospitalId,departmentId, doctorId, fromDate, toDate, AppConstants.VISIT_STATUS_PENDING, AppConstants.VISIT_STATUS_CANCELLED, AppConstants.VISIT_STATUS_COMPLETED, AppConstants.VISIT_STATUS_CLOSED, AppConstants.VISIT_TYPE_FOLLOW_UP, AppConstants.VISIT_TYPE_NEW);

            List<AppointmentSummaryDoctorResponse> responseList = list.stream().map(item -> {
                AppointmentSummaryDoctorResponse response = new AppointmentSummaryDoctorResponse();
                response.setDoctorId(item.getDoctorId());
                response.setDoctorName(item.getDoctorName());
                response.setTotalCount(item.getTotalCount());
                response.setCompletedCount(item.getCompletedCount());
                response.setCancelledCount(item.getCancelledCount());
                response.setNoShowCount(item.getNoShowCount());
                response.setPendingCount(item.getPendingCount());
                return response;
            }).toList();
            log.info("Doctor wise appointment summary response prepared successfully. Response count: {}", responseList.size());
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while fetching doctor wise appointment summary report", e);
            return ResponseUtils.createFailureResponse(
                    List.of(),
                    new TypeReference<>() {},
                    "Something went wrong while fetching doctor wise appointment summary report",
                    500
            );
        }
    }


    public static List<AvailableTokenSlotResponse> generateSlotsWithAvailability(int tokenStart,int tokenInterval, int totalTokens, String dayStartTime, String dayEndTime, int timeTakenMin, Set<Long> occupiedTokenNumbers, int flag) {

        List<AvailableTokenSlotResponse> slots = new ArrayList<>();

        if (totalTokens <= 0 || timeTakenMin <= 0) {
            return slots;
        }

        LocalTime start = LocalTime.parse(dayStartTime);
        LocalTime end = LocalTime.parse(dayEndTime);

        int slotIndex = 0;

        for (int tokenNum = tokenStart; tokenNum <= totalTokens; tokenNum++) {

            LocalTime slotStart = start.plusMinutes(slotIndex * timeTakenMin);
            LocalTime slotEnd = slotStart.plusMinutes(timeTakenMin);

            if (!slotStart.isBefore(end) || slotEnd.isAfter(end)) {
                break;
            }

            boolean isOnline = tokenInterval > 0 && tokenNum % tokenInterval == 0;
            boolean isAvailable = !occupiedTokenNumbers.contains((long) tokenNum);

            boolean shouldAdd =
                    tokenInterval == 0 || (flag == 0 && !isOnline) || (flag == 1 && isOnline);

            if (shouldAdd) {
                slots.add(new AvailableTokenSlotResponse(
                        tokenNum,
                        slotStart,
                        slotEnd,
                        isAvailable
                ));
            }
            slotIndex++;
        }
        return slots;
    }



    private Patient updatePatientDetails(PatientRequest request, boolean followUp) {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            log.info("current user not found");
            throw new RuntimeException("Current user not found");
        }
        Patient patient = patientRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + request.getId()));

        patient.setUhidNo(request.getUhidNo());
        patient.setUpdatedOn(Instant.now());
        patient.setLastChgBy(currentUser.getFirstName() + " " +
                currentUser.getMiddleName() + " " +
                currentUser.getLastName());
        patient.setPatientFn(request.getPatientFn());
        patient.setPatientMn(request.getPatientMn());
        patient.setPatientLn(request.getPatientLn());
        patient.setPatientDob(request.getPatientDob());
        patient.setPatientAge(request.getPatientAge());
        patient.setPatientEmailId(request.getPatientEmailId());
        patient.setPatientMobileNumber(request.getPatientMobileNumber());
        patient.setPatientImage(request.getPatientImage());
        patient.setFileName(request.getFileName());
        patient.setPatientAddress1(request.getPatientAddress1());
        patient.setPatientAddress2(request.getPatientAddress2());
        patient.setPatientCity(request.getPatientCity());
        patient.setPatientPincode(request.getPatientPincode());
        patient.setPincode(request.getPincode());
        patient.setEmerFn(request.getEmerFn());
        patient.setEmerLn(request.getEmerLn());
        patient.setEmerMobile(request.getEmerMobile());
        patient.setNokFn(request.getNokFn());
        patient.setNokLn(request.getNokLn());
        patient.setNokEmail(request.getNokEmail());
        patient.setNokMobileNumber(request.getNokMobileNumber());
        patient.setNokAddress1(request.getNokAddress1());
        patient.setNokAddress2(request.getNokAddress2());
        patient.setNokCity(request.getNokCity());
        patient.setNokPincode(request.getNokPincode());
        patient.setPatientStatus(request.getPatientStatus());
        patient.setRegDate(request.getRegDate());
        patient.setPatientAbhaId(request.getPatientAbhaId());

        Optional.ofNullable(request.getPatientGenderId())
                .flatMap(masGenderRepository::findById)
                .ifPresent(patient::setPatientGender);

        Optional.ofNullable(request.getPatientRelationId())
                .flatMap(masRelationRepository::findById)
                .ifPresent(patient::setPatientRelation);

        Optional.ofNullable(request.getPatientMaritalStatusId())
                .flatMap(masMaritalStatusRepository::findById)
                .ifPresent(patient::setPatientMaritalStatus);

        Optional.ofNullable(request.getPatientReligionId())
                .flatMap(masReligionRepository::findById)
                .ifPresent(patient::setPatientReligion);

        Optional.ofNullable(request.getPatientDistrictId())
                .flatMap(masDistrictRepository::findById)
                .ifPresent(patient::setPatientDistrict);

        Optional.ofNullable(request.getPatientStateId())
                .flatMap(masStateRepository::findById)
                .ifPresent(patient::setPatientState);

        Optional.ofNullable(request.getPatientCountryId())
                .flatMap(masCountryRepository::findById)
                .ifPresent(patient::setPatientCountry);

        Optional.ofNullable(request.getNokDistrictId())
                .flatMap(masDistrictRepository::findById)
                .ifPresent(patient::setNokDistrict);

        Optional.ofNullable(request.getNokStateId())
                .flatMap(masStateRepository::findById)
                .ifPresent(patient::setNokState);

        Optional.ofNullable(request.getNokCountryId())
                .flatMap(masCountryRepository::findById)
                .ifPresent(patient::setNokCountry);

        Optional.ofNullable(request.getNokRelationId())
                .flatMap(masRelationRepository::findById)
                .ifPresent(patient::setNokRelation);

        if (!followUp) {
            Optional.ofNullable(request.getPatientHospitalId())
                    .flatMap(masHospitalRepository::findById)
                    .ifPresent(patient::setPatientHospital);
        }

        return patientRepository.save(patient);
    }
    private Visit updateExistingVisitById(VisitRequest visit, Patient patient) {
        if (visit.getId() == null) {
            throw new RuntimeException("Visit ID is required for updating existing visit");
        }

        validateDuplicateAppointment(visit, patient.getId(), visit.getId());

        Visit existingVisit = visitRepository.findById(visit.getId())
                .orElseThrow(() -> new RuntimeException("Visit not found with id: " + visit.getId()));


        log.info("Updating existing visit ID: {} for patient: {}",
                existingVisit.getId(), patient.getId());

        existingVisit.setLastChgDate(Instant.now());
        existingVisit.setPriority(visit.getPriority());
        existingVisit.setVisitType(visit.getVisitType());

        if (visit.getDepartmentId() != null) {
            masDepartmentRepository.findById(visit.getDepartmentId())
                    .ifPresent(existingVisit::setDepartment);
        }

        if (visit.getDoctorId() != null) {
            userRepository.findById(visit.getDoctorId()).ifPresent(doctor -> {
                existingVisit.setDoctor(doctor);
                existingVisit.setDoctorName(visit.getDoctorName());
            });
        }

        if (visit.getSessionId() != null) {
            masOpdSessionRepository.findById(visit.getSessionId())
                    .ifPresent(existingVisit::setSession);
        }

        if (visit.getVisitDate() != null) {
            existingVisit.setVisitDate(visit.getVisitDate());
        }

        return visitRepository.save(existingVisit);
    }

    private void validateDuplicateAppointments(List<VisitRequest> visitList, Long patientId) {
        if (visitList == null || visitList.isEmpty() || patientId == null) {
            return;
        }
        Map<String, Long> seenAppointments = new HashMap<>();
        for (VisitRequest visit : visitList) {
            if (visit == null) {
                continue;
            }
            if (visit.getDoctorId() == null || visit.getVisitDate() == null) {
                continue;
            }
            LocalDate visitDate = visit.getVisitDate().atZone(ZoneOffset.UTC).toLocalDate();
            String appointmentKey = visit.getDoctorId() + "|" + visitDate +"|" + visit.getDepartmentId() + "|" + visit.getPatientId();
            Long currentVisitId = visit.getId();

            if (seenAppointments.containsKey(appointmentKey)) {
                throw new ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT,
                        AppConstants.DUPLICATE_APPOINTMENT_MSG
                );
            } else {
                seenAppointments.put(appointmentKey, currentVisitId);
            }
        }
    }

    private void validateDuplicateAppointment(VisitRequest visit, Long patientId, Long excludeVisitId) {
        if (visit == null|| patientId == null
                || visit.getDoctorId() == null|| visit.getDepartmentId() == null
                || visit.getVisitDate() == null) {
            return;
        }
        LocalDate visitDate = visit.getVisitDate().atZone(ZoneOffset.UTC).toLocalDate();
        Instant startOfDay = visitDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = visitDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).minusNanos(1).toInstant();

        boolean duplicateExists =
                visitRepository.existsDuplicatePatientAppointment(
                        patientId,
                        visit.getDoctorId(),
                        visit.getDepartmentId(),
                        startOfDay,
                        endOfDay,
                        AppConstants.VISIT_STATUS_CANCELLED,
                        excludeVisitId
                );

        if (duplicateExists) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    AppConstants.DUPLICATE_APPOINTMENT_MSG
            );
        }
    }

    private String cleanStringParameter(String param) {
        if (param == null || param.trim().isEmpty()) {
            return null;
        }
        return param.trim();
    }


    private CancelledAppointmentResponse mapCancelledAppointmentProjectionToDto(
            com.hims.projection.CancelledAppointmentProjection projection) {

        if (projection == null) {
            return null;
        }

        CancelledAppointmentResponse dto = new CancelledAppointmentResponse();
        dto.setVisitId(projection.getVisitId());
        dto.setPatientId(projection.getPatientId());
        dto.setPatientName(projection.getPatientName() != null ? projection.getPatientName().trim() : null);
        dto.setMobileNumber(projection.getMobileNumber());
        dto.setAge(projection.getPatientAge());
        dto.setGender(projection.getGender());
        dto.setDoctorId(projection.getDoctorId());
        dto.setDoctorName(projection.getDoctorName());
        dto.setDepartmentId(projection.getDepartmentId());
        dto.setDepartmentName(projection.getDepartmentName());
        dto.setAppointmentDate(projection.getAppointmentDate());
        dto.setAppointmentTime(projection.getAppointmentTime());  // Combined time "HH:MM to HH:MM"
        dto.setCancellationDateTime(projection.getCancellationDateTime());
        dto.setCancelledBy(projection.getCancelledBy());
        dto.setCancellationReason(projection.getCancellationReason());

        return dto;
    }

    private String generateUhid(Patient patient) {
        List<Patient> existing = patientRepository.findByPatientMobileNumberAndPatientRelation(patient.getPatientMobileNumber(), patient.getPatientRelation());
        return (patient.getPatientMobileNumber() + patient.getPatientRelation().getCode() + (existing.size() + 1));
    }

    /**
     * Helper method to map personal details
     */
    private FollowUpPatientResponseDetails.PersonalDetails mapPersonalDetails(PatientProjectionFollowUpDetails p) {
        FollowUpPatientResponseDetails.PersonalDetails personal = new FollowUpPatientResponseDetails.PersonalDetails();
        personal.setFirstName(p.getFirstName());
        personal.setMiddleName(p.getMiddleName());
        personal.setLastName(p.getLastName());
        personal.setMobileNo(p.getMobileNo());
        personal.setEmail(p.getEmail());
        personal.setDob(p.getDob());
        personal.setAge(ConverterUtils.ageCalculator(p.getDob()));
        personal.setGender(p.getGenderId());
        personal.setGenderName(p.getGenderName());
        personal.setRelation(p.getRelationId());
        personal.setRelationName(p.getRelationName());
        personal.setPatientAbhaId(p.getPatientAbhaId());
        return personal;
    }
    /**
     * Helper method to map address details
     */
    private FollowUpPatientResponseDetails.AddressDetails mapAddressDetails(PatientProjectionFollowUpDetails p) {
        FollowUpPatientResponseDetails.AddressDetails address = new FollowUpPatientResponseDetails.AddressDetails();
        address.setAddress1(p.getAddress1());
        address.setAddress2(p.getAddress2());
        address.setCity(p.getCity());
        address.setPinCode(p.getPinCode());
        address.setCountry(p.getCountryId());
        address.setCountryName(p.getCountryName());
        address.setState(p.getStateId());
        address.setStateName(p.getStateName());
        address.setDistrict(p.getDistrictId());
        address.setDistrictName(p.getDistrictName());
        return address;
    }
    /**
     * Helper method to map NOK (Next of Kin) details
     */
    private FollowUpPatientResponseDetails.NokDetails mapNokDetails(PatientProjectionFollowUpDetails p) {
        FollowUpPatientResponseDetails.NokDetails nok = new FollowUpPatientResponseDetails.NokDetails();
        nok.setFirstName(p.getNokFirstName());
        nok.setLastName(p.getNokLastName());
        nok.setEmail(p.getNokEmail());
        nok.setMobileNo(p.getNokMobile());
        nok.setAddress1(p.getNokAddress1());
        nok.setAddress2(p.getNokAddress2());
        nok.setCity(p.getNokCity());
        nok.setPinCode(p.getNokPinCode());
        nok.setCountry(p.getNokCountryId());
        nok.setCountryName(p.getNokCountryName());
        nok.setState(p.getNokStateId());
        nok.setStateName(p.getNokStateName());
        nok.setDistrict(p.getNokDistrictId());
        nok.setDistrictName(p.getNokDistrictName());
        return nok;
    }
    /**
     * Helper method to map emergency details
     */
    private FollowUpPatientResponseDetails.EmergencyDetails mapEmergencyDetails(PatientProjectionFollowUpDetails p) {
        FollowUpPatientResponseDetails.EmergencyDetails emergency = new FollowUpPatientResponseDetails.EmergencyDetails();
        emergency.setFirstName(p.getEmerFirstName());
        emergency.setLastName(p.getEmerLastName());
        emergency.setMobileNo(p.getEmerMobile());
        return emergency;
    }
    /**
     * Helper method to map vital details
     */
    private FollowUpPatientResponseDetails.VitalDetails mapVitalDetails(PatientVitalsProjection opd) {
        FollowUpPatientResponseDetails.VitalDetails vitals = new FollowUpPatientResponseDetails.VitalDetails();
        vitals.setHeight(opd.getHeight());
        vitals.setWeight(opd.getWeight());
        vitals.setTemperature(opd.getTemperature());
        vitals.setBpSys(opd.getBpSystolic());
        vitals.setBpDia(opd.getBpDiastolic());
        vitals.setPulse(opd.getPulse());
        vitals.setRr(opd.getRr());
        vitals.setSpo2(opd.getSpo2());
        vitals.setBmi(opd.getBmi());
        return vitals;
    }
    /**
     * Helper method to map appointment details
     */
    private List<FollowUpPatientResponseDetails.AppointmentDetailResponse> mapAppointmentDetails(List<AppointmentProjection> visits) {
        return visits.stream().map(v -> {
            FollowUpPatientResponseDetails.AppointmentDetailResponse appt = new FollowUpPatientResponseDetails.AppointmentDetailResponse();
            appt.setAppointmentId(v.getAppointmentId());
            appt.setSpecialityId(v.getSpecialityId());
            appt.setSpecialityName(v.getSpecialityName());
            appt.setDoctorId(v.getDoctorId());
            appt.setDoctorName(v.getDoctorName());
            appt.setSessionId(v.getSessionId());
            appt.setSessionName(v.getSessionName());
            appt.setVisitDate(v.getVisitDate());
            appt.setVisitType(v.getVisitType());
            appt.setTokenNo(v.getTokenNo());
            appt.setVisitStatus(AppConstants.VISIT_STATUS_COMPLETED.equalsIgnoreCase(v.getVisitStatus()) ? "Completed" : "Pending");

            if (v.getStartTime() != null) {
                appt.setTokenStartTime(HelperUtils.extractTimeFromInstant(v.getStartTime()));
            }
            if (v.getEndTime() != null) {
                appt.setTokenEndTime(HelperUtils.extractTimeFromInstant(v.getEndTime()));
            }
            return appt;
        }).toList();
    }


    @Override
    public boolean checkDuplicatePatient(String firstName, LocalDate dob, Long gender,
            String mobile,
            Long relation) {

        log.info(
                "Checking duplicate patient: firstName={}, dob={}, gender={}, mobile={}, relation={}",
                firstName, dob, gender, mobile, relation
        );

        String normalizedFirstName = firstName == null ? "" : firstName.trim();
        String normalizedMobile = mobile == null ? "" : mobile.trim();


        boolean exists = patientRepository
                        .existsByPatientFnIgnoreCaseAndPatientDobAndPatientGenderIdAndPatientMobileNumberAndPatientRelationId(
                                normalizedFirstName,
                                dob,
                                gender,
                                normalizedMobile,
                                relation
                        );

        log.info(
                "Duplicate patient check result: firstName={}, mobile={}, exists={}",
                normalizedFirstName,
                normalizedMobile,
                exists
        );
        return exists;
    }

}

