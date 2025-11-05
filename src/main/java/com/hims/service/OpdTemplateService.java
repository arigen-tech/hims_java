package com.hims.service;

import com.hims.entity.OpdTemplateInvestigation;
import com.hims.request.InvestigationByTemplateRequest;
import com.hims.request.OpdTempInvReq;
import com.hims.request.OpdTemplateInvestigationRequest;
import com.hims.request.OpdTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.InvestigationByTemplateResponse;
import com.hims.response.OpdTemplateInvestigationResponse;
import com.hims.response.OpdTemplateResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OpdTemplateService {
    public ApiResponse<OpdTemplateResponse> getByTemplateId(Long templateId);

    public ApiResponse<OpdTemplateResponse> createOpdTemplate(OpdTemplateRequest opdTempReq);

    public ApiResponse<String> updateOpdTemplate(OpdTempInvReq opdTempInvReq);

    public ApiResponse<InvestigationByTemplateResponse> multiInvestigationTemplate (InvestigationByTemplateRequest investByTempReq);
}
