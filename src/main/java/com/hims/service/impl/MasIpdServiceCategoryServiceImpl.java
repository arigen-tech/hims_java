package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasIpdServiceCategory;
import com.hims.entity.repository.MasIpdServiceCategoryRepository;
import com.hims.response.ApiResponse;
import com.hims.response.IpdServiceCategoryResponse;
import com.hims.service.MasIpdServiceCategoryService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasIpdServiceCategoryServiceImpl implements MasIpdServiceCategoryService {
    @Autowired
    private MasIpdServiceCategoryRepository masIpdServiceCategoryRepository;
    @Override
    public ApiResponse<List<IpdServiceCategoryResponse>> getAll(int flag) {
        log.info("Fetching IPD Service Category list, flag={}", flag);
        try {
            List<MasIpdServiceCategory> list =
                    (flag == 1)
                            ? masIpdServiceCategoryRepository.findByStatusIgnoreCaseOrderByCategoryNameAsc(AppConstants.STATUS_Y.toLowerCase())
                            : masIpdServiceCategoryRepository.findAllByOrderByStatusDescLastChgDateDesc();

            return ResponseUtils.createSuccessResponse(list.stream().map(this::toResponse).toList(), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error fetching IPD Service Category list", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
    private IpdServiceCategoryResponse toResponse(MasIpdServiceCategory entity) {

        IpdServiceCategoryResponse response = new IpdServiceCategoryResponse();
        response.setCategoryId(entity.getCategoryId());
        response.setCategoryCode(entity.getCategoryCode());
        response.setCategoryName(entity.getCategoryName());
        response.setDisplayOrder(entity.getDisplayOrder());
        response.setIsSubcategoryRequired(entity.getIsSubcategoryRequired());
        response.setGstApplicable(entity.getGstApplicable());
        response.setGstPercentage(entity.getGstPercentage());
        response.setStatus(entity.getStatus());
        response.setLastUpdate(entity.getLastChgDate());
        return response;
    }
}
