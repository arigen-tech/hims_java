package com.hims.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.InventoryService;
import com.hims.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@RestController
@Tag(name = "Inventory Controller for all common inventory related service",description = "All the apis are for common inventory will be here ")
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/indent/tracking")
    public ResponseEntity<?> getIndentTracking(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(inventoryService.getIndentTrackingList(page, size));
    }

    @GetMapping("/indent/tracking/search")
    public ResponseEntity<?> searchIndentTrackingList(
            @RequestParam(required = false) Long fromDepartmentId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {

        return ResponseEntity.ok(
                inventoryService.searchIndentTrackingList(
                        fromDepartmentId,
                        fromDate,
                        toDate,
                        page,
                        size
                )
        );
    }

    @GetMapping("/indent/tracking/{indentMId}")
    public ResponseEntity<?> getIndentDetailsWRTIndentHeader(@PathVariable Long indentMId){
        return  ResponseEntity.ok(inventoryService.getIndentDetailsForIndentTracking(indentMId));
    }

    @GetMapping("/storeStockLedger")
    public ResponseEntity<?> getStoreStockLedgerReport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam Long hospitalId,
            @RequestParam Long itemId,
            @RequestParam String batchNo
    ) {
        return ResponseEntity.ok(inventoryService.getStoreStockLedgerReport(

                        page,
                        size,
                        hospitalId,
                        itemId,
                        batchNo
                )
        );
    }

    @GetMapping("/indent/tracking/statusMap")
    public ResponseEntity<?> getStatusMapForIndentTracking(){
        return  ResponseEntity.ok(inventoryService.getStatusMapForIndentTracking());
    }

    @GetMapping("/item/search")
    public ResponseEntity<?> getStockLedger(
            @RequestParam(required = false) Long sectionId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(inventoryService.getStoreItems(sectionId,keyword, page, size));
    }

    @GetMapping("/item/batches/{itemId}")
    public ResponseEntity<?> getAllBatchesWrtItem(@PathVariable Long itemId){
        return  ResponseEntity.ok(inventoryService.getBatchesFromItemId(itemId));
    }

    @GetMapping("/indent/getIssueMId")
    public ResponseEntity<?> getIssueMIdWrtIndentMId(@RequestParam Long indentMId){
        return  ResponseEntity.ok(inventoryService.getIssueMIdFromIndentMId(indentMId));
    }

    @GetMapping("/indent/getReceiveMId")
    public ResponseEntity<?> getReceiveMIdWrtIndentMId(@RequestParam Long indentMId){
        return  ResponseEntity.ok(inventoryService.getReceiveMIdFromIndentMId(indentMId));
    }

    @GetMapping("/indent/getReturnMId")
    public ResponseEntity<?> getReturnMIdWrtIndentMId(@RequestParam Long indentMId){
        return  ResponseEntity.ok(inventoryService.getReturnMIdFromIndentMId(indentMId));
    }

    @GetMapping("/indentApplicable/departments")
    public ApiResponse<?> fetchIndentApplicableDepartmentsExceptCurrent() {
        List<DepartmentDropdownResponse> list = inventoryService.fetchIndentApplicableDepartmentsExceptCurrent();
        return ResponseUtils.createSuccessResponse(
                list,
                new TypeReference<>() {}
        );
    }

    @GetMapping("/currentDepartment/{id}")
    public ResponseEntity<?> getCurrentDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getCurrentDepartmentById(id));
    }

    @GetMapping("/item/{itemId}")
    public ApiResponse<?> getItemDetailsById(@RequestParam Long hospitalId ,@PathVariable Long itemId) {
        return inventoryService.getItemById(hospitalId,itemId);
    }

    /**
     * Save (create or update) indent as DRAFT - backend sets status "S"
     */

    @PostMapping("/indent/save")
    public ApiResponse<StoreInternalIndentResponse> saveIndent(@RequestBody StoreInternalIndentRequest request) {
        return inventoryService.saveIndent(request);
    }

    /**
     * Submit an existing indent - backend sets status "Y"
     */
    @PostMapping("/indent/submit")
    public ApiResponse<StoreInternalIndentResponse> submitIndent(@RequestBody StoreInternalIndentRequest request) {
        return inventoryService.submitIndent(request);
    }

    /**
     * List of indents wrt department. Optional status filter ("S" or "Y")
     */

    @GetMapping("/indents/viewUpdate")
    public ResponseEntity<?> getAllIndentsForViewUpdateWrtDept(
            @RequestParam Long deptId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            @RequestParam(required = false) String status
    ) {



        return ResponseEntity.ok(
                inventoryService.getAllIndentsForViewUpdateWrtDept(
                        deptId,
                        page,
                        size,
                        fromDate,
                        toDate,
                        status
                )
        );
    }

    @GetMapping("/indents/approval/pending")
    public ResponseEntity<?> pendingForIndentApprovalWrtDept(
            @RequestParam("deptId") Long deptId) {
        return ResponseEntity.ok(inventoryService.pendingForIndentApprovalWrtDept(deptId));
    }

    @GetMapping("/indentDetailsForIssueWithAvailableStock/{indentMId}")
    public  ResponseEntity<?> getIndentDetailsForIssueWithAvailableStock(@PathVariable Long indentMId,Long departmentId){
        return ResponseEntity.ok(inventoryService.getIndentDetailsForIssueWithAvailableStock(indentMId,departmentId));

    }

    @PostMapping("/indent/approve")
    public ApiResponse<StoreInternalIndentResponse> approveRejectIndentForRequestDept(@RequestBody StoreInternalIndentApprovalRequest request) {
        return inventoryService.approveRejectIndent(request);
    }

    @GetMapping("/indents/approvedForIssueDept")
    public ResponseEntity<?> getAllIndentsApprovedForIssueDept(
            @RequestParam("deptId") Long deptId) {
        return ResponseEntity.ok(inventoryService.getAllIndentsApprovedForIssueDept(deptId));
    }

    @PostMapping("/indent/approvedByIssueDept")
    public ApiResponse<StoreInternalIndentResponse> approveIndentByIssueDept(
            @RequestBody IssueInternalIndentApprovalRequest request) {
        return inventoryService.approveIndentByIssueDept(request);
    }

    @GetMapping("/indents/forIssue")
    public ResponseEntity<?> getAllIndentsForIssueWrtDept(
            @RequestParam("deptId") Long deptId) {

        return ResponseEntity.ok(inventoryService.getAllIndentsForIssueWrtDept(deptId));
    }

    @GetMapping("/storeIssueM/list")
    public ResponseEntity<ApiResponse<List<StoreIssueMResponse>>> getIssuesIndentListWrtToDept(
            @RequestParam(required = false) Long toDeptId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        ApiResponse<List<StoreIssueMResponse>> response =
                inventoryService.getIssuesIndentListWrtToDept(toDeptId, fromDate, toDate);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/indentDetailsForIssue/{indentMId}")
    public  ResponseEntity<?> getIndentDetailsForIssue(@PathVariable Long indentMId,@RequestParam Long deptId) {

        return ResponseEntity.ok(inventoryService.getIndentDetailsWrtIndentAndDeptForIssue(indentMId,deptId));
    }

    @GetMapping("/indents/getPrevIssueInfos")
    public ApiResponse<List<PreviousIssueResponse>> getPreviousIssueInfos(
            @RequestParam Long itemId,
            @RequestParam(required = false) Long indentMId) {

        return inventoryService.getPreviousIssueInfos(itemId, indentMId);
    }

    @PostMapping("/indent/issue")
    public ApiResponse<StoreInternalIndentResponse> issueIndent(@RequestBody StoreInternalIssueRequest request) {
        return inventoryService.issueIndent(request);
    }

    @GetMapping("/indents/forReceiving")
    public ResponseEntity<?> getAllIndentsForReceiving(
            @RequestParam Long fromDeptId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

         return ResponseEntity.ok(inventoryService.getAllIndentsForReceiving(fromDeptId, fromDate, toDate));
    }

    @GetMapping("/indentDetailsForReceive/{indentMId}")
    public  ResponseEntity<?> getIndentDetailsForIssue(@PathVariable Long indentMId) {

        return ResponseEntity.ok(inventoryService.getIndentDetailsWrtIndentForReceiving(indentMId));
    }

    @PostMapping("/indent/receive")
    public ResponseEntity<ApiResponse<StoreIndentReceiveResponse>> saveReceiving(
            @RequestBody StoreIndentReceiveRequest request) {
        ApiResponse<StoreIndentReceiveResponse> response =
                inventoryService.saveReceiving(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/indents/viewUpdate/details/{indentMId}")
    public ResponseEntity<?> getIndentDetailsForViewUpdateWrtDept(@RequestParam Long currentDeptId,@PathVariable Long indentMId){
        return  ResponseEntity.ok(inventoryService.getIndentDetailsForRequestingDept(indentMId,currentDeptId));
    }


    //   ========================================================Opening Balance Entry=====================================================

    @PostMapping("/openingBalanceEntry/save")
    public ResponseEntity<?> saveOpeningBalanceEntry(@RequestBody OpeningBalanceEntryRequest openingBalanceEntryRequest) {
        return  ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.saveOpeningBalanceEntry(openingBalanceEntryRequest));
    }

    @GetMapping("/openingBalanceEntry/headers/{hospitalId}/{departmentId}")
    public ResponseEntity<?> getOpeningBalanceEntryHeaderListWrtDept(@RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "5") int size,
                                                                     @PathVariable Long hospitalId,
                                                                     @PathVariable Long departmentId,
                                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(inventoryService.getOpeningBalanceEntryHeaderListWrtDept(page,size,hospitalId,departmentId,fromDate,toDate));
    }


    @GetMapping("/openingBalanceEntry/details/{balanceMId}")
    public ResponseEntity<?> getOpeningBalanceEntryDetailsWrtHeader(@PathVariable Long balanceMId){
        return  ResponseEntity.ok(inventoryService.getOpeningBalanceEntryDetailsWrtHeader(balanceMId));
    }

    @GetMapping("/openingBalanceEntry/headers/withoutPagination")
    public ResponseEntity<?> getOpeningBalanceEntryHeaderListWrtDeptWithoutPagination(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId) {
        return ResponseEntity.ok(inventoryService.getAllOpeningBalanceEntryHeadersWrtDeptWithOutPagination(hospitalId, departmentId));
    }

    @PostMapping("/openingBalanceEntry/submit")
    public ResponseEntity<?> createOpeningBalanceEntryAndUpdateStatus(
            @RequestBody OpeningBalanceEntryRequest request
    ) {

        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createOpeningBalanceEntryAndUpdateStatus(request));
    }

    @PutMapping("/openingBalanceEntry/updateById/{id}")
    public ResponseEntity<ApiResponse<String>> updateOpeningBalanceById(@PathVariable Long id,
                                                                    @RequestBody OpeningBalanceEntryRequest openingBalanceEntryRequest) {
        return ResponseEntity.ok(inventoryService.updateOpeningBalanceById(id,openingBalanceEntryRequest));
    }

    @PutMapping("/openingBalanceEntry/approve/{id}")
    public ResponseEntity<ApiResponse<String>> approveOpeningBalance(@PathVariable Long id,
                                                        @RequestBody OpeningBalanceRequestForApprove request
    ) {

        return  ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.approveOpeningBalance(id,request));
    }

}
