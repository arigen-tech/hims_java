package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasCareLevel;
import com.hims.entity.MasWard;
import com.hims.entity.MasWardCategory;
import com.hims.entity.User;
import com.hims.entity.repository.MasCareLevelRepo;
import com.hims.entity.repository.MasWardCategoryRepository;
import com.hims.entity.repository.MasWardRepository;
import com.hims.request.MasWardRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasWardResponse;
import com.hims.service.MasWardService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MasWardServiceImpl implements MasWardService {
    @Autowired
    private  AuthUtil authUtil;
    @Autowired
    private MasWardRepository masWardRepository;
    @Autowired
    private MasWardCategoryRepository masWardCategoryRepository;
    @Autowired
    private MasCareLevelRepo masCareLevelRepo;
    @Override
    public ApiResponse<List<MasWardResponse>> getAllMasWardCategory(int flag) {
        try {

            List<MasWard> masWards;
            if(flag==0){
                masWards=masWardRepository.findByStatusIgnoreCaseInOrderByLastUpdateDateDesc(List.of("y","n"));
            } else if (flag==1) {
                masWards=masWardRepository.findByStatusIgnoreCaseOrderByWardNameAsc("y");
            }else{
                return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Invalid Flag Value , Provide flag as 0 or 1",HttpStatus.BAD_REQUEST.value());
            }
            return  ResponseUtils.createSuccessResponse(masWards.stream().map(this::mapToResponse).toList(), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getAll() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<MasWardResponse> findById(Long id) {
        try {

            log.info("getById() method Started...");
            Optional<MasWard> masWard= masWardRepository.findById(id);
            if(masWard.isEmpty()){
                return  ResponseUtils.createNotFoundResponse("Mas Word Not Found", HttpStatus.NOT_FOUND.value());
            }
            log.info("getById() method Started...");
            return  ResponseUtils.createSuccessResponse(mapToResponse(masWard.get()), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("getById() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<MasWardResponse> addMasWard(MasWardRequest request) {
        try {

            log.info("MasWard() method Started...");
            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }
            MasWard masWard=new MasWard();
            masWard.setWardName(request.getWardName());
            masWard.setStatus("y");
            masWard.setLastUpdatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
            masWard.setCreatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
            masWard.setLastUpdateDate(LocalDate.now());
            Optional<MasWardCategory> masWardCategory= masWardCategoryRepository.findById(request.getWardCategoryId());
            if(masWardCategory.isEmpty()){
                return  ResponseUtils.createNotFoundResponse("Mas Ward Category Not Found", HttpStatus.NOT_FOUND.value());
            }
            masWard.setWardCategory(masWardCategory.get());
            Optional<MasCareLevel> masCareLevel= masCareLevelRepo.findById(request.getCareLevelId());
            if(masCareLevel.isEmpty()){
                return  ResponseUtils.createNotFoundResponse("Mas Ward Category Not Found", HttpStatus.NOT_FOUND.value());
            }
            masWard.setCareLevel(masCareLevel.get());
           MasWard masWard1= masWardRepository.save(masWard);
           log.info("MasWard() method Ended...");
            return  ResponseUtils.createSuccessResponse(mapToResponse(masWard1), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("masWard() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<MasWardResponse> update(Long id, MasWardRequest request) {
        try {

            log.info("updateMasWard() method Started...");

            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.NOT_FOUND.value());
            }

            MasWard masWard= masWardRepository.findById(id).orElseThrow(()-> new RuntimeException("Invalid Ward Id"));
           masWard.setWardName(request.getWardName());
           masWard.setLastUpdatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
           masWard.setStatus("y");
            Optional<MasWardCategory> masWardCategory= masWardCategoryRepository.findById(request.getWardCategoryId());
            if(masWardCategory.isEmpty()){
                return  ResponseUtils.createNotFoundResponse("Mas Ward Category Not Found", HttpStatus.NOT_FOUND.value());
            }
            masWard.setWardCategory(masWardCategory.get());
            Optional<MasCareLevel> masCareLevel= masCareLevelRepo.findById(request.getCareLevelId());
            if(masCareLevel.isEmpty()){
                return  ResponseUtils.createNotFoundResponse("Mas Care Level Not Found", HttpStatus.NOT_FOUND.value());
            }
            masWard.setCareLevel(masCareLevel.get());
            MasWard masWard1=masWardRepository.save( masWard);
            log.info("updateMasWard() method Ended...");

            return  ResponseUtils.createSuccessResponse(mapToResponse(masWard1), new TypeReference<>() {});


        } catch (Exception e) {
            log.error("updateMasWard() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<MasWardResponse> changeMasWardStatus(Long id, String status) {
        try {
            log.info("MasWard() method Started...");
            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.NOT_FOUND.value());
            }
            Optional<MasWard> masWard=masWardRepository.findById(id);
if(masWard.isEmpty()){
    return  ResponseUtils.createNotFoundResponse("Mas Word Not Found", HttpStatus.NOT_FOUND.value());
}
            MasWard masWard1=masWard.get();
            masWard1.setStatus(status);
            masWard1.setLastUpdatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
            MasWard save = masWardRepository.save( masWard1);
            log.info("changeActiveStatus() method Ended...");
            return  ResponseUtils.createSuccessResponse(mapToResponse(save), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("changeActiveStatus() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }
    private MasWardResponse mapToResponse(MasWard masWard){
        MasWardResponse masWardResponse=new MasWardResponse();
        masWardResponse.setWardId(masWard.getWardId());
        masWardResponse.setWardName(masWard.getWardName());
        masWardResponse.setStatus(masWard.getStatus());
        masWardResponse.setLastUpdatedBy(masWard.getLastUpdatedBy());
        masWardResponse.setLastUpdateDate(masWard.getLastUpdateDate());
        masWardResponse.setCreatedBy(masWard.getCreatedBy());
        masWardResponse.setCareLevelId(masWard.getCareLevel()!=null?masWard.getCareLevel().getCareId():null);
        masWardResponse.setCareLevelName(masWard.getCareLevel()!=null?masWard.getCareLevel().getCareLevelName():null);
        masWardResponse.setWardCategoryId(masWard.getWardCategory()!=null?masWard.getWardCategory().getId():null);
        masWardResponse.setWardCategoryName(masWard.getWardCategory()!=null?masWard.getWardCategory().getCategoryName():null);
        return masWardResponse;

    }
}
