package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasBloodCompatibility;
import com.hims.entity.MasLanguage;
import com.hims.entity.repository.MasLanguageRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasLanguageResponse;
import com.hims.service.MasLanguageService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasLanguageServiceImpl implements MasLanguageService {
    @Autowired
    private MasLanguageRepository masLanguageRepository;

    @Override
    public ApiResponse<List<MasLanguageResponse>> getAll(int flag) {
        try {
            List<MasLanguage> list =
                    (flag == 1)
                            ? masLanguageRepository.findByStatusIgnoreCaseOrderByLanguageNameAsc("y")
                            : masLanguageRepository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching mas language list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to fetch mas language list", 500);
        }
    }
    private MasLanguageResponse toResponse(MasLanguage masLanguage){
        MasLanguageResponse masLanguageResponse=new MasLanguageResponse();
        masLanguageResponse.setId(masLanguage.getLanguageId());
        masLanguageResponse.setLanguage(masLanguage.getLanguageName());
        return masLanguageResponse;
    }
}
