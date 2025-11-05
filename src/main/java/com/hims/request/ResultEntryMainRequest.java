package com.hims.request;

import com.hims.entity.DgMasInvestigation;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ResultEntryMainRequest {
    private Long relationId;
    private Long mainChargeCodeId;
    private Long subChargeCodeId;
    private String clinicalNotes;
    private Long sampleCollectionHeaderId;
    private Long patientId;

    private List<ResultEntryInvestigationRequest> investigationList;
}
