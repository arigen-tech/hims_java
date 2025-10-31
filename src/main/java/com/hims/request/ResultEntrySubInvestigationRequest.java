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
    private Long chargeCodeId;
    private Long sampleId;
    private Long uomId;
    private Long investigationId;
    private Long subInvestigationId;
   private Long normalValueId;
    private Long fixedValueId;


}
