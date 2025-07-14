package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasItemType;
import com.hims.entity.MasStoreGroup;
import com.hims.entity.repository.MasItemTypeRepository;
import com.hims.entity.repository.MasStoreGroupRepository;
import com.hims.request.MasItemTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasItemTypeResponse;
import com.hims.service.MasItemTypeService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasItemTypeServiceImp implements MasItemTypeService

{
    @Autowired
    private MasItemTypeRepository masItemTypeRepository;
    @Autowired
    private MasStoreGroupRepository masStoreGroupRepository;
    private String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    public ApiResponse<MasItemTypeResponse> addMasItemType(MasItemTypeRequest masItemTypeRequest) {
//        MasStoreGroup masStoreGroupObj = masStoreGroupRepository.findById(masItemTypeRequest.getMasStoreGroupId())
//                .orElseThrow(() -> new IllegalArgumentException("MasStoreGroup not found with ID: " + masItemTypeRequest.getMasStoreGroupId()));


        Optional<MasStoreGroup> masStoreGroup = masStoreGroupRepository.findById(masItemTypeRequest.getMasStoreGroupId());
        if (masStoreGroup.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("MasStoreGroup not found with id", 404);

        }else{
             MasItemType masItemType = new MasItemType();
            if ("Y".equalsIgnoreCase(masItemTypeRequest.getStatus()) || "N".equalsIgnoreCase(masItemTypeRequest.getStatus())) {
            masItemType.setCode(masItemTypeRequest.getCode());
            masItemType.setName(masItemTypeRequest.getName());
           // masItemType.setLastChgBy();
            masItemType.setLastChgDate(Instant.now());
            masItemType.setLastChgTime(getCurrentTimeFormatted());
            masItemType.setStatus(masItemTypeRequest.getStatus());
            masItemType.setMasStoreGroupId(masStoreGroup.get());
            return ResponseUtils.createSuccessResponse(convertedTOResponse(masItemTypeRepository.save(masItemType)), new TypeReference<>() {});

        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid status. Status should be 'Y' or 'N'", 400);
          }
        }
    }

    @Override
    public ApiResponse<MasItemTypeResponse> updateMasItemTypeID(int id, MasItemTypeRequest masItemTypeRequest) {
        Optional<MasItemType> oldMasItemType = masItemTypeRepository.findById(id);
        if (oldMasItemType.isPresent()) {
            MasItemType newMasItemType = oldMasItemType.get();
            newMasItemType.setCode(masItemTypeRequest.getCode());
            newMasItemType.setName(masItemTypeRequest.getName());
            // newMasItemType.setLastChgBy();
            newMasItemType.setLastChgDate(Instant.now());
            newMasItemType.setLastChgTime(getCurrentTimeFormatted());
            newMasItemType.setStatus(masItemTypeRequest.getStatus());

            Optional<MasStoreGroup> masStoreGroup = masStoreGroupRepository.findById(masItemTypeRequest.getMasStoreGroupId());
            if (masStoreGroup.isPresent()) {
                newMasItemType.setMasStoreGroupId(masStoreGroup.get());

            } else {
                return ResponseUtils.createNotFoundResponse("MasStoreGroup not found with id: " + masItemTypeRequest.getMasStoreGroupId(), 404);
            }


            return ResponseUtils.createSuccessResponse(convertedTOResponse(masItemTypeRepository.save(newMasItemType)), new TypeReference<>() {
            });


        }
            else{
                return ResponseUtils.createNotFoundResponse("MasItemType is not found", 404);
            }



    }

    @Override
    public ApiResponse<MasItemTypeResponse> updateMasItemTypeStatus(int id, String status) {
        Optional<MasItemType> oldMasItemType = masItemTypeRepository.findById(id);
        if(oldMasItemType.isPresent()){
            MasItemType newMasItemType = oldMasItemType.get();
             if ("Y".equalsIgnoreCase(status)|| "N".equalsIgnoreCase(status)){
                 newMasItemType.setStatus(status);
                 return ResponseUtils.createSuccessResponse(convertedTOResponse(masItemTypeRepository.save( newMasItemType)), new TypeReference<>() {});

             }else{
                 return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                 }, "Invalid status. Status should be 'Y' or 'N'", 400);
             }
        }else{
            return ResponseUtils.createNotFoundResponse("MasItemType is not found", 404);
        }
    }

    @Override
    public ApiResponse<MasItemTypeResponse> getByMasItemTypeStatus(int id) {
        Optional<MasItemType> masItemType = masItemTypeRepository.findById(id);
        if(masItemType.isPresent()){
            MasItemType newMasItemType= masItemType.get();
            return ResponseUtils.createSuccessResponse(convertedTOResponse(newMasItemType), new TypeReference<>() {});
        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "MasItemType is not found", 404);
        }
    }

    @Override
    public ApiResponse<List<MasItemTypeResponse>> getAllMasItemTypeStatus(int flag) {
        List<MasItemType> masItemTypes;
        if (flag == 1) {
            masItemTypes= masItemTypeRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            masItemTypes = masItemTypeRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasItemTypeResponse> responses = masItemTypes.stream()
                .map(this::convertedTOResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});

    }

    @Override
    public ApiResponse<List<MasItemTypeResponse>> findItemType(Long id) {
        List<MasItemType> masItemTypes=masItemTypeRepository.findByMasStoreGroupIdId(id);

        List<MasItemTypeResponse> responses = masItemTypes.stream()
                .map(this::convertedTOResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }


    private MasItemTypeResponse convertedTOResponse(MasItemType masItemType){
            MasItemTypeResponse masItemTypeResponse=new MasItemTypeResponse();
        masItemTypeResponse.setId(masItemType.getId());
        masItemTypeResponse.setCode(masItemType.getCode());
        masItemTypeResponse.setName(masItemType.getName());
        masItemTypeResponse.setLastChgBy(masItemType.getLastChgBy());
        masItemTypeResponse.setLastChgDate(masItemType.getLastChgDate());
        masItemTypeResponse.setLastChgTime(masItemType.getLastChgTime());
        masItemTypeResponse.setStatus(masItemType.getStatus());
        masItemTypeResponse.setMasStoreGroupId(masItemType.getMasStoreGroupId().getId());

            return masItemTypeResponse;
        }

    }

