package com.hims.entity.repository;

import com.hims.entity.TemplateApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TemplateApplicationRepository extends JpaRepository<TemplateApplication, Long> {
    List<TemplateApplication> findByStatusIgnoreCase(String status);
    List<TemplateApplication> findByStatusInIgnoreCase(List<String> statuses);
    List<TemplateApplication> findByTemplateId(Long templateId);
    Optional<TemplateApplication> findByTemplate_IdAndApp_AppId(Long templateId, String appId);
}
