package com.hims.response;

import com.hims.entity.DgSubMasInvestigation;
import com.hims.entity.MasMainChargeCode;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class DgNormalValueResponse {
    private Long normalId;
    private String sex;
    private Long fromAge;
    private Long toAge;
    private String minNormalValue;
    private String maxNormalValue;
    private String normalValue;
    private Long subInvestigationId ;
    private Long mainChargeCodeId;
}
