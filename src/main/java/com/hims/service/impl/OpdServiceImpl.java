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
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    @Autowired
    AuthUtil authUtil;

    /**
     * Retrieves pending pre-consultations with database-level pagination.
     *
     * @param pageable pagination information including page, size, and sort
     * @return ApiResponse containing paginated pending pre-consultation responses
     */
    @Override
    public ApiResponse<Page<OpdPreConsultationResponse>> getPendingPreConsultations(
            Pageable pageable,
            String patientName,
            String mobileNumber
    ) {
        try {
            User currentUser = getCurrentUser();

            if (currentUser == null || currentUser.getHospital() == null) {
                Page<OpdPreConsultationResponse> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
                return ResponseUtils.createSuccessResponse(emptyPage, new TypeReference<>() {});
            }

            Long hospitalId = currentUser.getHospital().getId();
            Long departmentId = authUtil.getCurrentDepartmentId();

            Page<OpdPreConsultationProjection> projectionPage =
                    visitRepository.findPendingPreConsultationsByHospitalPaged(
                            hospitalId,
                            departmentId,
                            AppConstants.STATUS_N.toLowerCase(),
                            AppConstants.STATUS_Y.toLowerCase(),
                            AppConstants.STATUS_N.toLowerCase(),
                            patientName,
                            mobileNumber,
                            pageable
                    );

            Page<OpdPreConsultationResponse> responsePage = projectionPage.map(this::mapOpdPreConsultationProjectionToResponse);
            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching pending pre-consultations: ",e);
            return ResponseUtils.createFailureResponse(new PageImpl<>(new ArrayList<>(), pageable, 0), new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
    /**
     * Retrieves the patient waiting list for the current hospital.
     *
     * @return ApiResponse containing list of patients in waiting list
     */
    @Override
    public ApiResponse<Page<PatientWaitingListResponse>> getWaitingList(
            Pageable pageable,
            String patientName,
            String mobileNumber
    ) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getHospital() == null) {
                return ResponseUtils.createFailureResponse(new PageImpl<>(new ArrayList<>(), pageable, 0), new TypeReference<>() {}, "User or hospital not found",
                        400
                );
            }

            Long hospitalId = currentUser.getHospital().getId();
            Long departmentId = authUtil.getCurrentDepartmentId();

            Page<PatientWaitingListProjection> projectionPage =
                    visitRepository.findWaitingPatientsByHospital(
                            hospitalId,
                            departmentId,
                            AppConstants.STATUS_Y.toLowerCase(),
                            AppConstants.STATUS_Y.toLowerCase(),
                            AppConstants.STATUS_N.toLowerCase(),
                            patientName,
                            mobileNumber,
                            pageable
                    );

            Page<PatientWaitingListResponse> responsePage = projectionPage.map(this::mapPatientWaitingListProjectionToResponse);
            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error fetching patient waiting list: ",e);
            return ResponseUtils.createFailureResponse(new PageImpl<>(new ArrayList<>(), pageable, 0), new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value()
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
        response.setDepartmentName(projection.getDepartmentName());
        response.setOpdDate(projection.getOpdDate());
        return response;
    }
}