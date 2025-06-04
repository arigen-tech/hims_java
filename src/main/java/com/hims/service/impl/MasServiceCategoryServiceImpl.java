package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasServiceCategory;
import com.hims.entity.repository.MasServiceCategoryRepository;
import com.hims.response.ApiResponse;
import com.hims.service.MasServiceCategoryService;
import com.hims.utils.ResponseUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MasServiceCategoryServiceImpl implements MasServiceCategoryService {

    private final MasServiceCategoryRepository masServiceCategoryRepository;

    public MasServiceCategoryServiceImpl(MasServiceCategoryRepository masServiceCategoryRepository) {
        this.masServiceCategoryRepository = masServiceCategoryRepository;
    }

    @Override
    public ApiResponse<List<MasServiceCategory>> findAll(int flag) {
        List<MasServiceCategory> response=new ArrayList<>();
        if(flag==1){

            response = masServiceCategoryRepository.findAllByStatus("y");
        }else{
            response = masServiceCategoryRepository.findAll();
        }
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>(){});
    }
}
