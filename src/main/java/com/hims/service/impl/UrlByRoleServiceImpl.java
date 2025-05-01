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

//    @Override
//    public ApiResponse<List<UrlByRoleResponse>> getAllUrlByRoleId(Long roleId) {
//        List<RoleTemplate> roleTemplates = roleTemplateRepository.findByRoleId(roleId);
//
//        if (roleTemplates.isEmpty()) {
//            return ResponseUtils.createNotFoundResponse("No templates found for given role", 404);
//        }
//
//        List<UrlByRoleResponse> result = new ArrayList<>();
//
//        for (RoleTemplate roleTemplate : roleTemplates) {
//            Long templateId = roleTemplate.getTemplate().getId();
//
//            List<TemplateApplication> templateApps = templateApplicationRepository.findByTemplateId(templateId);
//
//            List<MasApplication> allApps = templateApps.stream()
//                    .map(TemplateApplication::getApp)
//                    .distinct()
//                    .collect(Collectors.toList());
//
//            List<MasApplication> parentApps = allApps.stream()
//                    .filter(app -> "#".equals(app.getUrl()))
//                    .collect(Collectors.toList());
//
//            for (MasApplication parent : parentApps) {
//                UrlByRoleResponse response = new UrlByRoleResponse();
//                response.setParentName(parent.getName());
//                response.setParentUrl(parent.getUrl());
//
//                List<MasApplication> childrenApps = masApplicationRepository.findByParentId(parent.getAppId());
//
//                List<UrlByRoleResponse.ChildUrl> children = childrenApps.stream()
//                        .filter(child -> !"#".equals(child.getUrl()))
//                        .map(childApp -> {
//                            UrlByRoleResponse.ChildUrl child = new UrlByRoleResponse.ChildUrl();
//                            child.setChiledName(childApp.getName());
//                            child.setChiledUrl(childApp.getUrl());
//                            return child;
//                        })
//                        .collect(Collectors.toList());
//
//                response.setChildren(children);
//                result.add(response);
//            }
//        }
//
//        return Optional.of(result)
//                .filter(list -> !list.isEmpty())
//                .map(list -> ResponseUtils.createSuccessResponse(list, new TypeReference<List<UrlByRoleResponse>>() {}))
//                .orElseGet(() -> ResponseUtils.createNotFoundResponse("No application menu found for given role", 404));
//    }

