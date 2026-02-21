package com.hims.service;

import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Visit;
import com.hims.request.*;
import com.hims.response.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface OPDService {

    ApiResponse<OpdPatientVitalResponce> getPatientVitals(Long visitId);

    ApiResponse<OpdPatientDetail> createOPDPatientDetail(OpdPatientDetailFinalRequest request);

    ApiResponse<OpdPatientDetail> updateRecallPatient(RecallOpdPatientDetailRequest request);

    ApiResponse<List<OpdPatientDetailsWaitingresponce>> searchActiveVisits(ActiveVisitSearchRequest request);

    ApiResponse<List<OpdPatientRecallResponce>> getRecallOPDVisits(String name, String mobile, LocalDate visitDate);

    ApiResponse<String> closeVisit(Long visitId, String status);

    Visit updateVisitStatus(Long visitId, Instant visitDate, Long doctorId);

    ApiResponse<List<Visit>> getPendingPreConsultations();

    ApiResponse<String> saveVitals(OpdPatientDetailRequest request);

    ApiResponse<List<Visit>> getOPDWaitingList();

    ApiResponse<List<VisitResponse>> getPatientVisitHistory(int patientId);

    ApiResponse<OpdTemplateResponse> getTemplate(Long templateId);

    ApiResponse<List<OpdTemplateResponse>> getTemplatesByType(int templateType);

    ApiResponse<OpdTemplateResponse> createOPDTemplate(OpdTemplateRequest request);

    ApiResponse<String> updateOPDTemplate(OpdTempInvReq request);

    ApiResponse<InvestigationByTemplateResponse> addTemplateInvestigations(InvestigationByTemplateRequest request);

    ApiResponse<OpdTemplateResponse> saveTemplateTreatment(OpdTemplateRequest request);

    ApiResponse<OpdTemplateResponse> updateTemplateTreatment(Long templateId, OpdTemplateRequest request);

    ApiResponse<List<OpdTemplateResponse>> getAllTemplateTreatments(int templateType);

    Long getAvailableStock(Long hospitalId, Integer departmentId, Long itemId, Integer days);
}




