package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.MasHospitalRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasHospitalResponse;
import com.hims.service.MasHospitalService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasHospitalServiceImpl implements MasHospitalService {

    private static final Logger log = LoggerFactory.getLogger(MasHospitalServiceImpl.class);

    @Autowired
    private MasHospitalRepository masHospitalRepository;

    @Autowired
    private MasCountryRepository masCountryRepository;

    @Autowired
    private MasStateRepository masStateRepository;

    @Autowired
    private MasDistrictRepository masDistrictRepository;

    @Autowired
    private UserRepo userRepo;

    private String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
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
        try{
            MasHospital hospital = new MasHospital();
            hospital.setHospitalCode(hospitalRequest.getHospitalCode());
            hospital.setHospitalName(hospitalRequest.getHospitalName());
            hospital.setStatus("y");
            hospital.setAddress(hospitalRequest.getAddress());
            hospital.setContactNumber(hospitalRequest.getContactNumber());
            hospital.setContactNumber2(hospitalRequest.getContactNumber2());
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            hospital.setLastChgBy(String.valueOf(currentUser.getUserId()));
            hospital.setLastChgDate(LocalDate.now());
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
            return ResponseUtils.createSuccessResponse(convertToResponse(savedHospital), new TypeReference<>() {
            });
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasHospitalResponse> updateHospital(Long id, MasHospitalRequest hospitalRequest) {
        try{
            Optional<MasHospital> existingHospitalOpt = masHospitalRepository.findById(id);
            if (existingHospitalOpt.isPresent()) {
                MasHospital existingHospital = existingHospitalOpt.get();
                existingHospital.setHospitalCode(hospitalRequest.getHospitalCode());
                existingHospital.setHospitalName(hospitalRequest.getHospitalName());
                existingHospital.setAddress(hospitalRequest.getAddress());
                existingHospital.setContactNumber(hospitalRequest.getContactNumber());
                existingHospital.setContactNumber2(hospitalRequest.getContactNumber2());
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                existingHospital.setLastChgBy(String.valueOf(currentUser.getUserId()));
                existingHospital.setLastChgDate(LocalDate.now());
                existingHospital.setLastChgTime(getCurrentTimeFormatted());

                // Update relationships if IDs are provided
                if (hospitalRequest.getCountryId() != null) {
                    Optional<MasCountry> country = masCountryRepository.findById(hospitalRequest.getCountryId());
                    country.ifPresent(existingHospital::setCountry);
                }

                if (hospitalRequest.getStateId() != null) {
                    Optional<MasState> state = masStateRepository.findById(hospitalRequest.getStateId());
                    state.ifPresent(existingHospital::setState);
                }

                if (hospitalRequest.getDistrictId() != null) {
                    Optional<MasDistrict> district = masDistrictRepository.findById(hospitalRequest.getDistrictId());
                    district.ifPresent(existingHospital::setDistrict);
                }

                existingHospital.setPinCode(hospitalRequest.getPinCode());
                existingHospital.setCity(hospitalRequest.getCity());
                existingHospital.setEmail(hospitalRequest.getEmail());
                existingHospital.setGstnNo(hospitalRequest.getGstnNo());
                existingHospital.setRegCostApplicable(hospitalRequest.getRegCostApplicable());
                existingHospital.setAppCostApplicable(hospitalRequest.getAppCostApplicable());
                existingHospital.setPreConsultationAvailable(hospitalRequest.getPreConsultationAvailable());

                MasHospital updatedHospital = masHospitalRepository.save(existingHospital);
                return ResponseUtils.createSuccessResponse(convertToResponse(updatedHospital), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasHospitalResponse>() {
                        },
                        "Hospital not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasHospitalResponse> changeStatus(Long id, String status) {
        try{
            Optional<MasHospital> existingHospitalOpt = masHospitalRepository.findById(id);
            if (existingHospitalOpt.isPresent()) {
                MasHospital existingHospital = existingHospitalOpt.get();

                // Validate status value
                if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<MasHospitalResponse>() {
                            },
                            "Invalid status value. Use 'Y' for Active and 'N' for Inactive.", 400);
                }

                existingHospital.setStatus(status);
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                existingHospital.setLastChgBy(String.valueOf(currentUser.getUserId()));
                MasHospital updatedHospital = masHospitalRepository.save(existingHospital);

                return ResponseUtils.createSuccessResponse(convertToResponse(updatedHospital), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasHospitalResponse>() {
                        },
                        "Hospital not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
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