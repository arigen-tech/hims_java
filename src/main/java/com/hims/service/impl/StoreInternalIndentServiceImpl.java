package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.StoreInternalIndentService;
import com.hims.utils.AuthUtil;
import com.hims.utils.DepartmentConfig;
import com.hims.utils.ResponseUtils;
import com.hims.utils.StockFound;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreInternalIndentServiceImpl implements StoreInternalIndentService {

    private final StoreInternalIndentMRepository indentMRepository;
    private final StoreInternalIndentTRepository indentTRepository;
    private final MasStoreItemRepository masStoreItemRepository;
    private final MasDepartmentRepository masDepartmentRepository;
    private final StoreItemBatchStockRepository storeItemBatchStockRepository;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    DepartmentConfig departmentConfig;
    @Autowired
    StoreStockLedgerRepository storeStockLedgerRepository;
    @Autowired
    StoreIssueMRepository storeIssueMRepository;
    @Autowired
    StoreIssueTRepository storeIssueTRepository;

    // Add these repository injections to your StoreInternalIndentServiceImpl
    @Autowired
    private StoreIndentReceiveMRepository receiveMRepository;

    @Autowired
    private StoreIndentReceiveTRepository receiveTRepository;

    @Autowired
    private StoreReturnMRepository returnMRepository;

    @Autowired
    private StoreReturnTRepository returnTRepository;

    @Autowired
    private StoreItemDamagedStockRepository damagedStockRepository;


    @Autowired
    StockFound stockFound;

    @Autowired
    private StoreItemBatchStockRepository batchStockRepository;

    @Value("${fixed.departments}")
    private String fixedDepartmentsConfig;

    @Value("${hos.define.storeDay}")
    private Integer hospDefinedstoreDays;

    @Value("${hos.define.storeId}")
    private Integer deptIdStore;

    @Value("${hos.define.dispensaryDay}")
    private Integer hospDefineddispDays;

    @Value("${hos.define.dispensaryId}")
    private Integer dispdeptId;

    @Value("${hos.define.wardPharmDay}")
    private Integer hospDefinedwardDays;

    @Value("${hos.define.wardPharmacyId}")
    private Integer warddeptId;

    // Save (draft). Backend will set status = "S"
    @Override
    @Transactional
    public ApiResponse<StoreInternalIndentResponse> saveIndent(StoreInternalIndentRequest request) {
        return processIndent(request, "S"); // "S" for Save/Draft
    }

    // Submit indent — backend sets status = "Y"
    @Override
    @Transactional
    public ApiResponse<StoreInternalIndentResponse> submitIndent(StoreInternalIndentRequest request) {
        return processIndent(request, "Y"); // "Y" for Submit
    }

    // NEW: Approval/Rejection method
    @Override
    @Transactional
    public ApiResponse<StoreInternalIndentResponse> approveRejectIndent(StoreInternalIndentApprovalRequest request) {
        try {
            StoreInternalIndentM indentM = indentMRepository.findById(request.getIndentMId())
                    .orElseThrow(() -> new RuntimeException("Indent not found with ID: " + request.getIndentMId()));

            // Validate current status - only pending indents (Y) can be approved/rejected
            if (!"Y".equalsIgnoreCase(indentM.getStatus())) {
                throw new RuntimeException("Only pending indents can be approved or rejected. Current status: " + indentM.getStatus());
            }

            User currentUser = authUtil.getCurrentUser();
            String currentUserName = currentUser != null ? currentUser.getFirstName() : "";

            // Validate action
            if (!"approved".equalsIgnoreCase(request.getAction()) && !"rejected".equalsIgnoreCase(request.getAction())) {
                throw new RuntimeException("Invalid action. Must be 'approved' or 'rejected'");
            }

            // Update status based on action
            String newStatus = "approved".equalsIgnoreCase(request.getAction()) ? "A" : "R";

            indentM.setStatus(newStatus);
            indentM.setApprovedBy(currentUserName);
            indentM.setApprovedDate(LocalDateTime.now());
            indentM.setRemarks(request.getRemarks());

            // Handle items update if provided
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                for (StoreInternalIndentDetailRequest itemReq : request.getItems()) {
                    if (itemReq.getIndentTId() != null) {
                        // Update existing item
                        Optional<StoreInternalIndentT> existingDetail = indentTRepository.findById(itemReq.getIndentTId());
                        if (existingDetail.isPresent()) {
                            StoreInternalIndentT detail = existingDetail.get();

                            // Verify this detail belongs to the current indent
                            if (!detail.getIndentM().getIndentMId().equals(indentM.getIndentMId())) {
                                throw new RuntimeException("Indent detail does not belong to this indent");
                            }

                            // Update fields if provided
                            if (itemReq.getRequestedQty() != null) {
                                detail.setRequestedQty(itemReq.getRequestedQty());
                            }

                            // Calculate and update current available stock
                            Long currentStock = calculateCurrentStock(detail.getItemId().getItemId(), indentM.getFromDeptId().getId());
                            detail.setAvailableStock(BigDecimal.valueOf(currentStock));

                            if (itemReq.getReason() != null) {
                                detail.setReason(itemReq.getReason());
                            }

                            indentTRepository.save(detail);
                        }
                    }
                }
            }

            // Handle deleted items
            handleDeletedItemsForApproval(request.getDeletedT(), indentM);

            // Save the updated indent
            indentM = indentMRepository.save(indentM);

            // Build and return response
            StoreInternalIndentResponse response = buildResponse(indentM);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<StoreInternalIndentResponse>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null,
                    new TypeReference<StoreInternalIndentResponse>() {},
                    "Error processing indent: " + e.getMessage(), 400);
        }
    }
    // Helper method for handling deleted items in approval
    private void handleDeletedItemsForApproval(List<Long> deletedT, StoreInternalIndentM indentM) {
        if (deletedT != null && !deletedT.isEmpty()) {
            for (Long deletedId : deletedT) {
                Optional<StoreInternalIndentT> toDelete = indentTRepository.findById(deletedId);
                if (toDelete.isPresent() &&
                        toDelete.get().getIndentM().getIndentMId().equals(indentM.getIndentMId())) {
                    indentTRepository.deleteById(deletedId);
                }
            }
        }
    }

    // Get all indents with status filter
    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<StoreInternalIndentResponse>> getAllIndentsForPending(Long deptId) {
        try {
            // Get the current login department for dynamic stock calculation
            Long loginDeptId = authUtil.getCurrentDepartmentId();
            if (loginDeptId == null) {
                throw new RuntimeException("Login department id not found");
            }

            // ✅ use toDeptId instead of fromDeptId
            List<StoreInternalIndentM> indents =
                    indentMRepository.findByToDeptId_IdAndStatus(deptId, "Y");

            // if you did NOT create the OrderBy method above, sort here
            indents.sort(Comparator.comparing(StoreInternalIndentM::getIndentMId).reversed());

            List<StoreInternalIndentResponse> responseList = new ArrayList<>();
            for (StoreInternalIndentM indent : indents) {
                responseList.add(buildResponseWithLoginDept(indent, loginDeptId)); // Use new method
            }

            return ResponseUtils.createSuccessResponse(
                    responseList,
                    new TypeReference<List<StoreInternalIndentResponse>>() {}
            );

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<List<StoreInternalIndentResponse>>() {},
                    "Error fetching pending indents: " + e.getMessage(),
                    500
            );
        }
    }

    // Common method to process both save and submit
    private ApiResponse<StoreInternalIndentResponse> processIndent(StoreInternalIndentRequest request, String status) {
        StoreInternalIndentM indentM;
        boolean isNew = (request.getIndentMId() == null);

        User currentUser = authUtil.getCurrentUser();
        String currentUserName = currentUser != null ? currentUser.getFirstName() : "";

        if (isNew) {
            indentM = new StoreInternalIndentM();
            indentM.setCreatedDate(LocalDateTime.now());
            indentM.setCreatedBy(currentUserName);
            // Set indent date on create
            indentM.setIndentDate(
                    request.getIndentDate() != null ? request.getIndentDate() : LocalDateTime.now()
            );
            indentM.setIndentNo(generateIndentNo());

            // from department = current login department (id from token)
            Long deptId = authUtil.getCurrentDepartmentId();
            if (deptId == null) {
                throw new RuntimeException("Current department id not found in token");
            }
            MasDepartment fromDept = masDepartmentRepository.findById(deptId)
                    .orElseThrow(() -> new RuntimeException("Current department not found"));
            indentM.setFromDeptId(fromDept);

        } else {
            // Existing indent
            indentM = indentMRepository.findById(request.getIndentMId())
                    .orElseThrow(() -> new RuntimeException("Indent not found for update"));
        }

        // to department
        if (request.getToDeptId() != null) {
            MasDepartment toDept = masDepartmentRepository.findById(request.getToDeptId())
                    .orElseThrow(() -> new RuntimeException("To department not found"));
            indentM.setToDeptId(toDept);
        } else {
            indentM.setToDeptId(null);
        }

        // Set status (S for save/draft, Y for submit)
        indentM.setStatus(status);

        // For submitted indents, set approval info and UPDATE indent date
        if ("Y".equals(status)) {
            indentM.setApprovedBy(currentUserName);
            indentM.setApprovedDate(LocalDateTime.now());

            // 🔹 Update indent date on submit
            indentM.setIndentDate(
                    request.getIndentDate() != null ? request.getIndentDate() : LocalDateTime.now()
            );
        }

        // Save header first to get id
        indentM = indentMRepository.save(indentM);

        // Handle items - Only create/update valid items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (StoreInternalIndentDetailRequest dReq : request.getItems()) {
                // Validate required fields
                if (dReq.getItemId() == null || dReq.getRequestedQty() == null) {
                    continue; // Skip invalid items
                }

                StoreInternalIndentT detail;

                if (dReq.getIndentTId() != null) {
                    // UPDATE existing item - only if indentTId is provided and exists
                    Optional<StoreInternalIndentT> existingDetail = indentTRepository.findById(dReq.getIndentTId());
                    if (existingDetail.isPresent()) {
                        detail = existingDetail.get();
                        // Verify this detail belongs to the current indent
                        if (!detail.getIndentM().getIndentMId().equals(indentM.getIndentMId())) {
                            throw new RuntimeException("Indent detail does not belong to this indent");
                        }
                    } else {
                        // If indentTId is provided but not found, treat as new item
                        detail = new StoreInternalIndentT();
                        detail.setIndentM(indentM);

                        MasStoreItem item = masStoreItemRepository.findById(dReq.getItemId())
                                .orElseThrow(() -> new RuntimeException("Item not found: " + dReq.getItemId()));
                        detail.setItemId(item);
                        detail.setIssueStatus("N");
                    }
                } else {
                    // CREATE new item - when no indentTId is provided
                    detail = new StoreInternalIndentT();
                    detail.setIndentM(indentM);

                    MasStoreItem item = masStoreItemRepository.findById(dReq.getItemId())
                            .orElseThrow(() -> new RuntimeException("Item not found: " + dReq.getItemId()));
                    detail.setItemId(item);
                    detail.setIssueStatus("N");
                }

                // Update fields
                detail.setRequestedQty(dReq.getRequestedQty());
                detail.setAvailableStock(
                        dReq.getAvailableStock() != null ? dReq.getAvailableStock() : BigDecimal.valueOf(0)
                );
                detail.setReason(dReq.getReason() != null ? dReq.getReason() : "");

                indentTRepository.save(detail);
            }
        }

        // Handle deleted items
        handleDeletedItems(request, indentM);

        StoreInternalIndentResponse resp = buildResponse(indentM);
        return ResponseUtils.createSuccessResponse(resp, new TypeReference<StoreInternalIndentResponse>() {});
    }

    // FIXED: Handle deleted items properly whether it's a single Long or List<Long>
    private void handleDeletedItems(StoreInternalIndentRequest request, StoreInternalIndentM indentM) {
        Object deletedT = request.getDeletedT();

        if (deletedT != null) {
            List<Long> deletedIds = new ArrayList<>();

            // Handle different types of deletedT
            if (deletedT instanceof List) {
                // It's a List
                @SuppressWarnings("unchecked")
                List<Object> deletedList = (List<Object>) deletedT;
                for (Object item : deletedList) {
                    if (item instanceof Long) {
                        deletedIds.add((Long) item);
                    } else if (item instanceof Integer) {
                        deletedIds.add(((Integer) item).longValue());
                    }
                }
            } else if (deletedT instanceof Long) {
                // It's a single Long
                deletedIds.add((Long) deletedT);
            } else if (deletedT instanceof Integer) {
                // It's a single Integer
                deletedIds.add(((Integer) deletedT).longValue());
            }

            // Process deletions
            for (Long deletedId : deletedIds) {
                Optional<StoreInternalIndentT> toDelete = indentTRepository.findById(deletedId);
                if (toDelete.isPresent() &&
                        toDelete.get().getIndentM().getIndentMId().equals(indentM.getIndentMId())) {
                    indentTRepository.deleteById(deletedId);
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<StoreInternalIndentResponse> getIndentById(Long indentMId) {
        StoreInternalIndentM indentM = indentMRepository.findById(indentMId)
                .orElseThrow(() -> new RuntimeException("Indent not found"));
        StoreInternalIndentResponse resp = buildResponse(indentM);
        return ResponseUtils.createSuccessResponse(resp, new TypeReference<StoreInternalIndentResponse>() {});
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<StoreInternalIndentResponse>> listIndentsByCurrentDept() {
        Long deptId = authUtil.getCurrentDepartmentId();
        if (deptId == null) {
            throw new RuntimeException("Current department id not found in token");
        }

        List<String> allowedStatuses = Arrays.asList("S", "Y");
        List<StoreInternalIndentM> list =
                indentMRepository.findByFromDeptId_IdAndStatusIn(deptId, allowedStatuses);

        List<StoreInternalIndentResponse> respList = new ArrayList<>();
        for (StoreInternalIndentM m : list) {
            respList.add(buildResponse(m));
        }

        return ResponseUtils.createSuccessResponse(
                respList,
                new TypeReference<List<StoreInternalIndentResponse>>() {}
        );
    }

    // Create from ROL
    @Override
    @Transactional
    public ApiResponse<StoreInternalIndentResponse> createIndentFromROL(StoreInternalIndentRequest baseRequest) {
        return saveIndent(baseRequest); // backend sets status S
    }

    // Create from previous indent copying detail lines
    @Override
    @Transactional
    public ApiResponse<StoreInternalIndentResponse> createIndentFromPrevious(Long previousIndentMId) {
        StoreInternalIndentM previous = indentMRepository.findById(previousIndentMId)
                .orElseThrow(() -> new RuntimeException("Previous indent not found"));

        User currentUser = authUtil.getCurrentUser();
        String currentUserName = currentUser != null ? currentUser.getFirstName() : "";

        StoreInternalIndentM newHeader = new StoreInternalIndentM();
        newHeader.setIndentNo(generateIndentNo());
        newHeader.setIndentDate(LocalDateTime.now());
        newHeader.setFromDeptId(previous.getFromDeptId());
        newHeader.setToDeptId(previous.getToDeptId());
        newHeader.setStatus("S");
        newHeader.setCreatedBy(currentUserName);
        newHeader.setCreatedDate(LocalDateTime.now());
        newHeader = indentMRepository.save(newHeader);

        List<StoreInternalIndentT> prevDetails = indentTRepository.findByIndentM(previous);
        BigDecimal totalCost = BigDecimal.ZERO;
        for (StoreInternalIndentT pd : prevDetails) {
            StoreInternalIndentT d = new StoreInternalIndentT();
            d.setIndentM(newHeader);
            d.setItemId(pd.getItemId());
            d.setRequestedQty(pd.getRequestedQty());
            d.setAvailableStock(pd.getAvailableStock());
            d.setItemCost(pd.getItemCost());
            d.setTotalCost(pd.getTotalCost());
            d.setIssueStatus("S");
            indentTRepository.save(d);
            if (pd.getTotalCost() != null) totalCost = totalCost.add(pd.getTotalCost());
        }
        newHeader.setTotalCost(totalCost);
        indentMRepository.save(newHeader);

        StoreInternalIndentResponse resp = buildResponse(newHeader);
        return ResponseUtils.createSuccessResponse(resp, new TypeReference<StoreInternalIndentResponse>() {});
    }


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<StoreInternalIndentResponse>> getAllIndentsForApproved(Long deptId) {
        try {
            // Get current login department for dynamic stock calculation
            Long loginDeptId = authUtil.getCurrentDepartmentId();
            if (loginDeptId == null) {
                throw new RuntimeException("Login department id not found");
            }

            // ✅ use toDeptId instead of fromDeptId
            List<StoreInternalIndentM> indents =
                    indentMRepository.findByToDeptId_IdAndStatus(deptId, "A");

            indents.sort(Comparator.comparing(StoreInternalIndentM::getIndentMId).reversed());

            List<StoreInternalIndentResponse> responseList = new ArrayList<>();
            for (StoreInternalIndentM indent : indents) {
                responseList.add(buildResponseWithLoginDept(indent, loginDeptId)); // Use new method
            }

            return ResponseUtils.createSuccessResponse(
                    responseList,
                    new TypeReference<List<StoreInternalIndentResponse>>() {}
            );

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<List<StoreInternalIndentResponse>>() {},
                    "Error fetching approved indents: " + e.getMessage(),
                    500
            );
        }
    }
    // Submit approved indent for issue - updates status from A to AA
    @Override
    @Transactional
    public ApiResponse<StoreInternalIndentResponse> submitApprovedIndent(IssueInternalIndentApprovalRequest request) {
        try {
            // 1. Load indent master
            StoreInternalIndentM indentM = indentMRepository.findById(request.getIndentMId())
                    .orElseThrow(() -> new RuntimeException("Indent not found with ID: " + request.getIndentMId()));

            // 2. Only indents with status A are allowed
            if (!"A".equalsIgnoreCase(indentM.getStatus())) {
                throw new RuntimeException(
                        "Only indents with status 'A' can be processed. Current status: " + indentM.getStatus());
            }

            // 3. Current user
            User currentUser = authUtil.getCurrentUser();
            String currentUserName = currentUser != null ? currentUser.getFirstName() : "";

            String action = request.getAction() != null ? request.getAction().trim().toLowerCase() : "";

            // 4. Common item processing (approve qty + reason)
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                for (IssueInternalIndentDetailRequest itemReq : request.getItems()) {
                    if (itemReq.getIndentTId() != null) {
                        StoreInternalIndentT detail = indentTRepository.findById(itemReq.getIndentTId())
                                .orElseThrow(() -> new RuntimeException(
                                        "Indent detail not found with ID: " + itemReq.getIndentTId()));

                        // Ensure this detail belongs to the current indent
                        if (!detail.getIndentM().getIndentMId().equals(indentM.getIndentMId())) {
                            throw new RuntimeException("Indent detail does not belong to this indent");
                        }

                        // Update approve quantity if provided
                        if (itemReq.getApproveQty() != null) {
                            detail.setApprovedQty(itemReq.getApproveQty());
                        }

                        // Update reason if provided
                        if (itemReq.getReason() != null) {
                            detail.setReason(itemReq.getReason());
                        }

                        indentTRepository.save(detail);
                    }
                }
            }

            // 5. Action-based status change
            if ("approved".equals(action)) {
                // Submitted for issue
                indentM.setStatus("AA");                 // Approved and submitted for issue
                indentM.setStoreApprovedBy(currentUserName);
                indentM.setStoreApprovedDate(LocalDateTime.now());
            } else if ("rejected".equals(action)) {
                // Rejected after approval
                indentM.setStatus("RR");                 // Rejected after approval
                indentM.setStoreApprovedBy(currentUserName);
                indentM.setStoreApprovedDate(LocalDateTime.now());
            } else {
                throw new RuntimeException(
                        "Invalid action. Must be 'approved' or 'rejected'. Provided: " + request.getAction());
            }

            // 6. Set remarks (common)
            indentM.setRemarks(request.getRemarks());

            // 7. If you add deletedT in IssueInternalIndentApprovalRequest, you can enable this:
            // handleDeletedItemsForSubmit(request.getDeletedT(), indentM);

            // 8. Save master
            indentM = indentMRepository.save(indentM);

            // 9. Build response
            StoreInternalIndentResponse response = buildResponse(indentM);
            return ResponseUtils.createSuccessResponse(
                    response,
                    new TypeReference<StoreInternalIndentResponse>() {}
            );

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<StoreInternalIndentResponse>() {},
                    "Error submitting indent: " + e.getMessage(),
                    400
            );
        }
    }
    // Helper method for handling deleted items in submit
    private void handleDeletedItemsForSubmit(List<Long> deletedT, StoreInternalIndentM indentM) {
        if (deletedT != null && !deletedT.isEmpty()) {
            for (Long deletedId : deletedT) {
                Optional<StoreInternalIndentT> toDelete = indentTRepository.findById(deletedId);
                if (toDelete.isPresent() &&
                        toDelete.get().getIndentM().getIndentMId().equals(indentM.getIndentMId())) {
                    indentTRepository.deleteById(deletedId);
                }
            }
        }
    }

    // ============ CHANGE IN getAllIndentsForIssueDepartment METHOD ============
// Location: StoreInternalIndentServiceImpl.java - Replace the batch mapping section

    // ============ CHANGE IN getAllIndentsForIssueDepartment METHOD ============
// Location: StoreInternalIndentServiceImpl.java - Replace the batch mapping section

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<StoreInternalIndentResponse>> getAllIndentsForIssueDepartment(Long deptId) {
        try {

            if (deptId == null) {
                throw new RuntimeException("deptId is required");
            }

            // Fetch only "AA" status indents for issue dept
            List<StoreInternalIndentM> indents =
                    indentMRepository.findByToDeptId_IdAndStatusIn(
                            deptId,
                            Arrays.asList("AA", "PI")
                    );

            // Sort DESC (latest indent first)
            indents.sort(Comparator.comparing(StoreInternalIndentM::getIndentMId).reversed());

            List<StoreInternalIndentResponse> masterResponseList = new ArrayList<>();

            for (StoreInternalIndentM indent : indents) {

                StoreInternalIndentResponse masterResp = buildResponse(indent);

                // DETAILS PART
                List<StoreInternalIndentT> details =
                        indentTRepository.findByIndentM(indent);

                List<StoreInternalIndentDetailResponse> detailResponseList = new ArrayList<>();

                for (StoreInternalIndentT detail : details) {

                    StoreInternalIndentDetailResponse d = new StoreInternalIndentDetailResponse();

                    d.setIndentTId(detail.getIndentTId());
                    d.setItemId(detail.getItemId().getItemId());
                    d.setItemName(detail.getItemId().getNomenclature());
                    d.setPvmsNo(detail.getItemId().getPvmsNo());
                    d.setUnitAuName(detail.getItemId().getUnitAU().getUnitName());
                    d.setUnitAUid(detail.getItemId().getUnitAU().getUnitId());

                    d.setRequestedQty(detail.getRequestedQty());
                    d.setApprovedQty(detail.getApprovedQty());
                    d.setIssuedQty(detail.getIssuedQty());
                    d.setReceivedQty(detail.getReceivedQty());
                    d.setAvailableStock(detail.getAvailableStock());
                    d.setItemCost(detail.getItemCost());
                    d.setTotalCost(detail.getTotalCost());
                    d.setIssueStatus(detail.getIssueStatus());
                    d.setReason(detail.getReason());

                    // ========================= BATCH STOCK =========================
                    // CHANGE 1: Get the department object first (for current issuing dept)
                    MasDepartment issuingDept = masDepartmentRepository.findById(deptId)
                            .orElse(null);

                    if (issuingDept == null) {
                        System.out.println("Department not found for ID: " + deptId);
                        d.setBatches(new ArrayList<>());
                        detailResponseList.add(d);
                        continue;
                    }

                    Long hospitalId = authUtil.getCurrentUser().getHospital().getId();
                    Integer deptIdAsInt = deptId.intValue();
                    Long itemId = detail.getItemId().getItemId();

                    // CHANGE 2: Filter batches for CURRENT DEPARTMENT ONLY
                    // Using MasDepartment object instead of Long
                    List<StoreItemBatchStock> batchStocks =
                            storeItemBatchStockRepository.findByDepartmentIdAndItemId(
                                    issuingDept,  // Pass MasDepartment object, not Long
                                    detail.getItemId()
                            );

                    // CHANGE 3: Sort by expiry date (FEFO - First Expiry First Out)
                    if (batchStocks != null && !batchStocks.isEmpty()) {
                        batchStocks.sort(Comparator.comparing(StoreItemBatchStock::getExpiryDate));
                    }

                    // Map batches with current department stock
                    List<BatchResponse> batchResponseList = new ArrayList<>();

                    if (batchStocks != null && !batchStocks.isEmpty()) {
                        for (StoreItemBatchStock batch : batchStocks) {
                            BatchResponse br = new BatchResponse();
                            br.setBatchNo(batch.getBatchNo());
                            br.setManufactureDate(batch.getManufactureDate());
                            br.setExpiryDate(batch.getExpiryDate());

                            // ===== CHANGE 4: Show ONLY current department stock =====
                            Long closingStock = batch.getClosingStock() != null ? batch.getClosingStock() : 0L;
                            br.setBatchStock(closingStock);  // This is the stock from current department

                            // Keep other department stocks for reference (optional)
                            Long avlableStokes = stockFound.getAvailableStocks(
                                    hospitalId,
                                    deptIdStore,
                                    itemId,
                                    hospDefinedstoreDays
                            );
                            br.setStoreStocks(avlableStokes);

                            Long dispstocks = stockFound.getAvailableStocks(
                                    hospitalId,
                                    dispdeptId,
                                    itemId,
                                    hospDefineddispDays
                            );
                            br.setDispStocks(dispstocks);

                            Long wardstocks = stockFound.getAvailableStocks(
                                    hospitalId,
                                    warddeptId,
                                    itemId,
                                    hospDefinedwardDays
                            );
                            br.setWardStocks(wardstocks);

                            batchResponseList.add(br);
                        }
                    } else {
                        // No batches found for current department
                        System.out.println("No batches found for item: " + itemId + " in department: " + deptId);
                    }

                    d.setBatches(batchResponseList);
                    detailResponseList.add(d);
                }

                masterResp.setItems(detailResponseList);
                masterResponseList.add(masterResp);
            }

            return ResponseUtils.createSuccessResponse(
                    masterResponseList,
                    new TypeReference<List<StoreInternalIndentResponse>>() {}
            );

        } catch (Exception e) {

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<List<StoreInternalIndentResponse>>() {},
                    "Error fetching indents: " + e.getMessage(),
                    500
            );
        }
    }




    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<PreviousIssueResponse>> getPreviousIssues(Long itemId, Long currentIndentMId) {
        try {
            if (itemId == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<List<PreviousIssueResponse>>() {},
                        "Item ID is required",
                        400
                );
            }

            System.out.println("=== Fetching Previous Issues ===");
            System.out.println("Item ID: " + itemId);

            List<Map<String, Object>> resultMaps =
                    indentTRepository.findPreviousIssuesForItemAsMap(itemId);

            List<PreviousIssueResponse> previousIssues = resultMaps.stream().map(map -> {
                PreviousIssueResponse response = new PreviousIssueResponse();

                // Issue Date
                Object issueDateObj = map.get("issueDate");
                if (issueDateObj instanceof java.sql.Date)
                    response.setIssueDate(((java.sql.Date) issueDateObj).toLocalDate());
                else if (issueDateObj instanceof java.sql.Timestamp)
                    response.setIssueDate(((java.sql.Timestamp) issueDateObj).toLocalDateTime().toLocalDate());
                else if (issueDateObj instanceof LocalDateTime)
                    response.setIssueDate(((LocalDateTime) issueDateObj).toLocalDate());
                else if (issueDateObj instanceof LocalDate)
                    response.setIssueDate((LocalDate) issueDateObj);

                response.setIndentNo((String) map.get("indentNo"));
                response.setBatchNo((String) map.get("batchNo"));
                response.setIssueNo((String) map.get("issueNo"));

                // Qty Issued
                Object qtyObj = map.get("qtyIssued");
                if (qtyObj instanceof BigDecimal)
                    response.setQtyIssued((BigDecimal) qtyObj);
                else if (qtyObj instanceof Number)
                    response.setQtyIssued(BigDecimal.valueOf(((Number) qtyObj).doubleValue()));

                // Expiry Date
                Object exp = map.get("expiryDate");
                if (exp instanceof java.sql.Date)
                    response.setExpiryDate(((java.sql.Date) exp).toLocalDate());
                else if (exp instanceof LocalDate)
                    response.setExpiryDate((LocalDate) exp);

                return response;
            }).collect(Collectors.toList());

            System.out.println("Total records found: " + previousIssues.size());

            // If nothing found → show current batch stock
            if (previousIssues.isEmpty()) {
                List<StoreItemBatchStock> batches =
                        batchStockRepository.findByItemIdItemId(itemId);

                if (!batches.isEmpty()) {
                    PreviousIssueResponse curr = new PreviousIssueResponse();
                    curr.setIndentNo("Current Stock Info");
                    curr.setIssueDate(LocalDate.now());
                    curr.setIssueNo("Current");
                    curr.setQtyIssued(BigDecimal.ZERO);

                    StringBuilder batchInfo = new StringBuilder();
                    for (StoreItemBatchStock b : batches) {
                        if (batchInfo.length() > 0) batchInfo.append(", ");
                        batchInfo.append(b.getBatchNo())
                                .append("(")
                                .append(b.getClosingStock() != null ? b.getClosingStock() : 0)
                                .append(")");
                    }

                    curr.setBatchNo(batchInfo.toString());
                    previousIssues.add(curr);
                }
            }

            return ResponseUtils.createSuccessResponse(
                    previousIssues,
                    new TypeReference<List<PreviousIssueResponse>>() {}
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<List<PreviousIssueResponse>>() {},
                    "Error fetching previous issues: " + e.getMessage(),
                    500
            );
        }
    }




    //=============================================================indentreceving================================================


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<StoreInternalIndentResponse>> getAllIndentsForReceiving(
            Long fromDeptId,
            LocalDate fromDate,
            LocalDate toDate) {

        try {

            List<StoreInternalIndentM> indents;

            if (fromDeptId != null && fromDate != null && toDate != null) {

                LocalDateTime start = fromDate.atStartOfDay();
                LocalDateTime end = toDate.atTime(23, 59, 59);

                indents = indentMRepository
                        .findByFromDeptId_IdAndStatusAndIssuedDateBetween(
                                fromDeptId, "FI", start, end
                        );

            } else if (fromDeptId != null) {

                indents = indentMRepository
                        .findByFromDeptId_IdAndStatus(fromDeptId, "FI");

            } else {

                indents = indentMRepository.findByStatus("FI");
            }

            indents.sort(Comparator.comparing(
                    StoreInternalIndentM::getIssuedDate,
                    Comparator.nullsLast(Comparator.reverseOrder())
            ));

            List<StoreInternalIndentResponse> responseList = new ArrayList<>();

            for (StoreInternalIndentM indent : indents) {

                StoreInternalIndentResponse response =
                        buildSimpleResponse(indent);

                // 🔹 Get issue master for this indent
                List<StoreIssueM> issueMasters =
                        storeIssueMRepository.findByIndentMId(indent);

                Map<Long, List<StoreIssueT>> issueItemMap =
                        new HashMap<>();

                for (StoreIssueM issueM : issueMasters) {

                    List<StoreIssueT> issueTs =
                            storeIssueTRepository.findByStoreIssueMId(issueM);

                    for (StoreIssueT issueT : issueTs) {

                        Long indentTId =
                                issueT.getIndentTId().getIndentTId();

                        issueItemMap
                                .computeIfAbsent(indentTId, k -> new ArrayList<>())
                                .add(issueT);
                    }
                }

                List<StoreInternalIndentDetailResponse> itemResponses =
                        new ArrayList<>();

                for (StoreInternalIndentT indentT :
                        indentTRepository.findByIndentM(indent)) {

                    StoreInternalIndentDetailResponse dr =
                            new StoreInternalIndentDetailResponse();

                    dr.setIndentTId(indentT.getIndentTId());
                    dr.setItemId(indentT.getItemId().getItemId());
                    dr.setItemName(indentT.getItemId().getNomenclature());
                    dr.setPvmsNo(indentT.getItemId().getPvmsNo());
                    dr.setUnitAuName(indentT.getItemId().getUnitAU().getUnitName());
                    dr.setUnitAUid(indentT.getItemId().getUnitAU().getUnitId());

                    dr.setRequestedQty(indentT.getRequestedQty());
                    dr.setApprovedQty(indentT.getApprovedQty());
                    dr.setIssuedQty(indentT.getIssuedQty());
                    dr.setReceivedQty(indentT.getReceivedQty());
                    dr.setIssueStatus(indentT.getIssueStatus());
                    dr.setReason(indentT.getReason());

                    // 🔹 BATCHES FROM ISSUE TABLE
                    List<BatchResponse> batchResponses =
                            new ArrayList<>();

                    List<StoreIssueT> issuedBatches =
                            issueItemMap.get(indentT.getIndentTId());

                    if (issuedBatches != null) {

                        for (StoreIssueT issueT : issuedBatches) {

                            BatchResponse br = new BatchResponse();
                            br.setBatchNo(issueT.getBatchNo());
                            br.setManufactureDate(issueT.getDom());
                            br.setExpiryDate(issueT.getExpiryDate());
                            br.setBrandName(issueT.getBrandname());

                            br.setManufacturerName(issueT.getManufacturername());
                            br.setBatchIssuedQty(issueT.getIssuedQty());
//                            br.setBatchReceivedQty();

                            // Issued qty is the max receivable qty
////                            br.setBatchstock(
////                                    issueT.getIssuedQty() != null
////                                            ? issueT.getIssuedQty().longValue()
////                                            : 0L
//                            );

                            batchResponses.add(br);
                        }
                    }

                    dr.setBatches(batchResponses);
                    itemResponses.add(dr);
                }

                response.setItems(itemResponses);
                response.setReceivingStatus(determineReceivingStatus(indent));
                response.setTotalIssuedQty(calculateTotalIssuedQty(indent));
                response.setTotalReceivedQty(calculateTotalReceivedQty(indent));

                responseList.add(response);
            }

            return ResponseUtils.createSuccessResponse(
                    responseList,
                    new TypeReference<List<StoreInternalIndentResponse>>() {}
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<List<StoreInternalIndentResponse>>() {},
                    "Error fetching indents for receiving: " + e.getMessage(),
                    500
            );
        }
    }




    private String determineReceivingStatus(StoreInternalIndentM indent) {
        List<StoreInternalIndentT> details = indentTRepository.findByIndentM(indent);

        if (details.isEmpty()) {
            return "NOT_STARTED";
        }

        boolean allReceived = true;
        boolean noneReceived = true;

        for (StoreInternalIndentT detail : details) {
            BigDecimal issued = nvl(detail.getIssuedQty());
            BigDecimal received = nvl(detail.getReceivedQty());

            if (received.compareTo(BigDecimal.ZERO) > 0) {
                noneReceived = false;
            }

            if (issued.compareTo(BigDecimal.ZERO) > 0 && received.compareTo(issued) < 0) {
                allReceived = false;
            }
        }

        if (allReceived && !noneReceived) {
            return "FULLY_RECEIVED";
        } else if (!noneReceived && !allReceived) {
            return "PARTIALLY_RECEIVED";
        } else {
            return "NOT_STARTED";
        }
    }

    // Helper to calculate total received quantity
    private BigDecimal calculateTotalReceivedQty(StoreInternalIndentM indent) {
        List<StoreInternalIndentT> details = indentTRepository.findByIndentM(indent);
        return details.stream()
                .map(d -> nvl(d.getReceivedQty()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Helper to calculate total issued quantity
    private BigDecimal calculateTotalIssuedQty(StoreInternalIndentM indent) {
        List<StoreInternalIndentT> details = indentTRepository.findByIndentM(indent);
        return details.stream()
                .map(d -> nvl(d.getIssuedQty()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }







    // Add this method to StoreInternalIndentServiceImpl
    @Override
    @Transactional
    public ApiResponse<StoreIndentReceiveResponse> saveReceiving(StoreIndentReceiveRequest request) {
        try {
            // Validate input
            if (request.getIndentMId() == null) {
                throw new RuntimeException("Indent Master ID is required");
            }

            if (request.getItems() == null || request.getItems().isEmpty()) {
                throw new RuntimeException("At least one item must be received");
            }

            // Load indent master
            StoreInternalIndentM indentM = indentMRepository.findById(request.getIndentMId())
                    .orElseThrow(() -> new RuntimeException("Indent not found with ID: " + request.getIndentMId()));

            // Check if already received
            if (receiveMRepository.existsByStoreInternalIndent(indentM)) {
                throw new RuntimeException("This indent has already been received");
            }

            // Get current user
            User currentUser = authUtil.getCurrentUser();
            String currentUserName = currentUser != null ? currentUser.getFirstName() : "";

            // Get current department (receiving department)
            Long receivingDeptId = authUtil.getCurrentDepartmentId();
            MasDepartment receivingDept = masDepartmentRepository.findById(receivingDeptId)
                    .orElseThrow(() -> new RuntimeException("Receiving department not found"));

            // Get store department (issuing department)
            MasDepartment storeDept = indentM.getToDeptId();

            // ==============================================
            // 1. Create Store Indent Receive Master
            // ==============================================
            StoreIndentReceiveM receiveM = new StoreIndentReceiveM();
            receiveM.setStoreInternalIndent(indentM);
            receiveM.setReceivedDate(request.getReceivingDate() != null ?
                    request.getReceivingDate() : LocalDateTime.now());
            receiveM.setReceivedBy(currentUserName);
            receiveM.setRemarks(request.getRemarks());
            receiveM.setStatus("R");
            receiveM.setReceivedDepartment(receivingDept);
            receiveM.setStoreDepartment(storeDept);
            receiveM.setCreatedBy(currentUserName);
            receiveM.setLastUpdateDate(LocalDateTime.now());

            // Check if any rejection exists to determine is_return
            boolean hasRejections = request.getItems().stream()
                    .anyMatch(item -> item.getQtyRejected() != null &&
                            item.getQtyRejected().compareTo(BigDecimal.ZERO) > 0);
            receiveM.setIsReturn(hasRejections ? "N" : "Y");

            receiveM = receiveMRepository.save(receiveM);

            // Track if we need to create returns
            boolean createReturn = false;
            List<StoreReturnItemDetail> returnItems = new ArrayList<>();

            // ==============================================
            // 2. Process each item for receiving
            // ==============================================
            for (StoreIndentReceiveItemRequest itemReq : request.getItems()) {
                // Load indent detail
                StoreInternalIndentT indentT = indentTRepository.findById(itemReq.getIndentTId())
                        .orElseThrow(() -> new RuntimeException("Indent detail not found: " + itemReq.getIndentTId()));

                // Validate indent belongs to the master
                if (!indentT.getIndentM().getIndentMId().equals(indentM.getIndentMId())) {
                    throw new RuntimeException("Indent detail does not belong to this indent master");
                }

                BigDecimal qtyIssued = nvl(itemReq.getQtyIssued());
                BigDecimal qtyReceived = nvl(itemReq.getQtyReceived());
                BigDecimal qtyRejected = nvl(itemReq.getQtyRejected());

                // Validate quantities
                if (qtyReceived.compareTo(BigDecimal.ZERO) < 0 || qtyRejected.compareTo(BigDecimal.ZERO) < 0) {
                    throw new RuntimeException("Quantities cannot be negative");
                }

                BigDecimal totalReceived = qtyReceived.add(qtyRejected);
                if (totalReceived.compareTo(qtyIssued) > 0) {
                    throw new RuntimeException("Total received + rejected cannot exceed issued quantity for item: " +
                            indentT.getItemId().getNomenclature());
                }

                // Get the corresponding issue transaction
                List<StoreIssueT> issueTs = storeIssueTRepository.findByIndentTId(indentT);
                if (issueTs.isEmpty()) {
                    throw new RuntimeException("No issue transaction found for item: " +
                            indentT.getItemId().getNomenclature());
                }

                // For simplicity, assume first issue transaction
                StoreIssueT issueT = issueTs.get(0);

                // ==============================================
                // 3. Create Store Indent Receive Transaction
                // ==============================================
                StoreIndentReceiveT receiveT = new StoreIndentReceiveT();
                receiveT.setStoreIndentReceiveM(receiveM);
                receiveT.setStoreInternalIndentT(indentT);
                receiveT.setStoreIssueT(issueT);
                receiveT.setItem(indentT.getItemId());

                receiveT.setBatchNo(issueT.getBatchNo());
                receiveT.setExpiryDate(issueT.getExpiryDate());
                receiveT.setBrandName(issueT.getBrandname());
                receiveT.setManufacturerName(issueT.getManufacturername());
                receiveT.setIssuedQty(qtyIssued);
                receiveT.setReceivedQty(qtyReceived);
                receiveT.setRejectedQty(qtyRejected);
//                receiveT.setRejectionReason(itemReq.getRejectionReason());
                receiveT.setCreatedBy(currentUserName);
                receiveT.setLastUpdateDate(LocalDateTime.now());
                receiveTRepository.save(receiveT);

                // ==============================================
                // 4. Update indent detail with received quantity
                // ==============================================
                BigDecimal previousReceived = nvl(indentT.getReceivedQty());
                BigDecimal newTotalReceived = previousReceived.add(qtyReceived);
                indentT.setReceivedQty(newTotalReceived);
                indentTRepository.save(indentT);

                // ==============================================
                // 5. Update batch stock if received quantity > 0
                // ==============================================
                if (qtyReceived.compareTo(BigDecimal.ZERO) > 0) {
                    updateBatchStockForReceiving(indentT, issueT, qtyReceived, currentUserName);
                }

                // ==============================================
                // 6. Handle rejected items (prepare for return)
                // ==============================================
                if (qtyRejected.compareTo(BigDecimal.ZERO) > 0) {
                    createReturn = true;
                    returnItems.add(new StoreReturnItemDetail(
                            receiveT,
                            issueT,
                            indentT.getItemId(),
                            issueT.getStockId(),
                            qtyRejected
//                            itemReq.getRejectionReason()
                    ));
                }

                // ==============================================
                // 7. Create ledger entry for received quantity
                // ==============================================
                if (qtyReceived.compareTo(BigDecimal.ZERO) > 0) {
                    createReceivingLedgerEntry(
                            qtyReceived,
                            indentT.getIndentTId(),
                            issueT.getStockId().getStockId(),
                            "RECEIVED AGAINST ISSUE NO: " + indentM.getIssueNo(),
                            currentUserName
                    );
                }
            }

            // ==============================================
            // 8. Update indent master status and receiving info
            // ==============================================
            indentM.setReceivedBy(currentUserName);
            indentM.setReceivedDate(LocalDateTime.now());
//            boolean hasRejections = request.getItems().stream()
//                    .anyMatch(item -> item.getQtyRejected() != null &&
//                            item.getQtyRejected().compareTo(BigDecimal.ZERO) > 0);
            indentM.setIsReturn(hasRejections ? "N" : "Y");

            indentMRepository.save(indentM);

            // ==============================================
            // 9. Create returns if needed
            // ==============================================
            if (createReturn && !returnItems.isEmpty()) {
                createStoreReturn(receiveM, returnItems, currentUserName);
            }

            // ==============================================
            // 10. Build response
            // ==============================================
            StoreIndentReceiveResponse response = new StoreIndentReceiveResponse();
            response.setReceiveMId(receiveM.getReceiveMId());
            response.setIndentNo(indentM.getIndentNo());
            response.setIssueNo(indentM.getIssueNo());
            response.setReceivedDate(receiveM.getReceivedDate());
            response.setReceivedBy(receiveM.getReceivedBy());
            response.setStatus(receiveM.getStatus());
            response.setIsReturn(receiveM.getIsReturn());
            response.setMessage("Receiving saved successfully!");

            if (createReturn) {
                response.setReturnCreated(true);
                response.setReturnMessage("Return created for rejected items");
            }

            return ResponseUtils.createSuccessResponse(
                    response,
                    new TypeReference<StoreIndentReceiveResponse>() {}
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<StoreIndentReceiveResponse>() {},
                    "Error saving receiving: " + e.getMessage(),
                    400
            );
        }
    }

    // Helper class for return items
    private static class StoreReturnItemDetail {
        StoreIndentReceiveT receiveT;
        StoreIssueT issueT;
        MasStoreItem item;
        StoreItemBatchStock stock;
        BigDecimal rejectedQty;
        String rejectionReason;

        public StoreReturnItemDetail(StoreIndentReceiveT receiveT, StoreIssueT issueT,
                                     MasStoreItem item, StoreItemBatchStock stock,
                                     BigDecimal rejectedQty) {
            this.receiveT = receiveT;
            this.issueT = issueT;
            this.item = item;
            this.stock = stock;
            this.rejectedQty = rejectedQty;
//            this.rejectionReason = rejectionReason;
        }
    }

    // Update batch stock for received quantity
    private void updateBatchStockForReceiving(StoreInternalIndentT indentT,
                                              StoreIssueT issueT, BigDecimal qtyReceived, String userName) {

        StoreItemBatchStock stock = issueT.getStockId();
        if (stock == null) {
            // If no specific stock record, find the batch in receiving department
            Long receivingDeptId = authUtil.getCurrentDepartmentId();
            MasDepartment receivingDept = masDepartmentRepository.findById(receivingDeptId).orElse(null);

            if (receivingDept == null) return;

            List<StoreItemBatchStock> stocks = batchStockRepository.findByDepartmentIdAndItemId(
                    receivingDept,
                    indentT.getItemId()
            );

            if (!stocks.isEmpty()) {
                stock = stocks.get(0); // Use first batch
            }
        }

        if (stock != null) {
            Long currentStock = stock.getClosingStock() != null ? stock.getClosingStock() : 0L;
            Long newStock = currentStock + qtyReceived.longValue();
            stock.setClosingStock(newStock);
            stock.setIndentReceivedQty((stock.getIndentReceivedQty() != null ? stock.getIndentReceivedQty() : 0L) +
                    qtyReceived.longValue());
            stock.setLastChgBy(userName);
            stock.setLastChgDate(LocalDateTime.now());
            batchStockRepository.save(stock);
        }
    }

    // Create ledger entry for receiving
    private void createReceivingLedgerEntry(BigDecimal qty, Long indentTId, Long stockId,
                                            String remarks, String userName) {

        StoreItemBatchStock stock = batchStockRepository.findById(stockId)
                .orElseThrow(() -> new EntityNotFoundException("Stock with ID " + stockId + " not found."));

        StoreStockLedger ledger = new StoreStockLedger();
        ledger.setCreatedDt(LocalDateTime.now());
        ledger.setCreatedBy(userName);
        ledger.setTxnDate(LocalDate.now());
        ledger.setQtyIn(qty);
        ledger.setQtyOut(null);
        ledger.setStockId(stock);
        ledger.setTxnType("RECEIVED");
        ledger.setRemarks(remarks);
        ledger.setTxnReferenceId(indentTId);

        storeStockLedgerRepository.save(ledger);
    }

    // Check if all items are fully received
    private boolean checkAllItemsFullyReceived(StoreInternalIndentM indentM) {
        List<StoreInternalIndentT> items = indentTRepository.findByIndentM(indentM);

        for (StoreInternalIndentT item : items) {
            BigDecimal issued = nvl(item.getIssuedQty());
            BigDecimal received = nvl(item.getReceivedQty());

            if (issued.compareTo(BigDecimal.ZERO) > 0 && received.compareTo(issued) < 0) {
                return false; // Not fully received
            }
        }
        return true;
    }

    // Create store return for rejected items
    private void createStoreReturn(StoreIndentReceiveM receiveM,
                                   List<StoreReturnItemDetail> returnItems, String userName) {

        // ==============================================
        // 1. Create Store Return Master
        // ==============================================
        StoreReturnM returnM = new StoreReturnM();
        returnM.setStoreIndentReceiveM(receiveM);
        returnM.setStoreDepartment(receiveM.getStoreDepartment());
        returnM.setReturnDate(LocalDateTime.now());
        returnM.setReturnedBy(userName);
        returnM.setLastUpdatedBy(userName);
        returnM.setReceivedBy(null); // Will be set when store accepts return
        returnM.setRemarks(receiveM.getRemarks());

        returnM.setStatus("N");
        returnM.setCreatedBy(userName);
        returnM.setLastUpdateDate(LocalDateTime.now());
        returnM = returnMRepository.save(returnM);

        // ==============================================
        // 2. Create Store Return Transactions
        // ==============================================
        for (StoreReturnItemDetail itemDetail : returnItems) {
            StoreReturnT returnT = new StoreReturnT();

            returnT.setStoreReturnM(returnM);
            returnT.setStoreIssueT(itemDetail.issueT);
            returnT.setStoreIndentReceiveT(itemDetail.receiveT);
            returnT.setStoreItemBatchStock(itemDetail.stock);
            returnT.setMasStoreItem(itemDetail.item);
            returnT.setBatchNo(itemDetail.issueT.getBatchNo());
            returnT.setExpiryDate(itemDetail.issueT.getExpiryDate());
            returnT.setDom(itemDetail.issueT.getDom());
            returnT.setBrandName(itemDetail.issueT.getBrandname());
            returnT.setManufacturerName(itemDetail.issueT.getManufacturername());



            returnT.setRejectedQty(
                    itemDetail.rejectedQty != null ? itemDetail.rejectedQty : BigDecimal.ZERO
            );
//            returnT.setRejectionReason(itemDetail.rejectionReason);
            returnT.setCreatedBy(userName);
            returnT.setLastUpdateDate(LocalDateTime.now());

            returnTRepository.save(returnT);
        }

        // ==============================================
        // 3. Update receive master to indicate return exists
        // ==============================================
        receiveM.setIsReturn("N");
        receiveMRepository.save(receiveM);
    }
















//=============================================== fully and partial================================

    @Override
    @Transactional
    public ApiResponse<StoreInternalIndentResponse> issueIndent(StoreInternalIssueRequest request) {
        try {
            // === Validate ===
            if (request.getIndentMId() == null) {
                throw new RuntimeException("Indent Master ID is required");
            }

            if (request.getItems() == null || request.getItems().isEmpty()) {
                throw new RuntimeException("At least one item must be issued");
            }

            // === Load Master ===
            StoreInternalIndentM indentM = indentMRepository.findById(request.getIndentMId())
                    .orElseThrow(() -> new RuntimeException("Indent not found"));

            // === Generate Issue No ===
            String issueNo = generateIssueNumber();

            // === Current User ===
            String userName = authUtil.getCurrentUser().getFirstName();

            // ============================================================
            // === CREATE STORE_ISSUE_M ===================================
            // ============================================================
            StoreIssueM issueM = new StoreIssueM();
            issueM.setIssueNo(issueNo);
            issueM.setIssueDate(LocalDateTime.now());
            issueM.setIssuedDate(LocalDateTime.now());
            issueM.setToDeptId(indentM.getToDeptId());
            issueM.setFromStoreId(indentM.getFromDeptId());
            issueM.setHospitalId(indentM.getToDeptId().getHospital());
            issueM.setIndentMId(indentM);
            issueM.setIssuedBy(userName);
            issueM.setStatus("I"); // Issued

            issueM = storeIssueMRepository.save(issueM);

            // Track issued items
            boolean anyItemIssued = false;

            // ============================================================
            // === PROCESS EACH ITEM =====================================
            // ============================================================
            for (StoreInternalIssueDetailRequest itemReq : request.getItems()) {
                StoreInternalIndentT indentT = indentTRepository.findById(itemReq.getIndentTId())
                        .orElseThrow(() -> new RuntimeException("Indent detail not found: " + itemReq.getIndentTId()));

                if (!indentT.getIndentM().getIndentMId().equals(indentM.getIndentMId())) {
                    throw new RuntimeException("Indent detail does not belong to indent master");
                }

                BigDecimal approved = nvl(indentT.getApprovedQty());
                BigDecimal prevIssued = nvl(indentT.getIssuedQty());
                BigDecimal newIssue = nvl(itemReq.getIssuedQty());

                // === FIX: Calculate available stock from database (NOT from frontend) ===
                List<StoreItemBatchStock> allBatches =
                        batchStockRepository.findByDepartmentIdAndItemId(indentM.getToDeptId(), indentT.getItemId());

                BigDecimal actualAvailableStock = BigDecimal.ZERO;
                if (allBatches != null && !allBatches.isEmpty()) {
                    actualAvailableStock = allBatches.stream()
                            .map(b -> nvl(BigDecimal.valueOf(b.getClosingStock())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                }

                // === ITEMS NOT ISSUED (qtyIssued = 0) ===
                if (newIssue.compareTo(BigDecimal.ZERO) <= 0) {
                    // Item not issued - update with actual available stock
                    indentT.setAvailableStock(actualAvailableStock);
                    indentT.setIssueStatus("N"); // Not issued
                    indentTRepository.save(indentT);
                    continue; // Skip to next item
                }

                // === ITEMS TO BE ISSUED (qtyIssued > 0) ===
                BigDecimal remainingApproved = approved.subtract(prevIssued);

                // Rule 1: Must issue full remaining quantity if issuing
                if (newIssue.compareTo(remainingApproved) != 0) {
                    throw new RuntimeException("Must issue full remaining quantity for item " +
                            indentT.getItemId().getNomenclature() + ". Remaining: " + remainingApproved +
                            ", Trying to issue: " + newIssue);
                }

                // Rule 2: Check stock availability for full issue (using actual stock)
                if (actualAvailableStock.compareTo(remainingApproved) < 0) {
                    // Insufficient stock for full issue - don't issue this item
                    // But still update available stock
                    indentT.setAvailableStock(actualAvailableStock);
                    indentT.setIssueStatus("N");
                    indentTRepository.save(indentT);
                    continue;
                }

                // Rule 3: Should not exceed approved
                if (newIssue.compareTo(remainingApproved) > 0) {
                    throw new RuntimeException("Issuing more than approved quantity");
                }

                // === Get batch stock (FEFO) ===
                List<StoreItemBatchStock> batchList =
                        batchStockRepository.findByDepartmentIdAndItemId(indentM.getToDeptId(), indentT.getItemId());

                if (batchList == null || batchList.isEmpty()) {
                    throw new RuntimeException("Stock not available for item " + indentT.getItemId().getNomenclature());
                }

                batchList.sort(Comparator.comparing(StoreItemBatchStock::getExpiryDate)); // FEFO

                long requiredQty = newIssue.longValue();
                long remainingQty = requiredQty;

                // ============================================================
                // === ISSUE STOCK FEFO + CREATE ISSUE_T ======================
                // ============================================================
                for (StoreItemBatchStock batch : batchList) {
                    if (remainingQty <= 0) break;

                    long closing = batch.getClosingStock() == null ? 0L : batch.getClosingStock();
                    if (closing <= 0) continue;

                    long qtyToIssue = Math.min(closing, remainingQty);

                    // --- Update batch stock ---
                    batch.setClosingStock(closing - qtyToIssue);
                    batch.setIndentIssueQty((batch.getIndentIssueQty() == null ? 0 : batch.getIndentIssueQty()) + qtyToIssue);
                    batch.setLastChgBy(userName);
                    batch.setLastChgDate(LocalDateTime.now());
                    batchStockRepository.save(batch);

                    // === STORE_ISSUE_T ENTRY ===
                    StoreIssueT issueT = new StoreIssueT();
                    issueT.setStoreIssueMId(issueM);
                    issueT.setItemId(indentT.getItemId());
                    issueT.setIndentTId(indentT);
                    issueT.setStockId(batch);
                    issueT.setIssuedQty(BigDecimal.valueOf(qtyToIssue));
                    issueT.setBatchNo(batch.getBatchNo());
                    issueT.setExpiryDate(batch.getExpiryDate());
                    issueT.setDom(batch.getManufactureDate());
                    issueT.setManufacturername(batch.getManufacturerId().getManufacturerName());
                    issueT.setBrandname(batch.getBrandId().getBrandName());
                    issueT.setStatus("I");
                    issueT.setUnitPrice(nvl(batch.getMrpPerUnit()));

                    storeIssueTRepository.save(issueT);

                    // === Ledger ===
                    transferOutLedger(
                            qtyToIssue,
                            indentT.getIndentTId(),
                            batch.getStockId(),
                            "ISSUE AGAINST INDENT NO: " + indentM.getIndentNo()
                    );

                    remainingQty -= qtyToIssue;
                }

                if (remainingQty > 0) {
                    throw new RuntimeException("Insufficient stock for item " +
                            indentT.getItemId().getNomenclature() + ". Required: " + requiredQty);
                }

                // === Update issued qty ===
                BigDecimal newTotalIssued = prevIssued.add(newIssue);
                indentT.setIssuedQty(newTotalIssued);

                // === FIX: Calculate NEW available stock after issuance ===
                BigDecimal newAvailableStock = actualAvailableStock.subtract(newIssue);
                indentT.setAvailableStock(newAvailableStock);

                // === Set item issue status ===
                if (approved.compareTo(BigDecimal.ZERO) == 0) {
                    indentT.setIssueStatus("N"); // Not applicable
                } else {
                    indentT.setIssueStatus("Y"); // Yes, fully issued
                }

                indentTRepository.save(indentT);
                anyItemIssued = true;
            }

            if (!anyItemIssued) {
                throw new RuntimeException("No items were issued");
            }

            // ============================================================
            // === UPDATE MASTER STATUS ===================================
            // ============================================================
            // ALWAYS set to "FI" if any items were issued
            indentM.setStatus("FI"); // Fully Issued
            indentM.setStoreIssueMId(issueM);

            indentM.setIssuedBy(userName);
            indentM.setIssuedDate(LocalDateTime.now());
            indentM.setIssueNo(issueNo);
            indentMRepository.save(indentM);

            StoreInternalIndentResponse resp = buildResponse(indentM);

            return ResponseUtils.createSuccessResponse(
                    resp,
                    new TypeReference<StoreInternalIndentResponse>() {}
            );

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<StoreInternalIndentResponse>() {},
                    e.getMessage(),
                    400
            );
        }
    }

    // Helper method
    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }






    // === Helper method to generate issue number ===
    private String generateIssueNumber() {
        // Option 1: Simple timestamp-based
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ISS-" + timestamp;

        // Option 2: Sequential number (you'd need to query the last issue number)
        // Long lastIssueNumber = issueRepository.findMaxIssueNumber();
        // return "ISS-" + String.format("%06d", (lastIssueNumber == null ? 1 : lastIssueNumber + 1));

        // Option 3: UUID
        // return "ISS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }






    // ---------- Helpers ----------
    private String generateIndentNo() {
        Optional<StoreInternalIndentM> last = indentMRepository.findTopByOrderByIndentMIdDesc();
        long nextId = last.map(m -> m.getIndentMId() + 1).orElse(1L);
        return "IND-" + nextId;
    }


    // New method that calculates available stock based on login department
    private StoreInternalIndentResponse buildResponseWithLoginDept(StoreInternalIndentM m, Long loginDeptId) {
        StoreInternalIndentResponse res = buildSimpleResponse(m);
        List<StoreInternalIndentT> details = indentTRepository.findByIndentM(m);
        List<StoreInternalIndentDetailResponse> dList = new ArrayList<>();

        for (StoreInternalIndentT d : details) {
            StoreInternalIndentDetailResponse dr = new StoreInternalIndentDetailResponse();
            dr.setIndentTId(d.getIndentTId());
            if (d.getItemId() != null) {
                dr.setItemId(d.getItemId().getItemId());
                dr.setItemName(d.getItemId().getNomenclature());
                dr.setPvmsNo(d.getItemId().getPvmsNo());
                dr.setUnitAuName(d.getItemId().getUnitAU().getUnitName());
                dr.setUnitAUid(d.getItemId().getUnitAU().getUnitId());
            }
            dr.setRequestedQty(d.getRequestedQty());
            dr.setApprovedQty(d.getApprovedQty());
            dr.setIssuedQty(d.getIssuedQty());
            dr.setReceivedQty(d.getReceivedQty());

            // ✅ DYNAMIC CALCULATION: Calculate available stock based on login department
            if (d.getItemId() != null) {
                BigDecimal currentStock = calculateCurrentStockForDept(
                        d.getItemId().getItemId(),
                        loginDeptId
                );
                dr.setAvailableStock(currentStock);
            } else {
                dr.setAvailableStock(d.getAvailableStock()); // fallback to stored value
            }

            dr.setItemCost(d.getItemCost());
            dr.setTotalCost(d.getTotalCost());
            dr.setIssueStatus(d.getIssueStatus());
            dr.setReason(d.getReason());

            dList.add(dr);
        }
        res.setItems(dList);
        return res;
    }

    // Helper method to calculate current stock for a department
    private BigDecimal calculateCurrentStockForDept(Long itemId, Long departmentId) {
        try {
            LocalDate today = LocalDate.now();
            List<StoreItemBatchStock> validBatches =
                    storeItemBatchStockRepository.findNonExpiredBatchesForROL(
                            itemId,
                            departmentId,
                            today
                    );

            // Sum all batch stocks
            Long totalStock = validBatches.stream()
                    .map(batch -> batch.getClosingStock() != null ? batch.getClosingStock() : 0L)
                    .mapToLong(Long::longValue)
                    .sum();

            return BigDecimal.valueOf(totalStock);
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }



    private StoreInternalIndentResponse buildResponse(StoreInternalIndentM m) {
        StoreInternalIndentResponse res = buildSimpleResponse(m);
        List<StoreInternalIndentT> details = indentTRepository.findByIndentM(m);
        List<StoreInternalIndentDetailResponse> dList = new ArrayList<>();
        for (StoreInternalIndentT d : details) {
            StoreInternalIndentDetailResponse dr = new StoreInternalIndentDetailResponse();
            dr.setIndentTId(d.getIndentTId());
            if (d.getItemId() != null) {
                dr.setItemId(d.getItemId().getItemId());
                dr.setItemName(d.getItemId().getNomenclature());
                dr.setPvmsNo(d.getItemId().getPvmsNo());
            }
            dr.setRequestedQty(d.getRequestedQty());
            dr.setApprovedQty(d.getApprovedQty());
            dr.setIssuedQty(d.getIssuedQty());
            dr.setReceivedQty(d.getReceivedQty());
            dr.setAvailableStock(d.getAvailableStock());
            dr.setItemCost(d.getItemCost());
            dr.setTotalCost(d.getTotalCost());
            dr.setIssueStatus(d.getIssueStatus());
            dr.setReason(d.getReason());
            dr.setUnitAuName(d.getItemId().getUnitAU().getUnitName());
            dr.setUnitAUid(d.getItemId().getUnitAU().getUnitId());

            dList.add(dr);
        }
        res.setItems(dList);
        return res;
    }

    private StoreInternalIndentResponse buildSimpleResponse(StoreInternalIndentM m) {
        StoreInternalIndentResponse res = new StoreInternalIndentResponse();
        res.setIndentMId(m.getIndentMId());
        res.setIndentNo(m.getIndentNo());
        res.setIndentDate(m.getIndentDate());
        if (m.getFromDeptId() != null) {
            res.setFromDeptId(m.getFromDeptId().getId());
            res.setFromDeptName(m.getFromDeptId().getDepartmentName());
        }
        if (m.getToDeptId() != null) {
            res.setToDeptId(m.getToDeptId().getId());
            res.setToDeptName(m.getToDeptId().getDepartmentName());
        }
        res.setTotalCost(m.getTotalCost());
        res.setStatus(m.getStatus());
        res.setCreatedBy(m.getCreatedBy());
        res.setCreatedDate(m.getCreatedDate());
        res.setApprovedBy(m.getApprovedBy());
        res.setApprovedDate(m.getApprovedDate());
        res.setStoreApprovedBy(m.getStoreApprovedBy());
        res.setStoreApprovedDate(m.getStoreApprovedDate());
        res.setIssuedBy(m.getIssuedBy());
        res.setIssuedDate(m.getIssuedDate());
        res.setReceivedBy(m.getReceivedBy());
        res.setReceivedDate(m.getReceivedDate());
        res.setIssueNo(m.getIssueNo());
        res.setRemark(m.getRemarks()); // Add remarks to response

        return res;
    }

    // ===================================== department hardcode =======================
    private List<Long> getFixedDeptIds() {
        return Arrays.stream(fixedDepartmentsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    public List<MasDepartment> getOtherFixedDepartmentsForCurrentUser() {
        Long currentDeptId = authUtil.getCurrentDepartmentId();
        List<Long> fixedIds = getFixedDeptIds();

        if (currentDeptId == null) {
            return masDepartmentRepository.findByIdIn(fixedIds);
        }

        if (fixedIds.contains(currentDeptId)) {
            List<Long> targetIds = fixedIds.stream()
                    .filter(id -> !id.equals(currentDeptId))
                    .collect(Collectors.toList());
            return masDepartmentRepository.findByIdIn(targetIds);
        }

        return masDepartmentRepository.findByIdIn(fixedIds);
    }

    // ================================Rol=============================
//    @Override
//    @Transactional(readOnly = true)
//    public ApiResponse<List<ROLItemResponse>> getROLItems() {
//        try {
//            Long currentDeptId = authUtil.getCurrentDepartmentId();
//            Long hospitalId = authUtil.getCurrentUser().getHospital().getId();
//
//            if (currentDeptId == null) {
//                return ResponseUtils.createFailureResponse(
//                        null,
//                        new TypeReference<List<ROLItemResponse>>() {},
//                        "Current department id not found in token",
//                        400
//                );
//            }
//
//            // Fetch ACTIVE items for this hospital + department
//            List<MasStoreItem> allItems =
//                    masStoreItemRepository.findByStatusIgnoreCaseAndHospitalIdAndDepartmentId(
//                            "y", hospitalId, currentDeptId
//                    );
//
//            List<ROLItemResponse> rolItems = new ArrayList<>();
//
//            for (MasStoreItem item : allItems) {
//                Long currentStock = calculateCurrentStock(item.getItemId(), currentDeptId);
//                Integer reorderLevel = getReorderLevelForDepartment(item, currentDeptId);
//
//                // Calculate stocks for different departments
//                Long storeStock = calculateStockForDepartment(item.getItemId(), departmentConfig.getStoreId());
//                Long wardStock = calculateStockForDepartment(item.getItemId(), departmentConfig.getWardPharmacyId());
//                Long dispStock = calculateStockForDepartment(item.getItemId(), departmentConfig.getDispensaryId());
//
//                if (reorderLevel != null && reorderLevel > 0 && currentStock <= reorderLevel) {
//                    rolItems.add(new ROLItemResponse(item, currentStock, reorderLevel, storeStock, wardStock, dispStock));
//                }
//            }
//
//            return ResponseUtils.createSuccessResponse(
//                    rolItems,
//                    new TypeReference<List<ROLItemResponse>>() {}
//            );
//        } catch (Exception e) {
//            return ResponseUtils.createFailureResponse(
//                    null,
//                    new TypeReference<List<ROLItemResponse>>() {},
//                    "Error fetching ROL items: " + e.getMessage(),
//                    500
//            );
//        }
//    }


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ROLItemResponse>> getROLItems() {
        try {
            Long currentDeptId = authUtil.getCurrentDepartmentId();
            Long hospitalId = authUtil.getCurrentUser().getHospital().getId();

            if (currentDeptId == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<List<ROLItemResponse>>() {},
                        "Current department id not found in token",
                        400
                );
            }

            // Get items that were EVER indented by this department
            List<Long> indentedItemIds = getIndentedItemIds(currentDeptId);

            System.out.println("Excluding " + indentedItemIds.size() + " previously indented items from ROL");

            // Fetch ACTIVE items for this hospital + department
            List<MasStoreItem> allItems =
                    masStoreItemRepository.findByStatusIgnoreCase(
                            "y"
                    );

            List<ROLItemResponse> rolItems = new ArrayList<>();

            for (MasStoreItem item : allItems) {
                // Skip if this item was EVER indented by this department
                if (indentedItemIds.contains(item.getItemId())) {
                    continue;
                }

                Long currentStock = calculateCurrentStock(item.getItemId(), currentDeptId);
                Integer reorderLevel = getReorderLevelForDepartment(item, currentDeptId);

                // Calculate stocks for different departments
                Long storeStock = calculateStockForDepartment(item.getItemId(), departmentConfig.getStoreId());
                Long wardStock = calculateStockForDepartment(item.getItemId(), departmentConfig.getWardPharmacyId());
                Long dispStock = calculateStockForDepartment(item.getItemId(), departmentConfig.getDispensaryId());

                if (reorderLevel != null && reorderLevel > 0 && currentStock <= reorderLevel) {
                    rolItems.add(new ROLItemResponse(item, currentStock, reorderLevel, storeStock, wardStock, dispStock));
                }
            }

            System.out.println("Returning " + rolItems.size() + " ROL items after permanent filtering");

            return ResponseUtils.createSuccessResponse(
                    rolItems,
                    new TypeReference<List<ROLItemResponse>>() {}
            );
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<List<ROLItemResponse>>() {},
                    "Error fetching ROL items: " + e.getMessage(),
                    500
            );
        }
    }

    private List<Long> getIndentedItemIds(Long departmentId) {
        try {
            List<String> statuses = Arrays.asList("S", "Y");

            List<Long> indentedItems = indentTRepository.findIndentedItemIds(
                    departmentId,
                    statuses
            );

            return indentedItems != null ? indentedItems : new ArrayList<>();

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }





    // Helper method to calculate stock for specific department
    private Long calculateStockForDepartment(Long itemId, Long departmentId) {
        try {
            LocalDate today = LocalDate.now();
            List<StoreItemBatchStock> batches = storeItemBatchStockRepository.findNonExpiredBatchesForROL(
                    itemId, departmentId, today
            );
            return batches.stream()
                    .map(batch -> batch.getClosingStock() != null ? batch.getClosingStock() : 0L)
                    .mapToLong(Long::longValue)
                    .sum();
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    private Long calculateCurrentStock(Long itemId, Long departmentId) {
        try {
            LocalDate today = LocalDate.now();

            List<StoreItemBatchStock> validBatches =
                    storeItemBatchStockRepository.findNonExpiredBatchesForROL(
                            itemId,
                            departmentId,
                            today
                    );

            return validBatches.stream()
                    .map(batch -> batch.getClosingStock() != null ? batch.getClosingStock() : 0L)
                    .mapToLong(Long::longValue)
                    .sum();
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    private Integer getReorderLevelForDepartment(MasStoreItem item, Long departmentId) {
        // ward pharmacy
        if (departmentId.equals(departmentConfig.getWardPharmacyId())) {
            return item.getWardROL() != null ? item.getWardROL().intValue() : null;
        }

        // main store
        if (departmentId.equals(departmentConfig.getStoreId())) {
            return item.getStoreROL() != null ? item.getStoreROL().intValue() : null;
        }

        // dispensary
        if (departmentId.equals(departmentConfig.getDispensaryId())) {
            return item.getDispROL() != null ? item.getDispROL().intValue() : null;
        }

        // general medicine (if you want to treat as storeROL or wardROL)
        if (departmentId.equals(departmentConfig.getGeneralMedicineId())) {
            return item.getStoreROL() != null ? item.getStoreROL().intValue() : null;
        }

        // default fallback
        return item.getStoreROL() != null
                ? item.getStoreROL().intValue()
                : (item.getWardROL() != null ? item.getWardROL().intValue() : null);
    }




//=============================================ledger Entry================================


    private String transferOutLedger(long qty, Long indentTId, Long stockId, String remarks) {

        StoreItemBatchStock stock = storeItemBatchStockRepository.findById(stockId)
                .orElseThrow(() -> new EntityNotFoundException("Stock with ID " + stockId + " not found."));

        StoreStockLedger ledger = new StoreStockLedger();
        ledger.setCreatedDt(LocalDateTime.now());

        User currentUser = authUtil.getCurrentUser();
        String fName = currentUser.getFirstName()
                + (currentUser.getMiddleName() != null ? " " + currentUser.getMiddleName() : "")
                + (currentUser.getLastName() != null ? " " + currentUser.getLastName() : "");

        ledger.setCreatedBy(fName.trim());
        ledger.setTxnDate(LocalDate.now());

        ledger.setQtyOut(BigDecimal.valueOf(qty));
        ledger.setQtyIn(null);

        ledger.setStockId(stock);
        ledger.setTxnType("ISSUE");                 // you can use config if available
        ledger.setRemarks(remarks);
        ledger.setTxnReferenceId(indentTId);

        storeStockLedgerRepository.save(ledger);

        return "success";
    }







}