package com.hims.mapper;

import com.hims.entity.MasConsentType;
import com.hims.request.MasConsentTypeRequest;
import com.hims.response.MasConsentTypeResponse;
import org.springframework.stereotype.Component;

@Component
public class MasConsentTypeMapper {
    public void updateEntity(MasConsentTypeRequest request, MasConsentType entity) {
        entity.setConsentTypeCode(request.getConsentTypeCode().trim());
        entity.setConsentTypeName(request.getConsentTypeName().trim());
    }

    public MasConsentTypeResponse toResponse(MasConsentType entity) {
        MasConsentTypeResponse response = new MasConsentTypeResponse();
        response.setConsentTypeId(entity.getConsentTypeId());
        response.setConsentTypeCode(entity.getConsentTypeCode());
        response.setConsentTypeName(entity.getConsentTypeName());
        response.setStatus(entity.getStatus());
        response.setLastChgBy(entity.getLastChgBy());
        response.setLastChgDate(entity.getLastChgDate());
        return response;
    }
}
