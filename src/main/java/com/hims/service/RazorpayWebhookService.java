package com.hims.service;

import org.springframework.transaction.annotation.Transactional;

public interface RazorpayWebhookService {


     @Transactional
     void processWebhook(String rawBody, String razorpaySignature,String eventIdHeader, String webhookSecret);
}