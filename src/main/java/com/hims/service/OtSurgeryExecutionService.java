package com.hims.service;

import com.hims.request.OtSurgeryAnaesthesiaRequest;
import com.hims.request.OtSurgeryExecutionRequest;
import com.hims.request.OtSurgeryDrugRequest;
import com.hims.request.OtSurgeryConsumableRequest;
import com.hims.request.SurgeryDrugConsumableTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OtSurgeryAnaesthesiaResponse;
import com.hims.response.OtSurgeryExecutionResponse;
import com.hims.response.OtSurgeryDrugResponse;
import com.hims.response.OtSurgeryConsumableResponse;
import org.springframework.data.domain.Page;
import java.util.List;
import com.hims.response.SurgeryDrugConsumableTemplateResponse;

public interface OtSurgeryExecutionService {
    ApiResponse<OtSurgeryExecutionResponse> saveExecution(OtSurgeryExecutionRequest request);
    ApiResponse<List<OtSurgeryDrugResponse>> saveDrug(OtSurgeryDrugRequest request);
    ApiResponse<List<OtSurgeryDrugResponse>> getDrugs(Long otBookingId);
    ApiResponse<Page<OtSurgeryDrugResponse>> getDrugs(Long otBookingId, int page, int size);
    ApiResponse<List<OtSurgeryConsumableResponse>> saveConsumables(OtSurgeryConsumableRequest request);
    ApiResponse<List<OtSurgeryConsumableResponse>> getConsumables(Long otBookingId);
    ApiResponse<Page<OtSurgeryConsumableResponse>> getConsumables(Long otBookingId, int page, int size);
    ApiResponse<OtSurgeryAnaesthesiaResponse> saveAnaesthesia(OtSurgeryAnaesthesiaRequest request);
    ApiResponse<OtSurgeryAnaesthesiaResponse> getAnaesthesiaByBookingId(Long otBookingId);

    ApiResponse<SurgeryDrugConsumableTemplateResponse> saveDrugConsumableTemplate(
            SurgeryDrugConsumableTemplateRequest request);
    ApiResponse<SurgeryDrugConsumableTemplateResponse> getDrugConsumableTemplateById(Long templateId);
    ApiResponse<List<SurgeryDrugConsumableTemplateResponse>> getActiveDrugConsumableTemplates(
            Long surgeryId, String sectionCode);
}
