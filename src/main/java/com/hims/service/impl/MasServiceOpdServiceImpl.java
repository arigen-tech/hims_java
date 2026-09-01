package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.projection.MasServiceOpdProjection;
import com.hims.request.MasServiceOpdRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasServiceOpdResponse;
import com.hims.service.MasServiceOpdService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class MasServiceOpdServiceImpl implements MasServiceOpdService {
    private static final Logger log = LoggerFactory.getLogger(MasServiceOpdServiceImpl.class);

    @Autowired
    MasServiceOpdRepository masServiceOpdRepository;
    @Autowired
    private MasHospitalRepository masHospitalRepository;

    @Autowired
    private MasDepartmentRepository masDepartmentRepository;

    @Autowired
    UserRepo userRepo;

    @Autowired
    private  MasServiceCategoryRepository masServiceCategoryRepository;

    @Override
    public ApiResponse<Page<MasServiceOpdResponse>> getOpdTariffByDepartmentAndDoctor(Long hospitalId, Long departmentId, Long doctorId,String doctorName, Pageable pageable) {

        try {
            boolean exists = masHospitalRepository.existsById(hospitalId);
            if (!exists) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Hospital not found with ID: " + hospitalId, 404);
            }

            Page<MasServiceOpdProjection> pageData = masServiceOpdRepository.getOpdTariffByDepartmentAndDoctor(hospitalId, departmentId, doctorId,doctorName, pageable);

            Page<MasServiceOpdResponse> response = pageData.map(p -> {
                MasServiceOpdResponse dto = new MasServiceOpdResponse();
                dto.setId(p.getId());
                dto.setServiceName(p.getServiceName());
                dto.setBaseTariff(p.getBaseTariff());
                dto.setServiceCategory(p.getServiceCategory());
                dto.setDepartmentName(p.getDepartmentName());
                dto.setDoctorFirstName(p.getDoctorFirstName());
                dto.setDoctorMiddleName(p.getDoctorMiddleName());
                dto.setDoctorLastName(p.getDoctorLastName());
                dto.setFromDate(p.getFromDate());
                dto.setToDate(p.getToDate());
                dto.setStatus(p.getStatus());
                return dto;
            });
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,  HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
    @Override
    public ApiResponse<String> save(MasServiceOpdRequest req) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Current user not found",
                        401
                );
            }

            boolean duplicate = masServiceOpdRepository
                    .existsOverlappingTariff(
                            req.getServiceCategory(),
                            req.getHospitalId(),
                            req.getDepartmentId(),
                            req.getDoctorId(),
                            req.getFromDate(),
                            req.getToDate(),
                            AppConstants.STATUS_Y.toLowerCase()
                    );

            if (duplicate) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        AppConstants.DUPLICATE_DATA_OPD_SERVICE_TARIFF,
                        HttpStatus.CONFLICT.value()
                );
            }

            MasServiceOpd opd = new MasServiceOpd();

            opd.setBaseTariff(req.getBaseTariff());
            opd.setServiceCategory(masServiceCategoryRepository.findById(req.getServiceCategory()).orElse(null));
            opd.setHospitalId(masHospitalRepository.findById(req.getHospitalId()).orElse(null));
            opd.setDepartmentId(masDepartmentRepository.findById(req.getDepartmentId()).orElse(null));
            opd.setDoctorId(userRepo.findById(req.getDoctorId()).orElse(null));
            opd.setFromDt(req.getFromDate());
            opd.setToDt(req.getToDate());
            opd.setStatus(AppConstants.STATUS_Y.toLowerCase());
            opd.setLastChgBy(currentUser.getUsername());
            opd.setLastChgDt(Instant.now());
            masServiceOpdRepository.save(opd);
            return ResponseUtils.createSuccessResponse("Doctor tariff created successfully",new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<String> edit(Long id, MasServiceOpdRequest req) {
        try {

            boolean duplicate = masServiceOpdRepository
                    .existsOverlappingTariff(
                            req.getServiceCategory(),
                            req.getHospitalId(),
                            req.getDepartmentId(),
                            req.getDoctorId(),
                            req.getFromDate(),
                            req.getToDate(),
                            AppConstants.STATUS_Y.toLowerCase()
                    );

            if (duplicate) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        AppConstants.DUPLICATE_DATA_OPD_SERVICE_TARIFF,
                        HttpStatus.CONFLICT.value()
                );
            }

            MasServiceOpd existing = masServiceOpdRepository.findById(id).orElse(null);
            if (existing == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Service not found with ID: " + id, 404);
            }

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", 401);
            }

            MasServiceCategory category = masServiceCategoryRepository.findById(req.getServiceCategory()).orElse(null);
            MasDepartment department = masDepartmentRepository.findById(req.getDepartmentId()).orElse(null);
            MasHospital hospital = masHospitalRepository.findById(req.getHospitalId()).orElse(null);
            User doctor = userRepo.findById(req.getDoctorId()).orElse(null);
            existing.setBaseTariff(req.getBaseTariff());
            existing.setServiceCategory(category);
            existing.setDepartmentId(department);
            existing.setDoctorId(doctor);
            existing.setHospitalId(hospital);
            existing.setFromDt(req.getFromDate());
            existing.setToDt(req.getToDate());
            existing.setStatus(AppConstants.STATUS_Y.toLowerCase());
            existing.setLastChgBy(currentUser.getUsername());
            existing.setLastChgDt(java.time.Instant.now());

            MasServiceOpd updated = masServiceOpdRepository.save(existing);
            return ResponseUtils.createSuccessResponse(" doctor tariff update successfully", new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,  HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @Override
    public ApiResponse<String> updateStatus(Long id, String status) {
        try {
            MasServiceOpd entity = masServiceOpdRepository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Service OPD not found with ID: " + id, 404);
            }

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", 401);
            }

            entity.setStatus(status.toLowerCase());
            entity.setLastChgBy(currentUser.getUsername());
            entity.setLastChgDt(Instant.now());

            MasServiceOpd updated = masServiceOpdRepository.save(entity);
            return ResponseUtils.createSuccessResponse("status update successfully", new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,  HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);

        }
        return user;
    }
}
