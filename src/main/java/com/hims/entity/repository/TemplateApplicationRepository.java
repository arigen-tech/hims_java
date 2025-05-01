package com.hims.entity.repository;

import com.hims.entity.TemplateApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TemplateApplicationRepository extends JpaRepository<TemplateApplication, Long> {
    List<TemplateApplication> findByStatusIgnoreCase(String status);
    List<TemplateApplication> findByStatusInIgnoreCase(List<String> statuses);
    List<TemplateApplication> findByTemplateId(Long templateId);
    Optional<TemplateApplication> findByTemplate_IdAndApp_AppId(Long templateId, String appId);

    List<TemplateApplication> findByApp_AppId(String appId);
    @Query("SELECT ta FROM TemplateApplication ta WHERE ta.template.id = :templateId AND ta.app.appId = :appId")
    Optional<TemplateApplication> findByTemplateAndApp(
            @Param("templateId") Long templateId,
            @Param("appId") String appId);

    List<TemplateApplication> findByTemplateIdAndStatusIgnoreCase(Long templateId, String y);

    Optional<TemplateApplication> findByTemplateIdAndApp_AppId(Long templateId, String appId);

    List<TemplateApplication> findByApp_AppIdAndStatusNot(String appId, String status);

}
