package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasVaccineMaster;
import com.hims.entity.User;
import com.hims.entity.repository.MasVaccineMasterRepository;
import com.hims.request.MasVaccineMasterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasVaccineMasterResponse;
import com.hims.service.MasVaccineMasterService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MasVaccineMasterServiceImpl
        implements MasVaccineMasterService {

    @Autowired
    private MasVaccineMasterRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasVaccineMasterResponse>> getAll(int flag) {
        log.info("Fetching Vaccine Master list, flag={}", flag);
        try {
            List<MasVaccineMaster> list =
                    (flag == 1)
                            ? repository.findByStatusIgnoreCaseOrderByVaccineGroupAscDisplayOrderAsc("y")
                            : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::toResponse).toList(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error fetching Vaccine Master list", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasVaccineMasterResponse> getById(Long id) {
        log.info("Fetching Vaccine Master by id={}", id);
        try {
            return repository.findById(id)
                    .map(e -> ResponseUtils.createSuccessResponse(
                            toResponse(e), new TypeReference<>() {}))
                    .orElse(ResponseUtils.createNotFoundResponse(
                            "Vaccine not found", 404));
        } catch (Exception e) {
            log.error("Error fetching Vaccine Master by id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Something went wrong", 500);
        }
    }

    @Override
    public ApiResponse<MasVaccineMasterResponse> create(
            MasVaccineMasterRequest request) {
        log.info("Creating Vaccine Master");
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            MasVaccineMaster entity = MasVaccineMaster.builder()
                    .vaccineLabel(request.getVaccineLabel())
                    .recommendedAge(request.getRecommendedAge())
                    .vaccineGroup(request.getVaccineGroup())
                    .displayOrder(request.getDisplayOrder())
                    .isMultiDose(
                            request.getIsMultiDose() == null ? "N" : request.getIsMultiDose())
                    .dosePerVial(request.getDosePerVial())
                    .status("Y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error creating Vaccine Master", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Creation failed", 500);
        }
    }

    @Override
    public ApiResponse<MasVaccineMasterResponse> update(
            Long id, MasVaccineMasterRequest request) {
        log.info("Updating Vaccine Master id={}", id);
        try {
            MasVaccineMaster entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Vaccine not found", 404);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            entity.setVaccineLabel(request.getVaccineLabel());
            entity.setRecommendedAge(request.getRecommendedAge());
            entity.setVaccineGroup(request.getVaccineGroup());
            entity.setDisplayOrder(request.getDisplayOrder());
            entity.setIsMultiDose(request.getIsMultiDose());
            entity.setDosePerVial(request.getDosePerVial());
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error updating Vaccine Master id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Update failed", 500);
        }
    }

    @Override
    public ApiResponse<MasVaccineMasterResponse> changeStatus(
            Long id, String status) {
        log.info("Changing Vaccine Master status, id={}, status={}", id, status);
        try {
            MasVaccineMaster entity = repository.findById(id).orElse(null);
            if (entity == null) {
                return ResponseUtils.createNotFoundResponse(
                        "Vaccine not found", 404);
            }

            if (!status.equals("y")
                    && !status.equals("n")) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Invalid status", 400);
            }

            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", 404);
            }

            entity.setStatus(status);
            entity.setLastUpdatedBy(user.getFirstName());
            entity.setLastUpdateDate(LocalDateTime.now());

            repository.save(entity);

            return ResponseUtils.createSuccessResponse(
                    toResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error changing Vaccine Master status id={}", id, e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Status update failed", 500);
        }
    }

    private MasVaccineMasterResponse toResponse(MasVaccineMaster e) {
        return new MasVaccineMasterResponse(
                e.getVaccineId(),
                e.getVaccineLabel(),
                e.getRecommendedAge(),
                e.getVaccineGroup(),
                e.getDisplayOrder(),
                e.getIsMultiDose(),
                e.getDosePerVial(),
                e.getStatus(),
                e.getLastUpdateDate());
    }
}
