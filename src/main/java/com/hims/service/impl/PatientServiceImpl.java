package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.RecordNotFoundException;
import com.hims.exception.SDDException;
import com.hims.exception.patientRegistrationException.AppSetupNotFoundException;
import com.hims.exception.patientRegistrationException.TokenAlreadyBookedException;
import com.hims.helperUtil.HelperUtils;
import com.hims.mapper.OpdPatientDetailMapper;
import com.hims.mapper.PatientMapper;
import com.hims.mapper.VisitMapper;
import com.hims.projection.CancelledAppointmentProjection;
import com.hims.projection.OpdPreConsultationProjection;
import com.hims.projection.PatientProjection;
import com.hims.projection.PatientWaitingListProjection;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.*;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import kong.unirest.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {
    private static final String UPLOAD_DIR = "patientImage/";
    private static final Logger log = LoggerFactory.getLogger(PatientServiceImpl.class);
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
    VisitRepository visitRepository;
    @Autowired
    MasOpdSessionRepository masOpdSessionRepository;
    @Autowired
    AppSetupRepository appSetupRepository;

    @Autowired
    PaymentDetailRepository paymentDetailRepository;

    @Autowired
    PatientMapper patientMapper;

    @Autowired
    OpdPatientDetailMapper opdPatientDetailMapper;

    @Autowired
    VisitMapper visitMapper;

    @Autowired
    DoctorRosterServices doctorRosterServices;


    @Value("${upload.image.path}")
    private String baseUrl;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private OpdPatientDetailRepository opdPatientDetailRepository;
    @Autowired
    private MasServiceCategoryRepository masServiceCategoryRepository;

    @Autowired
    private BillingHeaderRepository billingHeaderRepository;
    @Autowired
    private BillingDetailRepository billingDetailRepository;

    @Autowired
    private AuthUtil authUtil;

    @Value("${serviceCategoryOPD}")
    private String serviceCategoryOPD;

    @Autowired
    private PatientLoginService patientLoginService;

    @Autowired
    private MasAppointmentChangeReasonRepository changeReasonRepository;

    @Autowired
    private RescheduleHistoryRepository historyRepository;

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RestClient.Builder builder;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<PatientRegFollowUpResp> registerPatientWithOpd(PatientRequest request, OpdPatientDetailRequest opdPatientDetailRequest, List<VisitRequest> visit) {
        PatientRegFollowUpResp resp=new PatientRegFollowUpResp();
        Optional<Patient> existingPatient = patientRepository.findByUniqueCombination(
                request.getPatientFn(),
                request.getPatientLn(),
                (masGenderRepository.findById(request.getPatientGenderId())).get(),
                request.getPatientDob() != null ? request.getPatientDob() : null,
                request.getPatientAge(),
                request.getPatientMobileNumber(),
                (masRelationRepository.findById(request.getPatientRelationId())).get());
        if (existingPatient.isPresent()) {
            resp.setPatient(PatientMapper.mapToDTO(existingPatient.get()));
            return ResponseUtils.createFailureResponse(resp, new TypeReference<>() {
                    },
                    "Patient already Registered", 500);
        }
        Patient patient = savePatient(request,false);
        patientLoginService.savePatientLogin(patient);
        resp.setPatient(PatientMapper.mapToDTO(patient));
        OpdPatientDetail newOpd=new OpdPatientDetail();
        if(visit!=null){
            List<Visit> savedVisits = new ArrayList<>();
            if (!visit.isEmpty()) {
                for (VisitRequest v : visit) {
                    Instant today = v.getVisitDate();
                    String visitType = getVisitTypeForFollowUpOrNew(patient.getId(), today);
                    v.setVisitType(visitType);
                    Visit saved = createSingleAppointment(v, patient);
                    savedVisits.add(saved);

                    if (saved.getHospital().getPreConsultationAvailable().equalsIgnoreCase("n")) {
                        newOpd = addOpdDetails(saved, opdPatientDetailRequest, patient);
                        }
                    }
            }
            if(savedVisits.get(0).getBillingStatus().equalsIgnoreCase("n")){
                List<OpdVisitResponseDTO> visitResponses = savedVisits.stream()
                        .map(visitMapper::mapToDTO)
                        .toList();
                resp.setVisits(visitResponses);
            }
            OPDBillingPatientResponse finalResponse =  buildFinalResponse(patient,savedVisits);
            resp.setOpdBillingPatientResponse(finalResponse);
        }
        resp.setOpdPatientDetail(opdPatientDetailMapper.mapToDTO(newOpd));
        return ResponseUtils.createSuccessResponse(resp, new TypeReference<>() {
        });
    }


    private String getVisitTypeForFollowUpOrNew(Long patientId, Instant visitDate) {
        int count = visitRepository.countByPatientIdAndVisitDate(patientId, visitDate);
        return count > 0 ? "F" : "N";
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

    @Override
    @Transactional
    public ApiResponse<PaymentResponse> paymentStatusReq(PaymentUpdateRequest request) {
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

            patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));

        } else {
            throw new RuntimeException("Patient ID is required");
        }

        resp.setPatient(PatientMapper.mapToDTO(patient));

        if (followUpRequest.isAppointmentFlag()) {
            List<VisitRequest> visitList = details.getVisits();
            OpdPatientDetailRequest opdReq = details.getOpdPatientDetail();
            List<Visit> updatedVisits = new ArrayList<>();
            OpdPatientDetail opdDetails = null;

            if (visitList != null && !visitList.isEmpty()) {
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
                        v.setVisitType("F");
                    }
                    Visit visit;
                    if (v.getId() != null) {
                        visit = updateExistingVisitById(v, patient);
                    } else {
                        visit = createSingleAppointment(v, patient);
                    }
                    updatedVisits.add(visit);
                    if (visit.getHospital().getPreConsultationAvailable()
                            .equalsIgnoreCase("n")) {
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


    private Visit updateExistingVisitById(VisitRequest visit, Patient patient) {
        if (visit.getId() == null) {
            throw new RuntimeException("Visit ID is required for updating existing visit");
        }

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

    public Patient updatePatientDetails(PatientRequest request, boolean followUp) {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            log.info("current user not found");
            throw new RuntimeException("Current user not found");
        }
        Patient patient = patientRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + request.getId()));

        patient.setUpdatedOn(Instant.now());
        patient.setUhidNo(patient.getUhidNo());
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


    @Override
    public ApiResponse<String> uploadImage(MultipartFile file) {
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
    public ApiResponse<List<Patient>> searchPatient(PatientSearchReq req) {

        String mobileNo = cleanStringParameter(req.getMobileNo());
        String patientName = cleanStringParameter(req.getPatientName());
        String uhidNo = cleanStringParameter(req.getUhidNo());
        LocalDate appointmentDate = req.getAppointmentDate();

        List<Patient> patientList;

        if (patientName != null) {
            patientList = patientRepository.searchPatients(mobileNo, patientName, uhidNo, appointmentDate);
        } else {
            patientList = patientRepository.searchPatients(mobileNo,patientName, uhidNo);
        }

        return ResponseUtils.createSuccessResponse(patientList, new TypeReference<>() {});
    }

    private String cleanStringParameter(String param) {
        if (param == null || param.trim().isEmpty()) {
            return null;
        }
        return param.trim();
    }
//    @Override
//    public ApiResponse<List<OpdPreConsultationResponse>> getPendingPreConsultations() {
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        User current_user=userRepository.findByUserName(username);
//        List<OpdPreConsultationProjection> projections = visitRepository.findPendingPreConsultationsByHospital(current_user.getHospital().getId(),"n","y");
//
//        // Convert projections to response objects
//        List<OpdPreConsultationResponse> response = projections.stream().map(proj -> {
//            OpdPreConsultationResponse resp = new OpdPreConsultationResponse();
//            resp.setVisitId(proj.getVisitId());
//            resp.setPatientId(proj.getPatientId());
//            resp.setPatientName(proj.getPatientName());
//            resp.setAge(proj.getPatientAge());
//            resp.setGender(proj.getGender());
//            resp.setDepartmentId(String.valueOf(proj.getDepartmentId()));
//            resp.setDepartmentName(proj.getDepartmentName());
//            resp.setMobleNumber(proj.getMobileNumber());
//            resp.setVisitType(proj.getVisitType());
//            resp.setDoctorId(proj.getDoctorId());
//            resp.setDoctorName(proj.getDoctorName());
//            resp.setAppointmentDate(proj.getAppointmentDate() != null ? proj.getAppointmentDate().toString() : "");
//            resp.setAppointmentTime(proj.getAppointmentTime());
//            resp.setTokenNumber(String.valueOf(proj.getTokenNumber()));
//            return resp;
//        }).collect(Collectors.toList());
//
//        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
//        });
//    }




//    @Override
//    public ApiResponse<List<PatientWaitingListResponse>> getWaitingList() {
//
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        User currentUser = userRepository.findByUserName(username);
//
//        List<PatientWaitingListResponse> responseList =
//                visitRepository.findWaitingPatientsByHospital(
//                        currentUser.getHospital().getId(),
//                        AppConstants.STATUS_Y.toLowerCase(),
//                        AppConstants.STATUS_Y.toLowerCase()
//                ).stream().map(p -> {
//                    PatientWaitingListResponse resp = new PatientWaitingListResponse();
//                    resp.setTokenNo(String.valueOf(p.getTokenNo()));
//                    resp.setPatientNo(p.getMobileNumber());
//                    resp.setPatientName(p.getPatientName());
//                    resp.setRelation(p.getRelation());
//                    resp.setAge(p.getDob());
//                    resp.setGender(p.getGender());
//                    resp.setVisitType(p.getVisitType());
//                    return resp;
//                }).collect(Collectors.toList());
//
//        return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});
//    }

    @Override
    public ApiResponse<String> saveVitalDetails(OpdPatientDetailRequest request) {
        Patient patient = patientRepository.getReferenceById(request.getPatientId());

        Visit visit = visitRepository.getReferenceById(request.getVisitId());

        OpdPatientDetail savedDetails=addOpdDetails(visit,request,patient);
        visit.setPreConsultation("y");
        visitRepository.save(visit);
        if(savedDetails!=null){
            return ResponseUtils.createSuccessResponse("success", new TypeReference<String>() {
            });
        }
        else {
            return ResponseUtils.createFailureResponse("error", new TypeReference<String>() {},"Error saving data",500);
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


    private Visit createSingleAppointment(VisitRequest visit, Patient patient) {
        LocalDate date = visit.getVisitDate().atOffset(ZoneOffset.UTC).toLocalDate();

        Instant startOfDay = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).minusNanos(1).toInstant();
        boolean alreadyExists =
                visitRepository.existsByDepartment_IdAndDoctor_UserIdAndVisitDateBetweenAndSession_IdAndTokenNoAndVisitStatusNot(
                        visit.getDepartmentId(),
                        visit.getDoctorId(),
                        startOfDay,
                        endOfDay,
                        visit.getSessionId(),
                        visit.getTokenNo(),
                        AppConstants.VISIT_STATUS_CANCELLED.toLowerCase()  // "c"
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
        newVisit.setVisitStatus(AppConstants.VISIT_STATUS_PENDING.toLowerCase());
        newVisit.setDisplayPatientStatus(AppConstants.DISPLAY_PATIENT_STATUS);
        newVisit.setPriority(visit.getPriority());
        newVisit.setDepartment(masDepartmentRepository.getReferenceById(visit.getDepartmentId()));
        newVisit.setDoctorName(userRepository.getReferenceById(visit.getDoctorId()).getFullName());
        assert setup != null;
        if(setup.getHospital().getAppCostApplicable().equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())){
            newVisit.setBillingStatus(AppConstants.PAYMENT_PAID.toLowerCase());
        }else{
            newVisit.setBillingStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase());
        }
        newVisit.setVisitType(visit.getVisitType());
        newVisit.setPatient(patient);

        if (visit.getDoctorId() != null) {
            userRepository.findById(visit.getDoctorId()).ifPresent(newVisit::setDoctor);
        }

        if (visit.getHospitalId() != null) {
            Optional<MasHospital> hospital=masHospitalRepository.findById(visit.getHospitalId());
            if(hospital.isPresent()){
                newVisit.setHospital(hospital.get());
                if(hospital.get().getPreConsultationAvailable().equalsIgnoreCase(AppConstants.STATUS_Y.toLowerCase())){
                    newVisit.setPreConsultation(AppConstants.STATUS_N.toLowerCase());
                } else if (hospital.get().getPreConsultationAvailable().equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())) {
                    newVisit.setPreConsultation(AppConstants.STATUS_Y.toLowerCase());
                }
            }
        }

        if (visit.getIniDoctorId() != null) {
            assert visit.getDoctorId() != null;
            userRepository.findById(visit.getDoctorId()).ifPresent(newVisit::setIniDoctor);
        }

        if (visit.getSessionId() != null) {
            masOpdSessionRepository.findById(visit.getSessionId()).ifPresent(newVisit::setSession);
        }

        Visit savedVisit=visitRepository.save(newVisit);
        //create billing header and detail
        MasServiceCategory serviceCategory=masServiceCategoryRepository.findByServiceCateCode(serviceCategoryOPD);
        MasDiscount discount=new MasDiscount();
        ApiResponse<OpdBillingPaymentResponse> resp=billingService.saveBillingForOpd(savedVisit,serviceCategory,null);
        newVisit.setBillingHd(resp.getResponse().getHeader());
        visitRepository.save(newVisit);
        return savedVisit;
    }

    private String generateUhid(Patient patient) {
        List<Patient> existing = patientRepository.findByPatientMobileNumberAndPatientRelation(patient.getPatientMobileNumber(), patient.getPatientRelation());
        return (patient.getPatientMobileNumber() + patient.getPatientRelation().getCode() + (existing.size() + 1));
    }

/*    private Long getNextAvailableToken(List<Long> existingTokens, int startToken, int maxToken) {
        int expected = startToken;
        for (Long token : existingTokens) {
            if (token > maxToken) break;
            if (token != expected) return (long) expected;
            expected++;
        }
        if (expected > maxToken) {
            throw new IllegalStateException("All tokens are already assigned.");
        }
        return (long) expected;
    }*/


    @Override
    public ApiResponse<FollowUpPatientResponseDetails> getAllFollowUpDetails(Long patientId) {
        FollowUpPatientResponseDetails resp = new FollowUpPatientResponseDetails();
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));

        FollowUpPatientResponseDetails.PersonalDetails personal = new FollowUpPatientResponseDetails.PersonalDetails();
        personal.setFirstName(patient.getPatientFn());
        personal.setMiddleName(patient.getPatientMn());
        personal.setLastName(patient.getPatientLn());
        personal.setMobileNo(patient.getPatientMobileNumber());
        personal.setEmail(patient.getPatientEmailId());
        personal.setDob(patient.getPatientDob());
        personal.setAge(patient.getPatientAge());
        personal.setGender(patient.getPatientGender() != null ? patient.getPatientGender().getId() : null);
        personal.setRelation(patient.getPatientRelation() != null ? patient.getPatientRelation().getId() : null);

        resp.setPersonal(personal);
        FollowUpPatientResponseDetails.AddressDetails address = new FollowUpPatientResponseDetails.AddressDetails();
        address.setAddress1(patient.getPatientAddress1());
        address.setAddress2(patient.getPatientAddress2());
        address.setCity(patient.getPatientCity());
        address.setPinCode(patient.getPatientPincode());
        address.setCountry(patient.getPatientCountry() != null ? patient.getPatientCountry().getId() : null);
        address.setState(patient.getPatientState() != null ? patient.getPatientState().getId() : null);
        address.setDistrict(patient.getPatientDistrict() != null ? patient.getPatientDistrict().getId() : null);

        resp.setAddress(address);
        FollowUpPatientResponseDetails.NokDetails nok = new FollowUpPatientResponseDetails.NokDetails();
        nok.setFirstName(patient.getNokFn());
        nok.setLastName(patient.getNokLn());
        nok.setEmail(patient.getNokEmail());
        nok.setMobileNo(patient.getNokMobileNumber());
        nok.setAddress1(patient.getNokAddress1());
        nok.setAddress2(patient.getNokAddress2());
        nok.setCity(patient.getNokCity());
        nok.setPinCode(patient.getNokPincode());
        //nok.setRelation(patient.getNokRelation() != null ? patient.getNokRelation().getId() : null);
        nok.setState(patient.getNokState() != null ? patient.getNokState().getId() : null);
        nok.setDistrict(patient.getNokDistrict() != null ? patient.getNokDistrict().getId() : null);
        nok.setCountry(patient.getNokCountry() != null ? patient.getNokCountry().getId() : null);

        resp.setNok(nok);
        FollowUpPatientResponseDetails.EmergencyDetails emergency = new FollowUpPatientResponseDetails.EmergencyDetails();
        emergency.setFirstName(patient.getEmerFn());
        emergency.setLastName(patient.getEmerLn());
        emergency.setMobileNo(patient.getEmerMobile());

        resp.setEmergency(emergency);
        OpdPatientDetail opd = opdPatientDetailRepository.findTopByPatientOrderByOpdPatientDetailsIdDesc(patient);

        if (opd != null) {
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

            resp.setVitals(vitals);
        }
        List<Visit> visits = visitRepository.findRelevantVisitsByPatientId(patientId);
        List<FollowUpPatientResponseDetails.AppointmentDetailResponse> appointmentList = new ArrayList<>();

        for (Visit v : visits) {
            FollowUpPatientResponseDetails.AppointmentDetailResponse appt = new FollowUpPatientResponseDetails.AppointmentDetailResponse();
            appt.setAppointmentId(v.getId());
            appt.setSpecialityId(v.getDepartment() != null ? v.getDepartment().getId() : null);
            appt.setSpecialityName(v.getDepartment() != null ? v.getDepartment().getDepartmentName() : null);
            appt.setDoctorId(v.getDoctor() != null ? v.getDoctor().getUserId() : null);
            appt.setDoctorName(v.getDoctorName());
            appt.setSessionId(v.getSession() != null ? v.getSession().getId() : null);
            appt.setSessionName(v.getSession() != null ? v.getSession().getSessionName() : null);
            appt.setVisitDate(v.getVisitDate());
            appt.setVisitType(v.getVisitType());
            appt.setTokenNo(v.getTokenNo());
            appt.setVisitStatus(v.getVisitStatus());
            if (AppConstants.STATUS_Y.equalsIgnoreCase(v.getVisitStatus())) {
                appt.setVisitStatus("Completed");
            } else if (AppConstants.STATUS_N.equalsIgnoreCase(v.getVisitStatus())) {
                appt.setVisitStatus("Pending");
            }
            appt.setTokenStartTime(HelperUtils.extractTimeFromInstant(v.getStartTime()));
            appt.setTokenEndTime(HelperUtils.extractTimeFromInstant(v.getEndTime()));
            appointmentList.add(appt);
        }

        resp.setAppointments(appointmentList);
        resp.setPhotoUrl(patient.getPatientImage());
        return ResponseUtils.createSuccessResponse(resp, new TypeReference<FollowUpPatientResponseDetails>() {});
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
            throw new SDDException(500,"User authentication failed or user has no first name");
        }
        // Update visit
        visit.setVisitStatus(AppConstants.VISIT_STATUS_CANCELLED.toLowerCase());
        visit.setCancelledBy(currentUser.getFirstName());
        visit.setCancelledDateTime(Instant.now());
        if (request.getCancelReasonId() != null) {
            MasAppointmentChangeReason reason = changeReasonRepository.findById(request.getCancelReasonId())
                    .orElseThrow(() -> new SDDException(500,"Cancel reason not found with ID: "));
            visit.setReason(reason);
        }
        bill.setPaymentStatus(AppConstants.PAYMENT_PAID.toLowerCase());
        billingHeaderRepository.save(bill);
        Visit savedVisit = visitRepository.save(visit);
        return new ApiResponse<>(HttpStatus.OK, AppConstants.APPOINTMENT_CANCELLED);
    }

    @Override
    @Transactional
    public ApiResponse<RescheduleAppointmentResponse> rescheduleAppointment(RescheduleAppointmentRequest request) {
        Optional<Visit> optionalVisit = visitRepository.findById(request.getVisitId());
        if (optionalVisit.isEmpty()) {
            return new ApiResponse<>(HttpStatus.NOT_FOUND, "Appointment not found with ID: " + request.getVisitId());
        }
        Visit v = optionalVisit.get();
        VisitRescheduleHistory history = new VisitRescheduleHistory();
        history.setVisitId(v);
        history.setRescheduleDatetime(request.getVisitDate());
        history.setRescheduleBy(authUtil.getCurrentUser().getFirstName());
        history.setNewTokenNo(request.getTokenNumber());
        history.setOldTokenNo(v.getTokenNo());
        history.setNewVisitDatetime(request.getAppointmentStartTime());
        history.setOldVisitDatetime(v.getVisitDate());
        history.setRescheduleDatetime(Instant.now());
        history.setRescheduleReason("");
        historyRepository.save(history);


        v.setVisitDate(request.getVisitDate());
        v.setStartTime(request.getAppointmentStartTime());
        v.setEndTime(request.getAppointmentEndTime());
        v.setTokenNo(request.getTokenNumber());
        v.setLastChgDate(Instant.now());

        visitRepository.save(v);
        return new ApiResponse<>(HttpStatus.OK, "Success");
    }


    @Transactional
    @Override
    public ApiResponse<BookingAppointmentResponse> bookAppointment(Long patientId, VisitRequest visitReq) {
        Patient patient = null;
        BookingAppointmentResponse response = new BookingAppointmentResponse();

        if (patientId != null) {
            patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));

            if (visitReq!=null) {

                Instant date = visitReq.getVisitDate();
                String visitType = getVisitTypeForFollowUpOrNew(patient.getId(), date);
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
    public ApiResponse<List<CancelledAppointmentResponse>> getCancelledAppointments(
            Long hospitalId,
            Long departmentId,
            Long doctorId,
            LocalDate fromDate,
            LocalDate toDate,
            Long cancellationReasonId
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

    /**
     * Maps CancelledAppointmentProjection to CancelledAppointmentResponse
     */
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

    private String formatAgeGender(String age, String gender) {
        StringBuilder result = new StringBuilder();

        if (age != null && !age.trim().isEmpty()) {
            result.append(age);
        }

        if (gender != null && !gender.trim().isEmpty()) {
            if (!result.isEmpty()) {
                result.append(" / ");
            }
            result.append(gender);
        }

        return !result.isEmpty() ? result.toString() : "";
    }

    public ApiResponse<List<AvailableTokenSlotResponse>> getAppointmentSlots(Long deptId, Long doctorId, String appointmentDate, Long sessionId,int flag) {
        int startToken,intervalToken,totalToken,totalOnlineTokens,timeTakenMin=0;
        String startTime,endTime="";

        LocalDate date = LocalDate.parse(appointmentDate);
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        List<AppSetup> optionalSetup = appSetupRepository.findByDoctorHospitalSessionAndDayName(
                doctorId, deptId, sessionId, dayName);

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
            occupiedTokens = visitRepository.findOccupiedTokens(
                            deptId, doctorId, sessionId, startOfDay, endOfDay)
                    .stream().collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Error fetching occupied tokens", e);
        }

        List<AvailableTokenSlotResponse> list = generateSlotsWithAvailability(
                startToken, intervalToken, totalToken,
                startTime, endTime, timeTakenMin, occupiedTokens,flag);

        return ResponseUtils.createSuccessResponse(list, new TypeReference<List<AvailableTokenSlotResponse>>() {});
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

}
