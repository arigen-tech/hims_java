package com.hims.response;

import com.hims.projection.OpdBillingProjection;
import lombok.Data;

import java.util.List;

@Data
public class OpdPendingBillingResponse{
    private Long patientId;
    private List<Long> visitIds;
    private String mobileNo;
    private String registrationNo;
    private Long billingHdId;
    private String patientName;
    private String age;
    private String gender;
    private String relation;
    private String billingType;
    private String consultingDoctorName;
    private String departmentName;
    private Double netAmount;
    private String appointmentDate;


    public OpdPendingBillingResponse() {
    }
}
