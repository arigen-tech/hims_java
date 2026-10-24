package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasOtType;
import com.hims.entity.repository.MasOtTypeRepository;
import com.hims.mapper.MasOtTypeMapper;
import com.hims.response.ApiResponse;
import com.hims.response.MasOtTypeResponse;
import com.hims.service.MasOtTypeService;
import com.hims.utils.ResponseUtils;
import kong.unirest.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasOtTypeServiceImpl implements MasOtTypeService {

    @Autowired
    private MasOtTypeRepository masOtTypeRepository;

    @Autowired
    private MasOtTypeMapper masOtTypeMapper;

    @Override
    public ApiResponse<List<MasOtTypeResponse>> getAllOtTypes(int flag) {
        log.info("Fetching OT Type list, flag={}", flag);
        try {
            List<MasOtType> list;
            if (flag == 1) {
                list = masOtTypeRepository.findByStatusIgnoreCaseOrderByOtTypeNameAsc(AppConstants.STATUS_Y);
            } else {
                list = masOtTypeRepository.findAllByOrderByStatusDescOtTypeNameAsc();
            }

            List<MasOtTypeResponse> response = masOtTypeMapper.toResponseList(list);

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching OT Type list", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ApiResponse<MasOtTypeResponse> getById(Long id) {
        log.info("Fetching OT Type by id: {}", id);
        try {
            MasOtType entity = masOtTypeRepository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "OT Type not found",
                        HttpStatus.NOT_FOUND
                );
            }
            return ResponseUtils.createSuccessResponse(masOtTypeMapper.toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching OT Type id: {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
