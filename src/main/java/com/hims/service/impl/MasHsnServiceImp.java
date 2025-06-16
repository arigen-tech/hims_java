package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasHSN;
import com.hims.entity.repository.MasHsnRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasDepartmentResponse;
import com.hims.response.MasHsnResponse;
import com.hims.service.MasHsnService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasHsnServiceImp implements MasHsnService {
    @Autowired
    private MasHsnRepository masHsnRepository;
    @Override
    public ApiResponse<List<MasHsnResponse>> getAllMasStoreItem(int flag) {
        List<MasHSN> masHSN;

        if (flag == 1) {
            masHSN = masHsnRepository.findByStatusIgnoreCase("y");
        } else if (flag == 0) {
            masHSN = masHsnRepository.findByStatusInIgnoreCase(List.of("y", "n"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasHsnResponse> responses = masHSN.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});


    }
    private MasHsnResponse mapToResponse(MasHSN masHSN){
        return null;
    }
}
