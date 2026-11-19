package com.hims.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.repository.MasBloodDiscardReasonRepository;
import com.hims.exception.SDDException;
import com.hims.response.ApiResponse;
import com.hims.response.MasBloodDiscardReasonRes;
import com.hims.service.MasBloodDiscardReasonService;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasBloodDiscardReasonServiceImpl implements MasBloodDiscardReasonService {


    private  final MasBloodDiscardReasonRepository masBloodDiscardReasonRepository;

    @Override
    public ApiResponse<List<MasBloodDiscardReasonRes>> getAllReasons(int flag) {

        try {
            log.info("getAllReasons method started with flag - {}",flag);
            if (flag != 0 && flag != 1) {
                throw new SDDException(
                        "flag",
                        HttpStatus.BAD_REQUEST.value(),
                        "Invalid flag. Please provide 0 or 1."
                );
            }
            List<MasBloodDiscardReasonRes> allDiscardReasons
                    = masBloodDiscardReasonRepository.getAllDiscardReasons(flag);

            log.info("getAllReasons method ended with flag - {}",flag);
            return ResponseUtils.createSuccessResponse(allDiscardReasons, new TypeReference<>() {});

        }catch (SDDException e){
            log.info("getAllReasons methods error :: ",e);
            return  ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    e.getMessage(),
                    e.getStatus()
            );
        }catch (Exception e){
            log.info("getAllReasons methods error :: ",e);
            return  ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
}
