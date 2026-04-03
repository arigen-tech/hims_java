package com.hims.controller;


import com.hims.request.InvestigationValidationRequest;
import com.hims.request.ResultUpdateRequest;
import com.hims.request.ResultValidationUpdateRequest;
import com.hims.request.SampleCollectionRequest;
import com.hims.response.ApiResponse;
import com.hims.service.LabService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@Tag(name = "Lab", description = "Lab related operations")
@RequiredArgsConstructor
@RequestMapping("/lab")
public class LabController {

    private final LabService labService;

    /**
     * Fetch Pending Sample Collection Headers

     * This API retrieves a list of pending sample collection records (headers)
     * for a given hospital. It supports optional filtering based on patient details.

     * - hospitalId (mandatory): Used to fetch records specific to a hospital.

     * - patientName (optional): If provided, filters results by patient name
     *   (supports partial search).

     * - patientMobileNumber (optional): If provided, filters results by
     *   patient mobile number.

     * If no optional filters are provided, all pending sample collection
     * headers for the given hospital will be returned.
     *
     * @param hospitalId ID of the hospital (required)
     * @param patientName Patient name for search (optional)
     * @param patientMobileNumber Patient mobile number for search (optional)
     * @return List of pending sample collection headers
     */
    @GetMapping("/pendingSampleForCollection/headers")
    public ResponseEntity<?> getPendingSampleHeadersForCollection(@RequestParam Long  hospitalId,
                                                                  @RequestParam(required = false) String patientName,
                                                                  @RequestParam(required = false) String patientMobileNumber,
                                                                  @RequestParam (defaultValue = "0") int page,
                                                                  @RequestParam (defaultValue = "5") int size) {


        return ResponseEntity.ok(labService.getPendingSampleHeadersForCollection(hospitalId, patientName, patientMobileNumber, page, size));
    }

    /**
     * Fetch Pending Sample Collection Details
     *
     * This API retrieves detailed information of pending sample collection
     * for a specific order.
     *
     * - orderHdId (mandatory): Unique identifier of the order header.
     *
     * The response includes all associated sample details that are pending
     * for collection under the given order.
     *
     * @param orderHdId ID of the order header (required)
     * @return List of pending sample collection details for the given order
     */
    @GetMapping("/pendingSampleForCollection/details")
    public ResponseEntity<?> getPendingSampleDetailsWrtHeader(
            @RequestParam Long orderHdId) {

        return ResponseEntity.ok(
                labService.getPendingSampleDetailsForCollection(orderHdId)
        );
    }

    /**
     * Save Pending Samples for Collection
     * This API is used to save or update sample collection details
     * for pending laboratory samples under a specific department.
     * - departmentId (mandatory): Identifies the department where
     *   the sample collection is being performed.
     * - request (mandatory): Contains the list of sample collection
     *   details such as order information, sample status, collection
     *   data, and other relevant fields.
     * The API processes the provided data and updates the status of
     * pending samples as collected (or partially collected) based on
     * the request payload.
     * @param departmentId ID of the department (required)
     * @param request Request body containing sample collection details
     * @return Status of the save operation along with processed data
     */
    @PostMapping("/savePendingSamplesForCollection")
    public ResponseEntity<?> savePendingSamplesForCollection(
            @RequestParam Long departmentId,
            @RequestBody SampleCollectionRequest request) {

        return ResponseEntity.ok(
                labService.savePendingSamplesForCollection(departmentId, request)
        );
    }


    /**
     * Fetch Pending Sample Validation Headers

     * This API retrieves a list of sample collection headers that are pending
     * for validation for a specific hospital. It supports optional filtering based on patient details
     * and server-side pagination.

     * - hospitalId (mandatory): Used to fetch pending validation records
     *   associated with the given hospital.

     * - patientName (optional): If provided, filters results by patient name
     *   (supports partial search).

     * - patientMobileNumber (optional): If provided, filters results by
     *   patient mobile number.

     * - page (optional): Page number for pagination (default: 0)
     * - size (optional): Number of records per page (default: 5)

     * If no optional filters are provided, all pending sample validation
     * headers for the given hospital will be returned.

     * The response typically includes summary/header-level information such as
     * patient details, collection info, and identifiers required to fetch
     * corresponding detail records.
     *
     * @param hospitalId ID of the hospital (required)
     * @param patientName Patient name for search (optional)
     * @param patientMobileNumber Patient mobile number for search (optional)
     * @param page Page number for pagination (optional, default: 0)
     * @param size Number of records per page (optional, default: 5)
     * @return Paginated list of pending sample validation headers
     */
    @GetMapping("/pendingSampleForValidation/headers")
    public ResponseEntity<?> getSampleHeaderForValidation(
            @RequestParam Long hospitalId,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String patientMobileNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(
                labService.getSampleHeaderForValidation(hospitalId, patientName, patientMobileNumber, page, size)
        );
    }

