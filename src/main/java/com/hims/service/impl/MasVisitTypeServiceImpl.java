package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasVisitType;
import com.hims.entity.repository.MasVisitTypeRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasVisitTypeResponse;
import com.hims.service.MasVisitTypeService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasVisitTypeServiceImpl implements MasVisitTypeService {
    @Autowired
    private MasVisitTypeRepository masVisitTypeRepository;

    @Override
    public ApiResponse<List<MasVisitTypeResponse>> getAll(int flag) {
        try {
            log.info("getAll() method Started...");

            List<MasVisitType> list;

            if (flag == 0) {
                list = masVisitTypeRepository.findAllByOrderByStatusDescLastChangedDateDesc();
            } else if (flag == 1) {
                list = masVisitTypeRepository.findByStatusIgnoreCaseOrderByVisitTypeNameAsc(AppConstants.STATUS_Y.toLowerCase());
            } else {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid Flag Value , Provide flag as 0 or 1",
                        HttpStatus.BAD_REQUEST.value());
            }

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("getAll() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
    private MasVisitTypeResponse mapToResponse(MasVisitType entity) {
        if (entity == null) return null;

        MasVisitTypeResponse response = new MasVisitTypeResponse();
        response.setVisitTypeId(entity.getVisitTypeId());
        response.setVisitTypeCode(entity.getVisitTypeCode());
        response.setVisitTypeName(entity.getVisitTypeName());
        response.setDescription(entity.getDescription());
        response.setStatus(entity.getStatus());
        response.setLastChangedBy(entity.getLastChangedBy());
        response.setLastChangedDate(entity.getLastChangedDate());

        return response;
    }

}
