package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.User;
import com.hims.entity.repository.UserRepo;
import com.hims.entity.repository.VisitRepository;
import com.hims.projection.OpdPreConsultationProjection;
import com.hims.projection.PatientWaitingListProjection;
import com.hims.response.ApiResponse;
import com.hims.response.OpdPreConsultationResponse;
import com.hims.response.PatientWaitingListResponse;
import com.hims.service.OPDService;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.hims.helperUtil.ConverterUtils.ageCalculator;

/**
 * OPD Service Implementation
 *
 * Provides business logic for OPD operations including retrieval of pending
 * pre-consultations and patient waiting lists filtered by hospital and status.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpdServiceImpl implements OPDService {

    private final UserRepo userRepository;
    private final VisitRepository visitRepository;

    /**
     * Retrieves pending pre-consultations with database-level pagination.
     *
     * @param pageable pagination information including page, size, and sort
     * @return ApiResponse containing paginated pending pre-consultation responses
     */
    @Override
    public ApiResponse<Page<OpdPreConsultationResponse>> getPendingPreConsultations(Pageable pageable) {
        log.info("Fetching pending pre-consultations with pagination - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getHospital() == null) {
                log.warn("Current user or hospital not found");
                Page<OpdPreConsultationResponse> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
                return ResponseUtils.createSuccessResponse(emptyPage, new TypeReference<>() {});
            }

            Long hospitalId = currentUser.getHospital().getId();
            log.debug("Fetching pre-consultations for hospital ID: {} with pagination", hospitalId);

            // Call repository method with Pageable for database-level pagination
            Page<OpdPreConsultationProjection> projectionPage = visitRepository
                    .findPendingPreConsultationsByHospitalPaged(
                            hospitalId,
                            AppConstants.STATUS_N.toLowerCase(),
                            AppConstants.STATUS_Y.toLowerCase(),
                            pageable
                    );

            // Map projections to responses
            Page<OpdPreConsultationResponse> responsePage = projectionPage
                    .map(this::mapOpdPreConsultationProjectionToResponse);

            log.info("Successfully fetched {} pre-consultations for page {}, total records: {}",
                    responsePage.getNumberOfElements(),
                    pageable.getPageNumber(),
                    responsePage.getTotalElements());

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching pending pre-consultations with pagination", e);
            return ResponseUtils.createFailureResponse(
                    new PageImpl<>(new ArrayList<>(), pageable, 0),
                    new TypeReference<>() {},
                    "Error fetching pending pre-consultations: " + e.getMessage(),
                    500
            );
        }
    }

    /**
     * Retrieves the patient waiting list for the current hospital.
     *
     * @return ApiResponse containing list of patients in waiting list
     */
    @Override
    public ApiResponse<List<PatientWaitingListResponse>> getWaitingList() {
        log.info("Fetching patient waiting list for current hospital");
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getHospital() == null) {
                log.warn("Current user or hospital not found");
                return ResponseUtils.createFailureResponse(
                        new ArrayList<>(),
                        new TypeReference<>() {},
                        "User or hospital not found",
                        400
                );
            }

            Long hospitalId = currentUser.getHospital().getId();
            log.debug("Fetching waiting list for hospital ID: {}", hospitalId);

            List<PatientWaitingListProjection> projections = visitRepository
                    .findWaitingPatientsByHospital(
                            hospitalId,
                            AppConstants.STATUS_Y.toLowerCase(),
                            AppConstants.STATUS_Y.toLowerCase()
                    );

            List<PatientWaitingListResponse> responseList = projections.stream()
                    .map(this::mapPatientWaitingListProjectionToResponse)
                    .collect(Collectors.toList());

            log.info("Successfully fetched {} patients from waiting list", responseList.size());
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching patient waiting list", e);
            return ResponseUtils.createFailureResponse(
                    new ArrayList<>(),
                    new TypeReference<>() {},
                    "Error fetching patient waiting list: " + e.getMessage(),
                    500
            );
        }
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return User object or null if not found
     */
    private User getCurrentUser() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUserName(username);
            if (user == null) {
                log.warn("User not found in database for username: {}", username);
            }
            return user;
        } catch (Exception e) {
            log.error("Error retrieving current user from security context", e);
            return null;
        }
    }

    /**
     * Maps OpdPreConsultationProjection to OpdPreConsultationResponse.
     *
     * @param projection the projection object to map
     * @return mapped response object
     */
    private OpdPreConsultationResponse mapOpdPreConsultationProjectionToResponse(
            OpdPreConsultationProjection projection) {
        OpdPreConsultationResponse response = new OpdPreConsultationResponse();
        response.setVisitId(projection.getVisitId());
        response.setPatientId(projection.getPatientId());
        response.setPatientName(projection.getPatientName());
        response.setAge(projection.getPatientAge());
        response.setGender(projection.getGender());
        response.setDepartmentId(String.valueOf(projection.getDepartmentId()));
        response.setDepartmentName(projection.getDepartmentName());
        response.setMobleNumber(projection.getMobileNumber());
        response.setVisitType(projection.getVisitType());
        response.setDoctorId(projection.getDoctorId());
        response.setDoctorName(projection.getDoctorName());
        response.setAppointmentDate(projection.getAppointmentDate() != null
                ? projection.getAppointmentDate().toString()
                : "");
        response.setAppointmentTime(projection.getAppointmentTime());
        response.setTokenNumber(String.valueOf(projection.getTokenNumber()));
        return response;
    }

    /**
     * Maps PatientWaitingListProjection to PatientWaitingListResponse.
     *
     * @param projection the projection object to map
     * @return mapped response object
     */
    private PatientWaitingListResponse mapPatientWaitingListProjectionToResponse(
            PatientWaitingListProjection projection) {
        PatientWaitingListResponse response = new PatientWaitingListResponse();
        response.setPatientId(projection.getPatientId());
        response.setVisitId(projection.getVisitId());
        response.setTokenNo(String.valueOf(projection.getTokenNo()));
        response.setMobileNo(projection.getMobileNumber());
        response.setPatientName(projection.getPatientName());
        response.setRelation(projection.getRelation());
        response.setAge(ageCalculator(projection.getDob()));
        response.setDob(projection.getDob());
        response.setGender(projection.getGender());
        response.setVisitType(projection.getVisitType());
        return response;
    }
}