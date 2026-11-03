package com.hims.service;

import com.hims.request.OtBookingRequestHdDto;
import com.hims.request.OtRequest;
import com.hims.response.ActiveAdmissionOtResponse;
import com.hims.response.ApiResponse;
import com.hims.response.PendingForOtResponse;
import com.hims.response.OtBookingHistoryResponse;
import com.hims.request.OtTeamMemberRequest;
import com.hims.response.OtTeamMemberResponse;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

public interface OtService {

    ApiResponse<String> createOrUpdateOtBookingHeader(OtBookingRequestHdDto otBookingRequestDto);

    ApiResponse<Page<ActiveAdmissionOtResponse>> activeAdmissionList(int page, int size, String patientName, String mobileNo, String admissionNo, Long wardId);

    ApiResponse<String> saveOtRequest(@Valid OtRequest request);

    ApiResponse<Page<PendingForOtResponse>> pendingForReviewOt(int page, int size, String patientName, String mobileNo, String patientType);

    ApiResponse<Page<OtBookingHistoryResponse>> getOtBookingHistory(String patient, Long statusId, int page, int size);

    ApiResponse<String> saveAcceptAndReject(Long otBookingRequestId, String flag, String remark);

    ApiResponse<OtTeamMemberResponse> saveOtTeamMember(OtTeamMemberRequest request);

    ApiResponse<OtTeamMemberResponse> getOtTeamMembers(Long otBookingId);
}
