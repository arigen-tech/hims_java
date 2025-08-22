package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.IndentRequest;
import com.hims.request.IndentTRequest;
import com.hims.response.ApiResponse;
import com.hims.response.IndentResponse;
import com.hims.response.IndentTResponse;
import com.hims.service.IndentService;
import com.hims.utils.AuthUtil;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IndentServiceImpl implements IndentService {
    @Autowired
    private StoreInternalIndentMRepository storeInternalIndentMRepository;
    @Autowired
    private StoreInternalIndentTRepository storeInternalIndentTRepository;
    @Autowired
    private StoreIssueMRepository storeIssueMRepository;
    @Autowired
    private StoreIssueTRepository storeIssueTRepository;
    @Autowired
    private MasDepartmentRepository masDepartmentRepository;
    @Autowired
    private MasStoreItemRepository masStoreItemRepository;
    @Autowired
    AuthUtil authUtil;

    private final RandomNumGenerator randomNumGenerator;

    public IndentServiceImpl(RandomNumGenerator randomNumGenerator) {
        this.randomNumGenerator = randomNumGenerator;
    }

    public String createInvoice() {
        return randomNumGenerator.generateOrderNumber("IND", true, true);
    }

    @Override
    @Transactional
    public ApiResponse<String> createIndent(IndentRequest indentRequest) {
        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            StoreInternalIndentM indentM = new StoreInternalIndentM();
            MasDepartment depObj = masDepartmentRepository.getById(indentRequest.getFromDeptId());
            MasDepartment depObj2 = masDepartmentRepository.getById(indentRequest.getToDeptId());
            indentM.setFromDeptId(depObj);
            indentM.setToDeptId(depObj2);
            indentM.setIndentDate(LocalDateTime.now());
            indentM.setCreatedBy(currentUser.getCreatedBy());
            indentM.setCreatedDate(LocalDateTime.now());
            indentM.setIndentNo(createInvoice());
            //       indentM.setTotalCost();
            //        indentM.setApprovedBy();
//        indentM.setApprovedDate();
//        indentM.setStoreApprovedBy();
//        indentM.setStoreApprovedDate();
//        indentM.setIssuedBy();
//        indentM.setCreatedDate();
//        indentM.setReceivedBy();
//        indentM.setReceivedDate();
//        indentM.setIssueNo();

//        indentM.setStoreIssueMId();
            indentM.setStatus("s");
            storeInternalIndentMRepository.save(indentM);

            List<StoreInternalIndentT> dtList = new ArrayList<>();
            for (IndentTRequest req : indentRequest.getIndentReq()) {
                StoreInternalIndentT indentT = new StoreInternalIndentT();
                indentT.setIndentM(indentM);
                Optional<MasStoreItem> masStoreItem = masStoreItemRepository.findById(req.getItemId());
                indentT.setItemId(masStoreItem.get());
                indentT.setRequestedQty(req.getRequestedQty());
//            indentT.setApprovedQty();
//            indentT.setAvailableStock();
//            indentT.setTotalCost();
//            indentT.setItemCost();
                indentT.setIssueStatus("n");
//            indentT.setIssuedQty();
//            indentT.setReceivedQty();

                dtList.add(indentT);
            }
            storeInternalIndentTRepository.saveAll(dtList);
            return ResponseUtils.createSuccessResponse("create indent successfully", new TypeReference<>() {
            });
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "Failed to create indent: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<String> getIndent(Long id) {
return null;
    }
    private IndentResponse convertedToResponse(StoreInternalIndentM indentM, List<StoreInternalIndentT> indentTList){
        IndentResponse response = new IndentResponse();
        response.setIndentMId(indentM.getIndentMId());
        response.setIndentNo(indentM.getIndentNo());
        response.setIndentDate(indentM.getIndentDate());
       Optional<MasDepartment> depObj = Optional.of(masDepartmentRepository.getById(indentM.getFromDeptId().getId()));
        Optional<MasDepartment> depObj2 = Optional.of(masDepartmentRepository.getById(indentM.getToDeptId().getId()));
        response.setFromDeptId(depObj.isPresent()?depObj.get().getId():null);
        response.setToDeptId(depObj2.isPresent()?depObj2.get().getId():null);
        response.setTotalCost(indentM.getTotalCost());
        response.setCreatedBy(indentM.getCreatedBy());
        response.setCreatedDate(indentM.getCreatedDate());
        response.setApprovedBy(indentM.getApprovedBy());
        response.setApprovedDate(indentM.getApprovedDate());
        response.setStoreApprovedBy(indentM.getStoreApprovedBy());
        response.setStoreApprovedDate(indentM.getStoreApprovedDate());
        response.setIssuedBy(indentM.getIssuedBy());
        response.setIssuedDate(indentM.getIssuedDate());
        response.setReceivedBy(indentM.getReceivedBy());
        response.setReceivedDate(indentM.getReceivedDate());
        response.setIssueNo(indentM.getIssueNo());
        response.setStoreIssueMId(indentM.getStoreIssueMId()!=null?indentM.getStoreIssueMId().getStoreIssueMId():null);
        response.setStatus(indentM.getStatus());

        // Details mapping
        List<IndentTResponse> dtResponses = indentTList.stream().map(dt -> {
            IndentTResponse res = new IndentTResponse();
            res.setIndentTId(dt.getIndentTId());
            res.setIndentM(indentM.getIndentMId());
            Optional<MasStoreItem> masStoreItem=masStoreItemRepository.findById(dt.getItemId().getItemId());
            res.setItemId(masStoreItem.isPresent()?masStoreItem.get().getItemId():null);
            res.setRequestedQty(dt.getRequestedQty());
            res.setApprovedQty(dt.getApprovedQty());
            res.setIssuedQty(dt.getIssuedQty());
            res.setReceivedQty(dt.getReceivedQty());
            res.setAvailableStock(dt.getAvailableStock());
            res.setItemCost(dt.getItemCost());
            res.setTotalCost(dt.getTotalCost());
            res.setIssueStatus(dt.getIssueStatus());
            return res;
        }).collect(Collectors.toList());

        response.setIndentTResponseList(dtResponses);
        return response;


    }
}