package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasReligion;
import com.hims.entity.repository.MasReligionRepository;
import com.hims.request.MasReligionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasReligionResponse;
import com.hims.service.MasReligionService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasReligionServiceImpl implements MasReligionService {

    @Autowired
    private MasReligionRepository masReligionRepository;

    @Override
    public ApiResponse<List<MasReligionResponse>> getAllReligions(int flag) {
        List<MasReligion> religions;

        if (flag == 1) {
            religions = masReligionRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            religions = masReligionRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasReligionResponse> responses = religions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }


    @Override
    @Transactional
    public ApiResponse<MasReligionResponse> addReligion(MasReligionRequest religionRequest) {
        MasReligion religion = new MasReligion();
        religion.setName(religionRequest.getName());
        religion.setStatus(religionRequest.getStatus()); // Default status can be set here if needed
        religion.setLastChgBy(religionRequest.getLastChgBy());
        religion.setLastChgDate(Instant.now());

        MasReligion savedReligion = masReligionRepository.save(religion);
        return ResponseUtils.createSuccessResponse(convertToResponse(savedReligion), new TypeReference<>() {});
    }

    @Override
    @Transactional
    public ApiResponse<MasReligionResponse> updateReligion(Long id, MasReligionResponse religionDetails) {
        Optional<MasReligion> existingReligionOpt = masReligionRepository.findById(id);
        if (existingReligionOpt.isPresent()) {
            MasReligion existingReligion = existingReligionOpt.get();
            existingReligion.setName(religionDetails.getName());
            existingReligion.setStatus(religionDetails.getStatus());
            existingReligion.setLastChgBy(religionDetails.getLastChgBy());
            existingReligion.setLastChgDate(Instant.now());

            MasReligion updatedReligion = masReligionRepository.save(existingReligion);
            return ResponseUtils.createSuccessResponse(convertToResponse(updatedReligion), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasReligionResponse>() {}, "Religion not found", 404);
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasReligionResponse> changeStatus(Long id, String status) {
        Optional<MasReligion> existingReligionOpt = masReligionRepository.findById(id);
        if (existingReligionOpt.isPresent()) {
            MasReligion existingReligion = existingReligionOpt.get();

            // Validate status value
            if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasReligionResponse>() {}, "Invalid status value. Use 'Y' for Active and 'N' for Inactive.", 400);
            }

            existingReligion.setStatus(status); // Set status as "Y" or "N"
            MasReligion updatedReligion = masReligionRepository.save(existingReligion);

            return ResponseUtils.createSuccessResponse(convertToResponse(updatedReligion), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasReligionResponse>() {}, "Religion not found", 404);
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasReligionResponse> findById(Long id) {
        Optional<MasReligion> existingReligionOpt = masReligionRepository.findById(id);
        if (existingReligionOpt.isPresent()) {
            return ResponseUtils.createSuccessResponse(convertToResponse(existingReligionOpt.get()), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasReligionResponse>() {}, "Religion not found", 404);
        }
    }

    private MasReligionResponse convertToResponse(MasReligion religion) {
        MasReligionResponse response = new MasReligionResponse();
        response.setId(religion.getId());
        response.setName(religion.getName());
        response.setStatus(religion.getStatus());
        response.setLastChgBy(religion.getLastChgBy());
        response.setLastChgDate(religion.getLastChgDate());
        return response;
    }
}