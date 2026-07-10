package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PendingSampleDetailResponse {

    private Integer orderDtId;
    private Long investigationId;
    private  String investigationName;
    private Long sampleId;
    private String sampleName;
    private Long collectionId;
    private String collectionName;
    private Long subChargeCodeId;
    private Long mainChargeCodeId;

}
