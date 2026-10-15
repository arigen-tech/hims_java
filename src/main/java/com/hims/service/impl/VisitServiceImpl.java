package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.repository.VisitRepository;
import com.hims.response.ApiResponse;
import com.hims.response.PatientVisitQueueResponse;
import com.hims.service.VisitService;
import com.hims.utils.HMISUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepository;
    @Override
    public ApiResponse<String> changeVisitStatus(Long visitId, String visitStatus) {
        try {
            log.info("changeVisitStatus method started for visitId: {} to status: {}", visitId, visitStatus);
            visitRepository.findById(visitId).ifPresent(visit -> {
                visit.setVisitStatus(visitStatus);
                visit.setLastChgDate(HMISUtil.getCurrentLocalDateTime());
                visitRepository.save(visit);
            });
            log.info("changeVisitStatus method ended for visitId: {} to status: {}", visitId, visitStatus);
            return ResponseUtils.createSuccessResponse("Visit status updated successfully",
                    new TypeReference<>() {}
            );
        }catch (Exception e) {
            log.error("Error occurred changeVisitStatus method for visitId: {} to status: {}. Error: {}", visitId, visitStatus, e.getMessage());
            return ResponseUtils.createFailureResponse(null,
                    new TypeReference<>() {},
                    "Failed to update visit status: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }

    }

    @Override
    public ApiResponse<Page<PatientVisitQueueResponse>> getPatientVisitQueue(Long hospitalId, Long doctorId, Long departmentId,int page, int size) {

        try {
            log.info("getPatientVisitQueue method started for hospitalId: {} to doctorId: {} and departmentId: {}", hospitalId, doctorId, departmentId);
            LocalDateTime startDate = LocalDate.now().atStartOfDay();
            LocalDateTime endDate = LocalDate.now().plusDays(1).atStartOfDay();
            Pageable pageable= PageRequest.of(page,size);

            Page<PatientVisitQueueResponse> queue =
                    visitRepository.getPatientVisitQueue(
                            hospitalId,
                            doctorId,
                            departmentId,
                            startDate,
                            endDate,
                            pageable
                    );
            return ResponseUtils.createSuccessResponse(queue,
                    new TypeReference<>() {
                    }
            );
        } catch (Exception e) {
            log.error("Error occurred getPatientVisitQueue method for hospitalId: {} to doctorId: {} and departmentId: {}. Error: {}", hospitalId, doctorId, departmentId, e.getMessage());
            return ResponseUtils.createFailureResponse(null,
                    new TypeReference<>() {
                    },
                    "Failed to get patient visit queue: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
}
