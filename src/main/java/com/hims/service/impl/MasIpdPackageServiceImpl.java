package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.projection.IpdPackageDetailsProjection;
import com.hims.request.IpdPackageRequest;
import com.hims.request.MasIpdPackageInclusionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.IpdPackageDetailsResponse;
import com.hims.response.IpdPackageResponse;
import com.hims.response.MasIpdPackageInclusionResponse;
import com.hims.service.MasIpdPackageService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MasIpdPackageServiceImpl implements MasIpdPackageService {
    @Autowired
    private MasAdmissionCategoryRepository masAdmissionCategoryRepository;
    @Autowired
    private MasDepartmentRepository masDepartmentRepository;
    @Autowired
    private MasIpdServiceCategoryRepository masIpdServiceCategoryRepository;
    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private MasIpdPackageRepository masIpdPackageRepository;
    @Autowired
    private MasIpdPackageInclusionRepository masIpdPackageInclusionRepository;


    @Override
    @Transactional
    public ApiResponse<String> savePackage(IpdPackageRequest request) {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }

        MasIpdPackage pkg = new MasIpdPackage();
        pkg.setPackageName(request.getPackageName());
        pkg.setStayDays(request.getStayDays());
        pkg.setStatus(AppConstants.STATUS_Y.toLowerCase());
        pkg.setPackageTypeId(masAdmissionCategoryRepository.findById(request.getPackageTypeId()).orElseThrow());
        pkg.setDeptId(masDepartmentRepository.findById(request.getDepartmentId()).orElseThrow());
        pkg.setLastChgBy(currentUser.getFullName());
        pkg.setLastChgDate(LocalDateTime.now());
        pkg.setGeneratedExclusions(request.getGeneratedExclusions());
        pkg.setGeneratedInclusions(request.getGeneratedInclusions());
        MasIpdPackage masIpdPackage = masIpdPackageRepository.save(pkg);


        List<MasIpdPackageInclusion> inclusions = request.getMasIpdPackageInclusionRequestList()
                .stream()
                .map(i -> {
                    MasIpdPackageInclusion inc = new MasIpdPackageInclusion();
                    inc.setMasIpdPackage(masIpdPackage);
                    inc.setServiceCategoryId(masIpdServiceCategoryRepository.findById(i.getServiceCategoryId()).orElseThrow());
                    inc.setCreatedBy(currentUser.getFullName());
                    String includeFlag = (i.getIncludedFlag() != null &&
                                    i.getIncludedFlag().equalsIgnoreCase(AppConstants.STATUS_Y))
                                    ? AppConstants.STATUS_Y.toLowerCase()
                                    : AppConstants.STATUS_N.toLowerCase();

                    inc.setIncludedFlag(includeFlag);
                    if (AppConstants.STATUS_Y.toLowerCase().equalsIgnoreCase(includeFlag)) {
                        inc.setLimitAmount(i.getLimitAmount());
                        inc.setLimitQty(i.getDays());
                    } else {
                        inc.setLimitAmount(null);
                        inc.setLimitQty(null);
                    }
                    inc.setStatus(AppConstants.STATUS_Y.toLowerCase());
                    inc.setCreatedDate(LocalDateTime.now());
                    inc.setLastUpdatedBy(currentUser.getFullName());
                    inc.setLastUpdatedDate(LocalDateTime.now());
                    return inc;
                }).toList();

        masIpdPackageInclusionRepository.saveAll(inclusions);

        return ResponseUtils.createSuccessResponse("package create successfully", new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<IpdPackageResponse>> getAllIpdPackages(int flag) {
        log.info("Fetching IPD Package list, flag={}", flag);
        try {
            List<MasIpdPackage> list = (flag == 1)
                            ? masIpdPackageRepository.findByStatusIgnoreCaseOrderByPackageNameAsc(AppConstants.STATUS_Y.toLowerCase())
                            : masIpdPackageRepository.findAllByOrderByStatusDescLastChgDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching IPD Package list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<IpdPackageResponse> changeStatus(Long id, String status) {
        try {
            MasIpdPackage entity = masIpdPackageRepository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse("IPD Package not found", HttpStatus.NOT_FOUND.value()
                );}
            User user = authUtil.getCurrentUser();
            entity.setStatus(status.toLowerCase());
            entity.setLastChgBy(user.getFullName());
            entity.setLastChgDate(LocalDateTime.now());
            masIpdPackageRepository.save(entity);
            return ResponseUtils.createSuccessResponse(toResponse(entity), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error changing status for IPD Package id: {}", id, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<IpdPackageDetailsResponse> getById(Long id) {
        try {
            List<IpdPackageDetailsProjection> rows = masIpdPackageRepository.getPackageDetails(id,AppConstants.STATUS_Y.toLowerCase());
            if (rows == null || rows.isEmpty()) {
                return ResponseUtils.createNotFoundResponse("IPD Package not found", HttpStatus.NOT_FOUND.value());
            }
            // Header (take from first row)
            IpdPackageDetailsProjection first = rows.get(0);
            IpdPackageDetailsResponse response = new IpdPackageDetailsResponse();
            response.setPackageName(first.getPackageName());
            response.setType(first.getType());
            response.setDepartmentName(first.getDepartmentName());
            response.setStay(first.getStayDays());
            response.setInclusions(first.getGeneratedInclusions());
            response.setExclusions(first.getGeneratedExclusions());
            response.setLastUpdate(first.getLastChgDate());
            response.setStatus(first.getStatus());

            //  Child List
            List<MasIpdPackageInclusionResponse> inclusionList =
                    rows.stream()
                            .filter(r -> r.getInclusionId() != null)
                            .map(r -> {
                                MasIpdPackageInclusionResponse dto = new MasIpdPackageInclusionResponse();
                                dto.setInclusionId(r.getInclusionId());
                                dto.setPackageId(r.getPackageId());
                                dto.setServiceCategoryId(r.getServiceCategoryId());
                                dto.setIncludedFlag(r.getInclusionFlag());

                                dto.setServiceCategoryName(r.getServiceCategoryName());
                                dto.setLimitAmount(r.getLimitAmount());
                                dto.setLimitQty(r.getLimitQty());

                                return dto;
                            })
                            .toList();

            response.setMasIpdPackageInclusionResponses(inclusionList);

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error fetching IPD Package (projection) id: {}", id, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
    @Override
    @Transactional
    public ApiResponse<String> updatePackage(Long id, IpdPackageRequest request) {

        try {
            User currentUser = authUtil.getCurrentUser();
            MasIpdPackage pkg = masIpdPackageRepository.findById(id).orElse(null);
            if (pkg == null) {
                return ResponseUtils.createNotFoundResponse("Package not found", HttpStatus.NOT_FOUND.value());
            }
            pkg.setPackageName(request.getPackageName());
            pkg.setStayDays(request.getStayDays());
            pkg.setPackageTypeId(masAdmissionCategoryRepository.findById(request.getPackageTypeId()).orElseThrow());
            pkg.setDeptId(masDepartmentRepository.findById(request.getDepartmentId()).orElseThrow());
            pkg.setGeneratedExclusions(request.getGeneratedExclusions());
            pkg.setGeneratedInclusions(request.getGeneratedInclusions());
            pkg.setLastChgBy(currentUser.getFullName());
            pkg.setLastChgDate(LocalDateTime.now());
            masIpdPackageRepository.save(pkg);


            List<MasIpdPackageInclusion> existingList = masIpdPackageInclusionRepository.findByMasIpdPackage_PackageId(id);

            // Convert to Map (categoryId → entity)
            Map<Long, MasIpdPackageInclusion> existingMap = existingList.stream().collect(Collectors.toMap(
                                    e -> e.getServiceCategoryId().getCategoryId(),
                                    e -> e
                            ));


            // UPDATE ONLY
            for (MasIpdPackageInclusionRequest i : request.getMasIpdPackageInclusionRequestList()) {
                MasIpdPackageInclusion inc = existingMap.get(i.getServiceCategoryId());
                if (inc != null) {
                    String includeFlag =
                            (i.getIncludedFlag() != null &&
                                    i.getIncludedFlag().equalsIgnoreCase(AppConstants.STATUS_Y))
                                    ? AppConstants.STATUS_Y.toLowerCase()
                                    : AppConstants.STATUS_N.toLowerCase();

                    inc.setIncludedFlag(includeFlag);


                    if (AppConstants.STATUS_Y.toLowerCase().equalsIgnoreCase(includeFlag)) {
                        inc.setLimitAmount(i.getLimitAmount());
                        inc.setLimitQty(i.getDays());
                    } else {
                        inc.setLimitAmount(null);
                        inc.setLimitQty(null);
                    }
                    inc.setLastUpdatedBy(currentUser.getFullName());
                    inc.setLastUpdatedDate(LocalDateTime.now());
                }
            }
            masIpdPackageInclusionRepository.saveAll(existingList);



            return ResponseUtils.createSuccessResponse("Package updated successfully", new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error updating package id: {}", id, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    private IpdPackageResponse toResponse(MasIpdPackage entity) {
        IpdPackageResponse response = new IpdPackageResponse();
        response.setPackageId(entity.getPackageId());
        response.setPackageName(entity.getPackageName());
        response.setType(entity.getPackageTypeId() != null ? entity.getPackageTypeId().getAdmissionCategoryName() : null);
        response.setDepartmentName(entity.getDeptId() != null ? entity.getDeptId().getDepartmentName() : null);
        response.setStay(entity.getStayDays());
        response.setInclusions(entity.getGeneratedInclusions());
        response.setExclusions(entity.getGeneratedExclusions());
        response.setLastUpdate(entity.getLastChgDate());
        response.setStatus(entity.getStatus());

        return response;
    }

}
