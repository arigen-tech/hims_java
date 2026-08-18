package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SampleCollectionRequest {
    int visitId;
    Long orderHdId;
    Long inpatientId;
    List<SampleCollectionInvestigationReq> sampleCollectionReq;
}
