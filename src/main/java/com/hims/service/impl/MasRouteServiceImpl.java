package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasRoute;
import com.hims.entity.User;
import com.hims.entity.repository.MasRouteRepository;
import com.hims.request.MasRouteRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasRouteResponse;
import com.hims.service.MasRouteService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MasRouteServiceImpl implements MasRouteService {

    @Autowired
    private MasRouteRepository repository;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<List<MasRouteResponse>> getAll(int flag) {
        try {
            List<MasRoute> list =
                    (flag == 1) ? repository.findByStatusIgnoreCaseOrderByLastUpdateDateDesc("y") : repository.findAllByOrderByLastUpdateDateDesc();

            List<MasRouteResponse> response =
                    list.stream().map(this::toResponse).collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Something went wrong: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasRouteResponse> getById(Long id) {
        try {
            MasRoute route = repository.findById(id).orElse(null);

            if (route == null)
                return ResponseUtils.createNotFoundResponse("Route ID not found!", 404);

            return ResponseUtils.createSuccessResponse(toResponse(route), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasRouteResponse> create(MasRouteRequest request) {
        try {
            User user = authUtil.getCurrentUser();

            MasRoute route = MasRoute.builder()
                    .routeCode(request.getRouteCode())
                    .routeName(request.getRouteName())
                    .description(request.getDescription())
                    .status("y")
                    .createdBy(user.getFirstName())
                    .lastUpdatedBy(user.getFirstName())
                    .lastUpdateDate(LocalDateTime.now())
                    .build();

            MasRoute saved = repository.save(route);

            return ResponseUtils.createSuccessResponse(toResponse(saved), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Something went wrong: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasRouteResponse> update(Long id, MasRouteRequest request) {
        try {
            MasRoute route = repository.findById(id).orElse(null);

            if (route == null)
                return ResponseUtils.createNotFoundResponse("Route ID not found!", 404);

            User user = authUtil.getCurrentUser();

            route.setRouteCode(request.getRouteCode());
            route.setRouteName(request.getRouteName());
            route.setDescription(request.getDescription());
            route.setLastUpdatedBy(user.getFirstName());
            route.setLastUpdateDate(LocalDateTime.now());

            repository.save(route);

            return ResponseUtils.createSuccessResponse(toResponse(route), new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Update failed: " + e.getMessage(), 500);
        }
    }

    @Override
    public ApiResponse<MasRouteResponse> changeStatus(Long id, String status) {
        try {
            MasRoute route = repository.findById(id).orElse(null);

            if (route == null)
                return ResponseUtils.createNotFoundResponse("Route ID not found!", 404);

            if (!status.equals("y") && !status.equals("n"))
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid status!", 400);

            User user = authUtil.getCurrentUser();

            route.setStatus(status);
            route.setLastUpdatedBy(user.getFirstName());
            route.setLastUpdateDate(LocalDateTime.now());

            repository.save(route);

            return ResponseUtils.createSuccessResponse(toResponse(route), new TypeReference<>() {});

        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Status update failed: " + e.getMessage(), 500);
        }
    }

    private MasRouteResponse toResponse(MasRoute r) {
        return new MasRouteResponse(
                r.getRouteId(),
                r.getRouteCode(),
                r.getRouteName(),
                r.getDescription(),
                r.getStatus(),
                r.getLastUpdateDate(),
                r.getCreatedBy(),
                r.getLastUpdatedBy()
        );
    }
}
