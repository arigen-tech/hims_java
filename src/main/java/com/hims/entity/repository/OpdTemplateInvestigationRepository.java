package com.hims.entity.repository;

import com.hims.entity.OpdTemplate;
import com.hims.entity.OpdTemplateInvestigation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpdTemplateInvestigationRepository extends JpaRepository<OpdTemplateInvestigation, Long> {
    List<OpdTemplateInvestigation> findByOpdTemplateId(OpdTemplate opdTemplate);
}
