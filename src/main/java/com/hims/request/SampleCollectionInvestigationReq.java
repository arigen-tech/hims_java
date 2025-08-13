package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SampleCollectionInvestigationReq {
    int subChargeCodeId;
    int investigationId;
    String empanelledStatus;
    int sampleId;
    int collectionId;
    String collected;
    String remarks;


}
