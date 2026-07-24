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












}
