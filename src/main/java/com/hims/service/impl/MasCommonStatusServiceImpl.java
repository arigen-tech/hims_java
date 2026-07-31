package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasCommonStatus;
import com.hims.entity.User;
import com.hims.entity.repository.MasCommonStatusRepository;
import com.hims.request.MasCommonStatusRequest;
import com.hims.response.ApiResponse;
import com.hims.response.EntityNameResponse;
import com.hims.response.MasCommonStatusResponse;
import com.hims.service.MasCommonStatusService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MasCommonStatusServiceImpl implements MasCommonStatusService {

    @Autowired
    private MasCommonStatusRepository masCommonStatusRepository;

    @Autowired
    private AuthUtil authUtil;

    @PersistenceContext
    private EntityManager entityManager;

    private User getCurrentUser(){
        User currentUser = authUtil.getCurrentUser();
        if(currentUser !=null){
            return currentUser;
        }
        throw  new RuntimeException("Current user not found");
    }

    @Override
    public ApiResponse<MasCommonStatusResponse> createCommonStatus(MasCommonStatusRequest request) {
        try {

            log.info("MasCommonStatus method started...");
            MasCommonStatus entity= new MasCommonStatus();
            entity.setEntityName(request.getEntityName());
            entity.setTableName(request.getTableName());
            entity.setColumnName(request.getColumnName());
            entity.setStatusCode(request.getStatusCode());
            entity.setStatusName(request.getStatusName());
            entity.setStatusDesc(request.getStatusDesc());
            entity.setRemarks(request.getRemarks());
            entity.setUpdatedBy(getCurrentUser().getFirstName()+" "+getCurrentUser().getLastName());
            MasCommonStatus save = masCommonStatusRepository.save(entity);
            log.info("MasCommonStatus method ended...");
            return ResponseUtils.createSuccessResponse(mapToResponse(save), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("createCommonStatus method error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasCommonStatusResponse> updateCommonStatusById(Long statusId, MasCommonStatusRequest request) {

        try {

            log.info("updateCommonStatusById method started...");
            Optional<MasCommonStatus> byId = masCommonStatusRepository.findById(statusId);
            if (byId.isEmpty()){
                log.warn("updateCommonStatusById method , common status id is not found !!");
                return  ResponseUtils.createNotFoundResponse("Invalid common status id",HttpStatus.NOT_FOUND.value());
            }
            MasCommonStatus entity = byId.get();
            entity.setEntityName(request.getEntityName());
            entity.setTableName(request.getTableName());
            entity.setColumnName(request.getColumnName());
            entity.setStatusCode(request.getStatusCode());
            entity.setStatusName(request.getStatusName());
            entity.setStatusDesc(request.getStatusDesc());
            entity.setRemarks(request.getRemarks());
            entity.setUpdatedBy(getCurrentUser().getFirstName()+" "+getCurrentUser().getLastName());
            MasCommonStatus save = masCommonStatusRepository.save(entity);
            log.info("updateCommonStatusById method ended...");
            return  ResponseUtils.createSuccessResponse(mapToResponse(save), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("updateCommonStatusById method error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasCommonStatusResponse> getCommonStatusById(Long statusId) {

        try {

            log.info("getCommonStatusById method started...");
            Optional<MasCommonStatus> byId = masCommonStatusRepository.findById(statusId);
            if (byId.isEmpty()) {
                log.warn("getCommonStatusById method , common status id is not found !!");
                return ResponseUtils.createNotFoundResponse("Invalid common status id", HttpStatus.NOT_FOUND.value());
            }
            MasCommonStatus entity = byId.get();
            log.info("getCommonStatusById method ended...");
            return  ResponseUtils.createSuccessResponse(mapToResponse(entity), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("getCommonStatusById method error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<MasCommonStatusResponse>> getAllCommonStatus() {
        try {
            log.info("getAllCommonStatus method started...");
            List<MasCommonStatus> allOrderByUpdateDateDesc = masCommonStatusRepository.findAllByOrderByUpdateDateDesc();
            log.info("getAllCommonStatus method ended...");
            return  ResponseUtils.createSuccessResponse(allOrderByUpdateDateDesc.stream().map(this::mapToResponse).toList(), new TypeReference<>() {});
        }catch (Exception e) {
            log.error("getAllCommonStatus method error :: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public Page<EntityNameResponse> searchEntities(String keyword, Pageable pageable) {

        log.info("searchEntities started for keyword: {}", keyword);

        Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();

        List<String> filtered = entities.stream()
                .map(EntityType::getName)
                .filter(name ->
                        name.toLowerCase().contains(keyword.toLowerCase()))
                .sorted()
                .toList();

        int start = (int) pageable.getOffset();

        if (start >= filtered.size()) {
            return Page.empty(pageable);
        }

        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<EntityNameResponse> result = filtered.subList(start, end)
                .stream()
                .map(EntityNameResponse::new)
                .toList();

        return new PageImpl<>(result, pageable, filtered.size());
    }


    @Override
    public ApiResponse<String> getTableNameForEntity(String entityName) {
        String tableName = entityManager.getMetamodel()
                .getEntities()
                .stream()
                .filter(e -> e.getName().equals(entityName))
                .map(e -> {
                    Table table = e.getJavaType().getAnnotation(Table.class);
                    return table != null ? table.name() : e.getName(); // fallback to entity name
                })
                .findFirst()
                .orElseThrow();
        return  ResponseUtils.createSuccessResponse(tableName, new TypeReference<String>() {});
    }

    public ApiResponse<List<String>> getColumnNamesFromEntity(String entityName) {
        List<String> columns = entityManager.getMetamodel()
                .getEntities()
                .stream()
                .filter(e -> e.getName().equals(entityName))
                .findFirst()
                .map(e -> {
                    Class<?> clazz = e.getJavaType();
                    return Arrays.stream(clazz.getDeclaredFields())
                            .map(f -> {
                                Column column = f.getAnnotation(Column.class);
                                return column != null ? column.name() : f.getName();
                            })
                            .collect(Collectors.toList());
                })
                .orElse(List.of());

        return  ResponseUtils.createSuccessResponse(columns, new TypeReference<>() {});
    }



    private MasCommonStatusResponse mapToResponse(MasCommonStatus entity){
        MasCommonStatusResponse response= new MasCommonStatusResponse();
        response.setCommonStatusId(entity.getCommonStatusId());
        response.setEntityName(entity.getEntityName());
        response.setTableName(entity.getTableName());
        response.setColumnName(entity.getColumnName());
        response.setStatusCode(entity.getStatusCode());
        response.setStatusName(entity.getStatusName());
        response.setStatusDesc(entity.getStatusDesc());
        response.setRemarks(entity.getRemarks());
        response.setUpdateDate(entity.getUpdateDate());

        return  response;
    }
}
