package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.response.*;
import com.hims.service.IndentReportGetApiService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class IndentReportGetApiServiceImpl implements IndentReportGetApiService {

    private final StoreInternalIndentMRepository indentMRepository;
    private final StoreInternalIndentTRepository indentTRepository;
    private final MasCommonStatusRepository commonStatusRepository;
    private final MasDepartmentRepository masDepartmentRepository;
    private final StoreIssueMRepository issueMRepository;
    private final StoreIndentReceiveMRepository receiveMRepository;
    private final StoreReturnMRepository returnMRepository;
    private final MasStoreItemRepository storeItemRepository;
    private final StoreItemBatchStockRepository storeItemBatchStockRepository;
    private final StoreStockLedgerRepository storeStockLedgerRepository;
    private final AuthUtil authUtil;


    @Value("${hos.define.adminId}")
    private Long adminDeptId;


    @Override
    public ApiResponse<Page<IndentTrackingListReportResponse>> getIndentTrackingList(int page, int size) {

        try {
            log.info("getIndentTrackingList method started...");
            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.UNAUTHORIZED.value());
            }
            Long deptId = authUtil.getCurrentDepartmentId();

            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "indentDate")
            );

            Page<StoreInternalIndentM> indentPage;

            if (isAdminDepartment(deptId)) {

                List<Long> deptIds =
                        masDepartmentRepository
                                .findByIndentApplicableIgnoreCase("Y")
                                .stream()
                                .map(MasDepartment::getId)
                                .toList();

                indentPage =
                        indentMRepository.findByFromDeptId_IdIn(
                                deptIds,
                                pageable
                        );

            } else {

                indentPage =
                        indentMRepository.findByFromDeptId_Id(
                                deptId,
                                pageable
                        );
            }

            Page<IndentTrackingListReportResponse> responsePage =
                    buildIndentTrackingResponse(indentPage);

            return ResponseUtils.createSuccessResponse(
                    responsePage,
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("getIndentTrackingList error", e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Internal Server Error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    /* ================= CORE BATCH BUILDER ================= */

    private Page<IndentTrackingListReportResponse> buildIndentTrackingResponse(Page<StoreInternalIndentM> indentPage) {

        List<Long> indentMIds =
                indentPage.getContent()
                        .stream()
                        .map(StoreInternalIndentM::getIndentMId)
                        .toList();

        List<StoreInternalIndentT> indentTList =
                indentTRepository.findByIndentMIds(indentMIds);

        Map<Long, List<IndentTResponseForIndentTracking>> indentTMap =
                indentTList.stream()
                        .collect(Collectors.groupingBy(
                                t -> t.getIndentM().getIndentMId(),
                                Collectors.mapping(
                                        this::mapIndentT,
                                        Collectors.toList()
                                )
                        ));

        return indentPage.map(indent -> {

            IndentTrackingListReportResponse res =
                    mapIndentMOnly(indent);

            res.setIndentTResponses(
                    indentTMap.getOrDefault(
                            indent.getIndentMId(),
                            List.of()
                    )
            );

            return res;
        });
    }

    /* ================= M MAPPER ================= */

    private IndentTrackingListReportResponse mapIndentMOnly(StoreInternalIndentM indent) {

        IndentTrackingListReportResponse res =
                new IndentTrackingListReportResponse();

        MasCommonStatus masCommonStatus =
                commonStatusRepository
                        .findByEntityNameAndColumnNameAndStatusCode(
                                AppConstants.ENTITY_STORE_INTERNAL_INDENT_M,
                                AppConstants.M_COLUMN_NAME,
                                indent.getStatus()
                        )
                        .orElseThrow(() ->
                                new RuntimeException("Status not found")
                        );

        res.setCurrentDeptId(authUtil.getCurrentDepartmentId());
        res.setDepartmentId(indent.getFromDeptId().getId());
        res.setDeptName(indent.getFromDeptId().getDepartmentName());

        res.setToDepartmentId(indent.getToDeptId().getId());
        res.setToDepartmentName(
                indent.getToDeptId().getDepartmentName()
        );

        res.setIndentMId(indent.getIndentMId());
        res.setIndentNo(indent.getIndentNo());

        if (indent.getIndentDate() != null) {
            res.setIndentDate(
                    indent.getIndentDate().toLocalDate()
            );
        }

        if (indent.getApprovedDate() != null) {
            res.setApprovedDate(
                    indent.getApprovedDate().toLocalDate()
            );
        }

        if (indent.getIssuedDate() != null) {
            res.setIssueDate(
                    indent.getIssuedDate().toLocalDate()
            );
        }

        res.setStatusId(masCommonStatus.getCommonStatusId());
        res.setStatusName(indent.getStatus());

        return res;
    }

    /* ================= T MAPPER ================= */

    private IndentTResponseForIndentTracking mapIndentT(StoreInternalIndentT t) {

        IndentTResponseForIndentTracking dto = new IndentTResponseForIndentTracking();

        dto.setIndentTId(t.getIndentTId());

        if (t.getItemId() != null) {

            dto.setItemId(t.getItemId().getItemId());
            dto.setItemName(t.getItemId().getNomenclature());

            if (t.getItemId().getUnitAU() != null) {

                dto.setItemUnitId(
                        t.getItemId()
                                .getUnitAU()
                                .getUnitId()
                );

                dto.setItemUnitName(
                        t.getItemId()
                                .getUnitAU()
                                .getUnitName()
                );
            }
        }

        dto.setQtyRequested(t.getRequestedQty());
        dto.setQtyApproved(t.getApprovedQty());
        dto.setQtyReceived(t.getReceivedQty());
        dto.setReasonForIndent(t.getReason());

        return dto;
    }

    /* ================= ADMIN CHECK ================= */

    private boolean isAdminDepartment(Long deptId) {

        return masDepartmentRepository
                .findById(deptId)
                .map(dept ->
                        "ADMIN".equalsIgnoreCase(
                                dept.getDepartmentName()
                        ) && dept.getId().equals(adminDeptId)
                )
                .orElse(false);
    }

    @Override
    public ApiResponse<Page<IndentTrackingListReportResponse>>
    searchIndentTrackingList(
            Long fromDepartmentId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {
        try {
            log.info("searchIndentTrackingList method started...");
            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.UNAUTHORIZED.value());
            }
            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "indentDate")
            );

            Page<StoreInternalIndentM> indentPage =
                    indentMRepository.findAll(
                            search(
                                    fromDepartmentId,
                                    fromDate,
                                    toDate
                            ),
                            pageable
                    );

            Page<IndentTrackingListReportResponse> responsePage =
                    buildIndentTrackingResponse(indentPage);

            log.info("searchIndentTrackingList method ended...");
            return ResponseUtils.createSuccessResponse(
                    responsePage,
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("searchIndentTrackingList error", e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Internal Server Error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<MasCommonStatusResponse>> getStatusMapForIndentTracking() {
        try {
            log.info("getStatusMapForIndentTracking method Started... ");
            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.UNAUTHORIZED.value());
            }
            List<String> entities = List.of(AppConstants.ENTITY_STORE_INTERNAL_INDENT_M);
            List<String> columns = List.of(AppConstants.M_COLUMN_NAME);
            List<MasCommonStatus> statusList = commonStatusRepository.findByEntityNameInAndColumnNameIn(entities, columns);
            log.info("getStatusMapForIndentTracking method Started... ");
            return ResponseUtils.createSuccessResponse(statusList.stream().map(this::mapToCommonStatusResponse).toList(), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("getStatusMapForIndentTracking method error :: ",e);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Long> getIssueMIdFromIndentMId(Long indentMId) {
        try {
            log.info("getIssueMIdFromIndentMId method started...");
            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.UNAUTHORIZED.value());
            }
            StoreIssueM byIndentMIdIndentMId = issueMRepository.findByIndentMId_IndentMId(indentMId);
            log.info("getIssueMIdFromIndentMId method started...");
            return ResponseUtils.createSuccessResponse(byIndentMIdIndentMId.getStoreIssueMId(), new TypeReference<>() {});
        }catch (Exception e) {
            log.error("getIssueMIdFromIndentMId method error :: ",e);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Long> getReceiveMIdFromIndentMId(Long indentMId) {
        try {
            log.info("getReceiveMIdFromIndentMId method started...");
            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.UNAUTHORIZED.value());
            }
            StoreIndentReceiveM receiveM = receiveMRepository.findByStoreInternalIndent_IndentMId(indentMId);
            log.info("getReceiveMIdFromIndentMId method started...");
            return ResponseUtils.createSuccessResponse(receiveM.getReceiveMId(), new TypeReference<>() {});
        }catch (Exception e) {
            log.error("getReceiveMIdFromIndentMId method error :: ",e);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Long> getReturnMIdFromIndentMId(Long indentMId) {
        try {
            log.info("getReturnMIdFromIndentMId method started...");
            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.UNAUTHORIZED.value());
            }
            StoreReturnM returnM = returnMRepository.findByStoreIndentReceiveM_StoreInternalIndent_IndentMId(indentMId);
            log.info("getReturnMIdFromIndentMId method started...");
            return ResponseUtils.createSuccessResponse(returnM.getReturnMId(), new TypeReference<>() {});
        }catch (Exception e) {
            log.error("getReceiveMIdFromIndentMId method error :: ",e);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Page<ItemStockLedgerWithBatchResponse>> getStoreItems(String keyword, int page, int size) {
       try {
           log.info("getStoreItems with item contains name {} ,method started...",keyword);
           User currentUser = authUtil.getCurrentUser();
           if(currentUser==null){
               return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.UNAUTHORIZED.value());
           }
           if(keyword==null){
               throw new RuntimeException("Search keyword can not be blank");
           }

           Pageable pageable=PageRequest.of(
                   page,
                   size,
                   Sort.by(Sort.Direction.ASC,"nomenclature")
           );
           Page<MasStoreItem> entities = storeItemRepository.findByNomenclatureContainingIgnoreCaseAndStatus(keyword, "y", pageable);
           Page<ItemStockLedgerWithBatchResponse> responses = entities.map((item) -> {
               ItemStockLedgerWithBatchResponse response = new ItemStockLedgerWithBatchResponse();
               response.setItemId(item.getItemId());
               response.setPvmsNo(item.getPvmsNo());
               response.setNomenclature(item.getNomenclature());
               return response;
           });
           log.info("getStoreItems with item contains name {} ,method ended...",keyword);
           return  ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
       }catch (Exception e) {
           log.error("getStoreItems method error :: ",e);
           return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR.value());
       }
    }

    @Override
    public ApiResponse<List<String>> getBatchesFromItemId(Long itemId) {
       try {
           log.info("getBatchesFromItemId method started...");
           User currentUser = authUtil.getCurrentUser();
           if(currentUser==null){
               return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.UNAUTHORIZED.value());
           }
           MasStoreItem masStoreItem = storeItemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Invalid Item ID , Item not found in MasStoreItem"));
           List<String> batches = storeItemBatchStockRepository.findByItemId(masStoreItem).stream().map(StoreItemBatchStock::getBatchNo).toList();
           log.info("getBatchesFromItemId method ended...");
           return  ResponseUtils.createSuccessResponse(batches, new TypeReference<>() {});
       }catch (Exception e) {
           log.error("getBatchesFromItemId method error :: ",e);
           return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR.value());
       }
    }

    @Override
    public ApiResponse<Page<StoreStockLedgerReportResponse>> getStoreStockLedgerReport(int page,int size,Long itemId, String batchNo) {
        try {
            log.info("getStoreStockLedgerReport method started with item id {} and batch number {}",itemId,batchNo);
            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.UNAUTHORIZED.value());
            }
            Long hospitalId = currentUser.getHospital().getId();
            if(itemId==null || batchNo==null){
                throw  new RuntimeException("Item id and batch number for this method can not be null");
            }
