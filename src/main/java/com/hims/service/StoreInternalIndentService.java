package com.hims.service;

import com.hims.entity.MasDepartment;
import com.hims.request.*;
import com.hims.response.*;

import java.time.LocalDate;
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

    public ApiResponse<List<StoreInternalIndentResponse>> getAllIndentsForReceiving(Long fromDeptId, LocalDate fromDate, LocalDate toDate);

    public ApiResponse<StoreIndentReceiveResponse> saveReceiving(StoreIndentReceiveRequest request) ;

    ApiResponse<List<StoreIssueMResponse>> getIssuesForReceiving( LocalDate fromDate, LocalDate toDate);
}
