package com.hims.request;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultEntryInvestigationRequest {
    private Long investigationId;
    private Long sampleCollectionDetailsId;
    List<ResultEntrySubInvestigationRequest> resultEntryDetailsRequestList;
}
