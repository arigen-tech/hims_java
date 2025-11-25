package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.StoreInternalIndentDetailRequest;
import com.hims.request.StoreInternalIndentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.ROLItemResponse;
import com.hims.response.StoreInternalIndentDetailResponse;
import com.hims.response.StoreInternalIndentResponse;
import com.hims.service.StoreInternalIndentService;
import com.hims.utils.AuthUtil;
import com.hims.utils.DepartmentConfig;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    @Value("${fixed.departments}")
    private String fixedDepartmentsConfig;

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

        // For submitted indents, set approval info
        if ("Y".equals(status)) {
            indentM.setApprovedBy(currentUserName);
            indentM.setApprovedDate(LocalDateTime.now());
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
                detail.setAvailableStock(dReq.getAvailableStock() != null ? dReq.getAvailableStock() : BigDecimal.valueOf(0));
                detail.setReason(dReq.getReason() != null ? dReq.getReason() : "");

                indentTRepository.save(detail);
            }
        }

        // Handle deleted items - FIXED: Properly handle single Long or List<Long>
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
    public ApiResponse<List<StoreInternalIndentResponse>> listIndentsByCurrentDept(String status) {
        Long deptId = authUtil.getCurrentDepartmentId();
        if (deptId == null) {
            throw new RuntimeException("Current department id not found in token");
        }

        List<StoreInternalIndentM> list;
        if (status != null && !status.isEmpty()) {
            list = indentMRepository.findByFromDeptId_IdAndStatus(deptId, status);
        } else {
            list = indentMRepository.findByFromDeptId_Id(deptId);
        }

        List<StoreInternalIndentResponse> respList = new ArrayList<>();
        for (StoreInternalIndentM m : list) {
            respList.add(buildResponse(m));
        }
        return ResponseUtils.createSuccessResponse(respList, new TypeReference<List<StoreInternalIndentResponse>>() {});
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
    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ROLItemResponse>> getROLItems() {
        try {
            Long currentDeptId = authUtil.getCurrentDepartmentId();
            Long hospitalId = authUtil.getCurrentUser().getHospital().getId();

            if (currentDeptId == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<List<ROLItemResponse>>() {},
                        "Current department id not found in token", 400);
            }

            List<MasStoreItem> allItems = masStoreItemRepository.findByHospitalIdAndDepartmentId(hospitalId, currentDeptId);
            List<ROLItemResponse> rolItems = new ArrayList<>();

            for (MasStoreItem item : allItems) {
                Long currentStock = calculateCurrentStock(item.getItemId(), currentDeptId);
                Integer reorderLevel = getReorderLevelForDepartment(item, currentDeptId);

                if (reorderLevel != null && reorderLevel > 0 && currentStock <= reorderLevel) {
                    ROLItemResponse rolItem = new ROLItemResponse(item, currentStock, reorderLevel);
                    rolItems.add(rolItem);
                }
            }

            return ResponseUtils.createSuccessResponse(rolItems, new TypeReference<List<ROLItemResponse>>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<List<ROLItemResponse>>() {},
                    "Error fetching ROL items: " + e.getMessage(), 500);
        }
    }

    private Long calculateCurrentStock(Long itemId, Long departmentId) {
        try {
            LocalDate today = LocalDate.now();
            Integer expiryDays = getExpiryDaysForDepartment(departmentId);
            LocalDate expiryThreshold = today.plusDays(expiryDays);

            List<StoreItemBatchStock> validBatches = storeItemBatchStockRepository
                    .findByItemId_ItemIdAndDepartmentId_IdAndExpiryDateAfter(itemId, departmentId, expiryThreshold);

            return validBatches.stream()
                    .mapToLong(batch -> {
                        Long closingStock = batch.getClosingStock();
                        return closingStock != null ? closingStock : 0L;
                    })
                    .sum();
        } catch (Exception e) {
            return 0L;
        }
    }

    private Integer getReorderLevelForDepartment(MasStoreItem item, Long departmentId) {
        if (departmentId.equals(departmentConfig.getType().getStore())) {
            return item.getReOrderLevelStore();
        } else if (departmentId.equals(departmentConfig.getType().getDispensary())) {
            return item.getReOrderLevelDispensary();
        } else if (departmentId.equals(departmentConfig.getType().getWard())) {
            return item.getWardROL() != null ? item.getWardROL().intValue() : null;
        } else {
            return item.getReOrderLevelStore();
        }
    }

    private Integer getExpiryDaysForDepartment(Long departmentId) {
        if (departmentId.equals(departmentConfig.getType().getStore())) {
            return departmentConfig.getStockExpiry().getStore();
        } else if (departmentId.equals(departmentConfig.getType().getDispensary())) {
            return departmentConfig.getStockExpiry().getDispensary();
        } else if (departmentId.equals(departmentConfig.getType().getWard())) {
            return departmentConfig.getStockExpiry().getWard();
        } else {
            return departmentConfig.getStockExpiry().getGeneral();
        }
    }
}