    /**
     * Fetch Pending Sample Validation Details

     * This API retrieves detailed sample information for validation based on
     * a specific sample collection header.

     * - sampleCollectionHeaderId (mandatory): Unique identifier of the
     *   sample collection header.

     * The response includes test/sample-level details that are pending
     * validation under the given header.
     *
     * @param sampleCollectionHeaderId ID of the sample collection header (required)
     * @return List of pending sample validation details for the given header
     */
    @GetMapping("/pendingSampleForValidation/details")
    public ResponseEntity<?> getSampleDetailsForValidation(
            @RequestParam Long sampleCollectionHeaderId) {

        return ResponseEntity.ok(
                labService.getSampleDetailsForValidationWrtHeader(sampleCollectionHeaderId)
        );
    }

    /**
     * Validate Pending Sample Investigations
     *
     * This API is used to validate laboratory investigations/tests for samples
     * that have been collected and are ready for validation. It processes each
     * validation request and updates the status of investigations, order details,
     * and order headers accordingly.
     *
     * - requests (mandatory): A list of investigation validation requests containing:
     *   - sampleHeaderId: ID of the sample collection header
     *   - detailId: ID of the sample collection detail to validate
     *   - accepted: Boolean flag indicating if the sample/investigation is accepted (true) or rejected (false)
     *   - reason: (if rejected) Reason for rejection
     *
     * Processing Steps:
     * 1. Fetch the current user performing validation
     * 2. Retrieve the sample collection header and associated order
     * 3. For each investigation request:
     *    - Update the turn-around-time (TAT) record with validation details
     *    - Update the sample collection detail status
     *    - Update the corresponding order detail status
     * 4. Determine and update the header validation status based on all details
     * 5. Update the order header status based on all order details
     *
     * Status Changes:
     * - Sample Collection Detail Status:
     *   - If accepted (yes): 'Y' (Validated)
     *   - If rejected (no): 'R' (Rejected)
     *
     * - Order Detail (DgOrderDt) Status:
     *   - If accepted: 'Y' (Validated) with VALIDATED tracking status
     *   - If rejected: 'N' (Not Validated/Rejected) with REJECTED tracking status
     *
     * - Sample Collection Header Status:
     *   - All Accepted: 'Y' (All Validated)
     *   - All Rejected: 'R' (All Rejected)
     *   - Partial Acceptance: 'Y' (Partially Validated)
     *
     * - Order Header (DgOrderHd) Status:
     *   - All Rejected: 'N'
     *   - All Accepted: 'Y'
     *   - Partial: 'P' (Pending/Partial)
     *
     * - Lab Turn Around Time (TAT):
     *   - Sets isReject flag based on acceptance
     *   - Records validator name and timestamp
     *
     * @param requests List of investigation validation requests (required)
     *                  Each request contains sampleHeaderId, detailId, accepted flag, and optional rejection reason
     * @return ApiResponse<String> with success message "Investigation validated successfully" on success,
     *         or error message on failure
     */
    @PostMapping("/sampleValidate")
    public ResponseEntity<ApiResponse<String>> validateInvestigations(
            @RequestBody List<InvestigationValidationRequest> requests) {

        return   ResponseEntity.ok(labService.validateInvestigations(requests));
    }


