package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasHospitalRepository;
import com.hims.entity.repository.MasOpdMedicalAdviseRepository;
import com.hims.request.MasOpdMedicalAdviseRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOpdMedicalAdviseResponse;
import com.hims.response.MasOutputTypeResponse;
import com.hims.response.UserContext;
import com.hims.service.MasOpdMedicalAdviseService;
import com.hims.service.UserContextService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasOpdMedicalAdviseServiceImpl implements MasOpdMedicalAdviseService {
    @Autowired
    private MasOpdMedicalAdviseRepository masOpdMedicalAdviseRepository;
    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private MasDepartmentRepository masDepartmentRepository;
    @Autowired
    private MasHospitalRepository masHospitalRepository;
    @Autowired
    private UserContextService userContextService;

    @Override
    public ApiResponse<List<MasOpdMedicalAdviseResponse>> getAll(int flag) {
        try {
            List<MasOpdMedicalAdvise> list =
                    (flag == 1)
                            ? masOpdMedicalAdviseRepository.findByStatusIgnoreCaseOrderByMedicalAdviseNameAsc("y")
                            : masOpdMedicalAdviseRepository.findAllByOrderByStatusDescLastUpdateDateDesc();

            List< MasOpdMedicalAdviseResponse> response =
                    list.stream().map(this::toResponse).collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Something went wrong: " + e.getMessage(),
                    500
            );
        }

    }




    @Override
    public ApiResponse<MasOpdMedicalAdviseResponse> create(MasOpdMedicalAdviseRequest request) {
        try {
            UserContext userContext = userContextService.getCurrentUserContext();
            MasDepartment masDepartment = null;
            if (request.getDepartmentId() != null) {
                masDepartment = masDepartmentRepository.findById(request.getDepartmentId()).orElse(null);
            }

            MasOpdMedicalAdvise advise =
                    MasOpdMedicalAdvise.builder()
                            .medicalAdviseName(request.getMedicalAdviceName())
                            .departmentId(masDepartment)
                            .status(AppConstants.STATUS_Y.toLowerCase())
                            .createdBy(userContext.getUserFullName())
                            .lastUpdatedBy(userContext.getUserFullName())
                            .lastUpdateDate(LocalDateTime.now())
                            .build();

            masOpdMedicalAdviseRepository.save(advise);

            return ResponseUtils.createSuccessResponse(
                    toResponse(advise), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasOpdMedicalAdviseResponse> update(Long id, MasOpdMedicalAdviseRequest request) {
        try {
            MasDepartment masDepartment = null;
            if (request.getDepartmentId() != null) {
                masDepartment = masDepartmentRepository.findById(request.getDepartmentId()).orElse(null);
            }
            MasOpdMedicalAdvise advise = masOpdMedicalAdviseRepository.findById(id).orElse(null);
            if (advise == null)
                return ResponseUtils.createNotFoundResponse("Medical advice not found", 404);

            UserContext userContext = userContextService.getCurrentUserContext();
            advise.setMedicalAdviseName(request.getMedicalAdviceName());
            advise.setDepartmentId( masDepartment);
            advise.setLastUpdatedBy(userContext.getUserFullName());
            advise.setLastUpdateDate(LocalDateTime.now());

            masOpdMedicalAdviseRepository.save(advise);

            return ResponseUtils.createSuccessResponse(
                    toResponse(advise), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasOpdMedicalAdviseResponse> changeStatus(Long id, String status) {
        try {
            MasOpdMedicalAdvise advise =
                    masOpdMedicalAdviseRepository.findById(id).orElse(null);

            if (advise == null)
                return ResponseUtils.createNotFoundResponse(
                        "Medical advice not found", 404);

            if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n"))
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status", 400);

            UserContext userContext = userContextService.getCurrentUserContext();

            advise.setStatus(status);
            advise.setLastUpdatedBy(userContext.getUserFullName());
            advise.setLastUpdateDate(LocalDateTime.now());

            masOpdMedicalAdviseRepository.save(advise);

            return ResponseUtils.createSuccessResponse(
                    toResponse(advise), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasOpdMedicalAdviseResponse> getById(Long id) {
        MasOpdMedicalAdvise obj = masOpdMedicalAdviseRepository.findById(id).orElse(null);

        if (obj == null)
            return ResponseUtils.createNotFoundResponse("Mas Opd Medical Advise ID not found", 404);

        return ResponseUtils.createSuccessResponse(toResponse(obj), new TypeReference<>() {});

    }

    private MasOpdMedicalAdviseResponse toResponse(MasOpdMedicalAdvise m) {
        MasOpdMedicalAdviseResponse response=new MasOpdMedicalAdviseResponse();
        response.setMedicalAdviseId(m.getMedicalAdviseId());
        response.setMedicalAdviseName(m.getMedicalAdviseName());

        response .setDepartmentId(m.getDepartmentId() != null ? m.getDepartmentId().getId() : null);

        response  .setDepartmentName(m.getDepartmentId() != null ? m.getDepartmentId().getDepartmentName() : null);

        response.setStatus(m.getStatus());
        response.setLastUpdateDate(m.getLastUpdateDate());
        return response;

    }
}
