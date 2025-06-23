package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.MasStoreItemRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStoreItemResponse;
import com.hims.response.MasStoreUnitResponse;
import com.hims.response.MasTemplateResponse;
import com.hims.service.MasStoreItemService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
    private AuthUtil authUtil;

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

            User users = authUtil.getCurrentUser();
            Long deptId = authUtil.getCurrentDepartmentId();

            if (users == null || deptId == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "User or Department ID missing", HttpStatus.UNAUTHORIZED.value());
            }


            MasStoreItem masStoreItem = new MasStoreItem();
            masStoreItem.setPvmsNo(masStoreItemRequest.getPvmsNo());
            masStoreItem.setNomenclature(masStoreItemRequest.getNomenclature());
            masStoreItem.setStatus("y");
            masStoreItem.setADispQty(masStoreItemRequest.getADispQty());
            masStoreItem.setHospitalId(1);
            masStoreItem.setDepartmentId(deptId);
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
            Optional<MasHSN> masHSN = masHsnRepository.findById(masStoreItemRequest.getHsnCode());
            if (masHSN.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("MasSHN not found", 404);

            }
            masStoreItem.setDispUnit(masStoreUnit.get());
            masStoreItem.setUnitAU(masStoreUnit1.get());
            masStoreItem.setItemClassId(masItemClass.get());
            masStoreItem.setGroupId(masStoreGroup.get());
            masStoreItem.setItemTypeId(masItemType.get());
            masStoreItem.setSectionId(masStoreSection.get());
            masStoreItem.setHsnCode(masHSN.get());

            MasStoreItem masStoreItem1 = masStoreItemRepository.save(masStoreItem);
            return ResponseUtils.createSuccessResponse(convertToResponse(masStoreItem1), new TypeReference<>() {
            });
        }catch(Exception ex){
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<MasStoreItemResponse> findById(Integer id) {
        try{
        Optional<MasStoreItem> masStoreItem=masStoreItemRepository.findById(id);
        if (masStoreItem .isPresent()) {
            MasStoreItem masStoreItem1 = masStoreItem.get();

            return ResponseUtils.createSuccessResponse( convertToResponse(masStoreItem1), new TypeReference<>() {
            });
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "MasStoreItem not found", 404);
        }}catch(Exception ex){
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<MasStoreItemResponse>> getAllMasStoreItem(int flag) {
        try {
            List<MasStoreItem> masStoreItems;
            if (flag == 1) {
                masStoreItems = masStoreItemRepository.findByStatusIgnoreCase("y");
            } else if (flag == 0) {
                masStoreItems = masStoreItemRepository.findByStatusInIgnoreCase(List.of("y", "n"));
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid flag value. Use 0 or 1.", 400);
            }

            List<MasStoreItemResponse> responses = masStoreItems.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
            });


        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        

        }


    }

    @Override
    public ApiResponse<MasStoreItemResponse> update(Integer id, MasStoreItemRequest request) {
        try{
        Optional<MasStoreItem> masStoreItem = masStoreItemRepository.findById(id);
            if (masStoreItem.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "MasStoreItem not found", 404);
            }
            User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                MasStoreItem masStoreItem1 = masStoreItem.get();
                masStoreItem1.setPvmsNo(request.getPvmsNo());
                masStoreItem1.setNomenclature(request.getNomenclature());
                masStoreItem1.setStatus(request.getStatus());
                masStoreItem1.setADispQty(request.getADispQty());
                masStoreItem1.setHospitalId(1);
                masStoreItem1.setLastChgBy(currentUser.getUserId());
                masStoreItem1.setLastChgDate(LocalDate.now());
                masStoreItem1.setLastChgTime(getCurrentTimeFormatted());
                masStoreItem1.setReOrderLevelStore(request.getReOrderLevelStore());
                masStoreItem1.setReOrderLevelDispensary(request.getReOrderLevelDispensary());
                if (request.getDispUnit()!=null) {
                    Optional<MasStoreUnit> masStoreUnit = masStoreUnitRepository.findById(request.getDispUnit());
                    if(masStoreUnit.isPresent()){
                        masStoreItem1.setUnitAU(masStoreUnit.get());
                    }else{
                        return ResponseUtils.createNotFoundResponse("MasStoreUnit not found", 404);
                    }
                }
                if (request.getUnitAU()!=null) {
                    Optional<MasStoreUnit> masStoreUnit1 = masStoreUnitRepository.findById(request.getUnitAU());
                    if(masStoreUnit1.isPresent()){
                        masStoreItem1.setUnitAU(masStoreUnit1.get());
                    }else {
                    return ResponseUtils.createNotFoundResponse("MasStoreUnit not found", 404);
                }}
                    if (request.getSectionId()!=null) {
                    Optional<MasStoreSection> masStoreSection = masStoreSectionRepository.findById(request.getSectionId());
                    if(masStoreSection.isPresent()){
                      masStoreItem1.setSectionId(masStoreSection.get());
                    }else{
                    return ResponseUtils.createNotFoundResponse("MasStoreSection not found", 404);
                }}

                if (request.getItemTypeId()!=null) {
                    Optional<MasItemType> masItemType = masItemTypeRepository.findById(request.getItemTypeId());
                    if(masItemType.isPresent()){
                        masStoreItem1.setItemTypeId(masItemType.get());
                    }else{
                        return ResponseUtils.createNotFoundResponse("MasItemType not found", 404);
                }}

                if (request.getGroupId()!=null) {
                    Optional<MasStoreGroup> masStoreGroup = masStoreGroupRepository.findById(request.getGroupId());
                    if(masStoreGroup.isPresent()) {
                        masStoreItem1.setGroupId(masStoreGroup.get());

                    }else{
                        return ResponseUtils.createNotFoundResponse("MasStoreGroup not found", 404);
                }
                }

                if (request.getItemClassId()!=null) {
                    Optional<MasItemClass> masItemClass = masItemClassRepository.findById(request.getItemClassId());
                    if(masItemClass.isPresent()) {
                        masStoreItem1.setItemClassId(masItemClass.get());
                    }else{
                    return ResponseUtils.createNotFoundResponse("MasItemClass not found", 404);

                }}
            if (request.getHsnCode()!=null) {
                Optional<MasHSN> masHSN = masHsnRepository.findById(request.getHsnCode());
                if(masHSN.isPresent()) {
                    masStoreItem1.setHsnCode(masHSN.get());
                }else{
                    return ResponseUtils.createNotFoundResponse("MasHSN not found", 404);

                }}

                MasStoreItem masStoreItem2= masStoreItemRepository.save(masStoreItem1);
                return ResponseUtils.createSuccessResponse(convertToResponse(masStoreItem2), new TypeReference<>() {
                });
        }catch(Exception ex){
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());

        }
        }

    @Override
    public ApiResponse<MasStoreItemResponse> changeMasStoreItemStatus(int id, String status) {
        try {
            Optional<MasStoreItem> masStoreItem= masStoreItemRepository.findById(id);
            if (masStoreItem.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "MasStoreItem not found with ID: " + id, HttpStatus.NOT_FOUND.value());
            }
            MasStoreItem entity = masStoreItem.get();
            if(status.equals("y")||status.equals("n")){
                entity.setStatus(status);
            }else{
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid status. Status should be 'y' or 'n'", 400);
            }
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            entity.setLastChgBy(currentUser.getUserId());
            entity.setLastChgDate(LocalDate.now());
            entity.setLastChgTime(getCurrentTimeFormatted());

            return ResponseUtils.createSuccessResponse(convertToResponse( masStoreItemRepository.save(entity)), new TypeReference<>() {
            });
        } catch (Exception ex) {
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
        response.setHsnCode(item.getHsnCode().getHsnCode());
        return response;
    }
}

