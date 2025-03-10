package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDistrict;
import com.hims.entity.MasState;
import com.hims.entity.repository.MasDistrictRepository;
import com.hims.entity.repository.MasStateRepository;
import com.hims.request.MasDistrictRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDistrictResponse;
import com.hims.service.MasDistrictService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasDistrictServiceImpl implements MasDistrictService {

    @Autowired
    private MasDistrictRepository masDistrictRepository;

    @Autowired
    private MasStateRepository masStateRepository;

    @Override
    public ApiResponse<MasDistrictResponse> addDistrict(MasDistrictRequest request) {
        MasState state = masStateRepository.findById(request.getStateId()).orElse(null);
        if (state == null) {
            return ResponseUtils.createNotFoundResponse("State not found", 404);
        }

        MasDistrict district = new MasDistrict();
        district.setDistrictName(request.getDistrictName());
        district.setStatus(request.getStatus());
        district.setLasChBy(request.getLasChBy());
        district.setLastChgDate(Instant.now());
        district.setState(state);

        MasDistrict savedDistrict = masDistrictRepository.save(district);
        return ResponseUtils.createSuccessResponse(mapToResponse(savedDistrict), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<String> changeDistrictStatus(Long id, String status) {
        Optional<MasDistrict> districtOpt = masDistrictRepository.findById(id);
        if (districtOpt.isPresent()) {
            MasDistrict district = districtOpt.get();
            district.setStatus(status);
            masDistrictRepository.save(district);
            return ResponseUtils.createSuccessResponse("District status updated", new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("District not found", 404);
        }
    }

    @Override
    public ApiResponse<MasDistrictResponse> editDistrict(Long id, MasDistrictRequest request) {
        Optional<MasDistrict> districtOpt = masDistrictRepository.findById(id);
        if (districtOpt.isPresent()) {
            MasDistrict district = districtOpt.get();
            district.setDistrictName(request.getDistrictName());
            district.setStatus(request.getStatus());
            district.setLasChBy(request.getLasChBy());
            district.setLastChgDate(Instant.now());
            masDistrictRepository.save(district);
            return ResponseUtils.createSuccessResponse(mapToResponse(district), new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("District not found", 404);
        }
    }

    @Override
    public ApiResponse<MasDistrictResponse> getDistrictById(Long id) {
        return masDistrictRepository.findById(id)
                .map(district -> ResponseUtils.createSuccessResponse(mapToResponse(district), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("District not found", 404));
    }

    @Override
    public ApiResponse<List<MasDistrictResponse>> getAllDistricts() {
        List<MasDistrictResponse> districts = masDistrictRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseUtils.createSuccessResponse(districts, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<MasDistrictResponse>> getDistrictsByStateId(Long stateId) {
        List<MasDistrictResponse> districts = masDistrictRepository.findByStateId(stateId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseUtils.createSuccessResponse(districts, new TypeReference<>() {});
    }

    private MasDistrictResponse mapToResponse(MasDistrict district) {
        MasDistrictResponse response = new MasDistrictResponse();
        response.setId(district.getId());
        response.setDistrictName(district.getDistrictName());
        response.setStatus(district.getStatus());
        response.setLasChBy(district.getLasChBy());
        response.setLastChgDate(district.getLastChgDate());
        response.setStateId(district.getState().getId());
        return response;
    }
}
