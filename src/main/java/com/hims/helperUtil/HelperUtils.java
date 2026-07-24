package com.hims.helperUtil;

import com.hims.constants.AppConstants;
import com.hims.utils.RandomNumGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
@Service
public class HelperUtils {

    @Autowired
    public RandomNumGenerator randomNumGenerator;

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
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public static String extractTimeFromInstant(Instant instant) {
        return instant.atZone(ZoneId.of("Asia/Kolkata"))
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm"));
    }


    public static String instantTimeToLocalDateTime(Instant instant) {
        return instant.atZone(ZoneId.of("Asia/Kolkata"))
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String createInvoices() {
        return randomNumGenerator.generateOrderNumber("BILL",true,true);
    }

    public String createInvoiceNumber() {
        return randomNumGenerator.generateOrderNumber("HIMS", true, true);
    }

    public void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }
        if (size <= 0) {
            throw new IllegalArgumentException(
                    "Page size must be greater than zero"
            );
        }
        if (size > 100) {
            throw new IllegalArgumentException(
                    "Page size cannot be greater than 100"
            );
        }
    }

    public void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return;
        }
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException(
                    "Both from date and to date are required when using date filter"
            );
        }
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "From date cannot be greater than to date"
            );
        }
    }


    public String cleanValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    public String normalizeRefundStatusFilter(String value) {
        String cleaned = cleanValue(value);
        if (cleaned == null) {
            return null;
        }

        if ("completed".equalsIgnoreCase(cleaned) || AppConstants.STATUS_Y.equalsIgnoreCase(cleaned)) {
            return AppConstants.STATUS_Y.toLowerCase();
        }

        if ("pending".equalsIgnoreCase(cleaned) || AppConstants.STATUS_N.equalsIgnoreCase(cleaned)) {
            return AppConstants.STATUS_N.toLowerCase();
        }

        if ("all".equalsIgnoreCase(cleaned)) {
            return null;
        }

        return cleaned.toUpperCase();
    }

}
