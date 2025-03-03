package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasHospital;
import com.hims.entity.repository.MasHospitalRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasHospitalResponse;
import com.hims.service.MasHospitalService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasHospitalServiceImpl implements MasHospitalService {

    @Autowired
    private MasHospitalRepository masHospitalRepository;

    @Override
    public ApiResponse<List<MasHospitalResponse>> getAllHospitals() {
        List<MasHospital> hospitals = masHospitalRepository.findAll();

        List<MasHospitalResponse> responses = hospitals.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    private MasHospitalResponse convertToResponse(MasHospital hospital) {
        MasHospitalResponse response = new MasHospitalResponse();
        response.setId(hospital.getId());
        response.setHospitalCode(hospital.getHospitalCode());
        response.setHospitalName(hospital.getHospitalName());
        response.setStatus(hospital.getStatus());
        response.setAddress(hospital.getAddress());
        response.setContactNumber(hospital.getContactNumber());
        response.setLastChgBy(hospital.getLastChgBy());
        response.setLastChgDate(hospital.getLastChgDate());
        response.setLastChgTime(hospital.getLastChgTime());
        response.setPinCode(hospital.getPinCode());
        response.setRegCostApplicable(hospital.getRegCostApplicable());
        response.setAppCostApplicable(hospital.getAppCostApplicable());
        response.setPreConsultationAvailable(hospital.getPreConsultationAvailable());

        if (hospital.getCountry() != null) {
            response.setCountryId(hospital.getCountry().getId());
            response.setCountryName(hospital.getCountry().getCountryName());
        }
        if (hospital.getState() != null) {
            response.setStateId(hospital.getState().getId());
            response.setStateName(hospital.getState().getStateName());
        }
        if (hospital.getDistrict() != null) {
            response.setDistrictId(hospital.getDistrict().getId());
            response.setDistrictName(hospital.getDistrict().getDistrictName());
        }

        return response;
    }
}
