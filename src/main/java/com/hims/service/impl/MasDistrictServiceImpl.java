package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDistrict;
import com.hims.entity.repository.MasDistrictRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasDistrictResponse;
import com.hims.service.MasDistrictService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasDistrictServiceImpl implements MasDistrictService {

    @Autowired
    private MasDistrictRepository masDistrictRepository;

    @Override
    public ApiResponse<List<MasDistrictResponse>> getAllDistricts() {
        List<MasDistrict> districts = masDistrictRepository.findAll();

        List<MasDistrictResponse> responses = districts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    private MasDistrictResponse convertToResponse(MasDistrict district) {
        MasDistrictResponse response = new MasDistrictResponse();
        response.setId(district.getId());
        response.setDistrictName(district.getDistrictName());
        response.setStatus(district.getStatus());
        response.setLasChBy(district.getLasChBy());
        response.setLastChgDate(district.getLastChgDate());

        if (district.getState() != null) {
            response.setStateId(district.getState().getId());
            response.setStateName(district.getState().getStateName());
        }

        return response;
    }
}
