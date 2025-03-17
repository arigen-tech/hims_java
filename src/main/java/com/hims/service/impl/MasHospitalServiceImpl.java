package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasHospital;
import com.hims.entity.repository.MasHospitalRepository;
import com.hims.entity.repository.MasCountryRepository;
import com.hims.entity.repository.MasStateRepository;
import com.hims.entity.repository.MasDistrictRepository;
import com.hims.entity.MasCountry;
import com.hims.entity.MasState;
import com.hims.entity.MasDistrict;
import com.hims.request.MasHospitalRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasHospitalResponse;
import com.hims.service.MasHospitalService;
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
public class MasHospitalServiceImpl implements MasHospitalService {

    @Autowired
    private MasHospitalRepository masHospitalRepository;

    @Autowired
    private MasCountryRepository masCountryRepository;

    @Autowired
    private MasStateRepository masStateRepository;

    @Autowired
    private MasDistrictRepository masDistrictRepository;

    private String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    public ApiResponse<List<MasHospitalResponse>> getAllHospitals(int flag) {
        List<MasHospital> hospitals;

        if (flag == 1) {
            // Fetch only records with status 'Y'
            hospitals = masHospitalRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            // Fetch all records with status 'Y' or 'N'
            hospitals = masHospitalRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            // Handle invalid flag values
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

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
        response.setContactNumber2(hospital.getContactNumber2());
        response.setLastChgBy(hospital.getLastChgBy());
        response.setLastChgDate(hospital.getLastChgDate());
        response.setLastChgTime(getCurrentTimeFormatted());

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

        response.setPinCode(hospital.getPinCode());
        response.setCity(hospital.getCity());
        response.setEmail(hospital.getEmail());
        response.setGstnNo(hospital.getGstnNo());
        response.setRegCostApplicable(hospital.getRegCostApplicable());
        response.setAppCostApplicable(hospital.getAppCostApplicable());
        response.setPreConsultationAvailable(hospital.getPreConsultationAvailable());

        return response;
    }

    @Override
    @Transactional
    public ApiResponse<MasHospitalResponse> addHospital(MasHospitalRequest hospitalRequest) {
        MasHospital hospital = new MasHospital();
        hospital.setHospitalCode(hospitalRequest.getHospitalCode());
        hospital.setHospitalName(hospitalRequest.getHospitalName());
        hospital.setStatus(hospitalRequest.getStatus());
        hospital.setAddress(hospitalRequest.getAddress());
        hospital.setContactNumber(hospitalRequest.getContactNumber());
        hospital.setContactNumber2(hospitalRequest.getContactNumber2());
        hospital.setLastChgBy(hospitalRequest.getLastChgBy());
        hospital.setLastChgDate(Instant.now());
        hospital.setLastChgTime(getCurrentTimeFormatted());

        if (hospitalRequest.getCountryId() != null) {
            Optional<MasCountry> country = masCountryRepository.findById(hospitalRequest.getCountryId());
            country.ifPresent(hospital::setCountry);
        }

        if (hospitalRequest.getStateId() != null) {
            Optional<MasState> state = masStateRepository.findById(hospitalRequest.getStateId());
            state.ifPresent(hospital::setState);
        }

        if (hospitalRequest.getDistrictId() != null) {
            Optional<MasDistrict> district = masDistrictRepository.findById(hospitalRequest.getDistrictId());
            district.ifPresent(hospital::setDistrict);
        }

        hospital.setPinCode(hospitalRequest.getPinCode());
        hospital.setCity(hospitalRequest.getCity());
        hospital.setEmail(hospitalRequest.getEmail());
        hospital.setGstnNo(hospitalRequest.getGstnNo());
        hospital.setRegCostApplicable(hospitalRequest.getRegCostApplicable());
        hospital.setAppCostApplicable(hospitalRequest.getAppCostApplicable());
        hospital.setPreConsultationAvailable(hospitalRequest.getPreConsultationAvailable());

        MasHospital savedHospital = masHospitalRepository.save(hospital);
        return ResponseUtils.createSuccessResponse(convertToResponse(savedHospital), new TypeReference<>() {});
    }

    @Override
    @Transactional
    public ApiResponse<MasHospitalResponse> updateHospital(Long id, MasHospitalResponse hospitalDetails) {
        Optional<MasHospital> existingHospitalOpt = masHospitalRepository.findById(id);
        if (existingHospitalOpt.isPresent()) {
            MasHospital existingHospital = existingHospitalOpt.get();
            existingHospital.setHospitalCode(hospitalDetails.getHospitalCode());
            existingHospital.setHospitalName(hospitalDetails.getHospitalName());
            existingHospital.setStatus(hospitalDetails.getStatus());
            existingHospital.setAddress(hospitalDetails.getAddress());
            existingHospital.setContactNumber(hospitalDetails.getContactNumber());
            existingHospital.setContactNumber2(hospitalDetails.getContactNumber2());
            existingHospital.setLastChgBy(hospitalDetails.getLastChgBy());
            existingHospital.setLastChgDate(Instant.now());
            existingHospital.setLastChgTime(getCurrentTimeFormatted());

            // Update relationships if IDs are provided
            if (hospitalDetails.getCountryId() != null) {
                Optional<MasCountry> country = masCountryRepository.findById(hospitalDetails.getCountryId());
                country.ifPresent(existingHospital::setCountry);
            }

            if (hospitalDetails.getStateId() != null) {
                Optional<MasState> state = masStateRepository.findById(hospitalDetails.getStateId());
                state.ifPresent(existingHospital::setState);
            }

            if (hospitalDetails.getDistrictId() != null) {
                Optional<MasDistrict> district = masDistrictRepository.findById(hospitalDetails.getDistrictId());
                district.ifPresent(existingHospital::setDistrict);
            }

            existingHospital.setPinCode(hospitalDetails.getPinCode());
            existingHospital.setCity(hospitalDetails.getCity());
            existingHospital.setEmail(hospitalDetails.getEmail());
            existingHospital.setGstnNo(hospitalDetails.getGstnNo());
            existingHospital.setRegCostApplicable(hospitalDetails.getRegCostApplicable());
            existingHospital.setAppCostApplicable(hospitalDetails.getAppCostApplicable());
            existingHospital.setPreConsultationAvailable(hospitalDetails.getPreConsultationAvailable());

            MasHospital updatedHospital = masHospitalRepository.save(existingHospital);
            return ResponseUtils.createSuccessResponse(convertToResponse(updatedHospital), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasHospitalResponse>() {},
                    "Hospital not found", 404);
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasHospitalResponse> changeStatus(Long id, String status) {
        Optional<MasHospital> existingHospitalOpt = masHospitalRepository.findById(id);
        if (existingHospitalOpt.isPresent()) {
            MasHospital existingHospital = existingHospitalOpt.get();

            // Validate status value
            if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasHospitalResponse>() {},
                        "Invalid status value. Use 'Y' for Active and 'N' for Inactive.", 400);
            }

            existingHospital.setStatus(status);
            MasHospital updatedHospital = masHospitalRepository.save(existingHospital);

            return ResponseUtils.createSuccessResponse(convertToResponse(updatedHospital), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasHospitalResponse>() {},
                    "Hospital not found", 404);
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasHospitalResponse> findById(Long id) {
        Optional<MasHospital> existingHospitalOpt = masHospitalRepository.findById(id);
        if (existingHospitalOpt.isPresent()) {
            return ResponseUtils.createSuccessResponse(convertToResponse(existingHospitalOpt.get()), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasHospitalResponse>() {},
                    "Hospital not found", 404);
        }
    }
}