package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasMainChargeCode;
import com.hims.entity.MasSubChargeCode;
import com.hims.entity.User;
import com.hims.entity.repository.MasMainChargeCodeRepository;
import com.hims.entity.repository.MasSubChargeCodeRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasSubChargeCodeReq;
import com.hims.response.ApiResponse;
import com.hims.response.MasSubChargeCodeDTO;
import com.hims.service.MasSubChargeCodeService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MasSubChargeCodeServiceImpl implements MasSubChargeCodeService {

    private static final Logger log = LoggerFactory.getLogger(MasSubChargeCodeServiceImpl.class);

    @Autowired
    private MasSubChargeCodeRepository subRepo;

    @Autowired
    private MasMainChargeCodeRepository mainRepo;

    @Autowired
    private UserRepo userRepo;

    private String getCurrentTimeFormatted(){
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

    private MasSubChargeCodeDTO toResponse(MasSubChargeCode code) {
        MasSubChargeCodeDTO dto = new MasSubChargeCodeDTO();
        dto.setSubId(code.getSubId());
        dto.setSubCode(code.getSubCode());
        dto.setSubName(code.getSubName());
        dto.setStatus(code.getStatus());
        dto.setLastChgBy(code.getLastChgBy());
        dto.setLastChgDate(code.getLastChgDate());
        dto.setLastChgTime(code.getLastChgTime());
        dto.setMainChargeId(code.getMainChargeId().getChargecodeId());

        return dto;
    }
    @Override
    public ApiResponse<MasSubChargeCodeDTO> createSubCharge(MasSubChargeCodeReq codeReq){
        try{
            Optional<MasMainChargeCode> mainChargeCode = mainRepo.findById(codeReq.getMainChargeId());
            if (mainChargeCode.isPresent()) {

                MasSubChargeCode subCode = new MasSubChargeCode();
                    subCode.setSubCode(codeReq.getSubCode());
                    subCode.setSubName(codeReq.getSubName());
                    subCode.setStatus("y");
                    User currentUser = getCurrentUser();
                    if (currentUser == null) {
                        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                                },
                                "Current user not found", HttpStatus.UNAUTHORIZED.value());
                    }
                    subCode.setLastChgBy(String.valueOf(currentUser.getUserId()));
                    subCode.setLastChgDate(LocalDate.now());
                    subCode.setLastChgTime(getCurrentTimeFormatted());
                    subCode.setMainChargeId(mainChargeCode.get());
                    return ResponseUtils.createSuccessResponse(toResponse(subRepo.save(subCode)), new TypeReference<>() {});
            }
            else {
                return ResponseUtils.createNotFoundResponse("MainCharge not found with id", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    public ApiResponse<MasSubChargeCodeDTO> updateSubCharge(Long subId, MasSubChargeCodeReq codeReq){
        try{
            Optional<MasSubChargeCode> newSubCode = subRepo.findById(subId);
            if (newSubCode.isPresent()) {
                MasSubChargeCode newCode = newSubCode.get();
                newCode.setSubCode(codeReq.getSubCode());
                newCode.setSubName(codeReq.getSubName());
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                newCode.setLastChgBy(String.valueOf(currentUser.getUserId()));
                newCode.setLastChgDate(LocalDate.now());
                newCode.setLastChgTime(getCurrentTimeFormatted());

                Optional<MasMainChargeCode> mainChargeCode = mainRepo.findById(codeReq.getMainChargeId());
                if (mainChargeCode.isPresent()) {
                    newCode.setMainChargeId(mainChargeCode.get());
                } else {
                    return ResponseUtils.createNotFoundResponse("MainCharge not found with id: " + codeReq.getMainChargeId(), 404);
                }

                return ResponseUtils.createSuccessResponse(toResponse(subRepo.save(newCode)), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createNotFoundResponse("SubCharge is not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    public ApiResponse<MasSubChargeCodeDTO> changeSubChargeStatus(Long subId, String status){
        try{
            Optional<MasSubChargeCode> newSubCode = subRepo.findById(subId);
            if (newSubCode.isPresent()) {
                MasSubChargeCode subCode = newSubCode.get();
                if ("Y".equalsIgnoreCase(status) || "N".equalsIgnoreCase(status)) {
                    subCode.setStatus(status);

                    User currentUser = getCurrentUser();
                    if (currentUser == null) {
                        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                                },
                                "Current user not found", HttpStatus.UNAUTHORIZED.value());
                    }
                    subCode.setLastChgBy(String.valueOf(currentUser.getUserId()));
                    subCode.setLastChgDate(LocalDate.now());
                    subCode.setLastChgTime(getCurrentTimeFormatted());

                    return ResponseUtils.createSuccessResponse(toResponse(subRepo.save(subCode)), new TypeReference<>() {
                    });

                } else {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, "Invalid status. Status should be 'Y' or 'N'", 400);
                }
            } else {
                return ResponseUtils.createNotFoundResponse("MainCharge is not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    public ApiResponse<MasSubChargeCodeDTO> getBySubId(Long subId){
        Optional <MasSubChargeCode> subCode = subRepo.findById(subId);
        if(subCode.isPresent()){
            MasSubChargeCode newSubCode= subCode.get();
            return ResponseUtils.createSuccessResponse(toResponse(newSubCode), new TypeReference<>() {});
        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "SubCharge is not found", 404);
        }
    }

    public ApiResponse<List<MasSubChargeCodeDTO>> getAllSubCharge(int flag){
        List<MasSubChargeCode> subCode;
        if (flag == 1) {
            subCode = subRepo.findByStatus("y");
        } else if (flag == 0) {
            subCode = subRepo.findAll();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasSubChargeCodeDTO> subDTO = subCode.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(subDTO, new TypeReference<>() {});
    }

}
