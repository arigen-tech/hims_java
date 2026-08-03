package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SampleCollectionRequest {
    int visitId;
    int orderHdId;
    Long inpatientId;
    List<SampleCollectionInvestigationReq> sampleCollectionReq;
}
