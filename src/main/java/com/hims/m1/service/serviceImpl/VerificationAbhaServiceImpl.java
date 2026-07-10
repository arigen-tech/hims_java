package com.hims.m1.service.serviceImpl;

import com.hims.m1.Mapper.VerificationMapper;
import com.hims.m1.abdm_request.AbhaSendOtpRequest;
import com.hims.m1.abdm_request.AbhaVerifyOtpMainRequest;
import com.hims.m1.abdm_request.AbhaVerifyOtpSubRequest;
import com.hims.m1.abdm_request.AbhaVerifyOtpSubSubRequest;
import com.hims.m1.abdm_response.AbdmCertificateResponse;
import com.hims.m1.abdm_response.AbdmVerifyResponse;
import com.hims.m1.abdm_response.DefaultOtpSendResponse;
import com.hims.m1.abdm_response.MobilAbhaResponse;
import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.apiResponse.ResponseUtils;
import com.hims.m1.request.VerificationRequest;
import com.hims.m1.request.VerifyIndexOTPRequest;
import com.hims.m1.request.VerifyOTPRequest;
import com.hims.m1.response.OtpSendResponse;
import com.hims.m1.response.OtpVerificationResponse;
import com.hims.m1.response.ParseErrorResponse;
import com.hims.m1.service.CaptchaService;
import com.hims.m1.service.VerificationAbhaService;
import com.hims.m1.util.AadhaarEncryptor;
import com.hims.m1.util.ErrorHandel;
import com.hims.m1.util.NhaHeaderUtil;
import com.hims.m1.util.VerhoeffValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Service
public class VerificationAbhaServiceImpl implements VerificationAbhaService {


    @Autowired
    VerificationMapper verificationMapper;

    @Autowired
    private CaptchaService captchaService;


    @Autowired
    private AadhaarEncryptor aadhaarEncryptor;

