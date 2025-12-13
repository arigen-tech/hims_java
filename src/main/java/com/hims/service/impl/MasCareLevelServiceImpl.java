package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasCareLevel;
import com.hims.entity.User;
import com.hims.entity.repository.MasCareLevelRepo;
import com.hims.request.MasCareLevelRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasCareLevelResponse;
import com.hims.service.MasCareLevelService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasCareLevelServiceImpl implements MasCareLevelService {

    private final MasCareLevelRepo masCareLevelRepo;

    private final AuthUtil authUtil;



    @Override
    public ApiResponse<MasCareLevelResponse> createCareLevel(MasCareLevelRequest request) {

        try {

            log.info("createCareLevel() method Started...");

            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.NOT_FOUND.value());
            }
            MasCareLevel masCareLevel= new MasCareLevel();
            masCareLevel.setCareLevelName(request.getCareLevelName());
            masCareLevel.setDescription(request.getDescription());
            masCareLevel.setCreatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
            masCareLevel.setUpdatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
            masCareLevel.setStatus("y");
            MasCareLevel save = masCareLevelRepo.save(masCareLevel);
            log.info("createCareLevel() method Ended...");

            return  ResponseUtils.createSuccessResponse(mapToResponse(save), new TypeReference<>() {});


        } catch (Exception e) {
           log.error("createCareLevel() Error :: ",e);
           return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<MasCareLevelResponse> updateCareLevel(Long careId, MasCareLevelRequest request) {
        try {

            log.info("updateCareLevel() method Started...");

            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.NOT_FOUND.value());
            }

            MasCareLevel masCareLevel= masCareLevelRepo.findById(careId).orElseThrow(()-> new RuntimeException("Invalid Care Id"));
            masCareLevel.setCareLevelName(request.getCareLevelName());
            masCareLevel.setDescription(request.getDescription());
            masCareLevel.setUpdatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
            MasCareLevel save = masCareLevelRepo.save(masCareLevel);
            log.info("updateCareLevel() method Ended...");

            return  ResponseUtils.createSuccessResponse(mapToResponse(save), new TypeReference<>() {});


        } catch (Exception e) {
            log.error("updateCareLevel() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasCareLevelResponse> changeActiveStatus(Long careId, String status) {
       try {

           log.info("changeActiveStatus() method Started...");

           User currentUser = authUtil.getCurrentUser();
           if(currentUser==null){
               return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.NOT_FOUND.value());
           }
           MasCareLevel masCareLevel= masCareLevelRepo.findById(careId).orElseThrow(()-> new RuntimeException("Invalid Care Id"));
           masCareLevel.setStatus(status);
           masCareLevel.setUpdatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
           MasCareLevel save = masCareLevelRepo.save(masCareLevel);
           log.info("changeActiveStatus() method Ended...");

           return  ResponseUtils.createSuccessResponse(mapToResponse(save), new TypeReference<>() {});

       } catch (Exception e) {
           log.error("changeActiveStatus() Error :: ",e);
           return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
       }
    }

    @Override
    public ApiResponse<MasCareLevelResponse> getById(Long careId) {
        try {

            log.info("getById() method Started...");

            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.NOT_FOUND.value());
            }

            MasCareLevel masCareLevel= masCareLevelRepo.findById(careId).orElseThrow(()-> new RuntimeException("Invalid Care Id"));
            log.info("getById() method Started...");
            return  ResponseUtils.createSuccessResponse(mapToResponse(masCareLevel), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("getById() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<MasCareLevelResponse>> getAll(int flag) {

        try {

            log.info("getAll() method Started...");

            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.NOT_FOUND.value());
            }
            List<MasCareLevel> masCareLevels;
            if(flag==0){
                masCareLevels=masCareLevelRepo.findByStatusIgnoreCaseInOrderByLastUpdateDateDesc(List.of("y","n"));
            } else if (flag==1) {
                masCareLevels=masCareLevelRepo.findByStatusIgnoreCaseOrderByCareLevelNameAsc("y");
            }else{
                return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Invalid Flag Value , Provide flag as 0 or 1",HttpStatus.BAD_REQUEST.value());
            }
            return  ResponseUtils.createSuccessResponse(masCareLevels.stream().map(this::mapToResponse).toList(), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getAll() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private MasCareLevelResponse mapToResponse(MasCareLevel entity){
        MasCareLevelResponse masCareLevelResponse=new MasCareLevelResponse();
        masCareLevelResponse.setCareId(entity.getCareId());
        masCareLevelResponse.setCareLevelName(entity.getCareLevelName());
        masCareLevelResponse.setDescription(entity.getDescription());
        masCareLevelResponse.setLastUpdateDate(entity.getLastUpdateDate());
        masCareLevelResponse.setStatus(entity.getStatus());
        return  masCareLevelResponse;
    }

}
