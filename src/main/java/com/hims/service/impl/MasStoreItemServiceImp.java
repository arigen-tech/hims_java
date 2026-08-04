package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.projection.*;
import com.hims.request.MasStoreItemRequest;
import com.hims.request.NonDrugStoreItemRequest;
import com.hims.response.*;
import com.hims.service.MasStoreItemService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import com.hims.utils.StockFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MasStoreItemServiceImp implements MasStoreItemService {
    @Autowired
    private MasStoreItemRepository masStoreItemRepository;

    @Autowired
    private MasItemClassRepository masItemClassRepository;
    @Autowired
    private MasStoreGroupRepository masStoreGroupRepository;
    @Autowired
    private MasItemTypeRepository masItemTypeRepository;
    @Autowired
    private MasStoreSectionRepository masStoreSectionRepository;
    @Autowired
    private MasStoreUnitRepository masStoreUnitRepository;
    @Autowired
    private MasHsnRepository masHsnRepository;
    @Autowired
    private MasItemCategoryRepository masItemCategoryRepository;

    @Autowired
    private  StoreItemBatchStockRepository storeItemBatchStockRepository;
    @Autowired
    private MasItemFacilityRepository masItemFacilityRepository;
    @Autowired
    private StoreItemFacilityMapRepository storeItemFacilityMapRepository;


    @Autowired
    UserRepo userRepo;
    @Autowired
    AuthUtil authUtil;

    @Autowired
    StockFound stockFound;

    @Autowired
    private UserDepartmentRepository userDepartmentRepository;

    @Autowired
    private MasDepartmentRepository masDepartmentRepository;

    @Value("${masstoreitem.section.id}")
    private Integer sectionId;

    @Value("${hos.define.storeDay}")
    private Integer hospDefinedstoreDays;

    @Value("${hos.define.storeId}")
    private Integer deptIdStore;

    @Value("${hos.define.dispensaryDay}")
    private Integer hospDefineddispDays;

    @Value("${hos.define.dispensaryId}")
    private Integer dispdeptId;

    @Value("${hos.define.wardPharmDay}")
    private Integer hospDefinedwardDays;

    @Value("${hos.define.wardPharmacyId}")
    private Integer warddeptId;
    @Value("${drugSectionCode}")
    private String drugSectionCode;

    @Value("${medicalConsumableItemTypeCode}")
    private String medicalConsumableItemTypeCode;

    @Value("${medicalNonConsumableItemTypeCode}")
    private String medicalNonConsumableItemTypeCode;

    @Value("${mas.item.group}")
    private String masItemGroup;


    private static final Logger log = LoggerFactory.getLogger(DoctorRosterServicesImpl.class);

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }

    private String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    @Transactional
    public ApiResponse<MasStoreItemResponse> addMasStoreItem(MasStoreItemRequest masStoreItemRequest) {
        try{
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "HospitalId user not found", HttpStatus.UNAUTHORIZED.value());
        }
        Optional<MasStoreItem> existingItem = masStoreItemRepository
                .findFirstByPvmsNoOrNomenclature(masStoreItemRequest.getPvmsNo(), masStoreItemRequest.getNomenclature());

        if (existingItem.isPresent()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Pvms No or Nomenclature already exists", HttpStatus.CONFLICT.value());
        }
        long deptId = authUtil.getCurrentDepartmentId();
        MasDepartment depObj = masDepartmentRepository.getById(deptId);
        MasStoreItem masStoreItem = new MasStoreItem();
        masStoreItem.setPvmsNo(masStoreItemRequest.getPvmsNo());
        masStoreItem.setNomenclature(masStoreItemRequest.getNomenclature());
        masStoreItem.setStatus("y");
        masStoreItem.setAdispQty(masStoreItemRequest.getAdispQty());
//       masStoreItem.setHospitalId(currentUser.getHospital().getId());
//        masStoreItem.setDepartmentId(depObj.getId());
        masStoreItem.setLastChgBy(currentUser.getUserId());
        masStoreItem.setLastChgDate(LocalDate.now());
        masStoreItem.setLastChgTime(getCurrentTimeFormatted());
