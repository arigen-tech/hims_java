package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDietPreference;
import com.hims.entity.User;
import com.hims.entity.repository.MasDietPreferenceRepository;
import com.hims.request.MasDietPreferenceRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDietPreferenceResponse;
import com.hims.service.MasDietPreferenceService;
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

    @Override
    public ApiResponse<List<MasDietPreferenceResponse>> getAll(int flag) {
        List<MasDietPreference> list =
                (flag == 1) ? repository.findByStatusIgnoreCase("Y") : repository.findAll();

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

        User user = authUtil.getCurrentUser();

        MasDietPreference data = MasDietPreference.builder()
                .preferenceName(request.getPreferenceName())
                .description(request.getDescription())
                .status("y")
                .createdBy(user.getFirstName())
                .lastUpdatedBy(user.getFirstName())
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

        User user = authUtil.getCurrentUser();

        data.setPreferenceName(request.getPreferenceName());
        data.setDescription(request.getDescription());
        data.setLastUpdatedBy(user.getFirstName());
        data.setLastUpdateDate(LocalDateTime.now());

        repository.save(data);

        return ResponseUtils.createSuccessResponse(toResponse(data), new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasDietPreferenceResponse> changeStatus(Long id, String status) {

        MasDietPreference data = repository.findById(id).orElse(null);

        if (data == null)
            return ResponseUtils.createNotFoundResponse("ID Not Found!", 404);

        User user = authUtil.getCurrentUser();

        if (!status.equalsIgnoreCase("y") && !status.equalsIgnoreCase("n"))
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid Status!", 400);

        data.setStatus(status.toUpperCase());
        data.setLastUpdatedBy(user.getFirstName());
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
                m.getLastUpdateDate(),
                m.getCreatedBy(),
                m.getLastUpdatedBy()
        );
    }

}
