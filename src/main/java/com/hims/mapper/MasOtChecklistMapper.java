package com.hims.mapper;

import com.hims.entity.MasOtChecklist;
import com.hims.request.MasOtChecklistRequest;
import com.hims.response.MasOtChecklistResponse;

public final class MasOtChecklistMapper {
    private MasOtChecklistMapper() {}

    public static void updateEntity(MasOtChecklistRequest request, MasOtChecklist entity) {
        entity.setChecklistStage(request.getChecklistStage().trim().toUpperCase());
        entity.setChecklistCode(request.getChecklistCode().trim());
        entity.setChecklistName(request.getChecklistName().trim());
        entity.setIsMandatory(flagOrDefault(request.getIsMandatory()));
        entity.setAllowNa(flagOrDefault(request.getAllowNa()));
        entity.setDisplayOrder(request.getDisplayOrder());
    }

    public static MasOtChecklistResponse toResponse(MasOtChecklist entity) {
        MasOtChecklistResponse response = new MasOtChecklistResponse();
        response.setChecklistId(entity.getChecklistId());
        response.setChecklistStage(entity.getChecklistStage());
        response.setChecklistCode(entity.getChecklistCode());
        response.setChecklistName(entity.getChecklistName());
        response.setIsMandatory(entity.getIsMandatory());
        response.setAllowNa(entity.getAllowNa());
        response.setDisplayOrder(entity.getDisplayOrder());
        response.setStatus(entity.getStatus());
        response.setLastChgBy(entity.getLastChgBy());
        response.setLastChgDate(entity.getLastChgDate());
        return response;
    }

    private static String flagOrDefault(String value) {
        return value == null || value.isBlank() ? "N" : value.trim().toUpperCase();
    }
}
