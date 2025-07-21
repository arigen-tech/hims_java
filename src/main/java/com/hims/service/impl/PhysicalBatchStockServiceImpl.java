package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.OpeningBalanceDtRequest;
import com.hims.request.StoreStockTakingMRequest;
import com.hims.request.StoreStockTakingMRequest2;
import com.hims.request.StoreStockTakingTRequest;
import com.hims.response.ApiResponse;
import com.hims.response.StoreStockTakingMResponse;
import com.hims.response.StoreStockTakingTResponse;
import com.hims.service.PhysicalBatchStockService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Autowired
    private MasHospitalRepository masHospitalRepository;



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
        stock.setCreatedBy(currentUser.getFirstName() + " " + currentUser.getMiddleName() + " " + currentUser.getLastName());

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

    @Override
    public List<StoreStockTakingMResponse> getListByStatusPhysical(String[] statuses) {
        List<StoreStockTakingM> masterList = storeStockTakingMRepository.findByStatusIn(Arrays.asList(statuses));

        return masterList.stream()
                .map(master -> {
                    List<StoreStockTakingT> transactionList = storeStockTakingTRepository.findByTakingMId(master);
                    return convertedResponse(master, transactionList);
                })
                .collect(Collectors.toList());
//        List<StoreStockTakingMResponse> responseList = new ArrayList<>();
//
//        List<StoreStockTakingM> masterList = storeStockTakingMRepository.findByStatusIn(Arrays.asList(statuses));
//
//        for (StoreStockTakingM master : masterList) {
//            List<StoreStockTakingT> transactionList = storeStockTakingTRepository.findByTakingMId(master);
//            StoreStockTakingMResponse response = convertedResponse(master, transactionList);
//            responseList.add(response);
//        }
//
//        return responseList;
    }

    @Override
    public ApiResponse<String> updateByStatus(Long id, String status) {
        Optional<StoreStockTakingM> optionalT = storeStockTakingMRepository.findById(id);
        if (optionalT.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("StoreStockTakingM id not found", 404);
        }
//        if (status != null && status.length() > 1) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
//                    "Status '" + status + "' is too long. Must be a single character.", 400);
//        }
        StoreStockTakingM m = optionalT.get();
        m.setStatus(status);
        storeStockTakingMRepository.save(m);
        return ResponseUtils.createSuccessResponse("StoreStockTakingM  status updated to '" + status + "'", new TypeReference<>() {
        });
    }

    @Override
    public ApiResponse<String> updatePhysicalById(Long id, StoreStockTakingMRequest storeStockTakingMRequest) {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "HospitalId user not found", HttpStatus.UNAUTHORIZED.value());
        }

        Optional<StoreStockTakingM> optionalM = storeStockTakingMRepository.findById(id);

        if (optionalM.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("StoreStockTakingM entry not found with id " + id, 404);
        }

       addDetails(storeStockTakingMRequest.getStockEntries(), id);
        if (storeStockTakingMRequest.getDeletedT() != null && !storeStockTakingMRequest.getDeletedT().isEmpty()) {
            for (Long ids : storeStockTakingMRequest.getDeletedT()) {
                deletedById(ids);
            }
        }

        StoreStockTakingM m = optionalM.get();
        long deptId = authUtil.getCurrentDepartmentId();
        MasDepartment depObj = masDepartmentRepository.getById(deptId);
        m.setDepartmentId(depObj);
        m.setLastChgDate(LocalDateTime.now());
        m.setCreatedBy(currentUser.getCreatedBy());
        if (storeStockTakingMRequest.getStatus().equals("s") || storeStockTakingMRequest.getStatus() == null) {
            m.setStatus("s");
        } else if (storeStockTakingMRequest.getStatus().equals("p")) {
            m.setStatus("p");
        }
        StoreStockTakingM updatedM = storeStockTakingMRepository.save(m);
        return ResponseUtils.createSuccessResponse(" successfully", new TypeReference<>() {
        });

    }

    @Override
    public ApiResponse<String> approvedPhysical(Long id, StoreStockTakingMRequest2 request) {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }
        Optional<StoreStockTakingM> stockM = storeStockTakingMRepository.findById(id);
        if (stockM.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("StoreStockTakingM not found", 404);
        }
        String fName = currentUser.getFirstName() + " " + currentUser.getMiddleName() + " " + currentUser.getLastName();
        StoreStockTakingM  stockM2 = stockM.get();
        stockM2.setApprovedDt(LocalDateTime.now());
        stockM2.setApprovedBy(fName);
        StoreStockTakingM stockM3 = storeStockTakingMRepository.save(stockM2);

