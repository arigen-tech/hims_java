package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.projection.BillingTemplateDetailProjection;
import com.hims.projection.BillingTemplateMainProjection;
import com.hims.projection.BillingTemplateProjection;
import com.hims.request.TemplateItemRequest;
import com.hims.request.TemplateRequest;
import com.hims.request.TemplateUpdateRequest;
import com.hims.response.*;
import com.hims.service.BillingTemplateService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingTemplateServiceImpl implements BillingTemplateService {
    @Autowired
    private MasIpdProcedureSurgeryConsumableTemplateRepository templateRepo;
    @Autowired
    private MasIpdProcedureConsumableTemplateDetailRepository detailRepo;
    @Autowired
    private MasProcedureRepository procedureRepo;
    @Autowired
    private MasSurgeryRepository surgeryRepo;
    @Autowired
    private MasStoreItemRepository itemRepo;
    @Autowired
    private AuthUtil authUtil;

    @Transactional
    public ApiResponse<String> saveBillingTemplate(TemplateRequest request) {

        try {
            User user = authUtil.getCurrentUser();
            MasIpdProcedureSurgeryConsumableTemplate template = new MasIpdProcedureSurgeryConsumableTemplate();
            template.setTemplateName(request.getTemplateName());
            template.setTemplateType(request.getTemplateType().toLowerCase());
            template.setStatus(AppConstants.STATUS_Y.toLowerCase());
            template.setCreatedBy(user.getFullName());
            template.setLastUpdatedBy(user.getFullName());
            template.setLastUpdateDate(LocalDateTime.now());

            // CONDITION
            if (AppConstants.PROCEDURE.equalsIgnoreCase(request.getTemplateType())) {
                MasProcedure procedure = procedureRepo.findById(request.getProcedure()).orElseThrow(() -> new RuntimeException("Procedure not found"));
                template.setProcedure(procedure);
                template.setSurgery(null);

            } else if (AppConstants.SURGERY.equalsIgnoreCase(request.getTemplateType())) {
                MasSurgery surgery = surgeryRepo.findById(request.getProcedure()).orElseThrow(() -> new RuntimeException("Surgery not found"));
                template.setSurgery(surgery);
                template.setProcedure(null);
            }
            MasIpdProcedureSurgeryConsumableTemplate savedTemplate = templateRepo.save(template);

            // Save Items
            List<MasIpdProcedureConsumableTemplateDetail> details = request.getTemplateItemRequests().stream().map(item -> {
                MasIpdProcedureConsumableTemplateDetail d = new MasIpdProcedureConsumableTemplateDetail();
                d.setTemplate(savedTemplate);
                MasStoreItem storeItem = itemRepo.findById(item.getItemId()).orElseThrow(() -> new RuntimeException("Item not found"));
                d.setItem(storeItem);
                d.setDefaultQty(item.getQty());
                return d;
            }).toList();
            detailRepo.saveAll(details);
            return ResponseUtils.createSuccessResponse("Billing Template create", new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Billing Template not create", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INSUFFICIENT_STORAGE.value()
            );
        }
    }

    @Override
    public ApiResponse<String> changeStatusBillingTemplate(Long id, String status) {
        try {
            MasIpdProcedureSurgeryConsumableTemplate entity = templateRepo.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("billing template id not found", HttpStatus.BAD_REQUEST.value());
            }

            if (!status.equalsIgnoreCase(AppConstants.STATUS_Y.toLowerCase()) && !status.equalsIgnoreCase(AppConstants.STATUS_N.toLowerCase())) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Invalid status", HttpStatus.BAD_REQUEST.value());
            }
            User user = authUtil.getCurrentUser();
            entity.setStatus(status.toLowerCase());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());
            templateRepo.save(entity);
            return ResponseUtils.createSuccessResponse("status change successfully", new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("billing status failed", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "Status update failed", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
    @Transactional
    public ApiResponse<String> updateBillingTemplate(Long templateId, TemplateUpdateRequest request) {
        try {
            User user = authUtil.getCurrentUser();
            // FETCH TEMPLATE
            MasIpdProcedureSurgeryConsumableTemplate template = templateRepo.findById(templateId).orElseThrow(() -> new RuntimeException("Template not found"));

            // UPDATE TEMPLATE
            template.setTemplateName(request.getTemplateName());
            template.setTemplateType(request.getTemplateType().toLowerCase());
            template.setLastUpdatedBy(user.getFullName());
            template.setLastUpdateDate(LocalDateTime.now());

            if (AppConstants.PROCEDURE.equalsIgnoreCase(request.getTemplateType())) {
                MasProcedure procedure = procedureRepo.findById(request.getProcedureId())
                        .orElseThrow(() -> new RuntimeException("Procedure not found"));
                template.setProcedure(procedure);
                template.setSurgery(null);

            } else if (AppConstants.SURGERY.equalsIgnoreCase(request.getTemplateType())) {
                MasSurgery surgery = surgeryRepo.findById(request.getProcedureId())
                        .orElseThrow(() -> new RuntimeException("Surgery not found"));
                template.setSurgery(surgery);
                template.setProcedure(null);
            }


            //  DELETE SELECTED ITEMS (optimized)
            if (request.getDeleteTemplateDetailsId() != null && !request.getDeleteTemplateDetailsId().isEmpty()) {
                List<MasIpdProcedureConsumableTemplateDetail> toDelete = detailRepo.findValidDetails(templateId, request.getDeleteTemplateDetailsId());
                detailRepo.deleteAll(toDelete);
            }

            //  FETCH EXISTING ITEMS
            List<MasIpdProcedureConsumableTemplateDetail> existingItems = detailRepo.findByTemplate(template);

            Map<Long, MasIpdProcedureConsumableTemplateDetail> existingMap = existingItems.stream()
                            .collect(Collectors.toMap(
                                    e -> e.getItem().getItemId(),
                                    Function.identity()
                            ));

            //  FETCH ALL ITEMS
            List<Long> itemIds = request.getTemplateItemRequests()
                    .stream()
                    .map(TemplateItemRequest::getItemId)
                    .toList();

            Map<Long, MasStoreItem> itemMap = itemRepo.findAllById(itemIds)
                            .stream()
                            .collect(Collectors.toMap(
                                    MasStoreItem::getItemId,
                                    Function.identity()));

            List<MasIpdProcedureConsumableTemplateDetail> toSave = new ArrayList<>();

            // UPDATE + ADD
            for (TemplateItemRequest item : request.getTemplateItemRequests()) {
                MasStoreItem storeItem = itemMap.get(item.getItemId());
                if (storeItem == null) {
                    throw new RuntimeException("Item not found: " + item.getItemId());
                }
                if (existingMap.containsKey(item.getItemId())) {
                    // UPDATE
                    MasIpdProcedureConsumableTemplateDetail existing = existingMap.get(item.getItemId());
                    existing.setDefaultQty(item.getQty());
                    existing.setItem(storeItem);
                    toSave.add(existing);

                } else {
                    // NEW
                    MasIpdProcedureConsumableTemplateDetail newItem = new MasIpdProcedureConsumableTemplateDetail();
                    newItem.setTemplate(template);
                    newItem.setItem(storeItem);
                    newItem.setDefaultQty(item.getQty());
                    toSave.add(newItem);
                }
            }
            detailRepo.saveAll(toSave);
            return ResponseUtils.createSuccessResponse("Billing Template updated successfully", new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error in updateBillingTemplate", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    public ApiResponse<BillingTemplateResponse> getByIdBillingTemplate(Long id) {
        try {

            BillingTemplateProjection template = templateRepo.getTemplateById(id, AppConstants.PROCEDURE, AppConstants.SURGERY);
            if (template == null) {
                return ResponseUtils.createNotFoundResponse("billing template id not found", HttpStatus.BAD_REQUEST.value());
            }
            BillingTemplateResponse res = new BillingTemplateResponse();
            res.setTemplateId(template.getTemplateId());
            res.setTemplateType(template.getTemplateType());
            res.setTemplateName(template.getTemplateName());
            res.setProcedureName(template.getProcedureName());
            List<BillingTemplateDetailProjection> details = detailRepo.getTemplateDetails(id);
            // Map to response
            List<BillingTemplateDetailItemResponse> itemList = details.stream().map(d -> {
                BillingTemplateDetailItemResponse item = new BillingTemplateDetailItemResponse();
                item.setTemplateDetailsId(d.getTemplateDetailsId());
                item.setItemId(d.getItemId());
                item.setItemName(d.getItemName());
                item.setUnit(d.getUnit());
                item.setType(d.getType());
                item.setQty(d.getQty());
                return item;
            }).toList();
            res.setBillingTemplateDetailItemResponseList(itemList);
            return ResponseUtils.createSuccessResponse(res, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error in getByIdBillingTemplate", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    public ApiResponse<Page<BillingTemplateSearchResponse>> searchTemplates(String templateType, String templateName, int page, int size) {
        try {

            Pageable pageable = PageRequest.of(page, size, Sort.by("templateId").descending());
            Page<BillingTemplateMainProjection> result = templateRepo.searchTemplates(templateType, templateName, AppConstants.PROCEDURE, AppConstants.SURGERY, pageable);
            Page<BillingTemplateSearchResponse> response =
                    result.map(p -> {
                        BillingTemplateSearchResponse res = new BillingTemplateSearchResponse();
                        res.setTemplateId(p.getTemplateId());
                        res.setTemplateType(p.getTemplateType());
                        res.setTemplateName(p.getTemplateName());
                        res.setProcedure(p.getProcedure());
                        res.setItemCount(p.getItemCount());
                        res.setStatus(p.getStatus());
                        return res;
                    });

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error in searchTemplates with pagination", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<Page<?>> searchProcedureAndSurgery(String templateType, int page, int size, String search) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            if ("Procedure".equalsIgnoreCase(templateType)) {
                Page<MasProcedureSearchResponse> result = procedureRepo.searchMasProcedure(AppConstants.STATUS_Y.toLowerCase(), search, pageable)
                        .map(p -> {
                            MasProcedureSearchResponse dto = new MasProcedureSearchResponse();
                            dto.setId(p.getProcedureId());
                            dto.setProcedurename(p.getProcedureName());
                            return dto;
                        });

                return ResponseUtils.createSuccessResponse(result, new TypeReference<Page<?>>() {
                });

            } else if ("Surgery".equalsIgnoreCase(templateType)) {
                Page<MasSurgerySearchResponse> result = surgeryRepo.searchMasSurgery(AppConstants.STATUS_Y.toLowerCase(), search, pageable)
                        .map(s -> {
                            MasSurgerySearchResponse dto = new MasSurgerySearchResponse();
                            dto.setId(s.getSurgeryId());
                            dto.setSurgeryName(s.getSurgeryName());
                            return dto;
                        });

                return ResponseUtils.createSuccessResponse(result, new TypeReference<Page<?>>() {
                });
            }
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid Template Type: " + templateType, HttpStatus.BAD_REQUEST.value());
        } catch (Exception e) {
            log.error("Error in searchProcedureAndSurgery: {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }
}

