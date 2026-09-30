package com.hims.controller;


import com.hims.service.WhatsAppNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/whatsapp")
public class WhatsAppNotificationController {
    private final WhatsAppNotificationService whatsAppNotificationService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendTest(@RequestParam("phone") String phone) {
        whatsAppNotificationService.sendReportReady(phone, "Test Patient", UUID.randomUUID());
        return ResponseEntity.ok(Map.of(
                "status", "triggered",
                "note", "Send is async - check application logs for the Meta API response"
        ));
    }

    @PostMapping(value = "/send-pdf", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> sendPdf(
            @RequestParam("phone") String phone,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty() || !"application/pdf".equals(file.getContentType())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please upload a PDF file"));
        }

        try {
            File tempFile = File.createTempFile("whatsapp-test-", ".pdf");
            file.transferTo(tempFile);

            whatsAppNotificationService.sendReportReadyWithAttachment(phone, tempFile);

            return ResponseEntity.ok(Map.of(
                    "status", "triggered",
                    "note", "Send is async - check application logs for the result. Remember you must have messaged the test number from your WhatsApp within the last 24 hours."
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process uploaded file"));
        }
    }
}
