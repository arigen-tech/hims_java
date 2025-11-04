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

}
