package com.hims.request;

import lombok.Data;

import java.util.List;

@Data
public class OpdTempInvReq {
    private Long templateId;
    private List<OpdTemplateInvestigationRequest> opdTempInvest;
    private List<Long> deletedTempIvs;
}
