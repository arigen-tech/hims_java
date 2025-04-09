package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.OpdPatientDetailRequest;
import com.hims.request.PatientRequest;
import com.hims.request.VisitRequest;
import com.hims.response.ApiResponse;
import com.hims.service.PatientService;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {
    private static final String UPLOAD_DIR = "patientImage/";

    @Value("${upload.image.path}")
    private String baseUrl;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private OpdPatientDetailRepository opdPatientDetailRepository;
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

    @Override
    public ApiResponse<Patient> registerPatientWithOpd(PatientRequest request, OpdPatientDetailRequest opdPatientDetailRequest, VisitRequest visit) {
        Patient patient=new Patient();
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
        patient.setLastChgBy(request.getLastChgBy());

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
        Optional<Patient> existingPatient = patientRepository.findByUniqueCombination(
                patient.getPatientFn(),
                patient.getPatientLn(),
                patient.getPatientGender(),
                patient.getPatientDob() != null ? patient.getPatientDob() : null,
                patient.getPatientAge(),
                patient.getPatientMobileNumber(),
                patient.getPatientRelation()
        );

        if (existingPatient.isPresent()) {
            return ResponseUtils.createFailureResponse(patient, new TypeReference<>() {},
                    "Patient already Registered",500);
        }
        patient.setUhidNo(generateUhid(patient));
        patient = patientRepository.save(patient); // Save patient
        if(visit!=null) {
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
                    visit.getDoctorId(), visit.getHospitalId(), visit.getSessionId(),  todayDayName);


            if (!optionalSetup.isPresent()) {
                throw new IllegalStateException("AppSetup not configured for today’s session.");
            }

            AppSetup setup = optionalSetup.get();
            Integer startToken = setup.getStartToken() != null ? setup.getStartToken() : 1;
            Integer maxToken = setup.getTotalToken() != null ? setup.getTotalToken() : Integer.MAX_VALUE;
            // Map request fields to Visit entity
            List<Long> existingTokens = visitRepository
                    .findAllTokensForSessionToday(visit.getDoctorId(), visit.getHospitalId(), visit.getSessionId());

            Long nextToken = getNextAvailableToken(existingTokens, startToken, maxToken);

            newVisit.setTokenNo(nextToken);
            newVisit.setVisitDate(visit.getVisitDate());
            newVisit.setLastChgDate(Instant.now());
            newVisit.setVisitStatus("y");
            newVisit.setPriority(visit.getPriority());
            newVisit.setDepartmentId(visit.getDepartmentId());
            newVisit.setDoctorName(visit.getDoctorName());
            newVisit.setBillingStatus("n");
            newVisit.setPatient(patient);
            if (visit.getDoctorId() != null) {
                userRepository.findById(visit.getDoctorId()).ifPresent(newVisit::setDoctor);
            }

            if (visit.getHospitalId() != null) {
                masHospitalRepository.findById(visit.getHospitalId()).ifPresent(newVisit::setHospital);
            }

            if (visit.getIniDoctorId() != null) {
                userRepository.findById(visit.getIniDoctorId()).ifPresent(newVisit::setIniDoctor);
            }

            if (visit.getSessionId() != null) {
                masOpdSessionRepository.findById(visit.getSessionId()).ifPresent(newVisit::setSession);
            }

            // Save visit
            Visit savedVisit=visitRepository.save(newVisit);

            if (savedVisit.getHospital().getPreConsultationAvailable().equalsIgnoreCase("n")) {
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
                opdPatientDetail.setPatient(patient);

                opdPatientDetail.setVisit(savedVisit);

                MasDepartment department = masDepartmentRepository.findById(savedVisit.getDepartmentId())
                        .orElseThrow(() -> new RuntimeException("Department not found"));
                opdPatientDetail.setDepartment(department);
                opdPatientDetail.setHospital(savedVisit.getHospital());
                opdPatientDetail.setDoctor(savedVisit.getDoctor());
                opdPatientDetail.setLastChgDate(Instant.now());
                opdPatientDetail.setLastChgBy(opdPatientDetailRequest.getLastChgBy());
                opdPatientDetailRepository.save(opdPatientDetail); // Save OPD details
            }
        }
        return ResponseUtils.createSuccessResponse(patient, new TypeReference<>() {
        });
    }

    @Override
    public ApiResponse<Patient> updatePatient(PatientRequest request) {
        Patient patient=new Patient();
        Patient existing=patientRepository.findById(request.getId()).get();
        patient.setId(request.getId());
        patient.setUhidNo(request.getUhidNo());
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
        patient.setCreatedOn(existing.getCreatedOn());
        patient.setUpdatedOn(Instant.now());
        patient.setLastChgBy(request.getLastChgBy());

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
        Optional<Patient> existingPatient = patientRepository.findByUniqueCombination(
                patient.getPatientFn(),
                patient.getPatientLn(),
                patient.getPatientGender(),
                patient.getPatientDob() != null ? patient.getPatientDob() : null,
                patient.getPatientAge(),
                patient.getPatientMobileNumber(),
                patient.getPatientRelation()
        );
        patient.setUhidNo(generateUhid(patient));
        patient = patientRepository.save(patient); // Save patient
    return ResponseUtils.createSuccessResponse(patient, new TypeReference<Patient>() {
    });
    }

    @Override
    public ApiResponse<String> uploadImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            // Ensure the upload directory exists
            File uploadDir = new File(baseUrl+UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Generate a unique filename
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(baseUrl+UPLOAD_DIR, filename);

            // Save file to the server
            Files.write(filePath, file.getBytes());
            return ResponseUtils.createSuccessResponse(baseUrl+UPLOAD_DIR + filename, new TypeReference<>() {
            });

        } catch (IOException e) {
            throw new RuntimeException("Error uploading file: " + e.getMessage());
        }
    }

    private String generateUhid(Patient patient) {
        List<Patient> existing =patientRepository.findByPatientMobileNumberAndPatientRelation(patient.getPatientMobileNumber(), patient.getPatientRelation());
        return (patient.getPatientMobileNumber()+patient.getPatientRelation().getCode()+(existing.size()+1));
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
