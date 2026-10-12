package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.MasDietPreference;
import com.hims.entity.User;
import com.hims.entity.repository.MasDietPreferenceRepository;
import com.hims.request.MasDietPreferenceRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDietPreferenceResponse;
import com.hims.response.UserContext;
import com.hims.service.MasDietPreferenceService;
import com.hims.service.UserContextService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MasDietPreferenceServiceImpl implements MasDietPreferenceService {

    @Autowired
    private MasDietPreferenceRepository repository;

    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private UserContextService userContextService;

    @Override
    public ApiResponse<List<MasDietPreferenceResponse>> getAll(int flag) {
        List<MasDietPreference> list =
                (flag == 1) ? repository.findByStatusIgnoreCaseOrderByPreferenceNameAsc("Y") : repository.findAllByOrderByStatusDescLastUpdateDateDesc();

        List<MasDietPreferenceResponse> response =
                list.stream().map(this::toResponse).collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasDietPreferenceResponse> getById(Long id) {
        MasDietPreference obj = repository.findById(id)
                .orElse(null);

        if (obj == null)
            return ResponseUtils.createNotFoundResponse("ID Not Found!", 404);

        return ResponseUtils.createSuccessResponse(toResponse(obj), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasDietPreferenceResponse> create(MasDietPreferenceRequest request) {

        UserContext userContext = userContextService.getCurrentUserContext();

        MasDietPreference data = MasDietPreference.builder()
                .preferenceName(request.getPreferenceName())
                .description(request.getDescription())
                .status(AppConstants.STATUS_Y.toLowerCase())
                .createdBy(userContext.getUserFullName())
                .lastUpdatedBy(userContext.getUserFullName())
                .lastUpdateDate(LocalDateTime.now())
                .build();

        MasDietPreference saved = repository.save(data);

        return ResponseUtils.createSuccessResponse(toResponse(saved), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasDietPreferenceResponse> update(Long id, MasDietPreferenceRequest request) {

        MasDietPreference data = repository.findById(id).orElse(null);

        if (data == null)
            return ResponseUtils.createNotFoundResponse("ID Not Found!", 404);

        UserContext userContext = userContextService.getCurrentUserContext();

        data.setPreferenceName(request.getPreferenceName());
        data.setDescription(request.getDescription());
        data.setLastUpdatedBy(userContext.getUserFullName());
        data.setLastUpdateDate(LocalDateTime.now());

        repository.save(data);

        return ResponseUtils.createSuccessResponse(toResponse(data), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasDietPreferenceResponse> changeStatus(Long id, String status) {

        MasDietPreference data = repository.findById(id).orElse(null);

        if (data == null)
            return ResponseUtils.createNotFoundResponse("ID Not Found!", 404);

        UserContext userContext = userContextService.getCurrentUserContext();

        if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n"))
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid Status!", 400);

        data.setStatus(status);
        data.setLastUpdatedBy(userContext.getUserFullName());
        data.setLastUpdateDate(LocalDateTime.now());

        repository.save(data);

        return ResponseUtils.createSuccessResponse(toResponse(data), new TypeReference<>() {});
    }

    private MasDietPreferenceResponse toResponse(MasDietPreference m) {
        return new MasDietPreferenceResponse(
                m.getDietPreferenceId(),
                m.getPreferenceName(),
                m.getDescription(),
                m.getStatus(),
                m.getLastUpdateDate()
//                ,m.getCreatedBy(),
//                m.getLastUpdatedBy()
        );
    }

}
