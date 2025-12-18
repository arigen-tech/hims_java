package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.UserDepartment;
import com.hims.entity.repository.UserDepartmentRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.User;
import com.hims.entity.MasDepartment;
import com.hims.request.UserDepartmentRequest;
import com.hims.request.UserDepartmentRequestOne;
import com.hims.response.ApiResponse;
import com.hims.response.UserDepartmentResponse;
import com.hims.service.UserDepartmentService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Override
    public ApiResponse<List<UserDepartmentResponse>> getAllUserDepartments() {
        List<UserDepartment> userDepartments = userDepartmentRepository.findAllByOrderByUserAsc();
        List<UserDepartmentResponse> responses = userDepartments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }


    private UserDepartmentResponse convertToResponse(UserDepartment userDepartment) {
        UserDepartmentResponse response = new UserDepartmentResponse();
        response.setId(userDepartment.getId());
        response.setLastChgBy(userDepartment.getLastChgBy());
        response.setLasUpdatedDt(userDepartment.getLasUpdatedDt());

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

    @Override
    public ApiResponse<UserDepartmentResponse> addUserDepartment(UserDepartmentRequest request) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }

        UserDepartment userDepartment = new UserDepartment();
        userDepartment.setLastChgBy(currentUser.getUsername());
        userDepartment.setLasUpdatedDt(OffsetDateTime.now());

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

    @Override
    public ApiResponse<UserDepartmentResponse> updateUserDepartment(Long id, UserDepartmentResponse details) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }
        Optional<UserDepartment> existingOpt = userDepartmentRepository.findById(id);
        if (existingOpt.isPresent()) {
            UserDepartment existing = existingOpt.get();
            existing.setLastChgBy(currentUser.getUsername());
            existing.setLasUpdatedDt(OffsetDateTime.now());

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

    @Override
    public ApiResponse<UserDepartmentResponse> findById(Long id) {
        Optional<UserDepartment> existingOpt = userDepartmentRepository.findById(id);
        if (existingOpt.isPresent()) {
            return ResponseUtils.createSuccessResponse(convertToResponse(existingOpt.get()), new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<UserDepartmentResponse>() {}, "User Department not found", 404);
        }
    }

    @Override
    public ApiResponse<String> addOrUpdateUserDept(UserDepartmentRequestOne request) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Current user not found",
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        long userId = request.getUserId();
        List<Long> incomingDeptIds = request.getDepartments()
                .stream()
                .map(UserDepartmentRequestOne.Department::getDepartmentId)
                .collect(Collectors.toList());

        User user;
        try {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "User not found",
                    HttpStatus.NOT_FOUND.value()
            );
        }

        List<UserDepartment> existingUserDepartments = userDepartmentRepository.findByUser_UserId(userId);

        boolean success = false;

        for (UserDepartmentRequestOne.Department deptReq : request.getDepartments()) {
            long deptId = deptReq.getDepartmentId();
            String status = deptReq.getStatus();

            UserDepartment existing = existingUserDepartments.stream()
                    .filter(ud -> ud.getDepartment().getId() == deptId)
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setStatus(status);
                existing.setLasUpdatedDt(OffsetDateTime.now());
                existing.setLastChgBy(currentUser.getUsername());
                userDepartmentRepository.save(existing);
                success = true;
            } else {
                MasDepartment dept;
                try {
                    dept = masDepartmentRepository.findById(deptId)
                            .orElseThrow(() -> new RuntimeException("Department not found"));
                } catch (Exception e) {
                    return ResponseUtils.createFailureResponse(
                            null, new TypeReference<>() {},
                            "Department ID " + deptId + " not found",
                            HttpStatus.NOT_FOUND.value()
                    );
                }

                UserDepartment newUserDept = new UserDepartment();
                newUserDept.setUser(user);
                newUserDept.setDepartment(dept);
                newUserDept.setStatus(status);
                newUserDept.setLasUpdatedDt(OffsetDateTime.now());
                newUserDept.setLastChgBy(currentUser.getUsername());
                userDepartmentRepository.save(newUserDept);
                success = true;
            }
        }

        for (UserDepartment ud : existingUserDepartments) {
            if (!incomingDeptIds.contains(ud.getDepartment().getId())) {
                ud.setStatus("n");
                ud.setLasUpdatedDt(OffsetDateTime.now());
                ud.setLastChgBy(currentUser.getUsername());
                userDepartmentRepository.save(ud);
                success = true;
            }
        }

        if (success) {
            return ResponseUtils.createSuccessResponse(
                    "Departments updated successfully", new TypeReference<>() {});
        } else {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "No department updates were made",
                    HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    @Override
    public ApiResponse<List<UserDepartmentResponse>> getAllUserDepartmentsByUserId(Long userId) {
        List<UserDepartment> userDepartments = userDepartmentRepository.findByUser_UserIdAndStatus(userId, "y");

        List<UserDepartmentResponse> responses = userDepartments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<UserDepartmentResponse>> getAllUserDepartmentsByUserUserName(String userName) {
        List<UserDepartment> userDepartments = userDepartmentRepository.findByUser_UserNameAndUser_StatusAndStatusOrderByDepartment_DepartmentNameAsc(userName, "y","y");

        List<UserDepartmentResponse> responses = userDepartments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }




    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(username);
        if (user == null) {

        }
        return user;
    }
}
