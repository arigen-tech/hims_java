package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasStoreGroup;
import com.hims.entity.repository.MasStoreGroupRepository;
import com.hims.request.MasStoreGroupRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStoreGroupResponse;
import com.hims.service.MasStoreGroupService;
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
public class MasStoreGroupServiceImp implements MasStoreGroupService {
    @Autowired
    private MasStoreGroupRepository masStoreGroupRepository;
    private String getCurrentTimeFormatted() {

        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    @Override
    public ApiResponse<MasStoreGroupResponse> addMasStoreGroup(MasStoreGroupRequest masStoreGroupRequest) {
        MasStoreGroup masStoreGroup=new MasStoreGroup();
        if("Y".equalsIgnoreCase(masStoreGroupRequest.getStatus())||"N".equalsIgnoreCase(masStoreGroupRequest.getStatus())){
            masStoreGroup.setGroupCode(masStoreGroupRequest.getGroupCode());
            masStoreGroup.setGroupName(masStoreGroupRequest.getGroupName());
            masStoreGroup.setLastChgBy(masStoreGroupRequest.getLastChgBy());
            masStoreGroup.setLastChgTime(getCurrentTimeFormatted());
            masStoreGroup.setLastChgDate(Instant.now());
            masStoreGroup.setStatus(masStoreGroupRequest.getStatus());
            MasStoreGroup newMasStoreGroup=masStoreGroupRepository.save(masStoreGroup);
            return ResponseUtils.createSuccessResponse(convertedToResponse(newMasStoreGroup), new TypeReference<>() {
            });
        }else{

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid status. Status should be 'Y' or 'N'", 400);
        }
    }

    @Override
    public ApiResponse<MasStoreGroupResponse> updateMasStoreGroup(int id, MasStoreGroup masStoreGroup) {
        Optional<MasStoreGroup> oldMasStore=masStoreGroupRepository.findById(id);
        if(oldMasStore.isPresent()){
            MasStoreGroup newMasStoreGroup =oldMasStore.get();
            newMasStoreGroup.setGroupCode(masStoreGroup.getGroupCode());
            newMasStoreGroup.setGroupName(masStoreGroup.getGroupName());
            newMasStoreGroup.setLastChgBy(masStoreGroup.getLastChgBy());
            newMasStoreGroup.setLastChgTime(getCurrentTimeFormatted());
            newMasStoreGroup.setLastChgDate(Instant.now());
            newMasStoreGroup.setStatus(masStoreGroup.getStatus());
            return ResponseUtils.createSuccessResponse(convertedToResponse(masStoreGroupRepository.save(newMasStoreGroup)), new TypeReference<>() {
            });
        }else{
            return ResponseUtils.createNotFoundResponse("MasStoreGroup not found", 404);
        }
    }

    @Override
    public ApiResponse<MasStoreGroupResponse> updateStatusMasStoreGroup(int id, String status) {
        Optional<MasStoreGroup> masStoreGroup=masStoreGroupRepository.findById(id);
        if(masStoreGroup.isPresent()) {
            MasStoreGroup newMasStore = masStoreGroup.get();
            if ("Y".equalsIgnoreCase(status) || "n".equalsIgnoreCase(status)) {

                newMasStore.setStatus(status);
                return ResponseUtils.createSuccessResponse(convertedToResponse(masStoreGroupRepository.save(newMasStore)), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid status. Status should be 'Y' or 'N'", 400);
            }
        }else{
            return ResponseUtils.createNotFoundResponse("MasStoreGroup not found", 404);
        }
    }

    @Override
    public ApiResponse<MasStoreGroupResponse> getMasStoreGroup(int id) {
        Optional<MasStoreGroup> masStoreGroup=masStoreGroupRepository.findById(id);
        if(masStoreGroup.isPresent()){
            MasStoreGroup newMasStoreGroup=masStoreGroup.get();

            return ResponseUtils.createSuccessResponse(convertedToResponse(newMasStoreGroup), new TypeReference<>() {
            });
        }
        else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasStoreGroupResponse>() {}, "MasStoreGroup not found", 404);
        }



        }



    @Override
    public ApiResponse<List<MasStoreGroupResponse>> getMasStoreGroupAllId(int flag) {
        List<MasStoreGroup> masStoreGroups;
        if (flag == 1) {
            masStoreGroups = masStoreGroupRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            masStoreGroups = masStoreGroupRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid flag value. Use 0 or 1.", 400);
        }
        List<MasStoreGroupResponse> responses = masStoreGroups.stream()
                .map(this::convertedToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }


    private MasStoreGroupResponse convertedToResponse(MasStoreGroup masStoreGroup){
        MasStoreGroupResponse masStoreGroupResponse=new MasStoreGroupResponse();
        masStoreGroupResponse.setId(masStoreGroup.getId());
        masStoreGroupResponse.setGroupCode(masStoreGroup.getGroupCode());
        masStoreGroupResponse.setGroupName(masStoreGroup.getGroupName());
        masStoreGroupResponse.setLastChgBy(masStoreGroup.getLastChgBy());
        masStoreGroupResponse.setLastChgTime(masStoreGroup.getLastChgTime());
        masStoreGroupResponse.setLastChgDate(masStoreGroup.getLastChgDate());
        masStoreGroupResponse.setStatus(masStoreGroup.getStatus());
        return masStoreGroupResponse;
    }
}
