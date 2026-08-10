package com.hims.controller;

import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * OPD (Out-Patient Department) Controller
 * <p>
 * Handles all OPD-related operations including pre-consultation management
 * and patient waiting list retrieval. This controller provides endpoints
 * for retrieving pending pre-consultations and managing patient queues.
 */
@RestController
@RequestMapping("/opd")
@Tag(name = "OPD Management", description = "OPD patient management and consultation APIs")
@Slf4j
@RequiredArgsConstructor
public class OPDPatientController {

    private final OpdOpthDetailsService opdOpthDetailsService;
    private final OpdPatientDetailService opdPatientDetailService;
    private final OpdObgDetailsService opdObgDetailsService;
    private final OpdEntDetailsService opdEntDetailsService;
    @Autowired
    private OpdQuestionMasterService opdQuestionMasterService;


    @GetMapping("/getPendingPreConsultations")
    @Operation(
            summary = "Get Pending Pre-Consultations",
            description = "Retrieve paginated list of pending pre-consultations with patient name and mobile number filters"
    )
    public ResponseEntity<ApiResponse<Page<OpdPreConsultationResponse>>> getPendingPreConsultations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String mobileNumber
    ) {
        Pageable pageable = PageRequest.of(page, size);
        ApiResponse<Page<OpdPreConsultationResponse>> response =
                opdPatientDetailService.getPendingPreConsultations(pageable, patientName, mobileNumber);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getOpdWaitingList")
    @Operation(
            summary = "Get Patient Waiting List",
            description = "Retrieve paginated list of patients in waiting list with filters by patient name, mobile number, doctor ID, and session ID"
    )
    public ResponseEntity<ApiResponse<Page<PatientWaitingListResponse>>> getWaitingList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long sessionId
    ) {
        Pageable pageable = PageRequest.of(page, size);
        ApiResponse<Page<PatientWaitingListResponse>> response =
                opdPatientDetailService.getWaitingList(pageable, patientName, mobileNumber, doctorId, sessionId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/saveOphthalmologyExaminationDetails")
    @Operation(
            summary = "Save Vision Examination Details",
            description = "Save ophthalmology examination data for a patient visit"
    )
    public ApiResponse<String> opdVisionExaminationDetailsSave(@Valid @RequestBody OpdOpthDetailsRequest request) {
        log.info("Saving OPD vision examination details");
        return opdOpthDetailsService.opdVisionExaminationDetailsSave(request);
    }

    @PostMapping("/createOpdPatientDetails")
    @Operation(
            summary = "Create OPD Patient Registration",
            description = "Create OPD patient registration with order header, details, billing header, and details"
    )
    public ResponseEntity<ApiResponse<OpdPatientDetailResponseDTO>> createOpdPatientDetails(
            @Valid @RequestBody OpdPatientDetailCreateRequest request) {
        log.info("Creating OPD patient detail - Patient ID: {}", request.getPatientId());
        ApiResponse<OpdPatientDetailResponseDTO> response = opdPatientDetailService.createOpdPatientDetail(request);
        log.info("Successfully created OPD patient detail - Order ID: {}",
                response.getResponse() != null ? response.getResponse().getOrderId() : "N/A");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieve prescription details of a patient within the last 30 days.
     * <p>
     * Each patient can have multiple prescriptions. This endpoint fetches all
     * prescriptions issued within the last 30 days with their associated medication
     * details including dosage, frequency, cost, and billing information.
     */
    @GetMapping("/getPatientPrescriptionDetails/{patientId}")
    @Operation(
            summary = "Get Patient Prescription Details",
            description = "Retrieve all prescription details for a patient within the last 30 days with medication information"
    )
    public ResponseEntity<ApiResponse<List<PrescriptionDetailResponse>>> getPatientPrescriptionDetails(@PathVariable(name = "patientId") Long patientId) {
        log.info("Retrieving prescription details for patient ID: {}", patientId);

        ApiResponse<List<PrescriptionDetailResponse>> response = opdPatientDetailService.getPrescriptionDetailsByPatientId(patientId);

        if (response.getStatus() == 200) {
            log.info("Successfully retrieved prescriptions for patient ID: {}", patientId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatus()));
        }
    }

    /**
     * Fetch ophthalmology examination details for a specific visit.
     *
     * @param visitId Unique ID of the visit
     * @return ApiResponse containing ophthalmology examination details
     */
    @GetMapping("/getOphthalmologyExaminationDetail")
    public ApiResponse<OphthalmologyExaminationDetailResponse> getOphthalmologyExaminationDetail(@RequestParam Long visitId) {
        return opdOpthDetailsService.getOphthalmologyExaminationDetail(visitId);
    }

    /**
     * Fetch previous OPD history of a patient with pagination.
     *
     * @param patientId  Unique ID of the patient
     * @param hospitalId Unique ID of the hospital
     * @param page       Page number (default = 0)
     * @param size       Number of records per page (default = 5)
     * @return ApiResponse containing paginated list of patient history
     */
    @GetMapping("/getPreviousOpdVisitHistory")
    public ApiResponse<Page<PreviousOpdVisitResponse>> getPreviousOpdVisitHistory(
            @RequestParam Long patientId,
            @RequestParam Long hospitalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return opdPatientDetailService.getPreviousOpdVisit(patientId, hospitalId, page, size);
    }

    /**
     * Fetch previous vitals details of a patient with pagination.
     *
     * @param patientId  Unique ID of the patient
     * @param hospitalId Unique ID of the hospital
     * @param page       Page number for pagination (default = 0 → first page)
     * @param size       Number of records per page (default = 5)
     * @return ApiResponse containing paginated list of previous vitals details
     */
    @GetMapping("/getPreviousOpdVitalsDetailsHistory")
    public ApiResponse<Page<PreviousOpdVitalsDetailsResponse>> getPreviousOpdVitalsDetailsHistory(
            @RequestParam Long patientId,
            @RequestParam Long hospitalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return opdPatientDetailService.getPreviousOpdVitalsDetailsHistory(patientId, hospitalId, page, size);
    }

    /**
     * Fetch previous psychiatry assessment history of a patient with pagination.
     *
     * @param patientId  Unique ID of the patient
     * @param hospitalId Unique ID of the hospital
     * @param page       Page number for pagination (default = 0)
     * @param size       Number of records per page (default = 5)
     * @return ApiResponse containing paginated psychiatrist history entries
     */
    @GetMapping("/getPreviousOpdPsychiatristDetailsHistory")
    public ApiResponse<Page<PreviousOpdPsychiatryHistoryResponse>> getPreviousOpdPsychiatristDetailsHistory(
            @RequestParam Long patientId,
            @RequestParam Long hospitalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return opdPatientDetailService.getPreviousOpdPsychiatryDetailsHistory(patientId, hospitalId, page, size);
    }


    @GetMapping("/getRecallOpdPatientVisitList")
    public ResponseEntity<ApiResponse<Page<OpdRecallVisitResponse>>> getRecallOpdVisit(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        ApiResponse<Page<OpdRecallVisitResponse>> response = opdPatientDetailService.getRecallOpdVisit(name, mobile, visitDate, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recallPatientDetailsByVisit")
    public ResponseEntity<ApiResponse<OpdPatientRecallResponce>> getRecallVisits(
            @RequestParam Long visitId) {
        log.info("Received request for recall visit details. visitId={}", visitId);
        ApiResponse<OpdPatientRecallResponce> response = opdPatientDetailService.getRecallVisit(visitId);
        log.info("Successfully fetched recall visit details. visitId={}", visitId);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/update-recall-patient")
    public ResponseEntity<ApiResponse<String>> updateRecallOpdPatient(@Valid @RequestBody RecallOpdPatientDetailRequest request) {
        log.info("==== START updateRecallOpdPatient API ====");
        log.info("Request received for OPD Patient ID : {}", request.getPatientId());
        ApiResponse<String> response = opdPatientDetailService.updateRecallOpdPatientDetail(request);
        log.info("Recall OPD patient updated successfully for OPD ID : {}", request.getPatientId());
        log.info("==== END updateRecallOpdPatient API ====");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/saveOrUpdateOBGDetails/{visitId}")
    @Operation(
            summary = "Create or Update OBG Examination Details",
            description = "Create new or update existing OBG (Obstetrics and Gynecology) examination details for a visit. Since one visit can have only one OBG record, this endpoint intelligently creates or updates as needed."
    )
    public ApiResponse<String> saveOrUpdateOBGDetails(
            @PathVariable Long visitId,
            @Valid @RequestBody OpdObgDetailsRequest request) {
        log.info("Creating or updating OBG details for visit ID: {}", visitId);
        return opdObgDetailsService.createOrUpdateObgDetails(visitId, request);
    }

    @GetMapping("/getOBGDetailsByVisit")
    @Operation(
            summary = "Get OBG Examination Details by Visit",
            description = "Retrieve OBG (Obstetrics and Gynecology) examination details for a specific patient visit"
    )
    public ApiResponse<OpdObgDetailsResponse> getOBGDetailsByVisit(@RequestParam Long visitId) {
        log.info("Fetching OBG details for visit ID: {}", visitId);
        return opdObgDetailsService.getObgDetailsByVisitId(visitId);
    }


    @PostMapping("/saveOrUpdateEntDetails/{visitId}")
    @Operation(
            summary = "Create or Update ENT Examination Details",
            description = "Create new or update existing ENT (Ear, Nose, and Throat) examination details for a visit. Since one visit can have only one ENT record, this endpoint intelligently creates or updates as needed."
    )
    public ApiResponse<String> saveOrUpdateEntDetails(
            @PathVariable Long visitId,
            @Valid @RequestBody OpdEntDetailsRequest request) {
        log.info("Creating or updating ENT details for visit ID: {}", visitId);
        return opdEntDetailsService.createOrUpdateEntDetails(visitId, request);
    }

    @GetMapping("/getEntDetailsByVisit")
    @Operation(
            summary = "Get ENT Details",
            description = "Fetch OPD ENT details based on the provided Visit ID."
    )
    public ApiResponse<OpdEntDetailsResponse> getEntDetailsByVisit(@RequestParam Long visitId) {
        log.info("Received request to fetch ENT details for visitId: {}", visitId);
        return opdEntDetailsService.getEntDetailsByVisit(visitId);

    }
    @GetMapping("/getQuestionWiseAnswerValue/{questionHeadingId}")
    public ApiResponse<List<QuestionWiseAnswerResponse>> getQuestionWiseAnswer(@PathVariable Long questionHeadingId) {
        log.info("Received request to get question-wise answer for questionHeadingId: {}", questionHeadingId);
        return opdQuestionMasterService.getQuestionWiseAnswer(questionHeadingId);

    }

    @GetMapping("/getOpdReportsList")
    public ApiResponse<Page<OpdReportListResponse>> getOpdReportsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) Long hospitalId
    ) {

        log.info("Received request to fetch OPD reports. Filters - mobileNo: {}, patientName: {}, hospitalId: {}, page: {}, size: {}",
                mobileNo, patientName, hospitalId, page, size);

        Pageable pageable = PageRequest.of(page, size);

        return opdPatientDetailService.getOpdReportsList(pageable, mobileNo, patientName, hospitalId);
    }

}
