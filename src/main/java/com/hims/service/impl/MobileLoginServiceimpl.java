package com.hims.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.Patient;
import com.hims.entity.PatientLogin;
import com.hims.entity.repository.LabHdRepository;
import com.hims.entity.repository.PatientLoginRepository;
import com.hims.entity.repository.PatientRepository;
import com.hims.exception.SDDException;
import com.hims.jwt.JwtHelper;
import com.hims.request.LoginRequest;
import com.hims.request.PatientActivationOtpRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AuthResponse;
import com.hims.response.MobileLoginResponce;
import com.hims.response.PatientIdResponse;
import com.hims.service.MobileLoginService;
import com.hims.utils.ResponseUtils;
import com.hims.utils.SMSUtility;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class MobileLoginServiceimpl implements MobileLoginService {
    private static final Logger log = LoggerFactory.getLogger(LabRegistrationServicesImpl.class);
    @Autowired
    LabHdRepository labHdRepository;
    @Autowired
    private PatientLoginRepository patientLoginRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private SMSUtility smsUtility;
    @Value("${sms.otp.base-url}")
    private String otpBaseUrl;
    @Value("${sms.otp.send-path}")
    private String otpSendPath;
    @Value("${sms.otp.template.login}")
    private String loginOtpTemplate;
    @Value("${sms.otp.template.account-activation}")
    private String accountActivationOtpTemplate;
    @Autowired
    private JwtHelper jwtHelper;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AuthResponse> switchPatient(Long patientId, String mobileNumber) 
    {
        try {
            boolean patientBelongsToMobile = patientLoginRepository
                .findByMobileNoAndPatientIdOrderByPatientLoginIdDesc(mobileNumber, patientId)
                .stream()
                .anyMatch(this::isActive);

            if (!patientBelongsToMobile) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                "Patient is not linked to this mobile number", HttpStatus.BAD_REQUEST.value());
            }

            Patient patient = patientRepository.findById(patientId).orElse(null);
            if (patient == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                "Patient not found", HttpStatus.NOT_FOUND.value());
            }

            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(jwtHelper.mobileGenerateToken(mobileNumber, patientId));
            authResponse.setRefreshToken(jwtHelper.mobileGenerateRefreshToken(mobileNumber, patientId));
            authResponse.setPatientIdResponseList(List.of(toPatientIdResponse(patient)));
            authResponse.setMessage("Patient switched successfully");
            return ResponseUtils.createSuccessResponse(authResponse, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Unable to switch patient: patientId={}, mobileNumber={}", patientId, mobileNumber, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                "Unable to switch patient. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> deactivatePatient(Long patientId) {
        PatientLogin login = patientLoginRepository.findByPatientId(patientId).orElse(null);
        if (login == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Patient login account not found", HttpStatus.NOT_FOUND.value());
        }
        login.setStatus("N");
        patientLoginRepository.save(login);
        return ResponseUtils.createSuccessResponse("Patient login deactivated successfully", new TypeReference<>() {});
    }

    @Override
    @Transactional
    public ApiResponse<MobileLoginResponce> sendPatientActivationOtp(PatientActivationOtpRequest request) {
        PatientLogin login = patientLoginRepository.findByMobileNoOrderByPatientLoginIdDesc(request.getMobileNo())
                .stream().filter(row -> row.getPatientId().equals(request.getPatientId())).findFirst().orElse(null);
        if (login == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Patient is not linked to this mobile number", HttpStatus.NOT_FOUND.value());
        }
        if (isActive(login)) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Patient is already active", HttpStatus.BAD_REQUEST.value());
        }
        try {
            MobileLoginResponce response = new MobileLoginResponce();
            response.setMobileNo(request.getMobileNo());
            response.setSessionId(smsUtility.sendOtp(request.getMobileNo(), accountActivationOtpTemplate,
                    "Account Activation"));
            response.setMessage("OTP sent successfully");
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Unable to send patient activation OTP for patientId={}", request.getPatientId(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Unable to send OTP. Please try again.", HttpStatus.BAD_GATEWAY.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> verifyPatientActivationOtp(PatientActivationOtpRequest request) {
        PatientLogin login = patientLoginRepository.findByMobileNoOrderByPatientLoginIdDesc(request.getMobileNo())
                .stream().filter(row -> row.getPatientId().equals(request.getPatientId())).findFirst().orElse(null);
        if (login == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Patient is not linked to this mobile number", HttpStatus.NOT_FOUND.value());
        }
        if (isActive(login)) {
            return ResponseUtils.createSuccessResponse("Patient is already active", new TypeReference<>() {});
        }
        if (request.getSessionId() == null || request.getOtp() == null
                || !smsUtility.verifyOtp(request.getSessionId(), request.getOtp())) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Invalid or expired OTP", HttpStatus.BAD_REQUEST.value());
        }
        login.setStatus("Y");
        patientLoginRepository.save(login);
        return ResponseUtils.createSuccessResponse("Patient activated successfully", new TypeReference<>() {});
    }

    private boolean isActive(PatientLogin login) {
        return login.getStatus() == null || !"N".equalsIgnoreCase(login.getStatus());
    }

    private PatientIdResponse toPatientIdResponse(Patient patient) 
    {
        PatientIdResponse patientResponse = new PatientIdResponse();
        patientResponse.setPatientId(patient.getId());
        String fullName = Stream.of(patient.getPatientFn(), patient.getPatientMn(), patient.getPatientLn())
            .filter(Objects::nonNull)
            .filter(name -> !name.trim().isEmpty())
            .collect(Collectors.joining(" "));
        patientResponse.setPatientName(fullName.isEmpty() ? null : fullName);
        patientResponse.setAge(patient.getPatientAge());
        patientResponse.setGender(patient.getPatientGender() != null
            ? patient.getPatientGender().getGenderName() : null);
        patientResponse.setPatientPhoneNumber(patient.getPatientMobileNumber());
        patientResponse.setRelation(patient.getPatientRelation() != null
            ? patient.getPatientRelation().getRelationName() : null);
        patientResponse.setStatus("ACTIVE");
        return patientResponse;
    }

    @Override
    @Transactional
    public ApiResponse loginRequest(LoginRequest request) {

        MobileLoginResponce res = new MobileLoginResponce();
        try {
            List<PatientLogin> allPatientLogins = patientLoginRepository.findByMobileNoOrderByPatientLoginIdDesc(request.getMobileNo());
            List<PatientLogin> patientLoginList = allPatientLogins.stream().filter(this::isActive).toList();
            if (patientLoginList.isEmpty() && !allPatientLogins.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Patient is Currently Inactive", HttpStatus.FORBIDDEN.value());
            }
            if (patientLoginList.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Mobile number not registered",
                        HttpStatus.NOT_FOUND.value()
                );
            }
            // Send OTP
            Long mobile = Long.valueOf(request.getMobileNo());

            try {
                String uri = otpBaseUrl + otpSendPath
                        .replace("{mobile}", mobile.toString())
                        .replace("{template}", loginOtpTemplate);

                HttpResponse<String> response = Unirest.post(uri).asString();
                JSONObject jsonObject = new JSONObject(response.getBody());

                if (!"Success".equalsIgnoreCase(jsonObject.getString("Status"))) {
                    return ResponseUtils.createFailureResponse(
                            res,
                            new TypeReference<>() {},
                            "Failed to send OTP",
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    );
                }

                String sessionId = jsonObject.getString("Details");

                //  Convert PatientLogin list  PatientIdResponse list
//                List<PatientIdResponse> patientIdList = patientLoginList.stream()
//                        .map(pl -> {
//                            Patient patient = patientRepository.findById(pl.getPatientId()).orElse(null);
//                            if (patient == null) return null;
//
//                            PatientIdResponse pid = new PatientIdResponse();
//                            pid.setPatientId(patient.getId());
//                            String fullName = Stream.of(
//                                            patient.getPatientFn(),
//                                            patient.getPatientMn(),
//                                            patient.getPatientLn()
//                                    ).filter(Objects::nonNull)
//                                    .filter(s -> !s.trim().isEmpty())
//                                    .collect(Collectors.joining(" "));
//
//                            pid.setPatientName(fullName.isEmpty() ? null : fullName);
//                            pid.setAge(patient.getPatientAge());
//                            pid.setGender(patient.getPatientGender() != null ? patient.getPatientGender().getGenderName() : null);
//                            pid.setPatientPhoneNumber(patient.getPatientMobileNumber());
//                            pid.setRelation(patient.getPatientRelation() != null ? patient.getPatientRelation().getRelationName() : null);
//                            return pid;
//                        })
//                        .toList();
//
//                res.setPatientIdResponseList(patientIdList);
                res.setSessionId(sessionId);
                res.setMobileNo(request.getMobileNo());
                res.setMessage("OTP sent successfully");

                return ResponseUtils.createSuccessResponse(
                        res,
                        new TypeReference<MobileLoginResponce>() {}
                );

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseUtils.createFailureResponse(
                        res,
                        new TypeReference<>() {},
                        "Unable to send OTP",
                        500
                );
            }

        } catch (SDDException e) {
            return ResponseUtils.createFailureResponse(res,
                    new TypeReference<>() {},
                    e.getMessage(),
                    e.getStatus()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(res,
                    new TypeReference<>() {},
                    "Internal Server Error",
                    500
            );
        }
    }

}
