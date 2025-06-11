package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.MasStoreItemRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStoreItemResponse;
import com.hims.response.MasStoreUnitResponse;
import com.hims.service.MasStoreItemService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
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
    UserRepo userRepo;

    @Autowired
    private UserDepartmentRepository userDepartmentRepository;


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
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            MasStoreItem masStoreItem = new MasStoreItem();
            masStoreItem.setPvmsNo(masStoreItemRequest.getPvmsNo());
            masStoreItem.setNomenclature(masStoreItemRequest.getNomenclature());
            masStoreItem.setStatus("y");
            masStoreItem.setADispQty(masStoreItemRequest.getADispQty());
            masStoreItem.setHospitalId(1);

            masStoreItem.setLastChgBy(currentUser.getUserId());
            masStoreItem.setLastChgDate(LocalDate.now());
            masStoreItem.setLastChgTime(getCurrentTimeFormatted());
            masStoreItem.setReOrderLevelStore(masStoreItemRequest.getReOrderLevelStore());
            masStoreItem.setReOrderLevelDispensary(masStoreItemRequest.getReOrderLevelDispensary());
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
            masStoreItem.setDispUnit(masStoreUnit.get());
            masStoreItem.setUnitAU(masStoreUnit1.get());
            masStoreItem.setItemClassId(masItemClass.get());
            masStoreItem.setGroupId(masStoreGroup.get());
            masStoreItem.setItemTypeId(masItemType.get());
            masStoreItem.setSectionId(masStoreSection.get());
            MasStoreItem masStoreItem1 = masStoreItemRepository.save(masStoreItem);
            return ResponseUtils.createSuccessResponse(convertToResponse(masStoreItem1), new TypeReference<>() {
            });
        }catch(Exception ex){
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }



    private MasStoreItemResponse convertToResponse(MasStoreItem item) {
        MasStoreItemResponse  response = new MasStoreItemResponse();
        response.setItemId(item.getItemId());
        response.setNomenclature(item.getNomenclature());
        response.setPvmsNo(item.getPvmsNo());
        response.setStatus(item.getStatus());
        response.setReOrderLevelStore(item.getReOrderLevelStore());
        response.setReOrderLevelDispensary(item.getReOrderLevelDispensary());
        response.setLastChgDate(item.getLastChgDate());
        response.setLastChgBy(item.getLastChgBy());
        response.setLastChgTime(item.getLastChgTime());
        response.setHospitalId(item.getHospitalId());
        response.setGroupId(item.getGroupId().getId());
        response.setItemClassId(item.getItemClassId().getItemClassId());
        response.setItemTypeId(item.getItemTypeId().getId());
        response.setSectionId(item.getSectionId().getSectionId());
        response.setDispUnit(item.getDispUnit().getUnitId());
        response.setUnitAU(item.getUnitAU().getUnitId());
        response.setADispQty(item.getADispQty());
        return response;
    }

}

