package com.hims.controller;

import com.hims.request.OpdOpthDetailsRequest;
import com.hims.request.OtRequest;
import com.hims.request.OtTeamMemberRequest;
import com.hims.request.OtBookingRescheduleRequest;
import com.hims.request.OtBookingCancelRequest;
import com.hims.request.OtPacAssessmentRequest;
import com.hims.request.OtChecklistSubmitRequest;
import com.hims.request.PatientConsentRequest;
import com.hims.request.OtSurgeryAnaesthesiaRequest;
import com.hims.request.OtSurgeryExecutionRequest;
import com.hims.request.OtSurgeryDrugRequest;
import com.hims.request.OtSurgeryConsumableRequest;
import com.hims.request.SurgeryDrugConsumableTemplateRequest;
import com.hims.response.ActiveAdmissionOtResponse;
import com.hims.response.ActiveAdmissionResponse;
import com.hims.response.ApiResponse;
import com.hims.response.PendingForOtResponse;
import com.hims.response.OtBookingHistoryResponse;
import com.hims.response.OtBookingHistoryExecutionResponse;
import com.hims.response.OtTeamMemberResponse;
import com.hims.response.OtDashboardResponse;
import com.hims.response.OtDepartmentResponse;
import com.hims.response.PacPendingResponse;
import com.hims.response.OtPacAssessmentResponse;
import com.hims.response.OtChecklistSubmitResponse;
import com.hims.response.PatientConsentResponse;
import com.hims.response.OtSurgeryAnaesthesiaResponse;
import com.hims.response.OtSurgeryExecutionResponse;
import com.hims.response.OtSurgeryDrugResponse;
import com.hims.response.OtSurgeryConsumableResponse;
import com.hims.response.SurgeryDrugConsumableTemplateResponse;
import com.hims.service.OtService;
import com.hims.service.OtPacAssessmentService;
import com.hims.service.OtChecklistSubmissionService;
import com.hims.service.PatientConsentService;
import com.hims.service.OtSurgeryExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;

@RestController
@Validated
@Tag(name = "OTController")
@RequestMapping("/otController")
@Slf4j
@AllArgsConstructor
public class OTController {

    private final OtService otService;
    private final OtPacAssessmentService otPacAssessmentService;
    private final OtChecklistSubmissionService otChecklistSubmissionService;
    private final PatientConsentService patientConsentService;
    private final OtSurgeryExecutionService otSurgeryExecutionService;


