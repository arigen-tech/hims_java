package com.hims.entity.repository;

import com.hims.entity.OpdTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpdTemplateRepository extends JpaRepository<OpdTemplate, Long> {
    List<OpdTemplate> findByStatusIgnoreCase(String status);
    List<OpdTemplate> findByStatusInIgnoreCase(List<String> statuses);
}
