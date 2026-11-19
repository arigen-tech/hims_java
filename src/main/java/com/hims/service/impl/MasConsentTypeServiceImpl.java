package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasConsentType;
import com.hims.entity.repository.MasConsentTypeRepository;
import com.hims.exception.RecordNotFoundException;
import com.hims.mapper.MasConsentTypeMapper;
import com.hims.request.MasConsentTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasConsentTypeResponse;
import com.hims.response.UserContext;
import com.hims.service.MasConsentTypeService;
import com.hims.service.UserContextService;
import com.hims.utils.ResponseUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MasConsentTypeServiceImpl implements MasConsentTypeService {
    private final MasConsentTypeRepository repository;
    private final MasConsentTypeMapper mapper;
    private final UserContextService userContextService;

    public MasConsentTypeServiceImpl(MasConsentTypeRepository repository, MasConsentTypeMapper mapper,
                                     UserContextService userContextService) {
        this.repository = repository;
        this.mapper = mapper;
        this.userContextService = userContextService;
    }

    @Override
    @Transactional
    public ApiResponse<MasConsentTypeResponse> create(MasConsentTypeRequest request) {
        String code = request.getConsentTypeCode().trim();
        if (repository.findByConsentTypeCodeIgnoreCase(code).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Consent type code already exists");
        }
        LocalDateTime now = LocalDateTime.now();
        MasConsentType entity = new MasConsentType();
        mapper.updateEntity(request, entity);
        entity.setStatus("Y");
        entity.setLastChgBy(currentActor());
        entity.setLastChgDate(now);
        return success(repository.save(entity));
    }

    @Override
    @Transactional
    public ApiResponse<MasConsentTypeResponse> update(Long id, MasConsentTypeRequest request) {
        MasConsentType entity = repository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Consent type not found"));
        repository.findByConsentTypeCodeIgnoreCase(request.getConsentTypeCode().trim())
                .filter(existing -> !existing.getConsentTypeId().equals(id))
                .ifPresent(existing -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "Consent type code already exists"); });
        mapper.updateEntity(request, entity);
        entity.setLastChgBy(currentActor());
        entity.setLastChgDate(LocalDateTime.now());
        return success(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<MasConsentTypeResponse> getById(Long id) {
        return success(repository.findById(id).orElseThrow(() -> new RecordNotFoundException("Consent type not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<MasConsentTypeResponse>> getAll(int flag, String search) {
        validateFlag(flag);
        List<MasConsentTypeResponse> result = repository.searchAll(flag, normalize(search)).stream().map(mapper::toResponse).toList();
        return ResponseUtils.createSuccessResponse(result, new TypeReference<>() {});
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<MasConsentTypeResponse>> getAll(int flag, String search, int page, int size) {
        validateFlag(flag);
        Page<MasConsentTypeResponse> result = repository.search(flag, normalize(search),
                PageRequest.of(page, size, Sort.unsorted())).map(mapper::toResponse);
        return ResponseUtils.createSuccessResponse(result, new TypeReference<>() {});
    }

    @Override
    @Transactional
    public ApiResponse<MasConsentTypeResponse> changeStatus(Long id, String status) {
        if (status == null || !(status.equalsIgnoreCase("Y") || status.equalsIgnoreCase("N"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be Y or N");
        }
        MasConsentType entity = repository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Consent type not found"));
        entity.setStatus(status.toUpperCase());
        entity.setLastChgBy(currentActor());
        entity.setLastChgDate(LocalDateTime.now());
        return success(repository.save(entity));
    }

    private void validateFlag(int flag) {
        if (flag != 0 && flag != 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flag must be 0 or 1");
    }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String currentActor() {
        UserContext context = userContextService.getCurrentUserContext();
        return context == null ? "SYSTEM" : context.getUserFullName();
    }
    private ApiResponse<MasConsentTypeResponse> success(MasConsentType entity) {
        return ResponseUtils.createSuccessResponse(mapper.toResponse(entity), new TypeReference<>() {});
    }
}
