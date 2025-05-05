package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasMainChargeCode;
import com.hims.entity.User;
import com.hims.entity.repository.MasMainChargeCodeRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasMainChargeCodeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasMainChargeCodeDTO;
import com.hims.service.MasMainChargeCodeService;
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
public class MasMainChargeCodeServicesImpl implements MasMainChargeCodeService {

    private static final Logger log = LoggerFactory.getLogger(MasMainChargeCodeServicesImpl.class);

    @Autowired
    private MasMainChargeCodeRepository masMainChargeCodeRepository;

    @Autowired
    UserRepo userRepo;
    private String getCurrentTimeFormatted() {

        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private MasMainChargeCodeDTO toResponse(MasMainChargeCode code) {
        MasMainChargeCodeDTO dto = new MasMainChargeCodeDTO();
        dto.setChargecodeId(code.getChargecodeId());
        dto.setChargecodeCode(code.getChargecodeCode());
        dto.setChargecodeName(code.getChargecodeName());
        dto.setStatus(code.getStatus());
        dto.setLastChgBy(code.getLastChgBy());
        dto.setLastChgDate(code.getLastChgDate());
        dto.setLastChgTime(code.getLastChgTime());

        return dto;
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
    public ApiResponse<List<MasMainChargeCodeDTO>> getAllChargeCode(int flag) {
        List<MasMainChargeCode> charge;

        if (flag == 1) {
            charge = masMainChargeCodeRepository.findByStatus("y");
        } else if (flag == 0) {
            charge = masMainChargeCodeRepository.findAll();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasMainChargeCodeDTO> codeDTOS = charge.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(codeDTOS, new TypeReference<>() {});
    }


    @Override
    public ApiResponse<MasMainChargeCodeDTO> getChargeCodeById(Long chargecodeId) {
        Optional<MasMainChargeCode> code = masMainChargeCodeRepository.findById(chargecodeId);
        return code.map(value ->
                ResponseUtils.createSuccessResponse(
                        toResponse(value),
                        new TypeReference<>() {}
                )
        ).orElseGet(() -> ResponseUtils.createNotFoundResponse("Main Code not found", 404));
    }


    @Override
    public ApiResponse<MasMainChargeCodeDTO> createChargeCode(MasMainChargeCodeRequest codeRequest) {
        try{
            MasMainChargeCode chargeCode = new MasMainChargeCode();

                chargeCode.setChargecodeCode(codeRequest.getChargecode_code());
                chargeCode.setChargecodeName(codeRequest.getChargecode_name());
                chargeCode.setStatus("y");
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                chargeCode.setLastChgBy(String.valueOf(currentUser.getUserId()));
                chargeCode.setLastChgDate(LocalDate.now());
                chargeCode.setLastChgTime(getCurrentTimeFormatted());
                return ResponseUtils.createSuccessResponse(toResponse(masMainChargeCodeRepository.save(chargeCode)), new TypeReference<>() {});
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @Override
    public ApiResponse<MasMainChargeCodeDTO> updateChargeCode(Long chargecodeId, MasMainChargeCodeRequest codeRequest) {
        try{
            Optional<MasMainChargeCode> optionalCode = masMainChargeCodeRepository.findById(chargecodeId);

            if (optionalCode.isPresent()) {
                MasMainChargeCode chargeCode = optionalCode.get();
                chargeCode.setChargecodeCode(codeRequest.getChargecode_code());
                chargeCode.setChargecodeName(codeRequest.getChargecode_name());
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                chargeCode.setLastChgBy(String.valueOf(currentUser.getUserId()));
                chargeCode.setLastChgDate(LocalDate.now());
                chargeCode.setLastChgTime(getCurrentTimeFormatted());

                return ResponseUtils.createSuccessResponse(toResponse(masMainChargeCodeRepository.save(chargeCode)), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "MainCharge data not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasMainChargeCodeDTO> changeStatus(Long chargecodeId, String status) {
        try{
            Optional<MasMainChargeCode> existingCodeOpt = masMainChargeCodeRepository.findById(chargecodeId);
            if (existingCodeOpt.isPresent()) {
                MasMainChargeCode codes = existingCodeOpt.get();

                if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                    return ResponseUtils.createFailureResponse(
                            null,
                            new TypeReference<>() {
                            },
                            "Invalid status value. Use 'Y' for Active and 'N' for Inactive.",
                            400
                    );
                }
                codes.setStatus(status);

                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                codes.setLastChgBy(String.valueOf(currentUser.getUserId()));
                codes.setLastChgDate(LocalDate.now());
                codes.setLastChgTime(getCurrentTimeFormatted());

                MasMainChargeCode existingCode = masMainChargeCodeRepository.save(codes);

                return ResponseUtils.createSuccessResponse(
                        toResponse(existingCode),
                        new TypeReference<>() {
                        }
                );
            } else {
                return ResponseUtils.createNotFoundResponse("Main Code not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
