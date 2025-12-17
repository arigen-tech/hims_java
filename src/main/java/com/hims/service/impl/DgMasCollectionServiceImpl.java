package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.DgMasCollectionRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.DgMasCollectionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasCollectionResponse;
import com.hims.response.MasFrequencyResponse;
import com.hims.response.MasSubChargeCodeDTO;
import com.hims.service.DgMasCollectionService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DgMasCollectionServiceImpl implements DgMasCollectionService {
    @Autowired
    private DgMasCollectionRepository dgMasCollectionRepository;
    @Autowired
    private UserRepo userRepo;
    private static final Logger log = LoggerFactory.getLogger(MasStateServiceImpl.class);
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
    public ApiResponse<DgMasCollectionResponse> addDgMasCollection(DgMasCollectionRequest dgMasCollectionRequest) {
        try {
            DgMasCollection dgMasCollection = new DgMasCollection();
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            dgMasCollection.setCollectionCode(dgMasCollectionRequest.getCollectionCode());
            dgMasCollection.setCollectionName(dgMasCollectionRequest.getCollectionName());
            dgMasCollection.setStatus("y");
            dgMasCollection.setLastChgTime(getCurrentTimeFormatted());
            dgMasCollection.setLastChgDate(LocalDate.now());
            dgMasCollection.setLastChgBy(currentUser.getUsername());


            return ResponseUtils.createSuccessResponse(convertedToResponse(dgMasCollectionRepository.save(dgMasCollection)), new TypeReference<>() {
            });
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Something went wrong while adding collection: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }

    }

    @Override
    public ApiResponse<DgMasCollectionResponse> update(Long id, DgMasCollectionRequest request) {
        try{
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }
        Optional<DgMasCollection> dgMasCollection = dgMasCollectionRepository.findById(id);
        if (dgMasCollection.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<DgMasCollectionResponse>() {
            }, "DgMasCollection Id not found", 404);
        }
        DgMasCollection dgMasCollection1 = dgMasCollection.get();
        dgMasCollection1.setCollectionCode(request.getCollectionCode());
        dgMasCollection1.setCollectionName(request.getCollectionName());
        dgMasCollection1.setStatus("y");
        dgMasCollection1.setLastChgTime(getCurrentTimeFormatted());
        dgMasCollection1.setLastChgDate(LocalDate.now());
        dgMasCollection1.setLastChgBy(currentUser.getUsername());

        return ResponseUtils.createSuccessResponse(convertedToResponse(dgMasCollectionRepository.save(dgMasCollection1)), new TypeReference<>() {
        });
    }catch (Exception e) {

        return ResponseUtils.createFailureResponse(
                null,
                new TypeReference<>() {},
                "Something went wrong while updating collection: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
    }



    }

    @Override
    public ApiResponse<List<DgMasCollectionResponse>> getDgMasCollection(int flag) {
        List<DgMasCollection> dgMasCollections;
        if (flag == 1) {
            dgMasCollections = dgMasCollectionRepository.findByStatusOrderByLastChgDateDesc("y");
        } else if (flag == 0) {
            dgMasCollections = dgMasCollectionRepository.findAllByOrderByCollectionNameAsc();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<DgMasCollectionResponse> subDTO = dgMasCollections.stream()
                .map(this::convertedToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(subDTO, new TypeReference<>() {});

    }

    @Override
    public ApiResponse<DgMasCollectionResponse> findById(Long id) {
        Optional<DgMasCollection> dgMasCollection=dgMasCollectionRepository.findById(id);
        if(dgMasCollection.isEmpty()){
            return ResponseUtils.createFailureResponse(null, new TypeReference<DgMasCollectionResponse>() {}, "DgMasCollection Id not found", 404);
        }
        DgMasCollection dgMasCollection1= dgMasCollection.get();
        return ResponseUtils.createSuccessResponse(convertedToResponse(dgMasCollection.get()), new TypeReference<>() {
        });
    }

    @Override
    public ApiResponse<DgMasCollectionResponse> changeDgMasCollectionStatus(Long id, String status) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            if (!"y".equalsIgnoreCase(status) && !"n".equalsIgnoreCase(status)) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Invalid status value. Use 'y' or 'n'.", HttpStatus.BAD_REQUEST.value());
            }

            Optional<DgMasCollection> dgMasCollection = dgMasCollectionRepository.findById(id);
            if (dgMasCollection.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<DgMasCollectionResponse>() {
                }, "DgMasCollection Id not found", 404);
            }
            DgMasCollection dgMasCollection1 = dgMasCollection.get();
            dgMasCollection1.setStatus(status.toLowerCase());
            dgMasCollection1.setLastChgTime(getCurrentTimeFormatted());
            dgMasCollection1.setLastChgDate(LocalDate.now());
            dgMasCollection1.setLastChgBy(currentUser.getUsername());

            return ResponseUtils.createSuccessResponse(convertedToResponse(dgMasCollectionRepository.save(dgMasCollection1)), new TypeReference<>() {
            });
        }catch (Exception e) {

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Something went wrong while updating collection: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }

    }

    private DgMasCollectionResponse convertedToResponse(DgMasCollection dgMasCollection){
        DgMasCollectionResponse response=new DgMasCollectionResponse();
        response.setCollectionId(dgMasCollection.getCollectionId());
        response.setCollectionName(dgMasCollection.getCollectionName());
        response.setCollectionCode(dgMasCollection.getCollectionCode());
        response.setStatus(dgMasCollection.getStatus());
        response.setLastChgDate(dgMasCollection.getLastChgDate().atStartOfDay());
        response.setLastChgBy(dgMasCollection.getLastChgBy());
        response.setLastChgTime(dgMasCollection.getLastChgTime());
        return response;

    }
}
