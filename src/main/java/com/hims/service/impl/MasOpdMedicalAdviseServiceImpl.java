package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasOpdMedicalAdvise;
import com.hims.entity.MasOutputType;
import com.hims.entity.User;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasOpdMedicalAdviseRepository;
import com.hims.request.MasOpdMedicalAdviseRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOpdMedicalAdviseResponse;
import com.hims.response.MasOutputTypeResponse;
import com.hims.service.MasOpdMedicalAdviseService;
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
            User user = authUtil.getCurrentUser();
            Optional<MasDepartment> masDepartment= masDepartmentRepository.findById(request.getDepartmentId());

            MasOpdMedicalAdvise advise =
                    MasOpdMedicalAdvise.builder()
                            .medicalAdviseName(request.getMedicalAdviceName())
                            .departmentId(masDepartment.orElse(null))
                            .status("y")
                            .createdBy(user.getFirstName())
                            .lastUpdatedBy(user.getFirstName())
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
            MasOpdMedicalAdvise advise =
                    masOpdMedicalAdviseRepository.findById(id).orElse(null);
            Optional<MasDepartment> masDepartment= masDepartmentRepository.findById(request.getDepartmentId());

            if (advise == null)
                return ResponseUtils.createNotFoundResponse(
                        "Medical advice not found", 404);

            User user = authUtil.getCurrentUser();

            advise.setMedicalAdviseName(request.getMedicalAdviceName());
            advise.setDepartmentId( masDepartment.orElse(null));
            advise.setLastUpdatedBy(user.getFirstName());
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

            if (!status.equals("y")
                    && !status.equals("n"))
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status", 400);

            User user = authUtil.getCurrentUser();

            advise.setStatus(status);
            advise.setLastUpdatedBy(user.getFirstName());
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

    private MasOpdMedicalAdviseResponse toResponse(MasOpdMedicalAdvise m) {
        MasOpdMedicalAdviseResponse response=new MasOpdMedicalAdviseResponse();
        response.setMedicalAdviseId(m.getMedicalAdviseId());
        response.setMedicalAdviseName(m.getMedicalAdviseName());
        response.setDepartmentId(m.getDepartmentId().getId());
        response.setDepartmentName(m.getDepartmentId().getDepartmentName());
        response.setStatus(m.getStatus());
        response.setLastUpdateDate(m.getLastUpdateDate());
        return response;

    }
}
