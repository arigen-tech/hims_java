package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasOpdSession;
import com.hims.entity.User;
import com.hims.entity.repository.MasOpdSessionRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasOpdSessionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOpdSessionResponse;
import com.hims.service.MasOpdSessionService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasOpdSessionServiceImpl implements MasOpdSessionService {

    private static final Logger log = LoggerFactory.getLogger(MasOpdSessionServiceImpl.class);

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Autowired
    private MasOpdSessionRepository masOpdSessionRepository;

    @Autowired
    private UserRepo userRepo;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }

    @Override
    public ApiResponse<List<MasOpdSessionResponse>> getAllOpdSessions(int flag) {
        List<MasOpdSession> sessions;

        if (flag == 1) {
            sessions = masOpdSessionRepository.findByStatusIgnoreCaseOrderBySessionNameAsc("Y");
        } else if (flag == 0) {
            sessions = masOpdSessionRepository.findByStatusInIgnoreCaseOrderByLastChgDtDesc(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasOpdSessionResponse> responses = sessions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }


    private MasOpdSessionResponse convertToResponse(MasOpdSession session) {
        MasOpdSessionResponse response = new MasOpdSessionResponse();
        response.setId(session.getId());
        response.setSessionName(session.getSessionName());
        response.setFromTime(session.getFromTime());
        response.setEndTime(session.getEndTime());
        response.setStatus(session.getStatus());
        response.setLastChgBy(session.getLasChgBy());
        response.setLastChgDt(session.getLastChgDt());

        return response;
    }

    public ApiResponse<MasOpdSessionResponse> findById(Long id) {
        Optional<MasOpdSession> session = masOpdSessionRepository.findById(id);
        return session.map(value ->
                ResponseUtils.createSuccessResponse(
                        convertToResponse(value),
                        new TypeReference<MasOpdSessionResponse>() {}
                )
        ).orElseGet(() -> ResponseUtils.createNotFoundResponse("Session not found", 404));
    }

    // Add new OPD Session
    @Override
    public ApiResponse<MasOpdSessionResponse> addSession(MasOpdSessionRequest request) {
        try{
            MasOpdSession session = new MasOpdSession();

            session.setSessionName(request.getSessionName());

            // Convert String to LocalTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            session.setFromTime(LocalTime.parse(request.getFromTime(), formatter));
            session.setEndTime(LocalTime.parse(request.getEndTime(), formatter));

            session.setStatus("y");
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            session.setLasChgBy(String.valueOf(currentUser.getUserId()));
            session.setLastChgDt(LocalDate.now());

            masOpdSessionRepository.save(session);
            return ResponseUtils.createSuccessResponse(convertToResponse(session), new TypeReference<MasOpdSessionResponse>() {
            });
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @Override
    public ApiResponse<MasOpdSessionResponse> updateSession(Long id, MasOpdSessionRequest request) {
        try{
            Optional<MasOpdSession> sessionOptional = masOpdSessionRepository.findById(id);

            if (sessionOptional.isPresent()) {
                MasOpdSession session = sessionOptional.get();
                session.setSessionName(request.getSessionName());

                // Convert String to LocalTime
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                session.setFromTime(LocalTime.parse(request.getFromTime(), formatter));
                session.setEndTime(LocalTime.parse(request.getEndTime(), formatter));

                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                session.setLasChgBy(String.valueOf(currentUser.getUserId()));
                session.setLastChgDt(LocalDate.now());

                masOpdSessionRepository.save(session);
                return ResponseUtils.createSuccessResponse(convertToResponse(session), new TypeReference<MasOpdSessionResponse>() {
                });
            }

            return ResponseUtils.createNotFoundResponse("Session not found", HttpStatus.NOT_FOUND.value());
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // Change status of an OPD Session
    @Transactional
    public ApiResponse<MasOpdSessionResponse> changeOpdSessionStatus(Long id, String status) {
        try{
            Optional<MasOpdSession> existingSessionOpt = masOpdSessionRepository.findById(id);
            if (existingSessionOpt.isPresent()) {
                MasOpdSession existingSession = existingSessionOpt.get();

                if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n")) {
                    return ResponseUtils.createFailureResponse(
                            null,
                            new TypeReference<MasOpdSessionResponse>() {
                            },
                            "Invalid status value. Use 'Y' for Active and 'N' for Inactive.",
                            400
                    );
                }

                existingSession.setStatus(status);
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                existingSession.setLasChgBy(String.valueOf(currentUser.getUserId()));
                existingSession.setLastChgDt(LocalDate.now());
                MasOpdSession updatedSession = masOpdSessionRepository.save(existingSession);

                return ResponseUtils.createSuccessResponse(
                        convertToResponse(updatedSession),
                        new TypeReference<MasOpdSessionResponse>() {
                        }
                );
            } else {
                return ResponseUtils.createNotFoundResponse("Session not found", 404);
            }
        }
        catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}