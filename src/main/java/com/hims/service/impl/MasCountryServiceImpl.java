package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasCountry;
import com.hims.entity.User;
import com.hims.entity.repository.MasCountryRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasCountryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasCountryResponse;
import com.hims.service.MasCountryService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasCountryServiceImpl implements MasCountryService {

    private static final Logger log = LoggerFactory.getLogger(MasCountryServiceImpl.class);

    @Autowired
    private MasCountryRepository masCountryRepository;

    @Autowired
    private UserRepo userRepo;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }

    private String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    public ApiResponse<MasCountryResponse> addCountry(MasCountryRequest request) {
        try{
            MasCountry country = new MasCountry();
            country.setCountryCode(request.getCountryCode());
            country.setCountryName(request.getCountryName());
            country.setStatus("y");
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            country.setLastChgBy(String.valueOf(currentUser.getUserId()));
            country.setLastChgDate(Instant.now());
            country.setLastChgTime(getCurrentTimeFormatted());

            MasCountry savedCountry = masCountryRepository.save(country);
            return ResponseUtils.createSuccessResponse(mapToResponse(savedCountry), new TypeReference<>() {
            });
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<String> changeCountryStatus(Long id, String status) {
        try{
            Optional<MasCountry> countryOpt = masCountryRepository.findById(id);
            if (countryOpt.isPresent()) {
                MasCountry country = countryOpt.get();
                country.setStatus(status);
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                country.setLastChgBy(String.valueOf(currentUser.getUserId()));
                country.setLastChgDate(Instant.now());
                masCountryRepository.save(country);
                return ResponseUtils.createSuccessResponse("Country status updated", new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createNotFoundResponse("Country not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasCountryResponse> editCountry(Long id, MasCountryRequest request) {
        try{
            Optional<MasCountry> countryOpt = masCountryRepository.findById(id);
            if (countryOpt.isPresent()) {
                MasCountry country = countryOpt.get();
                country.setCountryCode(request.getCountryCode());
                country.setCountryName(request.getCountryName());
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                country.setLastChgBy(String.valueOf(currentUser.getUserId()));
                country.setLastChgDate(Instant.now());
                country.setLastChgTime(getCurrentTimeFormatted());

                masCountryRepository.save(country);
                return ResponseUtils.createSuccessResponse(mapToResponse(country), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createNotFoundResponse("Country not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasCountryResponse> getCountryById(Long id) {
        return masCountryRepository.findById(id)
                .map(country -> ResponseUtils.createSuccessResponse(mapToResponse(country), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Country not found", 404));
    }

    @Override
    public ApiResponse<List<MasCountryResponse>> getAllCountries(int flag) {
        List<MasCountry> countries;

        if (flag == 1) {
            countries = masCountryRepository.findByStatusIgnoreCaseOrderByCountryNameAsc("Y");
        } else if (flag == 0) {
            countries = masCountryRepository.findByStatusIgnoreCaseInOrderByLastChgDateDescLastChgTimeDesc(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasCountryResponse> responses = countries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
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
