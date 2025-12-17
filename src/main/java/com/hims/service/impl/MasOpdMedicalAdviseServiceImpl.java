package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasOpdMedicalAdvise;
import com.hims.entity.MasOutputType;
import com.hims.entity.repository.MasOpdMedicalAdviseRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasOpdMedicalAdviseResponse;
import com.hims.response.MasOutputTypeResponse;
import com.hims.service.MasOpdMedicalAdviseService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasOpdMedicalAdviseServiceImpl implements MasOpdMedicalAdviseService {
    @Autowired
    private MasOpdMedicalAdviseRepository masOpdMedicalAdviseRepository;
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
    private MasOpdMedicalAdviseResponse toResponse(MasOpdMedicalAdvise m) {
        MasOpdMedicalAdviseResponse response=new MasOpdMedicalAdviseResponse();
        response.setMedicalAdviseId(m.getMedicalAdviseId());
        response.setMedicalAdviseName(m.getMedicalAdviseName());
        response.setDepartmentId(m.getDepartmentId().getId());
        response.setDepartmentName(m.getDepartmentId().getDepartmentName());
        response.setStatus(m.getStatus());
        response.setCreatedBy(m.getCreatedBy());
        return response;

    }
}
