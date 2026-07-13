package com.hims.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PaidCancelledAppointmentResponse {
    private Long visitId;
    private Long patientId;
    private Long billingHeaderId;
    private String registrationNo;
    private String patientName;
    private String mobileNo;
    private Integer age;
    private String gender;
    private String billingType;
    private LocalDate date;
    private Long billingAmount;
    private LocalDate cancelledDate;
    private LocalDate refundDate;
    private String refundStatus;
    private String departmentName;
}
