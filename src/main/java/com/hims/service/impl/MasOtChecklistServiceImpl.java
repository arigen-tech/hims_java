package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasOtChecklist;
import com.hims.entity.repository.MasOtChecklistRepository;
import com.hims.exception.DuplicateOtChecklistException;
import com.hims.exception.RecordNotFoundException;
import com.hims.mapper.MasOtChecklistMapper;
import com.hims.request.MasOtChecklistRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasOtChecklistResponse;
import com.hims.service.MasOtChecklistService;
import com.hims.service.UserContextService;
import com.hims.response.UserContext;
import com.hims.utils.ResponseUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Service
public class MasOtChecklistServiceImpl implements MasOtChecklistService {
    private final MasOtChecklistRepository repository;
    private final UserContextService userContextService;

    public MasOtChecklistServiceImpl(MasOtChecklistRepository repository, UserContextService userContextService) {
        this.repository = repository;
        this.userContextService = userContextService;
    }

    @Override
    @Transactional
    public ApiResponse<MasOtChecklistResponse> save(MasOtChecklistRequest request) {
        MasOtChecklist entity;
        if (request.getChecklistId() == null) {
            entity = new MasOtChecklist();
            entity.setStatus("Y");
        } else {
            entity = repository.findById(request.getChecklistId())
                    .orElseThrow(() -> new RecordNotFoundException("OT checklist not found"));
        }

        repository.findByChecklistCodeIgnoreCase(request.getChecklistCode().trim())
                .filter(found -> !found.getChecklistId().equals(entity.getChecklistId()))
                .ifPresent(found -> { throw new DuplicateOtChecklistException("Checklist code already exists"); });

        MasOtChecklistMapper.updateEntity(request, entity);
        UserContext userContext = userContextService.getCurrentUserContext();
        entity.setLastChgBy(userContext.getUserFullName());
        entity.setLastChgDate(LocalDateTime.now());
        return success(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<MasOtChecklistResponse> getById(Long id) {
        MasOtChecklist entity = repository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("OT checklist not found"));
        return success(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<MasOtChecklistResponse>> getAll(int flag, String stage, String name, int page, int size) {
        if (flag != 0 && flag != 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flag must be 0 or 1");
        Page<MasOtChecklistResponse> result = repository.search(flag, normalize(stage), normalize(name), PageRequest.of(page, size))
                .map(MasOtChecklistMapper::toResponse);
        return ResponseUtils.createSuccessResponse(result, new TypeReference<>() {});
    }

    @Override
    @Transactional
    public ApiResponse<MasOtChecklistResponse> changeStatus(Long id, String status) {
        if (status == null || !(status.equalsIgnoreCase("Y") || status.equalsIgnoreCase("N")))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be Y or N");
        MasOtChecklist entity = repository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("OT checklist not found"));
        entity.setStatus(status.toUpperCase());
        UserContext userContext = userContextService.getCurrentUserContext();
        entity.setLastChgBy(userContext.getUserFullName());
        entity.setLastChgDate(LocalDateTime.now());
        return success(repository.save(entity));
    }

    private ApiResponse<MasOtChecklistResponse> success(MasOtChecklist entity) {
        return ResponseUtils.createSuccessResponse(MasOtChecklistMapper.toResponse(entity), new TypeReference<>() {});
    }

    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
