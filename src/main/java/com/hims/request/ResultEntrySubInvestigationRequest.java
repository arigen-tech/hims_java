package com.hims.request;

import com.hims.entity.*;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultEntrySubInvestigationRequest {
    private String result;
    private String remarks;
  //  private Long mainChargeCodeId;
    private Long sampleId;
    private Long investigationId;
    private Long subInvestigationId;
    private String resultType;
    private String comparisonType;
    private Long fixedId;
    private Long normalId;
    private String normalRange;
    private String fixedValue;


}
