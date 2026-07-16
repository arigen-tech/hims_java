package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasIpNursingAssessmentValue;
import com.hims.entity.repository.MasIpNursingAssessmentValueRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasIpNursingAssessmentValueResponse;
import com.hims.service.MasIpNursingAssessmentValueService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasIpNursingAssessmentValueServiceImpl implements MasIpNursingAssessmentValueService {
    @Autowired
    MasIpNursingAssessmentValueRepository masIpNursingAssessmentValueRepository;

    @Override
    public ApiResponse<List<MasIpNursingAssessmentValueResponse>> getAll(int flag) {

        log.info("Fetching IP Nursing Assessment Value list, flag={}", flag);

        try {
            List<MasIpNursingAssessmentValue> list = (flag == 1)
                    ? masIpNursingAssessmentValueRepository
                    .findByStatusIgnoreCaseOrderByCategoryCodeAscDisplayOrderAsc(
                            AppConstants.STATUS_Y.toLowerCase()
                    )
                    : masIpNursingAssessmentValueRepository
                    .findAllByOrderByStatusDescCategoryCodeAscDisplayOrderAsc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToNursingAssessmentValueResponse).toList(), new TypeReference<>() {});

        } catch (Exception e) {

            log.error("Error fetching IP Nursing Assessment Value list, flag={}", flag   );

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    500
            );

    }}

        public MasIpNursingAssessmentValueResponse mapToNursingAssessmentValueResponse(MasIpNursingAssessmentValue entity) {

            return MasIpNursingAssessmentValueResponse.builder()
                    .assessmentValueId(entity.getAssessmentValueId())
                    .categoryCode(entity.getCategoryCode())
                    .valueCode(entity.getValueCode())
                    .valueName(entity.getValueName())
                    .displayOrder(entity.getDisplayOrder())
                    .status(entity.getStatus())
                    .build();
        }
}
