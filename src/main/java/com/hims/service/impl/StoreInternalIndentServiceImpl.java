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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    StockFound stockFound;


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
            // ✅ use toDeptId instead of fromDeptId
            List<StoreInternalIndentM> indents =
                    indentMRepository.findByToDeptId_IdAndStatus(deptId, "Y");

            // if you did NOT create the OrderBy method above, sort here
            indents.sort(Comparator.comparing(StoreInternalIndentM::getIndentMId).reversed());

            List<StoreInternalIndentResponse> responseList = new ArrayList<>();
            for (StoreInternalIndentM indent : indents) {
                responseList.add(buildResponse(indent));
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
            // ✅ use toDeptId instead of fromDeptId
            List<StoreInternalIndentM> indents =
                    indentMRepository.findByToDeptId_IdAndStatus(deptId, "A");

            indents.sort(Comparator.comparing(StoreInternalIndentM::getIndentMId).reversed());

            List<StoreInternalIndentResponse> responseList = new ArrayList<>();
            for (StoreInternalIndentM indent : indents) {
                responseList.add(buildResponse(indent));
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
                indentM.setIssuedBy(currentUserName);
                indentM.setIssuedDate(LocalDateTime.now());
            } else if ("rejected".equals(action)) {
                // Rejected after approval
                indentM.setStatus("RR");                 // Rejected after approval
                indentM.setApprovedBy(currentUserName);  // who rejected
                indentM.setApprovedDate(LocalDateTime.now());
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







    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<StoreInternalIndentResponse>> getAllIndentsForIssueDepartment(Long deptId) {
        try {

            if (deptId == null) {
                throw new RuntimeException("deptId is required");
            }

            // Fetch only "AA" status indents for issue dept
            List<StoreInternalIndentM> indents =
                    indentMRepository.findByToDeptId_IdAndStatus(deptId, "AA");

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

                    List<StoreItemBatchStock> batchStocks =
                            storeItemBatchStockRepository.findByItemId_ItemId(
                                    detail.getItemId().getItemId()
                            );

                    Long itemId = detail.getItemId().getItemId();
                    Long hospitalId = authUtil.getCurrentUser().getHospital().getId();

                    // Map batches (preserve original behavior except batchstock)
                    List<BatchResponse> batchResponseList = batchStocks.stream().map(batch -> {
                        BatchResponse br = new BatchResponse();
                        br.setBatchNo(batch.getBatchNo());
                        br.setManufactureDate(batch.getManufactureDate());
                        br.setExpiryDate(batch.getExpiryDate());

                        // ===== UPDATED: show stock for the current department (deptId) =====
                        // convert Long deptId -> Integer for stockFound API
                        Integer deptIdAsInt = deptId.intValue();
                        Long currentDeptStock = stockFound.getAvailableStocks(
                                hospitalId,
                                deptIdAsInt,
                                itemId,
                                hospDefinedstoreDays
                        );
                        br.setBatchstock(currentDeptStock);

                        // ===== keep other department stocks as before =====
                        Long avlableStokes = stockFound.getAvailableStocks(
                                hospitalId,
                                deptIdStore,
                                itemId,
                                hospDefinedstoreDays
                        );
                        br.setStorestocks(avlableStokes);

                        Long dispstocks = stockFound.getAvailableStocks(
                                hospitalId,
                                dispdeptId,
                                itemId,
                                hospDefineddispDays
                        );
                        br.setDispstocks(dispstocks);

                        Long wardstocks = stockFound.getAvailableStocks(
                                hospitalId,
                                warddeptId,
                                itemId,
                                hospDefinedwardDays
                        );
                        br.setWardstocks(wardstocks);

                        return br;
                    }).collect(Collectors.toList());

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









    // ---------- Helpers ----------
    private String generateIndentNo() {
        Optional<StoreInternalIndentM> last = indentMRepository.findTopByOrderByIndentMIdDesc();
        long nextId = last.map(m -> m.getIndentMId() + 1).orElse(1L);
        return "IND-" + nextId;
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
}