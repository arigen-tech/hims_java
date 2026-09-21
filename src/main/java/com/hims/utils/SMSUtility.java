package com.hims.utils;

import com.hims.constants.SMSTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
@Slf4j
@Component
@RequiredArgsConstructor
public class SMSUtility {

    @Value("${sms.api.url}")
    private String smsApiUrl;

    @Value("${sms.api.key}")
    private String smsApiKey;

    @Value("${sms.sender}")
    private String smsSender;

    @Value("${sms.module}")
    private String smsModule;

    public String sendSMS(
            String mobile,
            SMSTemplate smsTemplate,
            Map<String, String> variables) {

        try {

            StringBuilder uri = new StringBuilder(smsApiUrl);

            uri.append("?module=").append(URLEncoder.encode(smsModule, StandardCharsets.UTF_8))
                    .append("&apikey=")
                    .append(URLEncoder.encode(
                            smsApiKey,
                            StandardCharsets.UTF_8
                    ))
                    .append("&to=")
                    .append(URLEncoder.encode(
                            mobile,
                            StandardCharsets.UTF_8
                    ))
                    .append("&from=")
                    .append(URLEncoder.encode(
                            smsSender,
                            StandardCharsets.UTF_8
                    ))
                    .append("&templatename=")
                    .append(URLEncoder.encode(
                            smsTemplate.getTemplateName(),
                            StandardCharsets.UTF_8
                    ));

            if (variables != null && !variables.isEmpty()) {

                for (Map.Entry<String, String> entry : variables.entrySet()) {

                    uri.append("&")
                            .append(URLEncoder.encode(
                                    entry.getKey(),
                                    StandardCharsets.UTF_8
                            ))
                            .append("=")
                            .append(URLEncoder.encode(
                                    entry.getValue(),
                                    StandardCharsets.UTF_8
                            ));
                }
            }

            RestTemplate restTemplate = new RestTemplate();

            String response = restTemplate.getForObject(
                    uri.toString(),
                    String.class
            );

            log.info(
                    "{} SMS sent successfully to mobile number: {}",
                    smsTemplate.getDescription(),
                    mobile
            );

            log.info(
                    "Response from SMS API for {}: {}",
                    smsTemplate.getDescription(),
                    response
            );

            return response;

        } catch (Exception e) {

            log.error(
                    "Error occurred while sending {} SMS to mobile number: {}",
                    smsTemplate.getDescription(),
                    mobile,
                    e
            );

            return ResponseUtils.getReturnMsg(
                    "0",
                    "Unable to send "
                            + smsTemplate.getDescription()
                            + " SMS"
            );
        }
    }
}