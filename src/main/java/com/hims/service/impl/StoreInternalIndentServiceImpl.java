package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.StoreInternalIndentDetailRequest;
import com.hims.request.StoreInternalIndentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.StoreInternalIndentDetailResponse;
import com.hims.response.StoreInternalIndentResponse;
import com.hims.service.StoreInternalIndentService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    @Autowired
    AuthUtil authUtil;

    @Value("${fixed.departments}")
    private String fixedDepartmentsConfig;

    // Save (draft). Backend will set status = "S"
    @Override
    @Transactional
    public ApiResponse<StoreInternalIndentResponse> saveIndent(StoreInternalIndentRequest request) {
        StoreInternalIndentM indentM;
        boolean isNew = (request.getIndentMId() == null);

        // current user & username
        User currentUser = authUtil.getCurrentUser();
        String currentUserName = currentUser != null ? currentUser.getFirstName() : ""; // adjust getter if needed

        if (isNew) {
            indentM = new StoreInternalIndentM();
            indentM.setCreatedDate(LocalDateTime.now());
            indentM.setCreatedBy(currentUserName);
            indentM.setIndentDate(
                    request.getIndentDate() != null ? request.getIndentDate() : LocalDateTime.now()
            );
            indentM.setIndentNo(
                    request.getIndentNo() == null ? generateIndentNo() : request.getIndentNo()
            );

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
            // do not change createdBy/createdDate
        }

        // to department
        if (request.getToDeptId() != null) {
            MasDepartment toDept = masDepartmentRepository.findById(request.getToDeptId())
                    .orElseThrow(() -> new RuntimeException("To department not found"));
            indentM.setToDeptId(toDept);
        } else {
            indentM.setToDeptId(null);
        }

        // Backend sets draft status
        indentM.setStatus("S");

        // Save header first to get id
        indentM = indentMRepository.save(indentM);

        // Delete previous details (simple approach) and reinsert
        List<StoreInternalIndentT> prev = indentTRepository.findByIndentM(indentM);
        if (prev != null && !prev.isEmpty()) {
            indentTRepository.deleteAll(prev);
        }

        BigDecimal totalCost = BigDecimal.ZERO;

        if (request.getItems() != null) {
            for (StoreInternalIndentDetailRequest dReq : request.getItems()) {
                StoreInternalIndentT detail = new StoreInternalIndentT();
                detail.setIndentM(indentM);

                MasStoreItem item = masStoreItemRepository.findById(dReq.getItemId())
                        .orElseThrow(() -> new RuntimeException("Item not found: " + dReq.getItemId()));
                detail.setItemId(item);

                detail.setRequestedQty(dReq.getRequestedQty());
                detail.setApprovedQty(dReq.getApprovedQty());
                detail.setIssuedQty(dReq.getIssuedQty());
                detail.setReceivedQty(dReq.getReceivedQty());
                detail.setAvailableStock(dReq.getAvailableStock());
                detail.setItemCost(dReq.getItemCost());

                BigDecimal lineTotal = dReq.getTotalCost();
                if (lineTotal == null && dReq.getItemCost() != null && dReq.getRequestedQty() != null) {
                    lineTotal = dReq.getItemCost().multiply(dReq.getRequestedQty());
                }
                detail.setTotalCost(lineTotal);
                detail.setIssueStatus(dReq.getIssueStatus());

                if (lineTotal != null) {
                    totalCost = totalCost.add(lineTotal);
                }

                indentTRepository.save(detail);
            }
        }

        indentM.setTotalCost(totalCost);
        indentMRepository.save(indentM);

        StoreInternalIndentResponse resp = buildResponse(indentM);
        return ResponseUtils.createSuccessResponse(resp, new TypeReference<StoreInternalIndentResponse>() {});
    }

    // Submit indent — backend sets status = "Y"
    @Override
    @Transactional
    public ApiResponse<StoreInternalIndentResponse> submitIndent(Long indentMId) {
        StoreInternalIndentM indentM = indentMRepository.findById(indentMId)
                .orElseThrow(() -> new RuntimeException("Indent not found"));

        User currentUser = authUtil.getCurrentUser();
        String currentUserName = currentUser != null ? currentUser.getFirstName() : "";

        // change status to Submitted
        indentM.setStatus("Y");
        indentM.setApprovedBy(currentUserName);
        indentM.setApprovedDate(LocalDateTime.now());

        indentM = indentMRepository.save(indentM);

        StoreInternalIndentResponse resp = buildResponse(indentM);
        return ResponseUtils.createSuccessResponse(resp, new TypeReference<StoreInternalIndentResponse>() {});
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
            respList.add(buildSimpleResponse(m));
        }
        return ResponseUtils.createSuccessResponse(respList, new TypeReference<List<StoreInternalIndentResponse>>() {});
    }

    // Create from ROL - simple implementation: accepts items on request or build from stock checks
    @Override
    @Transactional
    public ApiResponse<StoreInternalIndentResponse> createIndentFromROL(StoreInternalIndentRequest baseRequest) {
        baseRequest.setSourceType("R");
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
            // adapt getters if your MasDepartment uses different names
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

//    =====================================department hardcode =======================



    private List<Long> getFixedDeptIds() {
        return Arrays.stream(fixedDepartmentsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * Returns the other departments from fixed.departments
     * excluding the logged-in user's department (if it is one of them).
     */
    public List<MasDepartment> getOtherFixedDepartmentsForCurrentUser() {
        Long currentDeptId = authUtil.getCurrentDepartmentId();
        List<Long> fixedIds = getFixedDeptIds();

        if (currentDeptId == null) {
            // No dept in token -> return all configured departments
            return masDepartmentRepository.findByIdIn(fixedIds);
        }

        // If logged-in dept is in fixed list, exclude it and return others
        if (fixedIds.contains(currentDeptId)) {
            List<Long> targetIds = fixedIds.stream()
                    .filter(id -> !id.equals(currentDeptId))
                    .collect(Collectors.toList());
            return masDepartmentRepository.findByIdIn(targetIds);
        }

        // If logged-in dept not in fixed list, return all fixed departments
        return masDepartmentRepository.findByIdIn(fixedIds);
    }


}
