package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdvanceCollectionRequest {
    private Long inpatientId;
    private LocalDateTime collectionDateTime;
    private Long collectionTypeId;
    List<AdvanceCollectionDetailsRequest> requests;
    @Data
    public static class  AdvanceCollectionDetailsRequest{
        private Long modeType;
        private BigDecimal amount;

    }

}
