package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.mapper.OtChecklistMapper;
import com.hims.request.OtChecklistSubmitRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OtChecklistSubmitResponse;
import com.hims.response.UserContext;
import com.hims.service.OtChecklistSubmissionService;
import com.hims.service.UserContextService;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OtChecklistSubmissionServiceImpl implements OtChecklistSubmissionService {
    private final OtBookingRepository otBookingRepository;
    private final OtChecklistRepository otChecklistRepository;
    private final OtChecklistItemRepository itemRepository;
    private final MasOtChecklistRepository masterRepository;
    private final UserContextService userContextService;
    private final OtChecklistMapper mapper;

    @Override
    @Transactional
    public ApiResponse<OtChecklistSubmitResponse> submit(OtChecklistSubmitRequest request) {
        String stage = request.getChecklistStage().trim().toUpperCase(Locale.ROOT);
        String checklistStatusCode = request.getChecklistStatus().trim().toUpperCase(Locale.ROOT);
        String checklistStatus = "Y".equals(checklistStatusCode) ? "COMPLETED" : "DRAFT";
        OtBooking booking = otBookingRepository.findById(request.getOtBookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OT booking not found"));

        List<MasOtChecklist> masters = masterRepository
                .findActiveByStageOrderByDisplayOrder(stage, "Y");
        if (masters.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No active checklist items configured for stage " + stage);
        }
        Map<Long, MasOtChecklist> mastersById = masters.stream()
                .collect(Collectors.toMap(MasOtChecklist::getChecklistId, Function.identity()));
        Map<Long, OtChecklistSubmitRequest.OtChecklistItemSubmitRequest> submittedItems = new LinkedHashMap<>();
        for (OtChecklistSubmitRequest.OtChecklistItemSubmitRequest item : request.getItems()) {
            if (submittedItems.putIfAbsent(item.getChecklistId(), item) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate checklistId: " + item.getChecklistId());
            }
            MasOtChecklist master = mastersById.get(item.getChecklistId());
            if (master == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Checklist item " + item.getChecklistId() + " is inactive or does not belong to stage " + stage);
            }
            String verification = normalizeVerification(item.getVerificationStatus());
            if ("NA".equals(verification) && !"Y".equalsIgnoreCase(master.getAllowNa())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "NA is not allowed for checklist item " + master.getChecklistCode());
            }
        }

        String actor = currentActor();
        LocalDateTime now = LocalDateTime.now();
        OtChecklist header = otChecklistRepository
                .findByOtBooking_OtBookingIdAndChecklistStage(request.getOtBookingId(), stage)
                .orElseGet(() -> OtChecklist.builder()
                        .otBooking(booking)
                        .checklistStage(stage)
                        .status("Y")
                        .lastChgDate(now)
                        .build());
        List<OtChecklistItem> existingItems = header.getOtChecklistId() == null
                ? Collections.emptyList()
                : itemRepository.findByOtChecklist_OtChecklistIdOrderByOtChecklistItemId(header.getOtChecklistId());
        if ("COMPLETED".equals(checklistStatus)) {
            validateCompletedItems(masters, submittedItems, existingItems);
        }

        header.setChecklistStatus(checklistStatus);
        if (request.getRemarks() != null) header.setRemarks(request.getRemarks());
        header.setStatus("Y");
        header.setLastChgBy(actor);
        header.setLastChgDate(now);
        header.setVerifiedBy("COMPLETED".equals(checklistStatus) ? actor : null);
        header.setVerifiedDatetime("COMPLETED".equals(checklistStatus) ? now : null);
        header = otChecklistRepository.saveAndFlush(header);

        Map<Long, OtChecklistItem> existingByChecklistId = existingItems.stream()
                .collect(Collectors.toMap(item -> item.getChecklist().getChecklistId(), Function.identity()));
        List<OtChecklistItem> itemsToSave = new ArrayList<>();
        for (OtChecklistSubmitRequest.OtChecklistItemSubmitRequest submitted : submittedItems.values()) {
            MasOtChecklist master = mastersById.get(submitted.getChecklistId());
            String verification = normalizeVerification(submitted.getVerificationStatus());
            OtChecklistItem item = existingByChecklistId.getOrDefault(master.getChecklistId(),
                    OtChecklistItem.builder().otChecklist(header).checklist(master).build());
            item.setVerificationStatus(verification);
            item.setRemarks(submitted.getRemarks());
            item.setVerifiedBy(verification == null ? null : actor);
            item.setVerifiedDatetime(verification == null ? null : now);
            itemsToSave.add(item);
        }
        itemRepository.saveAll(itemsToSave);
        updateBookingChecklistFlag(booking, checklistStatusCode, actor, now);
        List<OtChecklistItem> responseItems = itemRepository
                .findByOtChecklist_OtChecklistIdOrderByOtChecklistItemId(header.getOtChecklistId());
        return ResponseUtils.createSuccessResponse(mapper.toResponse(header, responseItems), new TypeReference<>() {});
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<OtChecklistSubmitResponse>> getByBookingId(Long otBookingId) {
        if (!otBookingRepository.existsById(otBookingId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "OT booking not found");
        }
        List<OtChecklistSubmitResponse> response = otChecklistRepository
                .findByOtBooking_OtBookingIdAndStatusOrderByChecklistStageAsc(otBookingId, "Y")
                .stream()
                .map(header -> mapper.toResponse(header,
                        itemRepository.findByOtChecklist_OtChecklistIdOrderByOtChecklistItemId(header.getOtChecklistId())))
                .toList();
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
    }

    private void validateCompletedItems(List<MasOtChecklist> masters,
                                        Map<Long, OtChecklistSubmitRequest.OtChecklistItemSubmitRequest> submittedItems,
                                        List<OtChecklistItem> existingItems) {
        Map<Long, OtChecklistItem> existingByChecklistId = existingItems.stream()
                .collect(Collectors.toMap(item -> item.getChecklist().getChecklistId(), Function.identity()));
        for (MasOtChecklist master : masters) {
            OtChecklistSubmitRequest.OtChecklistItemSubmitRequest submitted = submittedItems.get(master.getChecklistId());
            OtChecklistItem saved = existingByChecklistId.get(master.getChecklistId());
            String value = submitted != null ? normalizeVerification(submitted.getVerificationStatus())
                    : saved == null ? null : normalizeVerification(saved.getVerificationStatus());
            if (value == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Every checklist item must be answered before completing: " + master.getChecklistCode());
            }
            if ("NA".equals(value) && !"Y".equalsIgnoreCase(master.getAllowNa())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "NA is not allowed for checklist item " + master.getChecklistCode());
            }
            if ("Y".equalsIgnoreCase(master.getIsMandatory())
                    && "NO".equals(value)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Mandatory checklist item cannot be marked NO: " + master.getChecklistCode());
            }
        }
    }

    private String normalizeVerification(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String currentActor() {
        UserContext user = userContextService.getCurrentUserContext();
        if (user != null && user.getUserFullName() != null && !user.getUserFullName().isBlank()) return user.getUserFullName();
        String actor = userContextService.getCurrentUserFullNameFromToken();
        return actor == null || actor.isBlank() ? "SYSTEM" : actor;
    }

    private void updateBookingChecklistFlag(OtBooking booking, String checklistStatusCode,
                                            String actor, LocalDateTime now) {
        booking.setIsChecklistDone(checklistStatusCode);
        booking.setLastChgBy(actor);
        booking.setLastChgDate(now);
        otBookingRepository.save(booking);
    }
}
