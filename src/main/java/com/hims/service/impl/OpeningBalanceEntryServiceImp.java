package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.OpeningBalanceDtRequest;
import com.hims.request.OpeningBalanceEntryRequest;
import com.hims.request.OpeningBalanceEntryRequest2;
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
import java.util.*;
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

        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "HospitalId user not found", HttpStatus.UNAUTHORIZED.value());
        }

        // Save HD record
        StoreBalanceHd hd = new StoreBalanceHd();
        MasDepartment depObj = masDepartmentRepository.getById(openingBalanceEntryRequest.getDepartmentId());
        hd.setHospitalId(currentUser.getHospital());
        hd.setDepartmentId(depObj);
        hd.setEnteredBy(openingBalanceEntryRequest.getEnteredBy());
        String orderNum = createInvoice();
        hd.setBalanceNo(orderNum);
        hd.setEnteredDt(LocalDateTime.now());
        hd.setStatus("s"); // status = saved
        hd.setLastUpdatedDt(LocalDateTime.now());

        StoreBalanceHd savedHd = hdRepo.save(hd);

        // Validation: DOM<= DOE and no duplicate itemId + batchNo + DOM + DOE
        List<OpeningBalanceDtRequest> dtRequests = openingBalanceEntryRequest.getStoreBalanceDtList();
        Set<String> uniqueCombinationSet = new HashSet<>();

        for (OpeningBalanceDtRequest dt : dtRequests) {


            if (dt.getManufactureDate() != null && dt.getExpiryDate() != null &&
                    dt.getManufactureDate().isAfter(dt.getExpiryDate())) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Manufacture Date cannot be after Expiry Date for itemId: " + dt.getItemId(),
                        HttpStatus.BAD_REQUEST.value());
            }


            String key = dt.getItemId() + "|" + dt.getBatchNo() + "|" + dt.getManufactureDate() + "|" + dt.getExpiryDate();
            if (!uniqueCombinationSet.add(key)) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Duplicate entry not allowed for same itemId, batchNo, DOM and DOE. Found duplicate for itemId: "
                                + dt.getItemId(),
                        HttpStatus.BAD_REQUEST.value());
            }
        }

        //  save DT records
        List<StoreBalanceDt> dtList = new ArrayList<>();
        for (OpeningBalanceDtRequest dtRequest : dtRequests) {

            StoreBalanceDt dt = new StoreBalanceDt();
            dt.setBalanceMId(savedHd);

            Optional<MasStoreItem> masStoreItem = itemRepo.findById(dtRequest.getItemId());
            if (masStoreItem.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("MasStoreItem not found", 404);
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
            dt.setTotalPurchaseCost(dtRequest.getTotalPurchaseCost());
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
            BigDecimal mrpPerUnit = dtRequest.getMrpPerUnit();
            BigDecimal totalMrp = mrpPerUnit.multiply(BigDecimal.valueOf(qty));
            dt.setTotalMrpValue(totalMrp);

            dt.setBrandId(brandRepo.findById(dtRequest.getBrandId()).orElse(null));
            Optional<MasManufacturer> masManufacturer=manufacturerRepo.findById(dtRequest.getManufacturerId());
            if(masManufacturer.isEmpty()){
                return ResponseUtils.createNotFoundResponse("MasStoreItem not found", 404);
            }
            dt.setManufacturerId(masManufacturer.get());

            dtList.add(dt);
        }

        dtRepo.saveAll(dtList);

        return ResponseUtils.createSuccessResponse(
                buildOpeningBalanceEntryResponse(savedHd, dtList),
                new TypeReference<>() {});
    }

    @Override
    @Transactional
    public ApiResponse<String> update(Long id, OpeningBalanceEntryRequest openingBalanceEntryRequest) {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "HospitalId user not found", HttpStatus.UNAUTHORIZED.value());
        }

        Optional<StoreBalanceHd> optionalHd = hdRepo.findById(id);
        if (optionalHd.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("Opening balance entry not found with id " + id, 404);
        }

         addDetails(openingBalanceEntryRequest.getStoreBalanceDtList(), id);

        StoreBalanceHd hd = optionalHd.get();

        // Update HD fields
        MasDepartment depObj = masDepartmentRepository.getById(openingBalanceEntryRequest.getDepartmentId());
        hd.setDepartmentId(depObj);
        hd.setEnteredBy(openingBalanceEntryRequest.getEnteredBy());
        hd.setLastUpdatedDt(LocalDateTime.now());
        if(openingBalanceEntryRequest.getStatus().equals("s")  || openingBalanceEntryRequest.getStatus() == null ) {
            hd.setStatus("s");
        }else if (openingBalanceEntryRequest.getStatus().equals("p")) {
            hd.setStatus("p");
        }
        StoreBalanceHd updatedHd = hdRepo.save(hd);
        return ResponseUtils.createSuccessResponse(" successfully", new TypeReference<>() {});

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

    @Override
    public List<OpeningBalanceEntryResponse> getListByStatus(String... statuses) {
        List<StoreBalanceHd> hdList = hdRepo.findByStatusIn(Arrays.asList(statuses));
        return hdList.stream()
                .map(hd -> {
                    List<StoreBalanceDt> dtList = dtRepo.findByBalanceMId(hd);
                    return buildOpeningBalanceEntryResponse(hd, dtList);
                })
                .collect(Collectors.toList());

    }

    @Override
    public ApiResponse<String> approved(Long id, OpeningBalanceEntryRequest2 request) {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }
        Optional<StoreBalanceHd> hd = hdRepo.findById(id);
        if(hd.isEmpty()){
            return ResponseUtils.createNotFoundResponse("Store Balance Hd not found", 404);

        }
        StoreBalanceHd hd1=hd.get();
        hd1.setStatus(request.getStatus());
        hd1.setApprovalDt(LocalDateTime.now());
        hd1.setRemarks(request.getRemark());
        hd1.setApprovedBy(String.valueOf(currentUser.getUserId()));
        StoreBalanceHd hd2=hdRepo.save(hd1);
        
        return ResponseUtils.createSuccessResponse("Approved successfully", new TypeReference<>() {});
    }

    public String addDetails(List<OpeningBalanceDtRequest> openingBalanceDtRequest, long hdId) {
        for (OpeningBalanceDtRequest dtRequest :openingBalanceDtRequest) {
            if (dtRequest.getBalanceId() == null) {

                StoreBalanceDt dt = new StoreBalanceDt();
                Optional<StoreBalanceHd> optionalHd = hdRepo.findById(hdId);
                if (optionalHd.isEmpty()) {
                    return "Opening balance entry not found with id ";
                }
                dt.setBalanceMId(optionalHd.get());
                Optional<MasStoreItem> masStoreItem = itemRepo.findById(dtRequest.getItemId());
                if (masStoreItem.isEmpty()) {
                    return "Item not found";
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
                dt.setTotalPurchaseCost(dtRequest.getTotalPurchaseCost());
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
                BigDecimal mrpPerUnit = dtRequest.getMrpPerUnit();
                BigDecimal totalMrp = mrpPerUnit.multiply(BigDecimal.valueOf(qty));
                dt.setTotalMrpValue(totalMrp);

                dt.setBrandId(brandRepo.findById(dtRequest.getBrandId()).orElse(null));
                Optional<MasManufacturer> masManufacturer = manufacturerRepo.findById(dtRequest.getManufacturerId());
                if (masManufacturer.isEmpty()) {
                    return "MasStoreItem not found";
                }
                dt.setManufacturerId(masManufacturer.get());
                dtRepo.save(dt);
            } else {
                Optional<StoreBalanceDt> storeBalanceDt=dtRepo.findById(dtRequest.getBalanceId());
                if(storeBalanceDt.isEmpty()){
                    return "StoreBalanceDt not found";
                }
                StoreBalanceDt dt =storeBalanceDt.get();
                Optional<MasStoreItem> masStoreItem = itemRepo.findById(dtRequest.getItemId());
                if (masStoreItem.isEmpty()) {
                    return "MasStoreItem not found";
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


                BigDecimal gst = dtRequest.getGstPercent();
                BigDecimal purchaseRatePerUnit = dtRequest.getPurchaseRatePerUnit();
                BigDecimal divisor = BigDecimal.ONE.add(gst.divide(BigDecimal.valueOf(100)));
                BigDecimal basePrice = purchaseRatePerUnit.divide(divisor, 2, RoundingMode.HALF_UP);
                BigDecimal gstAmount = purchaseRatePerUnit.subtract(basePrice);

                dt.setGstAmountPerUnit(gstAmount);
                dt.setBaseRatePerUnit(basePrice);

                BigDecimal totalMrp = dtRequest.getMrpPerUnit().multiply(BigDecimal.valueOf(dtRequest.getQty()));
                dt.setTotalMrpValue(totalMrp);

                dt.setBrandId(brandRepo.findById(dtRequest.getBrandId()).orElse(null));
                Optional<MasManufacturer> masManufacturer = manufacturerRepo.findById(dtRequest.getManufacturerId());
                if (masManufacturer.isEmpty()) {
                    return"Manufacturer not found ";
                }
                dt.setManufacturerId(masManufacturer.get());

                dtRepo.save(dt);
            }
        }
        return "successfully";
    }

    private OpeningBalanceEntryResponse buildOpeningBalanceEntryResponse(StoreBalanceHd hd, List<StoreBalanceDt> dtList) {
        OpeningBalanceEntryResponse response = new OpeningBalanceEntryResponse();
        response.setBalanceMId(hd.getBalanceMId());
        response.setBalanceNo(hd.getBalanceNo());
        response.setHospitalId(hd.getHospitalId().getId());
        response.setDepartmentId(hd.getDepartmentId().getId());
        response.setDepartmentName(hd.getDepartmentId().getDepartmentName());
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
            res.setItemName(dt.getItemId().getNomenclature());
            res.setItemCode(dt.getItemId().getPvmsNo());
            res.setItemUnit(dt.getItemId().getUnitAU().getUnitName());
            res.setItemGst(dt.getItemId().getHsnCode().getGstRate());
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
