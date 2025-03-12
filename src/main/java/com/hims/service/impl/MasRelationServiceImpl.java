package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasRelation;
import com.hims.entity.repository.MasRelationRepository;
import com.hims.request.MasRelationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasRelationResponse;
import com.hims.service.MasRelationService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasRelationServiceImpl implements MasRelationService {

    @Autowired
    private MasRelationRepository masRelationRepository;

    public ApiResponse<List<MasRelationResponse>> getAllRelations(int flag) {
        List<MasRelation> relations;

        if (flag == 1) {
            // Fetch only records with status 'Y'
            relations = masRelationRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            // Fetch all records with status 'Y' or 'N'
            relations = masRelationRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            // Handle invalid flag values
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasRelationResponse> responses = relations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    public ApiResponse<List<MasRelationResponse>> getAllRelations() {
        List<MasRelation> relations = masRelationRepository.findAll();

        List<MasRelationResponse> responses = relations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
        });
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


    @Override
    @Transactional
    public ApiResponse<MasRelationResponse> addRelation(MasRelationRequest relationRequest) {
        MasRelation relation = new MasRelation();
        relation.setRelationName(relationRequest.getRelationName());
        relation.setCode(relationRequest.getCode());
        relation.setStatus(relationRequest.getStatus()); // Default status can be set here if needed
        relation.setLastChgBy(relationRequest.getLastChgBy());
        relation.setLastChgDate(Instant.now());

        MasRelation savedRelation = masRelationRepository.save(relation);
        return ResponseUtils.createSuccessResponse(convertToResponse(savedRelation), new TypeReference<>() {
        });
    }

    @Override
    @Transactional
    public ApiResponse<MasRelationResponse> updateRelation(Long id, MasRelationResponse relationDetails) {
        Optional<MasRelation> existingRelationOpt = masRelationRepository.findById(id);
        if (existingRelationOpt.isPresent()) {
            MasRelation existingRelation = existingRelationOpt.get();
            existingRelation.setRelationName(relationDetails.getRelationName());
            existingRelation.setCode(relationDetails.getCode());
            existingRelation.setStatus(relationDetails.getStatus());
            existingRelation.setLastChgBy(relationDetails.getLastChgBy());
            existingRelation.setLastChgDate(Instant.now());

            MasRelation updatedRelation = masRelationRepository.save(existingRelation);
            return ResponseUtils.createSuccessResponse(convertToResponse(updatedRelation), new TypeReference<>() {
            });
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasRelationResponse>() {
            }, "Relation not found", 404);
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasRelationResponse> changeStatus(Long id, String status) {
        Optional<MasRelation> existingRelationOpt = masRelationRepository.findById(id);
        if (existingRelationOpt.isPresent()) {
            MasRelation existingRelation = existingRelationOpt.get();

            // Validate status value
            if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasRelationResponse>() {
                }, "Invalid status value. Use 'Y' for Active and 'N' for Inactive.", 400);
            }

            existingRelation.setStatus(status); // Set status as "Y" or "N"
            MasRelation updatedRelation = masRelationRepository.save(existingRelation);

            return ResponseUtils.createSuccessResponse(convertToResponse(updatedRelation), new TypeReference<>() {
            });
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasRelationResponse>() {
            }, "Relation not found", 404);
        }
    }

    @Override
    @Transactional
    public ApiResponse<MasRelationResponse> findById(Long id) {
        Optional<MasRelation> existingRelationOpt = masRelationRepository.findById(id);
        if (existingRelationOpt.isPresent()) {
            return ResponseUtils.createSuccessResponse(convertToResponse(existingRelationOpt.get()), new TypeReference<>() {
            });
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasRelationResponse>() {
            }, "Relation not found", 404);
        }
    }
}