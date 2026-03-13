package com.hims.response;

import lombok.Data;

@Data
public class LabBillingPatientResponse {
    private String registrationNo;
    private String mobileNo;
    private String appointmentDate;
    private String patientName;
    private String age;
    private String gender;
    private Double billAmount;
    private Long billingHeaderId;
    private Long dgOrderHdId;
    private Long patientId;
    private String billingType;
}
