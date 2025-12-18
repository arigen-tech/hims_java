package com.hims.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.PatientLogin;
import com.hims.entity.repository.LabHdRepository;
import com.hims.entity.repository.PatientLoginRepository;
import com.hims.exception.SDDException;
import com.hims.request.LoginRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MobileLoginResponce;
import com.hims.service.MobileLoginService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
            PatientLogin patientLogin = patientLoginRepository.findByMobileNo(request.getMobileNo());

            if (patientLogin == null) {
                return ResponseUtils.createFailureResponse(res, new TypeReference<>() {},
                        "Mobile number not registered",
                        HttpStatus.NOT_FOUND.value());
            }
            res.setMessage("Login successful");
            return ResponseUtils.createSuccessResponse(res, new TypeReference<MobileLoginResponce>() {});

        } catch (SDDException e) {
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {}, e.getMessage(), e.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(res, new TypeReference<>() {},
                    "Internal Server Error", 500);
        }
}}
