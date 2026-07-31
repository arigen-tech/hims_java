package com.hims.m1.util;


import com.hims.m1.request.DemoAuthRequest;

import java.util.*;


public class CreateAbdmRequest {

    public static Map<String, Object> createRequest(
            String txnId,
            String encryptedOtp,
            String mobile
    ) {

        Map<String, Object> otpMap = new HashMap<>();
        otpMap.put("timeStamp", NhaHeaderUtil.generateTimestamp());
        otpMap.put("txnId", txnId);
        otpMap.put("otpValue", encryptedOtp);
        otpMap.put("mobile", mobile);

        Map<String, Object> authDataMap = new HashMap<>();
        authDataMap.put("authMethods", Collections.singletonList("otp"));
        authDataMap.put("otp", otpMap);

        Map<String, Object> consentMap = new HashMap<>();
        consentMap.put("code", "abha-enrollment");
        consentMap.put("version", "1.4");

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("authData", authDataMap);
        requestMap.put("consent", consentMap);
        return requestMap;
    }

    public static Map<String, Object> createAbhaAddressRequest(
            String abhaRequest
    ) {

        Map<String, Object> request = new HashMap<>();
        request.put("abhaAddress", abhaRequest);


        return request;
    }

    public static Map<String, Object> getHealthRecordAndSendOtp(
            String encryHealthNumber, String type
    ) {
        Map<String, Object> request = new HashMap<>();

        if (type.equalsIgnoreCase("Mobile")) {
            List<String> scope = Arrays.asList(
                    "abha-address-login",
                    "mobile-verify"
            );
            request.put("scope", scope);
            request.put("otpSystem", "abdm");

        } else if (type.equalsIgnoreCase("Aadhaar")) {
            List<String> scope = Arrays.asList(
                    "abha-address-login",
                    "aadhaar-verify"
            );
            request.put("scope", scope);
            request.put("otpSystem", "aadhaar");
        }


        request.put("loginHint", "abha-address");
        request.put("loginId", encryHealthNumber);
        return request;
    }

    public static Map<String, Object> createSendOtpToNewNumber(
            String txnId,
            String encryptedMobileNumber
    ) {

        Map<String, Object> requestMap = new HashMap<>();

        requestMap.put("txnId", txnId);

        List<String> scopeList = Arrays.asList(
                "abha-enrol",
                "mobile-verify"
        );

        requestMap.put("scope", scopeList);
        requestMap.put("loginHint", "mobile");
        requestMap.put("loginId", encryptedMobileNumber);
        requestMap.put("otpSystem", "abdm");
        return requestMap;
    }


    public static Map<String, Object> createRequestVerifyFoAnotherNumber(
            String txnId,
            String encryptedOtp
    ) {

        Map<String, Object> request = new HashMap<>();

        request.put("scope", List.of("abha-enrol", "mobile-verify"));
        Map<String, Object> otp = new HashMap<>();
        otp.put("timeStamp", NhaHeaderUtil.generateTimestamp());   // current timestamp (UTC)
        otp.put("txnId", txnId);
        otp.put("otpValue", encryptedOtp);

        Map<String, Object> authData = new HashMap<>();
        authData.put("authMethods", List.of("otp"));
        authData.put("otp", otp);

        request.put("authData", authData);
        return request;
    }


    public static Map<String, Object> createRequestForOtp(
            String txnId,
            String encryptedAadhaar
    ) {

        Map<String, Object> requestMap = new HashMap<>();

        requestMap.put("txnId", txnId);
        requestMap.put("scope", Collections.singletonList("abha-enrol"));
        requestMap.put("loginHint", "aadhaar");
        requestMap.put("loginId", encryptedAadhaar);
        requestMap.put("otpSystem", "aadhaar");

        return requestMap;
    }


    public static Map<String, Object> createRequestForEmail(
            String encryptedEmail
    ) {
        Map<String, Object> requestMap = new HashMap<>();

        requestMap.put("scope", Arrays.asList(
                "abha-profile",
                "email-link-verify"
        ));
        requestMap.put("loginHint", "email");
        requestMap.put("loginId", encryptedEmail);
        requestMap.put("otpSystem", "abdm");

        return requestMap;
    }

