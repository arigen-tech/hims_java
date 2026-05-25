package com.hims.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasItemFacility;
import com.hims.entity.User;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasItemFacilityRepository;
import com.hims.request.MasItemFacilityRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasItemFacilityResponse;
import com.hims.service.MasItemFacilityService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import kong.unirest.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasItemFacilityServiceImpl implements MasItemFacilityService {
@Autowired
    private MasItemFacilityRepository repository;
    @Autowired
    private MasDepartmentRepository departmentRepository;
    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasItemFacilityResponse>> getAllFacility(int flag) {

        try {

            List<MasItemFacility> list = (flag == 1)
                    ? repository.findByStatusIgnoreCaseOrderByFacilityNameAsc(
                    AppConstants.STATUS_Y.toLowerCase())
                    : repository.findAllByOrderByLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error fetching facility list", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ApiResponse<MasItemFacilityResponse> getFacilityById(Long id) {

        try {

            return repository.findById(id)
                    .map(entity -> ResponseUtils.createSuccessResponse(
                            mapToResponse(entity),
                            new TypeReference<>() {}
                    ))
                    .orElse(ResponseUtils.createNotFoundResponse("Facility not found", HttpStatus.NOT_FOUND
                    ));

        } catch (Exception e) {

            log.error("Error fetching facility by id", e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, 500
            );
        }
    }

    @Override
    public ApiResponse<MasItemFacilityResponse> createFacility(
            MasItemFacilityRequest request) {

        try {

            User user = authUtil.getCurrentUser();

            MasItemFacility entity = new MasItemFacility();
            entity.setFacilityCode(request.getFacilityCode());
            entity.setFacilityName(request.getFacilityName());
            MasDepartment department = departmentRepository.findById(request.getDepartmentId()).orElse(null);
            entity.setDepartment(department);
            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setCreatedBy(user.getFullName());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error creating facility", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ApiResponse<MasItemFacilityResponse> updateFacility(
            Long id,
            MasItemFacilityRequest request) {

        try {

            MasItemFacility entity = repository.findById(id).orElse(null);

            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Facility not found",
                        HttpStatus.NOT_FOUND);
            }

            User user = authUtil.getCurrentUser();

            entity.setFacilityCode(request.getFacilityCode());
            entity.setFacilityName(request.getFacilityName());

            if (request.getDepartmentId() != null) {

                MasDepartment department = departmentRepository.findById(request.getDepartmentId()).orElse(null);

                entity.setDepartment(department);
            } else {

                entity.setDepartment(null);
            }
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());
            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error updating facility", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public ApiResponse<MasItemFacilityResponse> changeStatus(
            Long id,
            String status) {

        try {

            MasItemFacility entity = repository.findById(id).orElse(null);

            if (entity == null) {

                return ResponseUtils.createNotFoundResponse("Facility not found",
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

            User user = authUtil.getCurrentUser();
            entity.setStatus(status.toLowerCase());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());
            repository.save(entity);

            return ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {}
            );

        } catch (Exception e) {

            log.error("Error changing facility status", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private MasItemFacilityResponse mapToResponse(MasItemFacility entity) {

        MasItemFacilityResponse response = new MasItemFacilityResponse();
        response.setFacilityId(entity.getFacilityId());
        response.setFacilityCode(entity.getFacilityCode());
        response.setFacilityName(entity.getFacilityName());
        response.setDepartmentId(entity.getDepartment()!=null?entity.getDepartment().getId():null);
        response.setDepartmentName(entity.getDepartment()!=null?entity.getDepartment().getDepartmentName():null);
        response.setStatus(entity.getStatus());
        return response;
    }
}