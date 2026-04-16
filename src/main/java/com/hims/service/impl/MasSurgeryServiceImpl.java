package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasSurgery;
import com.hims.entity.User;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasSurgeryRepository;
import com.hims.request.MasSurgeryRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasSurgeryResponse;
import com.hims.service.MasSurgeryService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MasSurgeryServiceImpl implements MasSurgeryService {

    private final MasSurgeryRepository repository;
    private final MasDepartmentRepository departmentRepository;
    @Autowired
    private  AuthUtil authUtil;
    @Override
    public ApiResponse<List<MasSurgeryResponse>> getAllMasSurgery(int flag) {
        try {

            List<MasSurgery> list;
            if (flag == 1) {
                list = repository.findActive(AppConstants.STATUS_Y);
            } else if (flag == 0) {
                list = repository.findActiveAndDeactive();
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid flag value", HttpStatus.BAD_REQUEST.value());
            }

            return ResponseUtils.createSuccessResponse(list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error fetching surgery list", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
    @Override
    public ApiResponse<MasSurgeryResponse> getByIdMasSurgery(Long id) {
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Surgery not found", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasSurgeryResponse> createMasSurgery(MasSurgeryRequest request) {
        try {
            User user = authUtil.getCurrentUser();
            MasDepartment dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Invalid Department"));

            MasSurgery entity = MasSurgery.builder()
                    .surgeryCode(request.getSurgeryCode())
                    .surgeryName(request.getSurgeryName())
                    .department(dept)
                    .surgeryLevel(request.getSurgeryLevel())
                    .isAnesthesiaRequired(request.getIsAnesthesiaRequired())
                    .status(AppConstants.STATUS_Y.toLowerCase())
                    .lastUpdatedBy(user.getFullName())
                    .lastUpdatedDate(LocalDateTime.now())
                    .build();

            repository.save(entity);
            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error creating surgery", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Creation failed", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasSurgeryResponse> updateMasSurgery(Long id, MasSurgeryRequest request) {
        try {
            MasSurgery entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Surgery not found", 404);
            }
            User user = authUtil.getCurrentUser();

            MasDepartment dept = departmentRepository.findById(request.getDepartmentId()).orElseThrow(() -> new RuntimeException("Invalid Department"));
            entity.setSurgeryCode(request.getSurgeryCode());
            entity.setSurgeryName(request.getSurgeryName());
            entity.setDepartment(dept);
            entity.setSurgeryLevel(request.getSurgeryLevel());
            entity.setIsAnesthesiaRequired(request.getIsAnesthesiaRequired());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdatedDate(LocalDateTime.now());
            repository.save(entity);
            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Update failed", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasSurgeryResponse> changeStatusMasSurgery(Long id, String status) {
        try {
            MasSurgery entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Surgery not found", 404);
            }

            if (!status.equalsIgnoreCase(AppConstants.STATUS_Y.toLowerCase()) && !status.equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid status", HttpStatus.BAD_REQUEST.value());
            }
            User user = authUtil.getCurrentUser();
            entity.setStatus(status.toLowerCase());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdatedDate(LocalDateTime.now());
            repository.save(entity);
            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Status update failed", 500);
        }
    }

    private MasSurgeryResponse toResponse(MasSurgery e) {

        MasSurgeryResponse res = new MasSurgeryResponse();
        res.setSurgeryId(e.getSurgeryId());
        res.setSurgeryCode(e.getSurgeryCode());
        res.setSurgeryName(e.getSurgeryName());
        res.setDepartmentId(e.getDepartment().getId());
        res.setDepartmentName(e.getDepartment().getDepartmentName());
        res.setSurgeryLevel(e.getSurgeryLevel());
        res.setIsAnesthesiaRequired(e.getIsAnesthesiaRequired());
        res.setStatus(e.getStatus());
        return res;
    }
}