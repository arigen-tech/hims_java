package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasMainChargeCode;
import com.hims.entity.MasOpdSession;
import com.hims.entity.repository.MasMainChargeCodeRepository;
import com.hims.request.MasMainChargeCodeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasMainChargeCodeDTO;
import com.hims.response.MasOpdSessionResponse;
import com.hims.service.MasMainChargeCodeService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MasMainChargeCodeServicesImpl implements MasMainChargeCodeService {

    private static final Logger log = LoggerFactory.getLogger(MasMainChargeCodeServicesImpl.class);

    @Autowired
    MasMainChargeCodeRepository masMainChargeCodeRepository;

//    only use this and the commented part when need to connect department id from the table
//    @Autowired
//    MasDepartmentRepository departmentRepository;

    @Override
    public ApiResponse<List<MasMainChargeCodeDTO>> getAllChargeCode(int flag) {
        List<MasMainChargeCode> charge;

        if (flag == 1) {
            charge = masMainChargeCodeRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            charge = masMainChargeCodeRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasMainChargeCodeDTO> codeDTOS = charge.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(codeDTOS, new TypeReference<>() {});
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

    @Override
    public ApiResponse<MasMainChargeCodeDTO> getChargeCodeById(Long chargecodeId) {
        Optional<MasMainChargeCode> code = masMainChargeCodeRepository.findById(chargecodeId);
        return code.map(value ->
                ResponseUtils.createSuccessResponse(
                        toResponse(value),
                        new TypeReference<MasMainChargeCodeDTO>() {}
                )
        ).orElseGet(() -> ResponseUtils.createNotFoundResponse("Session not found", 404));
    }

    @Override
    public ApiResponse<MasMainChargeCodeDTO> getByStatus(String status) {
        return null;
    }

    @Override
    public ApiResponse<MasMainChargeCodeDTO> createChargeCode(MasMainChargeCodeRequest codeRequest) {
        MasMainChargeCode chargeCode = new MasMainChargeCode();
//
        chargeCode.setChargecodeCode(codeRequest.getChargecode_code());
        chargeCode.setChargecodeName(codeRequest.getChargecode_name());

        MasMainChargeCode mainCode = masMainChargeCodeRepository.save(chargeCode);
        return ResponseUtils.createSuccessResponse(toResponse(mainCode), new TypeReference<MasMainChargeCodeDTO>() {});
    }

    @Override
    public ApiResponse<MasMainChargeCodeDTO> updateChargeCode(Long chargecodeId, MasMainChargeCodeRequest codeRequest) {
        Optional<MasMainChargeCode> optionalCode = masMainChargeCodeRepository.findById(chargecodeId);

        if(optionalCode.isPresent()){
            MasMainChargeCode chargeCode = optionalCode.get();
            chargeCode.setChargecodeCode(codeRequest.getChargecode_code());
            chargeCode.setChargecodeName(codeRequest.getChargecode_name());

            masMainChargeCodeRepository.save(chargeCode);
            return ResponseUtils.createSuccessResponse(toResponse(chargeCode), new TypeReference<MasMainChargeCodeDTO>() {});
        }
        return ResponseUtils.createNotFoundResponse("Session not found", HttpStatus.NOT_FOUND.value());
    }

    @Override
    public ApiResponse<MasMainChargeCodeDTO> changeStatus(Long chargecodeId, String status) {
        Optional<MasMainChargeCode> existingCodeOpt = masMainChargeCodeRepository.findById(chargecodeId);
        if (existingCodeOpt.isPresent()) {
            MasMainChargeCode codes = existingCodeOpt.get();

            if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<MasMainChargeCodeDTO>() {
                        },
                        "Invalid status value. Use 'Y' for Active and 'N' for Inactive.",
                        400
                );
            }

            codes.setStatus(status);
            MasMainChargeCode existingCode = masMainChargeCodeRepository.save(codes);

            return ResponseUtils.createSuccessResponse(
                    toResponse(existingCode),
                    new TypeReference<>() {
                    }
            );
        } else {
            return ResponseUtils.createNotFoundResponse("Session not found", 404);
        }
    }
}
