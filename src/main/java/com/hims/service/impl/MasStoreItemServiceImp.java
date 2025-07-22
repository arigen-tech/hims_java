package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.MasStoreItemRequest;
import com.hims.response.*;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

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
    private MasItemCategoryRepository masItemCategoryRepository;

    @Autowired
    UserRepo userRepo;
    @Autowired
    AuthUtil authUtil;

    @Autowired
    private UserDepartmentRepository userDepartmentRepository;
@Autowired
private MasDepartmentRepository masDepartmentRepository;

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
        masStoreItem.setHospitalId(currentUser.getHospital().getId());
        masStoreItem.setDepartmentId(depObj.getId());
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

        MasStoreItem savedItem = masStoreItemRepository.save(masStoreItem);

        return ResponseUtils.createSuccessResponse(convertToResponse(savedItem), new TypeReference<>() {});
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
    public ApiResponse<List<MasStoreItemResponse>> getAllMasStoreItem(int flag, Long hospitalId, Long departmentId) {

        List<MasStoreItem> masStoreItems;
        if (flag == 1) {
            masStoreItems = masStoreItemRepository.findByStatusIgnoreCaseAndHospitalIdAndDepartmentId("y",hospitalId,departmentId);
        } else if (flag == 0) {
            masStoreItems = masStoreItemRepository.findByStatusInIgnoreCaseAndHospitalIdAndDepartmentId(List.of("y", "n"),hospitalId,departmentId);
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasStoreItemResponse> responses = masStoreItems.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
        });
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
            item.setReOrderLevelStore(request.getReOrderLevelStore());
            item.setReOrderLevelDispensary(request.getReOrderLevelDispensary());
            item.setHospitalId(currentUser.getHospital().getId());
            item.setDepartmentId(depObj.getId());
            item.setLastChgBy(currentUser.getUserId());
            item.setLastChgDate(LocalDate.now());
            item.setLastChgTime(getCurrentTimeFormatted());

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

            MasStoreItem updatedItem = masStoreItemRepository.save(item);

            return ResponseUtils.createSuccessResponse(convertToResponse(updatedItem), new TypeReference<>() {});
        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
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
            if (status.equals("y") || status.equals("n")) {
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
    public ApiResponse<List<MasStoreItemResponse2>> getAllMasStore(int flag, Long hospitalId, Long departmentId) {

         List<MasStoreItem> masStoreItems;
         if (flag == 1) {
             masStoreItems = masStoreItemRepository.findByStatusIgnoreCaseAndHospitalIdAndDepartmentId("y",hospitalId,departmentId);
         }
          else {
             return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
             }, "Invalid flag value. Use 1.", 400);
         }

         List<MasStoreItemResponse2> responses = masStoreItems.stream()
                 .map(this::convertToResponse2)
                 .collect(Collectors.toList());

         return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
         });


     }

    private MasStoreItemResponse convertToResponse(MasStoreItem item) {
        MasStoreItemResponse response = new MasStoreItemResponse();
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
        response.setDepartmentId(item.getDepartmentId());
        response.setAdispQty(item.getAdispQty());

            response.setGroupId(item.getGroupId()!=null?item.getGroupId().getId():null);
            response.setGroupName(item.getGroupId()!=null?item.getGroupId().getGroupName():null);



            response.setItemClassId(item.getItemClassId()!=null?item.getItemClassId().getItemClassId():null);
            response.setItemClassName(item.getItemClassId()!=null?item.getItemClassId().getItemClassName():null);



            response.setItemTypeId(item.getItemTypeId()!=null?item.getItemTypeId().getId():null);
            response.setItemTypeName(item.getItemTypeId()!=null?item.getItemTypeId().getName():null);

            response.setSectionId(item.getSectionId()!=null?item.getSectionId().getSectionId():null);
            response.setSectionName(item.getSectionId()!=null?item.getSectionId().getSectionName():null);

            response.setDispUnit(item.getDispUnit() !=null?item.getDispUnit().getUnitId():null);
            response.setDispUnitName(item.getDispUnit()!=null?item.getDispUnit().getUnitName():null);

            response.setUnitAU(item.getUnitAU()!=null?item.getUnitAU().getUnitId():null);
            response.setUnitAuName(item.getUnitAU()!=null?item.getUnitAU().getUnitName():null);

            response.setHsnCode(item.getHsnCode()!=null?item.getHsnCode().getHsnCode():null);
            response.setHsnGstPercent(item.getHsnCode()!=null?item.getHsnCode().getGstRate():null);

        response.setMasItemCategoryid(item.getMasItemCategory()!=null?item.getMasItemCategory().getItemCategoryId():null);
        response.setMasItemCategoryName(item.getMasItemCategory()!=null?item.getMasItemCategory().getItemCategoryName():null);
        return response;
    }

    private MasStoreItemResponse2 convertToResponse2(MasStoreItem item) {
        MasStoreItemResponse2 response = new MasStoreItemResponse2();
        response.setId(item.getItemId());
        response.setCode(item.getPvmsNo());
        response.setName(item.getNomenclature());
        response.setUnit(item.getUnitAU()!=null?item.getUnitAU().getUnitName():null);
        response.setDispUnit(item.getDispUnit()!=null?item.getUnitAU().getUnitName():null);
        response.setHsnGstPercentage(item.getHsnCode()!=null?item.getHsnCode().getGstRate():null);
        response.setHsnCode(item.getHsnCode()!=null?item.getHsnCode().getHsnCode():null);
        return response;
    }

}