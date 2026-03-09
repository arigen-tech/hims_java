package com.hims.service;

import com.hims.request.*;
import com.hims.response.*;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface InventoryService {
    ApiResponse<Page<IndentTrackingListResponse>> getIndentTrackingList(int page, int size);
    ApiResponse<Page<IndentTrackingListResponse>> searchIndentTrackingList(
            Long fromDepartmentId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    );
    ApiResponse<List<IndentDetailsResponseForIndentTracking>> getIndentDetailsForIndentTracking(Long indentMId);
    ApiResponse<Page<StoreStockLedgerReportResponse>> getStoreStockLedgerReport(int page, int size, Long hospitalId, Long itemId, String batchNo);
    ApiResponse<List<MasCommonStatusResponse>> getStatusMapForIndentTracking();

    ApiResponse<Page<ItemStockLedgerWithBatchResponse>> getStoreItems(Long sectionId,String keyword, int page, int size) ;
    ApiResponse<List<String>> getBatchesFromItemId(Long itemId);
    ApiResponse<Long> getIssueMIdFromIndentMId(Long indentMId);
    ApiResponse<Long> getReceiveMIdFromIndentMId(Long indentMId);
    ApiResponse<Long> getReturnMIdFromIndentMId(Long indentMId);
    List<DepartmentDropdownResponse> fetchIndentApplicableDepartmentsExceptCurrent();
    ApiResponse<DepartmentDropdownResponse> getCurrentDepartmentById(Long id);
    ApiResponse<MasStoreItemResponse> getItemById(Long hospitalId,Long itemId);
    ApiResponse<StoreInternalIndentResponse> saveIndent(StoreInternalIndentRequest request);
    ApiResponse<StoreInternalIndentResponse> submitIndent(StoreInternalIndentRequest request);

    ApiResponse<Page<IndentTrackingListResponse>> getAllIndentsForViewUpdateWrtDept(Long deptId, int page, int size, LocalDate fromDate, LocalDate toDate, String status);

    ApiResponse<List<IndentTrackingListResponse>> pendingForIndentApprovalWrtDept(Long deptId);
    ApiResponse<List<IndentDetailsWithAvlStock>> getIndentDetailsForIssueWithAvailableStock(Long indentMId,Long deptId);
    ApiResponse<StoreInternalIndentResponse> approveRejectIndent(StoreInternalIndentApprovalRequest request);
    ApiResponse<List<IndentTrackingListResponse>> getAllIndentsApprovedForIssueDept(Long deptId);
    ApiResponse<StoreInternalIndentResponse> approveIndentByIssueDept(IssueInternalIndentApprovalRequest request);
    ApiResponse<List<StoreInternalIndentMResponse>> getAllIndentsForIssueWrtDept(Long deptId);

    ApiResponse<List<StoreIssueMResponse>> getIssuesIndentListWrtToDept(Long toDeptId, LocalDate fromDate, LocalDate toDate);

    ApiResponse<List<IndentDetailsForIssueResponse>> getIndentDetailsWrtIndentAndDeptForIssue(Long indentMId,Long deptId);

    ApiResponse<List<PreviousIssueResponse>> getPreviousIssueInfos(Long itemId, Long currentIndentMId);

    ApiResponse<StoreInternalIndentResponse> issueIndent(StoreInternalIssueRequest request);


    ApiResponse<List<StoreIssueMResponse>> getAllIndentsForReceiving(
            Long fromDeptId,
            LocalDate fromDate,
            LocalDate toDate);


    ApiResponse<List<IndentDetailsResponseForReceiving>> getIndentDetailsWrtIndentForReceiving(Long indentMId);


     ApiResponse<StoreIndentReceiveResponse> saveReceiving(StoreIndentReceiveRequest request);

     ApiResponse<List<IndentDetailsResponseForRequestDept>> getIndentDetailsForRequestingDept(Long indentMId, Long deptId);


}
