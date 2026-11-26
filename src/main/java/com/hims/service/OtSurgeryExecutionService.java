package com.hims.service;

import com.hims.request.OtSurgeryAnaesthesiaRequest;
import com.hims.request.OtSurgeryExecutionRequest;
import com.hims.request.SurgeryDrugConsumableTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OtSurgeryAnaesthesiaResponse;
import com.hims.response.OtSurgeryExecutionResponse;
import com.hims.response.SurgeryDrugConsumableTemplateResponse;

import java.util.List;

public interface OtSurgeryExecutionService {
    ApiResponse<OtSurgeryExecutionResponse> saveExecution(OtSurgeryExecutionRequest request);
    ApiResponse<OtSurgeryAnaesthesiaResponse> saveAnaesthesia(OtSurgeryAnaesthesiaRequest request);
    ApiResponse<OtSurgeryAnaesthesiaResponse> getAnaesthesiaByBookingId(Long otBookingId);

    ApiResponse<SurgeryDrugConsumableTemplateResponse> saveDrugConsumableTemplate(
            SurgeryDrugConsumableTemplateRequest request);
    ApiResponse<SurgeryDrugConsumableTemplateResponse> getDrugConsumableTemplateById(Long templateId);
    ApiResponse<List<SurgeryDrugConsumableTemplateResponse>> getActiveDrugConsumableTemplates(
            Long surgeryId, String sectionCode);
}
