package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.IndentRequest;
import com.hims.request.IndentTRequest;
import com.hims.response.ApiResponse;
import com.hims.service.IndentService;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private final RandomNumGenerator randomNumGenerator;
    public IndentServiceImpl(RandomNumGenerator randomNumGenerator){
       this.randomNumGenerator=randomNumGenerator;
   }
    public String createInvoice() {
       return randomNumGenerator.generateOrderNumber("IND", true, true);
    }

    @Override
    @Transactional
    public ApiResponse<String> createIndent(IndentRequest indentRequest) {
        StoreInternalIndentM indentM=new StoreInternalIndentM();
        MasDepartment depObj = masDepartmentRepository.getById(indentRequest.getFromDeptId());
        MasDepartment depObj2 = masDepartmentRepository.getById(indentRequest.getToDeptId());
        indentM.setFromDeptId(depObj );
        indentM.setToDeptId(depObj2);
        indentM.setIndentDate(LocalDateTime.now());
        indentM.setCreatedBy(indentRequest.getCreatedBy());
        indentM.setIndentNo( createInvoice());
        //        indentM.setTotalCost();
        //        indentM.setCreatedDate(LocalDateTime.now());
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
        for(IndentTRequest req:indentRequest.getIndentReq()){
            StoreInternalIndentT indentT=new StoreInternalIndentT();
            indentT.setIndentM(indentM);
            Optional<MasStoreItem> masStoreItem=masStoreItemRepository.findById(req.getItemId());
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
    }
}
