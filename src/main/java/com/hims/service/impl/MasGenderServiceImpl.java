package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasGender;
import com.hims.entity.repository.MasGenderRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasGenderResponse;
import com.hims.service.MasGenderService;
import com.hims.utils.ResponseUtils;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
}
