package com.hims.helperUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
@Service
public class HelperUtils {

    // FOR dev  D:\BmsBackend\webapps\bmsreport
    public static String LASTFOLDERPATH = "D:/payroll/webapps/bmsreport";
    public static String FILEPATH = "https://icg.net.in/bmsreport/";

//     For UAT
//    public static String LASTFOLDERPATH = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/bmsreport";
//    public static String FILEPATH = "https://icg.net.in/bmsreport/";

//     For PROD
//    public static String LASTFOLDERPATH = "C:/Program Files/Tomcat 9.0/webapps/cgbmsreport";
//    public static String FILEPATH = "https://icg.net.in/cgbmsreport/";

    public static String getRollId() {
        return "ROL_ID" + ConverterUtils.getRandomTimeStamp();
    }

    public static Timestamp getCurrentTimeStamp() {
        return new Timestamp(new Date().getTime());
    }

    public static String getOtp() {
        return ConverterUtils.generateOTP();
    }


    public  String sendSMS(String mobile, String name,String password) {
        try {
            final String uri ="https://2factor.in/API/R1/?module=TRANS_SMS&apikey=5cdc6365-22b5-11ec-a13b-0200cd936042&to="+mobile+
                    "&from=CGMMSY&templatename=Username-New&var1="+name+"&var2="+mobile+"&var3="+password;

            MultiValueMap<String, String> requestHeaders = new LinkedMultiValueMap<String, String>();
            RestTemplate restTemplate = new RestTemplate();
            String responseObject = restTemplate.postForObject(uri, requestHeaders, String.class);

            System.out.println(responseObject.toString());
            System.out.println("SMS send succefully");
            return responseObject;
        } catch (Exception e) {

            return ResponseUtils.getReturnMsg("0", "We are unable to process your request");
        }
    }

    public  String generateOTP() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // Generates a 6-digit number
        return String.valueOf(otp); // Convert to string for OTP usage
    }

    public static String extractTimeFromInstant(Instant instant) {
        return instant
                .atZone(ZoneId.of("UTC"))
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

}