    @Override
    public Mono<ApiResponse<OtpSendResponse>> verificationByNumber(VerificationRequest verificationRequest) throws Exception {

        Mono<DefaultOtpSendResponse> sendOtpResponse = null;
        AbdmCertificateResponse abdmCertificateResponse = aadhaarEncryptor.fetchCertificates();
        AbhaSendOtpRequest request = new AbhaSendOtpRequest();
        String isTyp = "";

        if (verificationRequest.getInputType().equalsIgnoreCase("1001")) {
            String aadhaar = verificationRequest.getInputNumber();

            if (verificationRequest.getKey() == null) {
                return Mono.just(ResponseUtils.createFailureResponse("Key not found", false));
            }

            byte[] aesKey = NhaHeaderUtil.decryptAESKey(verificationRequest.getKey());
            aadhaar = NhaHeaderUtil.decryptData(aadhaar, aesKey);

            if (!aadhaar.matches("^[2-9][0-9]{11}$")) {
                return Mono.just(ResponseUtils.createFailureResponse(
                        "Invalid Aadhaar number.", false
                ));
            }
            verificationRequest.setInputNumber(aadhaar);

            boolean checkAdhaar = VerhoeffValidator.validate(verificationRequest.getInputNumber());
            if (!checkAdhaar) {
                return Mono.just(ResponseUtils.createFailureResponse("Invalid Aadhaar number.", false));
            }

            String encryptNumber = aadhaarEncryptor.doEncrypt(verificationRequest.getInputNumber(), abdmCertificateResponse);

            request.setScope(List.of("abha-login", "aadhaar-verify"));
            request.setLoginHint("aadhaar");
            request.setLoginId(encryptNumber);
            request.setOtpSystem("aadhaar");
            isTyp = "1";

        } else if (verificationRequest.getInputType().equalsIgnoreCase("1004")) {

            String digits = verificationRequest.getInputNumber().replaceAll("\\D", "");

            if (digits.length() != 14) {
                return Mono.just(ResponseUtils.createFailureResponse("Invalid ABHAA  number.", false));
            }

            String validNumber = String.format("%s-%s-%s-%s",
                    digits.substring(0, 2),
                    digits.substring(2, 6),
                    digits.substring(6, 10),
                    digits.substring(10, 14));

            String encryptNumber = aadhaarEncryptor.doEncrypt(validNumber, abdmCertificateResponse);

            if (verificationRequest.getAuthMethod() == null) {
                return Mono.just(ResponseUtils.createFailureResponse("Select AUTH type first.", false));
            }

            if (verificationRequest.getAuthMethod().equalsIgnoreCase("Mobile")) {
                request.setScope(List.of("abha-login", "mobile-verify"));
                request.setLoginHint("abha-number");

                request.setLoginId(encryptNumber);
                request.setOtpSystem("abdm");
            } else if (verificationRequest.getAuthMethod().equalsIgnoreCase("Aadhaar")) {
                request.setScope(List.of("abha-login", "aadhaar-verify"));
                request.setLoginHint("abha-number");

                request.setLoginId(encryptNumber);
                request.setOtpSystem("aadhaar");
            } else {
                return Mono.just(ResponseUtils.createFailureResponse("Select AUTH type first.", false));
            }


            isTyp = "1";

        } else if (verificationRequest.getInputType().equalsIgnoreCase("1005")) {
            String encryptNumber = aadhaarEncryptor.doEncrypt(verificationRequest.getInputNumber(), abdmCertificateResponse);

            request.setScope(List.of("abha-login", "aadhaar-verify"));
            request.setLoginHint("abha-number");
            request.setLoginId(encryptNumber);
            request.setOtpSystem("aadhaar");
            isTyp = "1";
        } else if (verificationRequest.getInputType().equalsIgnoreCase("1003")) {

            return verificationMapper
                    .findAbhaNumberViaAbhaAdress1(verificationRequest.getInputNumber())
                    .map(res -> {
                        OtpSendResponse response = new OtpSendResponse();
                        // new fields mapping
                        response.setHealthIdNumber(res.getHealthIdNumber());
                        response.setAbhaAddress(res.getAbhaAddress());
                        response.setAuthMethods(res.getAuthMethods());
                        response.setBlockedAuthMethods(res.getBlockedAuthMethods());
                        response.setStatus(res.getStatus());
                        response.setMessage(res.getMessage());
                        response.setFullName(res.getFullName());
                        response.setMobile(res.getMobile());
                        response.setOtpType(verificationRequest.getInputType());
                        return ResponseUtils.createSuccessResponse(
                                response,
                                true,
                                res.getMessage()
                        );
                    })
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
                        return Mono.just(
                                ResponseUtils.createFailureResponse(
                                        parseErrorResponse.getErrorMsg(),
                                        false,
                                        parseErrorResponse.getErrorCode()
                                )
                        );
                    });
        } else if (verificationRequest.getInputType().equalsIgnoreCase("1002")) {
            String encryptNumber = aadhaarEncryptor.doEncrypt(verificationRequest.getInputNumber(), abdmCertificateResponse);
            isTyp = "1";
            String mobile = verificationRequest.getInputNumber();
            if (!mobile.matches("^[0-9]{10}$")) {
                return Mono.just(ResponseUtils.createFailureResponse(
                        "Invalid mobile number. It must be exactly 10 digits.",
                        false
                ));
            }

            Map<String, Object> requestBody = new HashMap<>();
            List<String> scopeList = new ArrayList<>();
            scopeList.add("search-abha");

            requestBody.put("scope", scopeList);
            requestBody.put("mobile", encryptNumber);

            Mono<List<MobilAbhaResponse>> sendOtpResponse1 = verificationMapper.searchByMobileNumber(requestBody);

            return sendOtpResponse1
                    .map(res -> {
                        boolean status = false;
                        String msg = "";
                        if (res.get(0).getABHA() != null) {
                            status = true;
                            msg = "Data fetch sucessfully.";
                        } else {
                            status = false;
                            msg = "No Data found.";

                        }

                        OtpSendResponse response = new OtpSendResponse();
                        response.setIsType("1");
                        response.setOtpType(verificationRequest.getInputType());
                        response.setAbhaResponse(res);
                        return ResponseUtils.createSuccessResponse(response, status, msg);
                    })
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
                        return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));
                    });

        }


        isTyp = "1";
        sendOtpResponse = verificationMapper.sendOtp(request);

        String finalIsTyp = isTyp;
        return sendOtpResponse
                .map(res -> {
                    OtpSendResponse response = new OtpSendResponse();
                    response.setTxnId(res.getTxnId());
                    response.setIsType(finalIsTyp);
                    response.setOtpType(verificationRequest.getInputType());
                    return ResponseUtils.createSuccessResponse(response, true, res.getMessage());
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
                    return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));
                });

    }

    @Override
    public Mono<ApiResponse<OtpSendResponse>> sendOtpUsingAuth(VerificationRequest verificationRequest) throws Exception {


        AbdmCertificateResponse abdmCertificateResponse = aadhaarEncryptor.fetchCertificates();
        AbhaSendOtpRequest request = new AbhaSendOtpRequest();
        String isTyp = "";

        Mono<DefaultOtpSendResponse> sendOtpResponse = null;

        if (verificationRequest.getInputType().equalsIgnoreCase("1003")) {
            isTyp = "2";
            if (verificationRequest.getAuthMethod().contains("Mobile")) {
                sendOtpResponse = verificationMapper.findAbhaNumberViaAbhaAdress(verificationRequest.getInputNumber(), "Mobile");
            } else if (verificationRequest.getAuthMethod().contains("Aadhaar")) {
                sendOtpResponse = verificationMapper.findAbhaNumberViaAbhaAdress(verificationRequest.getInputNumber(), "Aadhaar");
            }

        } else {
            return Mono.just(ResponseUtils.createFailureResponse("Invalid Input Type.", false));
        }


        String finalIsTyp = isTyp;
        return sendOtpResponse
                .map(res -> {
                    OtpSendResponse response = new OtpSendResponse();
                    response.setTxnId(res.getTxnId());
                    response.setIsType(finalIsTyp);
                    response.setOtpType(verificationRequest.getInputType());
                    return ResponseUtils.createSuccessResponse(response, true, res.getMessage());
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
                    return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));
                });

    }


    @Override
    public Mono<ApiResponse<OtpVerificationResponse>> verifyOtpWithMobileNumber(VerifyOTPRequest verifyOTPRequest) throws Exception {


        AbhaVerifyOtpMainRequest request = new AbhaVerifyOtpMainRequest();

        AbdmCertificateResponse abdmCertificateResponse = aadhaarEncryptor.fetchCertificates();
        String encryptOtp = aadhaarEncryptor.doEncrypt(verifyOTPRequest.getOtp(), abdmCertificateResponse);


        if (verifyOTPRequest.getInputType().equalsIgnoreCase("1001")) {

            request.setScope(List.of("abha-login", "aadhaar-verify"));

            AbhaVerifyOtpSubSubRequest otp = new AbhaVerifyOtpSubSubRequest();
            otp.setTxnId(verifyOTPRequest.getTxnId());                 // txnId from OTP send API
            otp.setOtpValue(encryptOtp);       // encrypted OTP

            AbhaVerifyOtpSubRequest authData = new AbhaVerifyOtpSubRequest();
            authData.setAuthMethods(List.of("otp"));
            authData.setOtp(otp);

            request.setAuthData(authData);


        } else if (verifyOTPRequest.getInputType().equalsIgnoreCase("1002")) {
            request.setScope(List.of("abha-login", "mobile-verify"));

            AbhaVerifyOtpSubSubRequest otp = new AbhaVerifyOtpSubSubRequest();
            otp.setTxnId(verifyOTPRequest.getTxnId());                 // txnId from OTP send API
            otp.setOtpValue(encryptOtp);      // encrypted OTP

            AbhaVerifyOtpSubRequest authData = new AbhaVerifyOtpSubRequest();
            authData.setAuthMethods(List.of("otp"));
            authData.setOtp(otp);

            request.setAuthData(authData);


        } else if (verifyOTPRequest.getInputType().equalsIgnoreCase("1003")) {


            if (verifyOTPRequest.getAuthMethod() == null) {
                return Mono.just(ResponseUtils.createFailureResponse("Select AUTH type first.", false));
            }

            if (verifyOTPRequest.getAuthMethod().equalsIgnoreCase("Mobile")) {
                request.setScope(List.of("abha-address-login", "mobile-verify"));

                AbhaVerifyOtpSubSubRequest otp = new AbhaVerifyOtpSubSubRequest();
                otp.setTxnId(verifyOTPRequest.getTxnId());
                otp.setOtpValue(encryptOtp);

                AbhaVerifyOtpSubRequest authData = new AbhaVerifyOtpSubRequest();
                authData.setAuthMethods(List.of("otp"));
                authData.setOtp(otp);
                request.setAuthData(authData);

            } else if (verifyOTPRequest.getAuthMethod().equalsIgnoreCase("Aadhaar")) {
                request.setScope(List.of("abha-address-login", "aadhaar-verify"));

                AbhaVerifyOtpSubSubRequest otp = new AbhaVerifyOtpSubSubRequest();
                otp.setTxnId(verifyOTPRequest.getTxnId());
                otp.setOtpValue(encryptOtp);

                AbhaVerifyOtpSubRequest authData = new AbhaVerifyOtpSubRequest();
                authData.setAuthMethods(List.of("otp"));
                authData.setOtp(otp);
                request.setAuthData(authData);
            } else {
                return Mono.just(ResponseUtils.createFailureResponse("Select AUTH type first.", false));
            }


        } else if (verifyOTPRequest.getInputType().equalsIgnoreCase("1004")) {

            if (verifyOTPRequest.getAuthMethod() == null) {
                return Mono.just(ResponseUtils.createFailureResponse("Select AUTH type first.", false));
            }

            if (verifyOTPRequest.getAuthMethod().equalsIgnoreCase("Mobile")) {
                request.setScope(List.of("abha-login", "mobile-verify"));

                AbhaVerifyOtpSubSubRequest otp = new AbhaVerifyOtpSubSubRequest();
                otp.setTxnId(verifyOTPRequest.getTxnId());                 // txnId from OTP send API
                otp.setOtpValue(encryptOtp);      // encrypted OTP

                AbhaVerifyOtpSubRequest authData = new AbhaVerifyOtpSubRequest();
                authData.setAuthMethods(List.of("otp"));
                authData.setOtp(otp);
                request.setAuthData(authData);
            } else if (verifyOTPRequest.getAuthMethod().equalsIgnoreCase("Aadhaar")) {
                request.setScope(List.of("abha-login", "aadhaar-verify"));

                AbhaVerifyOtpSubSubRequest otp = new AbhaVerifyOtpSubSubRequest();
                otp.setTxnId(verifyOTPRequest.getTxnId());                 // txnId from OTP send API
                otp.setOtpValue(encryptOtp);      // encrypted OTP

                AbhaVerifyOtpSubRequest authData = new AbhaVerifyOtpSubRequest();
                authData.setAuthMethods(List.of("otp"));
                authData.setOtp(otp);
                request.setAuthData(authData);
            } else {
                return Mono.just(ResponseUtils.createFailureResponse("Select AUTH type first.", false));
            }


        } else if (verifyOTPRequest.getInputType().equalsIgnoreCase("1005")) {
            request.setScope(List.of("abha-login", "aadhaar-verify"));

            AbhaVerifyOtpSubSubRequest otp = new AbhaVerifyOtpSubSubRequest();
            otp.setTxnId(verifyOTPRequest.getTxnId());                 // txnId from OTP send API
            otp.setOtpValue(encryptOtp);      // encrypted OTP

            AbhaVerifyOtpSubRequest authData = new AbhaVerifyOtpSubRequest();
            authData.setAuthMethods(List.of("otp"));
            authData.setOtp(otp);

            request.setAuthData(authData);


        } else {
            return Mono.just(ResponseUtils.createFailureResponse("Invalid Input type.", false));
        }

        Mono<AbdmVerifyResponse> sendOtpResponse = null;

        if (verifyOTPRequest.getInputType().equalsIgnoreCase("1003")) {
            sendOtpResponse = verificationMapper.verifyAbhaAddressOtp(request);
        } else {
            sendOtpResponse = verificationMapper.verifyOtp(request);
        }


        return sendOtpResponse
                .map(res -> {
                    boolean status = true;
                    if (res.getAuthResult().equalsIgnoreCase("failed")) {
                        status = false;
                    }
                    OtpVerificationResponse response = new OtpVerificationResponse();
                    Optional.ofNullable(res.getToken()).ifPresent(response::setXToken);
                    Optional.ofNullable(verifyOTPRequest.getIsType()).ifPresent(response::setIsType);
                    Optional.ofNullable(res.getMessage()).ifPresent(response::setMessage);
                    Optional.ofNullable(res.getTxnId()).ifPresent(response::setTxnId);
                    Optional.ofNullable(verifyOTPRequest.getIsType()).ifPresent(response::setIsType);
                    Optional.ofNullable(res.getAccounts()).ifPresent(response::setAccounts);

                    return ResponseUtils.createSuccessResponse(response, status, res.getMessage());
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
                    return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));
                });
    }


    @Override
    public Mono<ApiResponse<OtpSendResponse>> verifyOtpWithMobileNumber(VerifyIndexOTPRequest verifyOTPRequest) throws Exception {


        AbdmCertificateResponse abdmCertificateResponse = aadhaarEncryptor.fetchCertificates();
        String encryptIndex = aadhaarEncryptor.doEncrypt(verifyOTPRequest.getIndex(), abdmCertificateResponse);


        Map<String, Object> requestBody = new HashMap<>();

        List<String> scopeList = new ArrayList<>();
        scopeList.add("abha-login");
        scopeList.add("search-abha");
        scopeList.add("mobile-verify");

        requestBody.put("scope", scopeList);
        requestBody.put("loginHint", "index");
        requestBody.put("loginId", encryptIndex); // RSA encrypted index value
        requestBody.put("otpSystem", "abdm");
        requestBody.put("txnId", verifyOTPRequest.getTxnId());


        Mono<DefaultOtpSendResponse> sendOtpResponse = verificationMapper.sendOtpViaMobileNumber(requestBody);

        return sendOtpResponse
                .map(res -> {
                    OtpSendResponse response = new OtpSendResponse();
                    response.setTxnId(res.getTxnId());
                    Optional.ofNullable(verifyOTPRequest.getIsType()).ifPresent(response::setIsType);
                    return ResponseUtils.createSuccessResponse(response, true, res.getMessage());
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
                    return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));
                });
    }


}
