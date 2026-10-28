package com.hims.runner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * One-time importer: ABDM LGD states+districts  ->  mas_district (FK to mas_state via lgd_state_code).
 *
 * Runs ONLY when  lgd.import.enabled=true  (so it never runs on a normal startup).
 * It fetches its own ABDM access token using the existing properties:
 *   abdm.urls.gateway-session, abdm.client.id, abdm.client.secret
 *
 * application.properties (temporary):
 *   lgd.import.enabled=true
 *   lgd.import.url=https://apihspsbx.abdm.gov.in/v4/int/v1.5/facility/lgd/states
 *   lgd.import.dry-run=true          # first run only logs; set false for the real import
 *
 * Remove the lgd.import.* lines after the import.
 * Uses native SQL only, so it does not touch shared entities/repositories.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "lgd.import.enabled", havingValue = "true")
public class LgdDistrictImportRunner implements CommandLineRunner {


    @PersistenceContext
    private EntityManager em;

    private final TransactionTemplate tx;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();

    @Value("${lgd.import.url}")
    private String url;

    @Value("${lgd.import.dry-run:true}")
    private boolean dryRun;

    @Value("${abdm.urls.gateway-session}")
    private String sessionUrl;

    @Value("${abdm.client.id}")
    private String clientId;

    @Value("${abdm.client.secret}")
    private String clientSecret;

    public LgdDistrictImportRunner(TransactionTemplate tx) {
        this.tx = tx;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("LGD district import started (dryRun={})", dryRun);

        String token = fetchAccessToken();
        JsonNode root = fetchStates(token);
        JsonNode states = unwrapList(root);
        if (states == null || !states.isArray() || states.isEmpty()) {
            log.error("LGD import: could not find a state list in response. Top-level: {}", preview(root));
            return;
        }
        log.info("LGD import: {} state entries received. First node sample = {}", states.size(), preview(states.get(0)));

        int[] totals = new int[]{0, 0, 0, 0}; // inserted, existing, stateNotFound, skipped(no code)
        Set<String> seenStates = new HashSet<>();

        for (JsonNode st : states) {
            String stateCode = text(st, "code", "stateCode", "lgdCode", "govLGDStateId", "id");
            if (stateCode == null) { totals[3]++; continue; }
            if (!seenStates.add(stateCode)) continue; // ignore duplicate states

            JsonNode districts = firstNode(st, "districts", "districtList", "district");
            if (districts == null || !districts.isArray()) {
                log.warn("LGD import: state {} has no nested districts array", stateCode);
                continue;
            }

            Number stateId = findStateId(stateCode);
            if (stateId == null) {
                log.warn("LGD import: no mas_state row for lgd_state_code={} ({}) - skipped {} districts",
                        stateCode, text(st, "name", "stateName"), districts.size());
                totals[2] += districts.size();
                continue;
            }

            for (JsonNode d : districts) {
                String code = text(d, "code", "districtCode", "lgdCode", "govLGDDistrictId", "id");
                String name = text(d, "name", "districtName");
                if (code == null || name == null) { totals[3]++; continue; }

                if (districtExists(code, stateId)) { totals[1]++; continue; }

                if (!dryRun) {
                    final String fCode = code;
                    final String fName = name.trim().toUpperCase();
                    tx.executeWithoutResult(s -> em.createNativeQuery(
                                    "INSERT INTO mas_district (district_name, status, las_ch_by, last_chg_date, state_id, lgd_district_code) "
                                            + "VALUES (:n, 'y', 'LGD_IMPORT', CURRENT_TIMESTAMP, :sid, :c)")
                            .setParameter("n", fName)
                            .setParameter("sid", stateId.longValue())
                            .setParameter("c", fCode)
                            .executeUpdate());
                }
                totals[0]++;
            }
        }

        log.info("LGD import finished (dryRun={}): {} {}, {} already existed, {} skipped (state not in mas_state), {} skipped (no code/name)",
                dryRun, totals[0], dryRun ? "would be inserted" : "inserted", totals[1], totals[2], totals[3]);
    }

    // ---------- ABDM calls ----------

    private String fetchAccessToken() throws Exception {
        String body = mapper.writeValueAsString(mapper.createObjectNode()
                .put("clientId", clientId)
                .put("clientSecret", clientSecret)
                .put("grantType", "client_credentials"));

        HttpRequest req = HttpRequest.newBuilder(URI.create(sessionUrl))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("REQUEST-ID", UUID.randomUUID().toString())
                .header("TIMESTAMP", Instant.now().toString())
                .header("X-CM-ID", "sbx")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            throw new IllegalStateException("ABDM session API returned HTTP " + resp.statusCode() + ": " + resp.body());
        }
        JsonNode n = mapper.readTree(resp.body());
        String token = text(n, "accessToken", "access_token");
        if (token == null) {
            throw new IllegalStateException("ABDM session response had no accessToken");
        }
        return token;
    }

    private JsonNode fetchStates(String token) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .header("X-CM-ID", "sbx")
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            throw new IllegalStateException("LGD API returned HTTP " + resp.statusCode() + ": " + resp.body());
        }
        return mapper.readTree(resp.body());
    }

    // ---------- helpers ----------

    /** Response may be a bare array or wrapped in an object (data/states/result...). */
    private JsonNode unwrapList(JsonNode root) {
        if (root == null) return null;
        if (root.isArray()) return root;
        return firstNode(root, "states", "data", "result", "response");
    }

    private JsonNode firstNode(JsonNode n, String... names) {
        for (String f : names) {
            JsonNode v = n.get(f);
            if (v != null && !v.isNull()) return v;
        }
        return null;
    }

    private String text(JsonNode n, String... names) {
        for (String f : names) {
            JsonNode v = n.get(f);
            if (v != null && !v.isNull() && !v.asText().isBlank()) return v.asText().trim();
        }
        return null;
    }

    private Number findStateId(String lgdStateCode) {
        List<?> r = em.createNativeQuery(
                        "SELECT state_id FROM mas_state WHERE CAST(lgd_state_code AS VARCHAR(10)) = :c AND status = 'y'")
                .setParameter("c", lgdStateCode)
                .getResultList();
        return r.isEmpty() ? null : (Number) r.get(0);
    }

    private boolean districtExists(String lgdDistrictCode, Number stateId) {
        Number cnt = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM mas_district WHERE lgd_district_code = :c AND state_id = :s")
                .setParameter("c", lgdDistrictCode)
                .setParameter("s", stateId.longValue())
                .getSingleResult();
        return cnt.longValue() > 0;
    }

    private String preview(JsonNode n) {
        String s = String.valueOf(n);
        return s.length() > 400 ? s.substring(0, 400) + "..." : s;
    }
}