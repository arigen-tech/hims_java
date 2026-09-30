package com.hims.service;

import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.util.UUID;

public interface WhatsAppNotificationService {
        @Async
        void sendReportReady(String recipientNumber, String patientName, UUID reportToken) ;

        @Async
        void sendReportReadyWithAttachment(String recipientNumber, File pdfFile);

        @Async
        void sendFreeformText(String recipientNumber, String message);

}
