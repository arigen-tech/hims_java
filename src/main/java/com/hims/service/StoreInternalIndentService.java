package com.hims.service;

import com.hims.entity.MasDepartment;
import com.hims.request.IssueInternalIndentApprovalRequest;
import com.hims.request.StoreInternalIndentApprovalRequest;
import com.hims.request.StoreInternalIndentRequest;
import com.hims.request.StoreInternalIssueRequest;
import com.hims.response.ApiResponse;
import com.hims.response.PreviousIssueResponse;
import com.hims.response.ROLItemResponse;
import com.hims.response.StoreInternalIndentResponse;

import java.util.List;

public interface StoreInternalIndentService {
    // Save (draft) — backend sets status "S"
    ApiResponse<StoreInternalIndentResponse> saveIndent(StoreInternalIndentRequest request);

    // Submit — backend sets status "Y"
    public ApiResponse<StoreInternalIndentResponse> submitIndent(StoreInternalIndentRequest request);

    ApiResponse<StoreInternalIndentResponse> getIndentById(Long indentMId);

    ApiResponse<List<StoreInternalIndentResponse>> listIndentsByCurrentDept();


    // Import endpoints
    ApiResponse<StoreInternalIndentResponse> createIndentFromROL(StoreInternalIndentRequest baseRequest);
    ApiResponse<StoreInternalIndentResponse> createIndentFromPrevious(Long previousIndentMId);

    public List<MasDepartment> getOtherFixedDepartmentsForCurrentUser();

    ApiResponse<List<ROLItemResponse>> getROLItems();

    ApiResponse<StoreInternalIndentResponse> approveRejectIndent(StoreInternalIndentApprovalRequest request);
    public ApiResponse<List<StoreInternalIndentResponse>> getAllIndentsForPending(Long deptId);

    public ApiResponse<List<StoreInternalIndentResponse>> getAllIndentsForApproved(Long deptId);

    ApiResponse<StoreInternalIndentResponse> submitApprovedIndent(IssueInternalIndentApprovalRequest request);

    public ApiResponse<List<StoreInternalIndentResponse>> getAllIndentsForIssueDepartment(Long deptId);

    public ApiResponse<StoreInternalIndentResponse> issueIndent(StoreInternalIssueRequest request);

    public ApiResponse<List<PreviousIssueResponse>> getPreviousIssues(Long itemId, Long currentIndentMId);

}
