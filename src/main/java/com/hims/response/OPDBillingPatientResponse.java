package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class OPDBillingPatientResponse {
    //private Long billinghdid;
    private Long patientid;
    private String patientName;
    private String mobileNo;
    private String age;
    private String sex;
    private String relation;
    private String billingType;
    private String consultedDoctor;
    private String department;
    private BigDecimal amount;
    private String billingStatus;
    private String address;
    private Integer  orderhdid;
    private String orderhdPaymentStatus;
    private String flag;
    private String source;

//    private String visitType;
//    private Long tokenNo;
//    private Instant visitDate;
//    private String sessionName;
//    private BigDecimal registrationCost;



    private List<AppointmentBlock> appointments;
    private List<BillingDetailResponse> details;

}
