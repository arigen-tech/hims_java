package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasReceiptType;
import com.hims.entity.repository.MasReceiptTypeRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasReceiptTypeResponse;
import com.hims.service.MasReceiptTypeService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasReceiptTypeServiceImpl implements MasReceiptTypeService {

    @Autowired
    private MasReceiptTypeRepository repository;

    @Override
    public ApiResponse<List<MasReceiptTypeResponse>> getAll(int flag) {

        log.info("Fetching receipt type list, flag={}", flag);

        try {

            List<MasReceiptType> receiptTypes = (flag == 1)
                    ? repository.findByStatusIgnoreCaseOrderByReceiptTypeNameAsc(AppConstants.STATUS_Y.toLowerCase())
                    : repository.findAllByOrderByStatusDescUpdatedAtDesc();

            List<MasReceiptTypeResponse> response = receiptTypes.stream()
                    .map(this::mapToResponse)
                    .toList();

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {

            log.error("Error fetching receipt type list, flag={}", flag, e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );
        }
    }

    private MasReceiptTypeResponse mapToResponse(MasReceiptType entity) {

        return MasReceiptTypeResponse.builder()
                .receiptTypeId(entity.getReceiptTypeId())
                .receiptTypeCode(entity.getReceiptTypeCode())
                .receiptTypeName(entity.getReceiptTypeName())
                .build();
    }
}