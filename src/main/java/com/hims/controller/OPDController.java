package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.OpdPreConsultationResponse;
import com.hims.response.PatientWaitingListResponse;
import com.hims.service.OPDService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
public class OPDController {

    @Autowired
    private OPDService opdService;

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
     * @param sortBy the field to sort by, default is 'visitDate'
     * @return ResponseEntity containing ApiResponse with paginated OpdPreConsultationResponse
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
            @RequestParam(defaultValue = "5") int size
    ) {
        log.info("Fetching pending pre-consultations - page: {}, size: {}, sortBy: {}", page, size);
        try {
            Pageable pageable = PageRequest.of(page, size);

            ApiResponse<Page<OpdPreConsultationResponse>> response =
                    opdService.getPendingPreConsultations(pageable);
            Page<OpdPreConsultationResponse> responseData = response.getResponse();
            log.info("Successfully fetched {} pending pre-consultations from page {}, total records: {}",
                    responseData != null ? responseData.getNumberOfElements() : 0,
                    page,
                    responseData != null ? responseData.getTotalElements() : 0);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching pending pre-consultations: ", e);
            throw e;
        }
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
    @GetMapping("/getWaitingList")
    @Operation(
            summary = "Get Patient Waiting List",
            description = "Fetches the complete list of all patients currently waiting for OPD consultation in the current hospital. " +
                    "Returns essential patient information including token number, patient name, contact number, age, gender, " +
                    "visit type (New/Follow-up/Walk-in), and relation. This list helps manage patient flow and queue management in real-time."
    )
    public ResponseEntity<ApiResponse<List<PatientWaitingListResponse>>> getWaitingList() {
        log.info("Fetching OPD patient waiting list");
        try {
            ApiResponse<List<PatientWaitingListResponse>> response = opdService.getWaitingList();
            log.info("Successfully fetched {} patients from waiting list",
                    response.getResponse() != null ? response.getResponse().size() : 0);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching patient waiting list: ", e);
            throw e;
        }
    }
}