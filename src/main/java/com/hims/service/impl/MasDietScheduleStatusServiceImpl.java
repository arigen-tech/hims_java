package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDietScheduleStatus;
import com.hims.entity.User;
import com.hims.entity.repository.MasDietScheduleStatusRepository;
import com.hims.request.MasDietScheduleStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDietScheduleStatusResponse;
import com.hims.response.UserContext;
import com.hims.service.MasDietScheduleStatusService;
import com.hims.service.UserContextService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasDietScheduleStatusServiceImpl implements MasDietScheduleStatusService {

    @Autowired
    private MasDietScheduleStatusRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private UserContextService userContextService;

    @Override
    public ApiResponse<List<MasDietScheduleStatusResponse>> getAll(int flag) {
        try {
            List<MasDietScheduleStatus> list =
                    (flag == 1) ? repository.findByStatusIgnoreCaseOrderByStatusNameAsc("y") : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

            List<MasDietScheduleStatusResponse> response =
                    list.stream().map(this::toResponse).collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Something went wrong: " + ex.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasDietScheduleStatusResponse> getById(Long id) {
        try {
            MasDietScheduleStatus data = repository.findById(id).orElse(null);

            if (data == null)
                return ResponseUtils.createNotFoundResponse("ID Not Found!", 404);

            return ResponseUtils.createSuccessResponse(toResponse(data), new TypeReference<>() {});
        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Something went wrong: " + ex.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasDietScheduleStatusResponse> create(MasDietScheduleStatusRequest request) {
        try {
            UserContext userContext = userContextService.getCurrentUserContext();

            MasDietScheduleStatus data = MasDietScheduleStatus.builder()
                    .statusName(request.getStatusName())
                    .description(request.getDescription())
                    .status("y")
                    .createdBy(userContext.getUserFullName())
                    .lastUpdatedBy(userContext.getUserFullName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            repository.save(data);

            return ResponseUtils.createSuccessResponse(toResponse(data), new TypeReference<>() {});
        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Something went wrong: " + ex.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasDietScheduleStatusResponse> update(Long id, MasDietScheduleStatusRequest request) {
        try {
            MasDietScheduleStatus data = repository.findById(id).orElse(null);

            if (data == null)
                return ResponseUtils.createNotFoundResponse("ID Not Found!", 404);

            UserContext userContext = userContextService.getCurrentUserContext();

            data.setStatusName(request.getStatusName());
            data.setDescription(request.getDescription());
            data.setLastUpdatedBy(userContext.getUserFullName());
            data.setLastUpdateDate(LocalDateTime.now());

            repository.save(data);

            return ResponseUtils.createSuccessResponse(toResponse(data), new TypeReference<>() {});
        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Something went wrong: " + ex.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasDietScheduleStatusResponse> changeStatus(Long id, String status) {
        try {
            MasDietScheduleStatus data = repository.findById(id).orElse(null);

            if (data == null)
                return ResponseUtils.createNotFoundResponse("ID Not Found!", 404);

            if (!status.equals("y") && !status.equals("n"))
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid Status!", 400);

            UserContext userContext = userContextService.getCurrentUserContext();

            data.setStatus(status);
            data.setLastUpdatedBy(userContext.getUserFullName());
            data.setLastUpdateDate(LocalDateTime.now());

            repository.save(data);

            return ResponseUtils.createSuccessResponse(toResponse(data), new TypeReference<>() {});
        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Something went wrong: " + ex.getMessage(), 500);
        }
    }


    private MasDietScheduleStatusResponse toResponse(MasDietScheduleStatus m) {
        return new MasDietScheduleStatusResponse(
                m.getDietScheduleStatusId(),
                m.getStatusName(),
                m.getDescription(),
                m.getStatus(),
                m.getLastUpdateDate()
//               , m.getCreatedBy(),
//                m.getLastUpdatedBy()
        );
    }

}