//        masStoreItem.setReOrderLevelStore(masStoreItemRequest.getReOrderLevelStore());
//        masStoreItem.setReOrderLevelDispensary(masStoreItemRequest.getReOrderLevelDispensary());
        masStoreItem.setIsGeneric(masStoreItemRequest.getIsGeneric());
        masStoreItem.setDangerousDrug(masStoreItemRequest.getDangerousDrug());
        masStoreItem.setDrugSchedule(masStoreItemRequest.getDrugSchedule());
        masStoreItem.setHighValueDrug(normalizeYN(masStoreItemRequest.getHighValueDrug()));
        masStoreItem.setAvailableInOpd(normalizeYN(masStoreItemRequest.getAvailableInOpd()));
        masStoreItem.setAvailableInIpd(normalizeYN(masStoreItemRequest.getAvailableInIpd()));
        masStoreItem.setAvailableInEmergency(normalizeYN(masStoreItemRequest.getAvailableInEmergency()));
        masStoreItem.setAvailableInOt(normalizeYN(masStoreItemRequest.getAvailableInOt()));


        Optional<MasStoreUnit> masStoreUnit = masStoreUnitRepository.findById(masStoreItemRequest.getDispUnit());
        if (masStoreUnit.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("MasStoreUnit not found", 404);
        }
        Optional<MasStoreUnit> masStoreUnit1 = masStoreUnitRepository.findById(masStoreItemRequest.getUnitAU());
        if (masStoreUnit1.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("MasStoreUnit not found", 404);
        }
        Optional<MasStoreSection> masStoreSection = masStoreSectionRepository.findById(masStoreItemRequest.getSectionId());
        if (masStoreSection.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("MasStoreSection not found", 404);
        }
        Optional<MasItemType> masItemType = masItemTypeRepository.findById(masStoreItemRequest.getItemTypeId());
        if (masItemType.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("MasItemType not found", 404);
        }
        Optional<MasStoreGroup> masStoreGroup = masStoreGroupRepository.findById(masStoreItemRequest.getGroupId());
        if (masStoreGroup.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("MasStoreGroup not found", 404);
        }
        Optional<MasItemClass> masItemClass = masItemClassRepository.findById(masStoreItemRequest.getItemClassId());
        if (masItemClass.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("MasItemClass not found", 404);
        }
        Optional<MasHSN> masHSN = masHsnRepository.findById(masStoreItemRequest.getHsnCode());
        if (masHSN.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("MasHSN not found", 404);
        }
        Optional<MasItemCategory> masItemCategory = masItemCategoryRepository.findById(masStoreItemRequest.getMasItemCategoryId());
        if (masItemCategory.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("MasItemCategory not found", 404);
        }

        masStoreItem.setDispUnit(masStoreUnit.get());
        masStoreItem.setUnitAU(masStoreUnit1.get());
        masStoreItem.setItemClassId(masItemClass.get());
        masStoreItem.setGroupId(masStoreGroup.get());
        masStoreItem.setItemTypeId(masItemType.get());
        masStoreItem.setSectionId(masStoreSection.get());
        masStoreItem.setHsnCode(masHSN.get());
        masStoreItem.setMasItemCategory(masItemCategory.get());
        masStoreItem.setStoreROL(masStoreItemRequest.getReOrderLevelStore());
        masStoreItem.setDispROL(masStoreItemRequest.getReOrderLevelDispensary());
        masStoreItem.setWardROL(masStoreItemRequest.getReOrderLevelWard());

        MasHospital hospital = currentUser.getHospital();

        if (AppConstants.STATUS_Y.toLowerCase().equalsIgnoreCase(hospital.getRoIsManual())) {
            masStoreItem.setStoreRoLManual(AppConstants.STATUS_Y.toLowerCase());
            masStoreItem.setDispRoLManual(AppConstants.STATUS_Y.toLowerCase());
            masStoreItem.setWardRoLManual(AppConstants.STATUS_Y.toLowerCase());

        } else if(AppConstants.STATUS_Y.toLowerCase().equalsIgnoreCase(hospital.getRolIsAuto())){
            masStoreItem.setStoreRoLAuto(AppConstants.STATUS_Y.toLowerCase());
            masStoreItem.setDispRoLAuto(AppConstants.STATUS_Y.toLowerCase());
            masStoreItem.setWardRoLAuto(AppConstants.STATUS_Y.toLowerCase());
        }
        MasStoreItem savedItem = masStoreItemRepository.save(masStoreItem);
        if (masStoreItemRequest.getFacility() != null && !masStoreItemRequest.getFacility().isEmpty()) {

            for (Long facilityId : masStoreItemRequest.getFacility()) {

                StoreItemFacilityMap storeItemFacilityMap = new StoreItemFacilityMap();

                storeItemFacilityMap.setItem(savedItem);
                storeItemFacilityMap.setFacility(masItemFacilityRepository.findById(facilityId).orElseThrow());
                storeItemFacilityMap.setStatus(AppConstants.STATUS_N.toLowerCase());
                storeItemFacilityMap.setCreatedBy(currentUser.getFullName());
                storeItemFacilityMap.setLastUpdatedBy(currentUser.getFullName());
                storeItemFacilityMap.setLastUpdateDate(LocalDateTime.now());
                storeItemFacilityMapRepository.save(storeItemFacilityMap);
            }
        }
        return ResponseUtils.createSuccessResponse(convertToResponse(savedItem), new TypeReference<>() {});
    } catch (Exception e) {
            throw new RuntimeException("Failed to save MasStoreItem : " + e.getMessage());
    }
    }

    @Override
    public ApiResponse<MasStoreItemResponse> findById(Long id) {
        try {
            Optional<MasStoreItem> masStoreItem = masStoreItemRepository.findById(id);
            if (masStoreItem.isPresent()) {
                MasStoreItem masStoreItem1 = masStoreItem.get();

                return ResponseUtils.createSuccessResponse(convertToResponse(masStoreItem1), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "MasStoreItem not found", 404);
            }
        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<MasStoreItemResponse>> getAllMasStoreItem(int flag) {


        log.info("getAllMasStoreItem started with flag: {}", flag);

        List<MasStoreItem> masStoreItems;

        if (flag == 1) {
            masStoreItems = masStoreItemRepository
                    .findByStatusIgnoreCaseOrderByNomenclatureAsc("y");
        } else if (flag == 0) {
            masStoreItems = masStoreItemRepository
                    .findAllOrderByStatusDesc(List.of("y", "n"));
        } else {
            log.warn("Invalid flag received: {}", flag);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Invalid flag value. Use 0 or 1.", 400);
        }

        log.info("Items fetched count: {}", masStoreItems.size());

        // Fetch all stocks in one query
        long stockStart = System.currentTimeMillis();
        List<StoreItemBatchStock> allStocks =
                storeItemBatchStockRepository.findByItemIds(masStoreItems);
        log.info("Stock fetch time: {} ms",
                System.currentTimeMillis() - stockStart);

        //  Group stocks by itemId
        Map<Long, List<StoreItemBatchStock>> stockMap = allStocks.stream()
                .collect(Collectors.groupingBy(s -> s.getItemId().getItemId()));


        List<MasStoreItemResponse> responses = masStoreItems.stream()
                .map(item -> convertToResponsewithAllStock(item, stockMap))
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }
//
////
////    @Override
////    public ApiResponse<List<MasStoreItemResponse>> getAllMasStoreItemWithOutStock(int flag) {
////
////        List<MasStoreItem> masStoreItems;
////        if (flag == 1) {
////            masStoreItems =
////                    masStoreItemRepository
////                            .findBySectionIdSectionIdAndStatusIgnoreCaseOrderByLastChgDateDescLastChgTimeDesc(
////                                    sectionId, "y"
////                            );
////
////        } else if (flag == 0) {
////            masStoreItems =
////                    masStoreItemRepository
////                            .findBySectionIdSectionIdAndStatusInIgnoreCaseOrderByLastChgDateDescLastChgTimeDesc(
////                                    sectionId, List.of("y", "n")
////                            );
////
////        } else {
////            return ResponseUtils.createFailureResponse(
////                    null,
////                    new TypeReference<>() {},
////                    "Invalid flag value. Use 0 or 1.",
////                    400
////            );
////        }
//
////        //Fetch all stocks in one query
////        List<StoreItemBatchStock> allStocks = storeItemBatchStockRepository.findByItemIds(masStoreItems);
////
////        // Group stocks by itemId
////        Map<Long, List<StoreItemBatchStock>> stockMap = allStocks.stream()
////                .collect(Collectors.groupingBy(s -> s.getItemId().getItemId()));
//
//        List<MasStoreItemResponse> responses = masStoreItems.stream()
//                .map(this::convertToResponse)
//                .collect(Collectors.toList());
//
//        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
//        });
//    }
@Override
public ApiResponse<List<MasStoreItemResponse>> getAllMasStoreItemWithOutStock(int flag) {

    try {
        List<MasStoreItemsProjection> masStoreItems;
        if (flag == 1) {
            masStoreItems = masStoreItemRepository.findActiveItemsBySectionId("y");

        } else if (flag == 0) {
            masStoreItems = masStoreItemRepository.findAllItemsBySectionIdAndStatusIn(sectionId, List.of("y", "n"));

        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<Long> itemIds = masStoreItems.stream().map(MasStoreItemsProjection::getItemId).filter(Objects::nonNull).distinct().toList();

        Map<Long, List<MasStoreItemResponse.MasFacilityCodeResponse>> facilityMap = Collections.emptyMap();

        if (!itemIds.isEmpty()) {
            facilityMap = storeItemFacilityMapRepository.findFacilityByItemIds(itemIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            MasStoreItemFacilityProjection::getItemId,
                            Collectors.mapping(f -> {
                                MasStoreItemResponse.MasFacilityCodeResponse res = new MasStoreItemResponse.MasFacilityCodeResponse();
                                res.setFacilityId(f.getFacilityId());
                                res.setFacilityCode(f.getFacilityCode());
                                return res;
                            }, Collectors.toList())
                    ));
        }

        Map<Long, List<MasStoreItemResponse.MasFacilityCodeResponse>> finalFacilityMap = facilityMap;

        List<MasStoreItemResponse> responses = masStoreItems.stream()
                .map(item -> convertProjectionToResponse(item, finalFacilityMap))
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});

    } catch (Exception e) {
        log.error("Error while fetching Mas Store Items without stock. Flag: {}", flag, e);
        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
    }
}

