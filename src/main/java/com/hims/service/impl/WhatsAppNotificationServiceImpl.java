package com.hims.service.impl;

import com.hims.service.WhatsAppNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppNotificationServiceImpl implements WhatsAppNotificationService {

    private final RestTemplate restTemplate;

    @Value("${whatsapp.api.version}")
    private String apiVersion;

    @Value("${whatsapp.api.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.api.access-token}")
    private String accessToken;

    @Async
    @Override
    public void sendReportReady(String recipientNumber, String patientName, UUID reportToken) {
        String url = String.format("https://graph.facebook.com/%s/%s/messages", apiVersion, phoneNumberId);

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", normalizeIndianNumber(recipientNumber),
                "type", "template",
                "template", Map.of(
                        "name", "hello_world",       // swap to your approved template once ready
                        "language", Map.of("code", "en_US")
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            log.info("WhatsApp send response: {}", response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("WhatsApp send failed: {}", e.getResponseBodyAsString());
        }
    }

    @Async
    @Override
    public void sendReportReadyWithAttachment(String recipientNumber, File pdfFile) {
        String mediaId = uploadMedia(pdfFile); // same method as before, unchanged
        String url = String.format("https://graph.facebook.com/%s/%s/messages", apiVersion, phoneNumberId);

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", normalizeIndianNumber(recipientNumber),
                "type", "document",
                "document", Map.of(
                        "id", mediaId,
                        "filename", "LabReport.pdf",
                        "caption", "Your lab report is ready."
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            log.info("WhatsApp freeform document sent: {}", response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("WhatsApp freeform document send failed: {}", e.getResponseBodyAsString());
        }
    }

    public String uploadMedia(File pdfFile) {
        String url = String.format("https://graph.facebook.com/%s/%s/media", apiVersion, phoneNumberId);

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("messaging_product", "whatsapp");
        form.add("file", new FileSystemResource(pdfFile));
        form.add("type", "application/pdf");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, new HttpEntity<>(form, headers), Map.class);

        return (String) response.getBody().get("id"); // this is the media_id
    }


    @Async
    @Override
    public void sendFreeformText(String recipientNumber, String message) {
        String url = String.format("https://graph.facebook.com/%s/%s/messages", apiVersion, phoneNumberId);

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", normalizeIndianNumber(recipientNumber),
                "type", "text",
                "text", Map.of("body", message)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            log.info("WhatsApp freeform text sent: {}", response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("WhatsApp freeform text send failed - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("WhatsApp freeform text send - unexpected error", e);
        }
    }

    private String normalizeIndianNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        String digits = raw.replaceAll("\\D", "");

        if (digits.length() == 10) {
            return "91" + digits;
        }
        if (digits.length() == 12 && digits.startsWith("91")) {
            return digits;
        }
        if (digits.length() == 13 && digits.startsWith("091")) {
            return "91" + digits.substring(3);
        }

        throw new IllegalArgumentException("Unrecognized Indian phone number format: " + raw);
    }
}