//            Sort sort = Sort.by(Sort.Direction.DESC, "stockId.expiryDate");
            Sort sort = Sort.by(Sort.Direction.DESC, "createdDt");
            Pageable pageable=PageRequest.of(
                    page,
                    size,
                    sort
            );

            Specification<StoreStockLedger> spec = searchLedger(
                    currentUser.getHospital().getId(),
                    authUtil.getCurrentDepartmentId(),
                    itemId,
                    batchNo
            );
            Page<StoreStockLedger> all = storeStockLedgerRepository.findAll(spec, pageable);

            log.info("getStoreStockLedgerReport method ended with item id {} and batch number {}",itemId,batchNo);

            return ResponseUtils.createSuccessResponse(all.map(this::mapToStoreStockLedgerReportResponse), new TypeReference<Page<StoreStockLedgerReportResponse>>() {});
        }catch (Exception e) {
            log.error("getStoreStockLedgerReport method error :: ",e);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private static Specification<StoreStockLedger> searchLedger(Long hospitalId,Long deptId,Long itemId,String batchNo){
        return (root, query, cb) -> {

            List<Predicate> predicates= new ArrayList<>();

            Join<StoreStockLedger, MasHospital> hospitalJoin = root.join("hospital", JoinType.INNER);

            predicates.add(
                   cb.equal(hospitalJoin.get("id"),hospitalId)
            );

            Join<StoreStockLedger, MasDepartment> deptJoin = root.join("dept", JoinType.INNER);
            predicates.add(
                    cb.equal(deptJoin.get("id"),deptId)
            );
            Join<StoreStockLedger, StoreItemBatchStock> storeItemBatchStockJoin = root.join("stockId", JoinType.INNER);
            Join<StoreItemBatchStock,MasStoreItem> storeItemJoin=storeItemBatchStockJoin.join("itemId",JoinType.INNER);
            predicates.add(
                    cb.equal(storeItemJoin.get("itemId"),itemId)
            );
            predicates.add(
                    cb.equal(storeItemBatchStockJoin.get("batchNo"),batchNo)
            );

            return  cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private StoreStockLedgerReportResponse mapToStoreStockLedgerReportResponse(StoreStockLedger entity){
        StoreStockLedgerReportResponse response= new StoreStockLedgerReportResponse();
        response.setLedgerId(entity.getLedgerId());
        response.setCreatedDate(entity.getCreatedDt());
        response.setReferenceNum(entity.getReferenceNum());
        response.setTxnType(entity.getTxnType());
        response.setTxnSource(entity.getTxnSource());
        response.setQtyBefore(entity.getQtyBefore());
        response.setQtyIn(entity.getQtyIn());
        response.setQtyOut(entity.getQtyOut());
        response.setQtyReturn(entity.getQtyReject());
        response.setQtyAfter(entity.getQtyAfter());
        response.setRemarks(entity.getRemarks());
        return  response;
    }


    private MasCommonStatusResponse mapToCommonStatusResponse(MasCommonStatus entity){
        MasCommonStatusResponse response= new MasCommonStatusResponse();
        response.setCommonStatusId(entity.getCommonStatusId());
        response.setEntityName(entity.getEntityName());
        response.setTableName(entity.getTableName());
        response.setColumnName(entity.getColumnName());
        response.setStatusCode(entity.getStatusCode());
        response.setStatusName(entity.getStatusName());
        response.setStatusDesc(entity.getStatusDesc());
        response.setRemarks(entity.getRemarks());
        response.setUpdateDate(entity.getUpdateDate());

        return  response;
    }
    public static Specification<StoreInternalIndentM> search(
            Long fromDepartmentId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            /* ===== FROM DEPARTMENT FILTER ===== */
            if (fromDepartmentId != null) {
                predicates.add(
                        cb.equal(
                                root.get("fromDeptId").get("id"),
                                fromDepartmentId
                        )
                );
            }

            /* ===== DATE FILTER ===== */
            if (fromDate != null && toDate != null) {
                predicates.add(
                        cb.between(
                                root.get("indentDate"),
                                fromDate.atStartOfDay(),
                                toDate.atTime(23, 59, 59)
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
