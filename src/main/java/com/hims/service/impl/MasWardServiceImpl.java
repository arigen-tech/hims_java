package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.MasCareLevelRepo;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasWardCategoryRepository;
import com.hims.entity.repository.MasWardRepository;
import com.hims.request.MasWardRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DepartmentByDepartmentTypeCode;
import com.hims.response.MasWardResponse;
import com.hims.service.MasWardService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.hims.constants.AppConstants.*;

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

    @Autowired
    private MasDepartmentRepository masDepartmentRepository;


    @Value("${department.type.code.ward}")
    private String wardDepartmentTypeCode;
    @Override
    public ApiResponse<List<MasWardResponse>> getAllMasWardCategory(int flag) {
        try {

            List<MasWard> masWards;
            if(flag==FLAG_ALL){
                masWards=masWardRepository.findAllByOrderByStatusDescLastUpdateDateDesc();
            } else if (flag==FLAG_ACTIVE_ONLY) {
                masWards=masWardRepository.findByStatusIgnoreCaseOrderByWardNameAsc(STATUS_ACTIVE);
            }else{
                return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},MSG_INVALID_FLAG,HttpStatus.BAD_REQUEST.value());
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
            masWard.setStatus(STATUS_ACTIVE);
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
                return  ResponseUtils.createNotFoundResponse("Mas care level Not Found", HttpStatus.NOT_FOUND.value());
            }
            masWard.setCareLevel(masCareLevel.get());
            Optional<MasDepartment> masDepartment= masDepartmentRepository.findById(request.getDepartmentId());
            if(masDepartment.isEmpty()){
                return  ResponseUtils.createNotFoundResponse("Department Not Found", HttpStatus.NOT_FOUND.value());
            }
            masWard.setDepartment(masDepartment.get());
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
           masWard.setStatus(STATUS_ACTIVE);
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
            Optional<MasDepartment> masDepartment= masDepartmentRepository.findById(request.getDepartmentId());
            if(masDepartment.isEmpty()){
                return  ResponseUtils.createNotFoundResponse("Department Not Found", HttpStatus.NOT_FOUND.value());
            }
            masWard.setDepartment(masDepartment.get());
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

    @Override
    public ApiResponse<List<MasWardResponse>> getWardByCategory(Long wardCategoryId) {
        try {
            log.info("getWardByCategory() method Started with wardCategoryId: {}", wardCategoryId);

            if(wardCategoryId == null || wardCategoryId <= 0) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Ward Category ID must not be null or zero", HttpStatus.BAD_REQUEST.value());
            }

            List<MasWard> masWards = masWardRepository.findByWardCategory_Id(wardCategoryId);

            if(masWards.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("No wards found for this category", HttpStatus.NOT_FOUND.value());
            }

            log.info("getWardByCategory() method Ended. Found {} wards", masWards.size());
            return ResponseUtils.createSuccessResponse(masWards.stream().map(this::mapToResponse).toList(), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getWardByCategory() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<MasWardResponse>> getWardByCategoryAndStatus(Long wardCategoryId, String status) {
        try {
            log.info("getWardByCategoryAndStatus() method Started with wardCategoryId: {}, status: {}", wardCategoryId, status);

            if(wardCategoryId == null || wardCategoryId <= 0) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Ward Category ID must not be null or zero", HttpStatus.BAD_REQUEST.value());
            }

            if(status == null || status.trim().isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Status must not be null or empty", HttpStatus.BAD_REQUEST.value());
            }

            List<MasWard> masWards = masWardRepository.findByWardCategory_IdAndStatus(wardCategoryId, status);

            if(masWards.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("No wards found for this category and status", HttpStatus.NOT_FOUND.value());
            }

            log.info("getWardByCategoryAndStatus() method Ended. Found {} wards", masWards.size());
            return ResponseUtils.createSuccessResponse(masWards.stream().map(this::mapToResponse).toList(), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getWardByCategoryAndStatus() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
    @Override
    public ApiResponse<List<DepartmentByDepartmentTypeCode>> getDepartmentListByDepartmentTypeCode() {
        try {
            List<DepartmentByDepartmentTypeCode> departmentList =
                    masDepartmentRepository.findDepartmentsByDepartmentTypeCode(wardDepartmentTypeCode, AppConstants.STATUS_Y.toLowerCase());

            return ResponseUtils.createSuccessResponse(departmentList, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while fetching department list by department type code: {}", wardDepartmentTypeCode, e);
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
        masWardResponse.setDepartmentId(masWard.getDepartment()!=null?masWard.getDepartment().getId():null);
        masWardResponse.setDepartmentName(masWard.getDepartment()!=null?masWard.getDepartment().getDepartmentName():null);
        return masWardResponse;

    }
}
