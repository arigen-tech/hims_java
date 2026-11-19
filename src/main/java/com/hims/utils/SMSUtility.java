package com.hims.utils;

import com.hims.constants.SMSTemplate;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import kong.unirest.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
@Slf4j
@Component
@RequiredArgsConstructor
public class SMSUtility {

    @Value("${sms.api.url}")
    private String smsApiUrl;

    @Value("${sms.otp.base-url}")
    private String otpBaseUrl;

    @Value("${sms.otp.send-path}")
    private String otpSendPath;

    @Value("${sms.otp.verify-path}")
    private String otpVerifyPath;

    @Value("${sms.api.key}")
    private String smsApiKey;

    @Value("${sms.sender}")
    private String smsSender;

    @Value("${sms.module}")
    private String smsModule;

    public void sendSMS(
            String mobile,
            SMSTemplate smsTemplate,
            Map<String, String> variables,
            boolean shouldSend) {

        try {

            if (!shouldSend) {
                log.info("SMS sending skipped for {}", smsTemplate.getDescription());
                return ;
            }

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



        } catch (Exception e) {

            log.error(
                    "Error occurred while sending {} SMS to mobile number: {}",
                    smsTemplate.getDescription(),
                    mobile,
                    e
            );
            throw  e;
        }
    }

    public String sendOtp(String mobile, SMSTemplate smsTemplate) {
        return sendOtp(mobile, smsTemplate.getTemplateName(), smsTemplate.getDescription());
    }

    public String sendOtp(String mobile, String templateName, String description) {

        try {

            String uri = otpBaseUrl + otpSendPath
                    .replace("{mobile}", URLEncoder.encode(mobile, StandardCharsets.UTF_8))
                    .replace("{template}", URLEncoder.encode(templateName, StandardCharsets.UTF_8));

            log.info("Sending {} OTP to mobile: {}", description, mobile);

            HttpResponse<String> response = Unirest.post(uri).asString();

            JSONObject jsonObject = new JSONObject(response.getBody());

            if (!"Success".equalsIgnoreCase(
                    jsonObject.optString("Status"))) {

                log.error(
                        "Failed to send {} OTP to mobile: {}. Response: {}",
                        description,
                        mobile,
                        response.getBody()
                );

                throw  new RuntimeException( "Failed to send "+ description+" OTP to mobile: "+mobile+". Response: "+response.getBody());
            }

            String sessionId = jsonObject.getString("Details");

            log.info(
                    "{} OTP sent successfully to mobile: {}",
                    description,
                    mobile
            );

            return sessionId;

        } catch (Exception e) {

            log.error(
                    "Error while sending {} OTP to mobile: {}",
                    description,
                    mobile,
                    e
            );

            throw  e;
        }

    }
            public boolean verifyOtp(String sessionId, String otp) {

                try {

                    String uri = otpBaseUrl + otpVerifyPath
                            .replace("{sessionId}", URLEncoder.encode(sessionId, StandardCharsets.UTF_8))
                            .replace("{otp}", URLEncoder.encode(otp, StandardCharsets.UTF_8));


                    log.info("Verifying OTP for sessionId: {}", sessionId);

                    HttpResponse<String> response = Unirest.post(uri.toString())
                            .header(
                                    "content-type",
                                    "application/x-www-form-urlencoded"
                            )
                            .asString();

                    log.info("OTP verify response status: {}", response.getStatus());
                    log.info("OTP verify response body: {}", response.getBody());

                    JSONObject jsonObject =
                            new JSONObject(response.getBody());

                    String status = jsonObject.optString("Status");

                    if ("Success".equalsIgnoreCase(status)) {

                        log.info(
                                "OTP verified successfully for sessionId: {}",
                                sessionId
                        );

                        return true;
                    }

                    log.warn(
                            "OTP verification failed for sessionId: {}. Response: {}",
                            sessionId,
                            response.getBody()
                    );

                    return false;

                } catch (Exception e) {

                    log.error(
                            "Error occurred while verifying OTP for sessionId: {}",
                            sessionId,
                            e
                    );

                    return false;
                }
            }
}
