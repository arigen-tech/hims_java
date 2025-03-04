package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasGender;
import com.hims.entity.repository.MasGenderRepository;
import com.hims.helperUtil.HelperUtils;
import com.hims.request.MasGenderRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasGenderResponse;
import com.hims.service.MasGenderService;
import com.hims.utils.ResponseUtils;
import lombok.NoArgsConstructor;
import lombok.experimental.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
public class MasGenderServiceImpl implements MasGenderService {
    @Autowired
    private MasGenderRepository masGenderRepository;

    public ApiResponse<List<MasGenderResponse>> getAllGenders() {
        List<MasGender> genders = masGenderRepository.findAll();

        List<MasGenderResponse> responses = genders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    private MasGenderResponse convertToResponse(MasGender gender) {
        MasGenderResponse response = new MasGenderResponse();
        response.setId(gender.getId());
        response.setGenderCode(gender.getGenderCode());
        response.setGenderName(gender.getGenderName());
        response.setCode(gender.getCode());
        response.setStatus(gender.getStatus());
        return response;
    }


    @Override
    @Transactional
    public ApiResponse<MasGenderResponse> addGender(MasGenderRequest genderRequest) {
        MasGender gender = new MasGender();
        gender.setGenderCode(genderRequest.getGenderCode());
        gender.setGenderName(genderRequest.getGenderName());
        gender.setCode(genderRequest.getCode());
//        gender.setLastChgDt(Instant.ofEpochSecond(System.currentTimeMillis()));
        gender.setLastChgDt(Instant.now());
        gender.setStatus(genderRequest.getStatus());

        MasGender savedGender = masGenderRepository.save(gender);
        return ResponseUtils.createSuccessResponse(convertToResponse(savedGender), new TypeReference<>() {});
    }

    @Override
    @Transactional
    public ApiResponse<MasGenderResponse> updateGender(Long id, MasGenderResponse genderDetails) {
        Optional<MasGender> existingGenderOpt = masGenderRepository.findById(id);
        if (existingGenderOpt.isPresent()) {
            MasGender existingGender = existingGenderOpt.get();
            existingGender.setGenderCode(genderDetails.getGenderCode());
            existingGender.setGenderName(genderDetails.getGenderName());
            existingGender.setCode(genderDetails.getCode());
            existingGender.setStatus(genderDetails.getStatus());
            existingGender.setLastChgDt(Instant.now());

            MasGender updatedGender = masGenderRepository.save(existingGender);
            return ResponseUtils.createSuccessResponse(convertToResponse(updatedGender), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasGenderResponse>() {}, "Gender not found", 404);
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasGenderResponse> changeStatus(Long id, String status) {
        Optional<MasGender> existingGenderOpt = masGenderRepository.findById(id);
        if (existingGenderOpt.isPresent()) {
            MasGender existingGender = existingGenderOpt.get();

            // Ensure the status is either "Y" (Active) or "N" (Inactive)
            if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasGenderResponse>() {}, "Invalid status value. Use 'Y' for Active and 'N' for Inactive.", 400);
            }

            existingGender.setStatus(status); // Set status as "Y" or "N"
            MasGender updatedGender = masGenderRepository.save(existingGender);

            return ResponseUtils.createSuccessResponse(convertToResponse(updatedGender), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasGenderResponse>() {}, "Gender not found", 404);
        }
    }



}
