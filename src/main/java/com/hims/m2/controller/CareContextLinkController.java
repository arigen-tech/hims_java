package com.hims.m2.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.apiResponse.ResponseUtils;
import com.hims.m1.service.ApiControllerLogService;
import com.hims.m2.request.CareContextLinkRequest;
import com.hims.m2.response.CareContextLinkInitiateResponse;
import com.hims.m2.response.CareContextLinkPending;
import com.hims.m2.response.CareContextLinkStatusResponse;
import com.hims.m2.service.CareContextLinkService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class CareContextLinkController {

    @Autowired
    private CareContextLinkService careContextLinkService;

    @Autowired
    private ApiControllerLogService apiControllerLogService;

    // ---- called by your React page ----
    @PostMapping("/api/v1/abdm/m2/link/initiate")
    public Mono<ResponseEntity<ApiResponse<CareContextLinkInitiateResponse>>> initiate(@Valid @RequestBody CareContextLinkRequest req) {
        Mono<ResponseEntity<ApiResponse<CareContextLinkInitiateResponse>>> result = Mono
                .fromCallable(() -> {
                    CareContextLinkInitiateResponse data = new CareContextLinkInitiateResponse();
                    data.setRequestId(careContextLinkService.initiate(req));
                    return ResponseUtils.createSuccessResponse(data, true, "Care context link request submitted.");
                })
                .map(ResponseEntity::ok)
                .onErrorResume(Exception.class, ex -> {
                    log.error("Care context link initiate failed: {}", ex.getMessage());
                    ApiResponse<CareContextLinkInitiateResponse> failure =
                            ResponseUtils.<CareContextLinkInitiateResponse>createFailureResponse(ex.getMessage(), false);
                    return Mono.just(ResponseEntity.ok(failure));
                });

        return apiControllerLogService.logMonoApi("/api/v1/abdm/m2/link/initiate", req, result);
    }

    @GetMapping("/api/v1/abdm/m2/link/status/{requestId}")
    public Mono<ResponseEntity<ApiResponse<CareContextLinkStatusResponse>>> status(@PathVariable String requestId) {
        Mono<ResponseEntity<ApiResponse<CareContextLinkStatusResponse>>> result = Mono.fromCallable(() -> {
            CareContextLinkPending careContextLinkPending = careContextLinkService.status(requestId);
            if (careContextLinkPending == null) {
                ApiResponse<CareContextLinkStatusResponse> notFound =
                        ResponseUtils.<CareContextLinkStatusResponse>createFailureResponse("Link request not found.", false);
                return ResponseEntity.ok(notFound);
            }
            CareContextLinkStatusResponse data = new CareContextLinkStatusResponse();
            data.setRequestId(careContextLinkPending.getRequestId());
            data.setState(careContextLinkPending.getStatus());
            data.setMessage(careContextLinkPending.getMessage());
            return ResponseEntity.ok(ResponseUtils.createSuccessResponse(data, true, "Link status fetched."));
        });

        return apiControllerLogService.logMonoApi("/api/v1/abdm/m2/link/status", requestId, result);
    }

    // ---- called by the ABDM gateway (not wrapped, not logged to the API log table) ----
    // VERIFY paths: they must match what ABDM appends to your bridge URL.
    // The ngrok inspector at http://127.0.0.1:4040 shows the real ones.
    @PostMapping("/api/hiecm/v3/token/on-generate-token")
    public ResponseEntity<Void> onGenerateToken(@RequestBody JsonNode body) {
        log.info("on-generate-token callback: {}", body);
        String requestId = body.path("resp").path("requestId").asText(null); // VERIFY where requestId lives
        careContextLinkService.onGenerateToken(requestId, body);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/api/hiecm/hip/v3/link/carecontext/on-add-contexts")
    public ResponseEntity<Void> onAddContexts(@RequestBody JsonNode body) {
        log.info("on-add-contexts callback: {}", body);
        String requestId = body.path("resp").path("requestId").asText(null);
        careContextLinkService.onAddContexts(requestId, body);
        return ResponseEntity.accepted().build();
    }
}