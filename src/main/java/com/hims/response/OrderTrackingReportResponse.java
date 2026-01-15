package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class OrderTrackingReportResponse {
    private Long dgOrderHdId;
    private  String orderNum;
    private String patientName;
    private String mobileNum;
    private String age;
    private String gender;
    private String generatedSampleId;
    private String investigationName;
    private Long orderStatusId;
    private String orderStatusName;
    private LocalDate orderDate;

}
