package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderTrackingReportResponse {
    private Integer dgOrderHdId;
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
