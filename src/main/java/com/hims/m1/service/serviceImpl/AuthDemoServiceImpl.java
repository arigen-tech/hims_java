package com.hims.m1.service.serviceImpl;

import com.hims.m1.Mapper.VerificationMapper;
import com.hims.m1.abdm_response.AbdmCertificateResponse;
import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.apiResponse.ResponseUtils;
import com.hims.m1.request.DemoAuthRequest;
import com.hims.m1.response.AUthDeamoResponse;
import com.hims.m1.service.AuthDemoAbhaService;
import com.hims.m1.service.CaptchaService;
import com.hims.m1.util.AadhaarEncryptor;
import com.hims.m1.util.CreateAbdmRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hims.m1.util.NhaHeaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
public class AuthDemoServiceImpl implements AuthDemoAbhaService {

    private final SessionAndCertServiceImpl sessionAndCertService;
    private final WebClient webClient;
    @Autowired
    VerificationMapper verificationMapper;
    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private AadhaarEncryptor aadhaarEncryptor;

    public AuthDemoServiceImpl(SessionAndCertServiceImpl sessionAndCertService, WebClient.Builder webClientBuilder) {
        this.sessionAndCertService = sessionAndCertService;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<ApiResponse<AUthDeamoResponse>> authDemo(DemoAuthRequest demoAuthRequest) throws Exception {

        AbdmCertificateResponse abdmCertificateResponse = aadhaarEncryptor.fetchCertificates();
        String aadhaar = demoAuthRequest.getAadhaarNumber();

        if (demoAuthRequest.getKey() == null) {
            return Mono.just(ResponseUtils.createFailureResponse("Key not found", false));
        }

        byte[] aesKey = NhaHeaderUtil.decryptAESKey(demoAuthRequest.getKey());
        aadhaar = NhaHeaderUtil.decryptData(aadhaar, aesKey);


        String encryptAadhaarNumber = aadhaarEncryptor.doEncrypt(aadhaar, abdmCertificateResponse);

        Map<String, Object> makingRequest = CreateAbdmRequest.buildDemoAuthRequest(demoAuthRequest, encryptAadhaarNumber);
        Mono<AUthDeamoResponse> authDemo = verificationMapper.authDemo(makingRequest);
        return authDemo.map(res -> {
            if (res.getIsNew()) {
                return ResponseUtils.createSuccessResponse(res, true, "Abha create sucessfully.");
            } else {
                return ResponseUtils.createSuccessResponse(res, true, "Abha is already exist.");
            }
        }).onErrorResume(WebClientResponseException.class, ex -> {
            String errorCode = "";
            String errorMessage = "";
            try {
                JsonNode root = new ObjectMapper().readTree(ex.getResponseBodyAsString());
                if (root.has("message")) {
                    errorMessage = root.path("message").asText();
                }
                if (root.has("error") && root.path("error").has("code")) {
                    errorCode = root.path("error").path("code").asText(errorCode);
                    errorMessage = root.path("error").path("message").asText(errorMessage);
                }
            } catch (Exception e) {
                errorMessage = ex.getResponseBodyAsString();
            }
            return Mono.just(ResponseUtils.createFailureResponse(errorMessage, false, errorCode));
        });

    }

}
