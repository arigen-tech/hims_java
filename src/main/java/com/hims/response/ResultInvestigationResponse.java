package com.hims.response;

import lombok.Data;

import java.util.List;
 @Data
public class ResultInvestigationResponse {
     private Long investigationId;
     private String investigationName;
     List<ResultSubInvestigationResponse> resultSubInvestigationResponseList;
}
