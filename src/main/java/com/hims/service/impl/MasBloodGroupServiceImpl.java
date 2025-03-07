package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasBloodGroup;
import com.hims.entity.repository.MasBloodGroupRepository;
import com.hims.request.MasBloodGroupRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodGroupResponse;
import com.hims.service.MasBloodGroupService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasBloodGroupServiceImpl implements MasBloodGroupService {

    @Autowired
    private MasBloodGroupRepository masBloodGroupRepository;

    private String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    public ApiResponse<List<MasBloodGroupResponse>> getAllBloodGroups() {
        List<MasBloodGroup> bloodGroups = masBloodGroupRepository.findAll();

        List<MasBloodGroupResponse> responses = bloodGroups.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    @Override
    @Transactional
    public ApiResponse<MasBloodGroupResponse> addBloodGroup(MasBloodGroupRequest bloodGroupRequest) {
        MasBloodGroup bloodGroup = new MasBloodGroup();
        bloodGroup.setBloodGroupCode(bloodGroupRequest.getBloodGroupCode());
        bloodGroup.setBloodGroupName(bloodGroupRequest.getBloodGroupName());
        bloodGroup.setStatus(bloodGroupRequest.getStatus()); // Default status can be set here if needed
        bloodGroup.setLastChangedBy(bloodGroupRequest.getLastChangedBy());
        bloodGroup.setLastChangedDate(Instant.now());
        bloodGroup.setLastChangedTime(getCurrentTimeFormatted());
        bloodGroup.setHicCode(bloodGroupRequest.getHicCode());

        MasBloodGroup savedBloodGroup = masBloodGroupRepository.save(bloodGroup);
        return ResponseUtils.createSuccessResponse(convertToResponse(savedBloodGroup), new TypeReference<>() {});
    }

    @Override
    @Transactional
    public ApiResponse<MasBloodGroupResponse> updateBloodGroup(Long id, MasBloodGroupResponse bloodGroupDetails) {
        Optional<MasBloodGroup> existingBloodGroupOpt = masBloodGroupRepository.findById(id);
        if (existingBloodGroupOpt.isPresent()) {
            MasBloodGroup existingBloodGroup = existingBloodGroupOpt.get();
            existingBloodGroup.setBloodGroupCode(bloodGroupDetails.getBloodGroupCode());
            existingBloodGroup.setBloodGroupName(bloodGroupDetails.getBloodGroupName());
            existingBloodGroup.setStatus(bloodGroupDetails.getStatus());
            existingBloodGroup.setLastChangedBy(bloodGroupDetails.getLastChangedBy());
            existingBloodGroup.setLastChangedDate(Instant.now());
            existingBloodGroup.setLastChangedTime(getCurrentTimeFormatted());
            existingBloodGroup.setHicCode(bloodGroupDetails.getHicCode());

            MasBloodGroup updatedBloodGroup = masBloodGroupRepository.save(existingBloodGroup);
            return ResponseUtils.createSuccessResponse(convertToResponse(updatedBloodGroup), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasBloodGroupResponse>() {}, "Blood Group not found", 404);
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasBloodGroupResponse> changeStatus(Long id, String status) {
        Optional<MasBloodGroup> existingBloodGroupOpt = masBloodGroupRepository.findById(id);
        if (existingBloodGroupOpt.isPresent()) {
            MasBloodGroup existingBloodGroup = existingBloodGroupOpt.get();

            // Validate status value
            if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasBloodGroupResponse>() {}, "Invalid status value. Use 'Y' for Active and 'N' for Inactive.", 400);
            }

            existingBloodGroup.setStatus(status); // Set status as "Y" or "N"
            MasBloodGroup updatedBloodGroup = masBloodGroupRepository.save(existingBloodGroup);

            return ResponseUtils.createSuccessResponse(convertToResponse(updatedBloodGroup), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasBloodGroupResponse>() {}, "Blood Group not found", 404);
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasBloodGroupResponse> findById(Long id) {
        Optional<MasBloodGroup> existingBloodGroupOpt = masBloodGroupRepository.findById(id);
        if (existingBloodGroupOpt.isPresent()) {
            return ResponseUtils.createSuccessResponse(convertToResponse(existingBloodGroupOpt.get()), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasBloodGroupResponse>() {}, "Blood Group not found", 404);
        }
    }

    private MasBloodGroupResponse convertToResponse(MasBloodGroup bloodGroup) {
        MasBloodGroupResponse response = new MasBloodGroupResponse();
        response.setBloodGroupId(bloodGroup.getBloodGroupId());
        response.setBloodGroupCode(bloodGroup.getBloodGroupCode());
        response.setBloodGroupName(bloodGroup.getBloodGroupName());
        response.setStatus(bloodGroup.getStatus());
        response.setLastChangedBy(bloodGroup.getLastChangedBy());
        response.setLastChangedDate(bloodGroup.getLastChangedDate());
        response.setLastChangedTime(bloodGroup.getLastChangedTime());
        response.setHicCode(bloodGroup.getHicCode());
        return response;
    }
}