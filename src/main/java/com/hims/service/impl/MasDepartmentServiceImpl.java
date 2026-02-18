package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasWardCategory;
import com.hims.entity.repository.*;
import com.hims.request.MasDepartmentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDepartmentResponse;
import com.hims.response.MasDeptResponse;
import com.hims.response.MasUserDepartmentResponse;
import com.hims.service.MasDepartmentService;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static com.hims.constants.AppConstants.*;

@Service
@Slf4j
public class MasDepartmentServiceImpl implements MasDepartmentService {

    @Autowired
    private MasDepartmentRepository masDepartmentRepository;

    @Autowired
    MasUserDepartmentRepository masUserDepartmentRepository;

    @Autowired
    private UserDepartmentRepository userDepartmentRepository;

    @Autowired
    private MasDepartmentTypeRepository masDepartmentTypeRepository;

    @Autowired
    private MasHospitalRepository masHospitalRepository;
    @Autowired
    private MasWardCategoryRepository masWardCategoryRepository;

    @Value("${dept.type.ward}")
    private  Long WARD_ID;

    private boolean isValidStatus(String status) {
        return STATUS_ACTIVE_UPPER.equalsIgnoreCase(status) || STATUS_INACTIVE_UPPER.equalsIgnoreCase(status);
    }

    private String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    public ApiResponse<MasDepartmentResponse> addDepartment(MasDepartmentRequest request) {
        if (!isValidStatus(request.getStatus())) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status. Status should be 'Y' or 'N'", 400);
        }

        MasDepartment department = new MasDepartment();
        department.setDepartmentCode(request.getDepartmentCode());
        department.setDepartmentName(request.getDepartmentName());
        department.setStatus(request.getStatus());
        department.setLastChgBy(request.getLastChgBy());
        department.setLastChgTime(getCurrentTimeFormatted());
        department.setLastChgDate(Instant.now());
        department.setIndentApplicable(request.getIndentApplicable());
        if (request.getWardCategoryId() != null) {
            department.setWardCategory( masWardCategoryRepository.findById(request.getWardCategoryId()).orElse(null));
        }

        if (request.getDepartmentTypeId() != null) {
            department.setDepartmentType(masDepartmentTypeRepository.findById(request.getDepartmentTypeId()).orElse(null));
        }
        if (request.getHospitalId() != null) {
            department.setHospital(masHospitalRepository.findById(request.getHospitalId()).orElse(null));
        }
        department.setDepartmentNo(request.getDepartmentNo());

