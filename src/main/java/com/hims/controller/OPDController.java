package com.hims.controller;

import com.hims.response.ApiResponse;
import com.hims.response.OpdPreConsultationResponse;
import com.hims.response.PatientWaitingListResponse;
import com.hims.service.OPDService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * Retrieves a list of pending pre-consultations for the current hospital.
     *
     * This endpoint fetches all patients whose pre-consultation status is pending
     * and have not yet been marked as completed. Used by OPD staff to manage
     * the queue of patients awaiting preliminary assessment.
     *
     * @return ResponseEntity containing ApiResponse with list of OpdPreConsultationResponse
     */
    @GetMapping("/getPendingPreConsultations")
    @Operation(
            summary = "Get Pending Pre-Consultations",
            description = "Retrieves all pending pre-consultations for patients in the current hospital. " +
                    "This includes patient details, doctor information, appointment timing, and token numbers."
    )
    public ResponseEntity<ApiResponse<List<OpdPreConsultationResponse>>> getPendingPreConsultations() {
        log.info("Fetching pending pre-consultations");
        try {
            ApiResponse<List<OpdPreConsultationResponse>> response = opdService.getPendingPreConsultations();
            log.info("Successfully fetched {} pending pre-consultations",
                    response.getResponse() != null ? response.getResponse().size() : 0);
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
            description = "Retrieves the current waiting list of patients for OPD consultation. " +
                    "Lists patients in the order they are waiting with their token numbers and basic information."
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