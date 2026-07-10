package com.hims.m1.Mapper;


import com.hims.m1.abdm_request.AbhaSendOtpRequest;
import com.hims.m1.abdm_request.AbhaVerifyOtpMainRequest;
import com.hims.m1.abdm_response.*;
import com.hims.m1.client.AbdmProperties;
import com.hims.m1.response.AUthDeamoResponse;
import com.hims.m1.response.AbhaProfileResponse;
import com.hims.m1.service.MapperApiLogService;
import com.hims.m1.service.serviceImpl.SessionAndCertServiceImpl;
import com.hims.m1.util.AadhaarEncryptor;
import com.hims.m1.util.CreateAbdmRequest;
import com.hims.m1.util.NhaHeaderUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class VerificationMapper {

    private final SessionAndCertServiceImpl sessionAndCertService;
    private final WebClient webClient;
    private final MapperApiLogService mapperApiLogService;
    private final AbdmProperties abdmProperties;


    @Autowired
    private AadhaarEncryptor aadhaarEncryptor;

    public VerificationMapper(SessionAndCertServiceImpl sessionAndCertService,
                              AbdmProperties abdmProperties,
                              WebClient.Builder webClientBuilder,
                              MapperApiLogService mapperApiLogService) {
        this.sessionAndCertService = sessionAndCertService;
        this.abdmProperties = abdmProperties;
        this.webClient = webClientBuilder.build();
        this.mapperApiLogService = mapperApiLogService;
    }


    public Mono<AUthDeamoResponse> authDemo(Map<String, Object> request) throws Exception {

        return getAccessToken()
                .flatMap(accessToken -> {

                    String url = abdmProperties.getUrls().getEnrollmentEnrolByAadhaar();
                    String requestId = NhaHeaderUtil.generateRequestId();
                    String timestamp = NhaHeaderUtil.generateTimestamp();

                    // Generate curl equivalent
                    String curl = null;
                    try {
                        curl = "curl -X POST \"" + url + "\" " +
                                "  -H Content-Type: application/json " +
                                "  -H REQUEST-ID: " + requestId +
                                "  -H TIMESTAMP: " + timestamp +
                                "  -H Benefit-Name: MAAY " +
                                "  -H Authorization: Bearer " + accessToken +
                                "  -d '" + new ObjectMapper().writeValueAsString(request) + "'";
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    log.info("Equivalent CURL:\n{}", curl);

                    return webClient.post()
                            .uri(url)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .header("REQUEST-ID", requestId)
                            .header("TIMESTAMP", timestamp)
                            .header("Benefit-Name", "MAAY")
                            .header("Authorization", "Bearer " + accessToken)
                            .bodyValue(request)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                            .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                            .bodyToMono(AUthDeamoResponse.class)
                            .timeout(Duration.ofSeconds(30));
                })
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))
                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))
                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))
                .doOnNext(res ->
                        log.info("verifyEmail success response: {}", res)
                )
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "VerificationMapper.authDemo",
                        abdmProperties.getUrls().getEnrollmentEnrolByAadhaar(),
                        request,
                        mono
                ));
    }


    public Mono<DefaultOtpSendResponse> sendOtp(AbhaSendOtpRequest request) throws Exception {

        return getAccessToken()
                .flatMap(accessToken -> {

                    String profileLoginRequestOtpUrl = abdmProperties.getUrls().getProfileLoginRequestOtp();
                    String curl = buildCurl(
                            accessToken,
                            request,
                            profileLoginRequestOtpUrl
                    );

                    log.info("ABDM cURL Request:\n{}", curl);

                    return webClient.post()
                            .uri(profileLoginRequestOtpUrl)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                            .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                            .header("Authorization", "Bearer " + accessToken)
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
                        log.info("ABHA OTP sent successfully."))
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "VerificationMapper.sendOtp",
                        abdmProperties.getUrls().getProfileLoginRequestOtp(),
                        request,
                        mono
                ));

    }


    public Mono<DefaultOtpSendResponse> sendOtpViaMobileNumber(Map<String, Object> request) throws Exception {

        String json = new GsonBuilder()
                .setPrettyPrinting() // readable JSON
                .create().toJson(request);


        return getAccessToken()
                .flatMap(accessToken -> {

                    String profileLoginRequestOtpUrl = abdmProperties.getUrls().getProfileLoginRequestOtp();
                    String phrLoginAbhaRequestOtpUrl = abdmProperties.getUrls().getPhrLoginAbhaRequestOtp();

                    log.info("CURL Mobile NumberINDEX send OTP {}", buildCurl(accessToken, request, phrLoginAbhaRequestOtpUrl));

                    return webClient.post()
                            .uri(profileLoginRequestOtpUrl)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                            .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                            .header("Authorization", "Bearer " + accessToken)
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
                        log.info("ABHA OTP sent successfully."))
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "VerificationMapper.sendOtpViaMobileNumber",
                        abdmProperties.getUrls().getProfileLoginRequestOtp(),
                        request,
                        mono
                ));

    }


    public Mono<List<MobilAbhaResponse>> searchByMobileNumber(Map<String, Object> request) {

        return getAccessToken()
                .flatMap(accessToken -> {

                    String profileAccountAbhaSearchUrl = abdmProperties.getUrls().getProfileAccountAbhaSearch();
                    String requestId = NhaHeaderUtil.generateRequestId();
                    String timestamp = NhaHeaderUtil.generateTimestamp();

                    // Build CURL command (for debugging)
                    String curl = String.format(
                            "curl --location --request POST '%s' " +
                                    "--header 'Content-Type: application/json' " +
                                    "--header 'REQUEST-ID: %s' " +
                                    "--header 'TIMESTAMP: %s' " +
                                    "--header 'Authorization: Bearer %s' " +
                                    "--header 'BENEFIT_NAME: MAAY' " +
                                    "--data '%s'",
                            profileAccountAbhaSearchUrl,
                            requestId,
                            timestamp,
                            accessToken,
                            request
                    );

                    log.info("ABDM Mobile ABHA Search CURL => {}", curl);

                    return webClient.post()
                            .uri(profileAccountAbhaSearchUrl)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .header("REQUEST-ID", requestId)
                            .header("TIMESTAMP", timestamp)
                            .header("Authorization", "Bearer " + accessToken)
                            .header("BENEFIT_NAME", "MAAY")
                            .bodyValue(request)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                            .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                            .bodyToMono(new ParameterizedTypeReference<List<MobilAbhaResponse>>() {
                            })
                            .timeout(Duration.ofSeconds(30));
                })
                .doOnNext(res ->
                        log.info("ABHA search successful, records count: {}", res.size())
                )
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString())
                )
                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex)
                )
                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex)
                )
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "VerificationMapper.searchByMobileNumber",
                        abdmProperties.getUrls().getProfileAccountAbhaSearch(),
                        request,
                        mono
                ));

    }


    public Mono<AbdmVerifyResponse> verifyOtp(AbhaVerifyOtpMainRequest request) throws Exception {
        return getAccessToken()
                .flatMap(accessToken -> {

                    String profileLoginVerifyUrl = abdmProperties.getUrls().getProfileLoginVerify();
                    String curl = String.format(
                            "curl --location --request POST '%s' " +
                                    "--header 'Content-Type: application/json' " +
                                    "--header 'REQUEST-ID: %s' " +
                                    "--header 'TIMESTAMP: %s' " +
                                    "--header 'Authorization: Bearer %s' " +
                                    "--data '%s'",
                            profileLoginVerifyUrl,
                            NhaHeaderUtil.generateRequestId(),
                            NhaHeaderUtil.generateTimestamp(),
                            accessToken,
                            request
                    );

                    log.info("ABDM Verify OTP CURL => {}", curl);

                    return webClient.post()
                            .uri(profileLoginVerifyUrl)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                            .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                            .header("Authorization", "Bearer " + accessToken)
                            .bodyValue(request)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                            .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                            .bodyToMono(AbdmVerifyResponse.class)
                            .timeout(Duration.ofSeconds(30));
                })
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))
                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))
                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))
                .doOnNext(res ->
                        log.info("ABHA OTP verify successfully.")
                )
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "VerificationMapper.verifyOtp",
                        abdmProperties.getUrls().getProfileLoginVerify(),
                        request,
                        mono
                ));

    }


    public Mono<AbdmVerifyResponse> verifyAbhaAddressOtp(AbhaVerifyOtpMainRequest request) throws Exception {
        return getAccessToken()
                .flatMap(accessToken -> {

                    String phrLoginAbhaVerifyUrl = abdmProperties.getUrls().getPhrLoginAbhaVerify();
                    log.info("ABDM VERIFY OTP cURL:\n{}",
                            buildCurl(
                                    accessToken,
                                    request,
                                    phrLoginAbhaVerifyUrl
                            ));

                    return webClient.post()
                            .uri(phrLoginAbhaVerifyUrl)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                            .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                            .header("Authorization", "Bearer " + accessToken)
                            .bodyValue(request)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                            .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)
                            .bodyToMono(AbdmAddressVerifyResponse.class)
                            .timeout(Duration.ofSeconds(30));
                })
                .map(oldResponse -> {
                    AbdmVerifyResponse newResponse = new AbdmVerifyResponse();

                    if (!"failed".equalsIgnoreCase(oldResponse.getAuthResult())) {
                        newResponse.setMessage(oldResponse.getMessage());
                        newResponse.setAuthResult(oldResponse.getAuthResult());

                        List<VerifyOtpProfileAccount> users =
                                oldResponse.getUsers().stream().map(u -> {
                                    VerifyOtpProfileAccount user = new VerifyOtpProfileAccount();
                                    user.setAbhaNumber(u.getAbhaNumber());
                                    user.setName(u.getFullName());
                                    user.setProfilePhoto(u.getProfilePhoto());
                                    user.setPreferredAbhaAddress(u.getAbhaAddress());
                                    user.setStatus(u.getStatus());
                                    user.setKycVerified("ACTIVE".equalsIgnoreCase(u.getKycStatus()));
                                    return user;
                                }).toList();

                        newResponse.setAccounts(users);
                        newResponse.setToken(oldResponse.getTokens().getToken());
                        newResponse.setExpiresIn(oldResponse.getTokens().getExpiresIn());

                    } else {
                        newResponse.setMessage(oldResponse.getMessage());
                        newResponse.setAuthResult(oldResponse.getAuthResult());
                    }

                    return newResponse;
                })
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))
                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))
                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))
                .doOnNext(res ->
                        log.info("ABHA OTP verified successfully."))
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "VerificationMapper.verifyAbhaAddressOtp",
                        abdmProperties.getUrls().getPhrLoginAbhaVerify(),
                        request,
                        mono
                ));

    }


    public Mono<DefaultOtpSendResponse> findAbhaNumberViaAbhaAdress(String abhaAddress, String otpType) {


        return getAccessToken()
                .flatMap(firstResponse -> {

                    if (abhaAddress != null && !abhaAddress.isEmpty()) {

                        return getAccessToken()
                                .flatMap(newAccessToken ->
                                        getCertificate()
                                                .flatMap(cert -> {
                                                    String encryptNumber = aadhaarEncryptor.doEncrypt(abhaAddress, cert);
                                                    Map<String, Object> secondRequest = CreateAbdmRequest.getHealthRecordAndSendOtp(encryptNumber, otpType);
                                                    String phrLoginAbhaRequestOtpUrl = abdmProperties.getUrls().getPhrLoginAbhaRequestOtp();

                                                    // Generate headers
                                                    String requestId = NhaHeaderUtil.generateRequestId();
                                                    String timestamp = NhaHeaderUtil.generateTimestamp();

                                                    //  Print cURL
                                                    String curl = null;
                                                    try {
                                                        curl = "curl -X POST '" + phrLoginAbhaRequestOtpUrl + "' "
                                                                + "-H 'Content-Type: application/json' "
                                                                + "-H 'REQUEST-ID: " + requestId + "' "
                                                                + "-H 'TIMESTAMP: " + timestamp + "' "
                                                                + "-H 'Authorization: Bearer " + newAccessToken + "' "
                                                                + "-d '" + new ObjectMapper().writeValueAsString(secondRequest) + "'";
                                                    } catch (JsonProcessingException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                    log.info("CURL healthIdNumber send OTP {}", curl);

                                                    return webClient.post()
                                                            .uri(phrLoginAbhaRequestOtpUrl)
                                                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                                            .header("REQUEST-ID", requestId)
                                                            .header("TIMESTAMP", timestamp)
                                                            .header("Authorization", "Bearer " + newAccessToken)
                                                            .bodyValue(secondRequest)
                                                            .retrieve()
                                                            .onStatus(HttpStatusCode::is4xxClientError, this::abdmError)
                                                            .onStatus(HttpStatusCode::is5xxServerError, this::abdmError)

                                                            .bodyToMono(DefaultOtpSendResponse.class)
                                                            .map(otpResponse -> {
                                                                DefaultOtpSendResponse result = new DefaultOtpSendResponse();
                                                                result.setTxnId(otpResponse.getTxnId());
                                                                result.setMessage(otpResponse.getMessage());
                                                                return result;
                                                            })
                                                            .timeout(Duration.ofSeconds(30));
                                                })
                                )
                                .doOnError(WebClientResponseException.class, ex ->
                                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))
                                .doOnError(WebClientRequestException.class, ex ->
                                        log.error("ABDM network error", ex))
                                .doOnError(TimeoutException.class, ex ->
                                        log.error("ABDM request timed out", ex))
                                .doOnNext(res ->
                                        log.info("OTP send success: {}", res));

                    } else {
                        DefaultOtpSendResponse result = new DefaultOtpSendResponse();
                        result.setMessage("Invalid health Number. Please try again with valid ABHA address.");
                        return Mono.just(result);
                    }
                })
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("ABDM error response body: {}", ex.getResponseBodyAsString()))
                .doOnError(WebClientRequestException.class, ex ->
                        log.error("ABDM network error", ex))
                .doOnError(TimeoutException.class, ex ->
                        log.error("ABDM request timed out", ex))
                .doOnNext(res ->
                        log.info("OTP send success: {}", res))
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "VerificationMapper.findAbhaNumberViaAbhaAdress",
                        abdmProperties.getUrls().getPhrLoginAbhaSearch(),
                        payload("abhaAddress", abhaAddress),
                        mono
                ));
    }


    public Mono<AbhaProfileResponse> findAbhaNumberViaAbhaAdress1(String abhaAddress) {

        Map<String, Object> abhaAddressRequest = CreateAbdmRequest.createAbhaAddressRequest(abhaAddress);

        return getAccessToken()
                .flatMap(accessToken -> {

                    String requestId = NhaHeaderUtil.generateRequestId();
                    String timestamp = NhaHeaderUtil.generateTimestamp();
                    String phrLoginAbhaSearchUrl = abdmProperties.getUrls().getPhrLoginAbhaSearch();

                    String curl = String.format(
                            "curl --location --request POST '%s' " +
                                    "--header 'Content-Type: application/json' " +
                                    "--header 'REQUEST-ID: %s' " +
                                    "--header 'TIMESTAMP: %s' " +
                                    "--header 'Authorization: Bearer %s' " +
                                    "--data '%s'",
                            phrLoginAbhaSearchUrl,
                            requestId,
                            timestamp,
                            accessToken,
                            abhaAddressRequest
                    );

                    log.info("ABDM ABHA Search CURL => {}", curl);

                    return webClient.post()
                            .uri(phrLoginAbhaSearchUrl)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .header("REQUEST-ID", requestId)
                            .header("TIMESTAMP", timestamp)
                            .header("Authorization", "Bearer " + accessToken)
                            .bodyValue(abhaAddressRequest)
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
                        log.info("OTP send success: {}", res))
                .transform(mono -> mapperApiLogService.logAbdmApi(
                        "VerificationMapper.findAbhaNumberViaAbhaAdress",
                        abdmProperties.getUrls().getPhrLoginAbhaSearch(),
                        payload("abhaAddress", abhaAddress),
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

    public Mono<AbdmCertificateResponse> getCertificate() {
        return sessionAndCertService.getCertificateABDM()
                .map(apiResponse -> {
                    AbdmCertificateResponse tokenResponse = apiResponse.getResponse();
                    if (tokenResponse == null || tokenResponse.getPublicKey() == null) {
                        throw new RuntimeException("Access token missing");
                    }
                    return tokenResponse;
                });
    }

    private Map<String, Object> payload(String key, Object value) {
        java.util.Map<String, Object> requestData = new java.util.HashMap<>();
        requestData.put(key, value);
        return requestData;
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

    private String buildCurl(String accessToken, Object request, String url) {
        return new StringBuilder()
                .append("curl --location --request POST '").append(url).append("' ")
                .append("--header 'Content-Type: application/json' ")
                .append("--header 'Authorization: Bearer ").append(accessToken).append("' ")
                .append("--header 'REQUEST-ID: ").append(NhaHeaderUtil.generateRequestId()).append("' ")
                .append("--header 'TIMESTAMP: ").append(NhaHeaderUtil.generateTimestamp()).append("' ")
                .append("--data-raw '").append(requestToJson(request)).append("'")
                .toString();
    }

    private String requestToJson(Object request) {
        try {
            return new ObjectMapper().writeValueAsString(request);
        } catch (Exception e) {
            return "{}";
        }
    }


}
