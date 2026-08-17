package com.hims.controller;


import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.IPDPatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/ipd")
@AllArgsConstructor
@Slf4j

public class IPDPatientController {

    private final IPDPatientService ipdPatientService;

    @GetMapping("pendingAdmissionList")
    public ResponseEntity<ApiResponse<Page<IPDPatientWaitingListResponse>>> pendingAdmissionList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam Long hospitalId,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String mobileNo) {
        log.info("Request received for pending admission list. page: {}, size: {}, hospitalId: {}, patientName: {}, mobileNo: {}", page, size,
                hospitalId,
                patientName,
                mobileNo);

        ApiResponse<Page<IPDPatientWaitingListResponse>> response = ipdPatientService.pendingAdmissionList(page, size,
                hospitalId,
                        patientName,
                        mobileNo
                );
        log.info("Pending admission list fetched successfully for hospitalId: {}", hospitalId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Save IPD Patient Details",
            description = "This API is used to save IPD patient details including admission, bed allocation, NOK details, and document details.")
    @PostMapping(value = "/saveAdmissionDetails", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> saveAdmissionDetails(@Valid @ModelAttribute IpdPatientRequest request) {

        log.info("Request received to save IPD patient details for patientId: {}", request.getPatientId());

        return ipdPatientService.saveAdmissionDetails(request);
    }

    @GetMapping("getWardDetailsByDepartment")
    public ResponseEntity<ApiResponse<List<IpdWardResponse>>> getWardDetailsByDepartment(@RequestParam Long departmentId ){

        log.info("Request received to fetch ward details for departmentId: {}", departmentId);

        ApiResponse<List<IpdWardResponse>> response = ipdPatientService.getWardDetailsByDepartment(departmentId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("getRoomDetailsByWard/{wardId}")
    public ResponseEntity<ApiResponse<List<IpdRoomResponse>>> getRoomDetailsByWard(@PathVariable Long wardId ){

        log.info("Request received to fetch room details for wardId: {}", wardId);

        ApiResponse<List<IpdRoomResponse>> response = ipdPatientService.getRoomDetailsByWard(wardId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getWardDetailsByCategory/{wardCategoryId}")
    public ResponseEntity<ApiResponse<List<WardResponse>>> getWardDetailsByCategory(@PathVariable Long wardCategoryId) {

        log.info("GET /getWardByCategory/{} called", wardCategoryId);

        ApiResponse<List<WardResponse>> response = ipdPatientService.getWardDetailsByCategory(wardCategoryId);
        HttpStatus status = (response.getStatus() == 200) ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(response, status);
    }

    @GetMapping("getBedDetailsByRoom/{roomId}")
    public ResponseEntity<ApiResponse<List<BedResponse>>> getBedDetailsByRoom(@PathVariable Long roomId ){

        log.info("Request received to fetch beds for roomId: {}", roomId);

        ApiResponse<List<BedResponse>> response = ipdPatientService.getBedDetailsByRoom(roomId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("getNursingDashboardByWard/{wardId}")
    public ResponseEntity<ApiResponse<List<WardWiseDetailsResponse>>> getNursingDashboardByWard(@PathVariable Long wardId ){

        log.info("Request received to getWardWiseDetails");

        ApiResponse<List<WardWiseDetailsResponse>> response = ipdPatientService.getNursingDashboardByWard(wardId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("getTotalBedCountByWard/{wardId}")
    public ResponseEntity<ApiResponse<TotalBedCountResponse>> getTotalBedCountByWard(@PathVariable Long wardId ){

        log.info("Request received to fetch beds for departmentId: {}", wardId);

        ApiResponse<TotalBedCountResponse> response = ipdPatientService.getTotalBedCountByWard(wardId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("saveNursingMedicalAssessment")
    public ApiResponse<String> saveNursingMedicalAssessment(@RequestBody IpNursingMedicalAssessmentRequest request) {
        log.info(
                "Request received to save IP nursing medical assessment. inpatientId: {}, hospitalId: {}",
                request.getInpatientId(),
                request.getHospitalId()
        );
        return ipdPatientService.saveNursingMedicalAssessment(request);
    }

    @PutMapping("updateAdmissionInternalStatus/{inpatientId}/{internalStatusId}")
    public ApiResponse<String> updateAdmissionInternalStatus(@PathVariable Long inpatientId,@PathVariable Long internalStatusId) {

        log.info("Request received to change internal status for inpatientId: {}", inpatientId);

        return ipdPatientService.updateAdmissionInternalStatus(inpatientId,internalStatusId);
    }

    @GetMapping("getVitalsDetailsByInpatientId/{inpatientId}")
    public ResponseEntity<ApiResponse<List<IpVitalsResponse>>> getVitalsDetails(@PathVariable Long inpatientId ){

        log.info("Request received to fetch vitals details for inpatientId: {}", inpatientId);

        ApiResponse<List<IpVitalsResponse>> response = ipdPatientService.getVitalsDetails(inpatientId);

        return ResponseEntity.ok(response);
    }


    @PostMapping("saveVitalsDetails")
    public ApiResponse<String> saveVitalsDetails(@RequestBody IpVitalsRequest request) {

        log.info("Request received to save vitals details for inpatientId: {}", request.getInpatientId());

        return ipdPatientService.saveVitalsDetails(request);
    }
    @Operation(summary = "Save Intake/Output Details",
            description = """
                Saves intake and output details for an inpatient.

                Rules:
                - ioType = 'I' (Intake):
                    - intakeTypeId is required.
                    - intakeItemId is required.
                    - outputTypeId must be null.

                - ioType = 'O' (Output):
                    - outputTypeId is required.
                    - intakeTypeId must be null.
                    - intakeItemId must be null.
                """
    )
    @PostMapping("saveIntakeOutputDetails")
    public ApiResponse<String> saveIntakeOutputDetails(@Valid @RequestBody IpIntakeOutputSaveRequest request) {

        log.info("Request received to save intake/output details. inpatientId: {}, entryCount: {}", request.getInpatientId(), request.getEntries() != null ? request.getEntries().size() : 0);

        return ipdPatientService.saveIntakeOutputDetails(request);
    }

    @PostMapping("saveDailyCaseSheetEntry")
    public ApiResponse<String> saveDailyCaseSheetEntry(@Valid @RequestBody IpDailyCaseSheetEntryRequest request) {
        log.info(
                "Request received to save daily case sheet entry. inpatientId: {}, doctorId: {}",
                request.getInpatientId(),
                request.getDoctorId());

        return ipdPatientService.saveDailyCaseSheetEntry(request);
    }

    @GetMapping("getDailyCaseSheetEntry/{inpatientId}")
    public ResponseEntity<ApiResponse<List<DailyCaseSheetEntryResponse>>> getDailyCaseSheetEntry(@PathVariable Long inpatientId ){

        log.info("Request received to fetch vitals details for inpatientId: {}", inpatientId);

        ApiResponse<List<DailyCaseSheetEntryResponse>> response = ipdPatientService.getDailyCaseSheetEntry(inpatientId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("getBedDetailsByWard/{wardId}")
    public ResponseEntity<ApiResponse<List<BedDetailsByWardResponse>>> getBedDetailsByWard(@PathVariable Long wardId ){

        log.info("Request received to fetch getBedDetailsByWard: {}", wardId);

        ApiResponse<List<BedDetailsByWardResponse>> response = ipdPatientService.getBedDetailsByWard(wardId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("saveBedTransferRequest")
    public ApiResponse<String> saveBedTransferRequest(@Valid @RequestBody BedTransferRequest request) {
        log.info(
                "Request received to save Bed Transfer Request. inpatientId: {}, doctorId: {}",
                request.getInpatientId(),
                request.getDoctorId());

        return ipdPatientService.saveBedTransferRequest(request);
    }

    @GetMapping("wardPendingToTransferRequestList/{wardId}")
    public ResponseEntity<ApiResponse<List<PendingToTransferResponse>>> wardPendingToTransferRequest(  @RequestParam List<Long> wardIds ){

        log.info("Request received to fetch pending transfer requests for wardIds: {}", wardIds);
        ApiResponse<List<PendingToTransferResponse>> response = ipdPatientService.wardPendingToTransferRequest(wardIds);

        return ResponseEntity.ok(response);
    }
    /**
     * Updates a pending ward transfer request status.
     *
     * @param inpatientId   ID of the inpatient whose transfer request is being updated
     * @param transferStatus new transfer status, such as C for completed or R for rejected
     * @return API response containing the status update result
     */
    @PutMapping("wardPendingToTransferRequestStatusCompleteAndReject/{inpatientId}/{transferStatus}"
    )
    public ApiResponse<String> updateWardTransferRequestStatus(
            @PathVariable Long inpatientId,
            @PathVariable String transferStatus) {

        // Log the incoming transfer-status update request.
        log.info("Request received to update ward transfer status, " + "inpatientId: {}, transferStatus: {}",
                inpatientId,
                transferStatus
        );

        // Call the service method to complete or reject the transfer request.
        return ipdPatientService.wardPendingToTransferRequestStatusCompleteAndReject(inpatientId, transferStatus);
    }
    @PostMapping("saveInpatientBookingInvestigation")
    public ApiResponse<String> saveInpatientBookingInvestigation(@Valid @RequestBody InpatientBookingInvestigationRequest request) {
        log.info("Request received to save inpatient booking investigation. inpatientId: {}", request.getInpatientId());
        return ipdPatientService.saveInpatientBookingInvestigation(request);
    }


    @GetMapping("wardTransferList/{wardIds}")
    public ResponseEntity<ApiResponse<List<PendingToTransferResponse>>> wardTransferList(  @RequestParam  List<Long> wardIds){

        log.info("Request received to fetch pending transfer requests for wardIds: {}", wardIds);

        ApiResponse<List<PendingToTransferResponse>> response = ipdPatientService.wardTransferList(wardIds);

        return ResponseEntity.ok(response);
    }

   @PostMapping("saveIpDiagnosisEntry")
   public ApiResponse<String> saveIpDiagnosisEntry(@Valid @RequestBody IpDiagnosisEntryRequest  request) {

       log.info("Request received to saveIpDiagnosisEntry Request");

       return ipdPatientService.saveIpDiagnosisEntry(request);
   }

   @GetMapping("getIpDiagnosisEntry/{inpatientID}")
   public ResponseEntity<ApiResponse<List<IpDiagnosisEntryResponse>>> getIpDiagnosisEntry(@PathVariable Long inpatientID){

        log.info("Request received to fetch getIpDiagnosisEntry for inpatientID: {}", inpatientID);

        ApiResponse<List<IpDiagnosisEntryResponse>> response = ipdPatientService.getIpDiagnosisEntry(inpatientID);

        return ResponseEntity.ok(response);
    }

    /**
     * Fetches Intake or Output details for a given inpatient.
     *
     * @param inpatientID the unique ID of the inpatient
     * @param ioType the type of record to fetch:
     *               "I" - Intake details
     *               "O" - Output details
     * @return ResponseEntity containing the list of intake/output details
     */
    @GetMapping("getIntakeOutputDetails/{inpatientID}")
    public ResponseEntity<ApiResponse<List<IntakeOutputResponse>>> getIntakeOutputDetails(@PathVariable Long inpatientID){

        log.info("Request received to fetch saveIntakeOutputDetails for inpatientID: {}", inpatientID);

        ApiResponse<List<IntakeOutputResponse>> response = ipdPatientService.getIntakeOutputDetails(inpatientID);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/saveDischargeSummary")
    public ApiResponse<String> saveDischargeSummary(@Valid @RequestBody IpDischargeSummarySaveRequest request) {

        log.info("Request received to save discharge summary for inpatientId: {}", request.getInpatientId());

        return ipdPatientService.saveDischargeSummary(request);
    }

    @GetMapping("getPaymentStatus/{inpatientID}")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatus(@PathVariable Long inpatientID){

        log.info("Request received to fetch getPaymentStatus for inpatientID: {}", inpatientID);

        ApiResponse<PaymentStatusResponse> response = ipdPatientService.getPaymentStatus(inpatientID);

        return ResponseEntity.ok(response);
    }

    @GetMapping("getDischargeSummary/{inpatientID}")
    public ResponseEntity<ApiResponse<DischargeSummaryResponse>> getDischargeSummary(@PathVariable Long inpatientID){

        log.info("Request received to fetch getDischargeSummary for inpatientID: {}", inpatientID);

        ApiResponse<DischargeSummaryResponse> response = ipdPatientService.getDischargeSummary(inpatientID);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getIpdAdvanceCollection")
    public ResponseEntity<ApiResponse<Page<InpatientAdvanceCollectionResponse>>> getIpdAdvanceCollection(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) String admissionNo) {

        log.info("Request received to fetch IPD Advance Collection. page: {}, size: {}, patientName: {}, mobileNo: {}, admissionNo: {}",
                page, size, patientName, mobileNo, admissionNo);

        ApiResponse<Page<InpatientAdvanceCollectionResponse>> response = ipdPatientService.getIpdAdvanceCollection(
                            page,
                            size,
                            patientName,
                            mobileNo,
                            admissionNo);

            log.info("Successfully processed request for IPD Advance Collection.");
            return ResponseEntity.ok(response);


        }
    @GetMapping("/getPendingTrackingIPDBillList")
    public ResponseEntity<ApiResponse<Page<PendingTrackingIPDBillResponse>>> getPendingTrackingIPDBillList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) Long billType,
            @RequestParam(required = false) BigDecimal outStandingAmount) {

        log.info("Request received for Pending Tracking IPD Bill List. page={}, size={}, wardId={}, billType={}, outStandingAmount={}",
                page, size, wardId, billType, outStandingAmount);


            ApiResponse<Page<PendingTrackingIPDBillResponse>> response =
                    ipdPatientService.getPendingTrackingIPDBillList(
                            page,
                            size,
                            wardId,
                            billType,
                            outStandingAmount);

            log.info("Successfully fetched Pending Tracking IPD Bill List.");

            return ResponseEntity.ok(response);

    }
    @PostMapping("/saveAdvanceCollection")
    public ApiResponse<String> saveDischargeSummary(@Valid @RequestBody AdvanceCollectionRequest request) {

        log.info("Request received to save discharge summary for inpatientId: {}", request.getInpatientId());

        return ipdPatientService.saveAdvanceCollection(request);
    }


    @GetMapping("previousPaymentHistory/{billingHeaderID}")
    public ResponseEntity<ApiResponse<List<PreviousPaymentHistoryResponse>>> previousPaymentHistory(@PathVariable Long billingHeaderID){

        log.info("Request received to fetch PreviousPaymentHistory for billingHeaderID: {}", billingHeaderID);

        ApiResponse<List<PreviousPaymentHistoryResponse>> response = ipdPatientService.previousPaymentHistory(billingHeaderID);

        return ResponseEntity.ok(response);
    }

    @PostMapping("saveMedicationTreatment")
    public ApiResponse<String> saveMedicationTreatment(@Valid @RequestBody IpMedicinePrescriptionRequest request) {

        log.info("Request received to save MedicationTreatment inpatientId: {}", request.getInpatientId());

        return ipdPatientService.saveMedicationTreatment(request);
    }

    @GetMapping("getMedicationTreatmentByInpatientId/{inpatientId}")
    public ResponseEntity<ApiResponse<List<IpMedicinePrescriptionResponse>>> getMedicationTreatmentByInpatientId(@PathVariable Long inpatientId) {

        log.info("Request received to fetch MedicationTreatment for inpatientId: {}", inpatientId);

        ApiResponse<List<IpMedicinePrescriptionResponse>> response = ipdPatientService.getMedicationTreatmentByInpatientId(inpatientId);

        return ResponseEntity.ok(response);
    }
    @PostMapping("stopMedicationTreatment")
    public ApiResponse<String> stopMedicationTreatment(@Valid @RequestBody MedicinePrescriptionRequest request) {

        log.info("Request received to stop MedicationTreatment prescriptionId: {}", request.getPrescriptionId());

        return ipdPatientService.stopMedicationTreatment(request);
    }

    @PostMapping("saveMarDetails")
    public ApiResponse<String> saveMarDetails(@Valid @RequestBody List<MarDetailsRequest> request) {

        log.info("saveMarDetails API called with {} record(s)", request != null ? request.size() : 0);

        return ipdPatientService.saveMarDetails(request);
    }



    @GetMapping("/getMarAdministrationLog")
    public ResponseEntity<ApiResponse<Page<IpMarDetailsResponse>>> getMarAdministrationLog(
            @RequestParam Long inpatientId,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        log.info("Request received to fetch MAR Administration Log. inpatientId: {}, itemId: {}, page: {}, size: {}",
                inpatientId, itemId, page, size);

        ApiResponse<Page<IpMarDetailsResponse>> response = ipdPatientService.getMarAdministrationLog(inpatientId, itemId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getMarMedicineList")
    public ResponseEntity<ApiResponse<List<MarMedicineResponse>>> getMarMedicineList(@RequestParam Long inpatientId) {

        log.info("Request received to fetch unique medicines in MAR log for inpatientId: {}", inpatientId);

        ApiResponse<List<MarMedicineResponse>> response = ipdPatientService.getMarMedicineList(inpatientId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("saveInpatientProcedure")
    public ApiResponse<String> saveInpatientProcedure(@Valid @RequestBody InpatientProcedureRequest request) {
        log.info("saveInpatientProcedure API called. inpatientId={}, procedureId={}, procedureDatetime={}, performedBy={}",
                request != null ? request.getInpatientId() : null,
                request != null ? request.getProcedureId() : null,
                request != null ? request.getProcedureDatetime() : null,
                request != null ? request.getPerformedBy() : null);
        return ipdPatientService.saveInpatientProcedure(request);
    }


    @GetMapping("/getProcedureByInpatientId/{inpatientId}")
    public ResponseEntity<ApiResponse<List<IpProcedureTxnResponse>>> getIpProcedureTxnByInpatientId(@PathVariable Long inpatientId) {
        log.info("Request received to fetch IpProcedureTxn for inpatientId: {}", inpatientId);
        ApiResponse<List<IpProcedureTxnResponse>> response = ipdPatientService.getIpProcedureTxnByInpatientId(inpatientId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("saveProcedureConsumableTemplate")
    public ApiResponse<String> saveProcedureConsumableTemplate(@Valid @RequestBody ProcedureConsumableTemplateSaveRequest request) {

        log.info("saveProcedureConsumableTemplate API called. procedureId={}, templateCode={}, templateName={}",
               request.getProcedureId(), request.getTemplateCode(), request.getTemplateName());

        return ipdPatientService.saveProcedureConsumableTemplate(request);
    }
    @GetMapping("getProcedureConsumableTemplate")
    public ResponseEntity<ApiResponse<Page<ProcedureConsumableTemplateHeaderResponse>>> getTemplate(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Request received to fetch ProcedureConsumableTemplateHeader. " + "search={}, page={}, size={}", search, page, size);

        ApiResponse<Page<ProcedureConsumableTemplateHeaderResponse>> response = ipdPatientService.getTemplates(search, page, size);

        return ResponseEntity.ok(response);
    }
    @GetMapping("getProcedureConsumableTemplateDetails/{templateId}")
    public ResponseEntity<ApiResponse<List<ProcedureConsumableTemplateDetailsResponse>>>
    getProcedureConsumableTemplateDetails(@PathVariable Long templateId) {

        log.info("Request received to fetch Procedure Consumable Template Details for templateId: {}", templateId);

        ApiResponse<List<ProcedureConsumableTemplateDetailsResponse>> response =
                ipdPatientService.getProcedureConsumableTemplateDetails(templateId);

        return ResponseEntity.ok(response);
    }


    @PostMapping("saveNursingCareProcedure")
    public ApiResponse<String> saveNursingCareProcedure(@Valid @RequestBody List<ConsumableEntryRequest> request) {

        log.info("saveNursingCareProcedure API called with {} record(s)", request != null ? request.size() : 0);

        return ipdPatientService.saveNursingCareProcedure(request);
    }

    @GetMapping("getNursingCareProcedure/{inpatientId}")
    public ResponseEntity<ApiResponse<List<NursingCareProcedure>>> getNursingCareProcedure(@PathVariable Long inpatientId) {

        log.info("Request received to fetch Nursing Care Procedure details for inpatientId: {}", inpatientId);

        ApiResponse<List<NursingCareProcedure>> response = ipdPatientService.getNursingCareProcedure(inpatientId);

        return ResponseEntity.ok(response);
    }
    @GetMapping("getNursingMedicalAssessment/{inpatientId}")
    public ResponseEntity<ApiResponse<IpNursingMedicalAssessmentResponse>> getNursingMedicalAssessment(@PathVariable Long inpatientId ){

        log.info("Request received to fetch nursing medical assessment details for inpatientId: {}", inpatientId);

        ApiResponse<IpNursingMedicalAssessmentResponse> response = ipdPatientService.getNursingMedicalAssessment(inpatientId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("getAdmissionDetailsByInpatient/{inpatientId}")
    public ResponseEntity<ApiResponse<InpatientAdmissionDetailsResponse>> getAdmissionDetailsByInpatient(@PathVariable Long inpatientId ){

        ApiResponse<InpatientAdmissionDetailsResponse> response = ipdPatientService.getAdmissionDetailsByInpatient(inpatientId);

        return ResponseEntity.ok(response);
    }





}