    /**
     * Fetch Pending Sample Collection Headers for Result Entry
     *
     * This API retrieves a list of sample collection headers that are pending
     * for result entry for a specific hospital. These are samples that have been
     * validated and are ready for laboratory result entry/reporting. It supports
     * optional filtering based on patient details and server-side pagination.
     *
     * Query Filters:
     * - Result Entry Status: 'n' (not yet entered)
     * - Validation Status: 'y' (validated/approved samples)
     * - Hospital ID: Filters records for the specified hospital
     *
     * - hospitalId (mandatory): ID of the hospital to filter records
     * - patientName (optional): If provided, filters results by patient name
     *   (supports partial search).
     * - patientMobileNumber (optional): If provided, filters results by
     *   patient mobile number.
     * - page (optional): Page number for pagination (default: 0)
     * - size (optional): Number of records per page (default: 5)
     *
     * The response includes paginated sample collection header information with patient details,
     * collection date/time, and other relevant identifiers needed to proceed with result entry.
     *
     * @param hospitalId ID of the hospital (required)
     * @param patientName Patient name for search (optional)
     * @param patientMobileNumber Patient mobile number for search (optional)
     * @param page Page number for pagination (optional, default: 0)
     * @param size Number of records per page (optional, default: 5)
     * @return Paginated list of pending sample collection headers ready for result entry
     */
    @GetMapping("/pendingSampleForResultEntry/headers")
    public  ResponseEntity<?> getSampleHeaderForResultEntry(
            @RequestParam Long hospitalId,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String patientMobileNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(labService.getSampleHeaderForResultEntry(hospitalId, patientName, patientMobileNumber, page, size));
    }

    /**
     * Fetch Investigations for Result Entry
     *
     * This API retrieves all investigations/tests associated with a specific
     * sample collection header that are pending for result entry.
     *
     * Data Retrieved:
     * - Investigation ID and name
     * - Sample type/description
     * - Unit of Measurement (UOM)
     * - Normal reference range (min-max values)
     * - Investigation type
     *
     * - sampleCollectionHeaderId (mandatory): Unique identifier of the sample
     *   collection header to fetch investigations for
     *
     * This API is typically called after selecting a sample header, to display
     * all tests that need result entry for that sample.
     *
     * @param sampleCollectionHeaderId ID of the sample collection header (required)
     * @return List of investigations/tests for the selected sample collection
     */
    @GetMapping("/investigationsForResult/details")
    public ResponseEntity<?> getInvestigationsForResultEntry(@RequestParam Long sampleCollectionHeaderId) {
        return  ResponseEntity.ok(labService.getInvestigationsForResultEntry(sampleCollectionHeaderId));

    }

    /**
     * Fetch Sub-Investigations for Result Entry with Gender and Age Specific Values
     *
     * This API retrieves sub-investigations (component tests) for a given investigation,
     * filtered by patient gender and age to get appropriate reference ranges and
     * expected values.
     *
     * Data Retrieved:
     * - Sub-Investigation ID and name
     * - Expected value/reference range based on gender and age
     * - Comparison type (fixed values vs. normal range)
     * - Unit of Measurement (UOM)
     *
     * Processing Logic:
     * 1. Extract age in years from the provided age string (format: "nnY")
     * 2. Query sub-investigations by investigation ID and status = 'y' (active)
     * 3. Join with normal value table for gender and age-specific ranges:
     *    - Filter by patient gender (sex code: M/F/O)
     *    - Filter by age falling within the 'fromAge' to 'toAge' range
     * 4. Return comparison type and appropriate reference values
     *
     * - investigationId (mandatory): ID of the parent investigation
     * - genderCode (mandatory): Patient gender code (M=Male, F=Female, O=Other)
     * - age (mandatory): Patient age in format "nnnY" (e.g., "25Y")
     *
     * This API provides gender and age-specific reference ranges needed for
     * accurate result validation and interpretation.
     *
     * @param investigationId ID of the investigation to fetch sub-tests for (required)
     * @param genderCode Patient gender code - M/F/O (required)
     * @param age Patient age in format "nnY" (required)
     * @return List of sub-investigations with age and gender-specific reference ranges
     */
    @GetMapping("/subInvestigationsForResult/details")
    public ResponseEntity<?> getSubInvestigationsForResultEntry(@RequestParam Long investigationId,@RequestParam String genderCode,@RequestParam String age) {
        return ResponseEntity.ok(labService.getSubInvestigationsForResultEntry(investigationId,genderCode,age));
    }