        MasDepartment savedDepartment = masDepartmentRepository.save(department);
        return ResponseUtils.createSuccessResponse(mapToResponse(savedDepartment), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<String> changeDepartmentStatus(Long id, String status) {
        if (!isValidStatus(status)) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid status. Status should be 'Y' or 'N'", 400);
        }

        Optional<MasDepartment> departmentOpt = masDepartmentRepository.findById(id);
        if (departmentOpt.isPresent()) {
            MasDepartment department = departmentOpt.get();
            department.setStatus(status);
            department.setLastChgDate(Instant.now());
            masDepartmentRepository.save(department);
            return ResponseUtils.createSuccessResponse("Department status updated to '" + status + "'", new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Department not found", 404);
        }
    }

    @Override
    public ApiResponse<MasDepartmentResponse> editDepartment(Long id, MasDepartmentRequest request) {
        Optional<MasDepartment> departmentOpt = masDepartmentRepository.findById(id);
        if (departmentOpt.isPresent()) {
            MasDepartment department = departmentOpt.get();
            department.setDepartmentCode(request.getDepartmentCode());
            department.setDepartmentName(request.getDepartmentName());
            department.setStatus(request.getStatus());
            department.setLastChgBy(request.getLastChgBy());
            department.setLastChgTime(getCurrentTimeFormatted());
            department.setLastChgDate(Instant.now());
            department.setIndentApplicable(request.getIndentApplicable());


            if (request.getDepartmentTypeId() != null) {

                // If selected type = WARD
                if (request.getDepartmentTypeId().equals(WARD_ID)) {

                        department.setWardCategory(
                                masWardCategoryRepository.findById(request.getWardCategoryId()).orElse(null)
                        );

                } else {
                    // If NOT WARD → Always reset to null
                    department.setWardCategory(null);
                }

                // Set department type always
                department.setDepartmentType(
                        masDepartmentTypeRepository.findById(request.getDepartmentTypeId()).orElse(null)
                );
            }


            if (request.getHospitalId() != null) {
                department.setHospital(masHospitalRepository.findById(request.getHospitalId()).orElse(null));
            } else {
                department.setHospital(null);
            }

            department.setDepartmentNo(request.getDepartmentNo());

            masDepartmentRepository.save(department);
            return ResponseUtils.createSuccessResponse(mapToResponse(department), new TypeReference<>() {});
        } else {
            return ResponseUtils.createNotFoundResponse("Department not found", 404);
        }
    }


    @Override
    public ApiResponse<MasDepartmentResponse> getDepartmentById(Long id) {
        return masDepartmentRepository.findById(id)
                .map(dept -> ResponseUtils.createSuccessResponse(mapToResponse(dept), new TypeReference<>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("Department not found", 404));
    }

    @Override
    public ApiResponse<List<MasDepartmentResponse>> getAllDepartments(int flag) {
        List<MasDepartment> departments;

        if (flag == FLAG_ACTIVE_ONLY) {
            departments = masDepartmentRepository.findByStatusIgnoreCaseOrderByDepartmentNameAsc(STATUS_ACTIVE_UPPER);
        } else if (flag == FLAG_ALL) {
            departments = masDepartmentRepository.findAllByOrderByStatusDescLastChgDateDescLastChgTimeDesc();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, MSG_INVALID_FLAG, 400);
        }

        List<MasDepartmentResponse> responses = departments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }


    private MasDepartmentResponse mapToResponse(MasDepartment department) {
        MasDepartmentResponse response = new MasDepartmentResponse();
        response.setId(department.getId());
        response.setDepartmentCode(department.getDepartmentCode());
        response.setDepartmentName(department.getDepartmentName());
        response.setStatus(department.getStatus());
        response.setLastChgBy(department.getLastChgBy());
        response.setLastChgDate(department.getLastChgDate());
        response.setLastChgTime(department.getLastChgTime());
        response.setIndentApplicable(department.getIndentApplicable());
        if(department.getWardCategory()!=null){
            response.setWardCategoryId(department.getWardCategory().getId());
            response.setWardCategoryName(department.getWardCategory().getCategoryName());
        }
        if (department.getDepartmentType() != null) {
            response.setDepartmentTypeId(department.getDepartmentType().getId());
            response.setDepartmentTypeName(department.getDepartmentType().getDepartmentTypeName());
        }
        if (department.getHospital() != null) {
            response.setHospitalId(department.getHospital().getId());
            response.setHospitalName(department.getHospital().getHospitalName());
        }
        response.setDepartmentNo(department.getDepartmentNo());
        return response;
    }


    @Override
    public ApiResponse<List<MasUserDepartmentResponse>> getAllMasUserDepartments() {
        List<MasUserDepartmentResponse> departmentResponses = masUserDepartmentRepository.fetchAllUserDepartments();
        return ResponseUtils.createSuccessResponse(departmentResponses, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<MasUserDepartmentResponse>> getMasUserDepartmentsByDepartmentId(Long departmentId) {
        List<MasUserDepartmentResponse> departmentResponses = masUserDepartmentRepository.fetchByDepartmentId(departmentId);
        return ResponseUtils.createSuccessResponse(departmentResponses, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<MasUserDepartmentResponse>> getMasUserDepartmentsByUserId(Long userId) {
        List<MasUserDepartmentResponse> departmentResponses = masUserDepartmentRepository.fetchByUserId(userId);
        return ResponseUtils.createSuccessResponse(departmentResponses, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<MasDepartmentResponse>> getAllWardDepartmentByWardCategory(Long wardCategory) {

        Long departmentTypeId = WARD_ID;

        List<MasDepartment> departments =
                masDepartmentRepository.findActiveWardDepartments(
                        departmentTypeId,
                        wardCategory
                );

        List<MasDepartmentResponse> responses = departments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<MasDeptResponse>> getAllIndentApplicableDepartments(String indentApplicable) {

        try {
            log.info("getAllIndentApplicableDepartments method started ...");
            List<MasDepartment> departments = masDepartmentRepository.findByIndentApplicableIgnoreCase(indentApplicable);
            log.info("getAllIndentApplicableDepartments method ended ...");
            return  ResponseUtils.createSuccessResponse(departments.stream().map(this::mapToResponseForDropDown).toList(), new TypeReference<>() {});
        }catch (Exception e){
            log.error("getAllIndentApplicableDepartments method error :: ",e);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    private MasDeptResponse mapToResponseForDropDown(MasDepartment department){
        MasDeptResponse response= new MasDeptResponse();
        response.setDeptId(department.getId());
        response.setDeptName(department.getDepartmentName());
        return  response;
    }

}
