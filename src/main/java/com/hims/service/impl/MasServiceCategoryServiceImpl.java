package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasServiceCategory;
import com.hims.entity.repository.MasServiceCategoryRepository;
import com.hims.response.ApiResponse;
import com.hims.service.MasServiceCategoryService;
import com.hims.utils.ResponseUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MasServiceCategoryServiceImpl implements MasServiceCategoryService {

    private final MasServiceCategoryRepository masServiceCategoryRepository;

    public MasServiceCategoryServiceImpl(MasServiceCategoryRepository masServiceCategoryRepository) {
        this.masServiceCategoryRepository = masServiceCategoryRepository;
    }

    @Override
    public ApiResponse<List<MasServiceCategory>> findAll(int flag) {
        List<MasServiceCategory> response;
        if(flag==1){

            response = masServiceCategoryRepository.findAllByStatus("y");
        }else{
            response = masServiceCategoryRepository.findAll();
        }
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>(){});
    }
    @Override
    public ApiResponse<MasServiceCategory> save(MasServiceCategory req) {
        try {
            MasServiceCategory response = masServiceCategoryRepository.save(req);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>(){});
        }catch (Exception e){
            return ResponseUtils.createFailureResponse(req, new TypeReference<>(){},"Error Saving Data",500);
        }

    }

    @Override
    public ApiResponse<MasServiceCategory> edit(MasServiceCategory req) {
        try {
            MasServiceCategory response = masServiceCategoryRepository.save(req);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>(){});
        }catch (Exception e){
            return ResponseUtils.createFailureResponse(req, new TypeReference<>(){},"Error Saving Data",500);
        }
    }
}
