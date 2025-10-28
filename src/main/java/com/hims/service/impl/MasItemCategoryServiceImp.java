package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasItemCategory;
import com.hims.entity.MasItemClass;
import com.hims.entity.MasStoreSection;
import com.hims.entity.User;
import com.hims.entity.repository.MasItemCategoryRepository;
import com.hims.entity.repository.MasStoreSectionRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasItemCategoryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasItemCategoryResponse;
import com.hims.response.MasItemClassResponse;
import com.hims.service.MasItemCategoryService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasItemCategoryServiceImp implements MasItemCategoryService {

@Autowired
private MasItemCategoryRepository masItemCategoryRepository;
        @Autowired
        private MasStoreSectionRepository masStoreSectionRepository;
    @Autowired
    private UserRepo userRepo;
    private static final Logger log = LoggerFactory.getLogger(MasStateServiceImpl.class);
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
    public ApiResponse<MasItemCategoryResponse> addMasItemCategory(MasItemCategoryRequest masItemCategoryRequest) {
        Optional<MasStoreSection> masStoreSection=masStoreSectionRepository.findById(masItemCategoryRequest.getSectionId());
        if( masStoreSection.isEmpty()){
            return ResponseUtils.createNotFoundResponse("MasStoreSection not found", 404);
        }
        MasItemCategory masItemCategory=new MasItemCategory();
        masItemCategory.setMasStoreSection(masStoreSection.get());
        masItemCategory.setItemCategoryCode(masItemCategoryRequest.getItemCategoryCode());
        masItemCategory.setItemCategoryName(masItemCategoryRequest.getItemCategoryName());
        masItemCategory.setStatus("y");
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }
        masItemCategory.setLastChgBy(String.valueOf(currentUser.getUserId()));
        masItemCategory.setLastChgDate(LocalDate.now());
        masItemCategory.setLastChgTime(getCurrentTimeFormatted());

        MasItemCategory masItemCategory1 = masItemCategoryRepository.save(masItemCategory);
        return ResponseUtils.createSuccessResponse(mapToResponse( masItemCategory1), new TypeReference<>() {
        });


    }

    @Override
    public ApiResponse<List<MasItemCategoryResponse>> getAllMasItemCategory(int flag) {
        List<MasItemCategory> masItemCategory;

        if (flag == 1) {
            masItemCategory = masItemCategoryRepository.findByStatusIgnoreCase("y");
        } else if (flag == 0) {
            masItemCategory= masItemCategoryRepository.findByStatusInIgnoreCase(List.of("y", "n"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasItemCategoryResponse> responses = masItemCategory.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
        });
    }

    @Override
    public ApiResponse<MasItemCategoryResponse> findById(Integer id) {
        Optional<MasItemCategory> masItemCategory =masItemCategoryRepository.findById(id);
        if (masItemCategory .isPresent()) {
            MasItemCategory masItemCategory1 = masItemCategory .get();

            return ResponseUtils.createSuccessResponse(mapToResponse(masItemCategory1), new TypeReference<>() {
            });
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "MasItemCategory not found", 404);
        }
    }

    @Override
    public ApiResponse<MasItemCategoryResponse> changeMasItemCategoryStatus(int id, String status) {
        Optional<MasItemCategory> masItemCategory = masItemCategoryRepository.findById(id);
        if (masItemCategory.isPresent()) {
            MasItemCategory masItemCategory1=  masItemCategory.get();
            if ("y".equals(status) || "n".equals(status)) {
                masItemCategory1.setStatus(status);
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                masItemCategory1.setLastChgBy(String.valueOf(currentUser.getUserId()));
                masItemCategory1.setLastChgDate(LocalDate.now());
                masItemCategory1.setLastChgTime(getCurrentTimeFormatted());
                return ResponseUtils.createSuccessResponse(mapToResponse( masItemCategoryRepository.save(masItemCategory1)), new TypeReference<>() {
                });

            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid status. Status should be 'y' or 'n'", 400);
            }

        } else {
            return ResponseUtils.createNotFoundResponse("MasItemCategory not found", 404);
        }
    }

    @Override
    public ApiResponse<MasItemCategoryResponse> updateMasItemClass(int id, MasItemCategoryRequest masItemCategoryRequest) {
        Optional<MasItemCategory> masItemCategory = masItemCategoryRepository.findById(id);
        if (masItemCategory.isPresent()) {
            MasItemCategory masItemCategory1 = masItemCategory.get();
            masItemCategory1.setItemCategoryCode(masItemCategoryRequest.getItemCategoryCode());
            masItemCategory1.setItemCategoryName(masItemCategoryRequest.getItemCategoryName());
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            masItemCategory1.setLastChgBy(String.valueOf(currentUser.getUserId()));
            masItemCategory1.setLastChgDate(LocalDate.now());
            masItemCategory1.setLastChgTime(getCurrentTimeFormatted());

            if (masItemCategoryRequest.getSectionId() != null) {
                Optional<MasStoreSection> masStoreSection = masStoreSectionRepository.findById(masItemCategoryRequest.getSectionId());
                if (masStoreSection.isPresent()) {
                    masItemCategory1.setMasStoreSection(masStoreSection.get());
                } else {
                    return ResponseUtils.createNotFoundResponse("MasStoreSection not found", 404);
                }
            }
            return ResponseUtils.createSuccessResponse(mapToResponse(masItemCategoryRepository.save(masItemCategory1 )), new TypeReference<>() {
            });


        }else{
            return ResponseUtils.createNotFoundResponse("MasItemCategory not found", 404);
        }

    }

    @Override
    public ApiResponse<List<MasItemCategoryResponse>> findByMasItemCategoryBbySectionId(int id) {
        List<MasItemCategory> masItemCategory=masItemCategoryRepository.findByMasStoreSectionSectionId(id);



        List<MasItemCategoryResponse> responses = masItemCategory.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
        });
    }

    private MasItemCategoryResponse mapToResponse(MasItemCategory masItemCategory) {
        MasItemCategoryResponse response = new  MasItemCategoryResponse();
        response.setItemCategoryId(masItemCategory.getItemCategoryId());
        response.setItemCategoryCode(masItemCategory.getItemCategoryCode());
        response.setItemCategoryName(masItemCategory.getItemCategoryName());
        response.setStatus(masItemCategory.getStatus());
        response.setLastChgBy(masItemCategory.getLastChgBy());
        response.setLastChgTime(masItemCategory.getLastChgTime());
        response.setLastChgDate(masItemCategory.getLastChgDate());
       // response.setSectionId(masItemCategory.getMasStoreSection().getSectionId());
        if(masItemCategory.getMasStoreSection()!=null){
            response.setSectionId(masItemCategory.getMasStoreSection().getSectionId());
            response.setSectionName(masItemCategory.getMasStoreSection().getSectionName());

        }

        return response;
    }
}
