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
import com.hims.response.ApiResponse;
import com.hims.response.AuthResponse;
import com.hims.response.MobileLoginResponce;
import com.hims.response.PatientIdResponse;
import com.hims.service.MobileLoginService;
import com.hims.utils.ResponseUtils;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private JwtHelper jwtHelper;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AuthResponse> switchPatient(Long patientId, String mobileNumber) 
    {
        try {
            boolean patientBelongsToMobile = patientLoginRepository
                .findByMobileNoOrderByPatientLoginIdDesc(mobileNumber)
                .stream()
                .anyMatch(patientLogin -> patientId.equals(patientLogin.getPatientId()));

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
        return patientResponse;
    }

    @Override
    @Transactional
    public ApiResponse loginRequest(LoginRequest request) {

        MobileLoginResponce res = new MobileLoginResponce();
        try {
            List<PatientLogin> patientLoginList = patientLoginRepository.findByMobileNoOrderByPatientLoginIdDesc(
                            request.getMobileNo()
                    );
            if (patientLoginList == null || patientLoginList.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Mobile number not registered",
                        HttpStatus.NOT_FOUND.value()
                );
            }
            // Send OTP
            Long mobile = Long.valueOf(request.getMobileNo());

            try {
                String uri = "https://2factor.in/API/V1/999a2629-21e1-11ec-a13b-0200cd936042/SMS/"+ mobile +"/AUTOGEN/ARIHLT_LOGINOTP";

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
