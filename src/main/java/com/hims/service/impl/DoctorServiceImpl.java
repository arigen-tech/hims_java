package com.hims.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.repository.VisitRepository;
import com.hims.projection.VisitSummaryProjection;
import com.hims.response.ApiResponse;
import com.hims.service.DoctorService;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {


    private final VisitRepository visitRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<VisitSummaryProjection>> getTodayVisitSummary(Long hospitalId,
                                            Long doctorId,
                                            Long departmentId) {



        LocalDateTime startOfDay     = LocalDate.now().atStartOfDay();
        LocalDateTime startOfNextDay = LocalDate.now().plusDays(1).atStartOfDay();

        return ResponseUtils.createSuccessResponse(visitRepository.getVisitSummary(hospitalId,
                doctorId,
                departmentId,
                startOfDay,
                startOfNextDay
        ), new TypeReference<>() {});
    }
}
