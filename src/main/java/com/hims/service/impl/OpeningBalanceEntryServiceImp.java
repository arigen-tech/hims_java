package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.OpeningBalanceDtRequest;
import com.hims.request.OpeningBalanceEntryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpeningBalanceDtResponse;
import com.hims.response.OpeningBalanceEntryResponse;
import com.hims.service.OpeningBalanceEntryService;
import com.hims.utils.AuthUtil;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OpeningBalanceEntryServiceImp implements OpeningBalanceEntryService {
    @Autowired
    private StoreBalanceHdRepository hdRepo;
    @Autowired
    private StoreBalanceDtRepository dtRepo;
    @Autowired
    private MasStoreItemRepository itemRepo;
    @Autowired
    private MasBrandRepository brandRepo;
    @Autowired
    private MasManufacturerRepository manufacturerRepo;
    @Autowired
    UserRepo userRepo;

    @Autowired
    private MasDepartmentRepository masDepartmentRepository;

    @Autowired
    private MasHsnRepository masHsnRepository;

    @Autowired
    AuthUtil authUtil;



    private static final Logger log = LoggerFactory.getLogger(DoctorRosterServicesImpl.class);


    private final RandomNumGenerator randomNumGenerator;
    public OpeningBalanceEntryServiceImp(RandomNumGenerator randomNumGenerator) {
        this.randomNumGenerator = randomNumGenerator;
    }

    public String createInvoice() {
        return randomNumGenerator.generateOrderNumber("BIL",true,true);
    }

    @Override
    @Transactional
    public ApiResponse<OpeningBalanceEntryResponse> add(OpeningBalanceEntryRequest openingBalanceEntryRequest) {
        // entry for hd table
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "HospitalId user not found", HttpStatus.UNAUTHORIZED.value());
        }
        StoreBalanceHd hd = new StoreBalanceHd();
        MasDepartment depObj = masDepartmentRepository.getById(openingBalanceEntryRequest.getDepartmentId());
        hd.setHospitalId(currentUser.getHospital());
        hd.setDepartmentId(depObj);
        hd.setEnteredBy(openingBalanceEntryRequest.getEnteredBy());
        String orderNum = createInvoice();
        hd.setBalanceNo(orderNum);
        hd.setEnteredDt(LocalDateTime.now());
        //hd.setApprovalDt();
        // hd.getApprovalDt();
        hd.setStatus("s");
        hd.setLastUpdatedDt(LocalDateTime.now());
        StoreBalanceHd savedHd = hdRepo.save(hd);


        // entry for dt table
       //  List<OpeningBalanceDtResponse> dtResponseList = new ArrayList<>();
        List<StoreBalanceDt> dtList = new ArrayList<>();
        for (OpeningBalanceDtRequest dtRequest : openingBalanceEntryRequest.getStoreBalanceDtList()) {
            StoreBalanceDt dt = new StoreBalanceDt();
            dt.setBalanceMId(savedHd);
            Optional<MasStoreItem> masStoreItem=itemRepo.findById(dtRequest.getItemId());
            if(masStoreItem.isEmpty()){
                return ResponseUtils.createNotFoundResponse("MasStoreItem not found", 404);

            }
            dt.setItemId(masStoreItem.get());
            MasHSN hsnObj = masStoreItem.get().getHsnCode();
            dt.setHsnCode( hsnObj);
            dt.setGstPercent(dtRequest.getGstPercent());
            dt.setBatchNo(dtRequest.getBatchNo());
            dt.setManufactureDate(dtRequest.getManufactureDate());
            dt.setExpiryDate(dtRequest.getExpiryDate());
            dt.setQty(dtRequest.getQty());
            dt.setUnitsPerPack(dtRequest.getUnitsPerPack());
            dt.setPurchaseRatePerUnit(dtRequest.getPurchaseRatePerUnit());
            dt.setTotalPurchaseCost(dtRequest.getTotalPurchaseCost());
            dt.setMrpPerUnit(dtRequest.getMrpPerUnit());
            BigDecimal gst=dtRequest.getGstPercent();
            BigDecimal purchaseRatePerUnit=dtRequest.getPurchaseRatePerUnit();
            BigDecimal divisor = BigDecimal.ONE.add(gst.divide(BigDecimal.valueOf(100)));
            BigDecimal basePrice = purchaseRatePerUnit.divide(divisor, 2, RoundingMode.HALF_UP);
            BigDecimal gstAmount = purchaseRatePerUnit.subtract(basePrice);
            dt.setGstAmountPerUnit(gstAmount);
            BigDecimal baseRatePerUnit=purchaseRatePerUnit.subtract(gstAmount);;
            dt.setBaseRatePerUnit(baseRatePerUnit);
            Long q= dtRequest.getQty();
            BigDecimal mrpPerUnit=dtRequest.getMrpPerUnit();
            BigDecimal totalMrp= mrpPerUnit.multiply(BigDecimal.valueOf(q
            ));;
            dt.setTotalMrpValue(totalMrp);
            dt.setBrandId(brandRepo.findById(dtRequest.getBrandId()).orElse(null));
            dt.setManufacturerId(manufacturerRepo.findById(dtRequest.getManufacturerId()).orElse(null));
          //  StoreBalanceDt savedDt = dtRepo.save(dt);
          //  dtResponseList.add(convertToDtResponse(savedDt));
            dtList.add(dt);
        }
       dtRepo.saveAll(dtList);
        return ResponseUtils.createSuccessResponse(buildOpeningBalanceEntryResponse(savedHd,dtList), new TypeReference<>() {
        });
    }

    @Override
    @Transactional
    public ApiResponse<OpeningBalanceEntryResponse> update(Long id, OpeningBalanceEntryRequest openingBalanceEntryRequest) {

        Optional<StoreBalanceHd> optionalHd = hdRepo.findById(id);
        if (optionalHd.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("Opening balance entry not found", 404);
        }

        StoreBalanceHd hd = optionalHd.get();
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "HospitalId user not found", HttpStatus.UNAUTHORIZED.value());
        }

        MasDepartment department = masDepartmentRepository.getById(openingBalanceEntryRequest.getDepartmentId());
        hd.setDepartmentId(department);
        hd.setEnteredBy(openingBalanceEntryRequest.getEnteredBy());
        hd.setLastUpdatedDt(LocalDateTime.now());
        hd.setStatus("s");
        hd.setHospitalId(currentUser.getHospital());

        StoreBalanceHd updatedHd = hdRepo.save(hd);

        // Delete existing dt records and replace
       // dtRepo.deleteByBalanceMId(hd); // Custom method needed: void deleteByBalanceMId(StoreBalanceHd hd)

        List<StoreBalanceDt> updatedDtList = new ArrayList<>();
        for (OpeningBalanceDtRequest dtRequest : openingBalanceEntryRequest.getStoreBalanceDtList()) {
            StoreBalanceDt dt = new StoreBalanceDt();
            dt.setBalanceMId(updatedHd);

            Optional<MasStoreItem> masStoreItem = itemRepo.findById(dtRequest.getItemId());
            if (masStoreItem.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("MasStoreItem not found", 404);
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
            dt.setTotalPurchaseCost(dtRequest.getTotalPurchaseCost());
            dt.setMrpPerUnit(dtRequest.getMrpPerUnit());

            // Tax calculation
            BigDecimal gst = dtRequest.getGstPercent();
            BigDecimal purchaseRatePerUnit = dtRequest.getPurchaseRatePerUnit();
            BigDecimal divisor = BigDecimal.ONE.add(gst.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            BigDecimal basePrice = purchaseRatePerUnit.divide(divisor, 2, RoundingMode.HALF_UP);
            BigDecimal gstAmount = purchaseRatePerUnit.subtract(basePrice);

            dt.setGstAmountPerUnit(gstAmount);
            dt.setBaseRatePerUnit(basePrice);

            BigDecimal totalMrp = dt.getMrpPerUnit().multiply(BigDecimal.valueOf(dt.getQty()));
            dt.setTotalMrpValue(totalMrp);

            dt.setBrandId(brandRepo.findById(dtRequest.getBrandId()).orElse(null));
            dt.setManufacturerId(manufacturerRepo.findById(dtRequest.getManufacturerId()).orElse(null));

            updatedDtList.add(dt);
        }

        dtRepo.saveAll(updatedDtList);

        return ResponseUtils.createSuccessResponse(
                buildOpeningBalanceEntryResponse(updatedHd, updatedDtList), new TypeReference<>() {});


    }

    @Override
    public ApiResponse<String> updateByStatus(Long id, String status) {
        Optional<StoreBalanceHd> optionalHd = hdRepo.findById(id);
        if (optionalHd.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("Opening balance entry not found", 404);
        }
        StoreBalanceHd hd = optionalHd.get();
        hd.setStatus(status);
         hdRepo.save(hd);
         return ResponseUtils.createSuccessResponse("StoreBalanceHd status updated to '" + status + "'", new TypeReference<>() {});
    }


    @Override
    public ApiResponse<List<OpeningBalanceEntryResponse>> getListByStatus(String status) {
        List<StoreBalanceHd> hdList = hdRepo.findByStatus(status);
        return (ApiResponse<List<OpeningBalanceEntryResponse>>) hdList.stream()
                .map(hd -> {
                    List<StoreBalanceDt> dtList = dtRepo.findByBalanceMId(hd);
                    return buildOpeningBalanceEntryResponse(hd, dtList);
                })
                .collect(Collectors.toList());

    }

    @Override
    public ApiResponse<OpeningBalanceEntryResponse> getDetailsById(Long id) {
        StoreBalanceHd hd = hdRepo.findById(id).orElseThrow(() -> new RuntimeException("Entry not found"));
        List<StoreBalanceDt> dtList = dtRepo.findByBalanceMId(hd);
       // return buildOpeningBalanceEntryResponse(hd, dtList);
        return ResponseUtils.createSuccessResponse(buildOpeningBalanceEntryResponse(hd, dtList), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<OpeningBalanceEntryResponse> createAndUpdateStatus(OpeningBalanceEntryRequest request) {
        ApiResponse<OpeningBalanceEntryResponse> createResponse = this.add(request);
        updateByStatus(createResponse .getResponse().getBalanceMId(), "p");
        return ResponseUtils.createSuccessResponse(createResponse.getResponse(), new TypeReference<>() {});
    }

    private OpeningBalanceEntryResponse buildOpeningBalanceEntryResponse(StoreBalanceHd hd, List<StoreBalanceDt> dtList) {
        OpeningBalanceEntryResponse response = new OpeningBalanceEntryResponse();
        response.setBalanceMId(hd.getBalanceMId());
        response.setBalanceNo(hd.getBalanceNo());
        response.setHospitalId(hd.getHospitalId().getId());
        response.setDepartmentId(hd.getDepartmentId().getId());
        response.setEnteredBy(hd.getEnteredBy());
        response.setRemarks(hd.getRemarks());
        response.setStatus(hd.getStatus());
        response.setEnteredDt(hd.getEnteredDt());
        response.setApprovedBy(hd.getApprovedBy());
        response.setApprovalDt(hd.getApprovalDt());
        response.setLastUpdatedDt(hd.getLastUpdatedDt());

        List<OpeningBalanceDtResponse> dtResponses = dtList.stream().map(dt -> {
            OpeningBalanceDtResponse res = new OpeningBalanceDtResponse();
            res.setBalanceTId(dt.getBalanceTId());
            res.setBalanceMId(hd.getBalanceMId());
            res.setItemId(dt.getItemId().getItemId());
            res.setBatchNo(dt.getBatchNo());
            res.setManufactureDate(dt.getManufactureDate());
            res.setExpiryDate(dt.getExpiryDate());
            res.setQty(dt.getQty());
            res.setUnitsPerPack(dt.getUnitsPerPack());
            res.setPurchaseRatePerUnit(dt.getPurchaseRatePerUnit());
            res.setGstPercent(dt.getGstPercent());
            res.setMrpPerUnit(dt.getMrpPerUnit());
            res.setHsnCode(dt.getHsnCode().getHsnCode());
            res.setBaseRatePerUnit(dt.getBaseRatePerUnit());
            res.setGstAmountPerUnit(dt.getGstAmountPerUnit());
            res.setTotalPurchaseCost(dt.getTotalPurchaseCost());
            res.setTotalMrpValue(dt.getTotalMrpValue());
            res.setBrandId(dt.getBrandId() != null ? dt.getBrandId().getBrandId() : null);
            res.setBrandName(dt.getBrandId().getBrandName());
            res.setManufacturerId(dt.getManufacturerId() != null ? dt.getManufacturerId().getManufacturerId() : null);
            res.setManufacturerName(dt.getManufacturerId().getManufacturerName());
            return res;
        }).collect(Collectors.toList());

        response.setOpeningBalanceDtResponseList(dtResponses);
        return response;
    }


}
