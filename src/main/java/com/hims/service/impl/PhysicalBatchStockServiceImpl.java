package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.StoreStockTakingMRequest;
import com.hims.request.StoreStockTakingTRequest;
import com.hims.response.ApiResponse;
import com.hims.service.PhysicalBatchStockService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PhysicalBatchStockServiceImpl implements PhysicalBatchStockService {
    @Autowired
    AuthUtil authUtil;
    @Autowired
    private StoreStockTakingMRepository storeStockTakingMRepository;
    @Autowired
    private StoreStockTakingTRepository storeStockTakingTRepository;
    @Autowired
    private MasDepartmentRepository masDepartmentRepository;
    @Autowired
    private StoreItemBatchStockRepository storeItemBatchStockRepository;
    @Autowired
    private MasStoreItemRepository masStoreItemRepository;



    @Override
    public ApiResponse<String> createPhysicalStock(StoreStockTakingMRequest storeStockTakingM) {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "HospitalId user not found", HttpStatus.UNAUTHORIZED.value());
        }

        long deptId = authUtil.getCurrentDepartmentId();
        MasDepartment depObj = masDepartmentRepository.getById(deptId);

        StoreStockTakingM stock=new StoreStockTakingM();
       stock.setReason(storeStockTakingM.getReasonForTraking());
       stock.setHospitalId(currentUser.getHospital());
       stock.setDepartmentId(depObj);
        stock.setPhysicalDate(LocalDateTime.now());
       stock.setStockTakingNo("Physical");
        stock.setLastChgDate(LocalDateTime.now());
        stock.setStatus("s");
        stock.setCreatedBy(currentUser.getCreatedBy());
        StoreStockTakingM stock1=storeStockTakingMRepository.save(stock);



        List<StoreStockTakingTRequest> list=storeStockTakingM.getStockEntries();
        List<StoreStockTakingT> toList=new ArrayList<>();
        for(StoreStockTakingTRequest list2:list){
            StoreStockTakingT stockT=new StoreStockTakingT();
            stockT.setBatchNo(list2.getBatchNo());
            stockT.setExpiryDate(list2.getDoe());
            stockT.setComputedStock(list2.getComputedStock());
            stockT.setStockDeficient(list2.getStockDeficient());
            Optional<StoreItemBatchStock> stockItemBatchStock=storeItemBatchStockRepository.findById(list2.getStockId());
            if(stockItemBatchStock.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("stock not present", 404);
            }
            stockT.setStockId(stockItemBatchStock.get());
            stockT.setStockSurplus(list2.getStockSurplus());
            Optional<MasStoreItem> masStoreItem=masStoreItemRepository.findById(list2.getItemId());
            if(masStoreItem.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("item not present", 404);
            }
            stockT.setItemId(masStoreItem.get());
            stockT.setTakingMId(stock1);
            stockT.setRemarks(list2.getRemarks());
            stockT.setStoreStockService(list2.getStoreStockService());
            toList.add(stockT);

        }
        storeStockTakingTRepository.saveAll(toList);
        return ResponseUtils.createSuccessResponse(" successfully", new TypeReference<>() {
        });



    }
}
