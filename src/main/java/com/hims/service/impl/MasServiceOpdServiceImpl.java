package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasServiceOpd;
import com.hims.entity.repository.MasHospitalRepository;
import com.hims.entity.repository.MasServiceOpdRepository;
import com.hims.response.ApiResponse;
import com.hims.service.MasServiceOpdService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class MasServiceOpdServiceImpl implements MasServiceOpdService {
    @Autowired
    MasServiceOpdRepository masServiceOpdRepository;
    @Autowired
    private MasHospitalRepository masHospitalRepository;


    @Override
    public ApiResponse<List<MasServiceOpd>> findByHospitalId(Long id) {

        List<MasServiceOpd> response= masServiceOpdRepository.findByHospitalId(masHospitalRepository.findById(id).get());
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasServiceOpd> save(MasServiceOpd req) {
        try {
            MasServiceOpd response = masServiceOpdRepository.save(req);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>(){});
        }catch (Exception e){
            return ResponseUtils.createFailureResponse(req, new TypeReference<>(){},"Error Saving Data",500);
        }
    }

    @Override
    public ApiResponse<MasServiceOpd> edit(MasServiceOpd req) {
        try {
            MasServiceOpd response = masServiceOpdRepository.save(req);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>(){});
        }catch (Exception e){
            return ResponseUtils.createFailureResponse(req, new TypeReference<>(){},"Error Saving Data",500);
        }
    }
}
