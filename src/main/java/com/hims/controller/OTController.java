package com.hims.controller;

import com.hims.request.OpdOpthDetailsRequest;
import com.hims.request.OtRequest;
import com.hims.request.OtTeamMemberRequest;
import com.hims.request.OtBookingRescheduleRequest;
import com.hims.request.OtBookingCancelRequest;
import com.hims.response.ActiveAdmissionOtResponse;
import com.hims.response.ActiveAdmissionResponse;
import com.hims.response.ApiResponse;
import com.hims.response.PendingForOtResponse;
import com.hims.response.OtBookingHistoryResponse;
import com.hims.response.OtTeamMemberResponse;
import com.hims.response.OtDashboardResponse;
import com.hims.response.OtDepartmentResponse;
import com.hims.service.OtService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@RestController
@Validated
@Tag(name = "OTController")
@RequestMapping("/otController")
@Slf4j
@AllArgsConstructor
public class OTController {

    private final OtService otService;


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
            @RequestParam @NotBlank String status) {
        return otService.updateOtPatientTransaction(otBookingId, status);
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

}