@Override
public ApiResponse<Page<MasStoreItemResponse>> getAllMasStoreItemWithOutStockPaginated(
        int flag,
        int page,
        int size,
        String nomenclature,
        Integer itemClassId,
        Integer masItemCategoryid) {

    try {
        Pageable pageable = PageRequest.of(page, size);
        Integer querySectionId = (flag == 1) ? null : sectionId;

        Page<MasStoreItemsProjection> projectionPage = masStoreItemRepository.findItemsWithOutStockPaginated(
                flag, querySectionId, nomenclature, itemClassId, masItemCategoryid, pageable);

        List<Long> itemIds = projectionPage.getContent().stream()
                .map(MasStoreItemsProjection::getItemId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, List<MasStoreItemResponse.MasFacilityCodeResponse>> facilityMap = Collections.emptyMap();

        if (!itemIds.isEmpty()) {
            facilityMap = storeItemFacilityMapRepository.findFacilityByItemIds(itemIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            MasStoreItemFacilityProjection::getItemId,
                            Collectors.mapping(f -> {
                                MasStoreItemResponse.MasFacilityCodeResponse res = new MasStoreItemResponse.MasFacilityCodeResponse();
                                res.setFacilityId(f.getFacilityId());
                                res.setFacilityCode(f.getFacilityCode());
                                return res;
                            }, Collectors.toList())
                    ));
        }

        Map<Long, List<MasStoreItemResponse.MasFacilityCodeResponse>> finalFacilityMap = facilityMap;

        Page<MasStoreItemResponse> responsePage = projectionPage.map(item -> convertProjectionToResponse(item, finalFacilityMap));

        return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {});

    } catch (Exception e) {
        log.error("Error while fetching Mas Store Items without stock paginated. Flag: {}", flag, e);
        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, 500);
    }
}
    @Override
    public ApiResponse<MasStoreItemResponse> update(Long id, MasStoreItemRequest request) {
        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            Optional<MasStoreItem> masStoreItem = masStoreItemRepository.findById(id);
            if (masStoreItem.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "MasStoreItem not found", HttpStatus.NOT_FOUND.value());
            }

            MasStoreItem item = masStoreItem.get();
            if (!item.getPvmsNo().equals(request.getPvmsNo())) {
                Optional<MasStoreItem> duplicatePvms = masStoreItemRepository
                        .findByPvmsNoAndItemIdNot(request.getPvmsNo(), id);

                if (duplicatePvms.isPresent()) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                            "Pvms No already exists in another item", HttpStatus.CONFLICT.value());
                }

                item.setPvmsNo(request.getPvmsNo());
            }


            if (!item.getNomenclature().equals(request.getNomenclature())) {
                Optional<MasStoreItem> duplicateNomenclature = masStoreItemRepository
                        .findByNomenclatureAndItemIdNot(request.getNomenclature(), id);

                if (duplicateNomenclature.isPresent()) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                            "Nomenclature already exists in another item", HttpStatus.CONFLICT.value());
                }

                item.setNomenclature(request.getNomenclature());
            }

            long deptId = authUtil.getCurrentDepartmentId();
            MasDepartment depObj = masDepartmentRepository.getById(deptId);
            item.setAdispQty(request.getAdispQty());
