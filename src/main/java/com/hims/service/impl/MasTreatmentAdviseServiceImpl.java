package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasTreatmentAdvise;
import com.hims.entity.User;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasTreatmentAdviseRepository;
import com.hims.request.MasTreatmentAdviseRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasTreatmentAdviseResponse;
import com.hims.service.MasTreatmentAdviseService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MasTreatmentAdviseServiceImpl implements MasTreatmentAdviseService {
    @Autowired
    private AuthUtil util;
    @Autowired
    private MasTreatmentAdviseRepository masTreatmentAdviseRepository;
    @Autowired
    private MasDepartmentRepository masDepartmentRepository;


    @Override
    public ApiResponse<List<MasTreatmentAdviseResponse>> getAll(int flag) {
        try {
            List<MasTreatmentAdvise> list;

            if (flag == 0) {
                list = masTreatmentAdviseRepository.findAllByOrderByStatusDescLastUpdateDateDesc();
            } else if (flag == 1) {
                list = masTreatmentAdviseRepository.findByStatusIgnoreCaseOrderByTreatmentAdviceAsc("y");
            } else {
                return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Invalid Flag Value , Provide flag as 0 or 1", HttpStatus.BAD_REQUEST.value());
            }

            return ResponseUtils.createSuccessResponse(
                    list.stream().map(this::mapToResponse).toList(),
                    new TypeReference<>() {
                    }
            );

        } catch (Exception e) {
            log.error("getAll() Error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {
                    },
                    "Internal Server Error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<MasTreatmentAdviseResponse> add(MasTreatmentAdviseRequest request) {
        try {
            User currentUser = util.getCurrentUser();
            Long depart= util.getCurrentDepartmentId();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", 404);
            }

            MasDepartment department = masDepartmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Invalid Department Id"));

            MasTreatmentAdvise advise = new MasTreatmentAdvise();
            advise.setDepartment(department);
            advise.setTreatmentAdvice(request.getTreatmentAdvice());
            advise.setStatus("y");
            advise.setCreatedBy(currentUser.getFullName());
            advise.setLastUpdatedBy(currentUser.getFullName());
            advise.setLastUpdateDate(LocalDateTime.now());

            MasTreatmentAdvise saved = masTreatmentAdviseRepository.save(advise);

            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("add() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "Internal Server Error", 500);
        }
    }

    @Override
    public ApiResponse<MasTreatmentAdviseResponse> update(Long id, MasTreatmentAdviseRequest request) {
        try {
            User currentUser = util.getCurrentUser();
            Long depart =util.getCurrentDepartmentId();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", 404);
            }

            MasTreatmentAdvise advise = masTreatmentAdviseRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invalid Treatment Advise Id"));

            MasDepartment department = masDepartmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Invalid Department Id"));

            advise.setDepartment(department);
            advise.setTreatmentAdvice(request.getTreatmentAdvice());
            advise.setLastUpdateDate(LocalDateTime.now());
            advise.setLastUpdatedBy(currentUser.getFullName());
            advise.setStatus("y");

            MasTreatmentAdvise saved = masTreatmentAdviseRepository.save(advise);

            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("update() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "Internal Server Error", 500);
        }
    }

    @Override
    public ApiResponse<MasTreatmentAdviseResponse> changeStatus(Long id, String status) {
        try {
            User currentUser = util.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createNotFoundResponse("Current User Not Found", 404);
            }

            MasTreatmentAdvise advise = masTreatmentAdviseRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invalid Treatment Advise Id"));

            advise.setStatus(status);
            advise.setLastUpdateDate(LocalDateTime.now());
            advise.setLastUpdatedBy(currentUser.getFullName());

            MasTreatmentAdvise saved = masTreatmentAdviseRepository.save(advise);

            return ResponseUtils.createSuccessResponse(mapToResponse(saved), new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("changeStatus() Error :: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "Internal Server Error", 500);
        }
    }

    private MasTreatmentAdviseResponse mapToResponse(MasTreatmentAdvise entity) {
        MasTreatmentAdviseResponse res = new MasTreatmentAdviseResponse();

        res.setTreatmentAdviseId(entity.getTreatmentAdviseId());
        res.setTreatmentAdvice(entity.getTreatmentAdvice());
        res.setStatus(entity.getStatus());
        res.setLastUpdateDate(entity.getLastUpdateDate());
        res.setCreatedBy(entity.getCreatedBy());
        res.setLastUpdatedBy(entity.getLastUpdatedBy());

        if (entity.getDepartment() != null) {
            res.setDepartmentId(entity.getDepartment().getId());
            res.setDepartmentName(entity.getDepartment().getDepartmentName());
        }

        return res;
    }

}
