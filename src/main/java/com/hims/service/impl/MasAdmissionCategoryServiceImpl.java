package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasAdmissionCategory;
import com.hims.entity.repository.MasAdmissionCategoryRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasAdmissionCategoryResponse;
import com.hims.service.MasAdmissionCategoryService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasAdmissionCategoryServiceImpl implements MasAdmissionCategoryService {
    @Autowired
    private MasAdmissionCategoryRepository masAdmissionCategoryRepository;
    @Override
    public ApiResponse<List<MasAdmissionCategoryResponse>> getAllMasAdmissionCategory(int flag) {

        log.info("Fetching Admission Category list, flag={}", flag);

        try {
            List<MasAdmissionCategory> list = (flag == 1)
                    ? masAdmissionCategoryRepository.findByStatusIgnoreCaseOrderByAdmissionCategoryNameAsc(AppConstants.STATUS_Y.toLowerCase())
                    : masAdmissionCategoryRepository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error fetching Admission Category list", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
    private MasAdmissionCategoryResponse mapToResponse(MasAdmissionCategory entity) {

        MasAdmissionCategoryResponse response = new MasAdmissionCategoryResponse();

        response.setAdmissionCategoryId(entity.getAdmissionCategoryId());
        response.setAdmissionCategoryName(entity.getAdmissionCategoryName());
        response.setDescription(entity.getDescription());
        response.setStatus(entity.getStatus());
        response.setLastUpdateDate(entity.getLastUpdateDate());
        response.setCreatedBy(entity.getCreatedBy());
        response.setLastUpdatedBy(entity.getLastUpdatedBy());

        return response;
    }
}
