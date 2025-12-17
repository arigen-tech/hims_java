package com.hims.response;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Data
public class FollowUpPatientResponseDetails {

    private Long patientId;
    private PersonalDetails personal;
    private AddressDetails address;
    private NokDetails nok;
    private EmergencyDetails emergency;
    private VitalDetails vitals;

    private List<AppointmentDetailResponse> appointments;

    private BillingSummary billing;

    private String photoUrl; // optional → saved image path

    @Data
    public static class PersonalDetails {
        private String firstName;
        private String middleName;
        private String lastName;
        private String mobileNo;
        private String email;
        private LocalDate dob;
        private String age;
        private Long gender;
        private Long relation;
    }

    @Data
    public static class AddressDetails {
        private String address1;
        private String address2;
        private Long country;
        private Long state;
        private Long district;
        private String city;
        private String pinCode;
    }

    @Data
    public static class NokDetails {
        private String firstName;
        private String middleName;
        private String lastName;
        private String email;
        private String mobileNo;
        private String address1;
        private String address2;
        private Long country;
        private Long state;
        private Long district;
        private String city;
        private String pinCode;
    }

    @Data
    public static class EmergencyDetails {
        private String firstName;
        private String lastName;
        private String mobileNo;
    }

    @Data
    public static class VitalDetails {
        private String height;
        private String weight;
        private String temperature;
        private String pulse;
        private String bpSys;
        private String bpDia;
        private String rr;
        private String spo2;
        private String bmi;
    }

    @Data
    public static class AppointmentDetailResponse {
        private Long appointmentId;
        private Long specialityId;
        private String specialityName;
        private Long doctorId;
        private String doctorName;
        private Long sessionId;
        private String sessionName;
        private Instant visitDate;
        private String visitType;
        private Long tokenNo;
    }

    @Data
    public static class BillingSummary {
        private Long billingHdId;
        private BigDecimal registrationCost;
        private BigDecimal netAmount;
        private BigDecimal taxAmount;
        private BigDecimal discount;

        private List<BillingDetailResponse> billingDetails;
    }

    @Data
    public static class BillingDetailResponse {
        private Long id;
        private BigDecimal basePrice;
        private BigDecimal registrationCost;
        private BigDecimal netAmount;
        private BigDecimal taxAmount;
        private BigDecimal discount;
        private String billingType; // Y / N
    }
}