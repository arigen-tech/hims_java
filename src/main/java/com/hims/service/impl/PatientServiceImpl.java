package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.*;
import com.hims.response.ApiResponse;
import com.hims.response.OpdBillingPaymentResponse;
import com.hims.response.PatientRegFollowUpResp;
import com.hims.service.BillingService;
import com.hims.service.PatientService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
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
    @Value("${upload.image.path}")
    private String baseUrl;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private OpdPatientDetailRepository opdPatientDetailRepository;
    @Autowired
    private MasServiceCategoryRepository masServiceCategoryRepository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<PatientRegFollowUpResp> registerPatientWithOpd(PatientRequest request, OpdPatientDetailRequest opdPatientDetailRequest, VisitRequest visit) {
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
            resp.setPatient(existingPatient.get());
            return ResponseUtils.createFailureResponse(resp, new TypeReference<>() {
                    },
                    "Patient already Registered", 500);
        }
        Patient patient = savePatient(request,false);
        resp.setPatient(patient);

        OpdPatientDetail newOpd=new OpdPatientDetail();
        Visit savedVisit=new Visit();
        if (visit != null) {

            savedVisit = createAppointment(visit,  patient);
            if (savedVisit.getHospital().getPreConsultationAvailable().equalsIgnoreCase("n")) {
                newOpd = addOpdDetaials(savedVisit, opdPatientDetailRequest, patient);

            }
        }
        resp.setVisit(savedVisit);
        resp.setOpdPatientDetail(newOpd);
        return ResponseUtils.createSuccessResponse(resp, new TypeReference<>() {
        });
    }


    @Override
    public ApiResponse<PatientRegFollowUpResp> updatePatient(PatientFollowUpReq followUpRequest) {
        PatientRegFollowUpResp resp=new PatientRegFollowUpResp();
        PatientRequest request = followUpRequest.getPatientDetails().getPatient();
        Patient patient = savePatient(request, true);
        resp.setPatient(patient);
        if (followUpRequest.isAppointmentFlag()) {
            VisitRequest visit = followUpRequest.getPatientDetails().getVisit();
            OpdPatientDetailRequest opdPatientDetailRequest = followUpRequest.getPatientDetails().getOpdPatientDetail();
            if (visit != null) {
                Visit savedVisit = createAppointment(visit, patient);
                resp.setVisit(savedVisit);
                OpdPatientDetail newOpd=new OpdPatientDetail();
                if (savedVisit.getHospital().getPreConsultationAvailable().equalsIgnoreCase("n")) {

                    newOpd = addOpdDetaials(savedVisit, opdPatientDetailRequest, patient);

                }
                resp.setOpdPatientDetail(newOpd);
            }
        }
        return ResponseUtils.createSuccessResponse(resp, new TypeReference<>() {
        });

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
        String mobileNo = req.getMobileNo().trim();
//        if (mobileNo != null && mobileNo.trim().isEmpty()) {
//            mobileNo = null;
//        }

        String uhidNo = req.getUhidNo().trim();
//        if (uhidNo != null && uhidNo.trim().isEmpty()) {
//            uhidNo = null;
//        }

        LocalDate appointmentDate = req.getAppointmentDate();
        List<Patient> patientList;
        if (appointmentDate != null) {
            patientList = patientRepository.searchPatients(mobileNo, req.getPatientName(), uhidNo, req.getAppointmentDate());
        } else {
            patientList = patientRepository.searchPatients(mobileNo, req.getPatientName(), uhidNo);
        }

        return ResponseUtils.createSuccessResponse(patientList, new TypeReference<>() {
        });
    }

    @Override
    public ApiResponse<List<Visit>> getPendingPreConsultations() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User current_user=userRepository.findByUserName(username);
        List<Visit> response=visitRepository.findByHospitalAndPreConsultation(current_user.getHospital(),"n");
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
        });
    }

    @Override
    public ApiResponse<String> saveVitalDetails(OpdPatientDetailRequest request) {
        OpdPatientDetail savedDetails=addOpdDetaials(null,request,null);
        Visit visit=visitRepository.findById(request.getVisitId()).get();
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

    private Patient savePatient(PatientRequest request, boolean followUp) {

//        User loggedInUser=userRepository.findByUserName(request.getLastChgBy());
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

        // Fetch and set related entities using IDs

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
        patient = patientRepository.save(patient); // Save patient
        return patient;
    }

    private OpdPatientDetail addOpdDetaials(Visit savedVisit, OpdPatientDetailRequest opdPatientDetailRequest, Patient patient) {
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
        return opdPatientDetailRepository.save(opdPatientDetail); // Save OPD details
    }

    private Visit createAppointment(VisitRequest visit, Patient patient) {
        Visit newVisit = new Visit();
        String todayDayName = LocalDate.now()
                .getDayOfWeek()
                .name()
                .substring(0, 1)
                .toUpperCase() + LocalDate.now()
                .getDayOfWeek()
                .name()
                .substring(1)
                .toLowerCase();
        Optional<AppSetup> optionalSetup = appSetupRepository.findByDoctorHospitalSessionAndDayName(
                visit.getDoctorId(), visit.getHospitalId(), visit.getSessionId(), todayDayName);


        if (optionalSetup.isEmpty()) {
            throw new IllegalStateException("AppSetup not configured for today’s session.");
        }

        AppSetup setup = optionalSetup.get();
        int startToken = setup.getStartToken() != null ? setup.getStartToken() : 1;
        int maxToken = setup.getTotalToken() != null ? setup.getTotalToken() : Integer.MAX_VALUE;
        // Map request fields to Visit entity
        List<Long> existingTokens = visitRepository
                .findAllTokensForSessionToday(visit.getDoctorId(), visit.getHospitalId(), visit.getSessionId());

        Long nextToken = getNextAvailableToken(existingTokens, startToken, maxToken);
        Instant[] tokenTimes = calculateTokenTimeAsInstant(
                setup.getStartTime(), // e.g., "08:00"
                setup.getTimeTaken(), // e.g., 10

                nextToken,LocalDate.now());
        newVisit.setStartTime(tokenTimes[0]);
        newVisit.setEndTime(tokenTimes[1]);
        newVisit.setTokenNo(nextToken);
        newVisit.setVisitDate(visit.getVisitDate());
        newVisit.setLastChgDate(Instant.now());
        newVisit.setVisitStatus("p");
        newVisit.setPriority(visit.getPriority());
        newVisit.setDepartment(masDepartmentRepository.findById(visit.getDepartmentId()).get());
        newVisit.setDoctorName(visit.getDoctorName());
        newVisit.setBillingStatus("n");
        newVisit.setPatient(patient);
        if (visit.getDoctorId() != null) {
            userRepository.findById(visit.getDoctorId()).ifPresent(newVisit::setDoctor);
        }

        if (visit.getHospitalId() != null) {
            Optional<MasHospital> hospital=masHospitalRepository.findById(visit.getHospitalId());
            if(hospital.isPresent()){
                newVisit.setHospital(hospital.get());
                if(hospital.get().getPreConsultationAvailable().equalsIgnoreCase("y")){
                    newVisit.setPreConsultation("n");
                } else if (hospital.get().getPreConsultationAvailable().equalsIgnoreCase("n")) {
                    newVisit.setPreConsultation("y");
                }
            }
        }

        if (visit.getIniDoctorId() != null) {
            userRepository.findById(visit.getDoctorId()).ifPresent(newVisit::setIniDoctor);
        }

        if (visit.getSessionId() != null) {
            masOpdSessionRepository.findById(visit.getSessionId()).ifPresent(newVisit::setSession);
        }
        // Save visit
        Visit savedVisit=visitRepository.save(newVisit);
        //create billing header and detail
        Optional<MasServiceCategory> serviceCategory=masServiceCategoryRepository.findBySacCode("998515");
        MasDiscount discount=new MasDiscount();
        ApiResponse<OpdBillingPaymentResponse> resp=billingService.saveBillingForOpd(savedVisit,serviceCategory.get(),null);

        return savedVisit;
    }
    public static Instant[] calculateTokenTimeAsInstant(
            String startTimeStr,
            int timeTakenMinutes,
            Long tokenNo,
            LocalDate visitDate
    ) {
        if (startTimeStr == null || timeTakenMinutes <= 0 || tokenNo <= 0 || visitDate == null) {
            throw new IllegalArgumentException("Invalid input parameters.");
        }

        // Parse start time and calculate token offset
        LocalTime baseTime = LocalTime.parse(startTimeStr); // e.g., "10:00"
        long minutesToAdd = (tokenNo - 1) * timeTakenMinutes;

        // Apply token offset
        LocalTime tokenStartTime = baseTime.plusMinutes(minutesToAdd);
        LocalTime tokenEndTime = tokenStartTime.plusMinutes(timeTakenMinutes);

        // Combine with visit date
        LocalDateTime startDateTime = LocalDateTime.of(visitDate, tokenStartTime);
        LocalDateTime endDateTime = LocalDateTime.of(visitDate, tokenEndTime);

        // Treat time as UTC without converting via system/default zone
        return new Instant[] {
                startDateTime.toInstant(ZoneOffset.UTC),
                endDateTime.toInstant(ZoneOffset.UTC)
        };
    }

    private String generateUhid(Patient patient) {
        List<Patient> existing = patientRepository.findByPatientMobileNumberAndPatientRelation(patient.getPatientMobileNumber(), patient.getPatientRelation());
        return (patient.getPatientMobileNumber() + patient.getPatientRelation().getCode() + (existing.size() + 1));
    }

    private Long getNextAvailableToken(List<Long> existingTokens, int startToken, int maxToken) {
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
    }
}
