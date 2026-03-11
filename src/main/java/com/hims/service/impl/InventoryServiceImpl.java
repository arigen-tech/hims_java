package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.projection.IndentDetailsForIssueProjection;
import com.hims.projection.MasStoreItemProjection;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.InventoryService;
import com.hims.utils.AuthUtil;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

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
    private final MasCommonStatusRepository masCommonStatusRepository;

    private final  MasStoreItemRepository masStoreItemRepository;
    private final MasStoreSectionRepository masStoreSectionRepository;

    private final StoreIssueMRepository storeIssueMRepository;

    private final StoreIssueTRepository storeIssueTRepository;

    private final StoreIndentReceiveTRepository storeReceiveTRepository;

    private  final StoreReturnTRepository storeReturnTRepository;

    private final  StoreBalanceHdRepository storeBalanceHdRepository;

    private final StoreBalanceDtRepository storeBalanceDtRepository;

    private final RandomNumGenerator randomNumGenerator;

    private final MasBrandRepository masBrandRepository;

    private final MasManufacturerRepository masManufacturerRepository;

    @Value("${op_txn_type}")
    private String opTxnType;

    @Value("${hos.define.wardPharmacyId}")
    private Long warddeptId;

    @Value("${hos.define.storeId}")
    private Long deptIdStore;

    @Value("${hos.define.adminId}")
    private Long adminDeptId;

    @Value("${hos.define.dispensaryId}")
    private Long dispdeptId;

    @Value("${fixed.departments}")
    private String fixedDepartmentsConfig;


    @Value( "${hos.define.storeDay}")
    private int storeDrugExpDay;

    @Value( "${hos.define.dispensaryDay}")
    private int dispensaryDrugExpDay;

    @Value( "${hos.define.wardPharmDay}")
    private int wardPharmDrugExpDay;

    @Value(("${sectionId.drugs}"))
    private Long sectionIdForDrugs;



    @Override
    public ApiResponse<Page<IndentTrackingListResponse>> getIndentTrackingList(int page, int size) {

        try {

            log.info("getIndentTrackingList method started...");

            Long deptId = authUtil.getCurrentDepartmentId();

            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "indentDate")
            );

            List<Long> deptIds = null;

            if (isAdminDepartment(deptId)) {
                deptIds = masDepartmentRepository
                        .findByIndentApplicableIgnoreCase(AppConstants.STATUS_Y)
                        .stream()
                        .map(MasDepartment::getId)
                        .toList();
            } else {
                deptIds = List.of(deptId);
            }

            Page<IndentTrackingListResponse> result =
                    indentMRepository.findIndentTrackingListForAdmin(
                            deptIds,
                            deptId,
                            pageable
                    );
            log.info("getIndentTrackingList method ended...");
            return ResponseUtils.createSuccessResponse(result, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getIndentTrackingList error", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<Page<IndentTrackingListResponse>>
    searchIndentTrackingList(
            Long fromDepartmentId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size) {

        try {

            log.info("searchIndentTrackingList method started for department id :: {}",fromDepartmentId);

            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "indentDate")
            );

            Page<IndentTrackingListResponse> result =
                    indentMRepository.searchIndentTrackingListProjection(
                            fromDepartmentId,
                            fromDate != null ? fromDate.atStartOfDay() : null,
                            toDate != null ? toDate.atTime(23, 59, 59) : null,
                            authUtil.getCurrentDepartmentId(),
                            pageable
                    );
            log.info("searchIndentTrackingList method ended for department id :: {}",fromDepartmentId);
            return ResponseUtils.createSuccessResponse(result, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("searchIndentTrackingList error", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
    @Override
    public ApiResponse<List<IndentDetailsResponseForIndentTracking>> getIndentDetailsForIndentTracking(Long indentMId) {

        try {

            log.info("Indent details for indent tracking started for indentMId {}", indentMId);

            List<IndentDetailsResponseForIndentTracking> response =
                    indentTRepository.findIndentDetailsForTracking(indentMId);

            if (response.isEmpty()) {
                return ResponseUtils.createNotFoundResponse(
                        AppConstants.INDENT_M_NOT_FOUND_MSG,
                        HttpStatus.NOT_FOUND.value());
            }
            log.info("Indent details for indent tracking ended for indentMId {}", indentMId);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getIndentDetailsForIndentTracking method error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Page<StoreStockLedgerReportResponse>> getStoreStockLedgerReport(
            int page,
            int size,
            Long hospitalId,
            Long itemId,
            String batchNo) {

        try {

            log.info("getStoreStockLedgerReport method started with item id {} and batch number {}",itemId,batchNo);

            Sort sort = Sort.by(Sort.Direction.DESC, "createdDt");
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<StoreStockLedgerReportResponse> result =
                    storeStockLedgerRepository.findLedgerReport(
                            hospitalId,
                            authUtil.getCurrentDepartmentId(),
                            itemId,
                            batchNo,
                            pageable
                    );

            log.info("getStoreStockLedgerReport method ended with item id {} and batch number {}",itemId,batchNo);
            return ResponseUtils.createSuccessResponse(
                    result,
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("getStoreStockLedgerReport method error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<MasCommonStatusResponse>> getStatusMapForIndentTracking() {
        try {
            log.info("getStatusMapForIndentTracking method Started... ");
            List<String> entities = List.of(AppConstants.ENTITY_STORE_INTERNAL_INDENT_M);
            List<String> columns = List.of(AppConstants.M_COLUMN_NAME);
            List<MasCommonStatus> statusList = commonStatusRepository.findByEntityNameInAndColumnNameIn(entities, columns);
            log.info("getStatusMapForIndentTracking method Started... ");
            return ResponseUtils.createSuccessResponse(statusList.stream().map(this::mapToCommonStatusResponse).toList(), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("getStatusMapForIndentTracking method error :: ",e);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},AppConstants.INTERNAL_SERVER_ERR_MSG,HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Page<ItemStockLedgerWithBatchResponse>> getStoreItems(Long sectionId,String keyword, int page, int size) {
        try {
            log.info("getStoreItems with item contains name {} ,method started...",keyword);
            
            Pageable pageable=PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.ASC,"nomenclature")
            );
            Page<ItemStockLedgerWithBatchResponse> responses ;
            if(sectionId==null){
                responses=storeItemRepository.searchNonDrugItems(sectionIdForDrugs, keyword, pageable);
            }else{
                responses=storeItemRepository.searchItems(sectionId, keyword, pageable);
            }
            log.info("getStoreItems with item contains name {} ,method ended...",keyword);
            return  ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
        }catch (Exception e) {
            log.error("getStoreItems method error :: ",e);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},AppConstants.INTERNAL_SERVER_ERR_MSG,HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<String>> getBatchesFromItemId(Long itemId) {
        try {
            log.info("getBatchesFromItemId method started...");

            MasStoreItem masStoreItem = storeItemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Invalid Item ID , Item not found in MasStoreItem"));
            List<String> batches = storeItemBatchStockRepository.findByItemId(masStoreItem).stream().map(StoreItemBatchStock::getBatchNo).toList();
            log.info("getBatchesFromItemId method ended...");
            return  ResponseUtils.createSuccessResponse(batches, new TypeReference<>() {});
        }catch (Exception e) {
            log.error("getBatchesFromItemId method error :: ",e);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},AppConstants.INTERNAL_SERVER_ERR_MSG,HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Long> getIssueMIdFromIndentMId(Long indentMId) {
        try {
            log.info("getIssueMIdFromIndentMId method started...");

            Long issueMId = issueMRepository.findIssueMIdByIndentMId(indentMId);

            if (issueMId == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        AppConstants.INDENT_ISSUE_HEADER_NOT_FOUND_ERR_MSG,
                        HttpStatus.NOT_FOUND.value()
                );
            }
            log.info("getIssueMIdFromIndentMId method ended for indentMId - {} with issueMId {}",indentMId,issueMId);

            return ResponseUtils.createSuccessResponse(
                    issueMId,
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("getIssueMIdFromIndentMId method error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<Long> getReceiveMIdFromIndentMId(Long indentMId) {
        try {
            log.info("getReceiveMIdFromIndentMId method started...");
            Long receiveMId = receiveMRepository.findReceiveMIdByIndentMId(indentMId);
            if(receiveMId == null){
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        AppConstants.INDENT_RECEIVE_HEADER_NOT_FOUND_ERR_MSG,
                        HttpStatus.NOT_FOUND.value()
                );
            }
            log.info("getReceiveMIdFromIndentMId method ended for indentMId - {} with receiveMId {}",indentMId,receiveMId);
            return ResponseUtils.createSuccessResponse(
                    receiveMId,
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("getReceiveMIdFromIndentMId method error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<Long> getReturnMIdFromIndentMId(Long indentMId) {
        try {
            log.info("getReturnMIdFromIndentMId method started...");

            Long returnMId = returnMRepository.findReturnMIdByIndentMId(indentMId);

            if (returnMId == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Return record not found",
                        HttpStatus.NOT_FOUND.value()
                );
            }

            log.info("getReturnMIdFromIndentMId method completed...");

            return ResponseUtils.createSuccessResponse(
                    returnMId,
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("getReturnMIdFromIndentMId method error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public List<DepartmentDropdownResponse> fetchIndentApplicableDepartmentsExceptCurrent() {

        Long currentDeptId = authUtil.getCurrentDepartmentId();
        List<Long> fixedIds = getFixedDeptIds();

        if (currentDeptId == null) {
            return masDepartmentRepository.findDropdownDepartmentsByIds(fixedIds);
        }

        if (fixedIds.contains(currentDeptId)) {
            List<Long> targetIds = fixedIds.stream()
                    .filter(id -> !id.equals(currentDeptId))
                    .collect(Collectors.toList());

            return masDepartmentRepository.findDropdownDepartmentsByIds(targetIds);
        }

        return masDepartmentRepository.findDropdownDepartmentsByIds(fixedIds);
    }


    @Override
    public ApiResponse<DepartmentDropdownResponse> getCurrentDepartmentById(Long id) {

        return masDepartmentRepository.findCurrentDeptById(id)
                .map(dept -> ResponseUtils.createSuccessResponse(
                        dept,
                        new TypeReference<>() {}
                ))
                .orElseGet(() ->
                        ResponseUtils.createNotFoundResponse(AppConstants.DEPT_NOT_FOUND_ERR_MSG, HttpStatus.NOT_FOUND.value())
                );
    }

    @Override
    public ApiResponse<MasStoreItemResponse> getItemById(Long hospitalId,Long itemId) {

        try {
            log.info("getItemById method started for itemId - {}",itemId);

            Optional<MasStoreItemProjection> projection =
                    masStoreItemRepository.findItemWithStock(
                            itemId,
                            hospitalId,
                            deptIdStore,
                            dispdeptId,
                            warddeptId,
                            LocalDate.now().plusDays(storeDrugExpDay),
                            LocalDate.now().plusDays(dispensaryDrugExpDay),
                            LocalDate.now().plusDays(wardPharmDrugExpDay)
                    );

            if (projection.isEmpty()) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {}, AppConstants.ITEM_NOT_FOUND_ERR_MSG, HttpStatus.NOT_FOUND.value());
            }
            log.info("getItemById method ended for itemId - {}",itemId);

            return ResponseUtils.createSuccessResponse(
                    mapToResponse(projection.get()),
                    new TypeReference<>() {});

        } catch (Exception e) {
            log.info("getItemById method error for itemId - {} :: ",itemId);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},AppConstants.INTERNAL_SERVER_ERR_MSG,HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<StoreInternalIndentResponse> saveIndent(StoreInternalIndentRequest request) {
        MasCommonStatus masCommonStatus = masCommonStatusRepository.findByEntityNameAndColumnNameAndStatusCode(AppConstants.ENTITY_STORE_INTERNAL_INDENT_M, AppConstants.M_COLUMN_NAME, AppConstants.INDENT_CREATED_AT_REQ_DEPT)
                .orElseThrow(() -> new RuntimeException(AppConstants.STATUS_NOT_FOUND_ERR_MSG));
        return processIndent(request, masCommonStatus.getStatusCode()); // "S" for Save/Draft
    }

    @Override
    @Transactional
    public ApiResponse<StoreInternalIndentResponse> submitIndent(StoreInternalIndentRequest request) {
        MasCommonStatus masCommonStatus = masCommonStatusRepository.findByEntityNameAndColumnNameAndStatusCode(AppConstants.ENTITY_STORE_INTERNAL_INDENT_M, AppConstants.M_COLUMN_NAME, AppConstants.INDENT_SUBMITTED_AT_REQ_DEPT)
                .orElseThrow(() -> new RuntimeException(AppConstants.STATUS_NOT_FOUND_ERR_MSG));

        return processIndent(request, masCommonStatus.getStatusCode()); // "Y" for Submit
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<IndentTrackingListResponse>> getAllIndentsForViewUpdateWrtDept(
            Long deptId,
            int page,
            int size,
            LocalDate fromDate,
            LocalDate toDate,
            String status
    ) {
        try {

            log.info(("getAllIndentsForViewUpdateWrtDept method started for deptId - {}"), deptId);


            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "indentDate")
            );

             List<String> statues=status ==null ? List.of(AppConstants.INDENT_CREATED_AT_REQ_DEPT, AppConstants.INDENT_SUBMITTED_AT_REQ_DEPT):List.of(status);

            Page<IndentTrackingListResponse> result =
                    indentMRepository.findIndentListForViewUpdate(
                            deptId,
                            fromDate != null ? fromDate.atStartOfDay() : null,
                            toDate!= null ? toDate.atTime(23, 59, 59) : null,
                            statues,
                            pageable
                    );

            log.info("getAllIndentsForViewUpdateWrtDept method ended for deptId - {} with total elements - {}", deptId, result.getTotalElements());
            return ResponseUtils.createSuccessResponse(
                    result,
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("getAllIndentsForViewUpdateWrtDept error for deptId - {} :: ", deptId, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<IndentTrackingListResponse>> pendingForIndentApprovalWrtDept(Long deptId) {

       try {
           log.info("pendingForIndentApprovalWrtDept method started for deptId - {} :: ", deptId);
           List<IndentTrackingListResponse> response =
                   indentMRepository.pendingForIndentApprovalWrtDept(deptId,AppConstants.STATUS_Y);
           log.info("pendingForIndentApprovalWrtDept method ended for deptId - {} :: ", deptId);
           return ResponseUtils.createSuccessResponse(
                   response,
                   new TypeReference<>() {}
           );
       } catch (Exception e) {
              log.error("pendingForIndentApprovalWrtDept error for deptId - {} :: ", deptId, e);
              return ResponseUtils.createFailureResponse(
                     null,
                     new TypeReference<>() {},
                     AppConstants.INTERNAL_SERVER_ERR_MSG,
                     HttpStatus.INTERNAL_SERVER_ERROR.value()
              );
       }
    }

    @Override
    public ApiResponse<List<IndentDetailsWithAvlStock>> getIndentDetailsForIssueWithAvailableStock(Long indentMId,Long deptId) {
      try{
          log.info("getIndentDetailsForIssueWithAvailableStock method started for indentMId - {} with deptId {}:: ", indentMId,deptId);
            List<IndentDetailsWithAvlStock> response =
                    indentTRepository.findIndentDetailsWithStock(indentMId,deptId,LocalDate.now().plusDays(storeDrugExpDay));
          log.info("getIndentDetailsForIssueWithAvailableStock method ended for indentMId - {} with deptId {}:: ", indentMId,deptId);
            return ResponseUtils.createSuccessResponse(
                        response,
                        new TypeReference<>() {}
                );
      } catch (Exception e) {
            log.error("getIndentDetailsForIssueWithAvailableStock error for indentMId - {} :: ", indentMId, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
      }
    }

    @Override
    public ApiResponse<StoreInternalIndentResponse> approveRejectIndent(StoreInternalIndentApprovalRequest request) {
        try {
            StoreInternalIndentM indentM = indentMRepository.findById(request.getIndentMId())
                    .orElseThrow(() -> new RuntimeException(AppConstants.INDENT_HEADER_NOT_FOUND_ERR_MSG));

            // Validate current status - only pending indents (Y) can be approved/rejected
            if (!AppConstants.INDENT_SUBMITTED_AT_REQ_DEPT.equalsIgnoreCase(indentM.getStatus())) {
                throw new RuntimeException(AppConstants.INDENT_APPROVED_WARNING_MSG);
            }

            User currentUser = authUtil.getCurrentUser();
            String currentUserName = currentUser != null ? currentUser.getFirstName() : "";

            // Validate action
            if (!AppConstants.ACTION_APPROVED.equalsIgnoreCase(request.getAction()) && !AppConstants.ACTION_REJECTED.equalsIgnoreCase(request.getAction())) {
                throw new RuntimeException(AppConstants.INVALID_ACTION_WARNING_MSG);
            }

            // Update status based on action
            String approvedStatus = masCommonStatusRepository.findByEntityNameAndColumnNameAndStatusCode(AppConstants.ENTITY_STORE_INTERNAL_INDENT_M, AppConstants.M_COLUMN_NAME, AppConstants.INDENT_APPROVED_AT_REQ_DEPT).orElseThrow().getStatusCode();
            String rejectedStatus = masCommonStatusRepository.findByEntityNameAndColumnNameAndStatusCode(AppConstants.ENTITY_STORE_INTERNAL_INDENT_M, AppConstants.M_COLUMN_NAME, AppConstants.INDENT_REJECTED_AT_REQ_DEPT).orElseThrow().getStatusCode();


            String newStatus = AppConstants.ACTION_APPROVED.equalsIgnoreCase(request.getAction()) ? approvedStatus: rejectedStatus;

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
                                throw new RuntimeException(AppConstants.INDENT_DETAILS_NOT_FOUND_ERR_MSG);
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
            log.error("approveRejectIndent method error :: ",e);
            return ResponseUtils.createFailureResponse(null,
                    new TypeReference<StoreInternalIndentResponse>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<IndentTrackingListResponse>> getAllIndentsApprovedForIssueDept(Long deptId) {

        try {
            log.info("getAllIndentsApprovedForIssueDept method started for deptId - {} :: ", deptId);
            List<IndentTrackingListResponse> response =
                    indentMRepository.findIndentsWrtStatus(deptId,AppConstants.INDENT_APPROVED_AT_REQ_DEPT);
            log.info("getAllIndentsApprovedForIssueDept method started for deptId - {} :: ", deptId);
            return ResponseUtils.createSuccessResponse(
                    response,
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("getAllIndentsApprovedForIssueDept method error for deptId - {} :: ", deptId, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }


    }

    @Override
    @Transactional
    public ApiResponse<StoreInternalIndentResponse> approveIndentByIssueDept(IssueInternalIndentApprovalRequest request) {
        try {
            // 1. Load indent master
            StoreInternalIndentM indentM = indentMRepository.findById(request.getIndentMId())
                    .orElseThrow(() -> new RuntimeException(AppConstants.INDENT_HEADER_NOT_FOUND_ERR_MSG));

            // 2. Only indents with status A are allowed
            if (!AppConstants.INDENT_APPROVED_AT_REQ_DEPT.equalsIgnoreCase(indentM.getStatus())) {
                throw new RuntimeException(
                        AppConstants.INDENT_APPROVED_WARNING_MSG);
            }

            // 3. Current user
            User currentUser = authUtil.getCurrentUser();
            String currentUserName = currentUser != null ? currentUser.getFirstName() : "";

            String action = request.getAction() != null ? request.getAction().trim().toLowerCase() : "";

            String approvedStatus = masCommonStatusRepository
                    .findByEntityNameAndColumnNameAndStatusCode(AppConstants.ENTITY_STORE_INTERNAL_INDENT_M, AppConstants.M_COLUMN_NAME, AppConstants.INDENT_APPROVED_AT_ISSUED_DEPT)
                    .orElseThrow(() -> new RuntimeException(AppConstants.STATUS_NOT_FOUND_ERR_MSG))
                    .getStatusCode();
            String rejectedStatus = masCommonStatusRepository
                    .findByEntityNameAndColumnNameAndStatusCode(AppConstants.ENTITY_STORE_INTERNAL_INDENT_M, AppConstants.M_COLUMN_NAME, AppConstants.INDENT_REJECTED_AT_ISSUED_DEPT)
                    .orElseThrow(() -> new RuntimeException(AppConstants.STATUS_NOT_FOUND_ERR_MSG))
                    .getStatusCode();

            // 4. Common item processing (approve qty + reason)
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                for (IssueInternalIndentDetailRequest itemReq : request.getItems()) {
                    if (itemReq.getIndentTId() != null) {
                        StoreInternalIndentT detail = indentTRepository.findById(itemReq.getIndentTId())
                                .orElseThrow(() -> new RuntimeException(
                                        AppConstants.INDENT_DETAILS_NOT_FOUND_ERR_MSG));

                        // Ensure this detail belongs to the current indent
                        if (!detail.getIndentM().getIndentMId().equals(indentM.getIndentMId())) {
                            throw new RuntimeException(AppConstants.INDENT_DETAILS_NOT_FOUND_ERR_MSG);
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
            if (AppConstants.ACTION_APPROVED.equals(action)) {
                // Submitted for issue
                indentM.setStatus(approvedStatus);                 // Approved and submitted for issue
                indentM.setStoreApprovedBy(currentUserName);
                indentM.setStoreApprovedDate(LocalDateTime.now());
            } else if (AppConstants.ACTION_REJECTED.equals(action)) {
                // Rejected after approval
                indentM.setStatus(rejectedStatus);                 // Rejected after approval
                indentM.setStoreApprovedBy(currentUserName);
                indentM.setStoreApprovedDate(LocalDateTime.now());
            } else {
                throw new RuntimeException(
                        AppConstants.INVALID_ACTION_WARNING_MSG);
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
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                     HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<StoreInternalIndentMResponse>> getAllIndentsForIssueWrtDept(Long deptId) {
        try {
            log.info("getAllIndentsForIssueWrtDept method started for deptId - {} :: ", deptId);
            List<StoreInternalIndentMResponse> response =
                    indentMRepository.findIndentsWrtToDeptAndStatus(deptId,AppConstants.INDENT_APPROVED_AT_ISSUED_DEPT);
            log.info("getAllIndentsForIssueWrtDept method ended for deptId - {} :: ", deptId);
            return ResponseUtils.createSuccessResponse(
                    response,
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<StoreIssueMResponse>> getIssuesIndentListWrtToDept(
            Long toDeptId,
            LocalDate fromDate,
            LocalDate toDate) {

        try {

            LocalDateTime startDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
            LocalDateTime endDateTime = toDate != null ? toDate.atTime(LocalTime.MAX) : null;

            List<StoreIssueMResponse> responseList =
                    storeIssueMRepository.findIssuesBetweenDatesWrtToDept(
                            toDeptId,
                            startDateTime,
                            endDateTime
                    );

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error in getIssuesForReceiving | toDeptId={}, fromDate={}, toDate={}",
                    toDeptId, fromDate, toDate, e);

            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                     HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }

    }

    @Override
    public ApiResponse<List<IndentDetailsForIssueResponse>> getIndentDetailsWrtIndentAndDeptForIssue(Long indentMId, Long deptId) {
        try {
            log.info("getIndentDetailsWrtIndentAndDeptForIssue method started for indentMId - {} with deptId {}:: ", indentMId,deptId);
            List<IndentDetailsForIssueProjection> projections =
                    indentTRepository.findIndentDetailsForIssue(indentMId, deptId,LocalDate.now().plusDays(storeDrugExpDay));

            List<IndentDetailsForIssueResponse> responseList =
                    projections.stream()
                            .map(p -> new IndentDetailsForIssueResponse(
                                    p.getIndentTId(),
                                    p.getItemId(),
                                    p.getItemName(),
                                    p.getPvmsNo(),
                                    p.getRequestedQty(),
                                    p.getApprovedQty(),
                                    p.getAvailableStock(),
                                    p.getIssueStatus(),
                                    p.getReason(),
                                    p.getUnitAuName(),
                                    p.getUnitAUid(),
                                    p.getBatchNo(),
                                    p.getBatchAvailableStock(),
                                    p.getMfgDate(),
                                    p.getExpDate()
                            ))
                            .toList();

            log.info("getIndentDetailsWrtIndentAndDeptForIssue method ended for indentMId - {} with deptId {}:: ", indentMId,deptId);
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error in getIndentDetailsWrtIndentAndDeptForIssue | indentMId={}, deptId={}",
                    indentMId, deptId, e);

            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                     HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<PreviousIssueResponse>> getPreviousIssueInfos(Long itemId, Long currentIndentMId) {
        try {
            log.info("getPreviousIssueInfos method started for itemId - {} with currentIndentMId {}:: ", itemId,currentIndentMId);
            if (itemId == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<List<PreviousIssueResponse>>() {},
                        AppConstants.ITEM_NOT_FOUND_ERR_MSG,
                        HttpStatus.NOT_FOUND.value()
                );
            }
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
                        storeItemBatchStockRepository.findByItemIdItemId(itemId);

                if (!batches.isEmpty()) {
                    PreviousIssueResponse curr = new PreviousIssueResponse();
                    curr.setIndentNo("Current Stock Info");
                    curr.setIssueDate(LocalDate.now());
                    curr.setIssueNo("Current");
                    curr.setQtyIssued(BigDecimal.ZERO);

                    StringBuilder batchInfo = new StringBuilder();
                    for (StoreItemBatchStock b : batches) {
                        if (!batchInfo.isEmpty()) batchInfo.append(", ");
                        batchInfo.append(b.getBatchNo())
                                .append("(")
                                .append(b.getClosingStock() != null ? b.getClosingStock() : 0)
                                .append(")");
                    }

                    curr.setBatchNo(batchInfo.toString());
                    previousIssues.add(curr);
                }
            }
            log.info("getPreviousIssueInfos method ended for itemId - {} with currentIndentMId {}:: ", itemId,currentIndentMId);

            return ResponseUtils.createSuccessResponse(
                    previousIssues,
                    new TypeReference<List<PreviousIssueResponse>>() {}
            );

        } catch (Exception e) {
            log.error("getPreviousIssueInfos method error :: ",e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<List<PreviousIssueResponse>>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional
    public ApiResponse<StoreInternalIndentResponse> issueIndent(StoreInternalIssueRequest request) {
        try {
            // === Validate ===
            if (request.getIndentMId() == null) {
                throw new RuntimeException(AppConstants.INDENT_HEADER_NOT_FOUND_ERR_MSG);
            }

            if (request.getItems() == null || request.getItems().isEmpty()) {
                throw new RuntimeException(AppConstants.ATLEAST_ONE_INDENT_ISSUE_WARN_MSG);
            }

            // === Load Master ===
            StoreInternalIndentM indentM = indentMRepository.findById(request.getIndentMId())
                    .orElseThrow(() -> new RuntimeException(AppConstants.INDENT_HEADER_NOT_FOUND_ERR_MSG));

            // === Generate Issue No ===
            String issueNo = generateIssueNumber();

            // === Current User ===
            String userName = authUtil.getCurrentUser().getFirstName();

            String issuedStatusM = masCommonStatusRepository
                    .findByEntityNameAndColumnNameAndStatusCode(AppConstants.ENTITY_STORE_ISSUE_M, AppConstants.COLUMN_NAME, AppConstants.INDENT_ISSUED_AT_ISSUE_DEPT)
                    .orElseThrow(()-> new RuntimeException(AppConstants.STATUS_NOT_FOUND_ERR_MSG))
                    .getStatusCode();
            String issuedStatusT = masCommonStatusRepository
                    .findByEntityNameAndColumnNameAndStatusCode(AppConstants.ENTITY_STORE_ISSUE_T, AppConstants.COLUMN_NAME, AppConstants.INDENT_ISSUED_AT_ISSUE_DEPT)
                    .orElseThrow(()-> new RuntimeException(AppConstants.STATUS_NOT_FOUND_ERR_MSG))
                    .getStatusCode();
            String notIssuedStatus = masCommonStatusRepository
                    .findByEntityNameAndColumnNameAndStatusCode(AppConstants.ENTITY_STORE_INTERNAL_INDENT_T, AppConstants.T_COLUMN_NAME, AppConstants.INDENT_NOT_ISSUED_AT_ISSUE_DEPT)
                    .orElseThrow(() -> new RuntimeException(AppConstants.STATUS_NOT_FOUND_ERR_MSG))
                    .getStatusCode();
            String issuedStatusIndentT = masCommonStatusRepository
                    .findByEntityNameAndColumnNameAndStatusCode(AppConstants.ENTITY_STORE_INTERNAL_INDENT_T, AppConstants.T_COLUMN_NAME, AppConstants.STATUS_Y)
                    .orElseThrow(()-> new RuntimeException(AppConstants.STATUS_NOT_FOUND_ERR_MSG))
                    .getStatusCode();
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
            issueM.setStatus(issuedStatusM); // Issued

            issueM = storeIssueMRepository.save(issueM);

            // Track issued items
            boolean anyItemIssued = false;

            // ============================================================
            // === PROCESS EACH ITEM =====================================
            // ============================================================
            for (StoreInternalIssueDetailRequest itemReq : request.getItems()) {
                StoreInternalIndentT indentT = indentTRepository.findById(itemReq.getIndentTId())
                        .orElseThrow(() -> new RuntimeException(AppConstants.INDENT_DETAILS_NOT_FOUND_ERR_MSG));

                if (!indentT.getIndentM().getIndentMId().equals(indentM.getIndentMId())) {
                    throw new RuntimeException(AppConstants.INDENT_DETAILS_NOT_FOUND_ERR_MSG);
                }

                BigDecimal approved = nvl(indentT.getApprovedQty());
                BigDecimal prevIssued = nvl(indentT.getIssuedQty());
                BigDecimal newIssue = nvl(itemReq.getIssuedQty());

                // === FIX: Calculate available stock from database (NOT from frontend) ===
                List<StoreItemBatchStock> allBatches =
                        storeItemBatchStockRepository.findByDepartmentIdAndItemId(indentM.getToDeptId(), indentT.getItemId());

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
                    indentT.setIssueStatus(notIssuedStatus); // Not issued
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
                    indentT.setIssueStatus(notIssuedStatus);
                    indentTRepository.save(indentT);
                    continue;
                }

                // Rule 3: Should not exceed approved
                if (newIssue.compareTo(remainingApproved) > 0) {
                    throw new RuntimeException(AppConstants.ISSUED_MORE_THAN_APPROVED_WARN_MSG);
                }

                // === Get batch stock (FEFO) ===
                List<StoreItemBatchStock> batchList =
                        storeItemBatchStockRepository.findByDepartmentIdAndItemId(indentM.getToDeptId(), indentT.getItemId());

                if (batchList == null || batchList.isEmpty()) {
                    throw new RuntimeException(AppConstants.STOCK_NOT_AVAILABLE_WARN_MSG);
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
                    storeItemBatchStockRepository.save(batch);

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
                    issueT.setStatus(issuedStatusT);
                    issueT.setUnitPrice(nvl(batch.getMrpPerUnit()));

                    storeIssueTRepository.save(issueT);

                    // === Ledger ===
                    transferOutLedger(
                            BigDecimal.valueOf(closing),
                            qtyToIssue,
                            indentT.getIndentTId(),
                            batch.getStockId(),
                            "ISSUE AGAINST INDENT NO: " + indentM.getIndentNo(),
                            issueNo
                    );

                    remainingQty -= qtyToIssue;
                }

                if (remainingQty > 0) {
                    throw new RuntimeException(AppConstants.STOCK_NOT_AVAILABLE_WARN_MSG);
                }

                // === Update issued qty ===
                BigDecimal newTotalIssued = prevIssued.add(newIssue);
                indentT.setIssuedQty(newTotalIssued);

                // === FIX: Calculate NEW available stock after issuance ===
                BigDecimal newAvailableStock = actualAvailableStock.subtract(newIssue);
                indentT.setAvailableStock(newAvailableStock);

                // === Set item issue status ===
                if (approved.compareTo(BigDecimal.ZERO) == 0) {
                    indentT.setIssueStatus(notIssuedStatus); // Not applicable
                } else {
                    indentT.setIssueStatus(issuedStatusIndentT); // Yes, fully issued
                }

                indentTRepository.save(indentT);
                anyItemIssued = true;
            }

            if (!anyItemIssued) {
                throw new RuntimeException(AppConstants.ITEM_NOT_ISSUED_MSG);
            }

            // ============================================================
            // === UPDATE MASTER STATUS ===================================
            // ============================================================
            indentM.setStatus(AppConstants.INDENT_ISSUED_AT_ISSUED_DEPT);
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
            log.error("issueIndent method error :: ",e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<StoreInternalIndentResponse>() {},
                   AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<IndentDetailsResponseForReceiving>> getIndentDetailsWrtIndentForReceiving(Long indentMId) {

        try {
            log.info("getIndentDetailsForReceiving method started for indentMId - {} :: ", indentMId);
            List<IndentDetailsResponseForReceiving> response =
                    storeIssueTRepository.findIndentDetailsForReceiving(indentMId);
            log.info("getIndentDetailsForReceiving method ended for indentMId - {} :: ", indentMId);
            return ResponseUtils.createSuccessResponse(
                    response,
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.info("getIndentDetailsForReceiving method error for indentMId - {} :: ", indentMId, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional
    public ApiResponse<StoreIndentReceiveResponse> saveReceiving(StoreIndentReceiveRequest request) {
        try {
            // Validate input
            if (request.getIndentMId() == null) {
                throw new RuntimeException(AppConstants.INDENT_HEADER_NOT_FOUND_ERR_MSG);
            }

            if (request.getItems() == null || request.getItems().isEmpty()) {
                throw new RuntimeException(AppConstants.ATLEAST_ONE_INDENT_RECEIVE_WARN_MSG);
            }

            // Load indent master
            StoreInternalIndentM indentM = indentMRepository.findById(request.getIndentMId())
                    .orElseThrow(() -> new RuntimeException(AppConstants.INDENT_HEADER_NOT_FOUND_ERR_MSG));

            // Check if already received
            if (receiveMRepository.existsByStoreInternalIndent(indentM)) {
                throw new RuntimeException(AppConstants.ALREADY_RECEIVED_WARN_MSG);
            }

            // Get current user
            User currentUser = authUtil.getCurrentUser();
            String currentUserName = currentUser != null ? currentUser.getFirstName() : "";

            // Get current department (receiving department)
            Long receivingDeptId = authUtil.getCurrentDepartmentId();
            MasDepartment receivingDept = masDepartmentRepository.findById(receivingDeptId)
                    .orElseThrow(() -> new RuntimeException(AppConstants.RECEIVING_DEPT_NOT_FOUND_ERR_MSG));

            // Get store department (issuing department)
            MasDepartment storeDept = indentM.getToDeptId();

            String receivedStatusM = masCommonStatusRepository
                    .findByEntityNameAndColumnNameAndStatusCode(AppConstants.ENTITY_STORE_INDENT_RECEIVE_M, AppConstants.COLUMN_NAME, AppConstants.STATUS_R)
                    .orElseThrow()
                    .getStatusCode();
            String rejectStatusM = masCommonStatusRepository
                    .findByEntityNameAndColumnNameAndStatusCode(AppConstants.ENTITY_STORE_INDENT_RECEIVE_M, AppConstants.COLUMN_NAME_IS_RETURN, AppConstants.STATUS_N)
                    .orElseThrow()
                    .getStatusCode();
            String notRejectStatusM = masCommonStatusRepository
                    .findByEntityNameAndColumnNameAndStatusCode(AppConstants.ENTITY_STORE_INDENT_RECEIVE_M, AppConstants.COLUMN_NAME_IS_RETURN, AppConstants.STATUS_Y)
                    .orElseThrow()
                    .getStatusCode();

            // ==============================================
            // VALIDATION: received + rejected = issued for each item
            // ==============================================
            for (StoreIndentReceiveItemRequest itemReq : request.getItems()) {
                BigDecimal qtyIssued = nvl(itemReq.getQtyIssued());
                BigDecimal qtyReceived = nvl(itemReq.getQtyReceived());
                BigDecimal qtyRejected = nvl(itemReq.getQtyRejected());
                BigDecimal total = qtyReceived.add(qtyRejected);

                // Check if total equals issued
                if (total.compareTo(qtyIssued) != 0) {
                    StoreInternalIndentT indentT = indentTRepository.findById(itemReq.getIndentTId())
                            .orElse(null);
                    String itemName = indentT != null ? indentT.getItemId().getNomenclature() : AppConstants.FALLBACK_ITEM_NAME;
                    String batchNo = itemReq.getBatchNo() != null ? itemReq.getBatchNo() : AppConstants.NOT_APPLICABLE;

                    throw new RuntimeException(
                            String.format(
                                    AppConstants.STRING_FORMATTER_FOR_RECEIVING_VALIDATION,
                                    itemName, batchNo, qtyReceived, qtyRejected, total, qtyIssued
                            )
                    );
                }
            }

            // ==============================================
            // 1. Create Store Indent Receive Master
            // ==============================================
            StoreIndentReceiveM receiveM = new StoreIndentReceiveM();
            receiveM.setStoreInternalIndent(indentM);
            receiveM.setReceivedDate(request.getReceivingDate() != null ?
                    request.getReceivingDate() : LocalDateTime.now());
            receiveM.setReceivedBy(currentUserName);
            receiveM.setRemarks(request.getRemarks());
            receiveM.setStatus(receivedStatusM);
            receiveM.setReceivedDepartment(receivingDept);
            receiveM.setStoreDepartment(storeDept);
            receiveM.setCreatedBy(currentUserName);
            receiveM.setLastUpdateDate(LocalDateTime.now());

            // Check if any rejection exists
            boolean hasRejections = request.getItems().stream()
                    .anyMatch(item -> item.getQtyRejected() != null &&
                            item.getQtyRejected().compareTo(BigDecimal.ZERO) > 0);
            receiveM.setIsReturn(hasRejections ? rejectStatusM : notRejectStatusM);

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
                        .orElseThrow(() -> new RuntimeException(AppConstants.INDENT_DETAILS_NOT_FOUND_ERR_MSG));

                // Validate indent belongs to the master
                if (!indentT.getIndentM().getIndentMId().equals(indentM.getIndentMId())) {
                    throw new RuntimeException(AppConstants.INDENT_DETAILS_NOT_BELONG_TO_INDENT_M_WARN_MSG);
                }

                BigDecimal qtyIssued = nvl(itemReq.getQtyIssued());
                BigDecimal qtyReceived = nvl(itemReq.getQtyReceived());
                BigDecimal qtyRejected = nvl(itemReq.getQtyRejected());

                // Get the corresponding issue transaction
                List<StoreIssueT> issueTs = storeIssueTRepository.findByIndentTIdAndBatchNo(
                        indentT,
                        itemReq.getBatchNo()
                );

                if (issueTs.isEmpty()) {
                    throw new RuntimeException(AppConstants.INDENT_ISSUE_DETAILS_NOT_FOUND_ERR_MSG);
                }

                // Get the specific batch issue transaction
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
                receiveT.setCreatedBy(currentUserName);
                receiveT.setLastUpdateDate(LocalDateTime.now());
                StoreIndentReceiveT savedReceiveT = storeReceiveTRepository.save(receiveT);

                // ==============================================
                // 4. Update indent detail with received quantity
                // ==============================================
                BigDecimal previousReceived = nvl(indentT.getReceivedQty());
                BigDecimal newTotalReceived = previousReceived.add(qtyReceived);
                indentT.setReceivedQty(newTotalReceived);
                indentTRepository.save(indentT);


                // ==============================================
                // 5. Handle rejected items (prepare for return)
                // ==============================================
                if (qtyRejected.compareTo(BigDecimal.ZERO) > 0) {
                    createReturn = true;
                    returnItems.add(new StoreReturnItemDetail(
                            savedReceiveT,
                            issueT,
                            indentT.getItemId(),
                            issueT.getStockId(),
                            qtyRejected
                    ));


                }

                // ==============================================
                // 6. Create ledger entry for received quantity
                // ==============================================
                if (qtyReceived.compareTo(BigDecimal.ZERO) > 0) {

                    // 7. Update batch stock if received quantity > 0

                    StoreItemBatchStock storeItemBatchStock = updateBatchStockForReceiving(indentT, issueT, qtyReceived, currentUserName);

                    createReceivingLedgerEntry(
                            storeItemBatchStock,
                            qtyReceived,
                            indentT,
                            issueT.getStockId().getStockId(),
                            "RECEIVED AGAINST ISSUE NO: " + indentM.getIssueNo() + " BATCH: " + issueT.getBatchNo(),
                            currentUserName
                    );
                }
            }

            // ==============================================
            // 8. Update indent master status and receiving info
            // ==============================================
            indentM.setReceivedBy(currentUserName);
            indentM.setStatus(AppConstants.INDENT_RECEIVED_AT_REQ_DEPT);
            indentM.setReceivedDate(LocalDateTime.now());
            indentM.setIsReturn(hasRejections ? AppConstants.STATUS_N : AppConstants.STATUS_Y);

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
            response.setMessage(AppConstants.INDENT_RECEIVE_SUCCESS_MSG);

            if (createReturn) {
                response.setReturnCreated(true);
                response.setReturnMessage(AppConstants.RETURN_CREATED_FOR_REJECTED_ITEMS_MSG);
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
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<IndentDetailsResponseForRequestDept>> getIndentDetailsForRequestingDept(Long indentMId, Long deptId) {

        try {
            log.info("getIndentDetailsForRequestingDept method started for indentMId - {} and deptId {}:: ", indentMId, deptId);
            List<IndentDetailsResponseForRequestDept> response =
                    indentTRepository.getIndentDetailsForRequestDept(
                                    indentMId,
                                    deptIdStore,
                                    deptId,
                                    LocalDate.now().plusDays(storeDrugExpDay),
                                    LocalDate.now().plusDays(wardPharmDrugExpDay)
                            ).stream()
                            .map(r -> new IndentDetailsResponseForRequestDept(
                                    r.getIndentTId(),
                                    r.getItemName(),
                                    r.getItemUnitName(),
                                    r.getQtyRequested(),
                                    r.getQtyApproved(),
                                    r.getQtyReceived(),
                                    r.getReasonForIndent(),
                                    r.getStoreAvailableStock(),
                                    r.getCurrentDeptAvailableStock()
                            ))
                            .toList();

            log.info("getIndentDetailsForRequestingDept method ended for indentMId - {} and deptId {}:: ", indentMId, deptId);
            return ResponseUtils.createSuccessResponse(
                    response,
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.info("getIndentDetailsForRequestingDept method error for indentMId - {} and deptId {} :: ", indentMId, deptId, e);
             return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }

    }

    @Override
    @Transactional
    public ApiResponse<String> saveOpeningBalanceEntry(OpeningBalanceEntryRequest openingBalanceEntryRequest) {



        // Save HD record
        StoreBalanceHd hd = new StoreBalanceHd();
        MasDepartment depObj = masDepartmentRepository.findById(openingBalanceEntryRequest.getDepartmentId()).orElseThrow(()-> new RuntimeException("Department not found"));
        hd.setHospitalId(authUtil.getCurrentUser().getHospital());
        hd.setDepartmentId(depObj);
        hd.setEnteredBy(openingBalanceEntryRequest.getEnteredBy());
        String orderNum = createInvoice();
        hd.setBalanceNo(orderNum);
        hd.setEnteredDt(LocalDateTime.now());
        hd.setStatus(AppConstants.BALANCE_SAVED_STATUS.toLowerCase()); // status = saved
        hd.setLastUpdatedDt(LocalDateTime.now());
        String balanceType;
        if( masStoreSectionRepository.existsById(sectionIdForDrugs.intValue())){
            balanceType= AppConstants.ITEM_TYPE_DRUG;
        }else{
            balanceType=AppConstants.ITEM_TYPE_NON_DRUG;
        }
        hd.setBalanceType(balanceType);
        StoreBalanceHd savedHd = storeBalanceHdRepository.save(hd);


        List<OpeningBalanceDtRequest> dtRequests = openingBalanceEntryRequest.getStoreBalanceDtList();


        //  save DT records
        List<StoreBalanceDt> dtList = new ArrayList<>();
        for (OpeningBalanceDtRequest dtRequest : dtRequests) {
            StoreBalanceDt dt = new StoreBalanceDt();
            dt.setBalanceMId(savedHd);
            Optional<MasStoreItem> masStoreItem = masStoreItemRepository.findById(dtRequest.getItemId());
            if (masStoreItem.isEmpty()) {
                return ResponseUtils.createNotFoundResponse(AppConstants.ITEM_NOT_FOUND_ERR_MSG, HttpStatus.NOT_FOUND.value());
            }
            dt.setItemId(masStoreItem.get());
            MasHSN hsnObj = masStoreItem.get().getHsnCode();
            dt.setHsnCode(hsnObj);
            dt.setGstPercent(dtRequest.getGstPercent());
            dt.setBatchNo(dtRequest.getBatchNo());
            dt.setManufactureDate(dtRequest.getManufactureDate());
            dt.setExpiryDate(dtRequest.getExpiryDate());
            dt.setQty(dtRequest.getQty());
            dt.setUnitsPerPack(dtRequest.getUnitsPerPack());
            dt.setPurchaseRatePerUnit(dtRequest.getPurchaseRatePerUnit());
            dt.setTotalMrp(dtRequest.getTotalMrp());
            dt.setMrpPerUnit(dtRequest.getMrpPerUnit());

            // GST and base rate calculations
            BigDecimal gst = dtRequest.getGstPercent();
            BigDecimal purchaseRatePerUnit = dtRequest.getPurchaseRatePerUnit();
            BigDecimal divisor = BigDecimal.ONE.add(gst.divide(BigDecimal.valueOf(100)));
            BigDecimal basePrice = purchaseRatePerUnit.divide(divisor, 2, RoundingMode.HALF_UP);
            BigDecimal gstAmount = purchaseRatePerUnit.subtract(basePrice);
            dt.setGstAmountPerUnit(gstAmount);
            dt.setBaseRatePerUnit(basePrice);
            Long qty = dtRequest.getQty();
            BigDecimal purRateUnit = dtRequest.getPurchaseRatePerUnit();
            BigDecimal total = purRateUnit.multiply(BigDecimal.valueOf(qty));
            dt.setTotalPurchaseCost(total);
            dt.setBrandId(masBrandRepository.findById(dtRequest.getBrandId()).orElse(null));
            Optional<MasManufacturer> masManufacturer = masManufacturerRepository.findById(dtRequest.getManufacturerId());
            if (masManufacturer.isEmpty()) {
                return ResponseUtils.createNotFoundResponse(AppConstants.MANUFACTURER_NOT_FOUND_ERR_MSG, HttpStatus.NOT_FOUND.value());
            }
            dt.setManufacturerId(masManufacturer.get());
            dtList.add(dt);
        }
        storeBalanceDtRepository.saveAll(dtList);
        return ResponseUtils.createSuccessResponse(
                AppConstants.OPENING_BALANCE_ENTRY_SAVED_SUCCESS_MSG,
                new TypeReference<>() {
                });


    }
    public String createInvoice() {
        return randomNumGenerator.generateOrderNumber(AppConstants.BALANCE_NUM_GENERATION_PREFIX, true, true);
    }


    @Override
    public ApiResponse<Page<OpeningBalanceEntryHeaderResponse>> getOpeningBalanceEntryHeaderListWrtDept(Integer pageNo,Integer pageSize,Long hospitalId, Long deptId,LocalDate fromDate,LocalDate toDate) {
        try {
            log.info("getOpeningBalanceEntryHeaderListWrtDept method started for hospitalId - {} and deptId {}:: ", hospitalId, deptId);
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("enteredDt").descending());

            List<String> statuses= Stream.of(AppConstants.INDENT_CREATED_AT_REQ_DEPT,
                    AppConstants.INDENT_APPROVED_AT_REQ_DEPT,
                    AppConstants.INDENT_REJECTED_AT_REQ_DEPT,
                    AppConstants.BALANCE_SUBMIT_STATUS)
                    .map(String::toLowerCase).toList();

            Page<OpeningBalanceEntryHeaderResponse> page =
                    storeBalanceHdRepository.findOpeningBalanceHeadersWrtDept(
                            hospitalId,
                            deptId,
                            statuses,
                            fromDate!=null?fromDate.atStartOfDay():null,
                            toDate!=null?toDate.atTime(23, 59, 59):null,
                            pageable
                    );
            log.info("getOpeningBalanceEntryHeaderListWrtDept method ended for hospitalId - {} and deptId {}:: ", hospitalId, deptId);
            return  ResponseUtils.createSuccessResponse(
                    page,
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.info("getOpeningBalanceEntryHeaderListWrtDept method error for hospitalId - {} and deptId {} :: ", hospitalId, deptId, e);
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        AppConstants.INTERNAL_SERVER_ERR_MSG,
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                );
        }
    }

    @Override
    public ApiResponse<List<OpeningBalanceEntryDetailResponse>> getOpeningBalanceEntryDetailsWrtHeader(Long balanceMId) {
        try {
            log.info("getOpeningBalanceEntryDetailsWrtHeader method started for balanceMId - {} :: ", balanceMId);
            List<OpeningBalanceEntryDetailResponse> response =
                    storeBalanceDtRepository.findOpeningBalanceDetailsWrtHeader(balanceMId)
                            .stream()
                            .map(r -> new OpeningBalanceEntryDetailResponse(
                                    r.getBalanceTId(),
                                    r.getBalanceMId(),
                                    r.getItemId(),
                                    r.getItemName(),
                                    r.getItemUnit(),
                                    r.getItemGst(),
                                    r.getItemCode(),
                                    r.getBatchNo(),
                                    r.getManufactureDate(),
                                    r.getExpiryDate(),
                                    r.getQty(),
                                    r.getUnitsPerPack(),
                                    r.getPurchaseRatePerUnit(),
                                    r.getGstPercent(),
                                    r.getMrpPerUnit(),
                                    r.getHsnCode(),
                                    r.getBaseRatePerUnit(),
                                    r.getGstAmountPerUnit(),
                                    r.getTotalPurchaseCost(),
                                    r.getTotalMrpValue(),
                                    r.getBrandId(),
                                    r.getManufacturerId(),
                                    r.getBrandName(),
                                    r.getManufacturerName()
                            ))
                            .toList();
            log.info("getOpeningBalanceEntryDetailsWrtHeader method ended for balanceMId - {} :: ", balanceMId);
            return ResponseUtils.createSuccessResponse(
                    response,
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("getOpeningBalanceEntryDetailsWrtHeader method error for balanceMId - {} :: ", balanceMId, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<List<OpeningBalanceEntryDetailResponse>>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<OpeningBalanceEntryHeaderResponse>> getAllOpeningBalanceEntryHeadersWrtDeptWithOutPagination(Long hospitalId, Long deptId) {
       try {
           log.info("getAllOpeningBalanceEntryHeadersWrtDeptWithOutPagination method started for hospitalId - {} and deptId {}:: ", hospitalId, deptId);
           List<OpeningBalanceEntryHeaderResponse> responses = storeBalanceHdRepository.findOpeningBalanceHeadersWrtDeptWithoutPagination(hospitalId, deptId, AppConstants.BALANCE_SUBMIT_STATUS.toLowerCase());
           log.info("getAllOpeningBalanceEntryHeadersWrtDeptWithOutPagination method ended for hospitalId - {} and deptId {}:: ", hospitalId, deptId);
           return ResponseUtils.createSuccessResponse(
                   responses,
                   new TypeReference<>() {}
           );
       } catch (Exception e) {
            log.error("getAllOpeningBalanceEntryHeadersWrtDeptWithOutPagination method error for hospitalId - {} and deptId {} :: ", hospitalId, deptId, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
       }
    }

    @Override
    public ApiResponse<String> createOpeningBalanceEntryAndUpdateStatus(OpeningBalanceEntryRequest request) {
        StoreBalanceHd hd = new StoreBalanceHd();
        MasDepartment depObj = masDepartmentRepository.findById(request.getDepartmentId()).orElseThrow(()-> new RuntimeException("Department not found"));
        hd.setHospitalId(authUtil.getCurrentUser().getHospital());
        hd.setDepartmentId(depObj);
        hd.setEnteredBy(request.getEnteredBy());
        String orderNum = createInvoice();
        hd.setBalanceNo(orderNum);
        hd.setEnteredDt(LocalDateTime.now());
        hd.setStatus(AppConstants.BALANCE_SUBMIT_STATUS.toLowerCase()); // status = saved
        hd.setLastUpdatedDt(LocalDateTime.now());
        String balanceType;
        if( masStoreSectionRepository.existsById(sectionIdForDrugs.intValue())){
            balanceType= AppConstants.ITEM_TYPE_DRUG;
        }else{
            balanceType=AppConstants.ITEM_TYPE_NON_DRUG;
        }
        hd.setBalanceType(balanceType);
        StoreBalanceHd savedHd = storeBalanceHdRepository.save(hd);


        List<OpeningBalanceDtRequest> dtRequests = request.getStoreBalanceDtList();


        //  save DT records
        List<StoreBalanceDt> dtList = new ArrayList<>();
        for (OpeningBalanceDtRequest dtRequest : dtRequests) {
            StoreBalanceDt dt = new StoreBalanceDt();
            dt.setBalanceMId(savedHd);
            Optional<MasStoreItem> masStoreItem = masStoreItemRepository.findById(dtRequest.getItemId());
            if (masStoreItem.isEmpty()) {
                return ResponseUtils.createNotFoundResponse(AppConstants.ITEM_NOT_FOUND_ERR_MSG, HttpStatus.NOT_FOUND.value());
            }
            dt.setItemId(masStoreItem.get());
            MasHSN hsnObj = masStoreItem.get().getHsnCode();
            dt.setHsnCode(hsnObj);
            dt.setGstPercent(dtRequest.getGstPercent());
            dt.setBatchNo(dtRequest.getBatchNo());
            dt.setManufactureDate(dtRequest.getManufactureDate());
            dt.setExpiryDate(dtRequest.getExpiryDate());
            dt.setQty(dtRequest.getQty());
            dt.setUnitsPerPack(dtRequest.getUnitsPerPack());
            dt.setPurchaseRatePerUnit(dtRequest.getPurchaseRatePerUnit());
            dt.setTotalMrp(dtRequest.getTotalMrp());
            dt.setMrpPerUnit(dtRequest.getMrpPerUnit());

            // GST and base rate calculations
            BigDecimal gst = dtRequest.getGstPercent();
            BigDecimal purchaseRatePerUnit = dtRequest.getPurchaseRatePerUnit();
            BigDecimal divisor = BigDecimal.ONE.add(gst.divide(BigDecimal.valueOf(100)));
            BigDecimal basePrice = purchaseRatePerUnit.divide(divisor, 2, RoundingMode.HALF_UP);
            BigDecimal gstAmount = purchaseRatePerUnit.subtract(basePrice);
            dt.setGstAmountPerUnit(gstAmount);
            dt.setBaseRatePerUnit(basePrice);
            Long qty = dtRequest.getQty();
            BigDecimal purRateUnit = dtRequest.getPurchaseRatePerUnit();
            BigDecimal total = purRateUnit.multiply(BigDecimal.valueOf(qty));
            dt.setTotalPurchaseCost(total);
            dt.setBrandId(masBrandRepository.findById(dtRequest.getBrandId()).orElse(null));
            Optional<MasManufacturer> masManufacturer = masManufacturerRepository.findById(dtRequest.getManufacturerId());
            if (masManufacturer.isEmpty()) {
                return ResponseUtils.createNotFoundResponse(AppConstants.MANUFACTURER_NOT_FOUND_ERR_MSG, HttpStatus.NOT_FOUND.value());
            }
            dt.setManufacturerId(masManufacturer.get());
            dtList.add(dt);
        }
        storeBalanceDtRepository.saveAll(dtList);
        return ResponseUtils.createSuccessResponse(AppConstants.OPENING_BALANCE_ENTRY_SUBMIT_SUCCESS_MSG, new TypeReference<>() {
        });
    }

    @Override
    @Transactional
    public ApiResponse<String> updateOpeningBalanceById(Long id, OpeningBalanceEntryRequest openingBalanceEntryRequest) {



        Optional<StoreBalanceHd> optionalHd = storeBalanceHdRepository.findById(id);

        if (optionalHd.isEmpty()) {
            return ResponseUtils.createNotFoundResponse(AppConstants.OPENING_BALANCE_HEADER_NOT_FOUND_ERR_MSG, HttpStatus.NOT_FOUND.value());
        }

        addDetails(openingBalanceEntryRequest.getStoreBalanceDtList(), id);

        if (openingBalanceEntryRequest.getDeletedDt() != null && !openingBalanceEntryRequest.getDeletedDt().isEmpty()) {
            for (Long ids : openingBalanceEntryRequest.getDeletedDt()) {
                deletedById(ids);
            }
        }

        StoreBalanceHd hd = optionalHd.get();

        // Update HD fields
        MasDepartment depObj = masDepartmentRepository.getById(openingBalanceEntryRequest.getDepartmentId());
        hd.setDepartmentId(depObj);
        hd.setEnteredBy(openingBalanceEntryRequest.getEnteredBy());
        hd.setLastUpdatedDt(LocalDateTime.now());
        if (openingBalanceEntryRequest.getStatus().equalsIgnoreCase(AppConstants.BALANCE_SAVED_STATUS) ) {
            hd.setStatus(AppConstants.BALANCE_SAVED_STATUS.toLowerCase());
        } else if (openingBalanceEntryRequest.getStatus().equalsIgnoreCase(AppConstants.BALANCE_SUBMIT_STATUS)) {
            hd.setStatus(AppConstants.BALANCE_SUBMIT_STATUS.toLowerCase());
        }
        StoreBalanceHd updatedHd = storeBalanceHdRepository.save(hd);
        return ResponseUtils.createSuccessResponse(AppConstants.SUCCESS_MSG, new TypeReference<>() {
        });

    }

    @Transactional
    @Override
    public ApiResponse<String> approveOpeningBalance(Long id, OpeningBalanceRequestForApprove request) {
        User currentUser = authUtil.getCurrentUser();


        Optional<StoreBalanceHd> hdOpt = storeBalanceHdRepository.findById(id);
        if (hdOpt.isEmpty()) {
            return ResponseUtils.createNotFoundResponse(AppConstants.OPENING_BALANCE_HEADER_NOT_FOUND_ERR_MSG, 404);
        }

        String fName = currentUser.getFirstName() + " " + currentUser.getMiddleName() + " " + currentUser.getLastName();

        StoreBalanceHd hd = hdOpt.get();
        hd.setStatus(request.getStatus());
        hd.setApprovalDt(LocalDateTime.now());
        hd.setRemarks(request.getRemark());
        hd.setApprovedBy(fName);
        StoreBalanceHd hdObj = storeBalanceHdRepository.save(hd);

        if (AppConstants.BALANCE_APPROVE_STATUS.equalsIgnoreCase(request.getStatus())) {

            List<StoreBalanceDt> dtList = storeBalanceDtRepository.findByBalanceMId(hd);
            Map<String, StoreItemBatchStock> stockMap = new HashMap<>();

            for (StoreBalanceDt dt : dtList) {
                if (Boolean.TRUE.equals(dt.getIsApproved())) {
                    continue;
                }

                String batchNo = dt.getBatchNo().trim().toUpperCase();
                String key = dt.getItemId().getItemId() + "_" +
                        batchNo + "_" +
                        dt.getManufactureDate() + "_" +
                        dt.getExpiryDate() + "_" +
                        dt.getManufacturerId().getManufacturerId();

                StoreItemBatchStock stock;

                if (stockMap.containsKey(key)) {
                    stock = stockMap.get(key);
                    Long qty = dt.getQty();
                    stock.setQty(stock.getQty() + qty);
                    stock.setClosingStock(stock.getClosingStock() + qty);
                    stock.setOpeningBalanceQty(stock.getOpeningBalanceQty() + qty);
                    transferInLedger(stock.getQty(),qty, dt.getBalanceTId(), stock.getStockId(), hdObj.getRemarks(),hdObj.getBalanceNo());
                } else {
                    Optional<StoreItemBatchStock> existingStockOpt = storeItemBatchStockRepository.findMatchingStock(
                            dt.getItemId(),
                            batchNo,
                            dt.getManufactureDate(),
                            dt.getExpiryDate(),
                            dt.getManufacturerId().getManufacturerId()
                    );

                    if (existingStockOpt.isPresent()) {
                        stock = existingStockOpt.get();
                        Long qty = dt.getQty();
                        stock.setClosingStock(stock.getClosingStock() + qty);
                        stock.setOpeningBalanceQty(stock.getOpeningBalanceQty() + qty);
                        transferInLedger(stock.getClosingStock(),qty, dt.getBalanceTId(), stock.getStockId(), hdObj.getRemarks(), hd.getBalanceNo());
                    } else {
                        Long deptId = authUtil.getCurrentDepartmentId();
                        MasDepartment department = masDepartmentRepository.getById(deptId);

                        stock = new StoreItemBatchStock();
                        stock.setHospitalId(currentUser.getHospital());
                        stock.setDepartmentId(department);
                        stock.setItemId(dt.getItemId());
                        stock.setManufacturerId(dt.getManufacturerId());
                        stock.setBatchNo(batchNo);
                        stock.setManufactureDate(dt.getManufactureDate());
                        stock.setExpiryDate(dt.getExpiryDate());
                        stock.setOpeningBalanceQty(dt.getQty());
                        stock.setClosingStock(dt.getQty());
                        stock.setUnitsPerPack(dt.getUnitsPerPack());
                        stock.setPurchaseRatePerUnit(dt.getPurchaseRatePerUnit());
                        stock.setGstPercent(dt.getGstPercent());
                        stock.setMrpPerUnit(dt.getMrpPerUnit());
                        stock.setHsnCode(dt.getHsnCode());
                        stock.setGstAmountPerUnit(dt.getGstAmountPerUnit());
                        stock.setTotalPurchaseCost(dt.getTotalPurchaseCost());
                        stock.setTotalMrpValue(dt.getTotalMrp());
                        stock.setBrandId(dt.getBrandId());
                        stock.setLastChgDate(LocalDateTime.now());


                        stock.setLastChgBy(fName);

                        stock = storeItemBatchStockRepository.save(stock);

                        transferInLedger(0,dt.getQty(), dt.getBalanceTId(), stock.getStockId(), hdObj.getRemarks(),hdObj.getBalanceNo());
                    }

                    stock.setLastChgDate(LocalDateTime.now());
                    stock.setLastChgBy(currentUser.getUsername());

                    stockMap.put(key, stock);
                }

                dt.setIsApproved(true);
            }

            storeItemBatchStockRepository.saveAll(stockMap.values());
            storeBalanceDtRepository.saveAll(dtList);
        }

        return ResponseUtils.createSuccessResponse("Approved and stock moved to batch successfully", new TypeReference<>() {
        });
    }

    private String transferInLedger(long qtyBefore,long qty, long balanceDtId, long stockId, String remarks,String referenceNum) {
        Optional<StoreItemBatchStock> stockOpt = storeItemBatchStockRepository.findById(stockId);
        if (stockOpt.isEmpty()) {
            throw new EntityNotFoundException("Stock with ID " + stockId + " not found.");
        }
        StoreItemBatchStock stock = stockOpt.get();
        StoreStockLedger ledger = new StoreStockLedger();
        ledger.setCreatedDt(LocalDateTime.now());
        User currentUser = authUtil.getCurrentUser();
        String fName= currentUser.getFirstName() + " " + currentUser.getMiddleName() + " " + currentUser.getLastName();

        if (currentUser != null) {
            ledger.setCreatedBy(fName);
        }
        ledger.setTxnDate(LocalDate.now());
        ledger.setQtyIn(BigDecimal.valueOf(qty));
        ledger.setStockId(stock);
        ledger.setQtyBefore(BigDecimal.valueOf(stock.getClosingStock()));
        ledger.setQtyAfter(BigDecimal.valueOf(stock.getClosingStock()+qty));
        ledger.setReferenceNum(referenceNum);
        ledger.setHospital(authUtil.getCurrentUser().getHospital());
        ledger.setDept(masDepartmentRepository.findById(authUtil.getCurrentDepartmentId()).orElseThrow(()-> new RuntimeException("Department Not Found")));
        ledger.setTxnSource(opTxnType);
        ledger.setTxnType(opTxnType);
        ledger.setRemarks(remarks);
        ledger.setTxnReferenceId(balanceDtId);
        storeStockLedgerRepository.save(ledger);
        return "success";
    }


    public String addDetails(List<OpeningBalanceDtRequest> openingBalanceDtRequest, long hdId) {
        for (OpeningBalanceDtRequest dtRequest :openingBalanceDtRequest) {
            if (dtRequest.getBalanceId() == null) {

                StoreBalanceDt dt = new StoreBalanceDt();
                Optional<StoreBalanceHd> optionalHd = storeBalanceHdRepository.findById(hdId);
                if (optionalHd.isEmpty()) {
                    return AppConstants.OPENING_BALANCE_HEADER_NOT_FOUND_ERR_MSG;
                }
                dt.setBalanceMId(optionalHd.get());
                Optional<MasStoreItem> masStoreItem = masStoreItemRepository.findById(dtRequest.getItemId());
                if (masStoreItem.isEmpty()) {
                    return AppConstants.ITEM_NOT_FOUND_ERR_MSG;
                }

                dt.setItemId(masStoreItem.get());
                MasHSN hsnObj = masStoreItem.get().getHsnCode();
                dt.setHsnCode(hsnObj);
                dt.setGstPercent(dtRequest.getGstPercent());
                dt.setBatchNo(dtRequest.getBatchNo());
                dt.setManufactureDate(dtRequest.getManufactureDate());
                dt.setExpiryDate(dtRequest.getExpiryDate());
                dt.setQty(dtRequest.getQty());
                dt.setUnitsPerPack(dtRequest.getUnitsPerPack());
                dt.setPurchaseRatePerUnit(dtRequest.getPurchaseRatePerUnit());
                dt.setTotalMrp(dtRequest.getTotalMrp());
                dt.setMrpPerUnit(dtRequest.getMrpPerUnit());

                // GST and base rate calculations
                BigDecimal gst = dtRequest.getGstPercent();
                BigDecimal purchaseRatePerUnit = dtRequest.getPurchaseRatePerUnit();
                BigDecimal divisor = BigDecimal.ONE.add(gst.divide(BigDecimal.valueOf(100)));
                BigDecimal basePrice = purchaseRatePerUnit.divide(divisor, 2, RoundingMode.HALF_UP);
                BigDecimal gstAmount = purchaseRatePerUnit.subtract(basePrice);

                dt.setGstAmountPerUnit(gstAmount);
                dt.setBaseRatePerUnit(basePrice);

                Long qty = dtRequest.getQty();
                BigDecimal purRateUnit = dtRequest.getPurchaseRatePerUnit();
                BigDecimal total= purRateUnit.multiply(BigDecimal.valueOf(qty));
                dt.setTotalPurchaseCost(total);

                dt.setBrandId(masBrandRepository.findById(dtRequest.getBrandId()).orElse(null));
                Optional<MasManufacturer> masManufacturer = masManufacturerRepository.findById(dtRequest.getManufacturerId());
                if (masManufacturer.isEmpty()) {
                    return AppConstants.MANUFACTURER_NOT_FOUND_ERR_MSG;
                }
                dt.setManufacturerId(masManufacturer.get());
                storeBalanceDtRepository.save(dt);
            } else {
                Optional<StoreBalanceDt> storeBalanceDt=storeBalanceDtRepository.findById(dtRequest.getBalanceId());
                if(storeBalanceDt.isEmpty()){
                    return AppConstants.OPENING_BALANCE_DETAILS_NOT_FOUND_ERR_MSG;
                }
                StoreBalanceDt dt =storeBalanceDt.get();
                Optional<MasStoreItem> masStoreItem = masStoreItemRepository.findById(dtRequest.getItemId());
                if (masStoreItem.isEmpty()) {
                    return AppConstants.ITEM_NOT_FOUND_ERR_MSG;
                }
                dt.setItemId(masStoreItem.get());
                dt.setHsnCode(masStoreItem.get().getHsnCode());
                dt.setGstPercent(dtRequest.getGstPercent());
                dt.setBatchNo(dtRequest.getBatchNo());
                dt.setManufactureDate(dtRequest.getManufactureDate());
                dt.setExpiryDate(dtRequest.getExpiryDate());
                dt.setQty(dtRequest.getQty());
                dt.setUnitsPerPack(dtRequest.getUnitsPerPack());
                dt.setPurchaseRatePerUnit(dtRequest.getPurchaseRatePerUnit());
                dt.setTotalMrp(dtRequest.getTotalMrp());
                dt.setMrpPerUnit(dtRequest.getMrpPerUnit());
                BigDecimal gst = dtRequest.getGstPercent();
                BigDecimal purchaseRatePerUnit = dtRequest.getPurchaseRatePerUnit();
                BigDecimal divisor = BigDecimal.ONE.add(gst.divide(BigDecimal.valueOf(100)));
                BigDecimal basePrice = purchaseRatePerUnit.divide(divisor, 2, RoundingMode.HALF_UP);
                BigDecimal gstAmount = purchaseRatePerUnit.subtract(basePrice);

                dt.setGstAmountPerUnit(gstAmount);
                dt.setBaseRatePerUnit(basePrice);

                BigDecimal total = dtRequest.getPurchaseRatePerUnit().multiply(BigDecimal.valueOf(dtRequest.getQty()));
                dt.setTotalPurchaseCost(total);

                dt.setBrandId(masBrandRepository.findById(dtRequest.getBrandId()).orElse(null));
                Optional<MasManufacturer> masManufacturer = masManufacturerRepository.findById(dtRequest.getManufacturerId());
                if (masManufacturer.isEmpty()) {
                    return AppConstants.MANUFACTURER_NOT_FOUND_ERR_MSG;
                }
                dt.setManufacturerId(masManufacturer.get());

                storeBalanceDtRepository.save(dt);
            }
        }
        return AppConstants.SUCCESS_MSG;
    }

    private void  deletedById(Long id){
        storeBalanceDtRepository.deleteById(id);
    }


    private void createReceivingLedgerEntry(StoreItemBatchStock batchStock,BigDecimal qty, StoreInternalIndentT indentT, Long stockId,
                                            String remarks,  String userName) {

        StoreItemBatchStock stock = storeItemBatchStockRepository.findById(stockId)
                .orElseThrow(() -> new EntityNotFoundException(AppConstants.STOCK_NOT_FOUND_ERR_MSG));

        StoreStockLedger ledger = new StoreStockLedger();
        ledger.setCreatedDt(LocalDateTime.now());
        ledger.setCreatedBy(userName);
        ledger.setTxnDate(LocalDate.now());
        ledger.setQtyIn(qty);
        ledger.setQtyOut(null);
        ledger.setStockId(stock);
        ledger.setTxnType(AppConstants.TRANSACTION_TYPE_AND_SOURCE_RECEIVE);
        ledger.setRemarks(remarks);
        ledger.setTxnReferenceId(indentT.getIndentTId());
        ledger.setTxnSource(AppConstants.TRANSACTION_TYPE_AND_SOURCE_RECEIVE);
        ledger.setDept(masDepartmentRepository.findById(authUtil.getCurrentDepartmentId()).orElseThrow(()-> new RuntimeException("Invalid Department ID")));
        ledger.setHospital(authUtil.getCurrentUser().getHospital());
        ledger.setQtyBefore(batchStock.getClosingStock()>0?BigDecimal.valueOf(batchStock.getClosingStock()).subtract(qty):BigDecimal.ZERO);
        ledger.setQtyAfter(BigDecimal.valueOf(batchStock.getClosingStock()));
        ledger.setReferenceNum(indentT.getIndentM().getIssueNo());

        storeStockLedgerRepository.save(ledger);
    }


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

    private StoreItemBatchStock updateBatchStockForReceiving(
            StoreInternalIndentT indentT,
            StoreIssueT issueT,
            BigDecimal qtyReceived,
            String userName) {

        StoreItemBatchStock stock = issueT.getStockId();

        if (stock == null) {
            // If no specific stock record, find the batch in receiving department
            Long receivingDeptId = authUtil.getCurrentDepartmentId();
            MasDepartment receivingDept =
                    masDepartmentRepository.findById(receivingDeptId).orElse(null);

            if (receivingDept == null) {
                return null;
            }

            List<StoreItemBatchStock> stocks =
                    storeItemBatchStockRepository.findByDepartmentIdAndItemId(
                            receivingDept,
                            indentT.getItemId()
                    );
            if (!stocks.isEmpty()) {
                stock = stocks.get(0); // Use first batch
            }
        }
        if (stock == null) {
            return null;
        }
        Long currentStock =
                stock.getClosingStock() != null ? stock.getClosingStock() : 0L;

        Long receivedQty = qtyReceived.longValue();

        stock.setClosingStock(currentStock + receivedQty);
        stock.setIndentReceivedQty(
                (stock.getIndentReceivedQty() != null
                        ? stock.getIndentReceivedQty()
                        : 0L) + receivedQty
        );
        stock.setLastChgBy(userName);
        stock.setLastChgDate(LocalDateTime.now());

        return storeItemBatchStockRepository.save(stock);
    }

    private void createStoreReturn(StoreIndentReceiveM receiveM,
                                   List<InventoryServiceImpl.StoreReturnItemDetail> returnItems, String userName) {

        // ==============================================
        // 1. Create Store Return Master
        // ==============================================
        StoreReturnM returnM = new StoreReturnM();
        String returnNo = generateReturnNumber();
        returnM.setStoreIndentReceiveM(receiveM);
        returnM.setStoreDepartment(receiveM.getStoreDepartment());
        returnM.setReturnDate(LocalDateTime.now());
        returnM.setReturnedBy(userName);
        returnM.setLastUpdatedBy(userName);
        returnM.setReceivedBy(null); // Will be set when store accepts return
        returnM.setRemarks(receiveM.getRemarks());

        returnM.setStatus(AppConstants.STATUS_N);
        returnM.setCreatedBy(userName);
        returnM.setLastUpdateDate(LocalDateTime.now());
        returnM = returnMRepository.save(returnM);
        returnM.setReturnNo(returnNo);

        // ==============================================
        // 2. Create Store Return Transactions
        // ==============================================
        for (InventoryServiceImpl.StoreReturnItemDetail itemDetail : returnItems) {

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
            returnT.setCreatedBy(userName);
            returnT.setLastUpdateDate(LocalDateTime.now());

            storeReturnTRepository.save(returnT);

            //  LEDGER ENTRY FOR RETURN (REJECTED)

            returnedLedger(
                    BigDecimal.valueOf(itemDetail.stock.getClosingStock()),                // qty before (or compute properly if you track)
                    itemDetail.rejectedQty.longValue(),
                    itemDetail.receiveT.getStoreInternalIndentT().getIndentTId(),
                    itemDetail.stock.getStockId(),
                    "REJECTED DURING RECEIVING",
                    "RETURN NO: " + returnNo,
                    returnNo
            );
        }

        // ==============================================
        // 3. Update receive master to indicate return exists
        // ==============================================
        receiveM.setIsReturn(AppConstants.STATUS_N);
        receiveMRepository.save(receiveM);
    }

    private void returnedLedger(BigDecimal qtyBefore,long qtyReturned,Long indentTId,Long stockId,String rejectedReason,String remarks,String referenceNum){

        StoreItemBatchStock stock = storeItemBatchStockRepository.findById(stockId)
                .orElseThrow(() -> new EntityNotFoundException(AppConstants.STOCK_NOT_FOUND_ERR_MSG));

        StoreStockLedger ledger = new StoreStockLedger();
        ledger.setCreatedDt(LocalDateTime.now());

        User currentUser = authUtil.getCurrentUser();
        String fName = currentUser.getFirstName()
                + (currentUser.getMiddleName() != null ? " " + currentUser.getMiddleName() : "")
                + (currentUser.getLastName() != null ? " " + currentUser.getLastName() : "");

        ledger.setCreatedBy(fName.trim());
        ledger.setTxnDate(LocalDate.now());

        ledger.setQtyOut(null);
        ledger.setQtyIn(null);

        ledger.setStockId(stock);
        ledger.setTxnType(AppConstants.TRANSACTION_TYPE_AND_SOURCE_RETURN);                 // you can use config if available
        ledger.setRemarks(remarks);
        ledger.setTxnReferenceId(indentTId);
        ledger.setQtyBefore(qtyBefore);
        ledger.setQtyAfter(qtyBefore);
        ledger.setQtyReject(BigDecimal.valueOf(qtyReturned));
        ledger.setReferenceNum(referenceNum);
        ledger.setDept(masDepartmentRepository.findById(authUtil.getCurrentDepartmentId()).orElseThrow(()-> new RuntimeException("Department not found")));
        ledger.setHospital(currentUser.getHospital());
        ledger.setTxnSource(AppConstants.TRANSACTION_TYPE_AND_SOURCE_RETURN);
        storeStockLedgerRepository.save(ledger);

    }


    private String generateReturnNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(AppConstants.DATE_TIME_FORMAT_FOR_RANDOM_NO_GENERATION));
        return AppConstants.RETURN_NUM_GENERATION_PREFIX+ timestamp;

    }


    private String generateIssueNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(AppConstants.DATE_TIME_FORMAT_FOR_RANDOM_NO_GENERATION));
        return AppConstants.ISSUE_NUM_GENERATION_PREFIX + timestamp;
    }
    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
    private String transferOutLedger(BigDecimal qtyBefore, long qty, Long indentTId, Long stockId, String remarks,String  referenceNum) {

        StoreItemBatchStock stock = storeItemBatchStockRepository.findById(stockId)
                .orElseThrow(() -> new EntityNotFoundException(AppConstants.STOCK_NOT_AVAILABLE_WARN_MSG));

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
        ledger.setTxnType(AppConstants.TRANSACTION_TYPE_AND_SOURCE_ISSUE);                 // you can use config if available
        ledger.setRemarks(remarks);
        ledger.setTxnReferenceId(indentTId);
        ledger.setQtyBefore(qtyBefore);
        ledger.setQtyAfter(qtyBefore.subtract(BigDecimal.valueOf(qty)));
        ledger.setReferenceNum(referenceNum);
        ledger.setDept(masDepartmentRepository.findById(authUtil.getCurrentDepartmentId()).orElseThrow(()-> new RuntimeException("Department not found")));
        ledger.setHospital(currentUser.getHospital());
        ledger.setTxnSource(AppConstants.TRANSACTION_TYPE_AND_SOURCE_ISSUE);
        storeStockLedgerRepository.save(ledger);


        return AppConstants.SUCCESS_MSG;
    }

    @Override
    public ApiResponse<List<StoreIssueMResponse>> getAllIndentsForReceiving(
            Long fromDeptId,
            LocalDate fromDate,
            LocalDate toDate) {

        try {

            log.info("getAllIndentsForReceiving method started for fromDeptId - {} :: ", fromDeptId);
            LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
            LocalDateTime end = toDate != null ? toDate.atTime(23,59,59) : null;

            List<StoreIssueMResponse> data =
                    indentMRepository.findIndentMForReceiving(
                            fromDeptId,
                            start,
                            end
                    );
            log.info("getAllIndentsForReceiving method ended for fromDeptId - {} :: ", fromDeptId);
            return ResponseUtils.createSuccessResponse(
                    data,
                    new TypeReference<List<StoreIssueMResponse>>() {}
            );

        } catch (Exception e) {
            log.error("getAllIndentsForReceiving method error for fromDeptId - {} :: ", fromDeptId, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                     HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }




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

    private MasStoreItemResponse mapToResponse(MasStoreItemProjection p) {

        MasStoreItemResponse r = new MasStoreItemResponse();

        r.setItemId(p.getItemId());
        r.setPvmsNo(p.getPvmsNo());
        r.setNomenclature(p.getNomenclature());
        r.setStatus(p.getStatus());
        r.setLastChgBy(p.getLastChgBy());
        r.setLastChgDate(p.getLastChgDate());
        r.setLastChgTime(p.getLastChgTime());
        r.setAdispQty(p.getAdispQty());

        r.setUnitAU(p.getUnitAU());
        r.setDispUnit(p.getDispUnit());
        r.setSectionId(p.getSectionId());
        r.setItemTypeId(p.getItemTypeId());
        r.setGroupId(p.getGroupId());
        r.setItemClassId(p.getItemClassId());
        r.setMasItemCategoryid(p.getMasItemCategoryid());

        r.setMasItemCategoryName(p.getMasItemCategoryName());
        r.setUnitAuName(p.getUnitAuName());
        r.setDispUnitName(p.getDispUnitName());
        r.setSectionName(p.getSectionName());
        r.setItemTypeName(p.getItemTypeName());
        r.setGroupName(p.getGroupName());
        r.setItemClassName(p.getItemClassName());
        r.setHsnCode(p.getHsnCode());
        r.setHsnGstPercent(p.getHsnGstPercent());

        r.setReOrderLevelDispensary(p.getReOrderLevelDispensary());
        r.setReOrderLevelStore(p.getReOrderLevelStore());

        r.setStorestocks(p.getStorestocks());
        r.setDispstocks(p.getDispstocks());
        r.setWardstocks(p.getWardstocks());

        return r;
    }
    /* ================= ADMIN CHECK ================= */

    private boolean isAdminDepartment(Long deptId) {

        return masDepartmentRepository
                .findById(deptId)
                .map(dept ->
                        AppConstants.DEPARTMENT_NAME_ADMIN.equalsIgnoreCase(
                                dept.getDepartmentName()
                        ) && dept.getId().equals(adminDeptId)
                )
                .orElse(false);
    }

    private List<Long> getFixedDeptIds() {
        return Arrays.stream(fixedDepartmentsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
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

    // Common method to process both save and submit
    private ApiResponse<StoreInternalIndentResponse> processIndent(StoreInternalIndentRequest request, String status) {
        //For Issue Status N for StoreInternalIndentT
        MasCommonStatus masCommonStatus = masCommonStatusRepository.findByEntityNameAndColumnNameAndStatusCode(AppConstants.ENTITY_STORE_INTERNAL_INDENT_T, AppConstants.T_COLUMN_NAME, AppConstants.INDENT_NOT_ISSUED_AT_ISSUE_DEPT)
                .orElseThrow(() -> new RuntimeException(AppConstants.STATUS_NOT_FOUND_ERR_MSG));
        StoreInternalIndentM indentM;
        boolean isNew = (request.getIndentMId() == null);

        User currentUser = authUtil.getCurrentUser();
        String currentUserName = currentUser != null ? currentUser.getFirstName() : "";
        String indentType;
       if( masStoreSectionRepository.existsById(sectionIdForDrugs.intValue())){
           indentType=AppConstants.ITEM_TYPE_DRUG;
       }else{
           indentType=AppConstants.ITEM_TYPE_NON_DRUG;
       }

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
                throw new RuntimeException(AppConstants.CURRENT_DEPT_NOT_FOUND_ERR_MSG);
            }
            MasDepartment fromDept = masDepartmentRepository.findById(deptId)
                    .orElseThrow(() -> new RuntimeException(AppConstants.CURRENT_DEPT_NOT_FOUND_ERR_MSG));
            indentM.setFromDeptId(fromDept);
            indentM.setIndentType(indentType);

        } else {
            // Existing indent
            indentM = indentMRepository.findById(request.getIndentMId())
                    .orElseThrow(() -> new RuntimeException(AppConstants.INDENT_HEADER_NOT_FOUND_ERR_MSG));
        }

        // to department
        if (request.getToDeptId() != null) {
            MasDepartment toDept = masDepartmentRepository.findById(request.getToDeptId())
                    .orElseThrow(() -> new RuntimeException(AppConstants.DEPT_NOT_FOUND_ERR_MSG));
            indentM.setToDeptId(toDept);
        } else {
            indentM.setToDeptId(null);
        }

        // Set status (S for save/draft, Y for submit)
        indentM.setStatus(status);

        // For submitted indents, set approval info and UPDATE indent date
        if (AppConstants.INDENT_SUBMITTED_AT_REQ_DEPT.equals(status)) {
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
                            throw new RuntimeException(AppConstants.INDENT_DETAILS_NOT_FOUND_ERR_MSG);
                        }
                    } else {
                        // If indentTId is provided but not found, treat as new item
                        detail = new StoreInternalIndentT();
                        detail.setIndentM(indentM);

                        MasStoreItem item = masStoreItemRepository.findById(dReq.getItemId())
                                .orElseThrow(() -> new RuntimeException(AppConstants.ITEM_NOT_FOUND_ERR_MSG));
                        detail.setItemId(item);
                        detail.setIssueStatus(masCommonStatus.getStatusCode());
                    }
                } else {
                    // CREATE new item - when no indentTId is provided
                    detail = new StoreInternalIndentT();
                    detail.setIndentM(indentM);

                    MasStoreItem item = masStoreItemRepository.findById(dReq.getItemId())
                            .orElseThrow(() -> new RuntimeException(AppConstants.ITEM_NOT_FOUND_ERR_MSG));
                    detail.setItemId(item);
                    detail.setIssueStatus(masCommonStatus.getStatusCode());
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

    private String generateIndentNo() {
        Optional<StoreInternalIndentM> last = indentMRepository.findTopByOrderByIndentMIdDesc();
        long nextId = last.map(m -> m.getIndentMId() + 1).orElse(1L);
        return AppConstants.INDENT_NUM_GENERATION_PREFIX+ nextId;
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


}
