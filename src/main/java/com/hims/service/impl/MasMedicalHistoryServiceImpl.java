package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.MasMedicalHistoryRepository;
import com.hims.request.MasMedicalHistoryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasMedicalHistoryResponse;
import com.hims.response.MasWardResponse;
import com.hims.service.MasMedicalHistoryService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MasMedicalHistoryServiceImpl implements MasMedicalHistoryService {
    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private MasMedicalHistoryRepository masMedicalHistoryRepository;
    @Override
    public ApiResponse<List<MasMedicalHistoryResponse>> getAllMas(int flag) {
        try {
            List<MasMedicalHistory> masWards;
            if(flag==0){
                masWards= masMedicalHistoryRepository.findByStatusIgnoreCaseIn(List.of("y","n"));
            } else if (flag==1) {
                masWards= masMedicalHistoryRepository.findByStatusIgnoreCase("y");
            }else{
                return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Invalid Flag Value , Provide flag as 0 or 1", HttpStatus.BAD_REQUEST.value());
            }
            return  ResponseUtils.createSuccessResponse(masWards.stream().map(this::mapToResponse).toList(), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getAll() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasMedicalHistoryResponse> addMas(MasMedicalHistoryRequest request) {
        try {
            log.info("MasHistory() method Started...");
            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found", HttpStatus.NOT_FOUND.value());
            }
            MasMedicalHistory masWard=new MasMedicalHistory();
            masWard.setMedicalHistoryName(request.getMedicalHistoryName());
            masWard.setStatus("y");
            masWard.setLastUpdatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
            masWard.setCreatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
            masWard.setLastUpdateDate(LocalDateTime.now());
            MasMedicalHistory  masWard1=  masMedicalHistoryRepository.save(masWard);
            log.info("MasHistory() method Ended...");
            return  ResponseUtils.createSuccessResponse(mapToResponse(masWard1), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("MasHistory() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<MasMedicalHistoryResponse> update(Long id, MasMedicalHistoryRequest request) {
        try {
            log.info("updateMasHistory() method Started...");
            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.NOT_FOUND.value());
            }
            MasMedicalHistory masWard=  masMedicalHistoryRepository.findById(id).orElseThrow(()-> new RuntimeException("Invalid medical Id"));
            masWard.setMedicalHistoryName(request.getMedicalHistoryName());
            masWard.setLastUpdatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
            masWard.setLastUpdateDate(LocalDateTime.now());
            masWard.setStatus("y");
            MasMedicalHistory masWard1= masMedicalHistoryRepository.save( masWard);
            log.info("updateMasHistory() method Ended...");
            return  ResponseUtils.createSuccessResponse(mapToResponse(masWard1), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("updateMasHistory() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasMedicalHistoryResponse> changeMasStatus(Long id, String status) {
        try {
            log.info("MasHistory() method Started...");
            User currentUser = authUtil.getCurrentUser();
            if(currentUser==null){
                return  ResponseUtils.createNotFoundResponse("Current User Not Found",HttpStatus.NOT_FOUND.value());
            }
            Optional<MasMedicalHistory> masWard=masMedicalHistoryRepository.findById(id);
            if(masWard.isEmpty()){
                return  ResponseUtils.createNotFoundResponse("MasMedicalHistory id Not Found", HttpStatus.NOT_FOUND.value());
            }
            MasMedicalHistory masWard1=masWard.get();
            masWard1.setStatus(status);
            masWard1.setLastUpdateDate(LocalDateTime.now());
            masWard1.setLastUpdatedBy(currentUser.getFirstName()+" "+currentUser.getLastName());
            MasMedicalHistory save = masMedicalHistoryRepository.save( masWard1);
            log.info("changeActiveStatus() method Ended...");
            return  ResponseUtils.createSuccessResponse(mapToResponse(save), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("changeActiveStatus() Error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
    private MasMedicalHistoryResponse mapToResponse(MasMedicalHistory masWard){
        MasMedicalHistoryResponse masWardResponse=new MasMedicalHistoryResponse();
        masWardResponse.setMedicalHistoryId( masWard.getMedicalHistoryId());
        masWardResponse.setMedicalHistoryName( masWard.getMedicalHistoryName());
        masWardResponse.setStatus(masWard.getStatus());
        masWardResponse.setLastUpdatedBy(masWard.getLastUpdatedBy());
        masWardResponse.setLastUpdateDate(masWard.getLastUpdateDate());
        masWardResponse.setCreatedBy(masWard.getCreatedBy());
        return masWardResponse;

    }
}
