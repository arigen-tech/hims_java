package com.hims.response;

import lombok.Data;

import java.util.List;
 @Data
public class ResultInvestigationResponse {
     private Long investigationId;
     private String investigationName;
     private String diagNo;
     private Long sampleCollectionDetailsId;
      private Long sampleId;
      private String sampleName;
      private String unitName;
      private Long unitId;
      private String normalValue;
      private String resultType;
      List<ResultSubInvestigationResponse> resultSubInvestigationResponseList;
}
