package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDepartmentType;
import com.hims.entity.User;
import com.hims.entity.repository.MasDepartmentTypeRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasDepartmentTypeRequest;
import com.hims.response.MasDepartmentTypeResponse;
import com.hims.response.ApiResponse;
import com.hims.service.MasDepartmentTypeService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasDepartmentTypeServiceImpl implements MasDepartmentTypeService {

    private static final Logger log = LoggerFactory.getLogger(MasDepartmentTypeServiceImpl.class);

    @Autowired
    private MasDepartmentTypeRepository masDepartmentTypeRepository;

    @Autowired
    private UserRepo userRepo;

    private boolean isValidStatus(String status) {
        return "Y".equalsIgnoreCase(status) || "N".equalsIgnoreCase(status);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }

    @Override
    public ApiResponse<MasDepartmentTypeResponse> addDepartmentType(MasDepartmentTypeRequest request) {
        try{
            MasDepartmentType departmentType = new MasDepartmentType();
            departmentType.setDepartmentTypeCode(request.getDepartmentTypeCode());
            departmentType.setDepartmentTypeName(request.getDepartmentTypeName());
            departmentType.setStatus("y");
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            departmentType.setLastChgBy(String.valueOf(currentUser.getUserId()));
            departmentType.setLastChgDate(LocalDateTime.now());

            MasDepartmentType savedDepartmentType = masDepartmentTypeRepository.save(departmentType);
            return ResponseUtils.createSuccessResponse(mapToResponse(savedDepartmentType), new TypeReference<>() {
            });
        }
        catch (Exception e){
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<String> changeDepartmentTypeStatus(Long id, String status) {
        try{
            if (!isValidStatus(status)) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid status. Status should be 'Y' or 'N'", 400);
            }

            Optional<MasDepartmentType> departmentTypeOpt = masDepartmentTypeRepository.findById(id);
            if (departmentTypeOpt.isPresent()) {
                MasDepartmentType departmentType = departmentTypeOpt.get();
                departmentType.setStatus(status);
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                departmentType.setLastChgBy(String.valueOf(currentUser.getUserId()));
                departmentType.setLastChgDate(LocalDateTime.now());
                masDepartmentTypeRepository.save(departmentType);
                return ResponseUtils.createSuccessResponse("Department Type status updated to '" + status + "'", new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createNotFoundResponse("Department Type not found", 404);
            }
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasDepartmentTypeResponse> editDepartmentType(Long id, MasDepartmentTypeRequest request) {
        try{
            Optional<MasDepartmentType> departmentTypeOpt = masDepartmentTypeRepository.findById(id);
            if (departmentTypeOpt.isPresent()) {
                MasDepartmentType departmentType = departmentTypeOpt.get();
                departmentType.setDepartmentTypeCode(request.getDepartmentTypeCode());
                departmentType.setDepartmentTypeName(request.getDepartmentTypeName());
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                departmentType.setLastChgBy(String.valueOf(currentUser.getUserId()));
                departmentType.setLastChgDate(LocalDateTime.now());
                masDepartmentTypeRepository.save(departmentType);
                return ResponseUtils.createSuccessResponse(mapToResponse(departmentType), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createNotFoundResponse("Department Type not found", 404);
            }
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasDepartmentTypeResponse> getDepartmentTypeById(Long id) {
        Optional<MasDepartmentType> departmentTypeOpt = masDepartmentTypeRepository.findById(id);
        return departmentTypeOpt.map(departmentType -> ResponseUtils.createSuccessResponse(mapToResponse(departmentType), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Department Type not found", 404));
    }

    @Override
    public ApiResponse<List<MasDepartmentTypeResponse>> getAllDepartmentTypes(int flag) {
        List<MasDepartmentType> departmentTypes;

        if (flag == 1) {
            departmentTypes = masDepartmentTypeRepository.findByStatusIgnoreCaseOrderByDepartmentTypeNameAsc("Y");
        } else if (flag == 0) {
            departmentTypes = masDepartmentTypeRepository.findByStatusIgnoreCaseInOrderByLastChgDateDesc(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasDepartmentTypeResponse> responses = departmentTypes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }


    private MasDepartmentTypeResponse mapToResponse(MasDepartmentType departmentType) {
        MasDepartmentTypeResponse response = new MasDepartmentTypeResponse();
        response.setId(departmentType.getId());
        response.setDepartmentTypeCode(departmentType.getDepartmentTypeCode());
        response.setDepartmentTypeName(departmentType.getDepartmentTypeName());
        response.setStatus(departmentType.getStatus());
        response.setLastChgBy(departmentType.getLastChgBy());
        response.setLastChgDate(departmentType.getLastChgDate());
        return response;
    }
}