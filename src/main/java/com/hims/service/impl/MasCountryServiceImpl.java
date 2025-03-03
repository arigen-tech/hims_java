package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasCountry;
import com.hims.entity.repository.MasCountryRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasCountryResponse;
import com.hims.service.MasCountryService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasCountryServiceImpl implements MasCountryService {

    @Autowired
    private MasCountryRepository masCountryRepository;

    @Override
    public ApiResponse<List<MasCountryResponse>> getAllCountries() {
        List<MasCountry> countries = masCountryRepository.findAll();

        List<MasCountryResponse> responses = countries.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    private MasCountryResponse convertToResponse(MasCountry country) {
        MasCountryResponse response = new MasCountryResponse();
        response.setId(country.getId());
        response.setCountryCode(country.getCountryCode());
        response.setCountryName(country.getCountryName());
        response.setStatus(country.getStatus());
        response.setLastChgBy(country.getLastChgBy());
        response.setLastChgDate(country.getLastChgDate());
        response.setLastChgTime(country.getLastChgTime());

        return response;
    }
}
