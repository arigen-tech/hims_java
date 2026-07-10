package com.hims.m1.service.serviceImpl;

import com.hims.m1.Mapper.CreateAbhaMapper;
import com.hims.m1.Mapper.SaveApiLogMapper;
import com.hims.m1.abdm_response.*;
import com.hims.m1.abdm_response.GetAbhaDetails.AbhaProfileResponse;
import com.hims.m1.abdm_response.GetAbhaDetails.LocalizedDetails;
import com.hims.m1.abdm_response.UpdateEmail.EmailLinkResponse;
import com.hims.m1.abdm_response.UpdateMobile.UpdateMobileResponse;
import com.hims.m1.abdm_response.createAbha.CreateAbhaByAadhaarResponse;
import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.apiResponse.ResponseUtils;
import com.hims.m1.request.*;
import com.hims.m1.response.*;
import com.hims.m1.service.CaptchaService;
import com.hims.m1.service.CreateAbhaService;
import com.hims.m1.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class CreateAbhaServiceImpl implements CreateAbhaService {


    @Autowired
    CreateAbhaMapper createAbhaMapper;
    @Autowired
    private SaveApiLogMapper saveApiLogMapper;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private AadhaarEncryptor aadhaarEncryptor;

    @Override
    public Mono<ApiResponse<OtpSendResponse>> sendOtpToAdhar(CreateAbhaSendOTPRequest verificationRequest) throws Exception {
        AbdmCertificateResponse abdmCertificateResponse = aadhaarEncryptor.fetchCertificates();
       
        String aadhaar = verificationRequest.getAadhaarNumber();

        if (verificationRequest.getKey() == null) {
            return Mono.just(ResponseUtils.createFailureResponse("Key not found", false));
        }

        byte[] aesKey = NhaHeaderUtil.decryptAESKey(verificationRequest.getKey());
        aadhaar = NhaHeaderUtil.decryptData(aadhaar, aesKey);
        String encryptNumber = aadhaarEncryptor.doEncrypt(aadhaar, abdmCertificateResponse);


        if (!aadhaar.matches("^[2-9][0-9]{11}$")) {
            return Mono.just(ResponseUtils.createFailureResponse("Invalid Aadhaar number.", false));
        }

        boolean checkAdhaar = VerhoeffValidator.validate(aadhaar);
        if (!checkAdhaar) {
            return Mono.just(ResponseUtils.createFailureResponse("Invalid Aadhaar number.", false));
        }

        saveApiLogMapper.saveConsent(verificationRequest,aadhaar);

        Map<String, Object> requestOtp = CreateAbdmRequest.createRequestForOtp("", encryptNumber);

        Mono<DefaultOtpSendResponse> sendOtpResponse = createAbhaMapper.sendAbhaOtp(requestOtp);
        return sendOtpResponse.map(res -> {
            OtpSendResponse response = new OtpSendResponse();
            response.setTxnId(res.getTxnId());
            response.setIsType("1");
            return ResponseUtils.createSuccessResponse(response, true, res.getMessage());
        }).onErrorResume(WebClientResponseException.class, ex -> {
            ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
            return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));

        });
    }


    @Override
    public Mono<ApiResponse<CreateAbhaResponse>> createAbhaForVeryingOtp(VerifingAbhaSendOTPRequest verifyOTPRequest) throws Exception {


        AbdmCertificateResponse abdmCertificateResponse = aadhaarEncryptor.fetchCertificates();
        String encryptOtp = aadhaarEncryptor.doEncrypt(verifyOTPRequest.getOtp(), abdmCertificateResponse);
        String encryptMobileNumber = aadhaarEncryptor.doEncrypt(verifyOTPRequest.getMobileNumber(), abdmCertificateResponse);

        Map<String, Object> makingRequest = CreateAbdmRequest.createRequest(verifyOTPRequest.getTnxId(), encryptOtp, verifyOTPRequest.getMobileNumber());
        Mono<CreateAbhaByAadhaarResponse> sendOtpResponse = createAbhaMapper.verifyAbhaOtp(makingRequest, encryptMobileNumber, verifyOTPRequest.getMobileNumber());

        return sendOtpResponse.map(res -> {
                    CreateAbhaResponse response = new CreateAbhaResponse();
                    Optional.ofNullable(res.getTxnId()).ifPresent(response::setTxnId);
                    Optional.ofNullable(res.getTokens().getToken()).ifPresent(response::setXToken);
                    Optional.ofNullable(res.getTokens().getRefreshToken()).ifPresent(response::setRefreshToken);
                    Optional.ofNullable(res.getTokens().getExpiresIn()).ifPresent(response::setExpiresIn);
                    Optional.ofNullable(res.getTokens().getRefreshExpiresIn()).ifPresent(response::setRefreshExpiresIn);
                    Optional.ofNullable(res.getIsNew()).ifPresent(response::setIsNew);
                    response.setIsType("1");
                    Optional.ofNullable(res.getIsMatchToAadhaar()).ifPresent(response::setIsMatchToAadhaar);

                    return ResponseUtils.createSuccessResponse(response, true, res.getMessage());
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
                    return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));
                });
    }


    @Override
    public Mono<ApiResponse<CreateAbhaResponse>> verifyOtpToAadhaarAnotherNumber(VerifingAbhaSendOTPAnotherRequest verifyOTPRequest) throws Exception {


        AbdmCertificateResponse abdmCertificateResponse = aadhaarEncryptor.fetchCertificates();
        String encryptOtp = aadhaarEncryptor.doEncrypt(verifyOTPRequest.getOtp(), abdmCertificateResponse);

        Map<String, Object> makingRequest = CreateAbdmRequest.createRequestVerifyFoAnotherNumber(verifyOTPRequest.getTnxId(), encryptOtp);
        Mono<AbdmVerifyResponse> sendOtpResponse = createAbhaMapper.verifyAbhaAnotherNumberOtp(makingRequest);

        return sendOtpResponse.map(res -> {
            CreateAbhaResponse response = new CreateAbhaResponse();
            boolean status = false;
            if (res.getAuthResult().equalsIgnoreCase("Failed")) {
                Optional.ofNullable(res.getTxnId()).ifPresent(response::setTxnId);
                Optional.ofNullable(res.getAuthResult()).ifPresent(response::setXToken);
            } else {
                status = true;
                Optional.ofNullable(res.getTxnId()).ifPresent(response::setTxnId);
                response.setIsType("1");
                Optional.ofNullable(res.getAuthResult()).ifPresent(response::setXToken);
            }

            return ResponseUtils.createSuccessResponse(response, status, res.getMessage());
        }).onErrorResume(WebClientResponseException.class, ex -> {
            ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
            return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));
        });
    }


    @Override
    public Mono<ApiResponse<CreateAbhaResponse>> verifyEmail(UpdateEmailVerificationRequest verifyOTPRequest) throws Exception {

        AbdmCertificateResponse abdmCertificateResponse = aadhaarEncryptor.fetchCertificates();
        String encryptEmail = aadhaarEncryptor.doEncrypt(verifyOTPRequest.getEmailId(), abdmCertificateResponse);

        Map<String, Object> makingRequest = CreateAbdmRequest.createRequestForEmail(encryptEmail);

        Mono<CreateAbhaByAadhaarResponse> sendOtpResponse = createAbhaMapper.verifyEmail(makingRequest, verifyOTPRequest.getXToken());

        return sendOtpResponse.map(res -> {
            CreateAbhaResponse response = new CreateAbhaResponse();

            return ResponseUtils.createSuccessResponse(response, true, res.getMessage());
        }).onErrorResume(WebClientResponseException.class, ex -> {
            ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
            return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));

        });
    }


    @Override
    public Mono<ApiResponse<AbhaSuggetionResponse>> abhaAddressSuggestion(AbhaSuggestionRequest verifyOTPRequest) throws Exception {

        Mono<AbhaAddressSuggestionRequest> sendOtpResponse = createAbhaMapper.getAbhaSuggestion(verifyOTPRequest.getTnxId());

        return sendOtpResponse.map(res -> {
            AbhaSuggetionResponse response = new AbhaSuggetionResponse();
            Optional.ofNullable(res.getTxnId()).ifPresent(response::setTnxId);
            response.setIsType("1");
            Optional.ofNullable(res.getAbhaAddressList()).ifPresent(response::setAbhaAddressList);

            return ResponseUtils.createSuccessResponse(response, true, "Suggestion list fetch sucessfully.");
        }).onErrorResume(WebClientResponseException.class, ex -> {
            ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
            return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));

        });
    }


    @Override
    public Mono<ApiResponse<GetAbhaProfileResponse>> getProfileDetails(AbhaDownlaodAndDetailsRequest verifyOTPRequest) throws Exception {
        Mono<?> profileMono;

        if ("1".equalsIgnoreCase(verifyOTPRequest.getIsType())) {
            profileMono = createAbhaMapper.getAbhaDetails(verifyOTPRequest.getXToken());
        } else {
            profileMono = createAbhaMapper.getAbhaDetailsViaAbhaAdress(verifyOTPRequest.getXToken());
        }

        return profileMono.map(res -> {

            GetAbhaProfileResponse response = new GetAbhaProfileResponse();

            if (res instanceof AbhaProfileResponse profile) {

                mapCommonFields(profile, response);

            } else if (res instanceof AbhaProfileViaAbhaAddress profile) {

                mapCommonFields(profile, response);
            }

            return ResponseUtils.createSuccessResponse(
                    response,
                    true,
                    "ABHA profile fetched successfully."
            );
        });

    }

    private GetAbhaProfileResponse mapCommonFields(AbhaProfileResponse profile, GetAbhaProfileResponse response) {

        Optional.ofNullable(profile.getPincode()).ifPresent(response::setPincode);
        Optional.ofNullable(profile.getAddress()).ifPresent(response::setAddress);
        Optional.ofNullable(profile.getStateCode()).ifPresent(response::setStateCode);
        Optional.ofNullable(profile.getDistrictCode()).ifPresent(response::setDistrictCode);
        Optional.ofNullable(profile.getSubdistrictName()).ifPresent(response::setSubdistrictName);
        Optional.ofNullable(profile.getDistrictName()).ifPresent(response::setDistrictName);
        Optional.ofNullable(profile.getStateName()).ifPresent(response::setStateName);

        Optional.ofNullable(profile.getAbhaNumber()).ifPresent(response::setAbhaNumber);
        Optional.ofNullable(profile.getName()).ifPresent(response::setName);
        Optional.ofNullable(profile.getPreferredAbhaAddress()).ifPresent(response::setPreferredAbhaAddress);
        Optional.ofNullable(profile.getProfilePhoto()).ifPresent(response::setProfilePhoto);
        Optional.ofNullable(profile.getMobile()).ifPresent(response::setMobile);
        Optional.ofNullable(profile.getAddharNo()).ifPresent(response::setAddharNo);

        Optional.ofNullable(profile.getGender())
                .map(g -> switch (g.toUpperCase()) {
                    case "M" -> "Male";
                    case "F" -> "Female";
                    default -> g;
                })
                .ifPresent(response::setGender);

        if (profile.getDayOfBirth() != null &&
                profile.getMonthOfBirth() != null &&
                profile.getYearOfBirth() != null) {

            response.setDayOfBirth(
                    profile.getDayOfBirth() + "-" +
                            profile.getMonthOfBirth() + "-" +
                            profile.getYearOfBirth()
            );
        }

        Optional.ofNullable(profile.getLocalizedDetails())
                .map(LocalizedDetails::getGender)
                .ifPresent(response::setHindiGender);

        Optional.ofNullable(profile.getLocalizedDetails())
                .map(LocalizedDetails::getName)
                .ifPresent(response::setHindiName);

        return response;
    }


    private GetAbhaProfileResponse mapCommonFields(AbhaProfileViaAbhaAddress profile, GetAbhaProfileResponse response) {

        Optional.ofNullable(profile.getPinCode()).ifPresent(response::setPincode);
        Optional.ofNullable(profile.getAddress()).ifPresent(response::setAddress);
        Optional.ofNullable(profile.getStateCode()).ifPresent(response::setStateCode);
        Optional.ofNullable(profile.getDistrictCode()).ifPresent(response::setDistrictCode);
        Optional.ofNullable(profile.getSubDistrictName()).ifPresent(response::setSubdistrictName);
        Optional.ofNullable(profile.getDistrictName()).ifPresent(response::setDistrictName);
        Optional.ofNullable(profile.getStateName()).ifPresent(response::setStateName);

        Optional.ofNullable(profile.getAbhaNumber()).ifPresent(response::setAbhaNumber);
        Optional.ofNullable(profile.getFullName()).ifPresent(response::setName);
        Optional.ofNullable(profile.getAbhaAddress()).ifPresent(response::setPreferredAbhaAddress);
        Optional.ofNullable(profile.getProfilePhoto()).ifPresent(response::setProfilePhoto);
        Optional.ofNullable(profile.getMobile()).ifPresent(response::setMobile);

        Optional.ofNullable(profile.getGender())
                .map(g -> switch (g.toUpperCase()) {
                    case "M" -> "Male";
                    case "F" -> "Female";
                    default -> g;
                })
                .ifPresent(response::setGender);

        if (profile.getDayOfBirth() != null &&
                profile.getMonthOfBirth() != null &&
                profile.getYearOfBirth() != null) {

            response.setDayOfBirth(
                    profile.getDayOfBirth() + "-" +
                            profile.getMonthOfBirth() + "-" +
                            profile.getYearOfBirth()
            );
        }

        return response;
    }


    @Override
    public Mono<ApiResponse<OtpSendResponse>> updateEmailSendOtp(UpdateEmailVerificationRequest verifyOTPRequest) throws Exception {


        AbdmCertificateResponse abdmCertificateResponse = aadhaarEncryptor.fetchCertificates();
        String encryptEmail = aadhaarEncryptor.doEncrypt(verifyOTPRequest.getEmailId(), abdmCertificateResponse);

        Map<String, Object> makingRequest = CreateAbdmRequest.createRequestForEmailUpdate(encryptEmail);
        Mono<DefaultOtpSendResponse> sendOtpResponse = createAbhaMapper.updateEmailViaOtp(makingRequest, verifyOTPRequest.getXToken());

        return sendOtpResponse.map(res -> {
            OtpSendResponse response = new OtpSendResponse();
            Optional.ofNullable(res.getTxnId()).ifPresent(response::setTxnId);

            return ResponseUtils.createSuccessResponse(response, true, res.getMessage());
        }).onErrorResume(WebClientResponseException.class, ex -> {
            ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
            return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));

        });
    }


    @Override
    public Mono<ApiResponse<UpdateEmailResponse>> updateEmailVerifyOtp(UpdateVerifyRequest verifyOTPRequest) throws Exception {


        AbdmCertificateResponse abdmCertificateResponse = aadhaarEncryptor.fetchCertificates();
        String encryptEmail = aadhaarEncryptor.doEncrypt(verifyOTPRequest.getOtp(), abdmCertificateResponse);

        Map<String, Object> makingRequest = CreateAbdmRequest.createRequestForEmailUpdateVerify(encryptEmail, verifyOTPRequest.getTxnId());

        Mono<EmailLinkResponse> sendOtpResponse = createAbhaMapper.updateVerifyEmailOtp(makingRequest, verifyOTPRequest.getXToken());

        return sendOtpResponse.map(res -> {
            UpdateEmailResponse response = new UpdateEmailResponse();
            response.setIsType("1");
            Optional.ofNullable(res.getTxnId()).ifPresent(response::setTxnId);
            Optional.ofNullable(res.getAccounts()).ifPresent(response::setAccounts);
            Optional.ofNullable(res.getAuthResult()).ifPresent(response::setAuthResult);

            return ResponseUtils.createSuccessResponse(response, true, res.getMessage());
        }).onErrorResume(WebClientResponseException.class, ex -> {
            ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
            return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));
        });
    }


    @Override
    public Mono<ApiResponse<OtpSendResponse>> updateMobileSendOtp(UpdateMobileVerificationRequest verifyOTPRequest) throws Exception {


        AbdmCertificateResponse abdmCertificateResponse = aadhaarEncryptor.fetchCertificates();
        String encryptMobile = aadhaarEncryptor.doEncrypt(verifyOTPRequest.getMobileNumber(), abdmCertificateResponse);

        Map<String, Object> makingRequest = CreateAbdmRequest.createRequestForMobileUpdate(encryptMobile);
        Mono<DefaultOtpSendResponse> sendOtpResponse = createAbhaMapper.updateMobileViaOtp(makingRequest, verifyOTPRequest.getXToken());

        return sendOtpResponse.map(res -> {
            OtpSendResponse response = new OtpSendResponse();
            response.setIsType("1");
            Optional.ofNullable(res.getTxnId()).ifPresent(response::setTxnId);

            return ResponseUtils.createSuccessResponse(response, true, "Suggestion list fetch sucessfully.");
        }).onErrorResume(WebClientResponseException.class, ex -> {
            ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
            return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));

        });
    }


    @Override
    public Mono<ApiResponse<UpdateEmailResponse>> updateMobileVerifyOtp(UpdateVerifyRequest verifyOTPRequest) throws Exception {

        AbdmCertificateResponse abdmCertificateResponse = aadhaarEncryptor.fetchCertificates();
        String encryptMobile = aadhaarEncryptor.doEncrypt(verifyOTPRequest.getOtp(), abdmCertificateResponse);

        Map<String, Object> makingRequest = CreateAbdmRequest.createRequestForMobileUpdateVerify(encryptMobile, verifyOTPRequest.getTxnId());


        Mono<UpdateMobileResponse> verifyOTP = createAbhaMapper.updateVerifyMobileOtp(makingRequest, verifyOTPRequest.getXToken());

        return verifyOTP.map(res -> {
            UpdateEmailResponse response = new UpdateEmailResponse();
            Optional.ofNullable(res.getTxnId()).ifPresent(response::setTxnId);

            return ResponseUtils.createSuccessResponse(response, true, res.getMessage());
        }).onErrorResume(WebClientResponseException.class, ex -> {
            ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
            return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));

        });

    }

    @Override
    public Mono<ApiResponse<AbdmUpdateAddressResponse>> updateSuggestion(AbhaSuggestionUpdateRequest abhaSuggestionUpdateRequest) throws Exception {

        Map<String, Object> requestData = CreateAbdmRequest.createRequestForAbhaAddresssUpdate(abhaSuggestionUpdateRequest.getTnxId(), abhaSuggestionUpdateRequest.getAbhaAddress());
        Mono<AbdmUpdateAddressResponse> sendOtpResponse = createAbhaMapper.updateSuggestion(requestData);

        return sendOtpResponse.map(res -> {
            return ResponseUtils.createSuccessResponse(res, true, "Suggestion list update sucessfully.");
        }).onErrorResume(WebClientResponseException.class, ex -> {
            ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
            return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));

        });
    }


    @Override
    public Mono<ApiResponse<AbhaProfileResponse>> updateProfilePhoto(UpdateProfileRequest updateProfileRequest) throws Exception {

        Map<String, Object> makingRequest = CreateAbdmRequest.createRequestUpdateProfile(updateProfileRequest.getProfilePhoto());
        Mono<AbhaProfileResponse> sendOtpResponse = createAbhaMapper.updateProfilePhoto(makingRequest, updateProfileRequest.getXToken());

        return sendOtpResponse.map(res -> {
            return ResponseUtils.createSuccessResponse(res, true, "Profile photo update sucessfully.");
        }).onErrorResume(WebClientResponseException.class, ex -> {
            ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
            return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));
        });
    }


    @Override
    public Mono<ApiResponse<DownlaodAbhaCardResponse>> abhaCardDownload(AbhaDownlaodAndDetailsRequest verifyOTPRequest) throws Exception {
        Mono<byte[]> sendOtpResponse = null;

        if ("1".equalsIgnoreCase(verifyOTPRequest.getIsType())) {
            sendOtpResponse = createAbhaMapper.downlaodAbhaCard(verifyOTPRequest.getXToken());
        } else {
            sendOtpResponse = createAbhaMapper.downlaodAbhaCardViaAbhaAdress(verifyOTPRequest.getXToken());
        }

//        Mono<byte[]> sendOtpResponse = createAbhaMapper.downlaodAbhaCard(verifyOTPRequest.getXToken());

        return sendOtpResponse.map(res -> {
            DownlaodAbhaCardResponse response = new DownlaodAbhaCardResponse();
            Optional.ofNullable(res).ifPresent(response::setAbhaCard);

            return ResponseUtils.createSuccessResponse(response, true, "Abha download sucessfully.");
        }).onErrorResume(WebClientResponseException.class, ex -> {
            ParseErrorResponse parseErrorResponse = ErrorHandel.parseError(ex);
            return Mono.just(ResponseUtils.createFailureResponse(parseErrorResponse.getErrorMsg(), false, parseErrorResponse.getErrorCode()));
        });
    }


}
