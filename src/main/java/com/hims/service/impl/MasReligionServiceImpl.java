package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasReligion;
import com.hims.entity.repository.MasReligionRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasReligionResponse;
import com.hims.service.MasReligionService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasReligionServiceImpl implements MasReligionService {

    @Autowired
    private MasReligionRepository masReligionRepository;

    @Override
    public ApiResponse<List<MasReligionResponse>> getAllReligions() {
        List<MasReligion> religions = masReligionRepository.findAll();

        List<MasReligionResponse> responses = religions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
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