return null;
    }


    public String addDetails(List<StoreStockTakingTRequest> storeStockTakingTRequests, long mId) {
        for (StoreStockTakingTRequest tRequest :storeStockTakingTRequests) {
            if (tRequest.getId()== null) {
                StoreStockTakingT t = new StoreStockTakingT();
                Optional<StoreStockTakingM> optionalM =storeStockTakingMRepository.findById(mId);
                if (optionalM.isEmpty()) {
                    return "StoreStockTakingM entry not found with id ";
                }
                t .setBatchNo(tRequest.getBatchNo());
                t .setExpiryDate(tRequest.getDoe());
                t .setComputedStock(tRequest.getComputedStock());
                t .setStockDeficient(tRequest.getStockDeficient());
                Optional<StoreItemBatchStock> stockItemBatchStock=storeItemBatchStockRepository.findById(tRequest.getStockId());
                if(stockItemBatchStock.isEmpty()) {
                    return "stock not present";
                }
                t .setStockId(stockItemBatchStock.get());
                t .setStockSurplus(tRequest.getStockSurplus());
                Optional<MasStoreItem> masStoreItem=masStoreItemRepository.findById(tRequest.getItemId());
                if(masStoreItem.isEmpty()) {
                    return "item not present";
                }
                t .setItemId(masStoreItem.get());
                t .setTakingMId(optionalM.get());
                t .setRemarks(tRequest.getRemarks());
                t.setStoreStockService(tRequest.getStoreStockService());
                storeStockTakingTRepository.save(t);

            } else {
                Optional<StoreStockTakingT> storeStockTakingT=storeStockTakingTRepository.findById(tRequest.getId());
                if(storeStockTakingT.isEmpty()){
                    return " StoreStockTakingT not found";
                }
                StoreStockTakingT storeStockTakingT1=storeStockTakingT.get();
                storeStockTakingT1.setBatchNo(tRequest.getBatchNo());
                storeStockTakingT1.setExpiryDate(tRequest.getDoe());
                storeStockTakingT1.setComputedStock(tRequest.getComputedStock());
                storeStockTakingT1.setStockDeficient(tRequest.getStockDeficient());
                Optional<StoreItemBatchStock> stockItemBatchStock=storeItemBatchStockRepository.findById(tRequest.getStockId());
                if(stockItemBatchStock.isEmpty()) {
                    return "stock not present";
                }
                storeStockTakingT1.setStockId(stockItemBatchStock.get());
                storeStockTakingT1 .setStockSurplus(tRequest.getStockSurplus());
                Optional<MasStoreItem> masStoreItem=masStoreItemRepository.findById(tRequest.getItemId());
                if(masStoreItem.isEmpty()) {
                    return "item not present";
                }
                storeStockTakingT1 .setItemId(masStoreItem.get());
                storeStockTakingT1 .setRemarks(tRequest.getRemarks());
                storeStockTakingT1.setStoreStockService(tRequest.getStoreStockService());
                storeStockTakingTRepository.save(storeStockTakingT1);
            }
        }
        return "successfully";
    }
    private void  deletedById(Long id){
        storeStockTakingTRepository.deleteById(id);
    }



    private StoreStockTakingMResponse convertedResponse(StoreStockTakingM mEntity, List<StoreStockTakingT> tList) {
        StoreStockTakingMResponse response = new StoreStockTakingMResponse();
        response.setTakingMId(mEntity.getTakingMId());
        response.setPhysicalDate(mEntity.getPhysicalDate());
        response.setReason(mEntity.getReason());
        response.setStockTakingNo(mEntity.getStockTakingNo());
        response.setApprovedBy(mEntity.getApprovedBy());
        response.setApprovedDt(mEntity.getApprovedDt());
        response.setHospitalId(mEntity.getHospitalId()!=null?mEntity.getHospitalId().getId():null);
//       Optional<MasHospital> hospital=masHospitalRepository.findById(mEntity.getHospitalId());
       response.setHospitalName(mEntity.getHospitalId().getHospitalName());
        response.setDepartmentId(mEntity.getDepartmentId()!=null?mEntity.getDepartmentId().getId():null);
//        Optional<MasDepartment> masDepartment= masDepartmentRepository.findById(mEntity.getHospitalId().getId());
        response.setDepartmentName( mEntity.getDepartmentId().getDepartmentName());

        response.setLastChgDate(mEntity.getLastChgDate());
        response.setStatus(mEntity.getStatus());
        response.setCreatedBy(mEntity.getCreatedBy());

        List<StoreStockTakingTResponse> tResponses = tList.stream().map(t -> {
            StoreStockTakingTResponse tr = new StoreStockTakingTResponse();
            tr.setTakingTId(t.getTakingTId());
            tr.setBatchNo(t.getBatchNo());
            tr.setExpiryDate(t.getExpiryDate());
            tr.setComputedStock(t.getComputedStock());
            tr.setStoreStockService(t.getStoreStockService());
            tr.setRemarks(t.getRemarks());
            tr.setStockSurplus(t.getStockSurplus());
            tr.setStockDeficient(t.getStockDeficient());
            tr.setStockId(t.getStockId()!=null?t.getStockId().getStockId():null);
            tr.setItemId(t.getItemId()!=null?t.getItemId().getItemId():null);
            tr.setItemCode(t.getItemId().getPvmsNo());
            tr.setItemName(t.getItemId().getNomenclature());
            tr.setTakingMId(mEntity.getTakingMId());
            return tr;
        }).collect(Collectors.toList());
        response.setStoreStockTakingTResponseList(tResponses);
        return response;
    }
}
