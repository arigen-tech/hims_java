package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.OpeningBalanceEntryService;
import com.hims.utils.AuthUtil;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
    private StoreItemBatchStockRepository storeItemBatchStockRepository;

    @Autowired
    private MasHsnRepository masHsnRepository;

    @Autowired
    private StoreStockLedgerRepository storeStockLedgerRepository;

    @Autowired
    AuthUtil authUtil;

    @Value("${op_txn_type}")
    private String opTxnType;


    private static final Logger log = LoggerFactory.getLogger(DoctorRosterServicesImpl.class);


    private final RandomNumGenerator randomNumGenerator;

    public OpeningBalanceEntryServiceImp(RandomNumGenerator randomNumGenerator) {
        this.randomNumGenerator = randomNumGenerator;
    }

    public String createInvoice() {
        return randomNumGenerator.generateOrderNumber("BAL", true, true);
    }

    @Override
    @Transactional
    public ApiResponse<OpeningBalanceEntryResponse> add(OpeningBalanceEntryRequest openingBalanceEntryRequest) {

        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
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


        List<OpeningBalanceDtRequest> dtRequests = openingBalanceEntryRequest.getStoreBalanceDtList();


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
            dt.setBrandId(brandRepo.findById(dtRequest.getBrandId()).orElse(null));
            Optional<MasManufacturer> masManufacturer = manufacturerRepo.findById(dtRequest.getManufacturerId());
            if (masManufacturer.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("MasManufacturer not found", 404);
            }
            dt.setManufacturerId(masManufacturer.get());
            dtList.add(dt);
        }
        dtRepo.saveAll(dtList);
        return ResponseUtils.createSuccessResponse(
                buildOpeningBalanceEntryResponse(savedHd, dtList),
                new TypeReference<>() {
                });


    }

    @Override
    @Transactional
    public ApiResponse<String> update(Long id, OpeningBalanceEntryRequest openingBalanceEntryRequest) {
        User currentUser = authUtil.getCurrentUser();

        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "HospitalId user not found", HttpStatus.UNAUTHORIZED.value());
        }

        Optional<StoreBalanceHd> optionalHd = hdRepo.findById(id);

        if (optionalHd.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("Opening balance entry not found with id " + id, 404);
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
        if (openingBalanceEntryRequest.getStatus().equals("s") || openingBalanceEntryRequest.getStatus() == null) {
            hd.setStatus("s");
        } else if (openingBalanceEntryRequest.getStatus().equals("p")) {
            hd.setStatus("p");
        }
        StoreBalanceHd updatedHd = hdRepo.save(hd);
        return ResponseUtils.createSuccessResponse(" successfully", new TypeReference<>() {
        });

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
        return ResponseUtils.createSuccessResponse("StoreBalanceHd status updated to '" + status + "'", new TypeReference<>() {
        });
    }


    @Override
    public ApiResponse<OpeningBalanceEntryResponse> getDetailsById(Long id) {
        StoreBalanceHd hd = hdRepo.findById(id).orElseThrow(() -> new RuntimeException("Entry not found"));
        List<StoreBalanceDt> dtList = dtRepo.findByBalanceMId(hd);
        // return buildOpeningBalanceEntryResponse(hd, dtList);
        return ResponseUtils.createSuccessResponse(buildOpeningBalanceEntryResponse(hd, dtList), new TypeReference<>() {
        });
    }

    @Override
    public ApiResponse<OpeningBalanceEntryResponse> createAndUpdateStatus(OpeningBalanceEntryRequest request) {
        ApiResponse<OpeningBalanceEntryResponse> createResponse = this.add(request);
        updateByStatus(createResponse.getResponse().getBalanceMId(), "p");
        return ResponseUtils.createSuccessResponse(createResponse.getResponse(), new TypeReference<>() {
        });
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


    @Transactional
    @Override
    public ApiResponse<String> approved(Long id, OpeningBalanceEntryRequest2 request) {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }

        Optional<StoreBalanceHd> hdOpt = hdRepo.findById(id);
        if (hdOpt.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("Store Balance Hd not found", 404);
        }

        String fName = currentUser.getFirstName() + " " + currentUser.getMiddleName() + " " + currentUser.getLastName();

        StoreBalanceHd hd = hdOpt.get();
        hd.setStatus(request.getStatus());
        hd.setApprovalDt(LocalDateTime.now());
        hd.setRemarks(request.getRemark());
        hd.setApprovedBy(fName);
        StoreBalanceHd hdObj = hdRepo.save(hd);

        if ("a".equalsIgnoreCase(request.getStatus())) {

            List<StoreBalanceDt> dtList = dtRepo.findByBalanceMId(hd);
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
                    transferInLedger(qty, dt.getBalanceTId(), stock.getStockId(), hdObj.getRemarks());
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
                        transferInLedger(qty, dt.getBalanceTId(), stock.getStockId(), hdObj.getRemarks());
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

                        transferInLedger(dt.getQty(), dt.getBalanceTId(), stock.getStockId(), hdObj.getRemarks());
                    }

                    stock.setLastChgDate(LocalDateTime.now());
                    stock.setLastChgBy(currentUser.getUsername());

                    stockMap.put(key, stock);
                }

                dt.setIsApproved(true);
            }

            storeItemBatchStockRepository.saveAll(stockMap.values());
            dtRepo.saveAll(dtList);
        }

        return ResponseUtils.createSuccessResponse("Approved and stock moved to batch successfully", new TypeReference<>() {
        });
    }

    @Override
    public ApiResponse<List<?>> getAllStock(String type) {
        List<StoreItemBatchStock> allStocks = storeItemBatchStockRepository.findAll();
        if (type.equals("summary")) {
            Map<Long, List<StoreItemBatchStock>> groupedByItem = allStocks.stream()
                    .collect(Collectors.groupingBy(stock -> stock.getItemId().getItemId()));

            List<OpeningBalanceStockResponse> responseList = groupedByItem.entrySet().stream().map(entry -> {
                Long itemId = entry.getKey();
                List<StoreItemBatchStock> stocks = entry.getValue();


                StoreItemBatchStock first = stocks.get(0);


                Long totalQty = stocks.stream()
                        .mapToLong(s -> s.getOpeningBalanceQty() != null ? s.getOpeningBalanceQty() : 0)
                        .sum();

                OpeningBalanceStockResponse dto = new OpeningBalanceStockResponse();
                dto.setStockId(first.getStockId());
                dto.setItemId(itemId);
                dto.setItemName(first.getItemId().getNomenclature());
                dto.setItemCode(first.getItemId().getPvmsNo());
                dto.setOpeningQty(totalQty);
                dto.setUnitAu(first.getItemId().getUnitAU().getUnitName());
                dto.setSectionName(first.getItemId().getItemClassId().getMasStoreSection().getSectionName());
                dto.setSectionId(first.getItemId().getItemClassId().getMasStoreSection().getSectionId());
                dto.setClassId(first.getItemId().getItemClassId().getItemClassId());
                dto.setClassName(first.getItemId().getItemClassId().getItemClassName());

                return dto;
            }).collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
            });


        } else if (type.equals("details")) {
            List<OpeningBalanceStockResponse2> responseList = allStocks.stream().map(stock -> {
                OpeningBalanceStockResponse2 dto = new OpeningBalanceStockResponse2();
                dto.setStockId(stock.getStockId());
                dto.setItemId(stock.getItemId().getItemId());
                dto.setItemName(stock.getItemId().getNomenclature());
                dto.setItemCode(stock.getItemId().getPvmsNo());
                dto.setOpeningQty(stock.getOpeningBalanceQty());
                dto.setUnitAu(stock.getItemId().getUnitAU().getUnitName());
                dto.setBatchNo(stock.getBatchNo());
                dto.setDom(stock.getManufactureDate());
                dto.setDoe(stock.getExpiryDate());
                dto.setManufacturerName(stock.getManufacturerId().getManufacturerName());
                dto.setSectionName(stock.getItemId().getItemClassId().getMasStoreSection().getSectionName());
                dto.setSectionId(stock.getItemId().getItemClassId().getMasStoreSection().getSectionId());
                dto.setClassId(stock.getItemId().getItemClassId().getItemClassId());
                dto.setClassName(stock.getItemId().getItemClassId().getItemClassName());
                dto.setMedicineSource(stock.getBrandId().getBrandName());
                dto.setMrpPerUnit(stock.getMrpPerUnit());
                dto.setClosingQty(stock.getClosingStock());
                return dto;
            }).collect(Collectors.toList());
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
            });
        }
        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                },
                " invalid access Plz send summary or details ", HttpStatus.UNAUTHORIZED.value());


    }



    @Override
    public ApiResponse<List<OpeningBalanceStockResponse2>> getStockByDateRange(LocalDate fromDate, LocalDate toDate, Long itemId) {
        List<StoreItemBatchStock> stocks;

        if (itemId != null) {
            stocks = storeItemBatchStockRepository.findByItemIdAndExpiryDateRange(itemId, fromDate, toDate);
        } else {
            stocks = storeItemBatchStockRepository.findByExpiryDateRange(fromDate, toDate);
        }

        if (stocks.isEmpty()) {
            String message = (itemId != null)
                    ? "No data found for itemId " + itemId + " between expiry dates " + fromDate + " and " + toDate
                    : "No data found between expiry dates " + fromDate + " and " + toDate;

            return ResponseUtils.createNotFoundResponse(message, 404);
        }

        List<OpeningBalanceStockResponse2> responseList = stocks.stream()
                .map(this::convertedToResponse)
                .toList();

        return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
        });
    }




    @Override
    public ApiResponse<String> updateByMrp(List<UpdateMrpValue> marValue) {
        List<Long> notFoundIds = new ArrayList<>();
        for (UpdateMrpValue value : marValue) {
            Optional<StoreItemBatchStock> optionalStock = storeItemBatchStockRepository.findById(value.getStockId());
            if (optionalStock.isPresent()) {
                StoreItemBatchStock stock = optionalStock.get();
                stock.setMrpPerUnit(value.getMrpValue());
                Long openingQty=stock.getOpeningBalanceQty();
                BigDecimal newMrp=value.getMrpValue();
                stock.setTotalMrpValue(newMrp.multiply(BigDecimal.valueOf(openingQty)));
                storeItemBatchStockRepository.save(stock);
            } else {
                notFoundIds.add(value.getStockId());
            }
        }

        if (!notFoundIds.isEmpty()) {
            return ResponseUtils.createSuccessResponse("MRP updated for available stock IDs. Not found IDs: " + notFoundIds, new TypeReference<>() {
            });
        }
        return ResponseUtils.createSuccessResponse("MRP updated successfully for all provided stock IDs", new TypeReference<>() {
        });
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


    private void  deletedById(Long id){
        dtRepo.deleteById(id);
    }

    private String transferInLedger(long qty, long balanceDtId, long stockId, String remarks) {
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
        ledger.setQtyIn(qty);
        ledger.setStockId(stock);
        ledger.setTxnType(opTxnType);
        ledger.setRemarks(remarks);
        ledger.setTxnReferenceId(balanceDtId);
        storeStockLedgerRepository.save(ledger);
        return "success";
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
            res.setTotalMrpValue(dt.getTotalMrp());
            res.setBrandId(dt.getBrandId() != null ? dt.getBrandId().getBrandId() : null);
            res.setBrandName(dt.getBrandId().getBrandName());
            res.setManufacturerId(dt.getManufacturerId() != null ? dt.getManufacturerId().getManufacturerId() : null);
            res.setManufacturerName(dt.getManufacturerId().getManufacturerName());
            return res;
        }).collect(Collectors.toList());

        response.setOpeningBalanceDtResponseList(dtResponses);
        return response;
    }
    private OpeningBalanceStockResponse2 convertedToResponse(StoreItemBatchStock stock){
        OpeningBalanceStockResponse2 dto=new OpeningBalanceStockResponse2();
        dto.setStockId(stock.getStockId());
        dto.setItemId(stock.getItemId() != null?stock.getItemId().getItemId():null);
        dto.setItemName(stock.getItemId()!=null?stock.getItemId().getNomenclature():null);
        dto.setItemCode(stock.getItemId()!=null?stock.getItemId().getPvmsNo():null);
        dto.setUnitAu(stock.getItemId().getUnitAU() != null?stock.getItemId().getUnitAU().getUnitName():null);
        dto.setClassId(stock.getItemId().getItemClassId() != null?stock.getItemId().getItemClassId().getItemClassId():null);
        dto.setClassName(stock.getItemId().getItemClassId() != null?stock.getItemId().getItemClassId().getItemClassName():null);
        dto.setSectionId(stock.getItemId().getItemClassId().getMasStoreSection() != null?stock.getItemId().getItemClassId().getMasStoreSection().getSectionId():null);
        dto.setSectionName(stock.getItemId().getItemClassId().getMasStoreSection() != null?stock.getItemId().getItemClassId().getMasStoreSection().getSectionName():null);
        dto.setOpeningQty(stock.getOpeningBalanceQty());
        dto.setClosingQty(stock.getClosingStock());
        dto.setBatchNo(stock.getBatchNo());
        dto.setMrpPerUnit(stock.getMrpPerUnit());
        dto.setDom(stock.getManufactureDate());
        dto.setDoe(stock.getExpiryDate());
        dto.setManufacturerName(stock.getManufacturerId() != null?stock.getManufacturerId().getManufacturerName():null);
        dto.setMedicineSource(stock.getBrandId() != null?stock.getBrandId().getBrandName():null);
        return dto;
    }


}
