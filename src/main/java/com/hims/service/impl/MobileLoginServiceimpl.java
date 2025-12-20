package com.hims.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.PatientLogin;
import com.hims.entity.repository.LabHdRepository;
import com.hims.entity.repository.PatientLoginRepository;
import com.hims.exception.SDDException;
import com.hims.request.LoginRequest;
import com.hims.response.ApiResponse;
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


@Service
public class MobileLoginServiceimpl implements MobileLoginService {
    private static final Logger log = LoggerFactory.getLogger(LabRegistrationServicesImpl.class);
    @Autowired
    LabHdRepository labHdRepository;
    @Autowired
    private PatientLoginRepository patientLoginRepository;
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
                String uri = "https://2factor.in/API/V1/999a2629-21e1-11ec-a13b-0200cd936042/SMS/"
                        + mobile + "/AUTOGEN/DMSLOGIN";

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
                List<PatientIdResponse> patientIdList = patientLoginList.stream()
                        .map(pl -> {
                            PatientIdResponse pid = new PatientIdResponse();
                            pid.setPatientId(pl.getPatientId());
                            return pid;
                        })
                        .toList();

                res.setPatientIdResponseList(patientIdList);
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
