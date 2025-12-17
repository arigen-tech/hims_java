package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.DgUom;
import com.hims.entity.MasGender;
import com.hims.entity.User;
import com.hims.entity.repository.DgUomRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.DgUomRequest;
import com.hims.response.*;
import com.hims.service.DgUomService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DgUomServiceImp implements DgUomService {

    private static final Logger log = LoggerFactory.getLogger(DgUomServiceImp.class);


    @Autowired
    private DgUomRepository dgUomRepository;
    @Autowired
    UserRepo userRepo;
    private String getCurrentTimeFormatted() {

        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    @Override
    public ApiResponse<DgUomResponse> addDgUom(DgUomRequest dgUomRequest) {
        DgUom dgUom=new DgUom();
        if("y".equals(dgUomRequest.getStatus())||"n".equals(dgUomRequest.getStatus())) {
            dgUom.setStatus(dgUomRequest.getStatus());
        } else{
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid status. Status should be 'y' or 'n'", 400);
            }
            dgUom.setName(dgUomRequest.getName());
            dgUom.setUomCode(dgUomRequest.getUomCode());
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            dgUom.setLastChgBy(currentUser.getUsername());
            dgUom.setLastChgDate(Instant.now());
            dgUom.setLastChgTime(getCurrentTimeFormatted());
            return ResponseUtils.createSuccessResponse(convertedToResponse(dgUomRepository.save(dgUom)), new TypeReference<>() {
            });

        }

        @Override
        public ApiResponse<DgUomResponse> getByIdDgUom(Long id) {
        Optional<DgUom> oldDgUom=dgUomRepository.findById(id);
        if(oldDgUom.isPresent()){
            DgUom newDgUom=oldDgUom.get();
            return ResponseUtils.createSuccessResponse(convertedToResponse(newDgUom), new TypeReference<>() {});
        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<DgUomResponse>() {}, "DgUom data not found", 404);

        }
    }

    @Override
    public ApiResponse<List<DgUomResponse>> getAllDgUom(int flag) {
        List<DgUom> dpUom;
        if(flag==1){
            dpUom=dgUomRepository.findByStatusOrderByNameAsc("y");
        }else if(flag==0){
            dpUom=dgUomRepository.findAllByOrderByStatusDescLastChgDateDescLastChgTimeDesc();

        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }
        List<DgUomResponse> responses = dpUom.stream()
                .map(this::convertedToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});


    }

    @Override
    public ApiResponse<DgUomResponse> updateByStatusDgUom(Long id, String status) {
        Optional<DgUom> dgUom = dgUomRepository.findById(id);
        if (dgUom.isPresent()) {
            DgUom newDgUom = dgUom.get();
            if(status.equals("n")||status.equals("y")){
                newDgUom.setStatus(status);
            }else{
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid status. Status should be 'y' or 'n'", 400);
            }
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                newDgUom.setLastChgBy(currentUser.getUsername());
                return ResponseUtils.createSuccessResponse(convertedToResponse(dgUomRepository.save(newDgUom)), new TypeReference<>() {
                });

        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<DgUomResponse>() {}, "DgUom not found", 404);
        }
    }

    @Override
    public ApiResponse<DgUomResponse> updateByIdDgUom(Long id, DgUomRequest dgUomRequest) {
        Optional<DgUom> dgUom=dgUomRepository.findById(id);
        if(dgUom.isPresent()){
            DgUom newDgUom=dgUom.get();
            if("y".equals(dgUomRequest.getStatus())||"n".equals(dgUomRequest.getStatus())) {
                newDgUom.setStatus(dgUomRequest.getStatus());
            } else{
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid status. Status should be 'y' or 'n'", 400);
            }
            newDgUom.setName(dgUomRequest.getName());
            newDgUom.setUomCode(dgUomRequest.getUomCode());
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            newDgUom.setLastChgBy(currentUser.getUsername());
            newDgUom.setLastChgDate(Instant.now());
            newDgUom.setLastChgTime(getCurrentTimeFormatted());
            return ResponseUtils.createSuccessResponse(convertedToResponse(dgUomRepository.save( newDgUom)), new TypeReference<>() {
            });

        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<DgUomResponse>() {}, "FgUom data not found", 404);
        }
    }


    private DgUomResponse convertedToResponse(DgUom dgUom){
        DgUomResponse response=new DgUomResponse();
        response.setId(dgUom.getId());
        response.setName(dgUom.getName());
        response.setUomCode(dgUom.getUomCode());
        response.setStatus(dgUom.getStatus());
        response.setLastChgBy(dgUom.getLastChgBy());
        response.setLastChgDate(dgUom.getLastChgDate());
        response.setLastChgTime(dgUom.getLastChgTime());
        return response;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }

}
