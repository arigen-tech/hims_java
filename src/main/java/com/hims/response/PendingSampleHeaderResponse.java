package com.hims.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PendingSampleHeaderResponse {
    private LocalDate reqDate;
    private String patientName;
    private String relation;
    private String age;
    private String gender;
    private String mobile;
    private String department;
    private String doctorName;
    private String priority;
    private Long orderHdId;
    private Long visitId;
//    private Long investigationId;
//    private String investigation;
//    private String sample;
//    private String collection;
//    private String subChargeCode;
//    private Long subChargeCodeId;
//    private String orderNo;
//    private Long sampleId;
//    private Long mainChargcodeId;
//    private Long collectionId;
//    private String orderTime;
    //    private String name;




}
