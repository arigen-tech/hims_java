package com.hims.controller;

import com.hims.request.OpdOpthDetailsRequest;
import com.hims.response.*;
import com.hims.request.OpdPatientDetailFinalRequest;
import com.hims.service.OpdOpthDetailsService;
import com.hims.service.OpdPatientDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OPD (Out-Patient Department) Controller
 *
 * Handles all OPD-related operations including pre-consultation management
 * and patient waiting list retrieval. This controller provides endpoints
 * for retrieving pending pre-consultations and managing patient queues.
 */
@RestController
@RequestMapping("/opd")
@Tag(name = "OPD Management", description = "APIs for managing OPD operations including pre-consultations and waiting list")
@Slf4j
public class OPDPatientController {


    @Autowired
    private OpdOpthDetailsService opdOpthDetailsService;

    @Autowired
    private OpdPatientDetailService opdPatientDetailService;

    /**
     * Retrieves a list of pending pre-consultations for the current hospital with pagination.
     *
     * This endpoint fetches all patients whose pre-consultation status is pending
     * and have not yet been marked as completed. Used by OPD staff to manage
     * the queue of patients awaiting preliminary assessment. Results are paginated
     * at the database level for optimal performance and scalability.
     *
     * @param page the page number (0-indexed), default is 0
     * @param size the number of records per page, default is 10
     * j@return ResponseEntity containing ApiResponse with paginated OpdPreConsultationResponse
     */
    @GetMapping("/getPendingPreConsultations")
    @Operation(
            summary = "Get Pending Pre-Consultations",
            description = "Fetches a paginated list of all pending pre-consultations for the current hospital. " +
                    "This endpoint returns patient details including name, age, gender, appointment date/time, " +
                    "assigned doctor, department, and token number. Results are retrieved directly from the database " +
                    "with efficient server-side pagination and sorting capabilities."
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

    /**
     * Retrieves the patient waiting list for the current hospital.
     *
     * This endpoint fetches all patients currently waiting for OPD consultation
     * in the hospital. It provides essential patient information including token
     * number, name, age, gender, and relation to help manage patient flow.
     *
     * @return ResponseEntity containing ApiResponse with list of PatientWaitingListResponse
     */
    @GetMapping("/getOpdWaitingList")
    @Operation(
            summary = "Get Patient Waiting List",
            description = "Fetches the complete list of all patients currently waiting for OPD consultation in the current hospital. " +
                    "Returns essential patient information including token number, patient name, contact number, age, gender, " +
                    "visit type (New/Follow-up/Walk-in), and relation. This list helps manage patient flow and queue management in real-time."
    )
    public ResponseEntity<ApiResponse<Page<PatientWaitingListResponse>>> getWaitingList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String mobileNumber
    ) {
        Pageable pageable = PageRequest.of(page, size);

        ApiResponse<Page<PatientWaitingListResponse>> response =
                opdPatientDetailService.getWaitingList(pageable, patientName, mobileNumber);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/saveOphthalmologyExaminationDetails")
    @Operation(
            summary = "Save OPD Vision Examination Details",
            description = "Saves detailed ophthalmology (vision examination) data for a patient visit. " +
                    "This includes distance and near vision, retinoscopy, keratometry, tonometry, " +
                    "anterior and posterior segment findings, IOL power, spectacle details, and other eye examination parameters " +
                    "for both right eye (RE) and left eye (LE)."
    )
    public ApiResponse<String> opdVisionExaminationDetailsSave(@RequestBody OpdOpthDetailsRequest request) {
        return opdOpthDetailsService.opdVisionExaminationDetailsSave(request);
    }


    /**
     * Creates a new OPD patient detail record with billing information.
     *
     * This endpoint creates a comprehensive OPD patient registration including:
     * - OPD Patient Details (vital signs, medical history)
     * - Order Header (OPD order header information)
     * - Order Details (line items for procedures/investigations)
     * - Billing Header (billing summary)
     * - Billing Details (itemized billing)
     *
     * The structure follows the same pattern as lab registration for consistency
     * across the hospital management system.
     *
     * @param request the OPD patient detail request containing patient, order, and billing information
     * @return ResponseEntity containing ApiResponse with OpdPatientDetailResponseDTO
     */
    @PostMapping("/createOpdPatientDetails")
    @Operation(
            summary = "Create OPD Patient Detail with Lab Billing",
            description = "Creates a new OPD patient registration along with associated order header, order details, " +
                    "billing header, and billing details. This endpoint handles the complete patient intake process " +
                    "including vital registration and billing setup. Similar to lab registration, it creates a hierarchical " +
                    "structure: OrderHeader -> OrderDetails and BillingHeader -> BillingDetails for comprehensive tracking."
    )
    public ResponseEntity<ApiResponse<OpdPatientDetailResponseDTO>> createOpdPatientDetailWithBilling(
            @Valid @RequestBody OpdPatientDetailFinalRequest request) {
        log.info("Creating OPD patient detail with billing information - Patient ID: {}",
                request.getPatientId());
        try {
            ApiResponse<OpdPatientDetailResponseDTO> response = opdPatientDetailService.createOpdPatientDetailWithBilling(request);
            log.info("Successfully created OPD patient detail - Order ID: {}",
                    response.getResponse() != null ? response.getResponse().getOrderId() : "N/A");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating OPD patient detail: ", e);
            throw e;
        }
    }

    @GetMapping("/getOphthalmologyExaminationDetail")
    public ApiResponse<OphthalmologyExaminationDetailResponse> getOphthalmologyExaminationDetail(@RequestParam Long visitId) {
        return opdOpthDetailsService.getOphthalmologyExaminationDetail(visitId);
    }

}