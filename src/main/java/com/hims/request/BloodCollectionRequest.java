package com.hims.request;

import lombok.Data;
import org.hibernate.query.sql.internal.ParameterRecognizerImpl;

@Data
public class BloodCollectionRequest {
    private Long donorId;
    private Long screeningId;
    private Long donationTypeId;
    private Long collectionTypeId;
    private Long bagTypeId;

    private Integer totalCollectedVolume;

}
