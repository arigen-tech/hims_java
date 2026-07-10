package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.IpdConsultationTariff;
import com.hims.entity.User;
import com.hims.entity.repository.*;
import com.hims.projection.IpdConsultationTariffProjection;
import com.hims.request.IpdConsultationTariffRequest;
import com.hims.response.ApiResponse;
import com.hims.response.IpdConsultationTariffResponse;
import com.hims.service.IpdConsultationTariffService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IpdConsultationTariffServiceImpl implements IpdConsultationTariffService {

    private final IpdConsultationTariffRepository repository;

    private final MasServiceCategoryRepository serviceCategoryRepo;
    private final MasVisitTypeRepository visitTypeRepo;
    private final MasHospitalRepository hospitalRepo;
    private final MasDepartmentRepository departmentRepo;
    private final UserRepo  userRepo;

    private final AuthUtil authUtil;
    @Override
    public ApiResponse<Page<IpdConsultationTariffResponse>> getAllIpdConsultationTariff(Long departmentId, Long doctorId, int page, int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<IpdConsultationTariffProjection> projectionPage = repository.getAllIpdConsultationTariff(departmentId, doctorId, pageable);
            Page<IpdConsultationTariffResponse> responsePage = projectionPage.map(this::toResponse);
            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {});

        } catch (Exception e) {log.error("Error fetching tariff list", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<IpdConsultationTariffResponse> getByIdIpdConsultationTariff(Long id) {
        return repository.findById(id)
                .map(e -> ResponseUtils.createSuccessResponse(toResponse(e), new TypeReference<>() {}))
                .orElse(ResponseUtils.createNotFoundResponse(" IpdConsultationTariff Not found", HttpStatus.NOT_FOUND.value()));
    }

    @Override
    public ApiResponse<IpdConsultationTariffResponse> createIpdConsultationTariff(IpdConsultationTariffRequest req) {
        try {
            User user = authUtil.getCurrentUser();

            IpdConsultationTariff entity = new IpdConsultationTariff();

            entity.setServiceCategory(serviceCategoryRepo.findById(req.getServiceCategoryId()).orElse(null));
            entity.setVisitType(visitTypeRepo.findById(req.getVisitTypeId()).orElse(null));
            entity.setHospital(hospitalRepo.findById(req.getHospitalId()).orElse(null));
            entity.setDepartment(departmentRepo.findById(req.getDepartmentId()).orElse(null));
            entity.setDoctor(userRepo.findById(req.getDoctorId()).orElse(null));
            entity.setBaseTariff(req.getBaseTariff());
            entity.setFromDate(req.getFromDate());
            entity.setToDate(req.getToDate());
            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setLastChangedBy(user.getFullName());
            entity.setLastChangedDate(LocalDateTime.now());
            repository.save(entity);

            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating tariff", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<IpdConsultationTariffResponse> updateIpdConsultationTariff(Long id, IpdConsultationTariffRequest req) {
        log.info("Updating IpdConsultationTariff id={}", id);
        try {
            User user = authUtil.getCurrentUser();
            IpdConsultationTariff entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("IpdConsultationTariff Not found", HttpStatus.NOT_FOUND.value());
            }

            entity.setServiceCategory(serviceCategoryRepo.findById(req.getServiceCategoryId()).orElse(null));
            entity.setVisitType(visitTypeRepo.findById(req.getVisitTypeId()).orElse(null));
            entity.setHospital(hospitalRepo.findById(req.getHospitalId()).orElse(null));
            entity.setDepartment(departmentRepo.findById(req.getDepartmentId()).orElse(null));
            entity.setDoctor(userRepo.findById(req.getDoctorId()).orElse(null));

            entity.setBaseTariff(req.getBaseTariff());
            entity.setFromDate(req.getFromDate());
            entity.setToDate(req.getToDate());

            entity.setLastChangedBy(user.getFullName());
            entity.setLastChangedDate(LocalDateTime.now());
            repository.save(entity);
            return ResponseUtils.createSuccessResponse(toResponse(entity),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating IpdConsultationTariff id={}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<IpdConsultationTariffResponse> changeStatusIpdConsultationTariff(Long id, String status) {

        log.info("Changing status for IpdConsultationTariff id={}, status={}", id, status);

        try {
            User user = authUtil.getCurrentUser();

            IpdConsultationTariff entity = repository.findById(id).orElse(null);
            if (entity == null) {return ResponseUtils.createNotFoundResponse("IpdConsultationTariff Not found",
                        HttpStatus.NOT_FOUND.value());
            }

            entity.setStatus(status.toLowerCase());
            entity.setLastChangedBy(user.getFullName());
            entity.setLastChangedDate(LocalDateTime.now());

            repository.save(entity);
            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error changing status for IpdConsultationTariff id={}", id, e);

            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    private IpdConsultationTariffResponse toResponse(IpdConsultationTariff e) {
        IpdConsultationTariffResponse r = new IpdConsultationTariffResponse();
        r.setTariffId(e.getTariffId());
        r.setServiceCategoryId(e.getServiceCategory() != null ? e.getServiceCategory().getId() : null);
        r.setServiceCategoryName(e.getServiceCategory() != null ? e.getServiceCategory().getServiceCatName() : null);
        r.setVisitTypeId(e.getVisitType() != null ? e.getVisitType().getVisitTypeId() : null);
        r.setVisitTypeName(e.getVisitType() != null ? e.getVisitType().getVisitTypeName() : null);
        r.setDepartmentId(e.getDepartment() != null ? e.getDepartment().getId() : null);
        r.setDepartmentName(e.getDepartment() != null ? e.getDepartment().getDepartmentName() : null);
        r.setDoctorId(e.getDoctor() != null ? e.getDoctor().getUserId() : null);
        r.setDoctorName(e.getDoctor() != null ? e.getDoctor().getFullName() : null);
        r.setBaseTariff(e.getBaseTariff());
        r.setFromDate(e.getFromDate());
        r.setToDate(e.getToDate());
        r.setStatus(e.getStatus());

        return r;
    }
    private IpdConsultationTariffResponse toResponse(IpdConsultationTariffProjection p) {

        IpdConsultationTariffResponse res = new IpdConsultationTariffResponse();
        res.setTariffId(p.getTariffId());
        res.setServiceCategoryId(p.getServiceCategoryId());
        res.setServiceCategoryName(p.getServiceCategoryName());
        res.setVisitTypeId(p.getVisitTypeId());
        res.setVisitTypeName(p.getVisitTypeName());
        res.setDepartmentId(p.getDepartmentId());
        res.setDepartmentName(p.getDepartmentName());
        res.setDoctorId(p.getDoctorId());
        res.setDoctorName(p.getDoctorName());
        res.setBaseTariff(p.getBaseTariff());
        res.setFromDate(p.getFromDate());
        res.setToDate(p.getToDate());
        res.setStatus(p.getStatus());

        return res;
    }
}