package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.DgMasInvestigation;
import com.hims.entity.MasInvestigationPriceDetails;
import com.hims.entity.repository.DgMasInvestigationRepository;
import com.hims.entity.repository.MasInvestigationPriceDetailsRepository;
import com.hims.request.MasInvestigationPriceDetailsRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasInvestigationPriceDetailsResponse;
import com.hims.service.MasInvestigationPriceDetailsService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasInvestigationPriceDetailsServiceImpl implements MasInvestigationPriceDetailsService {

    @Autowired
    private MasInvestigationPriceDetailsRepository repository;

    @Autowired
    private DgMasInvestigationRepository investigationRepository;

    @Override
    public ApiResponse<List<MasInvestigationPriceDetailsResponse>> getAllPriceDetails(int flag) {
        try {
            List<MasInvestigationPriceDetails> priceDetailsList;
            if (flag == 1) {
                priceDetailsList = repository.findByStatusIgnoreCase("Y");
            } else if (flag == 0) {
                priceDetailsList = repository.findByStatusInIgnoreCase(List.of("Y", "N"));
            } else {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Invalid flag value. Use 0 for all, 1 for active.",
                        400
                );
            }

            // Check if the list is null
            if (priceDetailsList == null) {
                priceDetailsList = new ArrayList<>();
            }

            List<MasInvestigationPriceDetailsResponse> responseList = priceDetailsList.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            // Return a meaningful error response
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Error retrieving price details: " + e.getMessage(),
                    500
            );
        }
    }

    @Override
    public ApiResponse<MasInvestigationPriceDetailsResponse> findById(Long id) {
        return repository.findById(id)
                .map(details -> ResponseUtils.createSuccessResponse(mapToResponse(details), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Price details not found with id: " + id, 404));
    }

    @Override
    public ApiResponse<List<MasInvestigationPriceDetailsResponse>> findByInvestigationId(Long investigationId) {
        List<MasInvestigationPriceDetails> detailsList = repository.findByInvestigation_investigationId(investigationId);
        List<MasInvestigationPriceDetailsResponse> responses = detailsList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasInvestigationPriceDetailsResponse> addPriceDetails(MasInvestigationPriceDetailsRequest request) {
        Optional<DgMasInvestigation> investigationOpt = investigationRepository.findById(request.getInvestigationId());

        if (investigationOpt.isEmpty()) {
            return ResponseUtils.createNotFoundResponse(
                    "Investigation not found with id: " + request.getInvestigationId(),
                    HttpStatus.NOT_FOUND.value()
            );
        }

        DgMasInvestigation investigation = investigationOpt.get();

        // Check for overlapping date ranges
        List<MasInvestigationPriceDetails> existingDetails = repository
                .findByInvestigation_InvestigationId(investigation.getInvestigationId());

        boolean isOverlapping = existingDetails.stream().anyMatch(existing ->
                !request.getToDt().isBefore(existing.getFromDate()) && !request.getFromDt().isAfter(existing.getToDate())
        );

        if (isOverlapping) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Duplicate date range found for this Price Investigation .",
                    HttpStatus.CONFLICT.value()
            );
        }

        // Create and save new entry
        MasInvestigationPriceDetails details = new MasInvestigationPriceDetails();
        details.setInvestigation(investigation);
        details.setFromDate(request.getFromDt());
        details.setToDate(request.getToDt());
        details.setLastChgDt(LocalTime.now());
        details.setPrice(request.getPrice());
        details.setStatus("y"); //

        MasInvestigationPriceDetails saved = repository.save(details);
        return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {});
    }


    @Override
    @Transactional
    public ApiResponse<MasInvestigationPriceDetailsResponse> updatePriceDetails(Long id, MasInvestigationPriceDetailsRequest request) {
        try {
            // 1. Fetch the record to update
            Optional<MasInvestigationPriceDetails> optionalExisting = repository.findById(id);
            if (optionalExisting.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Record not found with ID: " + id, HttpStatus.NOT_FOUND.value());
            }

            MasInvestigationPriceDetails currentRecord = optionalExisting.get();

            // 2. Validate investigation ID
            Long requestedInvestigationId = request.getInvestigationId();
            Optional<DgMasInvestigation> investigationOpt = investigationRepository.findById(requestedInvestigationId);
            if (investigationOpt.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("Investigation not found with ID: " + requestedInvestigationId, HttpStatus.NOT_FOUND.value());
            }

            // 3. Validate dates
            if (request.getFromDt().isAfter(request.getToDt())) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "From date cannot be after To date",
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            // 4. For the same investigation ID, don't allow date range modifications
            if (currentRecord.getInvestigation().getInvestigationId().equals(requestedInvestigationId)) {
                if (!currentRecord.getFromDate().equals(request.getFromDt()) ||
                        !currentRecord.getToDate().equals(request.getToDt())) {
                    return ResponseUtils.createFailureResponse(
                            null,
                            new TypeReference<>() {},
                            "Duplicate date range found for this Price Investigation. Please create a new record for a different date range.",
                            HttpStatus.BAD_REQUEST.value()
                    );
                }
            } else {
                // 5. If changing investigation ID, check for overlapping date ranges
                List<MasInvestigationPriceDetails> existingRecords = repository
                        .findByInvestigation_investigationId(requestedInvestigationId);

                boolean hasOverlap = existingRecords.stream()
                        .anyMatch(existing ->
                                !request.getToDt().isBefore(existing.getFromDate()) &&
                                        !request.getFromDt().isAfter(existing.getToDate())
                        );

                if (hasOverlap) {
                    return ResponseUtils.createFailureResponse(
                            null,
                            new TypeReference<>() {},
                            "Duplicate date range found for investigation ID: " + requestedInvestigationId,
                            HttpStatus.CONFLICT.value()
                    );
                }
            }

            // 6. Update the record
            currentRecord.setInvestigation(investigationOpt.get());
            currentRecord.setFromDate(request.getFromDt());
            currentRecord.setToDate(request.getToDt());
            currentRecord.setPrice(request.getPrice());
            currentRecord.setStatus(request.getStatus());
            currentRecord.setLastChgDt(LocalDateTime.now().toLocalTime());

            // 7. Save and return response
            MasInvestigationPriceDetails updated = repository.save(currentRecord);
            return ResponseUtils.createSuccessResponse(mapToResponse(updated), new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Error updating price details: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }


    @Override
    @Transactional
    public ApiResponse<MasInvestigationPriceDetailsResponse> changeStatus(Long id, String status) {
        Optional<MasInvestigationPriceDetails> detailsOpt = repository.findById(id);

        if (detailsOpt.isPresent()) {
            if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Invalid status value. Use 'Y' for Active and 'N' for Inactive.",
                        400
                );
            }

            MasInvestigationPriceDetails details = detailsOpt.get();
            details.setStatus(status);
            details.setLastChgDt(LocalTime.now());

            MasInvestigationPriceDetails updated = repository.save(details);
            return ResponseUtils.createSuccessResponse(mapToResponse(updated), new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Price details not found with id: " + id, 404);
        }
    }

    private MasInvestigationPriceDetailsResponse mapToResponse(MasInvestigationPriceDetails entity) {
        MasInvestigationPriceDetailsResponse response = new MasInvestigationPriceDetailsResponse();
        response.setId(entity.getId());
        response.setInvestigationId(entity.getInvestigation().getInvestigationId());
        response.setFromDt(entity.getFromDate());
        response.setToDt(entity.getToDate());
        response.setLastChgDt(entity.getLastChgDt());
        response.setPrice(entity.getPrice());
        response.setStatus(entity.getStatus());
        return response;
    }
}
