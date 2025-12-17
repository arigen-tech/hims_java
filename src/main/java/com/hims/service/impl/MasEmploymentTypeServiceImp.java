package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasEmploymentType;
import com.hims.entity.repository.MasEmploymentTypeRepository;
import com.hims.request.MasEmploymentTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasEmploymentTypeResponse;
import com.hims.service.MasEmploymentTypeService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasEmploymentTypeServiceImp implements MasEmploymentTypeService {
    @Autowired
    private MasEmploymentTypeRepository masEmploymentTypeRepository;

    @Override
    public ApiResponse<List<MasEmploymentTypeResponse>> getAllMasEmploymentType(int flag) {
        List<MasEmploymentType> masEmploymentTypes;
        if (flag == 1) {
            masEmploymentTypes =  masEmploymentTypeRepository.findByStatusIgnoreCaseOrderByEmploymentTypeAsc("Y");
        } else if (flag == 0) {
            masEmploymentTypes = masEmploymentTypeRepository.findAllByOrderByStatusDescLastChangedDateDesc();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid flag value. Use 0 or 1.", 400);
        }
        List<MasEmploymentTypeResponse> responses = masEmploymentTypes.stream()
                .map(this::convertedToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }


    @Override
    public ApiResponse<MasEmploymentTypeResponse> addMasEmploymentType(MasEmploymentTypeRequest masEmploymentTypeRequest) {
        MasEmploymentType masEmploymentType=new MasEmploymentType();
        if("y".equalsIgnoreCase(masEmploymentTypeRequest.getStatus())||"n".equalsIgnoreCase(masEmploymentTypeRequest.getStatus())){
            masEmploymentType.setEmploymentType(masEmploymentTypeRequest.getEmploymentType());
            masEmploymentType.setStatus(masEmploymentTypeRequest.getStatus());
            masEmploymentType.setLastChangedBy(masEmploymentTypeRequest.getLastChangedBy());
            masEmploymentType.setLastChangedDate(LocalDateTime.now());

            return ResponseUtils.createSuccessResponse(convertedToResponse(masEmploymentTypeRepository.save(masEmploymentType)), new TypeReference<>() {});
        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid status value. Use y or n.", 400);

        }


    }

    @Override
    public ApiResponse<MasEmploymentTypeResponse> getMasEmploymentTypeId(Long id) {
        Optional<MasEmploymentType> masEmploymentType=masEmploymentTypeRepository.findById(id);
        if(masEmploymentType.isPresent()){
            MasEmploymentType newMasEmploymentType=masEmploymentType.get();
            return ResponseUtils.createSuccessResponse(convertedToResponse(newMasEmploymentType), new TypeReference<>() {});
        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasEmploymentTypeResponse>() {}, "MasEmploymentType not found", 404);
        }
    }

    @Override
    public ApiResponse<MasEmploymentTypeResponse> updateMasEmploymentTypeById(MasEmploymentType masEmploymentType, Long id) {
        Optional<MasEmploymentType> oldMasEmploymentType=masEmploymentTypeRepository.findById(id);
        if(oldMasEmploymentType.isPresent()){
            MasEmploymentType newMasEmploymentType=oldMasEmploymentType.get();

            newMasEmploymentType.setEmploymentType(masEmploymentType.getEmploymentType());
            newMasEmploymentType.setStatus(masEmploymentType.getStatus());
            newMasEmploymentType.setLastChangedBy(masEmploymentType.getLastChangedBy());
            newMasEmploymentType.setLastChangedDate(LocalDateTime.now());
            return ResponseUtils.createSuccessResponse(convertedToResponse(newMasEmploymentType), new TypeReference<>() {});

        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasEmploymentTypeResponse>() {}, "MasEmploymentType not found", 404);
        }
    }

    @Override
    public ApiResponse<MasEmploymentTypeResponse> updateMasEmploymentTypeByStatus(Long id, String status) {
        Optional<MasEmploymentType> masEmploymentType=masEmploymentTypeRepository.findById(id);
        if(masEmploymentType.isPresent()){
            MasEmploymentType newMasEmploymentType=masEmploymentType.get();
            if("y".equalsIgnoreCase(status)||"n".equalsIgnoreCase(status)){
                newMasEmploymentType.setStatus(status);
                return ResponseUtils.createSuccessResponse(convertedToResponse(newMasEmploymentType), new TypeReference<>() {});
            }else{
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid status value. Use y or n.", 400);

            }

        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasEmploymentTypeResponse>() {}, "MasEmploymentType not found", 404);
        }
    }

    private MasEmploymentTypeResponse convertedToResponse( MasEmploymentType masEmploymentType){
        MasEmploymentTypeResponse response=new MasEmploymentTypeResponse();
        response.setId(masEmploymentType.getId());
        response.setEmploymentType(masEmploymentType.getEmploymentType());
        response.setStatus(masEmploymentType.getStatus());
        response.setLastChangedBy(masEmploymentType.getLastChangedBy());
        response.setLastChangedDate(masEmploymentType.getLastChangedDate());

        return response;

    }

    }

