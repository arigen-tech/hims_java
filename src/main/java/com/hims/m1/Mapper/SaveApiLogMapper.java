package com.hims.m1.Mapper;


import com.hims.m1.config.RequestHeaderContext;
import com.hims.m1.entity.Abdm_ApiLog;
import com.hims.m1.entity.Abdm_Consent;
import com.hims.m1.repository.AbdmApiLogRepository;
import com.hims.m1.repository.AbdmConsentRepository;
import com.hims.m1.request.CreateAbhaSendOTPRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SaveApiLogMapper {

    // ABDM_API_LOG.REQUEST_BODY / RESPONSE_BODY are VARCHAR2(255) in Oracle.
    private static final int MAX_LOG_BODY_LENGTH = 255;
    private static final String SOURCE_RSHAA = "RSHAA";
    private static final String SOURCE_ABDM = "ABDM";

    @Autowired
    AbdmApiLogRepository abdmApiLogRepository;

    @Autowired
    AbdmConsentRepository abdmConsentRepository;

    @Autowired
    private ObjectMapper objectMapper;


    public void saveApiLog(String serverEndPoint,
                           String localEndPoint,
                           String statusCode,
                           String requestBody,
                           String responseBody,
                           Long hospitalId,
                           Long createdBy) {


        Abdm_ApiLog abdmApiLog = new Abdm_ApiLog();

        abdmApiLog.setServerApiEndPoint(serverEndPoint);
        abdmApiLog.setLocalApiEndPoint(localEndPoint);
        abdmApiLog.setStatusCode(statusCode);
        abdmApiLog.setRequestBody(requestBody);
        abdmApiLog.setResponseBody(responseBody);
        abdmApiLog.setHospitalId(hospitalId);
        abdmApiLog.setCreatedBy(createdBy);

//        abdmApiLogRepository.save(abdmApiLog);

    }

    public void saveControllerApiLog(String localEndPoint,
                                     String statusCode,
                                     Object requestBody,
                                     Object responseBody,
                                     Long hospitalId,
                                     Long createdBy) {
        saveApiLogWithSource(
                SOURCE_RSHAA,
                null,
                localEndPoint,
                statusCode,
                requestBody,
                responseBody,
                hospitalId,
                createdBy
        );
    }

    public void saveMapperApiLog(String serverEndPoint,
                                 String localEndPoint,
                                 String statusCode,
                                 Object requestBody,
                                 Object responseBody,
                                 Long hospitalId,
                                 Long createdBy) {
        saveApiLogWithSource(
                SOURCE_ABDM,
                serverEndPoint,
                localEndPoint,
                statusCode,
                requestBody,
                responseBody,
                hospitalId,
                createdBy
        );
    }

    public void saveConsent(CreateAbhaSendOTPRequest request,String aadhaarNumber) {

        try {

            Abdm_Consent abdmApiLog = new Abdm_Consent();

            abdmApiLog.setConsentName(request.getConsentName());
            abdmApiLog.setAadhaarNumber(maskAadhaarNumber(aadhaarNumber));
            abdmApiLog.setConsent1(request.getConsent1());
            abdmApiLog.setConsent2(request.getConsent2());
            abdmApiLog.setConsent3(request.getConsent3());
            abdmApiLog.setConsent4(request.getConsent4());
            abdmApiLog.setConsent5(request.getConsent5());
            abdmApiLog.setConsent6(request.getConsent6());
            abdmApiLog.setConsent7(request.getConsent7());
            abdmApiLog.setCreatedBy(RequestHeaderContext.getUserId());

//            abdmConsentRepository.save(abdmApiLog);
        } catch (Exception e) {
            log.warn("e", e.toString());
        }
    }

    private String maskAadhaarNumber(String aadhaarNumber) {
        if (aadhaarNumber == null || aadhaarNumber.isBlank()) {
            return aadhaarNumber;
        }

        String normalized = aadhaarNumber.replaceAll("\\s+", "");
        if (normalized.length() <= 4) {
            return normalized;
        }

        int maskedLength = normalized.length() - 4;
        return "X".repeat(maskedLength) + normalized.substring(maskedLength);
    }

    private String toJson(Object body) {
        if (body == null) {
            return null;
        }
        try {
            String rawJson = body instanceof String ? (String) body : objectMapper.writeValueAsString(body);
            return truncate(rawJson);
        } catch (JsonProcessingException exception) {
            log.warn("Unable to serialize API log body. Falling back to string conversion: {}", exception.getMessage());
            return truncate(String.valueOf(body));
        }
    }

    private void saveApiLogWithSource(String source,
                                      String serverEndPoint,
                                      String localEndPoint,
                                      String statusCode,
                                      Object requestBody,
                                      Object responseBody,
                                      Long hospitalId,
                                      Long createdBy) {

        String resolvedLocalEndpoint = applySourceFlag(source, localEndPoint);
        String resolvedServerEndpoint = serverEndPoint;

        if (resolvedServerEndpoint == null || resolvedServerEndpoint.trim().isEmpty()) {
            resolvedServerEndpoint = source;
        }

        saveApiLog(
                resolvedServerEndpoint,
                resolvedLocalEndpoint,
                statusCode,
                toJson(requestBody),
                toJson(responseBody),
                hospitalId,
                createdBy
        );
    }

    private String applySourceFlag(String source, String endpoint) {
        if (source == null || source.trim().isEmpty()) {
            return endpoint;
        }
        if (endpoint == null || endpoint.trim().isEmpty()) {
            return source;
        }
        String flagPrefix = source + "::";
        if (endpoint.startsWith(flagPrefix)) {
            return endpoint;
        }
        return flagPrefix + endpoint;
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() <= MAX_LOG_BODY_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_LOG_BODY_LENGTH);
    }


}
