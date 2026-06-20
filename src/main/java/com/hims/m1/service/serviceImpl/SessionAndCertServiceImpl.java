package com.hims.m1.service.serviceImpl;

import com.hims.m1.abdm_request.AbdmSessionRequest;
import com.hims.m1.abdm_response.AbdmCertificateResponse;
import com.hims.m1.abdm_response.AbdmSessionApiResponse;
import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.apiResponse.ResponseUtils;
import com.hims.m1.client.AbdmProperties;
import com.hims.m1.service.CaptchaService;
import com.hims.m1.service.SessionAndCertService;
import com.hims.m1.util.NhaHeaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
public class SessionAndCertServiceImpl implements SessionAndCertService {


    private final WebClient webClient;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private AbdmProperties abdmProperties;

    public SessionAndCertServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }


    public Mono<ApiResponse<AbdmSessionApiResponse>> fetchTokenFromApi() {

        AbdmSessionRequest request = new AbdmSessionRequest();
        request.setClientId(abdmProperties.getClient().getId());
        request.setClientSecret(abdmProperties.getClient().getSecret());
        request.setGrantType("client_credentials");

        return webClient.post()
                .uri(abdmProperties.getUrls().getGatewaySession())
                .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                .header("X-CM-ID", "sbx") // unchanged as requested
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AbdmSessionApiResponse.class)
                .timeout(Duration.ofSeconds(30))
                .map(token -> {
                    return ResponseUtils.createSuccessResponse(
                            token,
                            true,
                            "Session fetched successfully"
                    );
                })
                .doOnNext(response ->
                        log.info("NHA session token fetched successfully")
                )
                .doOnError(error ->
                        ResponseUtils.createFailureResponse(
                                "Some error occurred",
                                false
                        )
                );

    }


    @Override
    public Mono<ApiResponse<AbdmCertificateResponse>> getCertificateABDM() {

        return getAccessToken()
                .flatMap(accessToken ->

                        webClient.get()
                                .uri(abdmProperties.getUrls().getAbhaCertificate())
                                .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                                .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                                .header("Authorization", "Bearer " + accessToken)
                                .retrieve()
                                .bodyToMono(AbdmCertificateResponse.class)
                                .timeout(NhaHeaderUtil.getApiTimeDuration())
                                .map(cert ->
                                        ResponseUtils.createSuccessResponse(
                                                cert,
                                                true,
                                                "Certificate fetched successfully"
                                        )
                                )
                )
                .doOnNext(res ->
                        log.info("NHA certificate fetched successfully")
                )
                .onErrorResume(error -> {
                    return Mono.just(
                            ResponseUtils.createFailureResponse(
                                    "Some error occurred",
                                    false
                            )
                    );
                });
    }



    public Mono<String> getAccessToken() {
        return fetchTokenFromApi()
                .map(apiResponse -> {
                    AbdmSessionApiResponse tokenResponse = apiResponse.getResponse();
                    if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                        throw new RuntimeException("Access token missing");
                    }
                    return tokenResponse.getAccessToken();
                });
    }


}