//            item.setReOrderLevelStore(request.getReOrderLevelStore());
//            item.setReOrderLevelDispensary(request.getReOrderLevelDispensary());
//            item.setHospitalId(currentUser.getHospital().getId());
//            item.setDepartmentId(depObj.getId());
            item.setLastChgBy(currentUser.getUserId());
            item.setLastChgDate(LocalDate.now());
            item.setLastChgTime(getCurrentTimeFormatted());
            item.setStoreROL(request.getReOrderLevelStore());
            item.setDispROL(request.getReOrderLevelDispensary());
            item.setIsGeneric(request.getIsGeneric());
            item.setDangerousDrug(request.getDangerousDrug());
            item.setDrugSchedule(request.getDrugSchedule());
            item.setHighValueDrug(normalizeYN(request.getHighValueDrug()));
            item.setAvailableInOpd(normalizeYN(request.getAvailableInOpd()));
            item.setAvailableInIpd(normalizeYN(request.getAvailableInIpd()));
            item.setAvailableInEmergency(normalizeYN(request.getAvailableInEmergency()));
            item.setAvailableInOt(normalizeYN(request.getAvailableInOt()));

            if (request.getDispUnit() != null) {
                item.setDispUnit(masStoreUnitRepository.findById(request.getDispUnit())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DispUnit not found")));
            }

            if (request.getUnitAU() != null) {
                item.setUnitAU(masStoreUnitRepository.findById(request.getUnitAU())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "UnitAU not found")));
            }

            if (request.getSectionId() != null) {
                item.setSectionId(masStoreSectionRepository.findById(request.getSectionId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found")));
            }

            if (request.getItemTypeId() != null) {
                item.setItemTypeId(masItemTypeRepository.findById(request.getItemTypeId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemType not found")));
            }

            if (request.getGroupId() != null) {
                item.setGroupId(masStoreGroupRepository.findById(request.getGroupId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found")));
            }

            if (request.getItemClassId() != null) {
                item.setItemClassId(masItemClassRepository.findById(request.getItemClassId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemClass not found")));
            }

            if (request.getHsnCode() != null) {
                item.setHsnCode(masHsnRepository.findById(request.getHsnCode())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "HSN not found")));
            }

            if (request.getMasItemCategoryId() != null) {
                item.setMasItemCategory(masItemCategoryRepository.findById(request.getMasItemCategoryId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemCategory not found")));
            }
            MasHospital hospital = currentUser.getHospital();

            if (AppConstants.STATUS_Y.toLowerCase().equalsIgnoreCase(hospital.getRoIsManual())) {
                item .setStoreRoLManual(AppConstants.STATUS_Y.toLowerCase());
                item .setDispRoLManual(AppConstants.STATUS_Y.toLowerCase());
                item .setWardRoLManual(AppConstants.STATUS_Y.toLowerCase());

            } else if(AppConstants.STATUS_Y.toLowerCase().equalsIgnoreCase(hospital.getRolIsAuto())){
                item .setStoreRoLAuto(AppConstants.STATUS_Y.toLowerCase());
                item .setDispRoLAuto(AppConstants.STATUS_Y.toLowerCase());
                item .setWardRoLAuto(AppConstants.STATUS_Y.toLowerCase());
            }

            MasStoreItem updatedItem = masStoreItemRepository.save(item);
            // ================= UPDATE FACILITY MAP =================
            if (request.getFacility() != null) {

                // Request facility ids
                Set<Long> newFacilityIds = new HashSet<>(request.getFacility());

                // Existing mappings
                List<StoreItemFacilityMap> existingMappings = storeItemFacilityMapRepository.findByItemItemId(updatedItem.getItemId());

                // Existing facility ids
                Set<Long> existingFacilityIds = existingMappings.stream().map(map -> map.getFacility().getFacilityId())
                        .collect(Collectors.toSet());

                // ================= DELETE OLD =================

                List<StoreItemFacilityMap> mappingsToDelete = existingMappings.stream().filter(map -> !newFacilityIds.contains(map.getFacility().getFacilityId())).toList();

                if (!mappingsToDelete.isEmpty()) {
                    storeItemFacilityMapRepository.deleteAll(mappingsToDelete);
                }

                // ================= ADD NEW =================

                for (Long facilityId : newFacilityIds) {

                    if (!existingFacilityIds.contains(facilityId)) {

                        MasItemFacility facility = masItemFacilityRepository.findById(facilityId).orElseThrow(() -> new RuntimeException("Facility not found : " + facilityId));

                        StoreItemFacilityMap map = new StoreItemFacilityMap();

                        map.setItem(updatedItem);
                        map.setFacility(facility);
                        map.setStatus(AppConstants.STATUS_N.toLowerCase());
                        map.setCreatedBy(currentUser.getFullName());
                        map.setLastUpdatedBy(currentUser.getFullName());
                        map.setLastUpdateDate(LocalDateTime.now());

                        storeItemFacilityMapRepository.save(map);
                    }
                }
            }


            return ResponseUtils.createSuccessResponse(convertToResponse(updatedItem), new TypeReference<>() {});
        } catch (Exception ex) {
            log.error("An unexpected error occurred",ex);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<MasStoreItemResponse> changeMasStoreItemStatus(Long id, String status) {
        try {
            Optional<MasStoreItem> masStoreItem = masStoreItemRepository.findById(id);
            if (masStoreItem.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "MasStoreItem not found with ID: " + id, HttpStatus.NOT_FOUND.value());
            }
            MasStoreItem entity = masStoreItem.get();
            if (status.equalsIgnoreCase("y") || status.equalsIgnoreCase("n")) {
                entity.setStatus(status);
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid status. Status should be 'y' or 'n'", 400);
            }
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            entity.setLastChgBy(currentUser.getUserId());
            entity.setLastChgDate(LocalDate.now());
            entity.setLastChgTime(getCurrentTimeFormatted());

            return ResponseUtils.createSuccessResponse(convertToResponse(masStoreItemRepository.save(entity)), new TypeReference<>() {
            });
        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());

        }
    }

    @Override
    public ApiResponse<MasStoreItemResponse> findByCode(String code) {
        try {
            Optional<MasStoreItem> masStoreItem = masStoreItemRepository.findByPvmsNo(code);
            if (masStoreItem.isPresent()) {
                MasStoreItem masStoreItem1 = masStoreItem.get();

                return ResponseUtils.createSuccessResponse(convertToResponse(masStoreItem1), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "MasStoreItemCode not found", 404);
            }
        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

     @Override
    public ApiResponse<List<MasStoreItemResponseDto>> getAllMasStore(int flag) {

         List<MasStoreItem> masStoreItems;
         if (flag == 1) {
             masStoreItems = masStoreItemRepository.findByStatus("y");
         }
          else {
             return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
             }, "Invalid flag value. Use 1.", 400);
         }

         List<MasStoreItemResponseDto> responses = masStoreItems.stream()
                 .map(this::convertToResponse2)
                 .collect(Collectors.toList());

          return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
         });


     }

    @Override
    public ApiResponse<List<MasStoreItemResponseWithStock>> getAllMasStoreItemBySectionOnly(int flag) {

        long apiStart = System.currentTimeMillis();
        System.out.println("⏳ API START: getAllMasStoreItemBySectionOnly");

        List<MasStoreItem> masStoreItems;

        if (flag == 1) {
            masStoreItems = masStoreItemRepository
                    .findByStatusIgnoreCaseAndSectionId_SectionId("y", sectionId);
        } else if (flag == 0) {
            masStoreItems = masStoreItemRepository
                    .findByStatusInIgnoreCaseAndSectionId_SectionId(List.of("y", "n"), sectionId);
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Invalid flag value. Use 0 or 1.", 400);
        }

        long fetchEnd = System.currentTimeMillis();
        System.out.println("⏱ Time to fetch MasStoreItems: " + (fetchEnd - apiStart) + " ms");

        // ✅ Fetch all stocks in one query
        List<StoreItemBatchStock> allStocks = storeItemBatchStockRepository.findByItemIds(masStoreItems);

        // Group stocks by itemId
        Map<Long, List<StoreItemBatchStock>> stockMap = allStocks.stream()
                .collect(Collectors.groupingBy(s -> s.getItemId().getItemId()));

        long convertStart = System.currentTimeMillis();

        List<MasStoreItemResponseWithStock> responses = masStoreItems.stream()
                .map(item -> convertToResponseFast(item, stockMap))
                .collect(Collectors.toList());

        long convertEnd = System.currentTimeMillis();
        System.out.println("⏱ Time to convert items: " + (convertEnd - convertStart) + " ms");

        long apiEnd = System.currentTimeMillis();
        System.out.println("✅ TOTAL API TIME: " + (apiEnd - apiStart) + " ms");

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

//
//    @Override
//    public ApiResponse<Page<MasStoreItemResponseWithStock>> getMasStoreItemDynamic(
//            int flag,
//            String search,
//            int page,
//            int size) {
//        try {
//        long apiStart = System.currentTimeMillis();
//        Pageable pageable = PageRequest.of(page, size, Sort.by("nomenclature").ascending());
//        Page<MasStoreItem> masStoreItems;
//
//        // Dynamic filtering
//        if ((search != null && !search.isBlank()) || sectionId != null) {
//            masStoreItems = masStoreItemRepository.dynamicSearch(flag, Long.valueOf(sectionId), search.toLowerCase(), pageable);
//        } else {
//            if (flag == 1) {
//                masStoreItems = masStoreItemRepository.findByStatusIgnoreCase(AppConstants.STATUS_Y.toLowerCase(), pageable);
//            } else if (flag == 0) {
//                masStoreItems = masStoreItemRepository.findByStatusInIgnoreCase(List.of(AppConstants.STATUS_Y.toLowerCase(),AppConstants.STATUS_N.toLowerCase()), pageable);
//            } else {
//                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
//                        "Invalid flag value. Use 0 or 1.", HttpStatus.BAD_REQUEST.value());
//            }
//        }
//
//        // No data
//        if (masStoreItems.isEmpty()) {
//            return ResponseUtils.createSuccessResponse(Page.empty(pageable), new TypeReference<>() {});
//        }
//
//        // Preload stocks efficiently
//        List<Long> itemIds = masStoreItems.getContent().stream()
//                .map(MasStoreItem::getItemId)
//                .toList();
//
//        List<StoreItemBatchStock> allStocks = storeItemBatchStockRepository.findByItemId(itemIds);
//
//        Map<Long, List<StoreItemBatchStock>> stockMap = allStocks.stream()
//                .collect(Collectors.groupingBy(s -> s.getItemId().getItemId()));
//
//        // Map to response
//        Page<MasStoreItemResponseWithStock> responsePage = masStoreItems.map(item ->
//                convertToResponsefast(item, stockMap)
//        );
//        return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {});
//        } catch (Exception ex) {
//            log.error("Something went wrong while fetching store items: ", ex);
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value()
//            );
//        }
//    }
@Override
public ApiResponse<Page<MasStoreItemResponseWithStock>> getMasStoreItemDynamic(int flag, String search, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("nomenclature").ascending());
            Page<MasStoreItem> masStoreItems;

        // Dynamic filtering
        if ((search != null && !search.isBlank()) || sectionId != null) {
            masStoreItems = masStoreItemRepository.dynamicSearch(flag, Long.valueOf(sectionId), search.toLowerCase(), pageable);
        } else {
            if (flag == 1) {
                masStoreItems = masStoreItemRepository.findByStatusIgnoreCase(AppConstants.STATUS_Y.toLowerCase(), pageable);
            } else if (flag == 0) {
                masStoreItems = masStoreItemRepository.findByStatusInIgnoreCase(List.of(AppConstants.STATUS_Y.toLowerCase(), AppConstants.STATUS_N.toLowerCase()), pageable);
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", HttpStatus.BAD_REQUEST.value()
                );
            }
        }

        // No data case
        if (masStoreItems.isEmpty()) {
            return ResponseUtils.createSuccessResponse(Page.empty(pageable), new TypeReference<>() {});
        }

        // Fetch all stocks in one query
        List<Long> itemIds = masStoreItems.getContent().stream().map(MasStoreItem::getItemId).toList();

        List<StoreItemBatchStock> allStocks = storeItemBatchStockRepository.findByItemId(itemIds);

        // Group stock by itemId
        Map<Long, List<StoreItemBatchStock>> stockMap = allStocks.stream().collect(Collectors.groupingBy(s -> s.getItemId().getItemId()));
        // Map to response
        Page<MasStoreItemResponseWithStock> responsePage = masStoreItems.map(item -> convertToResponseFast(item, stockMap));
        return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {}
        );

        }
        catch (Exception ex) {
        log.error("Something went wrong while fetching store items: ", ex);
        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}

    @Override
    public ApiResponse<List<ItemProjection>> getAllDrugs(Integer sectionId) {
        try {

            List<ItemProjection> list = masStoreItemRepository.findDrugsBySection(sectionId);
            return ResponseUtils.createSuccessResponse(list, new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Internal server error", 500);
        }
    }

    @Override
    public ApiResponse<Page<ItemStockLedgerWithBatchResponse>> getStoreItems(String keyword, int page, int size) {
        try {
            log.info("getStoreItems with item contains name {} ,method started...",keyword);

            Pageable pageable=PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.ASC,"nomenclature")
            );
            Page<ItemStockLedgerWithBatchResponse> responses ;

                responses=masStoreItemRepository.searchItems( keyword, pageable);
                    log.info("getStoreItems with item contains name {} ,method ended...",keyword);
            return  ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
        }catch (Exception e) {
            log.error("getStoreItems method error :: ",e);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<NonDrugStoreItemResponse> addNonDrugStoreItem(NonDrugStoreItemRequest nonDrugStoreItemRequest) {
        try{
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "HospitalId user not found", HttpStatus.UNAUTHORIZED.value());
            }
            Optional<MasStoreItem> existingItem = masStoreItemRepository
                    .findFirstByPvmsNoOrNomenclature(nonDrugStoreItemRequest.getPvmsNo(), nonDrugStoreItemRequest.getNomenclature());

            if (existingItem.isPresent()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Pvms No or Nomenclature already exists", HttpStatus.CONFLICT.value());
            }
            long deptId = authUtil.getCurrentDepartmentId();
            MasDepartment depObj = masDepartmentRepository.getById(deptId);
            MasStoreItem masStoreItem = new MasStoreItem();
            masStoreItem.setPvmsNo(nonDrugStoreItemRequest.getPvmsNo());
            masStoreItem.setNomenclature(nonDrugStoreItemRequest.getNomenclature());
            masStoreItem.setStatus(AppConstants.STATUS_Y.toLowerCase());
            masStoreItem.setLastChgBy(currentUser.getUserId());
            masStoreItem.setLastChgDate(LocalDate.now());
            masStoreItem.setLastChgTime(getCurrentTimeFormatted());
            Optional<MasHSN> masHSN = masHsnRepository.findById(nonDrugStoreItemRequest.getHsn());
            if (masHSN.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("MasHSN not found", 404);
            }

            Optional<MasStoreUnit> masStoreUnit1 = masStoreUnitRepository.findById(nonDrugStoreItemRequest.getUnitAU());
            if (masStoreUnit1.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("MasStoreUnit not found", 404);
            }
            Optional<MasStoreSection> masStoreSection = masStoreSectionRepository.findById(nonDrugStoreItemRequest.getSectionId());
            if (masStoreSection.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("MasStoreSection not found", 404);
            }
            Optional<MasItemType> masItemType = masItemTypeRepository.findById(nonDrugStoreItemRequest.getItemTypeId());
            if (masItemType.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("MasItemType not found", 404);
            }
            Optional<MasStoreGroup> masStoreGroup = masStoreGroupRepository.findById(nonDrugStoreItemRequest.getGroupId());
            if (masStoreGroup.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("MasStoreGroup not found", 404);
            }
            Optional<MasItemClass> masItemClass = masItemClassRepository.findById(nonDrugStoreItemRequest.getItemClassId());
            if (masItemClass.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("MasItemClass not found", 404);
            }

            Optional<MasItemCategory> masItemCategory = masItemCategoryRepository.findById(nonDrugStoreItemRequest.getMasItemCategoryId());
            if (masItemCategory.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("MasItemCategory not found", 404);
            }
            masStoreItem.setUnitAU(masStoreUnit1.get());
            masStoreItem.setItemClassId(masItemClass.get());
            masStoreItem.setGroupId(masStoreGroup.get());
            masStoreItem.setItemTypeId(masItemType.get());
            masStoreItem.setSectionId(masStoreSection.get());
            masStoreItem.setMasItemCategory(masItemCategory.get());

            MasHospital hospital = currentUser.getHospital();

            if (AppConstants.STATUS_Y.toLowerCase().equalsIgnoreCase(hospital.getRoIsManual())) {
                masStoreItem.setStoreRoLManual(AppConstants.STATUS_Y.toLowerCase());
                masStoreItem.setDispRoLManual(AppConstants.STATUS_Y.toLowerCase());
                masStoreItem.setWardRoLManual(AppConstants.STATUS_Y.toLowerCase());

            } else if(AppConstants.STATUS_Y.toLowerCase().equalsIgnoreCase(hospital.getRolIsAuto())){
                masStoreItem.setStoreRoLAuto(AppConstants.STATUS_Y.toLowerCase());
                masStoreItem.setDispRoLAuto(AppConstants.STATUS_Y.toLowerCase());
                masStoreItem.setWardRoLAuto(AppConstants.STATUS_Y.toLowerCase());
            }
            MasStoreItem savedItem=masStoreItemRepository.save(masStoreItem);
            return ResponseUtils.createSuccessResponse(convertToResponseNonDrug(savedItem), new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to save MasStoreItem : " + e.getMessage());
        }

    }

    @Override
    public ApiResponse<NonDrugStoreItemResponse> updateNonDrugItem(Long id, NonDrugStoreItemRequest request) {
        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            Optional<MasStoreItem> masStoreItem = masStoreItemRepository.findById(id);
            if (masStoreItem.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "MasStoreItem not found", HttpStatus.NOT_FOUND.value());
            }

            MasStoreItem item = masStoreItem.get();
            if (!item.getPvmsNo().equals(request.getPvmsNo())) {
                Optional<MasStoreItem> duplicatePvms = masStoreItemRepository
                        .findByPvmsNoAndItemIdNot(request.getPvmsNo(), id);

                if (duplicatePvms.isPresent()) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                            "Pvms No already exists in another item", HttpStatus.CONFLICT.value());
                }

                item.setPvmsNo(request.getPvmsNo());
            }


            if (!item.getNomenclature().equals(request.getNomenclature())) {
                Optional<MasStoreItem> duplicateNomenclature = masStoreItemRepository
                        .findByNomenclatureAndItemIdNot(request.getNomenclature(), id);

                if (duplicateNomenclature.isPresent()) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                            "Nomenclature already exists in another item", HttpStatus.CONFLICT.value());
                }

                item.setNomenclature(request.getNomenclature());
            }

            long deptId = authUtil.getCurrentDepartmentId();
            MasDepartment depObj = masDepartmentRepository.getById(deptId);

            item.setLastChgBy(currentUser.getUserId());
            item.setLastChgDate(LocalDate.now());
            item.setLastChgTime(getCurrentTimeFormatted());


            Optional<MasHSN> masHSN = masHsnRepository.findById(request.getHsn());
            if (masHSN.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("MasHSN not found", 404);
            }

            if (request.getUnitAU() != null) {
                item.setUnitAU(masStoreUnitRepository.findById(request.getUnitAU())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "UnitAU not found")));
            }

            if (request.getSectionId() != null) {
                item.setSectionId(masStoreSectionRepository.findById(request.getSectionId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found")));
            }

            if (request.getItemTypeId() != null) {
                item.setItemTypeId(masItemTypeRepository.findById(request.getItemTypeId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemType not found")));
            }

            if (request.getGroupId() != null) {
                item.setGroupId(masStoreGroupRepository.findById(request.getGroupId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found")));
            }

            if (request.getItemClassId() != null) {
                item.setItemClassId(masItemClassRepository.findById(request.getItemClassId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemClass not found")));
            }


            if (request.getMasItemCategoryId() != null) {
                item.setMasItemCategory(masItemCategoryRepository.findById(request.getMasItemCategoryId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemCategory not found")));
            }
            MasHospital hospital = currentUser.getHospital();

            if (AppConstants.STATUS_Y.toLowerCase().equalsIgnoreCase(hospital.getRoIsManual())) {
                item .setStoreRoLManual(AppConstants.STATUS_Y.toLowerCase());
                item .setDispRoLManual(AppConstants.STATUS_Y.toLowerCase());
                item .setWardRoLManual(AppConstants.STATUS_Y.toLowerCase());

            } else if(AppConstants.STATUS_Y.toLowerCase().equalsIgnoreCase(hospital.getRolIsAuto())){
                item .setStoreRoLAuto(AppConstants.STATUS_Y.toLowerCase());
                item .setDispRoLAuto(AppConstants.STATUS_Y.toLowerCase());
                item .setWardRoLAuto(AppConstants.STATUS_Y.toLowerCase());
            }

            MasStoreItem updatedItem = masStoreItemRepository.save(item);



            return ResponseUtils.createSuccessResponse(convertToResponseNonDrug(updatedItem), new TypeReference<>() {});
        } catch (Exception ex) {
            log.error("An unexpected error occurred",ex);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<List<NonDrugStoreItemResponse>> getAllNonDrugItem() {

        List<NonDrugStoreItemProjection> projections = masStoreItemRepository.getAllNonDrugItems(sectionId);

        List<NonDrugStoreItemResponse> responseList = projections.stream()
                .map(p -> {
                    NonDrugStoreItemResponse dto = new NonDrugStoreItemResponse();

                    dto.setItemId(p.getItemId());
                    dto.setPvmsNo(p.getPvmsNo());
                    dto.setNomenclature(p.getNomenclature());

                    dto.setGroupId(p.getGroupId());
                    dto.setGroupName(p.getGroupName());

                    dto.setItemTypeId(p.getItemTypeId());
                    dto.setItemTypeName(p.getItemTypeName());

                    dto.setSectionId(p.getSectionId());
                    dto.setSectionName(p.getSectionName());

                    dto.setItemClassId(p.getItemClassId());
                    dto.setItemClassName(p.getItemClassName());

                    dto.setMasItemCategoryId(p.getMasItemCategoryId());
                    dto.setMasItemCategoryName(p.getMasItemCategoryName());

                    dto.setUnitAU(p.getUnitAU());
                    dto.setUnitAuName(p.getUnitAuName());

                    dto.setStatus(p.getStatus());

                    return dto;
                })
                .toList();

        return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});
    }
    @Override
    public ApiResponse<NonDrugStoreItemResponse> getNonDrugItemById(Long id) {
        try {
            Optional<MasStoreItem> masStoreItem = masStoreItemRepository.findById(id);
            if (masStoreItem.isPresent()) {
                MasStoreItem masStoreItem1 = masStoreItem.get();

                return ResponseUtils.createSuccessResponse(convertToResponseNonDrug(masStoreItem1), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "MasStoreItem not found", 404);
            }
        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }
    @Override
    public ApiResponse<Page<NonDrugStoreItemResponse>> medicalConsumableItem(
            int page,
            int size,
            String  itemName,
            Integer sectionId,
            Integer itemClassId){

        log.info("Fetching Medical Consumable Items. Page: {}, Size: {}, ItemName: {}, ItemClass: {}",
                page, size, itemName, itemClassId);

        try {

            Pageable pageable = PageRequest.of(page, size);


            Page<MedicalConsumableItemProjection> projectionPage = masStoreItemRepository.medicalConsumableItem(
                            drugSectionCode,
                            medicalConsumableItemTypeCode,
                            masItemGroup,
                            itemName,
                            sectionId,
                            itemClassId,
                            pageable);

            Page<NonDrugStoreItemResponse> responsePage = projectionPage.map(projection -> {
                NonDrugStoreItemResponse response = new NonDrugStoreItemResponse();

                response.setItemId(projection.getItemId());
                response.setPvmsNo(projection.getPvmsNo());
                response.setNomenclature(projection.getNomenclature());
                response.setGroupId(projection.getGroupId());
                response.setGroupName(projection.getGroupName());
                response.setItemTypeId(projection.getItemTypeId());
                response.setItemTypeName(projection.getItemTypeName());
                response.setSectionId(projection.getSectionId());
                response.setSectionName(projection.getSectionName());
                response.setItemClassId(projection.getItemClassId());
                response.setItemClassName(projection.getItemClassName());
                response.setMasItemCategoryId(projection.getMasItemCategoryId());
                response.setMasItemCategoryName(projection.getMasItemCategoryName());
                response.setUnitAU(projection.getUnitAU());
                response.setUnitAuName(projection.getUnitAuName());
                response.setStatus(projection.getStatus());

                return response;
            });

            log.info("Successfully fetched {} Medical Consumable Items.",
                    responsePage.getTotalElements());

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {
            });

        } catch (Exception ex) {

            log.error("Error while fetching Medical Consumable Items", ex);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Page<NonDrugStoreItemResponse>> nonMedicalConsumableItem(
            int page,
            int size,
            String  itemName,
            Integer sectionId,
            Integer itemClassId){

        log.info("Fetching Non Medical Consumable Items. Page: {}, Size: {}, ItemName: {}, ItemClass: {}",
                page, size, itemName, itemClassId);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<MedicalConsumableItemProjection> projectionPage = masStoreItemRepository.nonMedicalConsumableItem(
                            medicalNonConsumableItemTypeCode,
                            masItemGroup,
                            itemName,
                             sectionId,
                            itemClassId,
                            pageable);

            Page<NonDrugStoreItemResponse> responsePage = projectionPage.map(projection -> {
                NonDrugStoreItemResponse response = new NonDrugStoreItemResponse();

                response.setItemId(projection.getItemId());
                response.setPvmsNo(projection.getPvmsNo());
                response.setNomenclature(projection.getNomenclature());
                response.setGroupId(projection.getGroupId());
                response.setGroupName(projection.getGroupName());
                response.setItemTypeId(projection.getItemTypeId());
                response.setItemTypeName(projection.getItemTypeName());
                response.setSectionId(projection.getSectionId());
                response.setSectionName(projection.getSectionName());
                response.setItemClassId(projection.getItemClassId());
                response.setItemClassName(projection.getItemClassName());
                response.setMasItemCategoryId(projection.getMasItemCategoryId());
                response.setMasItemCategoryName(projection.getMasItemCategoryName());
                response.setUnitAU(projection.getUnitAU());
                response.setUnitAuName(projection.getUnitAuName());
                response.setStatus(projection.getStatus());

                return response;
            });

            log.info("Successfully fetched {} Medical Consumable Items.",
                    responsePage.getTotalElements());

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {
            });

        } catch (Exception ex) {

            log.error("Error while fetching Medical Consumable Items", ex);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private MasStoreItemResponseWithStock convertToResponseFast(MasStoreItem item,
                                                                Map<Long, List<StoreItemBatchStock>> stockMap) {

        long start = System.currentTimeMillis();

        MasStoreItemResponseWithStock response = new MasStoreItemResponseWithStock();
        response.setItemId(item.getItemId());
        response.setNomenclature(item.getNomenclature());
        response.setPvmsNo(item.getPvmsNo());
        response.setAdispQty(item.getAdispQty());
        response.setSectionId(item.getSectionId() != null ? item.getSectionId().getSectionId() : null);
        response.setItemClassId(item.getItemClassId() != null ? item.getItemClassId().getItemClassId() : null);
        response.setItemClassName(item.getItemClassId() != null ? item.getItemClassId().getItemClassName() : null);
        response.setDispUnit(item.getDispUnit() != null ? item.getDispUnit().getUnitId() : null);
        response.setDispUnitName(item.getDispUnit() != null ? item.getDispUnit().getUnitName() : null);

        response.setUnitAU(item.getUnitAU() != null ? item.getUnitAU().getUnitId() : null);
        response.setUnitAuName(item.getUnitAU() != null ? item.getUnitAU().getUnitName() : null);
        // Use preloaded stock
        List<StoreItemBatchStock> stockList = stockMap.getOrDefault(item.getItemId(), List.of());
        long hospitalId = getCurrentUser().getHospital().getId();

        long storeStocks = stockFound.calculateAvailableStock(stockList, hospitalId, deptIdStore, hospDefinedstoreDays);

        response.setStorestocks(storeStocks);

        long end = System.currentTimeMillis();
        System.out.println("⏱ convertToResponse() for itemId=" + item.getItemId() + " took: " + (end - start) + " ms");

        return response;
    }



    private MasStoreItemResponse convertToResponse(MasStoreItem item) {
        MasStoreItemResponse response = new MasStoreItemResponse();
        response.setItemId(item.getItemId());
        response.setNomenclature(item.getNomenclature());
        response.setPvmsNo(item.getPvmsNo());
        response.setStatus(item.getStatus());
        response.setReOrderLevelStore(item.getStoreROL());
        response.setReOrderLevelDispensary(item.getDispROL());
        response.setLastChgDate(item.getLastChgDate());
        response.setLastChgBy(item.getLastChgBy());
        response.setLastChgTime(item.getLastChgTime());
        response.setAdispQty(item.getAdispQty());
        response.setReOrderLevelStore(item.getStoreROL());
        response.setReOrderLevelDispensary(item.getDispROL());

        response.setGroupId(item.getGroupId() != null ? item.getGroupId().getId() : null);
        response.setGroupName(item.getGroupId() != null ? item.getGroupId().getGroupName() : null);


        response.setItemClassId(item.getItemClassId() != null ? item.getItemClassId().getItemClassId() : null);
        response.setItemClassName(item.getItemClassId() != null ? item.getItemClassId().getItemClassName() : null);


        response.setItemTypeId(item.getItemTypeId() != null ? item.getItemTypeId().getId() : null);
        response.setItemTypeName(item.getItemTypeId() != null ? item.getItemTypeId().getName() : null);

        response.setSectionId(item.getSectionId() != null ? item.getSectionId().getSectionId() : null);
        response.setSectionName(item.getSectionId() != null ? item.getSectionId().getSectionName() : null);

        response.setDispUnit(item.getDispUnit() != null ? item.getDispUnit().getUnitId() : null);
        response.setDispUnitName(item.getDispUnit() != null ? item.getDispUnit().getUnitName() : null);

        response.setUnitAU(item.getUnitAU() != null ? item.getUnitAU().getUnitId() : null);
        response.setUnitAuName(item.getUnitAU() != null ? item.getUnitAU().getUnitName() : null);

        response.setHsnCode(item.getHsnCode() != null ? item.getHsnCode().getHsnCode() : null);
        response.setHsnGstPercent(item.getHsnCode() != null ? item.getHsnCode().getGstRate() : null);

        response.setMasItemCategoryid(item.getMasItemCategory()!=null?item.getMasItemCategory().getItemCategoryId():null);
        response.setMasItemCategoryName(item.getMasItemCategory()!=null?item.getMasItemCategory().getItemCategoryName():null);
        response.setDangerousDrug(item.getDangerousDrug());
        response.setIsGeneric(item.getIsGeneric());
        response.setDrugSchedule(item.getDrugSchedule());
        response.setHighValueDrug(item.getHighValueDrug());
        response.setAvailableInOpd(item.getAvailableInOpd());
        response.setAvailableInIpd(item.getAvailableInIpd());
        response.setAvailableInEmergency(item.getAvailableInEmergency());
        response.setAvailableInOt(item.getAvailableInOt());

        List<MasStoreItemResponse.MasFacilityCodeResponse> facilityList = new ArrayList<>();
        List<StoreItemFacilityMap> storeItemFacilityMaps=storeItemFacilityMapRepository.findByItemItemId(item.getItemId());
        for (StoreItemFacilityMap map : storeItemFacilityMaps) {

                MasStoreItemResponse.MasFacilityCodeResponse facilityResponse = new MasStoreItemResponse.MasFacilityCodeResponse();
                facilityResponse.setFacilityId(map.getFacility() != null ? map.getFacility().getFacilityId() : null);
                facilityResponse.setFacilityCode(map.getFacility() != null ? map.getFacility().getFacilityCode() : null);
                facilityList.add(facilityResponse);
            }
        response.setFacilityCode(facilityList);
        return response;
    }

    private MasStoreItemResponse convertToResponsewithAllStock(MasStoreItem item, Map<Long, List<StoreItemBatchStock>> stockMap) {
        MasStoreItemResponse response = new MasStoreItemResponse();
        response.setItemId(item.getItemId());
        response.setNomenclature(item.getNomenclature());
        response.setPvmsNo(item.getPvmsNo());
        response.setStatus(item.getStatus());
        response.setReOrderLevelStore(item.getStoreROL());
        response.setReOrderLevelDispensary(item.getDispROL());
        response.setLastChgDate(item.getLastChgDate());
        response.setLastChgBy(item.getLastChgBy());
        response.setLastChgTime(item.getLastChgTime());
        response.setAdispQty(item.getAdispQty());
        response.setDangerousDrug(item.getDangerousDrug());
        response.setIsGeneric(item.getIsGeneric());
        response.setDrugSchedule(item.getDrugSchedule());
        response.setHighValueDrug(item.getHighValueDrug());
        response.setAvailableInOpd(item.getAvailableInOpd());
        response.setAvailableInIpd(item.getAvailableInIpd());
        response.setAvailableInEmergency(item.getAvailableInEmergency());
        response.setAvailableInOt(item.getAvailableInOt());


        response.setGroupId(item.getGroupId() != null ? item.getGroupId().getId() : null);
        response.setGroupName(item.getGroupId() != null ? item.getGroupId().getGroupName() : null);


        response.setItemClassId(item.getItemClassId() != null ? item.getItemClassId().getItemClassId() : null);
        response.setItemClassName(item.getItemClassId() != null ? item.getItemClassId().getItemClassName() : null);


        response.setItemTypeId(item.getItemTypeId() != null ? item.getItemTypeId().getId() : null);
        response.setItemTypeName(item.getItemTypeId() != null ? item.getItemTypeId().getName() : null);

        response.setSectionId(item.getSectionId() != null ? item.getSectionId().getSectionId() : null);
        response.setSectionName(item.getSectionId() != null ? item.getSectionId().getSectionName() : null);

        response.setDispUnit(item.getDispUnit() != null ? item.getDispUnit().getUnitId() : null);
        response.setDispUnitName(item.getDispUnit() != null ? item.getDispUnit().getUnitName() : null);

        response.setUnitAU(item.getUnitAU() != null ? item.getUnitAU().getUnitId() : null);
        response.setUnitAuName(item.getUnitAU() != null ? item.getUnitAU().getUnitName() : null);

        response.setHsnCode(item.getHsnCode() != null ? item.getHsnCode().getHsnCode() : null);
        response.setHsnGstPercent(item.getHsnCode() != null ? item.getHsnCode().getGstRate() : null);

        response.setMasItemCategoryid(item.getMasItemCategory()!=null?item.getMasItemCategory().getItemCategoryId():null);
        response.setMasItemCategoryName(item.getMasItemCategory()!=null?item.getMasItemCategory().getItemCategoryName():null);


        Long avlableStokes = stockFound.getAvailableStocks(authUtil.getCurrentUser().getHospital().getId(), deptIdStore, item.getItemId(), hospDefinedstoreDays);
        response.setStorestocks(avlableStokes);
        Long dispstocks = stockFound.getAvailableStocks(authUtil.getCurrentUser().getHospital().getId(), dispdeptId, item.getItemId(), hospDefineddispDays);
        response.setDispstocks(dispstocks);
        Long wardstocks = stockFound.getAvailableStocks(authUtil.getCurrentUser().getHospital().getId(), warddeptId, item.getItemId(), hospDefinedwardDays);
        response.setWardstocks(wardstocks );

        return response;
    }


    private MasStoreItemResponseDto convertToResponse2(MasStoreItem item) {
        MasStoreItemResponseDto response = new MasStoreItemResponseDto();
        response.setId(item.getItemId());
        response.setCode(item.getPvmsNo());
        response.setName(item.getNomenclature());
        response.setUnit(item.getUnitAU()!=null?item.getUnitAU().getUnitName():null);
        response.setDispUnit(item.getDispUnit()!=null?item.getUnitAU().getUnitName():null);
        response.setHsnGstPercentage(item.getHsnCode()!=null?item.getHsnCode().getGstRate():null);
        response.setHsnCode(item.getHsnCode()!=null?item.getHsnCode().getHsnCode():null);
        response.setADispQty(item.getAdispQty());
        response.setItemClassName(item.getItemClassId() !=null ? item.getItemClassId().getItemClassName() : null);
        return response;
    }

    private NonDrugStoreItemResponse convertToResponseNonDrug(MasStoreItem item) {
        NonDrugStoreItemResponse response = new NonDrugStoreItemResponse();
        response.setItemId(item.getItemId());
        response.setNomenclature(item.getNomenclature());
        response.setPvmsNo(item.getPvmsNo());
        response.setStatus(item.getStatus());

        response.setHsn(item.getHsnCode() != null ? item.getHsnCode().getHsnCode() : null);

        response.setGroupId(item.getGroupId() != null ? item.getGroupId().getId() : null);
        response.setGroupName(item.getGroupId() != null ? item.getGroupId().getGroupName() : null);

        response.setItemClassId(item.getItemClassId() != null ? item.getItemClassId().getItemClassId() : null);
        response.setItemClassName(item.getItemClassId() != null ? item.getItemClassId().getItemClassName() : null);

        response.setItemTypeId(item.getItemTypeId() != null ? item.getItemTypeId().getId() : null);
        response.setItemTypeName(item.getItemTypeId() != null ? item.getItemTypeId().getName(): null);

        response.setSectionId(item.getSectionId() != null ? item.getSectionId().getSectionId() : null);
        response.setSectionName(item.getSectionId() != null ? item.getSectionId().getSectionName() : null);

        response.setUnitAU(item.getUnitAU() != null ? item.getUnitAU().getUnitId() : null);
        response.setUnitAuName(item.getUnitAU() != null ? item.getUnitAU().getUnitName() : null);

        response.setMasItemCategoryId(item.getMasItemCategory()!=null?item.getMasItemCategory().getItemCategoryId():null);
        response.setMasItemCategoryName(item.getMasItemCategory()!=null?item.getMasItemCategory().getItemCategoryName():null);

        return response;
    }
    private MasStoreItemResponse convertProjectionToResponse(
            MasStoreItemsProjection item,
            Map<Long, List<MasStoreItemResponse.MasFacilityCodeResponse>> facilityMap
    ) {

        MasStoreItemResponse response = new MasStoreItemResponse();

        response.setItemId(item.getItemId());
        response.setPvmsNo(item.getPvmsNo());
        response.setNomenclature(item.getNomenclature());
        response.setStatus(item.getStatus());

        response.setLastChgBy(item.getLastChgBy());
        response.setLastChgDate(item.getLastChgDate());
        response.setLastChgTime(item.getLastChgTime());

        response.setStorestocks(item.getStorestocks());
        response.setDispstocks(item.getDispstocks());
        response.setWardstocks(item.getWardstocks());
        response.setAdispQty(item.getAdispQty());

        response.setHospitalId(item.getHospitalId());
        response.setDepartmentId(item.getDepartmentId());

        response.setDispUnit(item.getDispUnit());
        response.setDispUnitName(item.getDispUnitName());

        response.setUnitAU(item.getUnitAU());
        response.setUnitAuName(item.getUnitAuName());

        response.setSectionId(item.getSectionId());
        response.setSectionName(item.getSectionName());

        response.setItemTypeId(item.getItemTypeId());
        response.setItemTypeName(item.getItemTypeName());

        response.setGroupId(item.getGroupId());
        response.setGroupName(item.getGroupName());

        response.setItemClassId(item.getItemClassId());
        response.setItemClassName(item.getItemClassName());

        response.setMasItemCategoryid(item.getMasItemCategoryid());
        response.setMasItemCategoryName(item.getMasItemCategoryName());

        response.setHsnCode(item.getHsnCode());
        response.setHsnGstPercent(item.getHsnGstPercent());

        response.setReOrderLevelDispensary(item.getReOrderLevelDispensary());
        response.setReOrderLevelStore(item.getReOrderLevelStore());

        response.setIsGeneric(item.getIsGeneric());
        response.setDangerousDrug(item.getDangerousDrug());
        response.setDrugSchedule(item.getDrugSchedule());
        response.setHighValueDrug(item.getHighValueDrug());
        response.setAvailableInOpd(item.getAvailableInOpd());
        response.setAvailableInIpd(item.getAvailableInIpd());
        response.setAvailableInEmergency(item.getAvailableInEmergency());
        response.setAvailableInOt(item.getAvailableInOt());
        response.setFacilityCode(facilityMap.getOrDefault(item.getItemId(), Collections.emptyList()));

        return response;
    }

    String normalizeYN(String val) {
        if (val == null) {
            return null;
        }
        String lower = val.trim().toLowerCase();
        if ("y".equals(lower) || "n".equals(lower)) {
            return lower;
        }
        return val;
    }

}