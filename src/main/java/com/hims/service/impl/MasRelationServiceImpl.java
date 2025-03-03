package com.hims.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasRelation;
import com.hims.entity.repository.MasRelationRepository;
import com.hims.response.ApiResponse;
import com.hims.response.MasRelationResponse;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasRelationServiceImpl implements MasRelationService{

    @Autowired
    private MasRelationRepository masRelationRepository;

    public ApiResponse<List<MasRelationResponse>> getAllRelations() {
        List<MasRelation> relations = masRelationRepository.findAll();

        List<MasRelationResponse> responses = relations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    private MasRelationResponse convertToResponse(MasRelation relation) {
        MasRelationResponse response = new MasRelationResponse();
        response.setId(relation.getId());
        response.setRelationName(relation.getRelationName());
        response.setCode(relation.getCode());
        response.setStatus(relation.getStatus());
        response.setLastChgBy(relation.getLastChgBy());
        response.setLastChgDate(relation.getLastChgDate());
        return response;
    }
}