    @GetMapping("/activeAdmissionList")
    public ResponseEntity<ApiResponse<Page<ActiveAdmissionOtResponse>>> activeAdmissionList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) String admissionNo,
            @RequestParam(required = false) Long wardId) {
        log.info("Active admission OT list request. page={}, size={}, patientName={}, mobileNo={}, admissionNo={}, wardId={}", page, size, patientName, mobileNo,
                admissionNo,
                wardId);
        ApiResponse<Page<ActiveAdmissionOtResponse>> response = otService.activeAdmissionList(page, size,
                patientName,
                mobileNo,
                admissionNo,
                wardId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/saveOtRequest")
    public ApiResponse<String> saveOtRequest(@Valid @RequestBody OtRequest request) {
        log.info("Received OT booking request: {}", request);
        ApiResponse<String> response = otService.saveOtRequest(request);
        log.info("OT booking request saved successfully. Response: {}", response);
        return response;
    }

    @GetMapping("/pendingForReviewOtList")
    public ResponseEntity<ApiResponse<Page<PendingForOtResponse>>> pendingForReviewOtList(@RequestParam(defaultValue = "0") int page,
                                                                                      @RequestParam(defaultValue = "5") int size,
                                                                                      @RequestParam(required = false) String patientName,
                                                                                      @RequestParam(required = false) String mobileNo,
                                                                                      @RequestParam(required = false) String patientType) {
        ApiResponse<Page<PendingForOtResponse>> response = otService.pendingForReviewOt(page, size, patientName, mobileNo, patientType);
        return ResponseEntity.ok(response);

    }

    @GetMapping("/bookingHistory")
    @Operation(summary = "Get paginated OT booking history")
    public ResponseEntity<ApiResponse<Page<OtBookingHistoryResponse>>> getOtBookingHistory(
            @RequestParam(required = false) String patient,
            @RequestParam(required = false) @Positive Long patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduledDate,
            @RequestParam(required = false) Boolean ipd,
            @RequestParam(required = false) @Positive Long statusId,
            @RequestParam(required = false) @Positive Long otId,
            @RequestParam(required = false) @Positive Long departmentId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int size) {
        ApiResponse<Page<OtBookingHistoryResponse>> response =
                otService.getOtBookingHistory(patient, patientId, scheduledDate, ipd, statusId, otId, departmentId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patientReceiptList")
    @Operation(summary = "Get paginated OT patient receipt list (sent to OT)")
    public ResponseEntity<ApiResponse<Page<OtBookingHistoryResponse>>> getPatientReceiptList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @Positive Long departmentId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int size) {
        ApiResponse<Page<OtBookingHistoryResponse>> response =
                otService.getPatientReceiptList(search, departmentId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patientReceivedInOtList")
    @Operation(summary = "Get paginated OT checklist list for patients received in OT")
    public ResponseEntity<ApiResponse<Page<OtBookingHistoryResponse>>> getPatientReceivedInOtList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @Positive Long departmentId,
            @RequestParam(required = false) String checklistStatus,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(otService.getPatientReceivedInOtList(search, departmentId, checklistStatus, page, size));
    }

    @GetMapping("/patientSurgeryWorklist")
    @Operation(summary = "Get paginated OT list with surgery execution status")
    public ResponseEntity<ApiResponse<Page<OtBookingHistoryExecutionResponse>>> getPatientReceivedInOtListWithExecutionStatus(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @Positive Long departmentId,
            @RequestParam(required = false) String checklistStatus,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(otService.getPatientReceivedInOtListWithExecutionStatus(
                search, departmentId, checklistStatus, page, size));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get date-wise OT booking dashboard counts")
    public ResponseEntity<ApiResponse<List<OtDashboardResponse>>> getOtDashboard(
            @RequestParam @Min(1) @Max(12) int month,
            @RequestParam @Min(2000) int year,
            @RequestParam(required = false) @Positive Long otId,
            @RequestParam(required = false) @Positive Long departmentId) {
        ApiResponse<List<OtDashboardResponse>> response = otService.getOtDashboard(month, year, otId, departmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/departments")
    @Operation(summary = "Get departments with OT bookings for a hospital")
    public ResponseEntity<ApiResponse<List<OtDepartmentResponse>>> getOtDepartments(
            @RequestParam @Positive Long hospitalId) {
        return ResponseEntity.ok(otService.getOtDepartments(hospitalId));
    }

    @GetMapping("/pacPendingList")
    @Operation(summary = "Get pending PAC cases for scheduled OT bookings")
    public ResponseEntity<ApiResponse<Page<PacPendingResponse>>> getPacPendingList(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "All") String type,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(otService.getPacPendingList(search, type, page, size));
    }
    /**
     * Accept or reject an OT booking request.
     *
     * <p>
     * flag = "A" → Accept the OT booking request
     * flag = "R" → Reject the OT booking request
     * </p>
     *
     * @param otBookingRequestId OT booking request ID
     * @param flag              Action flag: A for Accept, R for Reject
     * @return success/failure response
     */
    @PostMapping("/saveAcceptAndReject")
    public ApiResponse<String> acceptAndRejectRequest(
            @RequestParam(required = false) Long otBookingRequestId,
            @RequestParam(required = false) String flag,
            @RequestParam(required = false) String remark) {

        log.info("Accept/Reject OT request API called: otBookingRequestId={}, flag={}, remark={}", otBookingRequestId, flag, remark);
        ApiResponse<String> response = otService.saveAcceptAndReject(otBookingRequestId, flag, remark);
        log.info("Accept/Reject OT request API completed: otBookingRequestId={}, flag={}, remark={}", otBookingRequestId, flag, remark);
        return response;
    }

    @PutMapping("/otPatientTransaction")
    @Operation(summary = "Update OT booking status for patient transaction")
    public ApiResponse<String> updateOtPatientTransaction(
            @RequestParam @Positive Long otBookingId,
            @RequestParam @NotBlank String status,
            @RequestParam(required = false) String remark) {
        return otService.updateOtPatientTransaction(otBookingId, status, remark);
    }

    @PostMapping("/reschedule")
    @Operation(summary = "Request rescheduling of an OT booking")
    public ApiResponse<String> rescheduleOtBooking(@Valid @RequestBody OtBookingRescheduleRequest request) {
        return otService.rescheduleOtBooking(request);
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel an OT booking")
    public ApiResponse<String> cancelOtBooking(@Valid @RequestBody OtBookingCancelRequest request) {
        return otService.cancelOtBooking(request);
    }

    @PostMapping("/teamMember")
    public ApiResponse<OtTeamMemberResponse> saveOtTeamMember(@Valid @RequestBody OtTeamMemberRequest request) {
        return otService.saveOtTeamMember(request);
    }

    @GetMapping("/teamMember")
    public ApiResponse<OtTeamMemberResponse> getOtTeamMembers(@RequestParam @Positive Long otBookingId) {
        return otService.getOtTeamMembers(otBookingId);
    }

    @PostMapping(value = "/pacAssessment", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<OtPacAssessmentResponse>> savePacAssessment(
            @RequestPart("request") @Valid OtPacAssessmentRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "documentNames", required = false) List<String> documentNames) {
        return ResponseEntity.ok(otPacAssessmentService.save(request, files, documentNames));
    }

    @GetMapping("/pacAssessment/{assessmentId}")
    public ResponseEntity<ApiResponse<OtPacAssessmentResponse>> getPacAssessment(@PathVariable @Positive Long assessmentId) {
        return ResponseEntity.ok(otPacAssessmentService.getById(assessmentId));
    }

    @GetMapping("/pacAssessment/booking/{otBookingId}")
    public ResponseEntity<ApiResponse<OtPacAssessmentResponse>> getPacAssessmentByBooking(@PathVariable @Positive Long otBookingId) {
        return ResponseEntity.ok(otPacAssessmentService.getByBookingId(otBookingId));
    }

    @PostMapping("/submitOtChecklist")
    @Operation(summary = "Save an OT checklist as draft or submit it as completed")
    public ResponseEntity<ApiResponse<OtChecklistSubmitResponse>> submitOtChecklist(
            @Valid @RequestBody OtChecklistSubmitRequest request) {
        return ResponseEntity.ok(otChecklistSubmissionService.submit(request));
    }

    @GetMapping("/getOtChecklist/{otBookingId}")
    @Operation(summary = "Get saved OT checklists by OT booking ID")
    public ResponseEntity<ApiResponse<List<OtChecklistSubmitResponse>>> getOtChecklistByBookingId(
            @PathVariable @Positive Long otBookingId) {
        return ResponseEntity.ok(otChecklistSubmissionService.getByBookingId(otBookingId));
    }

    @GetMapping("/inpatient/{inpatientId}/surgeries")
    @Operation(summary = "Get surgeries for an inpatient with an active OT booking status")
    public ResponseEntity<ApiResponse<List<OtBookingHistoryResponse.SurgeryDetail>>> getSurgeriesByInpatientId(
            @PathVariable @Positive Long inpatientId) {
        return ResponseEntity.ok(otService.getSurgeriesByInpatientId(inpatientId));
    }

    @PostMapping("/surgeryAnaesthesia")
    @Operation(summary = "Save or update surgery execution and anaesthesia by OT booking")
    public ResponseEntity<ApiResponse<OtSurgeryAnaesthesiaResponse>> saveSurgeryAnaesthesia(
            @Valid @RequestBody OtSurgeryAnaesthesiaRequest request) {
        return ResponseEntity.ok(otSurgeryExecutionService.saveAnaesthesia(request));
    }

    @PostMapping("/surgeryExecution")
    @Operation(summary = "Create or update surgery execution")
    public ResponseEntity<ApiResponse<OtSurgeryExecutionResponse>> saveSurgeryExecution(
            @Valid @RequestBody OtSurgeryExecutionRequest request) {
        return ResponseEntity.ok(otSurgeryExecutionService.saveExecution(request));
    }

    @PostMapping("/surgeryDrug")
    public ResponseEntity<ApiResponse<List<OtSurgeryDrugResponse>>> saveSurgeryDrug(
            @Valid @RequestBody OtSurgeryDrugRequest request) {
        return ResponseEntity.ok(otSurgeryExecutionService.saveDrug(request));
    }

    @GetMapping("/surgeryDrug")
    @Operation(summary = "Get active OT surgery drugs, optionally filtered and paginated")
    public ResponseEntity<ApiResponse<?>> getSurgeryDrugs(
            @RequestParam(required = false) @Positive Long otBookingId,
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(200) Integer size) {
        if ((page == null) != (size == null)) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "page and size must be provided together"));
        }
        ApiResponse<?> response = page == null
                ? otSurgeryExecutionService.getDrugs(otBookingId)
                : otSurgeryExecutionService.getDrugs(otBookingId, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/surgeryConsumable")
    @Operation(summary = "Save OT surgery consumables")
    public ResponseEntity<ApiResponse<List<OtSurgeryConsumableResponse>>> saveSurgeryConsumable(
            @Valid @RequestBody OtSurgeryConsumableRequest request) {
        return ResponseEntity.ok(otSurgeryExecutionService.saveConsumables(request));
    }

    @GetMapping("/surgeryConsumable")
    @Operation(summary = "Get active OT surgery consumables, optionally filtered and paginated")
    public ResponseEntity<ApiResponse<?>> getSurgeryConsumables(
            @RequestParam(required = false) @Positive Long otBookingId,
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(200) Integer size) {
        if ((page == null) != (size == null)) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, "page and size must be provided together"));
        }
        ApiResponse<?> response = page == null
                ? otSurgeryExecutionService.getConsumables(otBookingId)
                : otSurgeryExecutionService.getConsumables(otBookingId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/surgeryAnaesthesia/{otBookingId}")
    @Operation(summary = "Get surgery execution and anaesthesia by OT booking")
    public ResponseEntity<ApiResponse<OtSurgeryAnaesthesiaResponse>> getSurgeryAnaesthesia(
            @PathVariable @Positive Long otBookingId) {
        return ResponseEntity.ok(otSurgeryExecutionService.getAnaesthesiaByBookingId(otBookingId));
    }

    @PostMapping("/surgeryDrugConsumableTemplate")
    @Operation(summary = "Create or update a surgery drug or consumable template")
    public ResponseEntity<ApiResponse<SurgeryDrugConsumableTemplateResponse>> saveSurgeryDrugConsumableTemplate(
            @Valid @RequestBody SurgeryDrugConsumableTemplateRequest request) {
        return ResponseEntity.ok(otSurgeryExecutionService.saveDrugConsumableTemplate(request));
    }

    @GetMapping("/surgeryDrugConsumableTemplate/{templateId}")
    @Operation(summary = "Get a surgery drug or consumable template by ID")
    public ResponseEntity<ApiResponse<SurgeryDrugConsumableTemplateResponse>> getSurgeryDrugConsumableTemplate(
            @PathVariable @Positive Long templateId) {
        return ResponseEntity.ok(otSurgeryExecutionService.getDrugConsumableTemplateById(templateId));
    }

    @GetMapping("/surgeryDrugConsumableTemplate/active")
    @Operation(summary = "Get active templates by surgery and item section category")
    public ResponseEntity<ApiResponse<List<SurgeryDrugConsumableTemplateResponse>>> getActiveSurgeryDrugConsumableTemplates(
            @RequestParam @Positive Long surgeryId,
            @RequestParam @NotBlank String sectionCode) {
        return ResponseEntity.ok(otSurgeryExecutionService
                .getActiveDrugConsumableTemplates(surgeryId, sectionCode));
    }

    @PostMapping(value = "/patientConsent", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a patient consent document")
    public ResponseEntity<ApiResponse<PatientConsentResponse>> uploadPatientConsent(
            @Valid @ModelAttribute PatientConsentRequest request) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(patientConsentService.upload(request));
    }

    @GetMapping("/patientConsent")
    @Operation(summary = "Get patient consents by admission ID and/or OT booking ID")
    public ResponseEntity<ApiResponse<List<PatientConsentResponse>>> getPatientConsents(
            @RequestParam(required = false) @Positive Long admissionId,
            @RequestParam(required = false) @Positive Long otBookingId) {
        return ResponseEntity.ok(patientConsentService.getConsents(admissionId, otBookingId));
    }

    @GetMapping("/patientConsent/document/{patientConsentId}")
    @Operation(summary = "Open an uploaded patient consent document")
    public ResponseEntity<Resource> openPatientConsentDocument(
            @PathVariable @Positive Long patientConsentId) {
        PatientConsentService.PatientConsentFile file = patientConsentService.getDocument(patientConsentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(file.resource());
    }

}