    /**
     * Fetch Fixed Values Dropdown for Sub-Investigation
     *
     * This API retrieves a list of predefined/fixed values for a specific
     * sub-investigation. These fixed values are used when the result type for
     * a sub-investigation is "fixed value" (categorical/coded values) rather
     * than numeric ranges.
     *
     * Data Retrieved:
     * - Fixed value ID
     * - Fixed value description/text (e.g., "Positive", "Negative", "Normal", etc.)
     *
     * - subInvestigationId (mandatory): ID of the sub-investigation to fetch
     *   fixed values for
     *
     * Common Use Case:
     * When a laboratory test result is categorical (e.g., Blood Type, Culture Result,
     * Presence/Absence of pathogen), this API provides the available fixed value
     * options to display in a dropdown for result entry.
     *
     * @param subInvestigationId ID of the sub-investigation (required)
     * @return List of fixed values available for this sub-investigation
     */
    @GetMapping("/fixedValues/dropdown")
    public ResponseEntity<?> getFixedValuesDropdown(@RequestParam Long subInvestigationId) {
        return  ResponseEntity.ok(labService.getFixedValuesResultDropdown(subInvestigationId));
    }

    /**
     * Fetch Pending Sample Headers for Result Validation
     *
     * This API retrieves a list of sample collection headers that have laboratory
     * results entered but are pending validation/approval for a specific hospital.
     * It supports optional filtering based on patient details and server-side pagination.
     *
     * Query Filters:
     * - Result Status: 'n' (results entered but not validated)
     * - Hospital ID: Filters records for the specified hospital
     * - Patient Name: Optional partial search filter for patient name
     * - Patient Mobile Number: Optional exact match filter for patient mobile number
     *
     * Data Retrieved:
     * - Result entry header ID and timestamp details
     * - Patient information (name, gender, age, phone number)
     * - Main and sub charge code details (test categories)
     * - Referring doctor information
     * - Result entered by user details
     *
     * The response includes paginated sample collection header information with patient and
     * test details needed to proceed with result validation workflow.
     *
     * @param hospitalId ID of the hospital (required)
     * @param patientName Patient name for search (optional, supports partial match)
     * @param patientMobileNumber Patient mobile number for search (optional, exact match)
     * @param page Page number for pagination (optional, default: 0)
     * @param size Number of records per page (optional, default: 5)
     * @return Paginated list of pending sample headers ready for result validation
     */
    @GetMapping("/pendingSampleForResultValidation/headers")
    public  ResponseEntity<?> getSampleHeaderForResultValidation(@RequestParam Long hospitalId,
                                                                  @RequestParam(required = false) String patientName,
                                                                  @RequestParam(required = false) String patientMobileNumber,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(labService.getSampleHeaderForResultValidation(hospitalId, patientName, patientMobileNumber, page, size));
    }

    /**
     * Fetch Investigations for Result Validation
     *
     * This API retrieves all investigations/tests associated with a specific
     * result entry header that have results entered but are pending validation.
     *
     * Query Filters:
     * - Result Entry Header ID: Unique identifier of the result entry header
     * - Validation Status: 'n' (not yet validated)
     *
     * Data Retrieved:
     * - Result entry detail ID
     * - Investigation ID and name
     * - Unit of Measurement (UOM)
     * - Sample description/type
     * - Entered result value
     * - Normal reference range
     * - Generated sample ID
     * - Result type (numeric/fixed values)
     * - Remarks/comments
     *
     * This API is typically called after selecting a result entry header to display
     * all tests that need validation approval for that sample.
     *
     * @param resultEntryHeaderId ID of the result entry header (required)
     * @return List of investigations/tests pending result validation
     */
    @GetMapping("/investigationsForResultValidation/details")
    public ResponseEntity<?> getInvestigationsForResultValidation(@RequestParam Long resultEntryHeaderId) {
        return ResponseEntity.ok(labService.getInvestigationsForResultValidation(resultEntryHeaderId));
    }

