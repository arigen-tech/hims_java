package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasFrequency;
import com.hims.entity.MasGender;
import com.hims.entity.User;
import com.hims.entity.repository.MasFrequencyRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasFrequencyRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasFrequencyResponse;
import com.hims.response.MasGenderResponse;
import com.hims.service.MasFrequencyService;
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

import static com.hims.constants.AppConstants.*;

@Service
public class MasFrequencyServiceImp implements MasFrequencyService {

    private static final Logger log = LoggerFactory.getLogger(MasFrequencyServiceImp.class);

    @Autowired
    private MasFrequencyRepository masFrequencyRepository;

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
    public ApiResponse<MasFrequencyResponse> createMasFrequency(MasFrequencyRequest masFrequencyRequest) {
        MasFrequency masFrequency = new MasFrequency();
        if (!(STATUS_ACTIVE.equalsIgnoreCase(masFrequencyRequest.getStatus()) || STATUS_INACTIVE.equalsIgnoreCase(masFrequencyRequest.getStatus()))) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid status. Status should be 'Y' or 'N'", 400);
        } else {
//            masFrequency.setFrequencyCode(masFrequencyRequest.getFrequencyCode());
            masFrequency.setFrequencyName(masFrequencyRequest.getFrequencyName());
            masFrequency.setStatus(masFrequencyRequest.getStatus());
            masFrequency.setFeq(masFrequencyRequest.getFeq());
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        MSG_CURRENT_USER_NOT_FOUND, HttpStatus.UNAUTHORIZED.value());
            }
            masFrequency.setLastChgBy(String.valueOf(currentUser.getUserId()));
            masFrequency.setLastChgTime(getCurrentTimeFormatted());
            masFrequency.setLastChgDate(Instant.now());
            masFrequency.setOrderNo(masFrequencyRequest.getOrderNo());
//            masFrequency.setFrequency(masFrequencyRequest.getFrequency());

            return ResponseUtils.createSuccessResponse(convertedToResponse(masFrequencyRepository.save(masFrequency)), new TypeReference<>() {
            });
        }
    }

    @Override
    public ApiResponse<MasFrequencyResponse> updateMasFrequency(Long id, MasFrequencyRequest masFrequencyRequest) {
        Optional<MasFrequency> oldMasFrequency=masFrequencyRepository.findById(id);
        if(oldMasFrequency.isPresent()){
            MasFrequency masFrequency=oldMasFrequency.get();

//            masFrequency.setFrequencyCode(masFrequencyRequest.getFrequencyCode());
            masFrequency.setFrequencyName(masFrequencyRequest.getFrequencyName());
            masFrequency.setStatus(masFrequencyRequest.getStatus());
            masFrequency.setFeq(masFrequencyRequest.getFeq());
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        MSG_CURRENT_USER_NOT_FOUND, HttpStatus.UNAUTHORIZED.value());
            }
            masFrequency.setLastChgBy(String.valueOf(currentUser.getUserId()));
            masFrequency.setLastChgTime(getCurrentTimeFormatted());
            masFrequency.setLastChgDate(Instant.now());
            masFrequency.setOrderNo(masFrequencyRequest.getOrderNo());
//            masFrequency.setFrequency(masFrequencyRequest.getFrequency());

            return ResponseUtils.createSuccessResponse(convertedToResponse(masFrequencyRepository.save(masFrequency)), new TypeReference<>() {
            });
        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasFrequencyResponse>() {}, "MasFrequency not found", 404);
        }
    }

    @Override
    public ApiResponse<MasFrequencyResponse> updateMasFrequencyByStatus(Long id, String status) {
        Optional<MasFrequency> oldMasFrequency=masFrequencyRepository.findById(id);
        if(oldMasFrequency.isPresent()){
            MasFrequency masFrequency=oldMasFrequency.get();
            if(STATUS_ACTIVE.equalsIgnoreCase(status)||STATUS_INACTIVE.equalsIgnoreCase(status)){
                masFrequency.setStatus(status);
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            MSG_CURRENT_USER_NOT_FOUND, HttpStatus.UNAUTHORIZED.value());
                }
                masFrequency.setLastChgBy(String.valueOf(currentUser.getUserId()));
                masFrequency.setLastChgTime(getCurrentTimeFormatted());
                masFrequency.setLastChgDate(Instant.now());
                return ResponseUtils.createSuccessResponse(convertedToResponse(masFrequencyRepository.save(masFrequency)), new TypeReference<>() {
                });
            }else{
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid status. Status should be 'Y' or 'N'", 400);
            }


        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasFrequencyResponse>() {}, "MasFrequency not found", 404);
        }

    }

    @Override
    public ApiResponse<MasFrequencyResponse> getByIdMasFrequency(Long id) {
        Optional<MasFrequency> oldMasFrequency=masFrequencyRepository.findById(id);
        if(oldMasFrequency.isPresent()){
            return ResponseUtils.createSuccessResponse(convertedToResponse(oldMasFrequency.get()), new TypeReference<>() {});
        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasFrequencyResponse>() {}, "MasFrequency data not found", 404);
        }
    }

    @Override
    public ApiResponse<List<MasFrequencyResponse>> getByAllMasFrequency(int flag) {
        List<MasFrequency> masFrequency;


        if (flag == FLAG_ACTIVE_ONLY) {
            masFrequency= masFrequencyRepository.findByStatusIgnoreCaseOrderByFrequencyNameAsc(STATUS_ACTIVE_UPPER);
        } else if (flag == FLAG_ALL) {
            masFrequency= masFrequencyRepository.findAllByOrderByStatusDescLastChgDateDescLastChgTimeDesc();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, MSG_INVALID_FLAG, 400);
        }
        List<MasFrequencyResponse> responses = masFrequency.stream()
                .map(this::convertedToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});

    }


    private MasFrequencyResponse convertedToResponse(MasFrequency masFrequency){
        MasFrequencyResponse masFrequencyResponse=new MasFrequencyResponse();
        masFrequencyResponse.setFrequencyId(masFrequency.getFrequency_id());
//        masFrequencyResponse.setFrequencyCode(masFrequency.getFrequencyCode());
        masFrequencyResponse.setFrequencyName(masFrequency.getFrequencyName());
        masFrequencyResponse.setStatus(masFrequency.getStatus());
        masFrequencyResponse.setFeq(masFrequency.getFeq());
        masFrequencyResponse.setLastChgBy(masFrequency.getLastChgBy());
        masFrequencyResponse.setLastChgTime(getCurrentTimeFormatted());
        masFrequencyResponse.setLastChgDate(Instant.now());
        masFrequencyResponse.setOrderNo(masFrequency.getOrderNo());
//        masFrequencyResponse.setFrequency(masFrequency.getFrequency());

        return masFrequencyResponse;
    }
}
