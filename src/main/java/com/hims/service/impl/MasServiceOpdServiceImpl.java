package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.MasServiceOpdRequest;
import com.hims.response.ApiResponse;
import com.hims.service.MasServiceOpdService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ApiResponse<List<MasServiceOpd>> findByHospitalId(Long id) {
        try {
            boolean exists = masHospitalRepository.existsById(id);
            if (!exists) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Hospital not found with ID: " + id, 404);
            }

            List<MasServiceOpd> response = masServiceOpdRepository.findByHospitalIdId(id);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error fetching data by hospital ID", 500);
        }
    }


    @Override
    public ApiResponse<MasServiceOpd> save(MasServiceOpdRequest req) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", 401);
            }

            log.warn("id 1: {}", req.getServiceCategory());
            log.warn("id 1: {}", req.getDepartmentId());
            log.warn("id 1: {}", req.getDoctorId());

            MasServiceOpd opd = new MasServiceOpd();
            opd.setServiceCode(req.getServiceCode());
            opd.setServiceName(req.getServiceName());
            opd.setBaseTariff(req.getBaseTariff());

            opd.setServiceCategory(masServiceCategoryRepository.findById(req.getServiceCategory()).orElse(null));
            opd.setHospitalId(masHospitalRepository.findById(req.getHospitalId()).orElse(null));
            opd.setDepartmentId(masDepartmentRepository.findById(req.getDepartmentId()).orElse(null));
            opd.setDoctorId(userRepo.findById(req.getDoctorId()).orElse(null));

            opd.setFromDt(req.getFromDate());
            opd.setToDt(req.getToDate());
            opd.setStatus("y");

            opd.setLastChgBy(currentUser.getUsername());
            opd.setLastChgDt(Instant.now());

            MasServiceOpd saved = masServiceOpdRepository.save(opd);

            return ResponseUtils.createSuccessResponse(saved, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Error saving data", 500);
        }
    }


    @Override
    public ApiResponse<MasServiceOpd> edit(Long id, MasServiceOpdRequest req) {
        try {
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

            existing.setServiceCode(req.getServiceCode());
            existing.setServiceName(req.getServiceName());
            existing.setBaseTariff(req.getBaseTariff());
            existing.setServiceCategory(category);
            existing.setDepartmentId(department);
            existing.setDoctorId(doctor);
            existing.setHospitalId(hospital);
            existing.setFromDt(req.getFromDate());
            existing.setToDt(req.getToDate());
            existing.setStatus("y");
            existing.setLastChgBy(currentUser.getUsername());
            existing.setLastChgDt(java.time.Instant.now());

            MasServiceOpd updated = masServiceOpdRepository.save(existing);
            return ResponseUtils.createSuccessResponse(updated, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error Updating Data", 500);
        }
    }


    @Override
    public ApiResponse<MasServiceOpd> updateStatus(Long id, String status) {
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

            entity.setStatus(status);
            entity.setLastChgBy(currentUser.getUsername());
            entity.setLastChgDt(Instant.now());

            MasServiceOpd updated = masServiceOpdRepository.save(entity);
            return ResponseUtils.createSuccessResponse(updated, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error updating status", 500);
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
