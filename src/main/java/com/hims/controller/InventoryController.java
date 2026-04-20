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
@Tag(name = "Inventory Controller for all common inventory related service",description = "All the APIs are for common inventory will be here ")
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Retrieves a paginated list of all indent tracking records for the current department.
     * If the current user belongs to an admin department, returns indents for all indent-applicable departments.
     * Results are sorted by indent date in descending order.
     *
     * @param page Zero-based page number (default: 0)
     * @param size Number of records per page (default: 5)
     * @return Paginated indent tracking list with details such as indent number, date, department, and status
     */
    @GetMapping("/indent/tracking")
    public ResponseEntity<?> getIndentTracking(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(inventoryService.getIndentTrackingList(page, size));
    }

    /**
     * Searches and filters indent tracking records by department, date range, with pagination.
     * Allows filtering by source department (fromDepartmentId) and date range (fromDate to toDate).
     *
     * @param fromDepartmentId ID of the source department (optional)
     * @param fromDate Start date for the search range (optional, ISO date format)
     * @param toDate End date for the search range (optional, ISO date format)
     * @param page Zero-based page number (default: 0)
     * @param size Number of records per page (default: 5)
     * @return Filtered and paginated indent tracking results
     */
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

    /**
     * Retrieves detailed information for a specific indent including all indent detail transactions (items).
     * Used for viewing complete indent history and item breakdown.
     *
     * @param indentMId The Master ID of the indent record
     * @return List of indent detail responses containing item information, quantities, and status
     */
    @GetMapping("/indent/tracking/{indentMId}")
    public ResponseEntity<?> getIndentDetailsWRTIndentHeader(@PathVariable Long indentMId){
        return  ResponseEntity.ok(inventoryService.getIndentDetailsForIndentTracking(indentMId));
    }

    /**
     * Retrieves the store stock ledger report for a specific item and batch within a department/hospital.
     * Provides complete stock transaction history (IN/OUT movements) with pagination.
     *
     * @param page Zero-based page number (default: 0)
     * @param size Number of records per page (default: 5)
     * @param hospitalId The hospital ID to filter stock ledger
     * @param itemId The store item ID to generate ledger for
     * @param batchNo The batch number of the item
     * @return Paginated stock ledger entries with dates, quantities, and transaction types
     */
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

    /**
     * Fetches the status mapping for indent tracking workflow.
     * Returns all possible status codes and their descriptions for Store Internal Indent M (Master) column.
     * Example: "S" (Saved/Draft), "Y" (Submitted), "A" (Approved), etc.
     *
     * @return List of common status responses with status codes and descriptions
     */
    @GetMapping("/indent/tracking/statusMap")
    public ResponseEntity<?> getStatusMapForIndentTracking(){
        return  ResponseEntity.ok(inventoryService.getStatusMapForIndentTracking());
    }

    /**
     * Searches store items by keyword with optional section filtering.
     * Supports searching both drug items (when sectionId is null) and non-drug items (specific section).
     * Includes batch and stock information in results with pagination.
     *
     * @param sectionId The store section ID to filter items (null for drugs, specific ID for non-drugs)
     * @param keyword Search keyword to match against item nomenclature/name
     * @param page Zero-based page number (default: 0)
     * @param size Number of records per page (default: 5)
     * @return Paginated item list with stock and batch details, sorted by nomenclature
     */
    @GetMapping("/item/search")
    public ResponseEntity<?> getStockLedger(
            @RequestParam(required = false) Long sectionId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(inventoryService.getStoreItems(sectionId,keyword, page, size));
    }

    /**
     * Retrieves all available batch numbers for a specific store item within a hospital and department.
     * Includes only batches with expiry date beyond the configured drug expiry threshold.
     * Useful for selecting batches when issuing items.
     *
     * @param itemId The master item ID
     * @param hospitalId The hospital ID for scope filtering
     * @param departmentId The department ID for scope filtering
     * @return List of batch names with available stock information
     */
    @GetMapping("/item/batches/{itemId}")
    public ResponseEntity<?> getAllBatchesWrtItem(@PathVariable Long itemId,
                                                  @RequestParam Long hospitalId,
                                                  @RequestParam Long departmentId){
        return  ResponseEntity.ok(inventoryService.getBatchesFromItemId(itemId,hospitalId,departmentId));
    }

    /**
     * Fetches the Issue Master ID (StoreIssueM) associated with an indent.
     * Used to track the issue transaction created from this indent.
     *
     * @param indentMId The indent master ID
     * @return Issue master ID if an issue exists for this indent
     */
    @GetMapping("/indent/getIssueMId")
    public ResponseEntity<?> getIssueMIdWrtIndentMId(@RequestParam Long indentMId){
        return  ResponseEntity.ok(inventoryService.getIssueMIdFromIndentMId(indentMId));
    }

    /**
     * Fetches the Receive Master ID (StoreIndentReceiveM) associated with an indent.
     * Used to track the receiving transaction created from this indent.
     *
     * @param indentMId The indent master ID
     * @return Receive master ID if a received transaction exists for this indent
     */
    @GetMapping("/indent/getReceiveMId")
    public ResponseEntity<?> getReceiveMIdWrtIndentMId(@RequestParam Long indentMId){
        return  ResponseEntity.ok(inventoryService.getReceiveMIdFromIndentMId(indentMId));
    }

    /**
     * Fetches the Return Master ID (StoreReturnM) associated with an indent.
     * Used to track any return transactions created from rejected items in this indent.
     *
     * @param indentMId The indent master ID
     * @return Return master ID if a return transaction exists for this indent
     */
    @GetMapping("/indent/getReturnMId")
    public ResponseEntity<?> getReturnMIdWrtIndentMId(@RequestParam Long indentMId){
        return  ResponseEntity.ok(inventoryService.getReturnMIdFromIndentMId(indentMId));
    }

    /**
     * Retrieves list of departments eligible for indent transactions, excluding the current department.
     * Filters departments based on configured fixed department list.
     * Used for selecting "indent from" department in indent creation.
     *
     * @return List of department dropdown responses with ID and name
     */
    @GetMapping("/indentApplicable/departments")
    public ApiResponse<?> fetchIndentApplicableDepartmentsExceptCurrent() {
        List<DepartmentDropdownResponse> list = inventoryService.fetchIndentApplicableDepartmentsExceptCurrent();
        return ResponseUtils.createSuccessResponse(
                list,
                new TypeReference<>() {}
        );
    }

    /**
     * Fetches current department details by ID.
     * Returns department information in dropdown format.
     *
     * @param id The department ID
     * @return Department dropdown response with basic department information
     */
    @GetMapping("/currentDepartment/{id}")
    public ResponseEntity<?> getCurrentDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getCurrentDepartmentById(id));
    }

    /**
     * Retrieves detailed information for a specific store item including stock availability.
     * Provides item name, unit, GST, stock details across different departments and batches.
     * Used when selecting items for indents.
     *
     * @param hospitalId The hospital ID for scope filtering
     * @param itemId The master item ID
     * @param requestedDeptId The department requesting the item (optional, for context)
     * @param currentDeptId The current department context (optional, for stock comparison)
     * @return Item details with stock information and pricing
     */
    @GetMapping("/item/{itemId}")
    public ApiResponse<?> getItemDetailsById(@RequestParam Long hospitalId ,
                                             @PathVariable Long itemId,
                                             @RequestParam(required = false) Long requestedDeptId,
                                             @RequestParam(required = false) Long currentDeptId) {
        return inventoryService.getItemById(hospitalId,itemId,requestedDeptId,currentDeptId);
    }

    /**
     * Saves a new indent as DRAFT status (status code "S").
     * Allows departments to create and save indents without submitting them.
     * Backend generates indent master and detail records with draft status.
     *
     * @param request Indent request containing indent items, quantities, and department details
     * @return Saved indent response with indent master ID and generated indent number
     */
    @PostMapping("/indent/save")
    public ApiResponse<StoreInternalIndentResponse> saveIndent(@RequestBody StoreInternalIndentRequest request) {
        return inventoryService.saveIndent(request);
    }

    /**
     * Submits an existing DRAFT indent for approval (changes status to "Y").
     * Converts indent from draft status to submitted status for approval workflow.
     * Can only be performed on indents currently in DRAFT status.
     *
     * @param request Indent request containing updated indent details
     * @return Updated indent response with new status "Y" (Submitted)
     */
    @PostMapping("/indent/submit")
    public ApiResponse<StoreInternalIndentResponse> submitIndent(@RequestBody StoreInternalIndentRequest request) {
        return inventoryService.submitIndent(request);
    }

    /**
     * Retrieves list of indents for a department with optional status filtering and date range.
     * Used for viewing and updating indents within the requesting department.
     * Returns indents with statuses: Saved (S), Submitted (Y), or Approved (A) by default.
     *
     * @param departmentId The department ID to filter indents
     * @param page Zero-based page number (default: 0)
     * @param size Number of records per page (default: 5)
     * @param fromDate Start date filter (optional)
     * @param toDate End date filter (optional)
     * @param status Specific status filter - "S" (Saved), "Y" (Submitted), "A" (Approved) (optional)
     * @return Paginated indent list for view/update operations
     */
    @GetMapping("/indents/viewUpdate")
    public ResponseEntity<?> getAllIndentsForViewUpdateWrtDept(
            @RequestParam Long departmentId,
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
                        departmentId,
                        page,
                        size,
                        fromDate,
                        toDate,
                        status
                )
        );
    }

    /**
     * Retrieves indents pending approval for a department.
     * Returns submitted indents (status "Y") that are awaiting approval from the requesting department.
     * Used by approval authority to identify and process pending approvals.
     *
     * @param departmentId The department ID of the approving authority
     * @return List of indent tracking records pending approval
     */
    @GetMapping("/indents/approval/pending")
    public ResponseEntity<?> pendingForIndentApprovalWrtDept(
            @RequestParam Long departmentId) {
        return ResponseEntity.ok(inventoryService.pendingForIndentApprovalWrtDept(departmentId));
    }

    /**
     * Retrieves indent details with available stock information for issue processing.
     * Shows requested quantities and available stock from the issuing department.
     * Used when preparing to issue items against approved indents.
     *
     * @param indentMId The indent master ID
     * @param departmentId The issuing department ID
     * @return List of indent details with available stock information for each item
     */
    @GetMapping("/indentDetailsForIssueWithAvailableStock/{indentMId}")
    public  ResponseEntity<?> getIndentDetailsForIssueWithAvailableStock(@PathVariable Long indentMId,Long departmentId){
        return ResponseEntity.ok(inventoryService.getIndentDetailsForIssueWithAvailableStock(indentMId,departmentId));

    }

    /**
     * Approves or rejects an indent at the requesting department level.
     * Updates indent status to "A" (Approved) or "R" (Rejected) with optional remarks.
     * Can include item-level changes (quantity modifications, reasons, deletions).
     *
     * @param request Approval request containing approval action, remarks, and item modifications
     * @return Updated indent response with new approval status
     */
    @PostMapping("/indent/approve")
    public ApiResponse<StoreInternalIndentResponse> approveRejectIndentForRequestDept(@RequestBody StoreInternalIndentApprovalRequest request) {
        return inventoryService.approveRejectIndent(request);
    }

    /**
     * Retrieves indents approved by the requesting department and ready for issue.
     * Returns indents with status "A" (Approved at requesting dept) for the given issuing department.
     * Used by store/issuing department to identify which indents can be fulfilled.
     *
     * @param departmentId The issuing/store department ID
     * @return List of indent tracking records approved and ready for issue
     */
    @GetMapping("/indents/approvedForIssueDept")
    public ResponseEntity<?> getAllIndentsApprovedForIssueDept(
            @RequestParam Long departmentId) {
        return ResponseEntity.ok(inventoryService.getAllIndentsApprovedForIssueDept(departmentId));
    }

    /**
     * Approves or rejects an indent at the issuing/store department level.
     * Updates indent status based on store availability and approval decision.
     * Can include item-level approval quantities and reasons.
     * Used before creating issue transactions.
     *
     * @param request Issue department approval request with approval action and item details
     * @return Updated indent response with store approval status
     */
    @PostMapping("/indent/approvedByIssueDept")
    public ApiResponse<StoreInternalIndentResponse> approveIndentByIssueDept(
            @RequestBody IssueInternalIndentApprovalRequest request) {
        return inventoryService.approveIndentByIssueDept(request);
    }

    /**
     * Retrieves all indents approved and ready to be issued by the current store/department.
     * Returns indents with final approval status ready for issue transaction creation.
     *
     * @param departmentId The issuing department ID
     * @return List of indent master responses ready for issue processing
     */
    @GetMapping("/indents/forIssue")
    public ResponseEntity<?> getAllIndentsForIssueWrtDept(
            @RequestParam Long departmentId) {

        return ResponseEntity.ok(inventoryService.getAllIndentsForIssueWrtDept(departmentId));
    }

    /**
     * Retrieves list of store issue transactions (issues) for a receiving department within date range.
     * Used by receiving departments to track issued items and prepare for receiving.
     *
     * @param toDeptId The receiving department ID (optional)
     * @param fromDate Start date for issue search (optional)
     * @param toDate End date for issue search (optional)
     * @return List of issue master responses with issue details and item information
     */
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

    /**
     * Retrieves details of an approved indent ready for issue processing.
     * Shows item quantities, available stock, batches, and pricing information.
     * Used by issuing department to prepare and execute issue transaction.
     *
     * @param indentMId The indent master ID
     * @param departmentId The issuing department ID
     * @return List of indent detail responses with stock and batch information for issue
     */
    @GetMapping("/indentDetailsForIssue/{indentMId}")
    public  ResponseEntity<?> getIndentDetailsForIssue(@PathVariable Long indentMId,@RequestParam Long departmentId) {

        return ResponseEntity.ok(inventoryService.getIndentDetailsWrtIndentAndDeptForIssue(indentMId,departmentId));
    }

    /**
     * Retrieves previous issue information for a specific item.
     * Shows historical issue transactions and current stock info for batch selection.
     * Helps in selecting appropriate batches for issuing (FIFO, expiry awareness, etc.)
     *
     * @param itemId The store item ID
     * @param indentMId Current indent master ID (optional, for context)
     * @return List of previous issue responses with issue history and batch information
     */
    @GetMapping("/indents/getPrevIssueInfos")
    public ApiResponse<List<PreviousIssueResponse>> getPreviousIssueInfos(
            @RequestParam Long itemId,
            @RequestParam(required = false) Long indentMId) {

        return inventoryService.getPreviousIssueInfos(itemId, indentMId);
    }

    /**
     * Creates store issue transaction from an approved indent.
     * Generates issue number, reduces batch stock, and creates ledger entries.
     * Updates indent status to "I" (Issued) and creates StoreIssueM/T records.
     *
     * @param request Issue request containing indent items with batch, quantity, and expiry details
     * @return Issue response with generated issue number and transaction details
     */
    @PostMapping("/indent/issue")
    public ApiResponse<StoreInternalIndentResponse> issueIndent(@RequestBody StoreInternalIssueRequest request) {
        return inventoryService.issueIndent(request);
    }

    /**
     * Retrieves indents issued and ready to be received by a department.
     * Returns issued indents (from specified source department) within optional date range.
     * Used by receiving department to identify pending receipts.
     *
     * @param fromDeptId The source/issuing department ID
     * @param fromDate Start date for receive search (optional)
     * @param toDate End date for receive search (optional)
     * @return List of issue master responses ready for receiving
     */
    @GetMapping("/indents/forReceiving")
    public ResponseEntity<?> getAllIndentsForReceiving(
            @RequestParam Long fromDeptId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

         return ResponseEntity.ok(inventoryService.getAllIndentsForReceiving(fromDeptId, fromDate, toDate));
    }

    /**
     * Retrieves detailed information about an issued indent for receiving processing.
     * Shows issued quantities, batch information, and expected item details.
     * Used by receiving department to reconcile received items against issued quantities.
     *
     * @param indentMId The indent master ID
     * @return List of indent detail responses showing issued quantities and batch details
     */
    @GetMapping("/indentDetailsForReceive/{indentMId}")
    public  ResponseEntity<?> getIndentDetailsForIssue(@PathVariable Long indentMId) {

        return ResponseEntity.ok(inventoryService.getIndentDetailsWrtIndentForReceiving(indentMId));
    }

    /**
     * Records receipt of issued items against an indent.
     * Validates received + rejected quantities equal issued quantities.
     * Updates batch stock for received items, creates receiving ledger entries.
     * Auto-generates return transaction if items are rejected.
     *
     * @param request Receiving request with indent items, received quantities, and rejection details
     * @return Receive response with receive master ID, status, and return creation status if applicable
     */
    @PostMapping("/indent/receive")
    public ResponseEntity<ApiResponse<StoreIndentReceiveResponse>> saveReceiving(
            @RequestBody StoreIndentReceiveRequest request) {
        ApiResponse<StoreIndentReceiveResponse> response =
                inventoryService.saveReceiving(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * Retrieves indent details for the requesting department view/update screen.
     * Shows requested quantities, approved quantities, received quantities, and availability comparison.
     * Compares stock availability between store and requesting department.
     *
     * @param currentDeptId The current/requesting department ID
     * @param requestedDeptId The department that originally requested the indent
     * @param indentMId The indent master ID
     * @return List of indent detail responses with quantity tracking and availability comparison
     */
    @GetMapping("/indents/viewUpdate/details/{indentMId}")
    public ResponseEntity<?> getIndentDetailsForViewUpdateWrtDept(@RequestParam Long currentDeptId,@RequestParam Long requestedDeptId,@PathVariable Long indentMId){
        return  ResponseEntity.ok(inventoryService.getIndentDetailsForRequestingDept(indentMId,currentDeptId,requestedDeptId));
    }


    //   ========================================================Opening Balance Entry=====================================================

    /**
     * Saves a new opening balance entry in DRAFT status.
     * Records initial inventory stock balances for a department during system setup or period start.
     * Creates store balance master and detail records with "saved" status.
     *
     * @param openingBalanceEntryRequest Request containing balance type (drug/non-drug), items, quantities, prices, and batch details
     * @return Response with generated balance master ID
     */
    @PostMapping("/openingBalanceEntry/save")
    public ResponseEntity<?> saveOpeningBalanceEntry(@RequestBody OpeningBalanceEntryRequest openingBalanceEntryRequest) {
        return  ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.saveOpeningBalanceEntry(openingBalanceEntryRequest));
    }

    /**
     * Retrieves paginated list of opening balance entry headers for a department.
     * Allows filtering by date range and shows draft/submitted/approved records.
     * Used for viewing opening balance history.
     *
     * @param page Zero-based page number (default: 0)
     * @param size Number of records per page (default: 5)
     * @param hospitalId The hospital ID for scope filtering
     * @param departmentId The department ID to filter balance entries
     * @param fromDate Start date for balance search (optional)
     * @param toDate End date for balance search (optional)
     * @return Paginated list of opening balance headers with status and entry dates
     */
    @GetMapping("/openingBalanceEntry/headers/{hospitalId}/{departmentId}")
    public ResponseEntity<?> getOpeningBalanceEntryHeaderListWrtDept(@RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "5") int size,
                                                                     @PathVariable Long hospitalId,
                                                                     @PathVariable Long departmentId,
                                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(inventoryService.getOpeningBalanceEntryHeaderListWrtDept(page,size,hospitalId,departmentId,fromDate,toDate));
    }


    /**
     * Retrieves detailed line items for a specific opening balance entry.
     * Shows all items, quantities, rates, and calculation details including GST and costs.
     *
     * @param balanceMId The opening balance master ID
     * @return List of balance detail responses with item information, pricing, and calculations
     */
    @GetMapping("/openingBalanceEntry/details/{balanceMId}")
    public ResponseEntity<?> getOpeningBalanceEntryDetailsWrtHeader(@PathVariable Long balanceMId){
        return  ResponseEntity.ok(inventoryService.getOpeningBalanceEntryDetailsWrtHeader(balanceMId));
    }

    /**
     * Retrieves all opening balance entries for a department without pagination.
     * Returns only approved opening balance records ready for system use.
     * Used for dropdown/selection lists.
     *
     * @param hospitalId The hospital ID for scope filtering
     * @param departmentId The department ID to filter balance entries
     * @return List of approved opening balance headers without pagination
     */
    @GetMapping("/openingBalanceEntry/headers/withoutPagination")
    public ResponseEntity<?> getOpeningBalanceEntryHeaderListWrtDeptWithoutPagination(
            @RequestParam Long hospitalId,
            @RequestParam Long departmentId) {
        return ResponseEntity.ok(inventoryService.getAllOpeningBalanceEntryHeadersWrtDeptWithOutPagination(hospitalId, departmentId));
    }

    /**
     * Creates and submits an opening balance entry, setting status to "submitted".
     * Locks the entry for editing and makes it available for approval.
     *
     * @param request Opening balance entry request with complete details
     * @return Response with generated balance master ID
     */
    @PostMapping("/openingBalanceEntry/submit")
    public ResponseEntity<?> createOpeningBalanceEntryAndUpdateStatus(
            @RequestBody OpeningBalanceEntryRequest request
    ) {

        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createOpeningBalanceEntryAndUpdateStatus(request));
    }

    /**
     * Updates an existing opening balance entry (allowed only in draft/saved status).
     * Can add new items, update item details, or delete items (via deletedDt list).
     *
     * @param id The opening balance master ID to update
     * @param openingBalanceEntryRequest Updated opening balance request with modified details
     * @return API response with success message
     */
    @PutMapping("/openingBalanceEntry/updateById/{id}")
    public ResponseEntity<ApiResponse<String>> updateOpeningBalanceById(@PathVariable Long id,
                                                                    @RequestBody OpeningBalanceEntryRequest openingBalanceEntryRequest) {
        return ResponseEntity.ok(inventoryService.updateOpeningBalanceById(id,openingBalanceEntryRequest));
    }

    /**
     * Approves an opening balance entry and transfers items to batch stock.
     * Creates store batch stock records and ledger entries for each item.
     * Updates department inventory with opening balance quantities.
     * Status changes to "approved" and marks all items as approved.
     *
     * @param id The opening balance master ID to approve
     * @param request Approval request containing approval status and remarks
     * @return Response with success message and stock update details
     */
    @PutMapping("/openingBalanceEntry/approve/{id}")
    public ResponseEntity<ApiResponse<String>> approveOpeningBalance(@PathVariable Long id,
                                                        @RequestBody OpeningBalanceRequestForApprove request
    ) {

        return  ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.approveOpeningBalance(id,request));
    }


    /**
     * Retrieves stock information for a department and hospital, either in summary or detailed format.
     * Supports optional filtering by section, class, and specific item.
     * Returns summarized stock data or detailed batch-level information based on the type parameter.
     *
     * @param type The type of stock data to retrieve ("summary" for aggregated data or "details" for batch-level details)
     * @param hospitalId The hospital ID for scope filtering
     * @param departmentId The department ID to filter stock records
     * @param sectionId The store section ID to filter items (optional, null for all sections)
     * @param classId The item class ID to filter items (optional, null for all classes)
     * @param itemId The specific item ID to filter (optional, null for all items)
     * @return List of stock responses containing either OpeningBalanceStockResponse (summary) or OpeningBalanceStockResponseDto (details)
     */
    @GetMapping("/getAllStocks")
    public ResponseEntity<ApiResponse<List<?>>>  getAllStocks(@RequestParam String type,
                                                            @RequestParam Long hospitalId,
                                                            @RequestParam Long departmentId,
                                                            @RequestParam(required = false) Long sectionId,
                                                            @RequestParam(required = false) Long classId,
                                                            @RequestParam(required = false) Long itemId) {
        return ResponseEntity.ok(inventoryService.getAllStock(type, hospitalId, departmentId, sectionId, classId, itemId));

    }

}
