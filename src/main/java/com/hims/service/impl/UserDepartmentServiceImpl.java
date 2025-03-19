package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.UserDepartment;
import com.hims.entity.repository.UserDepartmentRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.User;
import com.hims.entity.MasDepartment;
import com.hims.request.UserDepartmentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.UserDepartmentResponse;
import com.hims.service.UserDepartmentService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserDepartmentServiceImpl implements UserDepartmentService {

    @Autowired
    private UserDepartmentRepository userDepartmentRepository;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private MasDepartmentRepository masDepartmentRepository;

    private String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public ApiResponse<List<UserDepartmentResponse>> getAllUserDepartments() {
        List<UserDepartment> userDepartments = userDepartmentRepository.findAll();
        List<UserDepartmentResponse> responses = userDepartments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    private UserDepartmentResponse convertToResponse(UserDepartment userDepartment) {
        UserDepartmentResponse response = new UserDepartmentResponse();
        response.setId(userDepartment.getId());
        response.setLastChgBy(userDepartment.getLastChgBy());
        response.setLasUpdatedBy(userDepartment.getLasUpdatedBy());

        if (userDepartment.getUser() != null) {
            response.setUserId(userDepartment.getUser().getUserId());
            response.setUsername(userDepartment.getUser().getUsername());
        }
        if (userDepartment.getDepartment() != null) {
            response.setDepartmentId(userDepartment.getDepartment().getId());
            response.setDepartmentName(userDepartment.getDepartment().getDepartmentName());
        }
        return response;
    }

    public ApiResponse<UserDepartmentResponse> addUserDepartment(UserDepartmentRequest request) {
        UserDepartment userDepartment = new UserDepartment();
        userDepartment.setLastChgBy(request.getLastChgBy());
        userDepartment.setLasUpdatedBy(OffsetDateTime.now());

        if (request.getUserId() != null) {
            Optional<User> user = userRepository.findById(request.getUserId());
            user.ifPresent(userDepartment::setUser);
        }

        if (request.getDepartmentId() != null) {
            Optional<MasDepartment> department = masDepartmentRepository.findById(request.getDepartmentId());
            department.ifPresent(userDepartment::setDepartment);
        }

        UserDepartment savedUserDepartment = userDepartmentRepository.save(userDepartment);
        return ResponseUtils.createSuccessResponse(convertToResponse(savedUserDepartment), new TypeReference<>() {});
    }

    public ApiResponse<UserDepartmentResponse> updateUserDepartment(Long id, UserDepartmentResponse details) {
        Optional<UserDepartment> existingOpt = userDepartmentRepository.findById(id);
        if (existingOpt.isPresent()) {
            UserDepartment existing = existingOpt.get();
            existing.setLastChgBy(details.getLastChgBy());
            existing.setLasUpdatedBy(OffsetDateTime.now());

            if (details.getUserId() != null) {
                Optional<User> user = userRepository.findById(details.getUserId());
                user.ifPresent(existing::setUser);
            }

            if (details.getDepartmentId() != null) {
                Optional<MasDepartment> department = masDepartmentRepository.findById(details.getDepartmentId());
                department.ifPresent(existing::setDepartment);
            }

            UserDepartment updated = userDepartmentRepository.save(existing);
            return ResponseUtils.createSuccessResponse(convertToResponse(updated), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<UserDepartmentResponse>() {}, "User Department not found", 404);
        }
    }

    public ApiResponse<UserDepartmentResponse> findById(Long id) {
        Optional<UserDepartment> existingOpt = userDepartmentRepository.findById(id);
        if (existingOpt.isPresent()) {
            return ResponseUtils.createSuccessResponse(convertToResponse(existingOpt.get()), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<UserDepartmentResponse>() {}, "User Department not found", 404);
        }
    }
}
