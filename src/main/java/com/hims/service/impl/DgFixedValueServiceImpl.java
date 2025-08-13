package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.DgFixedValue;
import com.hims.entity.repository.DgFixedValueRepository;
import com.hims.response.ApiResponse;
import com.hims.service.DgFixedValueService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DgFixedValueServiceImpl implements DgFixedValueService {
    @Autowired
    private DgFixedValueRepository dgFixedValueRepository;
    @Override
    public ApiResponse<List<DgFixedValue>> getDgFixedValue() {
        List<DgFixedValue> list = dgFixedValueRepository.findAll();
        return ResponseUtils.createSuccessResponse(list, new TypeReference<>() {});

    }


}