    public static Map<String, Object> createRequestForEmailUpdate(
            String encryptedEmail
    ) {
        Map<String, Object> request = new HashMap<>();

        request.put("scope", List.of("abha-profile", "email-verify"));
        request.put("loginHint", "email");
        request.put("loginId", encryptedEmail);   // {{encrypted email}}
        request.put("otpSystem", "abdm");

        return request;
    }

    public static Map<String, Object> createRequestForMobileUpdate(
            String encryptedMobile
    ) {
        Map<String, Object> request = new HashMap<>();
        request.put("scope", List.of("abha-profile", "mobile-verify"));

        request.put("loginHint", "mobile");
        request.put("loginId", encryptedMobile);   // replace with your encrypted mobile
        request.put("otpSystem", "abdm");
        return request;
    }

    public static Map<String, Object> createRequestForEmailUpdateVerify(
            String txnId,
            String encryptedOtp
    ) {
        Map<String, Object> request = new HashMap<>();
        request.put("scope", List.of("abha-profile", "email-verify"));
        Map<String, Object> authData = new HashMap<>();
        authData.put("authMethods", List.of("otp"));

        Map<String, Object> otp = new HashMap<>();
        otp.put("txnId", txnId);                 // {{txnId}}
        otp.put("otpValue", encryptedOtp);       // {{encrypted otp}}

        authData.put("otp", otp);

        request.put("authData", authData);

        return request;
    }

    public static Map<String, Object> createRequestForMobileUpdateVerify(
            String encryptedOtp,
            String txnId
    ) {
        Map<String, Object> request = new HashMap<>();
        request.put("scope", List.of("abha-profile", "mobile-verify"));

        Map<String, Object> authData = new HashMap<>();
        authData.put("authMethods", List.of("otp"));
        Map<String, Object> otp = new HashMap<>();
        otp.put("txnId", txnId);           // {{txnId}}
        otp.put("otpValue", encryptedOtp); // {{encrypted otp}}

        authData.put("otp", otp);

        request.put("authData", authData);

        return request;
    }


    public static Map<String, Object> createRequestForAbhaAddresssUpdate(
            String txnId,
            String abhaAddress
    ) {
        Map<String, Object> request = new HashMap<>();

        request.put("txnId", txnId);
        request.put("abhaAddress", abhaAddress);
        request.put("preferred", 1);
        return request;
    }


    public static Map<String, Object> createRequestUpdateProfile(
            String encryptProfilePhoto
    ) {
        Map<String, Object> request = new HashMap<>();

        request.put("profilePhoto", encryptProfilePhoto);
        return request;
    }

    public static Map<String, Object> buildDemoAuthRequest(DemoAuthRequest demoAuthRequest, String encryptAadhaarNumber) {

        Map<String, Object> demoAuth = new HashMap<>();
        demoAuth.put("aadhaarNumber", encryptAadhaarNumber);
        demoAuth.put("districtCode", demoAuthRequest.getDistrictCode());
        demoAuth.put("stateCode", demoAuthRequest.getStateCode());
        demoAuth.put("dateOfBirth", demoAuthRequest.getYearOfBirth());
        demoAuth.put("gender", demoAuthRequest.getGender());
        demoAuth.put("name", demoAuthRequest.getName());
        demoAuth.put("mobile", demoAuthRequest.getMobile());
        demoAuth.put("profilePhoto", "iVBORw0KGgo=");
        demoAuth.put("address", "NewDelhi");
        demoAuth.put("pincode", "110096");

        Map<String, Object> authData = new HashMap<>();
        authData.put("authMethods", List.of("demo_auth"));
        authData.put("demo_auth", demoAuth);

        Map<String, Object> consent = new HashMap<>();
        consent.put("code", "abha-enrollment");
        consent.put("version", "1.4");

        Map<String, Object> request = new HashMap<>();
        request.put("authData", authData);
        request.put("consent", consent);

        return request;
    }


}
