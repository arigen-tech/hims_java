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

    @Value("${sms.otp.url}")
    private String smsOtpUrl;

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

    public String sendOtp(String mobile, SMSTemplate smsTemplate) {

        try {

            StringBuilder uri = new StringBuilder(smsOtpUrl);
            uri.append(URLEncoder.encode(
                            smsApiKey,
                            StandardCharsets.UTF_8
                    ))
                    .append("/SMS/")
                    .append(URLEncoder.encode(
                            mobile,
                            StandardCharsets.UTF_8)
                    )
                    .append("/AUTOGEN/")
                    .append(URLEncoder.encode(
                            smsTemplate.getTemplateName(),
                            StandardCharsets.UTF_8
                    ));

            log.info("send otp uri :: {}",uri);

            HttpResponse<String> response = Unirest.post(uri.toString()).asString();

            JSONObject jsonObject = new JSONObject(response.getBody());

            if (!"Success".equalsIgnoreCase(
                    jsonObject.optString("Status"))) {

                log.error(
                        "Failed to send {} OTP to mobile: {}. Response: {}",
                        smsTemplate.getDescription(),
                        mobile,
                        response.getBody()
                );

                throw  new RuntimeException( "Failed to send "+ smsTemplate.getDescription()+" OTP to mobile: "+mobile+". Response: "+response.getBody());
            }

            String sessionId = jsonObject.getString("Details");

            log.info(
                    "{} OTP sent successfully to mobile: {}",
                    smsTemplate.getDescription(),
                    mobile
            );

            return sessionId;

        } catch (Exception e) {

            log.error(
                    "Error while sending {} OTP to mobile: {}",
                    smsTemplate.getDescription(),
                    mobile,
                    e
            );

            throw  e;
        }

    }
            public boolean verifyOtp(String sessionId, String otp) {

                try {

                    StringBuilder uri = new StringBuilder(smsOtpUrl);
                    uri.append(URLEncoder.encode(
                            smsApiKey,
                            StandardCharsets.UTF_8
                    ))
                            .append("/SMS/VERIFY/")
                            .append(URLEncoder.encode(sessionId,
                                    StandardCharsets.UTF_8
                            ))
                            .append("/")
                            .append( URLEncoder.encode(otp,
                                    StandardCharsets.UTF_8
                            ));


                    log.info("Otp verify Uri :: ",uri);

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