//    @Override
//    public ApiResponse<List<UrlByRoleResponse>> getAllUrlByRoleIds(List<Long> roleIds) {
//        Map<String, UrlByRoleResponse> parentMap = new LinkedHashMap<>();
//
//        for (Long roleId : roleIds) {
//            List<RoleTemplate> roleTemplates = roleTemplateRepository.findByRoleIdAndStatusIgnoreCase(roleId, "y");
//
//            for (RoleTemplate roleTemplate : roleTemplates) {
//                Long templateId = roleTemplate.getTemplate().getId();
//
//                List<TemplateApplication> activeTemplateApps = templateApplicationRepository.findByTemplateIdAndStatusIgnoreCase(templateId, "y");
//
//                for (TemplateApplication templateApp : activeTemplateApps) {
//                    MasApplication masApp = templateApp.getApp();
//                    if (masApp == null || !"y".equalsIgnoreCase(masApp.getStatus())) {
//                        continue;
//                    }
//
//                    if ("0".equals(masApp.getParentId())) {
//                        // It's a parent app
//                        String parentKey = masApp.getName();
//                        UrlByRoleResponse parentResponse = parentMap.getOrDefault(parentKey, new UrlByRoleResponse());
//
//                        parentResponse.setParentName(masApp.getName());
//                        parentResponse.setParentUrl(masApp.getUrl());
//                        parentResponse.setParentAppId(masApp.getAppId());
//
//                        if (parentResponse.getChildren() == null) {
//                            parentResponse.setChildren(new ArrayList<>());
//                        }
//
//                        // Build the children list:
//                        // 1. Fetch active TemplateApplications (again for same template)
//                        // 2. Only pick those MasApplications whose parentId matches current parent appId
//                        for (TemplateApplication childTemplateApp : activeTemplateApps) {
//                            MasApplication childMasApp = childTemplateApp.getApp();
//
//                            if (childMasApp != null
//                                    && !"0".equals(childMasApp.getParentId())
//                                    && "y".equalsIgnoreCase(childMasApp.getStatus())
//                                    && masApp.getAppId().equals(childMasApp.getParentId())) {
//
//                                boolean alreadyExists = parentResponse.getChildren().stream()
//                                        .anyMatch(c -> c.getChiledAppId().equals(childMasApp.getAppId()));
//
//                                if (!alreadyExists) {
//                                    UrlByRoleResponse.ChildUrl child = new UrlByRoleResponse.ChildUrl();
//                                    child.setChiledName(childMasApp.getName());
//                                    child.setChiledUrl(childMasApp.getUrl());
//                                    child.setChiledAppId(childMasApp.getAppId());
//
//                                    // Now check for sub-children
//                                    List<UrlByRoleResponse.ChildUrl> subChildren = new ArrayList<>();
//
//                                    for (TemplateApplication subChildTemplateApp : activeTemplateApps) {
//                                        MasApplication subChildMasApp = subChildTemplateApp.getApp();
//                                        if (subChildMasApp != null
//                                                && "y".equalsIgnoreCase(subChildMasApp.getStatus())
//                                                && childMasApp.getAppId().equals(subChildMasApp.getParentId())) {
//
//                                            UrlByRoleResponse.ChildUrl subChild = new UrlByRoleResponse.ChildUrl();
//                                            subChild.setChiledName(subChildMasApp.getName());
//                                            subChild.setChiledUrl(subChildMasApp.getUrl());
//                                            subChild.setChiledAppId(subChildMasApp.getAppId());
//                                            subChildren.add(subChild);
//                                        }
//                                    }
//
//                                    if (!subChildren.isEmpty()) {
//                                        child.setChildren(subChildren);
//                                    } else {
//                                        child.setChildren(null); // explicitly set null if no sub-children
//                                    }
//
//                                    parentResponse.getChildren().add(child);
//                                }
//                            }
//                        }
//
//
//                        parentMap.put(parentKey, parentResponse);
//
//                    }
//                }
//            }
//        }
//
//        List<UrlByRoleResponse> finalResult = new ArrayList<>(parentMap.values());
//
//        return Optional.of(finalResult)
//                .filter(list -> !list.isEmpty())
//                .map(list -> ResponseUtils.createSuccessResponse(list, new TypeReference<List<UrlByRoleResponse>>() {}))
//                .orElseGet(() -> ResponseUtils.createNotFoundResponse("No application menu found for given roles", 404));
//    }

    @Override
    public ApiResponse<List<UrlByRoleResponse>> getAllUrlByRoleIds(List<Long> roleIds) {
        // Step 1: Get all applications accessible to the provided roles
        Set<MasApplication> accessibleApps = new HashSet<>();

        for (Long roleId : roleIds) {
            List<RoleTemplate> roleTemplates = roleTemplateRepository.findByRoleIdAndStatusIgnoreCase(roleId, "y");

            for (RoleTemplate roleTemplate : roleTemplates) {
                Long templateId = roleTemplate.getTemplate().getId();
                List<TemplateApplication> activeTemplateApps = templateApplicationRepository.findByTemplateIdAndStatusIgnoreCase(templateId, "y");

                for (TemplateApplication templateApp : activeTemplateApps) {
                    MasApplication masApp = templateApp.getApp();
                    if (masApp != null && "y".equalsIgnoreCase(masApp.getStatus())) {
                        accessibleApps.add(masApp);
                    }
                }
            }
        }

        // Step 2: Create a map of all applications by their appId for easy access
        Map<String, MasApplication> appById = new HashMap<>();
        // Also create a map to store parent-child relationships
        Map<String, List<MasApplication>> childrenByParentId = new HashMap<>();

        for (MasApplication app : accessibleApps) {
            appById.put(app.getAppId(), app);

            // Organize children by parent ID
            String parentId = app.getParentId();
            if (parentId != null) {
                childrenByParentId.computeIfAbsent(parentId, k -> new ArrayList<>()).add(app);
            }
        }

        // Step 3: Build the hierarchical structure starting with root nodes (parentId = "0")
        List<UrlByRoleResponse> result = new ArrayList<>();
        List<MasApplication> rootApps = childrenByParentId.getOrDefault("0", Collections.emptyList());

        for (MasApplication rootApp : rootApps) {
            UrlByRoleResponse rootNode = convertToResponse(rootApp);
            buildHierarchy(rootNode, rootApp.getAppId(), childrenByParentId);
            result.add(rootNode);
        }

        // Sort by order number if available
        result.sort(Comparator.comparing(r -> {
            MasApplication app = appById.get(r.getAppId());
            return app != null && app.getOrderNo() != null ? app.getOrderNo() : Long.MAX_VALUE;
        }));

        return Optional.of(result)
                .filter(list -> !list.isEmpty())
                .map(list -> ResponseUtils.createSuccessResponse(list, new TypeReference<List<UrlByRoleResponse>>() {}))
                .orElseGet(() -> ResponseUtils.createNotFoundResponse("No application menu found for given roles", 404));
    }

    // Helper method to convert MasApplication to UrlByRoleResponse
    private UrlByRoleResponse convertToResponse(MasApplication app) {
        UrlByRoleResponse response = new UrlByRoleResponse();
        response.setAppId(app.getAppId());
        response.setName(app.getName());
        response.setUrl(app.getUrl());
        response.setChildren(new ArrayList<>());
        return response;
    }

    // Recursive method to build hierarchy
    private void buildHierarchy(UrlByRoleResponse parent, String parentId,
                                Map<String, List<MasApplication>> childrenByParentId) {
        List<MasApplication> children = childrenByParentId.getOrDefault(parentId, Collections.emptyList());

        // Sort children by order number if available
        children.sort(Comparator.comparing(app -> app.getOrderNo() != null ? app.getOrderNo() : Long.MAX_VALUE));

        for (MasApplication childApp : children) {
            UrlByRoleResponse childNode = convertToResponse(childApp);
            parent.getChildren().add(childNode);

            // Recursively build child's hierarchy
            buildHierarchy(childNode, childApp.getAppId(), childrenByParentId);
        }
    }

}
