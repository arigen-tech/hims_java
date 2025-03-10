package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasCountry;
import com.hims.entity.repository.MasCountryRepository;
import com.hims.request.MasCountryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasCountryResponse;
import com.hims.service.MasCountryService;
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
public class MasCountryServiceImpl implements MasCountryService {

    @Autowired
    private MasCountryRepository masCountryRepository;

    private String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    public ApiResponse<MasCountryResponse> addCountry(MasCountryRequest request) {
        MasCountry country = new MasCountry();
        country.setCountryCode(request.getCountryCode());
        country.setCountryName(request.getCountryName());
        country.setStatus(request.getStatus());
        country.setLastChgBy(request.getLastChgBy());
        country.setLastChgDate(Instant.now());
        country.setLastChgTime(getCurrentTimeFormatted());

        MasCountry savedCountry = masCountryRepository.save(country);
        return ResponseUtils.createSuccessResponse(mapToResponse(savedCountry), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<String> changeCountryStatus(Long id, String status) {
        Optional<MasCountry> countryOpt = masCountryRepository.findById(id);
        if (countryOpt.isPresent()) {
            MasCountry country = countryOpt.get();
            country.setStatus(status);
            country.setLastChgDate(Instant.now());
            masCountryRepository.save(country);
            return ResponseUtils.createSuccessResponse("Country status updated", new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Country not found", 404);
        }
    }

    @Override
    public ApiResponse<MasCountryResponse> editCountry(Long id, MasCountryRequest request) {
        Optional<MasCountry> countryOpt = masCountryRepository.findById(id);
        if (countryOpt.isPresent()) {
            MasCountry country = countryOpt.get();
            country.setCountryCode(request.getCountryCode());
            country.setCountryName(request.getCountryName());
            country.setStatus(request.getStatus());
            country.setLastChgBy(request.getLastChgBy());
            country.setLastChgDate(Instant.now());
            country.setLastChgTime(getCurrentTimeFormatted());

            masCountryRepository.save(country);
            return ResponseUtils.createSuccessResponse(mapToResponse(country), new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Country not found", 404);
        }
    }

    @Override
    public ApiResponse<MasCountryResponse> getCountryById(Long id) {
        return masCountryRepository.findById(id)
                .map(country -> ResponseUtils.createSuccessResponse(mapToResponse(country), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Country not found", 404));
    }

    @Override
    public ApiResponse<List<MasCountryResponse>> getAllCountries() {
        List<MasCountryResponse> countries = masCountryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseUtils.createSuccessResponse(countries, new TypeReference<>() {});
    }

    private MasCountryResponse mapToResponse(MasCountry country) {
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
