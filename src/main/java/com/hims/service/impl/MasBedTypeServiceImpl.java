package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasBedType;
import com.hims.entity.MasCareLevel;
import com.hims.entity.MasWardCategory;
import com.hims.entity.User;
import com.hims.entity.repository.MasBedTypeRepository;
import com.hims.request.MasBedTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBedTypeResponse;
import com.hims.response.MasRoomCategoryResponse;
import com.hims.response.MasWardCategoryResponse;
import com.hims.service.MasBedTypeService;
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
public class MasBedTypeServiceImpl implements MasBedTypeService {
    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private MasBedTypeRepository masBedTypeRepository;
    @Override
    public ApiResponse<?> masBedTypeCreate(MasBedTypeRequest request) {
        try {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found",400);
        }
        MasBedType masBedType=new MasBedType();
        masBedType.setBedTypeName(request.getBedTypeName());
        masBedType.setDescription(request.getDescription());
        masBedType.setStatus("y");
        masBedType.setCreatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
        masBedType.setLastUpdateDate(LocalDate.now());
        masBedType.setLastUpdatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
        MasBedType mas=masBedTypeRepository.save(masBedType);
        return ResponseUtils.createSuccessResponse(  mapToConverted(mas),new TypeReference<>(){});
        } catch (Exception e) {
            log.error("updateCareLevel() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<?> masBedTypeUpdate(Long id, MasBedTypeRequest request) {
        try {

            log.info("updateMasBed() method Started...");

            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.NOT_FOUND.value());
            }

            MasBedType masBedType= masBedTypeRepository.findById(id).orElseThrow(()-> new RuntimeException("Invalid MasBed Id"));
            masBedType.setBedTypeName(request.getBedTypeName());
            masBedType.setDescription(request.getDescription());
            masBedType.setLastUpdatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
            MasBedType save = masBedTypeRepository.save(masBedType);
            log.info("updateCareLevel() method Ended...");

            return  ResponseUtils.createSuccessResponse(mapToConverted(save), new TypeReference<>() {});


        } catch (Exception e) {
            log.error("updateMasBed() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasBedTypeResponse> changeActiveStatus(Long id, String status) {
        try {

            log.info("changeActiveStatus() method Started...");

            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.NOT_FOUND.value());
            }
            MasBedType masBedType= masBedTypeRepository.findById(id).orElseThrow(()-> new RuntimeException("Invalid MasBed Id"));
            masBedType.setStatus(status);
            masBedType.setLastUpdatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
            MasBedType save = masBedTypeRepository.save(masBedType);
            log.info("changeActiveStatus() method Ended...");

            return  ResponseUtils.createSuccessResponse(mapToConverted(save), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("changeActiveStatus() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<?> getById(Long id) {
        Optional<MasBedType> masBedType= masBedTypeRepository.findById(id);
        if(masBedType.isEmpty()){
            return ResponseUtils.createNotFoundResponse("MasBedType data not found",  404);
        }
        MasBedType masWardCategory1= masBedType.get();
        return ResponseUtils.createSuccessResponse(mapToConverted(masWardCategory1),new TypeReference<>(){});
    }

    @Override
    public ApiResponse<?> getAll(int flag) {
        try {
            log.info("Mas Bed Type get List Start");
            List<MasBedType> masBedTypes;
            if (flag == 1) {
                masBedTypes = masBedTypeRepository.findByStatusIgnoreCaseInOrderByLastUpdateDateDesc(List.of("y", "n"));
            } else if (flag == 0) {
                masBedTypes = masBedTypeRepository.findByStatusIgnoreCaseOrderByLastUpdateDateDesc("y");

            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid flag value. Use 0 or 1.", 400);
            }
            List<MasBedTypeResponse> responses = masBedTypes.stream()
                    .map(this::mapToConverted)
                    .collect(Collectors.toList());
            log.info("Mas Bed Type List succes");
            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
            });
        }catch(Exception e){
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
    private MasBedTypeResponse mapToConverted(MasBedType masBedType){
        MasBedTypeResponse masBedTypeResponse=new MasBedTypeResponse();
        masBedTypeResponse.setBedTypeId(masBedType.getBedTypeId());
        masBedTypeResponse.setBedTypeName(masBedType.getBedTypeName());
        masBedTypeResponse.setDescription(masBedType.getDescription());
        masBedTypeResponse.setStatus(masBedType.getStatus());
        masBedTypeResponse.setLastUpdateDate(masBedType.getLastUpdateDate());
        masBedTypeResponse.setCreatedBy(masBedType.getCreatedBy());
        masBedTypeResponse.setLastUpdatedBy(masBedType.getLastUpdatedBy());
        return masBedTypeResponse;
    }
}
