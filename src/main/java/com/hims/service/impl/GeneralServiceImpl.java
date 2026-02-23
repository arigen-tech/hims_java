package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasSubChargeCode;
import com.hims.entity.repository.MasSubChargeCodeRepository;
import com.hims.projection.ModalityDetailsProjection;
import com.hims.response.ApiResponse;
import com.hims.response.ModalityDetailsByDepartmentResponse;
import com.hims.service.GeneralService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class GeneralServiceImpl implements GeneralService {
    @Autowired
    private MasSubChargeCodeRepository masSubChargeCodeRepository;

    @Override
    public ApiResponse<List<ModalityDetailsByDepartmentResponse>> getModalityDetailsByDepartment(String code) {

            log.info("getModalityDetailsByDepartment called with deptCode={}", code);

            try {
                if (code == null || code.trim().isEmpty()) {
                    return ResponseUtils.createFailureResponse(
                            List.of(),
                            new TypeReference<List<ModalityDetailsByDepartmentResponse>>() {}, "department code is required",
                            400
                    );
                }

                String deptCode = code.trim();
                List<ModalityDetailsProjection> list =
                        masSubChargeCodeRepository.findModalityByDepartmentCode(deptCode, AppConstants.STATUS_Y);

                List<ModalityDetailsByDepartmentResponse> resp = list.stream()
                        .map(s -> {
                            ModalityDetailsByDepartmentResponse dto = new ModalityDetailsByDepartmentResponse();
                            dto.setId(s.getId());
                            dto.setModalityName(s.getModalityName());
                            return dto;
                        })
                        .toList();

                log.info("Modality details fetched successfully. deptCode={}, count={}", deptCode, resp.size());
                return ResponseUtils.createSuccessResponse(resp,
                        new TypeReference<List<ModalityDetailsByDepartmentResponse>>() {}
                );

            } catch (Exception e) {
                log.error("Error in getModalityDetailsByDepartment, deptCode={}", code, e);
                return ResponseUtils.createFailureResponse(
                        List.of(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<ModalityDetailsByDepartmentResponse>>() {},
                        "Something went wrong while fetching modality details",
                        500
                );
            }
        }
    }

