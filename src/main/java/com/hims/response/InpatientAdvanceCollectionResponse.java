package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InpatientAdvanceCollectionResponse {
    private Long inpatientId;
    private Long BillingHeaderId;
    private String uhid;
    private String patientName;
    private String age;
    private Long genderId;
    private String gender;
    private String mobileNo;
    private String admissionNo;
    private Long wardId;
    private String ward;
    private Long rooId;
    private String room;
    private Long bedId;
    private String bed;
    private LocalDateTime admissionDateTime;
    private Long billingTypeId;
    private String billingType;

}
