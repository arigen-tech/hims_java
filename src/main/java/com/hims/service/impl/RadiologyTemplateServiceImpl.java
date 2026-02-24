package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasPacsTemplate;
import com.hims.entity.MasSubChargeCode;
import com.hims.entity.User;
import com.hims.entity.repository.MasSubChargeCodeRepository;
import com.hims.entity.repository.RadiologyTemplateRepository;
import com.hims.request.RadiologyTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.RadiologyTemplateResponse;
import com.hims.service.RadiologyTemplateService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Service
@Slf4j
@RequiredArgsConstructor
public class RadiologyTemplateServiceImpl implements RadiologyTemplateService {

    private final RadiologyTemplateRepository radiologyTemplateRepository;
    private final AuthUtil authUtil;
    private final MasSubChargeCodeRepository masSubChargeCodeRepository;

    @Override
    public ApiResponse<List<RadiologyTemplateResponse>> getAll(int flag) {
        try {
            List<MasPacsTemplate> list = (flag == 1)
                    ? radiologyTemplateRepository.findByStatusIgnoreCaseOrderByTemplateNameAsc("y")
                    : radiologyTemplateRepository.findAllByOrderByLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("Error fetching radiology template list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to fetch data", 500
            );
        }
    }

    @Override
    public ApiResponse<RadiologyTemplateResponse> getById(Long id) {
        try {
            return radiologyTemplateRepository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse("Radiology template not found", 404));
        } catch (Exception e) {
            log.error("Error fetching radiology template by id : {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to fetch record", 500
            );
        }
    }

    @Override
    public ApiResponse<RadiologyTemplateResponse> create(RadiologyTemplateRequest request) {
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 401
                );
            }
            MasSubChargeCode subChargeCode = masSubChargeCodeRepository.findById(request.getSubChargecodeId())
                    .orElse(null);
            if (subChargeCode == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Invalid subChargecodeId: " + request.getSubChargecodeId(), 404
                );
            }

            MasPacsTemplate entity = new MasPacsTemplate();
            entity.setTemplateCode(request.getTemplateCode());
            entity.setTemplateName(request.getTemplateName());
            entity.setSubChargecodeId(subChargeCode);
            entity.setTemplateText(request.getTemplateText());
            entity.setStatus("y");

            entity.setCreatedBy(user.getFullName());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());
            MasPacsTemplate saved = radiologyTemplateRepository.save(entity);
            return ResponseUtils.createSuccessResponse(
                    toResponse(saved), new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error creating radiology template", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to create record", 500
            );
        }
    }

    @Override
    public ApiResponse<RadiologyTemplateResponse> update(Long id, RadiologyTemplateRequest request) {
        try {
            MasPacsTemplate entity = radiologyTemplateRepository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Radiology template not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 401
                );
            }



            MasSubChargeCode subChargeCode = masSubChargeCodeRepository.findById(request.getSubChargecodeId())
                    .orElse(null);

            if (subChargeCode == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Invalid subChargecodeId: " + request.getSubChargecodeId(), 404
                );
            }

            entity.setTemplateCode(request.getTemplateCode());
            entity.setTemplateName(request.getTemplateName());
            entity.setSubChargecodeId(subChargeCode);
            entity.setTemplateText(request.getTemplateText());

            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());
            MasPacsTemplate saved = radiologyTemplateRepository.save(entity);
            return ResponseUtils.createSuccessResponse(
                    toResponse(saved), new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error updating radiology template id : {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to update record", 500
            );
        }
    }

    @Override
    public ApiResponse<RadiologyTemplateResponse> changeStatus(Long id, String status) {
        try {
            if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status (allowed: y/n)", 400
                );
            }

            MasPacsTemplate entity = radiologyTemplateRepository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("Radiology template not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 401
                );
            }

            entity.setStatus(status.toLowerCase());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());
            MasPacsTemplate saved = radiologyTemplateRepository.save(entity);
            return ResponseUtils.createSuccessResponse(
                    toResponse(saved), new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error changing status for radiology template id : {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Failed to change status", 500
            );
        }
    }

    @Override
    public ApiResponse<List<RadiologyTemplateResponse>> getByIdTemplateList(Long modalityId) {
        try {
            if (modalityId == null) {
                return ResponseUtils.createFailureResponse(List.of(), new TypeReference<List<RadiologyTemplateResponse>>() {},
                        "subChargecodeId is required",
                        400
                );
            }
            List<MasPacsTemplate> templates = radiologyTemplateRepository
                            .findBySubChargecodeId_SubIdAndStatusIgnoreCaseOrderByTemplateNameAsc(modalityId, "y");

            List<RadiologyTemplateResponse> resp = templates.stream()
                    .map(t -> RadiologyTemplateResponse.builder()
                            .pacsTemplateId(t.getPacsTemplateId())
                            .templateCode(t.getTemplateCode())
                            .templateName(t.getTemplateName())
                            .templateText(t.getTemplateText())
                            .subChargecodeId(t.getSubChargecodeId().getSubId())
                            .subChargeCodeName(t.getSubChargecodeId().getSubName())
                            .build()
                    )
                    .toList();

            return ResponseUtils.createSuccessResponse(resp, new TypeReference<List<RadiologyTemplateResponse>>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    List.of(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<RadiologyTemplateResponse>>() {},
                    "Error while fetching template list",
                    500
            );
        }
    }





    private RadiologyTemplateResponse toResponse(MasPacsTemplate e) {
        RadiologyTemplateResponse res = new RadiologyTemplateResponse();
        res.setPacsTemplateId(e.getPacsTemplateId());
        res.setTemplateCode(e.getTemplateCode());
        res.setTemplateName(e.getTemplateName());
        res.setSubChargecodeId(e.getSubChargecodeId() != null ? e.getSubChargecodeId().getSubId() : null);
        res.setSubChargeCodeName(e.getSubChargecodeId() != null ? e.getSubChargecodeId().getSubName() : null);
        res.setTemplateText(e.getTemplateText());
        return res;
    }
}