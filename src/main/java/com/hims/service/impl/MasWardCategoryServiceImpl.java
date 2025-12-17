package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.DgUom;
import com.hims.entity.MasWardCategory;
import com.hims.entity.User;
import com.hims.entity.repository.MasCareLevelRepo;
import com.hims.entity.repository.MasWardCategoryRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasWardCategoryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.response.DgUomResponse;
import com.hims.response.MasWardCategoryResponse;
import com.hims.service.MasWardCategoryService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
public class MasWardCategoryServiceImpl implements MasWardCategoryService {
    @Autowired
    AuthUtil authUtil;
    @Autowired
    private MasWardCategoryRepository masWardCategoryRepository;
    @Autowired
    private MasCareLevelRepo masCareLevelRepo;


    @Override
    public ApiResponse<List<MasWardCategoryResponse>> getAllMasWardCategory(int flag) {
        try {
            log.info("Mas Ward Category get List Start");
            List<MasWardCategory> masWardCategories;
            if (flag == 1) {
                masWardCategories = masWardCategoryRepository.findByStatusOrderByCategoryNameAsc("y");
            } else if (flag == 0) {
                masWardCategories = masWardCategoryRepository.findAllByOrderByStatusDescLastUpdateDateDesc();

            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid flag value. Use 0 or 1.", 400);
            }
            List<MasWardCategoryResponse> responses = masWardCategories.stream()
                    .map(this::mapToConverted)
                    .collect(Collectors.toList());
            log.info("Mas Ward Category List succes");
            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
            });
        }catch(Exception e){
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<MasWardCategoryResponse> findById(Long id) {
        Optional<MasWardCategory> masWardCategory=masWardCategoryRepository.findById(id);
        if(masWardCategory.isEmpty()){
            return ResponseUtils.createNotFoundResponse("MasWardCategory data not found",  404);
        }
        MasWardCategory masWardCategory1=masWardCategory.get();
        return ResponseUtils.createSuccessResponse(mapToConverted(masWardCategory1),new TypeReference<>(){});
    }

    @Override
    public ApiResponse<MasWardCategoryResponse> addMasWard(MasWardCategoryRequest request) {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found",400);
        }
        MasWardCategory masWardCategory=new MasWardCategory();
        masWardCategory.setCategoryName(request.getCategoryName());
        masWardCategory.setDescription(request.getDescription());
        masWardCategory.setMasCareLevel(masCareLevelRepo.findById(request.getCareId()).orElseThrow(()-> new RuntimeException("Invalid care level Id")));
        masWardCategory.setStatus("y");
        masWardCategory.setCreatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
        masWardCategory.setLastUpdateDate(LocalDate.now());
        masWardCategory.setLastUpdatedBY(currentUser.getFirstName()+" "+currentUser.getLastName());

        MasWardCategory mas= masWardCategoryRepository.save(masWardCategory);
        return ResponseUtils.createSuccessResponse(  mapToConverted(mas),new TypeReference<>(){});
    }

    @Override
    public ApiResponse<MasWardCategoryResponse> update(Long id, MasWardCategoryRequest request) {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }
        Optional<MasWardCategory> masWardCategory=masWardCategoryRepository.findById(id);
        if(masWardCategory.isEmpty()){
            return ResponseUtils.createNotFoundResponse("MasWardCategory data not found",  404);
        }
        MasWardCategory masWardCategory1=masWardCategory.get();
        masWardCategory1.setCategoryName(request.getCategoryName());
        masWardCategory1.setDescription(request.getDescription());
        masWardCategory1.setMasCareLevel(masCareLevelRepo.findById(request.getCareId()).orElseThrow(()-> new RuntimeException("Invalid care level id")));
        masWardCategory1.setStatus("y");
        masWardCategory1.setLastUpdateDate(LocalDate.now());
        masWardCategory1.setLastUpdatedBY(currentUser.getFirstName()+" "+currentUser.getLastName());

        MasWardCategory mas= masWardCategoryRepository.save(masWardCategory1);
        return ResponseUtils.createSuccessResponse(  mapToConverted(mas),new TypeReference<>(){});

    }

    @Override
    public ApiResponse<MasWardCategoryResponse> changeMasWardStatus(Long id, String status) {

        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }
        Optional<MasWardCategory> masWardCategory=masWardCategoryRepository.findById(id);
        if(masWardCategory.isEmpty()){
            return ResponseUtils.createNotFoundResponse("MasWardCategory data not found",  404);
        }
        if("n".equals(status)||"y".equals(status)){
            MasWardCategory masWardCategory1=masWardCategory.get();
            masWardCategory1.setStatus(status);
            masWardCategory1.setLastUpdateDate(LocalDate.now());
            masWardCategory1.setLastUpdatedBY(currentUser.getFirstName()+" "+currentUser.getLastName());
            MasWardCategory mas= masWardCategoryRepository.save(masWardCategory1);
            log.info("");
            return ResponseUtils.createSuccessResponse(  mapToConverted(mas),new TypeReference<>(){});

        }else{

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "status y or n only", 400);
        }
    }
    private MasWardCategoryResponse mapToConverted(MasWardCategory  masWardCategory){
        MasWardCategoryResponse response=new MasWardCategoryResponse();
        response.setCategoryId(masWardCategory.getId());
        response.setCategoryName(masWardCategory.getCategoryName());
        response.setDescription(masWardCategory.getDescription());
        response.setCreatedBy(masWardCategory.getCreatedBy());
        response.setLastUpdatedBy(masWardCategory.getLastUpdatedBY());
        response.setStatus(masWardCategory.getStatus());
        response.setLastUpdateDate(masWardCategory.getLastUpdateDate());
        response.setCareId(masWardCategory.getMasCareLevel().getCareId());
        response.setCareLevelName(masWardCategory.getMasCareLevel().getCareLevelName());
        return response;
    }
}