    /**
     * Fetch Sub-Investigations for Result Validation
     *
     * This API retrieves sub-investigations (component tests) for a specific
     * result entry detail and investigation that are pending validation.
     *
     * Query Filters:
     * - Result Entry Detail ID: Unique identifier of the result entry detail
     * - Investigation ID: Parent investigation identifier
     * - Validation Status: 'n' (not yet validated)
     * - Sub-investigation exists (not null)
     *
     * Data Retrieved:
     * - Sub-investigation ID and name
     * - Normal reference range/value
     * - Comparison type (fixed values vs. normal range)
     * - Unit of Measurement (UOM)
     * - Result type (numeric/categorical)
     * - Fixed value ID (if applicable)
     *
     * This API provides detailed sub-investigation information needed for
     * result validation and approval workflow.
     *
     * @param resultEntryDetailId ID of the result entry detail (required)
     * @param investigationId ID of the parent investigation (required)
     * @return List of sub-investigations pending result validation
     */
    @GetMapping("/subInvestigationsForResultValidation/details")
    public ResponseEntity<?> getSubInvestigationsForResultValidation(@RequestParam Long resultEntryDetailId,@RequestParam Long investigationId) {
        return ResponseEntity.ok(labService.getSubInvestigationsForResultValidation(resultEntryDetailId,investigationId));
    }

    /**
     * Validate and Update Laboratory Results
     *
     * This API is used to validate and update laboratory results that have been
     * entered. It processes the validation request and updates the result status
     * accordingly in the system.
     *
     * - request (mandatory): Contains the result validation update details including:
     *   - Result entry details ID
     *   - Validation status
     *   - Validator information
     *   - Timestamp and remarks
     *
     * @param request Request body containing result validation and update details (required)
     * @return ApiResponse<String> with success message on successful validation and update,
     *         or error message on failure
     */
    @PutMapping("/resultValidate")
    public ApiResponse<String> updateAndValidateResult(@RequestBody ResultValidationUpdateRequest request) {
        return labService.updateAndValidateResult(request);
    }

