package com.hims.m2.service.impl;


import com.fasterxml.jackson.databind.JsonNode;
import com.hims.m2.request.CareContextLinkRequest;
import com.hims.m2.response.CareContextLinkPending;
import com.hims.m2.service.CareContextLinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CareContextLinkServiceImpl implements CareContextLinkService {

    @Value("${abdm.urls.gateway-session}") private String sessionUrl;
    @Value("${abdm.urls.m2-token-generate}") private String tokenUrl;
    @Value("${abdm.urls.m2-link-care-context}") private String linkUrl;
    @Value("${abdm.client.id}") private String clientId;
    @Value("${abdm.client.secret}") private String clientSecret;
    @Value("${abdm.m2.hip-id:}") private String hipId;
    @Value("${abdm.m2.cm-id:sbx}") private String cmId;

    private final WebClient webClient = WebClient.create();

    // TODO: move to a DB table. In-memory is lost on restart and breaks with multiple instances.
    private final Map<String, CareContextLinkPending> pending = new ConcurrentHashMap<>();

    private volatile String sessionToken;
    private volatile Instant sessionExpiresAt = Instant.EPOCH;

    @Override
    public String initiate(CareContextLinkRequest req) {
        if (hipId == null || hipId.isBlank()) {
            throw new IllegalStateException("abdm.m2.hip-id is empty. Set ABDM_M2_HIP_ID.");
        }
        String requestId = UUID.randomUUID().toString();
        CareContextLinkPending p = new CareContextLinkPending();
        p.setRequestId(requestId);
        p.setRequest(req);
        pending.put(requestId, p);

        Map<String, Object> body = new HashMap<>();
        body.put("abhaNumber", req.getAbhaNumber());
        body.put("abhaAddress", req.getAbhaAddress());
        body.put("name", req.getPatientName());
        body.put("gender", req.getGender());
        body.put("yearOfBirth", req.getYearOfBirth()); // VERIFY fields

        post(tokenUrl, requestId, null, body);
        return requestId;
    }

    /** Called from the gateway callback for token generation. */
    @Override
    public void onGenerateToken(String requestId, JsonNode payload) {
        CareContextLinkPending p = requestId == null ? null : pending.get(requestId);
        if (p == null) { log.warn("onGenerateToken: unknown requestId {}", requestId); return; }

        JsonNode err = payload.get("error");
        boolean hasError = err != null && !err.isNull();
        String linkToken = payload.hasNonNull("linkToken") ? payload.get("linkToken").asText() : null; // VERIFY
        if (hasError || linkToken == null) {
            p.setStatus("FAILED");
            p.setMessage(hasError ? err.toString() : "No link token returned");
            return;
        }

        List<Map<String, Object>> contexts = new ArrayList<>();
        for (CareContextLinkRequest.Ctx c : p.getRequest().getCareContexts()) {
            Map<String, Object> m = new HashMap<>();
            m.put("referenceNumber", c.getReferenceNumber());
            m.put("display", c.getDisplay());
            contexts.add(m);
        }
        String hiType = p.getRequest().getCareContexts().get(0).getHiType();

        Map<String, Object> patient = new HashMap<>();
        patient.put("referenceNumber", p.getRequest().getPatientId());
        patient.put("display", p.getRequest().getPatientName());
        patient.put("careContexts", contexts);
        patient.put("hiType", hiType);
        patient.put("count", contexts.size());

        Map<String, Object> body = new HashMap<>();
        body.put("abhaNumber", p.getRequest().getAbhaNumber());
        body.put("abhaAddress", p.getRequest().getAbhaAddress());
        body.put("patient", List.of(patient));                 // VERIFY shape

        String linkRequestId = UUID.randomUUID().toString();
        pending.put(linkRequestId, p);   // second callback arrives under this id
        p.setStatus("LINK_REQUESTED");
        post(linkUrl, linkRequestId, linkToken, body);
    }

    /** Called from the gateway callback for the link result. */
    @Override
    public void onAddContexts(String requestId, JsonNode payload) {
        CareContextLinkPending p = requestId == null ? null : pending.get(requestId);
        if (p == null) { log.warn("onAddContexts: unknown requestId {}", requestId); return; }

        JsonNode err = payload.get("error");
        if (err != null && !err.isNull()) {
            p.setStatus("FAILED");
            p.setMessage(err.toString());
        } else {
            p.setStatus("LINKED");
            // TODO: mark p.getRequest().getCareContexts() as linked in your DB here.
        }
    }

    @Override
    public CareContextLinkPending status(String requestId) {
        return pending.get(requestId);
    }

    // ---------------- private helpers ----------------

    /** M2 session token: fetched from gateway-session, cached, refreshed 60s before expiry. */
    private synchronized String getSessionToken() {
        if (sessionToken != null && Instant.now().isBefore(sessionExpiresAt.minusSeconds(60))) {
            return sessionToken;
        }
        try {
            JsonNode res = webClient.post().uri(sessionUrl)
                    .header("REQUEST-ID", UUID.randomUUID().toString())
                    .header("TIMESTAMP", Instant.now().toString())
                    .header("X-CM-ID", cmId)
                    .bodyValue(Map.of(
                            "clientId", clientId,
                            "clientSecret", clientSecret,
                            "grantType", "client_credentials")) // VERIFY
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (res == null || !res.hasNonNull("accessToken")) { // VERIFY field name
                throw new IllegalStateException("ABDM session response has no accessToken: " + res);
            }
            sessionToken = res.get("accessToken").asText();
            long ttl = res.hasNonNull("expiresIn") ? res.get("expiresIn").asLong() : 600;
            sessionExpiresAt = Instant.now().plusSeconds(ttl);
            return sessionToken;
        } catch (WebClientResponseException e) {
            log.error("ABDM session failed: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    private void post(String url, String requestId, String linkToken, Object body) {
        var spec = webClient.post().uri(url)
                .header("Authorization", "Bearer " + getSessionToken())
                .header("REQUEST-ID", requestId)
                .header("TIMESTAMP", Instant.now().toString())
                .header("X-CM-ID", cmId)
                .header("X-HIP-ID", hipId);                    // VERIFY header names
        if (linkToken != null) spec = spec.header("X-LINK-TOKEN", linkToken);

        spec.bodyValue(body).retrieve().toBodilessEntity()
                .doOnSuccess(r -> log.info("ABDM call OK: {} requestId={}", url, requestId))
                .doOnError(e -> {
                    String detail = e instanceof WebClientResponseException w
                            ? w.getStatusCode() + " " + w.getResponseBodyAsString()
                            : e.getMessage();
                    log.error("ABDM call failed: {} requestId={} -> {}", url, requestId, detail);
                    CareContextLinkPending p = pending.get(requestId);
                    if (p != null) { p.setStatus("FAILED"); p.setMessage(detail); }
                })
                .subscribe(r -> { }, e -> { });
    }
}
