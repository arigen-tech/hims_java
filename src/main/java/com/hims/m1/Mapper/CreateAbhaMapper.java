package com.hims.m1.Mapper;


import com.hims.m1.abdm_response.*;
import com.hims.m1.abdm_response.GetAbhaDetails.AbhaProfileResponse;
import com.hims.m1.abdm_response.UpdateEmail.EmailLinkResponse;
import com.hims.m1.abdm_response.UpdateMobile.UpdateMobileResponse;
import com.hims.m1.abdm_response.createAbha.CreateAbhaByAadhaarResponse;
import com.hims.m1.client.AbdmProperties;
import com.hims.m1.service.MapperApiLogService;
import com.hims.m1.service.SessionAndCertService;
import com.hims.m1.util.CreateAbdmRequest;
import com.hims.m1.util.NhaHeaderUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class CreateAbhaMapper {

    private final SessionAndCertService sessionAndCertService;
    private final WebClient webClient;
    private final AbdmProperties abdmProperties;

    private final MapperApiLogService mapperApiLogService;

    public CreateAbhaMapper(SessionAndCertService sessionAndCertService,
                            AbdmProperties abdmProperties,
                            WebClient.Builder webClientBuilder,
                            MapperApiLogService mapperApiLogService) {
        this.sessionAndCertService = sessionAndCertService;
        this.abdmProperties = abdmProperties;
        this.webClient = webClientBuilder.build();
        this.mapperApiLogService = mapperApiLogService;
    }


    public Mono<DefaultOtpSendResponse> sendAbhaOtp(Map<String, Object> request) throws Exception {


        return getAccessToken()
                .flatMap(accessToken ->
                        webClient.post()
                                .uri(abdmProperties.getUrls().getEnrollmentRequestOtp())
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                                .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                                .header("Authorization", "Bearer " + accessToken)
                                .bodyValue(request)
                                .retrieve()

                                .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                                .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                                .bodyToMono(DefaultOtpSendResponse.class)
                                .timeout(Duration.ofSeconds(30))
                )

                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))

                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))

                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))

                .doOnNext(res ->
                        log.info("OTP send abha success: {}", res)
                )
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.sendAbhaOtp",
                        abdmProperties.getUrls().getEnrollmentRequestOtp(),
                        request,
                        mono
                ));
    }


    public Mono<CreateAbhaByAadhaarResponse> verifyAbhaOtp(
            Map<String, Object> request,
            String encryptMobileNumber,
            String anotherMobileNumber
    ) {

        return getAccessToken()
                .flatMap(accessToken ->
                        webClient.post()
                                .uri(abdmProperties.getUrls().getEnrollmentEnrolByAadhaar())
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                                .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                                .header("Authorization", "Bearer " + accessToken)
                                .bodyValue(request)
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                                .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                                .bodyToMono(CreateAbhaByAadhaarResponse.class)
                                .timeout(Duration.ofSeconds(30))
                )
                .flatMap(firstResponse -> {


                    if (firstResponse.getABHAProfile() == null) {
                        firstResponse.setIsMatchToAadhaar(true);
                        return Mono.just(firstResponse);
                    }
                    if (firstResponse.getABHAProfile().getMobile() == null) {
                        firstResponse.setIsMatchToAadhaar(true);
                        return Mono.just(firstResponse);
                    }


                    if (!firstResponse.getABHAProfile().getMobile().equalsIgnoreCase(anotherMobileNumber)) {

                        return getAccessToken()
                                .flatMap(newAccessToken -> {

                                    Map<String, Object> secondRequest =
                                            CreateAbdmRequest.createSendOtpToNewNumber(
                                                    firstResponse.getTxnId(),
                                                    encryptMobileNumber
                                            );

                                    return webClient.post()
                                            .uri(abdmProperties.getUrls().getEnrollmentRequestOtp())
                                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                            .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                                            .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                                            .header("Authorization", "Bearer " + newAccessToken)
                                            .bodyValue(secondRequest)
                                            .retrieve()
                                            .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                                            .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                                            .bodyToMono(CreateAbhaByAadhaarResponse.class)
                                            .map(secondResponse -> {
                                                secondResponse.setTokens(firstResponse.getTokens());
                                                secondResponse.setIsMatchToAadhaar(false);
                                                secondResponse.setIsNew(firstResponse.getIsNew());
                                                return secondResponse;
                                            });
                                });

                    } else {
                        firstResponse.setIsMatchToAadhaar(true);
                        return Mono.just(firstResponse);
                    }
                })

                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))

                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))

                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))

                .doOnNext(res ->
                        log.info("OTP send another number success: {}", res))
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.verifyAbhaOtp",
                        abdmProperties.getUrls().getEnrollmentEnrolByAadhaar(),
                        request,
                        mono
                ));
    }

    public Mono<AbdmVerifyResponse> verifyAbhaAnotherNumberOtp(Map<String, Object> request) throws Exception {


        return getAccessToken()
                .flatMap(accessToken ->
                        webClient.post()
                                .uri(abdmProperties.getUrls().getEnrollmentAuthByAbdm())
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                                .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                                .header("Authorization", "Bearer " + accessToken)
                                .bodyValue(request)
                                .retrieve()

                                .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                                .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                                .bodyToMono(AbdmVerifyResponse.class)
                                .timeout(Duration.ofSeconds(30))
                )
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))

                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))

                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))

                .doOnNext(res ->
                        log.info("verify another otp verify success: {}", res)
                )
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.verifyAbhaAnotherNumberOtp",
                        abdmProperties.getUrls().getEnrollmentAuthByAbdm(),
                        request,
                        mono
                ));
    }


    public Mono<CreateAbhaByAadhaarResponse> verifyEmail(Map<String, Object> request, String xToken) throws Exception {


        return getAccessToken()
                .flatMap(accessToken ->
                        webClient.post()
                                .uri(abdmProperties.getUrls().getProfileAccountRequestEmailVerificationLink())
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                                .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                                .header("Authorization", "Bearer " + accessToken)
                                .header("X-token", "Bearer " + xToken)
                                .bodyValue(request)
                                .retrieve()

                                .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                                .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                                .bodyToMono(CreateAbhaByAadhaarResponse.class)
                                .timeout(Duration.ofSeconds(30))
                )
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))

                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))

                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))

                .doOnNext(res ->
                        log.info("verify Email success response: {}", res)
                )
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.verifyEmail",
                        abdmProperties.getUrls().getProfileAccountRequestEmailVerificationLink(),
                        request,
                        mono
                ));
    }

    public Mono<DefaultOtpSendResponse> updateEmailViaOtp(Map<String, Object> request, String xToken) throws Exception {


        return getAccessToken()
                .flatMap(accessToken ->
                        webClient.post()
                                .uri(abdmProperties.getUrls().getProfileAccountRequestOtp())
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                                .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                                .header("Authorization", "Bearer " + accessToken)
                                .header("X-token", "Bearer " + xToken)
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(DefaultOtpSendResponse.class)
                                .timeout(Duration.ofSeconds(30))
                )
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))

                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))

                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))

                .doOnNext(res ->
                        log.info("update email success response: {}", res)
                )
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.updateEmailViaOtp",
                        abdmProperties.getUrls().getProfileAccountRequestOtp(),
                        request,
                        mono
                ));
    }

    public Mono<DefaultOtpSendResponse> updateMobileViaOtp(
            Map<String, Object> request,
            String xToken
    ) throws Exception {

        return getAccessToken()
                .flatMap(accessToken -> {

                    // -------- CURL GENERATION (for debugging / sharing) --------
                    String curlCommand = null;
                    try {
                        String profileAccountRequestOtpUrl = abdmProperties.getUrls().getProfileAccountRequestOtp();
                        curlCommand = String.format(
                                "curl --location --request POST '%s' \\\n" +
                                        "--header 'Content-Type: application/json' \\\n" +
                                        "--header 'REQUEST-ID: %s' \\\n" +
                                        "--header 'TIMESTAMP: %s' \\\n" +
                                        "--header 'Authorization: Bearer %s' \\\n" +
                                        "--header 'X-token: Bearer %s' \\\n" +
                                        "--data-raw '%s'",
                                profileAccountRequestOtpUrl,
                                NhaHeaderUtil.generateRequestId(),
                                NhaHeaderUtil.generateTimestamp(),
                                accessToken,
                                xToken,
                                new ObjectMapper().writeValueAsString(request)
                        );
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    log.info("ABDM Update Mobile OTP CURL:\n{}", curlCommand);
                    // -----------------------------------------------------------

                    return webClient.post()
                            .uri(abdmProperties.getUrls().getProfileAccountRequestOtp())
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                            .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                            .header("Authorization", "Bearer " + accessToken)
                            .header("X-token", "Bearer " + xToken)
                            .bodyValue(request)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                            .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                            .bodyToMono(DefaultOtpSendResponse.class)
                            .timeout(Duration.ofSeconds(30));
                })
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))
                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))
                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))
                .doOnNext(res ->
                        log.info("update mobile via OTP success response: {}", res))
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.updateMobileViaOtp",
                        abdmProperties.getUrls().getProfileAccountRequestOtp(),
                        request,
                        mono
                ));
    }


    public Mono<AbhaAddressSuggestionRequest> getAbhaSuggestion(String tnxId) throws Exception {


        return getAccessToken()
                .flatMap(accessToken ->
                        webClient.get()
                                .uri(abdmProperties.getUrls().getEnrollmentEnrolSuggestion())
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                                .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                                .header("Authorization", "Bearer " + accessToken)
                                .header("Transaction_Id", tnxId)
                                .retrieve()

                                .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                                .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                                .bodyToMono(AbhaAddressSuggestionRequest.class)
                                .timeout(Duration.ofSeconds(30))
                )
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))

                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))

                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))

                .doOnNext(res ->
                        log.info("abha suggestion success response: {}", res)
                )
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.getAbhaSuggestion",
                        abdmProperties.getUrls().getEnrollmentEnrolSuggestion(),
                        payload("transactionId", tnxId),
                        mono
                ));
    }


    public Mono<AbhaProfileResponse> getAbhaDetails(String xToken) throws Exception {
        return getAccessToken()
                .flatMap(accessToken -> {

                    String url = abdmProperties.getUrls().getProfileAccount();

                    String requestId = NhaHeaderUtil.generateRequestId();
                    String timestamp = NhaHeaderUtil.generateTimestamp();

                    //  PRINT CURL
                    String curl = "curl -X GET '" + url + "'"
                            + " -H 'Content-Type: application/json'"
                            + " -H 'REQUEST-ID: " + requestId + "'"
                            + " -H 'TIMESTAMP: " + timestamp + "'"
                            + " -H 'Authorization: Bearer " + accessToken + "'"
                            + " -H 'X-token: Bearer " + xToken + "'";

                    log.info("CURL getAbhaDetails {}", curl);

                    return webClient.get()
                            .uri(url)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .header("REQUEST-ID", requestId)
                            .header("TIMESTAMP", timestamp)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .header("X-token", "Bearer " + xToken)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                            .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                            .bodyToMono(AbhaProfileResponse.class)
                            .timeout(Duration.ofSeconds(30));
                })
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))
                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))
                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))
                .doOnNext(res ->
                        log.info("get ABHA details success"))
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.getAbhaDetails",
                        abdmProperties.getUrls().getProfileAccount(),
                        payload("xToken", maskToken(xToken)),
                        mono
                ));
    }


    public Mono<AbhaProfileViaAbhaAddress> getAbhaDetailsViaAbhaAdress(String xToken) throws Exception {
        return getAccessToken()
                .flatMap(accessToken -> {

                    String url = abdmProperties.getUrls().getPhrProfileAbhaProfile();

                    String requestId = NhaHeaderUtil.generateRequestId();
                    String timestamp = NhaHeaderUtil.generateTimestamp();

                    //  PRINT CURL
                    String curl = "curl -X GET '" + url + "'"
                            + " -H 'Content-Type: application/json'"
                            + " -H 'REQUEST-ID: " + requestId + "'"
                            + " -H 'TIMESTAMP: " + timestamp + "'"
                            + " -H 'Authorization: Bearer " + accessToken + "'"
                            + " -H 'X-token: Bearer " + xToken + "'";

                    log.info("CURL getAbhaDetails {}", curl);

                    return webClient.get()
                            .uri(url)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .header("REQUEST-ID", requestId)
                            .header("TIMESTAMP", timestamp)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .header("X-token", "Bearer " + xToken)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                            .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                            .bodyToMono(AbhaProfileViaAbhaAddress.class)
                            .timeout(Duration.ofSeconds(30));
                })
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))
                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))
                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))
                .doOnNext(res ->
                        log.info("get ABHA details success"))
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.getAbhaDetailsViaAbhaAdress",
                        abdmProperties.getUrls().getPhrProfileAbhaProfile(),
                        payload("xToken", maskToken(xToken)),
                        mono
                ));
    }


    public Mono<byte[]> getAbhaQrCode(String xToken) throws Exception {
        return getAccessToken()
                .flatMap(accessToken -> {

                    String url = abdmProperties.getUrls().getProfileAccountQrCode();

                    String requestId = NhaHeaderUtil.generateRequestId();
                    String timestamp = NhaHeaderUtil.generateTimestamp();

                    //  PRINT CURL
                    String curl = "curl -X GET '" + url + "'"
                            + " -H 'Accept: image/png'"
                            + " -H 'REQUEST-ID: " + requestId + "'"
                            + " -H 'TIMESTAMP: " + timestamp + "'"
                            + " -H 'Authorization: Bearer " + accessToken + "'"
                            + " -H 'X-token: Bearer " + xToken + "'";

                    log.info("CURL getAbhaQrCode  {}", curl);

                    return webClient.get()
                            .uri(url)
                            .accept(MediaType.IMAGE_PNG)
                            .header("REQUEST-ID", requestId)
                            .header("TIMESTAMP", timestamp)
                            .header("Authorization", "Bearer " + accessToken)
                            .header("X-token", "Bearer " + xToken)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                            .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                            .bodyToMono(byte[].class)
                            .timeout(Duration.ofSeconds(30));
                })
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))
                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))
                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))
                .doOnNext(res ->
                        log.info("getAbha QR success, size={} bytes", res.length)
                )
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.getAbhaQrCode",
                        abdmProperties.getUrls().getProfileAccountQrCode(),
                        payload("xToken", maskToken(xToken)),
                        mono
                ));
    }


    public Mono<EmailLinkResponse> updateVerifyEmailOtp(Map<String, Object> request, String xToken) throws Exception {


        return getAccessToken()
                .flatMap(accessToken ->
                        webClient.post()
                                .uri(abdmProperties.getUrls().getEnrollmentEnrolByAadhaar())
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                                .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                                .header("Authorization", "Bearer " + accessToken)
                                .header("X-token", "Bearer " + xToken)
                                .bodyValue(request)
                                .retrieve()

                                .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                                .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                                .bodyToMono(EmailLinkResponse.class)
                                .timeout(Duration.ofSeconds(30))
                )
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))

                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))

                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))

                .doOnNext(res ->
                        log.info("update email verify OTP success response: {}", res)
                )
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.updateVerifyEmailOtp",
                        abdmProperties.getUrls().getEnrollmentEnrolByAadhaar(),
                        request,
                        mono
                ));
    }


    public Mono<UpdateMobileResponse> updateVerifyMobileOtp(Map<String, Object> request, String xToken) throws Exception {
        return getAccessToken()
                .flatMap(accessToken ->
                        webClient.post()
                                .uri(abdmProperties.getUrls().getProfileAccountVerify())
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                                .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                                .header("Authorization", "Bearer " + accessToken)
                                .header("X-token", "Bearer " + xToken)
                                .bodyValue(request)
                                .retrieve()

                                .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                                .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                                .bodyToMono(UpdateMobileResponse.class)
                                .timeout(Duration.ofSeconds(30))
                )
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))

                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))

                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))

                .doOnNext(res ->
                        log.info("update verify mobile OTP success response: {}", res)
                )
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.updateVerifyMobileOtp",
                        abdmProperties.getUrls().getProfileAccountVerify(),
                        request,
                        mono
                ));
    }


    public Mono<AbdmUpdateAddressResponse> updateSuggestion(Map<String, Object> request) throws Exception {


        return getAccessToken()
                .flatMap(accessToken ->
                        webClient.post()
                                .uri(abdmProperties.getUrls().getEnrollmentEnrolAbhaAddress())
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                                .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                                .header("Authorization", "Bearer " + accessToken)
                                .bodyValue(request)
                                .retrieve()

                                .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                                .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                                .bodyToMono(AbdmUpdateAddressResponse.class)
                                .timeout(Duration.ofSeconds(30))
                )
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))

                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))

                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))

                .doOnNext(res ->
                        log.info("update suggestion: {}", res)
                )
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.updateSuggestion",
                        abdmProperties.getUrls().getEnrollmentEnrolAbhaAddress(),
                        request,
                        mono
                ));
    }


    public Mono<AbhaProfileResponse> updateProfilePhoto(Map<String, Object> request, String xToken) throws Exception {

        String url = abdmProperties.getUrls().getProfileAccount();

        return getAccessToken()
                .flatMap(accessToken -> {
                    String requestId = NhaHeaderUtil.generateRequestId();
                    String timestamp = NhaHeaderUtil.generateTimestamp();

                    //  PRINT CURL
                    String curl = null;
                    try {
                        curl = "curl -X PATCH '" + url + "'"
                                + " -H 'Content-Type: application/json'"
                                + " -H 'REQUEST-ID: " + requestId + "'"
                                + " -H 'TIMESTAMP: " + timestamp + "'"
                                + " -H 'Authorization: Bearer " + accessToken + "'"
                                + " -H 'X-token: Bearer " + xToken + "'"
                                + " -d '" + new ObjectMapper().writeValueAsString(request) + "'";
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    log.info("Profile update curl  {}", curl);

                    return webClient.patch()
                            .uri(url)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .header("REQUEST-ID", requestId)
                            .header("TIMESTAMP", timestamp)
                            .header("Authorization", "Bearer " + accessToken)
                            .header("X-token", "Bearer " + xToken)
                            .bodyValue(request)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                            .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                            .bodyToMono(AbhaProfileResponse.class)
                            .timeout(Duration.ofSeconds(30));
                })
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))
                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))
                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))
                .doOnNext(res ->
                        log.info("Update profile photo response: {}", res)
                )
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.updateProfilePhoto",
                        abdmProperties.getUrls().getProfileAccount(),
                        request,
                        mono
                ));
    }


    public Mono<byte[]> downlaodAbhaCard(String xToken) throws Exception {


        return getAccessToken()
                .flatMap(accessToken ->
                        webClient.get()
                                .uri(abdmProperties.getUrls().getProfileAccountAbhaCard())
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                                .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                                .header("Authorization", "Bearer " + accessToken)
                                .header("X-token", "Bearer " + xToken)
                                .retrieve()

                                .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                                .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                                .bodyToMono(byte[].class)
                                .timeout(Duration.ofSeconds(30))
                )
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))

                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))

                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))

                .doOnNext(res ->
                        log.info("download abha card: {}", res)
                )
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.downlaodAbhaCard",
                        abdmProperties.getUrls().getProfileAccountAbhaCard(),
                        payload("xToken", maskToken(xToken)),
                        mono
                ));
    }

    public Mono<byte[]> downlaodAbhaCardViaAbhaAdress(String xToken) throws Exception {


        return getAccessToken()
                .flatMap(accessToken ->
                        webClient.get()
                                .uri(abdmProperties.getUrls().getPhrProfilePhrCard())
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                                .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                                .header("Authorization", "Bearer " + accessToken)
                                .header("X-token", "Bearer " + xToken)
                                .retrieve()

                                .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                                .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                                .bodyToMono(byte[].class)
                                .timeout(Duration.ofSeconds(30))
                )
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))

                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))

                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))

                .doOnNext(res ->
                        log.info("download abha card: {}", res)
                )
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "CreateAbhaMapper.downlaodAbhaCardViaAbhaAdress",
                        abdmProperties.getUrls().getPhrProfilePhrCard(),
                        payload("xToken", maskToken(xToken)),
                        mono
                ));
    }


    public Mono<String> getAccessToken() {
        return sessionAndCertService.fetchTokenFromApi()
                .map(apiResponse -> {
                    AbdmSessionApiResponse tokenResponse = apiResponse.getResponse();
                    if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                        throw new RuntimeException("Access token missing");
                    }
                    return tokenResponse.getAccessToken();
                });
    }

    private Map<String, Object> payload(String key, Object value) {
        java.util.Map<String, Object> requestData = new java.util.HashMap<>();
        requestData.put(key, value);
        return requestData;
    }

    private String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return token;
        }
        return "***MASKED***";
    }


    private Mono<? extends Throwable> abdmError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body ->
                        Mono.error(new WebClientResponseException(
                                response.statusCode().value(),
                                "ABDM API Error",
                                response.headers().asHttpHeaders(),
                                body.getBytes(StandardCharsets.UTF_8),
                                StandardCharsets.UTF_8
                        ))
                );
    }

}