    /**
     * Fetch Pending Sample Headers for Result Update
     *
     * This API retrieves a list of sample collection headers that have validated results
     * but are pending update/modification for a specific hospital. It supports pagination
     * and optional filtering based on patient details.
     *
     * Query Filters:
     * - Result Update Status: Pending for update/modification
     * - Validation Status: 'y' (validated/approved results)
     * - Hospital ID: Filters records for the specified hospital
     *
     * - hospitalId (mandatory): ID of the hospital to filter records
     * - patientName (optional): If provided, filters results by patient name
     *   (supports partial search)
     * - patientMobileNo (optional): If provided, filters results by patient
     *   mobile number
     * - page (optional): Page number for pagination (default: 0)
     * - size (optional): Number of records per page (default: 5)
     *
     * The response includes paginated sample collection header information with patient details,
     * test information, and other relevant identifiers needed to proceed with result update.
     *
     * @param hospitalId ID of the hospital (required)
     * @param patientName Patient name for search (optional)
     * @param patientMobileNumber Patient mobile number for search (optional)
     * @param page Page number for pagination (optional, default: 0)
     * @param size Number of records per page (optional, default: 5)
     * @return Paginated list of pending sample headers ready for result update
     */
    @GetMapping("/resultUpdate/headers")
    public ResponseEntity<?> getSampleHeaderForResultUpdate(
            @RequestParam Long hospitalId,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String patientMobileNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {

        return ResponseEntity.ok(
                labService.getResultHeaderForUpdate(
                        hospitalId,
                        patientName,
                        patientMobileNumber,
                        page,
                        size
                )
        );
    }

    /**
     * Fetch Investigations for Result Update
     *
     * This API retrieves all investigations/tests associated with a specific
     * order header that are pending for result update/modification.
     *
     * Query Filters:
     * - Order Header ID: Unique identifier of the order header
     * - Update Status: Records pending for result update
     *
     * Data Retrieved:
     * - Investigation ID and name
     * - Result entry detail information
     * - Current entered result values
     * - Unit of Measurement (UOM)
     * - Normal reference range
     * - Sample description/type
     *
     * This API is typically called after selecting a sample header from the result update
     * headers list, to display all tests whose results need to be updated.
     *
     * @param orderHdId ID of the order header (required)
     * @return List of investigations/tests pending result update
     */
    @GetMapping("/investigationsForResultUpdate/details")
    public  ResponseEntity<?> getInvestigationsForResultUpdate(@RequestParam Long orderHdId) {
        return ResponseEntity.ok(labService.getInvestigationsForResultUpdate(orderHdId));
    }

    /**
     * Fetch Sub-Investigations for Result Update
     *
     * This API retrieves sub-investigations (component tests) for a specific
     * result entry detail and investigation that are pending update/modification.
     *
     * Query Filters:
     * - Result Entry Detail ID: Unique identifier of the result entry detail
     * - Investigation ID: Parent investigation identifier
     * - Update Status: Records pending for result update
     *
     * Data Retrieved:
     * - Sub-investigation ID and name
     * - Current entered result value
     * - Normal reference range/value
     * - Comparison type (fixed values vs. normal range)
     * - Unit of Measurement (UOM)
     * - Result type (numeric/categorical)
     * - Fixed value ID (if applicable)
     *
     * This API provides detailed sub-investigation information needed for
     * result update and modification workflow.
     *
     * @param resultEntryDetailId ID of the result entry detail (required)
     * @param investigationId ID of the parent investigation (required)
     * @return List of sub-investigations pending result update
     */
    @GetMapping("/subInvestigationsForResultUpdate/details")
    public  ResponseEntity<?> getSubInvestigationsForResultUpdate(@RequestParam Long resultEntryDetailId,Long investigationId) {
        return ResponseEntity.ok(labService.getSubInvestigationsForResultUpdate(resultEntryDetailId,investigationId));
    }

    /**
     * Update Laboratory Results
     *
     * This API is used to update laboratory results that have been previously entered
     * and validated. It allows modification of result values and related information.
     *
     * - request (mandatory): Contains the result update details including:
     *   - Result entry detail ID
     *   - Updated result values
     *   - Sub-investigation information
     *   - Remarks/comments
     *   - Updated by user details
     *   - Update timestamp
     *
     * Processing:
     * 1. Validate the update request data
     * 2. Fetch the result entry detail record
     * 3. Update the result values for investigations and sub-investigations
     * 4. Update the modification timestamp and user information
     * 5. Persist changes to the database
     *
     * @param request Request body containing result update details (required)
     * @return ApiResponse<String> with success message on successful update,
     *         or error message on failure
     */
    @PutMapping("/updateResult")
    public ApiResponse<String> updateResult(@RequestBody ResultUpdateRequest request) {
        return labService.updateResult(request);
    }



    /* *************************************  Report Section ************************************************** */


    @GetMapping("/investigationsReport/all")
    public ResponseEntity<?> searchLabReports(
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(
                labService.getAllInvestigationsReport(
                        mobileNo,
                        patientName,
                        fromDate,
                        toDate,
                        page,
                        size
                )
        );
    }

    @GetMapping("/lab-tat/details")
    public ResponseEntity<?> getAllLabReports(
            @RequestParam(required = false) Long investigationId,
            @RequestParam(required = false) Long subChargeCodeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(
                labService.getDetailedTatReports(
                        investigationId, subChargeCodeId, fromDate, toDate, page, size
                )
        );
    }

    @GetMapping("/lab-tat/summary")
    public ResponseEntity<?> getTatSummaryLabReports(
            @RequestParam(required = false) Long investigationId,
            @RequestParam(required = false) Long subchargeCodeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(
                labService.getSummaryTatReports(
                        investigationId, subchargeCodeId, fromDate, toDate, page, size
                )
        );
    }

    @GetMapping("/AmendAudit/result")
    public ResponseEntity<?> getAmendAuditReports(
            @RequestParam(required = false) String phnNum,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) Long investigationId,
            @RequestParam(required = false) Long subChargeCodeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(
                labService.getAmendAuditReports(
                        phnNum, patientName, investigationId,
                        subChargeCodeId, fromDate, toDate, page, size
                )
        );
    }

    @GetMapping("/orderTrackingReport")
    public ResponseEntity<?> getOrderTrackingReport(
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(
                labService.getOrderTrackingReports(
                        patientName,
                        mobileNo,
                        fromDate,
                        toDate,
                        page,
                        size
                )
        );
    }


    @GetMapping("/incompleteInvestigation/report")
    public ResponseEntity<?> getIncompleteInvestigationsReport(
            @RequestParam(required = false) Long subChargeCodeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(
                labService.getIncompleteInvestigationReports(
                        subChargeCodeId, fromDate, toDate, page, size
                )
        );
    }

    @GetMapping("/rejectedInvestigation/report")
    public ResponseEntity<?> getRejectInvestigationReport(
            @RequestParam(required = false) Long subChargeCodeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(
                labService.getSampleRejectionReport(
                        subChargeCodeId, fromDate, toDate, page, size
                )
        );
    }

}
