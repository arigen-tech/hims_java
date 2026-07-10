package com.hims.m1.client;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "abdm")
public class AbdmProperties {

    private String environment;

    private Urls urls = new Urls();

    private Client client = new Client();

    private M2 m2 = new M2();



    @Data
    public static class Urls {
        private String gatewaySession;
        private String abhaCertificate;
        private String doctorGeneratePmjayOtp;
        private String doctorVerifyPmjayOtp;
        private String facilitySendOtpToContact;
        private String facilityValidateOtp;
        private String hprPublicCertificate;
        private String hprGenerateVerificationEmail;
        private String hprResentVerifyEmail;
        private String hprVerifyEmailOtp;
        private String hprGenerateMobileOtp;
        private String hprRegenerateMobileOtp;
        private String hprVerifyMobileOtp;
        private String enrollmentRequestOtp;
        private String enrollmentEnrolByAadhaar;
        private String enrollmentEnrolSuggestion;
        private String enrollmentAuthByAbdm;
        private String enrollmentEnrolAbhaAddress;
        private String profileLoginRequestOtp;
        private String profileLoginVerify;
        private String profileAccountAbhaSearch;
        private String profileAccountRequestEmailVerificationLink;
        private String profileAccountRequestOtp;
        private String profileAccount;
        private String profileAccountVerify;
        private String profileAccountQrCode;
        private String profileAccountAbhaCard;
        private String phrLoginAbhaRequestOtp;
        private String phrLoginAbhaVerify;
        private String phrLoginAbhaSearch;
        private String phrProfileAbhaProfile;
        private String phrProfilePhrCard;
        private String gatewayBridgeUrl;
        private String gatewayBridgeServices;
        private String gatewayBridgeServiceById;
        private String m2TokenGenerate;
        private String m2LinkCareContext;
        private String m2LinkedCareContexts;
        private String m2ContextNotify;
        private String m2SmsNotify;
        private String m2UserOnDiscover;
        private String m2UserOnInit;
        private String m2UserOnConfirm;
        private String m2ConsentHipOnNotify;
        private String m2HealthInfoHipOnRequest;
        private String m2HealthInfoNotify;
        private String m2PatientOnShare;
    }

    @Data
    public static class Client {
        private String id;
        private String secret;
    }

    @Data
    public static class M2 {
        private String hipId;
        private String hiuId;
        private String cmId;
        private String bridgeUrl;
        private Boolean autoGatewayCallbacks = true;
    }
}
