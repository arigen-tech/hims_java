package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.mapper.OtSurgeryDrugMapper;
import com.hims.request.OtSurgeryAnaesthesiaRequest;
import com.hims.request.OtSurgeryExecutionRequest;
import com.hims.request.OtSurgeryDrugRequest;
import com.hims.request.OtSurgeryConsumableRequest;
import com.hims.request.SurgeryDrugConsumableTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OtSurgeryAnaesthesiaResponse;
import com.hims.response.OtSurgeryExecutionResponse;
import com.hims.response.OtSurgeryDrugResponse;
import com.hims.response.OtSurgeryConsumableResponse;
import com.hims.response.UserContext;
import com.hims.response.SurgeryDrugConsumableTemplateResponse;
import com.hims.service.OtSurgeryExecutionService;
import com.hims.service.UserContextService;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OtSurgeryExecutionServiceImpl implements OtSurgeryExecutionService {
    @Value("${surgeryDrugConsumableTemplate.status.active}")
    private String activeStatus;

    @Value("${surgeryDrugConsumableTemplate.status.inactive}")
    private String inactiveStatus;

    @Value("${drugSectionCode}")
    private String drugSectionCode;

    @Value("${nonDrugSectionCode}")
    private String nonDrugSectionCode;

    @Value("${ot.billing.category.anaesthesia.code}")
    private String anaesthesiaBillingCategoryCode;

    @Value("${ot.billing.category.drug.code}")
    private String drugBillingCategoryCode;

    @Value("${ot.billing.category.medicalConsumable.code}")
    private String consumableBillingCategoryCode;


    private final OtBookingRepository bookingRepository;
    private final OtBookingRequestDtRepository bookingRequestDtRepository;
    private final OtSurgeryExecutionRepository executionRepository;
    private final OtSurgeryDrugRepository surgeryDrugRepository;
    private final OtSurgeryConsumableRepository surgeryConsumableRepository;
    private final OtSurgeryDrugMapper surgeryDrugMapper;
    private final StoreItemBatchStockRepository batchStockRepository;
    private final OtSurgeryAnaesthesiaRepository anaesthesiaRepository;
    private final MasAnaesthesiaTypeRepository anaesthesiaTypeRepository;
    private final IpdBillingHeaderRepository ipdBillingHeaderRepository;
    private final IpdBillingDetailsRepository ipdBillingDetailsRepository;
    private final MasIpdServiceCategoryRepository ipdServiceCategoryRepository;
    private final SurgeryDrugConsumableTemplateHdRepository templateHeaderRepository;
    private final SurgeryDrugConsumableTemplateDtRepository templateDetailRepository;
    private final MasSurgeryRepository surgeryRepository;
    private final MasStoreItemRepository storeItemRepository;
    private final UserContextService userContextService;

    @Override
    @Transactional
    public ApiResponse<OtSurgeryExecutionResponse> saveExecution(OtSurgeryExecutionRequest request) {
        OtSurgeryExecution execution = persistExecution(request,
                userContextService.getCurrentUserFullNameFromToken()).execution();
        return success(toExecutionResponse(request.getOtBookingId(), execution));
    }

    @Override
    @Transactional
    public ApiResponse<List<OtSurgeryDrugResponse>> saveDrug(OtSurgeryDrugRequest request) {
        String actor = userContextService.getCurrentUserFullNameFromToken();
        OtSurgeryExecution execution = executionRepository
                .findByOtBooking_OtBookingId(request.getOtBookingId())
                .orElse(null);
        if (execution == null) {
            if (request.getOtBookingRequestDtId() == null) {
                throw new SDDException(HttpStatus.BAD_REQUEST.value(),
                        "otBookingRequestDtId is required when no surgery execution exists for the OT booking");
            }
            OtSurgeryExecutionRequest executionRequest = new OtSurgeryExecutionRequest();
            executionRequest.setOtBookingId(request.getOtBookingId());
            executionRequest.setOtBookingRequestDtId(request.getOtBookingRequestDtId());
            execution = persistExecution(executionRequest, actor).execution();
        }

        UserContext userContext = userContextService.getCurrentUserContext();
        if (userContext == null || userContext.getHospitalId() == null || userContext.getDepartmentId() == null) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Hospital and department are required in access token");
        }
        List<OtSurgeryDrugResponse> responses = new java.util.ArrayList<>(request.getItems().size());
        for (OtSurgeryDrugRequest.DrugItem drugItem : request.getItems()) {
            responses.add(saveDrugItem(execution, drugItem, userContext, actor));
        }
        return success(responses);
    }

    @Override
    @Transactional
    public ApiResponse<List<OtSurgeryConsumableResponse>> saveConsumables(OtSurgeryConsumableRequest request) {
        String actor = userContextService.getCurrentUserFullNameFromToken();
        OtSurgeryExecution execution = executionRepository
                .findByOtBooking_OtBookingId(request.getOtBookingId())
                .orElse(null);
        if (execution == null) {
            if (request.getOtBookingRequestDtId() == null) {
                throw new SDDException(HttpStatus.BAD_REQUEST.value(),
                        "otBookingRequestDtId is required when no surgery execution exists for the OT booking");
            }
            OtSurgeryExecutionRequest executionRequest = new OtSurgeryExecutionRequest();
            executionRequest.setOtBookingId(request.getOtBookingId());
            executionRequest.setOtBookingRequestDtId(request.getOtBookingRequestDtId());
            execution = persistExecution(executionRequest, actor).execution();
        }

        UserContext userContext = userContextService.getCurrentUserContext();
        if (userContext == null || userContext.getHospitalId() == null || userContext.getDepartmentId() == null) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Hospital and department are required in access token");
        }
        List<OtSurgeryConsumableResponse> responses = new java.util.ArrayList<>(request.getItems().size());
        for (OtSurgeryConsumableRequest.ConsumableItem itemRequest : request.getItems()) {
            responses.add(saveConsumableItem(execution, itemRequest, userContext, actor));
        }
        return success(responses);
    }

    private OtSurgeryConsumableResponse saveConsumableItem(OtSurgeryExecution execution,
                                                      OtSurgeryConsumableRequest.ConsumableItem request,
                                                      UserContext userContext,
                                                      String actor) {
        long quantity;
        try {
            quantity = request.getQuantity().longValueExact();
        } catch (ArithmeticException ex) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Consumable quantity must be a whole number");
        }
        MasStoreItem item = storeItemRepository.findById(request.getItemId())
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(),
                        "Store item not found: " + request.getItemId()));
        List<StoreItemBatchStock> batches = batchStockRepository.findAvailableBatchesForUpdate(
                item.getItemId(), userContext.getHospitalId(), userContext.getDepartmentId(), java.time.LocalDate.now());
        if ((request.getBatchNo() == null || request.getBatchNo().isBlank()) && batches.size() > 1) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(),
                    "batchNo is required when the item has multiple available batches: " + request.getItemId());
        }
        if (request.getBatchNo() != null && !request.getBatchNo().isBlank()) {
            batches = batches.stream().filter(stock -> stock.getBatchNo() != null
                    && stock.getBatchNo().trim().equalsIgnoreCase(request.getBatchNo().trim())).toList();
        }
        StoreItemBatchStock selected = batches.stream()
                .filter(stock -> stock.getClosingStock() != null && stock.getClosingStock() >= quantity)
                .findFirst()
                .orElseThrow(() -> new SDDException(HttpStatus.BAD_REQUEST.value(),
                        "Insufficient stock for item " + request.getItemId() + " and selected batch"));

        selected.setClosingStock(selected.getClosingStock() - quantity);
        selected.setIpdIssueQty(zero(selected.getIpdIssueQty()).add(request.getQuantity()));
        selected.setLastChgBy(actor);

        OtSurgeryConsumable consumable = OtSurgeryConsumable.builder()
                .otSurgeryExecution(execution)
                .itemId(item.getItemId())
                .itemName(item.getNomenclature())
                .plannedQuantity(request.getPlannedQuantity())
                .usedQuantity(request.getQuantity())
                .batchNo(selected.getBatchNo())
                .remarks(request.getRemarks())
                .lastChgBy(actor)
                .build();
        consumable = surgeryConsumableRepository.save(consumable);
        updateIpdConsumableBilling(execution.getOtBooking(), consumable, selected);
        return OtSurgeryConsumableResponse.builder()
                .otSurgeryConsumableId(consumable.getOtSurgeryConsumableId())
                .otSurgeryExecutionId(execution.getOtSurgeryExecutionId())
                .itemId(consumable.getItemId())
                .itemName(consumable.getItemName())
                .quantity(consumable.getUsedQuantity())
                .plannedQuantity(consumable.getPlannedQuantity())
                .batchNo(consumable.getBatchNo())
                .remarks(consumable.getRemarks())
                .lastChgDate(consumable.getLastChgDate())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<OtSurgeryConsumableResponse>> getConsumables(Long otBookingId) {
        return success(surgeryConsumableRepository.findActive(otBookingId).stream()
                .map(this::toConsumableResponse).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<OtSurgeryConsumableResponse>> getConsumables(Long otBookingId, int page, int size) {
        Page<OtSurgeryConsumableResponse> results = surgeryConsumableRepository
                .findActive(otBookingId, PageRequest.of(page, size,
                        Sort.by(Sort.Direction.DESC, "otSurgeryConsumableId")))
                .map(this::toConsumableResponse);
        return success(results);
    }

    private OtSurgeryConsumableResponse toConsumableResponse(OtSurgeryConsumable consumable) {
        return OtSurgeryConsumableResponse.builder()
                .otSurgeryConsumableId(consumable.getOtSurgeryConsumableId())
                .otSurgeryExecutionId(consumable.getOtSurgeryExecution().getOtSurgeryExecutionId())
                .itemId(consumable.getItemId())
                .itemName(consumable.getItemName())
                .quantity(consumable.getUsedQuantity())
                .plannedQuantity(consumable.getPlannedQuantity())
                .batchNo(consumable.getBatchNo())
                .remarks(consumable.getRemarks())
                .lastChgDate(consumable.getLastChgDate())
                .build();
    }

    private OtSurgeryDrugResponse saveDrugItem(OtSurgeryExecution execution,
                                                OtSurgeryDrugRequest.DrugItem request,
                                                UserContext userContext,
                                                String actor) {
        long quantity;
        try {
            quantity = request.getQuantity().longValueExact();
        } catch (ArithmeticException ex) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Drug quantity must be a whole number");
        }
        MasStoreItem item = storeItemRepository.findById(request.getItemId())
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(),
                        "Store item not found: " + request.getItemId()));
        List<StoreItemBatchStock> batches = batchStockRepository.findAvailableBatchesForUpdate(
                item.getItemId(), userContext.getHospitalId(), userContext.getDepartmentId(), java.time.LocalDate.now());
        if ((request.getBatchNo() == null || request.getBatchNo().isBlank()) && batches.size() > 1) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(),
                    "batchNo is required when the item has multiple available batches: " + request.getItemId());
        }
        if (request.getBatchNo() != null && !request.getBatchNo().isBlank()) {
            batches = batches.stream().filter(stock -> stock.getBatchNo() != null
                    && stock.getBatchNo().trim().equalsIgnoreCase(request.getBatchNo().trim())).toList();
        }
        StoreItemBatchStock selected = batches.stream()
                .filter(stock -> stock.getClosingStock() != null && stock.getClosingStock() >= quantity)
                .findFirst()
                .orElseThrow(() -> new SDDException(HttpStatus.BAD_REQUEST.value(),
                        "Insufficient stock for item " + request.getItemId() + " and selected batch"));

        selected.setClosingStock(selected.getClosingStock() - quantity);
        selected.setIpdIssueQty(zero(selected.getIpdIssueQty()).add(request.getQuantity()));
        selected.setLastChgBy(actor);

        OtSurgeryDrug drug = OtSurgeryDrug.builder()
                .otSurgeryExecution(execution)
                .item(item)
                .itemName(item.getNomenclature())
                .quantity(request.getQuantity())
                .batchNo(selected.getBatchNo())
                .remarks(request.getRemarks())
                .usedBy(userContext.getUserId())
                .lastChgBy(actor)
                .build();
        drug = surgeryDrugRepository.save(drug);
        updateIpdDrugBilling(execution.getOtBooking(), drug, selected);
        return surgeryDrugMapper.toResponse(drug);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<OtSurgeryDrugResponse>> getDrugs(Long otBookingId) {
        return success(surgeryDrugRepository.findActive(otBookingId).stream().map(this::toDrugResponse).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<OtSurgeryDrugResponse>> getDrugs(Long otBookingId, int page, int size) {
        Page<OtSurgeryDrugResponse> results = surgeryDrugRepository
                .findActive(otBookingId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "otSurgeryDrugId")))
                .map(this::toDrugResponse);
        return success(results);
    }

    private OtSurgeryDrugResponse toDrugResponse(OtSurgeryDrug drug) {
        return surgeryDrugMapper.toResponse(drug);
    }

    private void updateIpdDrugBilling(OtBooking booking, OtSurgeryDrug drug, StoreItemBatchStock stock) {
        Inpatient admission = booking.getOtBookingRequest() == null
                ? null : booking.getOtBookingRequest().getAdmissionId();
        if (admission == null) return;
        IpdBillingHeader header = ipdBillingHeaderRepository.findByInpatientId_InpatientId(admission.getInpatientId())
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(),
                        "IPD billing header not found for OT admission"));
        BigDecimal quantity = drug.getQuantity();
        BigDecimal rate = stock.getMrpPerUnit() == null ? BigDecimal.ZERO : stock.getMrpPerUnit();
        BigDecimal amount = rate.multiply(quantity);
        IpdBillingDetails detail = IpdBillingDetails.builder()
                .billHeader(header)
                .category(getRequiredIpdBillingCategory(drugBillingCategoryCode))
                .sourceType("DRUG")
                .sourceId(drug.getOtSurgeryDrugId())
                .referenceNo("OT_SURGERY_DRUG_" + drug.getOtSurgeryDrugId())
                .itemName(drug.getItemName())
                .serviceDate(LocalDateTime.now())
                .quantity(quantity)
                .rate(rate)
                .amount(amount)
                .gstPercent(BigDecimal.ZERO)
                .gstAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .netAmount(amount)
                .payable(true)
                .cancelled(false)
                .itemStatus("ACTIVE")
                .batchNo(stock.getBatchNo())
                .expiryDate(stock.getExpiryDate())
                .remarks(drug.getRemarks())
                .createdAt(LocalDateTime.now())
                .build();
        ipdBillingDetailsRepository.save(detail);
        header.setTotalAmount(zero(header.getTotalAmount()).add(amount));
        header.setNetAmount(zero(header.getNetAmount()).add(amount));
        header.setPatientPayableAmount(zero(header.getPatientPayableAmount()).add(amount));
        header.setOutstandingAmount(zero(header.getOutstandingAmount()).add(amount));
        header.setUpdatedBy(userContextService.getCurrentUserFullNameFromToken());
        header.setUpdatedAt(LocalDateTime.now());
        ipdBillingHeaderRepository.save(header);
    }

    private void updateIpdConsumableBilling(OtBooking booking, OtSurgeryConsumable consumable,
                                            StoreItemBatchStock stock) {
        Inpatient admission = booking.getOtBookingRequest() == null
                ? null : booking.getOtBookingRequest().getAdmissionId();
        if (admission == null) return;
        IpdBillingHeader header = ipdBillingHeaderRepository.findByInpatientId_InpatientId(admission.getInpatientId())
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(),
                        "IPD billing header not found for OT admission"));
        BigDecimal quantity = consumable.getUsedQuantity();
        BigDecimal rate = stock.getMrpPerUnit() == null ? BigDecimal.ZERO : stock.getMrpPerUnit();
        BigDecimal amount = rate.multiply(quantity).setScale(2, java.math.RoundingMode.HALF_UP);
        IpdBillingDetails detail = IpdBillingDetails.builder()
                .billHeader(header)
                .category(getRequiredIpdBillingCategory(consumableBillingCategoryCode))
                .sourceType("PROCEDURE")
                .sourceId(consumable.getOtSurgeryConsumableId())
                .referenceNo("OT_SURGERY_CONSUMABLE_" + consumable.getOtSurgeryConsumableId())
                .itemName(consumable.getItemName())
                .serviceDate(LocalDateTime.now())
                .quantity(quantity)
                .rate(rate)
                .amount(amount)
                .gstPercent(BigDecimal.ZERO)
                .gstAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .netAmount(amount)
                .payable(true)
                .cancelled(false)
                .itemStatus("ACTIVE")
                .batchNo(stock.getBatchNo())
                .expiryDate(stock.getExpiryDate())
                .remarks(consumable.getRemarks())
                .createdAt(LocalDateTime.now())
                .build();
        ipdBillingDetailsRepository.save(detail);
        header.setTotalAmount(zero(header.getTotalAmount()).add(amount));
        header.setNetAmount(zero(header.getNetAmount()).add(amount));
        header.setPatientPayableAmount(zero(header.getPatientPayableAmount()).add(amount));
        header.setOutstandingAmount(zero(header.getOutstandingAmount()).add(amount));
        header.setUpdatedBy(userContextService.getCurrentUserFullNameFromToken());
        header.setUpdatedAt(LocalDateTime.now());
        ipdBillingHeaderRepository.save(header);
    }

    @Override
    @Transactional
    public ApiResponse<OtSurgeryAnaesthesiaResponse> saveAnaesthesia(OtSurgeryAnaesthesiaRequest request) {
        String actor = userContextService.getCurrentUserFullNameFromToken();
        ExecutionSaveResult executionSaveResult = persistExecution(toExecutionRequest(request), actor);
        OtBooking booking = executionSaveResult.booking();
        OtSurgeryExecution execution = executionSaveResult.execution();

        OtSurgeryExecution savedExecution = execution;
        OtSurgeryAnaesthesia anaesthesia = anaesthesiaRepository
                .findByOtSurgeryExecution_OtSurgeryExecutionId(execution.getOtSurgeryExecutionId())
                .orElseGet(() -> OtSurgeryAnaesthesia.builder().otSurgeryExecution(savedExecution).build());
        anaesthesia.setAnaesthesiaTypeId(request.getAnaesthesiaTypeId());
        anaesthesia.setAnaesthetistId(request.getAnaesthetistId());
        anaesthesia.setAssistantAnaesthetistId(request.getAssistantAnaesthetistId());
        anaesthesia.setAnaesthesiaStartDatetime(request.getAnaesthesiaStartDatetime());
        anaesthesia.setAnaesthesiaEndDatetime(request.getAnaesthesiaEndDatetime());
        anaesthesia.setAirwayManagement(request.getAirwayManagement());
        anaesthesia.setAsaGradeId(request.getAsaGradeId());
        anaesthesia.setPreInductionBp(request.getPreInductionBp());
        anaesthesia.setPreInductionPulse(request.getPreInductionPulse());
        anaesthesia.setPreInductionSpo2(request.getPreInductionSpo2());
        anaesthesia.setAnaesthesiaNotes(request.getAnaesthesiaNotes());
        anaesthesia.setComplication(request.getComplication() == null ? "N" : request.getComplication());
        anaesthesia.setComplicationRemarks(request.getComplicationRemarks());
        anaesthesia.setLastChgBy(actor);
        anaesthesia = anaesthesiaRepository.save(anaesthesia);
        updateIpdBilling(booking, execution, request);
        return success(toAnaesthesiaResponse(request.getOtBookingId(), execution, anaesthesia));
    }

    private ExecutionSaveResult persistExecution(OtSurgeryExecutionRequest request, String actor) {
        OtBooking booking = bookingRepository.findById(request.getOtBookingId())
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(), "OT booking not found"));
        OtBookingRequestDt requestDt = bookingRequestDtRepository.findById(request.getOtBookingRequestDtId())
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(), "OT booking surgery detail not found"));
        if (booking.getOtBookingRequest() == null || requestDt.getOtBookingRequest() == null
                || !booking.getOtBookingRequest().getOtBookingRequestId()
                .equals(requestDt.getOtBookingRequest().getOtBookingRequestId())) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(),
                    "Surgery detail does not belong to the specified OT booking");
        }

        OtSurgeryExecution execution = executionRepository.findByOtBooking_OtBookingId(request.getOtBookingId())
                .orElseGet(() -> OtSurgeryExecution.builder()
                        .otBooking(booking).otBookingRequestDt(requestDt).build());
        if (!execution.getOtBookingRequestDt().getOtBookingRequestDtId()
                .equals(request.getOtBookingRequestDtId())) {
            throw new SDDException(HttpStatus.CONFLICT.value(),
                    "This OT booking already has an execution for a different surgery detail");
        }
        execution.setSurgeryStartDatetime(request.getSurgeryStartDatetime());
        execution.setSurgeryEndDatetime(request.getSurgeryEndDatetime());
        if (request.getExecutionStatus() != null) execution.setExecutionStatus(request.getExecutionStatus());
        execution.setProcedureConfirmed("N");
        execution.setInstrumentCountCorrect("N");
        execution.setSpecimenLabelVerified("N");
        execution.setEquipmentIssuesReviewed("N");
        execution.setRecoveryConcernsReviewed("N");
        execution.setSignoutRemarks(request.getSignoutRemarks());
        execution.setSignoutVerifiedBy(request.getSignoutVerifiedBy());
        execution.setSignoutVerifiedDatetime(request.getSignoutVerifiedDatetime());
        execution.setRemarks(request.getExecutionRemarks());
        execution.setLastChgBy(actor);
        execution = executionRepository.save(execution);
        return new ExecutionSaveResult(booking, execution);
    }

    private OtSurgeryExecutionRequest toExecutionRequest(OtSurgeryAnaesthesiaRequest source) {
        OtSurgeryExecutionRequest request = new OtSurgeryExecutionRequest();
        request.setOtBookingId(source.getOtBookingId());
        request.setOtBookingRequestDtId(source.getOtBookingRequestDtId());
        request.setSurgeryStartDatetime(source.getSurgeryStartDatetime());
        request.setSurgeryEndDatetime(source.getSurgeryEndDatetime());
        request.setExecutionStatus(source.getExecutionStatus());
        request.setSignoutRemarks(source.getSignoutRemarks());
        request.setSignoutVerifiedBy(source.getSignoutVerifiedBy());
        request.setSignoutVerifiedDatetime(source.getSignoutVerifiedDatetime());
        request.setExecutionRemarks(source.getExecutionRemarks());
        return request;
    }

    private record ExecutionSaveResult(OtBooking booking, OtSurgeryExecution execution) {}

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OtSurgeryAnaesthesiaResponse> getAnaesthesiaByBookingId(Long otBookingId) {
        OtSurgeryAnaesthesia anaesthesia = anaesthesiaRepository.findLatestByBookingId(otBookingId)
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(),
                        "Anaesthesia record not found for OT booking"));
        return success(toAnaesthesiaResponse(otBookingId, anaesthesia.getOtSurgeryExecution(), anaesthesia));
    }

    @Override
    @Transactional
    public ApiResponse<SurgeryDrugConsumableTemplateResponse> saveDrugConsumableTemplate(
            SurgeryDrugConsumableTemplateRequest request) {
        MasSurgery surgery = surgeryRepository.findById(request.getSurgeryId())
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(), "Surgery not found"));
        SurgeryDrugConsumableTemplateHd header = request.getTemplateId() == null
                ? new SurgeryDrugConsumableTemplateHd()
                : templateHeaderRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(), "Template not found"));

        List<Long> itemIds = request.getItems().stream()
                .map(SurgeryDrugConsumableTemplateRequest.Item::getItemId).toList();
        if (itemIds.stream().distinct().count() != itemIds.size()) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Duplicate item IDs are not allowed");
        }
        List<MasStoreItem> items = storeItemRepository.findAllById(itemIds);
        if (items.size() != itemIds.size()) {
            throw new SDDException(HttpStatus.NOT_FOUND.value(), "One or more store items were not found");
        }
        if (items.stream().anyMatch(item -> item.getSectionId() == null
                || item.getSectionId().getSectionCode() == null)) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(),
                    "Every store item must be assigned to a section with a section code");
        }
        boolean drugTemplate = isDrug(items.get(0));
        if (items.stream().anyMatch(item -> isDrug(item) != drugTemplate)) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(),
                    "A template cannot mix DRUG and NON_DRUG items");
        }

        String actor = userContextService.getCurrentUserFullNameFromToken();
        header.setSurgery(surgery);
        header.setTemplateName(request.getTemplateName().trim());
        header.setDescription(request.getDescription());
        header.setStatus(normalizeStatus(request.getStatus()));
        header.setLastChgBy(actor);
        header = templateHeaderRepository.save(header);

        List<SurgeryDrugConsumableTemplateDt> previousDetails = templateDetailRepository
                .findByTemplateAndStatusIgnoreCaseOrderByTemplateDetailIdAsc(header, activeStatus);
        previousDetails.forEach(detail -> detail.setStatus(inactiveStatus));
        templateDetailRepository.saveAll(previousDetails);

        SurgeryDrugConsumableTemplateHd savedHeader = header;
        Map<Long, MasStoreItem> itemById = items.stream()
                .collect(Collectors.toMap(MasStoreItem::getItemId, item -> item));
        List<SurgeryDrugConsumableTemplateDt> details = request.getItems().stream().map(input ->
                SurgeryDrugConsumableTemplateDt.builder()
                        .template(savedHeader)
                        .item(itemById.get(input.getItemId()))
                        .plannedQuantity(input.getPlannedQuantity())
                        .status(activeStatus)
                        .lastChgBy(actor)
                        .build()).toList();
        templateDetailRepository.saveAll(details);

        return success(toTemplateResponse(header, details, drugTemplate ? drugSectionCode : nonDrugSectionCode));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<SurgeryDrugConsumableTemplateResponse> getDrugConsumableTemplateById(Long templateId) {
        SurgeryDrugConsumableTemplateHd header = templateHeaderRepository.findById(templateId)
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(), "Template not found"));
        List<SurgeryDrugConsumableTemplateDt> details = templateDetailRepository
                .findByTemplateAndStatusIgnoreCaseOrderByTemplateDetailIdAsc(header, activeStatus);
        String sectionCode = details.isEmpty() ? null
                : isDrug(details.get(0).getItem()) ? drugSectionCode : nonDrugSectionCode;
        return success(toTemplateResponse(header, details, sectionCode));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<SurgeryDrugConsumableTemplateResponse>> getActiveDrugConsumableTemplates(
            Long surgeryId, String sectionCode) {
        String normalized = sectionCode.trim().toUpperCase(Locale.ROOT);
        if (!drugSectionCode.equalsIgnoreCase(normalized) && !nonDrugSectionCode.equalsIgnoreCase(normalized)) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "sectionCode must be DRUG or NON_DRUG");
        }
        String selectedCode = drugSectionCode.equalsIgnoreCase(normalized) ? drugSectionCode : nonDrugSectionCode;
        List<SurgeryDrugConsumableTemplateResponse> responses = templateHeaderRepository
                .findActiveBySurgeryAndSectionCode(surgeryId, selectedCode, activeStatus,
                        drugSectionCode, nonDrugSectionCode).stream()
                .map(header -> toTemplateResponse(header, templateDetailRepository
                        .findByTemplateAndStatusIgnoreCaseOrderByTemplateDetailIdAsc(header, activeStatus), selectedCode))
                .toList();
        return success(responses);
    }

    private void updateIpdBilling(OtBooking booking, OtSurgeryExecution execution,
                                  OtSurgeryAnaesthesiaRequest request) {
        if (booking.getOtBookingRequest().getAdmissionId() == null || request.getAnaesthesiaTypeId() == null) return;
        MasAnaesthesiaType type = anaesthesiaTypeRepository.findById(request.getAnaesthesiaTypeId())
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(), "Anaesthesia type not found"));
        BigDecimal rate = type.getPrice() == null ? BigDecimal.ZERO : type.getPrice();
        String referenceNo = "OT_ANAESTHESIA_" + execution.getOtSurgeryExecutionId();
        IpdBillingDetails detail = ipdBillingDetailsRepository.findFirstByReferenceNo(referenceNo).orElse(null);
        BigDecimal oldAmount = detail == null || detail.getNetAmount() == null
                ? BigDecimal.ZERO : detail.getNetAmount();
        IpdBillingHeader header = ipdBillingHeaderRepository
                .findByInpatientId_InpatientId(booking.getOtBookingRequest().getAdmissionId().getInpatientId())
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(),
                        "IPD billing header not found for OT admission"));
        if (detail == null) {
            detail = IpdBillingDetails.builder().billHeader(header).referenceNo(referenceNo)
                    .quantity(BigDecimal.ONE).createdAt(LocalDateTime.now()).build();
        }
        detail.setCategory(getRequiredIpdBillingCategory(anaesthesiaBillingCategoryCode));
        detail.setItemName("Anaesthesia - " + type.getAnaesthesiaTypeName());
        detail.setServiceDate(LocalDateTime.now());
        detail.setRate(rate);
        detail.setAmount(rate);
        detail.setGstPercent(BigDecimal.ZERO);
        detail.setGstAmount(BigDecimal.ZERO);
        detail.setDiscountAmount(BigDecimal.ZERO);
        detail.setNetAmount(rate);
        detail.setPayable(true);
        detail.setCancelled(false);
        detail.setItemStatus("ACTIVE");
        ipdBillingDetailsRepository.save(detail);
        BigDecimal delta = rate.subtract(oldAmount);
        header.setTotalAmount(zero(header.getTotalAmount()).add(delta));
        header.setNetAmount(zero(header.getNetAmount()).add(delta));
        header.setPatientPayableAmount(zero(header.getPatientPayableAmount()).add(delta));
        header.setOutstandingAmount(zero(header.getOutstandingAmount()).add(delta));
        header.setUpdatedBy(userContextService.getCurrentUserFullNameFromToken());
        header.setUpdatedAt(LocalDateTime.now());
        ipdBillingHeaderRepository.save(header);
    }

    private MasIpdServiceCategory getRequiredIpdBillingCategory(String categoryCode) {
        return ipdServiceCategoryRepository.findByCategoryCodeIgnoreCase(categoryCode)
                .orElseThrow(() -> new SDDException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "IPD billing service category not found for code: " + categoryCode));
    }

    private OtSurgeryAnaesthesiaResponse toAnaesthesiaResponse(Long bookingId, OtSurgeryExecution execution,
                                                                OtSurgeryAnaesthesia anaesthesia) {
        OtSurgeryAnaesthesiaResponse.Execution executionResponse = OtSurgeryAnaesthesiaResponse.Execution.builder()
                .otSurgeryExecutionId(execution.getOtSurgeryExecutionId())
                .otBookingRequestDtId(execution.getOtBookingRequestDt().getOtBookingRequestDtId())
                .surgeryStartDatetime(execution.getSurgeryStartDatetime())
                .surgeryEndDatetime(execution.getSurgeryEndDatetime())
                .executionStatus(execution.getExecutionStatus())
                .procedureConfirmed(execution.getProcedureConfirmed())
                .instrumentCountCorrect(execution.getInstrumentCountCorrect())
                .specimenLabelVerified(execution.getSpecimenLabelVerified())
                .equipmentIssuesReviewed(execution.getEquipmentIssuesReviewed())
                .recoveryConcernsReviewed(execution.getRecoveryConcernsReviewed())
                .signoutRemarks(execution.getSignoutRemarks())
                .signoutVerifiedBy(execution.getSignoutVerifiedBy())
                .signoutVerifiedDatetime(execution.getSignoutVerifiedDatetime())
                .remarks(execution.getRemarks()).status(execution.getStatus())
                .lastChgBy(execution.getLastChgBy()).lastChgDate(execution.getLastChgDate()).build();
        OtSurgeryAnaesthesiaResponse.Anaesthesia anaesthesiaResponse =
                OtSurgeryAnaesthesiaResponse.Anaesthesia.builder()
                        .otSurgeryAnaesthesiaId(anaesthesia.getOtSurgeryAnaesthesiaId())
                        .otSurgeryExecutionId(execution.getOtSurgeryExecutionId())
                        .anaesthesiaTypeId(anaesthesia.getAnaesthesiaTypeId())
                        .anaesthetistId(anaesthesia.getAnaesthetistId())
                        .assistantAnaesthetistId(anaesthesia.getAssistantAnaesthetistId())
                        .anaesthesiaStartDatetime(anaesthesia.getAnaesthesiaStartDatetime())
                        .anaesthesiaEndDatetime(anaesthesia.getAnaesthesiaEndDatetime())
                        .airwayManagement(anaesthesia.getAirwayManagement()).asaGradeId(anaesthesia.getAsaGradeId())
                        .preInductionBp(anaesthesia.getPreInductionBp())
                        .preInductionPulse(anaesthesia.getPreInductionPulse())
                        .preInductionSpo2(anaesthesia.getPreInductionSpo2())
                        .anaesthesiaNotes(anaesthesia.getAnaesthesiaNotes())
                        .complication(anaesthesia.getComplication())
                        .complicationRemarks(anaesthesia.getComplicationRemarks())
                        .status(anaesthesia.getStatus()).lastChgBy(anaesthesia.getLastChgBy())
                        .lastChgDate(anaesthesia.getLastChgDate()).build();
        return OtSurgeryAnaesthesiaResponse.builder().otBookingId(bookingId)
                .execution(executionResponse).anaesthesia(anaesthesiaResponse).build();
    }

    private OtSurgeryExecutionResponse toExecutionResponse(Long bookingId, OtSurgeryExecution execution) {
        return OtSurgeryExecutionResponse.builder()
                .otBookingId(bookingId)
                .otSurgeryExecutionId(execution.getOtSurgeryExecutionId())
                .otBookingRequestDtId(execution.getOtBookingRequestDt().getOtBookingRequestDtId())
                .surgeryStartDatetime(execution.getSurgeryStartDatetime())
                .surgeryEndDatetime(execution.getSurgeryEndDatetime())
                .executionStatus(execution.getExecutionStatus())
                .procedureConfirmed(execution.getProcedureConfirmed())
                .instrumentCountCorrect(execution.getInstrumentCountCorrect())
                .specimenLabelVerified(execution.getSpecimenLabelVerified())
                .equipmentIssuesReviewed(execution.getEquipmentIssuesReviewed())
                .recoveryConcernsReviewed(execution.getRecoveryConcernsReviewed())
                .signoutRemarks(execution.getSignoutRemarks())
                .signoutVerifiedBy(execution.getSignoutVerifiedBy())
                .signoutVerifiedDatetime(execution.getSignoutVerifiedDatetime())
                .remarks(execution.getRemarks())
                .status(execution.getStatus())
                .lastChgBy(execution.getLastChgBy())
                .lastChgDate(execution.getLastChgDate())
                .build();
    }

    private SurgeryDrugConsumableTemplateResponse toTemplateResponse(SurgeryDrugConsumableTemplateHd header,
            List<SurgeryDrugConsumableTemplateDt> details, String sectionCode) {
        List<SurgeryDrugConsumableTemplateResponse.Item> itemResponses = details.stream().map(detail -> {
            MasStoreItem item = detail.getItem();
            MasStoreSection section = item.getSectionId();
            return SurgeryDrugConsumableTemplateResponse.Item.builder()
                    .templateDetailId(detail.getTemplateDetailId()).itemId(item.getItemId())
                    .itemName(item.getNomenclature()).pvmsNo(item.getPvmsNo())
                    .sectionId(section == null ? null : section.getSectionId())
                    .sectionCode(section == null ? null : section.getSectionCode())
                    .sectionName(section == null ? null : section.getSectionName())
                    .plannedQuantity(detail.getPlannedQuantity())
                    .unitId(item.getUnitAU() == null ? null : item.getUnitAU().getUnitId())
                    .unitName(item.getUnitAU() == null ? null : item.getUnitAU().getUnitName())
                    .status(detail.getStatus()).build();
        }).toList();
        return SurgeryDrugConsumableTemplateResponse.builder().templateId(header.getTemplateId())
                .surgeryId(header.getSurgery().getSurgeryId()).surgeryName(header.getSurgery().getSurgeryName())
                .templateName(header.getTemplateName()).description(header.getDescription())
                .sectionCode(sectionCode).status(header.getStatus()).lastChgBy(header.getLastChgBy())
                .lastChgDate(header.getLastChgDate()).items(itemResponses).build();
    }

    private boolean isDrug(MasStoreItem item) {
        return item.getSectionId() != null && drugSectionCode.equalsIgnoreCase(item.getSectionId().getSectionCode());
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return activeStatus;
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!activeStatus.equalsIgnoreCase(normalized) && !inactiveStatus.equalsIgnoreCase(normalized)) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "status must be Y or N");
        }
        return activeStatus.equalsIgnoreCase(normalized) ? activeStatus : inactiveStatus;
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private <T> ApiResponse<T> success(T data) {
        return ResponseUtils.createSuccessResponse(data, new TypeReference<>() {});
    }
}
