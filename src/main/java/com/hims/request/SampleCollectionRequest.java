package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SampleCollectionRequest {
    String patientType;
    //int collectionCenterId;
   // int OrderByDept;
    //int inpatientId;
    int visitId;
    ///int hinId;
    int orderHdId;
    List<SampleCollectionInvestigationReq> sampleCollectionReq;



}
