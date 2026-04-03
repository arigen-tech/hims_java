package com.hims.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InvestigationResultResponse {

    private Long sampleCollectionDetailsId;
    private  Long investigationId;
    private String investigationName;
    private Long sampleId;
    private  String sampleName;
    private String unitName;
    private String normalValue;
    private String investigationType;
    private String generatedSampleId;

}
