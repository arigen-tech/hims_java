package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasApplication;
import com.hims.entity.RoleTemplate;
import com.hims.entity.TemplateApplication;
import com.hims.entity.User;
import com.hims.entity.repository.MasApplicationRepository;
import com.hims.entity.repository.RoleTemplateRepository;
import com.hims.entity.repository.TemplateApplicationRepository;
import com.hims.exception.SDDException;
import com.hims.response.ApiResponse;
import com.hims.response.UrlByRoleResponse;
import com.hims.service.UrlByRoleService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UrlByRoleServiceImpl implements UrlByRoleService {

    @Autowired
    private TemplateApplicationRepository templateApplicationRepository;

    @Autowired
    private RoleTemplateRepository roleTemplateRepository;

    @Autowired
    private MasApplicationRepository masApplicationRepository;

    @Override
    public ApiResponse<List<UrlByRoleResponse>> getAllUrlByRoleId(Long roleId) {
        List<RoleTemplate> roleTemplates = roleTemplateRepository.findByRoleId(roleId);

        if (roleTemplates.isEmpty()) {
            return ResponseUtils.createNotFoundResponse("No templates found for given role", 404);
        }

        List<UrlByRoleResponse> result = new ArrayList<>();

        for (RoleTemplate roleTemplate : roleTemplates) {
            Long templateId = roleTemplate.getTemplate().getId();

            List<TemplateApplication> templateApps = templateApplicationRepository.findByTemplateId(templateId);

            List<MasApplication> allApps = templateApps.stream()
                    .map(TemplateApplication::getApp)
                    .distinct()
                    .collect(Collectors.toList());

            List<MasApplication> parentApps = allApps.stream()
                    .filter(app -> "#".equals(app.getUrl()))
                    .collect(Collectors.toList());

            for (MasApplication parent : parentApps) {
                UrlByRoleResponse response = new UrlByRoleResponse();
                response.setParentName(parent.getName());
                response.setParentUrl(parent.getUrl());

                List<MasApplication> childrenApps = masApplicationRepository.findByParentId(parent.getAppId());

                List<UrlByRoleResponse.ChildUrl> children = childrenApps.stream()
                        .filter(child -> !"#".equals(child.getUrl()))
                        .map(childApp -> {
                            UrlByRoleResponse.ChildUrl child = new UrlByRoleResponse.ChildUrl();
                            child.setChiledName(childApp.getName());
                            child.setChiledUrl(childApp.getUrl());
                            return child;
                        })
                        .collect(Collectors.toList());

                response.setChildren(children);
                result.add(response);
            }
        }

        return Optional.of(result)
                .filter(list -> !list.isEmpty())
                .map(list -> ResponseUtils.createSuccessResponse(list, new TypeReference<List<UrlByRoleResponse>>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("No application menu found for given role", 404));
    }

    @Override
    public ApiResponse<List<UrlByRoleResponse>> getAllUrlByRoleIds(List<Long> roleIds) {
        Map<String, UrlByRoleResponse> parentMap = new LinkedHashMap<>();

        for (Long roleId : roleIds) {
            // Only get active role templates (status 'y' or 'Y')
            List<RoleTemplate> roleTemplates = roleTemplateRepository.findByRoleIdAndStatusIgnoreCase(roleId, "y");

            for (RoleTemplate roleTemplate : roleTemplates) {
                Long templateId = roleTemplate.getTemplate().getId();
                List<TemplateApplication> templateApps = templateApplicationRepository.findByTemplateIdAndStatusIgnoreCase(templateId,"y");

                List<MasApplication> allApps = templateApps.stream()
                        .map(TemplateApplication::getApp)
                        .distinct()
                        .collect(Collectors.toList());

                List<MasApplication> parentApps = allApps.stream()
                        .filter(app -> "#".equals(app.getUrl()))
                        .collect(Collectors.toList());

                for (MasApplication parent : parentApps) {
                    String parentKey = parent.getName();
                    UrlByRoleResponse response = parentMap.getOrDefault(parentKey, new UrlByRoleResponse());
                    response.setParentName(parent.getName());
                    response.setParentUrl(parent.getUrl());

                    List<MasApplication> childrenApps = masApplicationRepository.findByParentIdAndStatusIgnoreCase((parent.getAppId()),"y");

                    List<UrlByRoleResponse.ChildUrl> children = childrenApps.stream()
                            .filter(child -> !"#".equals(child.getUrl()))
                            .map(childApp -> {
                                UrlByRoleResponse.ChildUrl child = new UrlByRoleResponse.ChildUrl();
                                child.setChiledName(childApp.getName());
                                child.setChiledUrl(childApp.getUrl());
                                return child;
                            })
                            .collect(Collectors.toList());

                    if (response.getChildren() == null) {
                        response.setChildren(new ArrayList<>());
                    }

                    Set<String> existingUrls = response.getChildren().stream()
                            .map(UrlByRoleResponse.ChildUrl::getChiledUrl)
                            .collect(Collectors.toSet());

                    for (UrlByRoleResponse.ChildUrl child : children) {
                        if (!existingUrls.contains(child.getChiledUrl())) {
                            response.getChildren().add(child);
                        }
                    }

                    parentMap.put(parentKey, response);
                }
            }
        }

        List<UrlByRoleResponse> finalResult = new ArrayList<>(parentMap.values());

        return Optional.of(finalResult)
                .filter(list -> !list.isEmpty())
                .map(list -> ResponseUtils.createSuccessResponse(list, new TypeReference<List<UrlByRoleResponse>>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("No application menu found for given roles", 404));
    }



}
