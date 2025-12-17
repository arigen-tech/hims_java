package com.hims.entity.repository;

import com.hims.entity.MasTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MasTemplateRepository extends JpaRepository<MasTemplate, Long> {
    List<MasTemplate> findByStatusIgnoreCase(String status);
    List<MasTemplate> findByStatusInIgnoreCase(List<String> statuses);

    List<MasTemplate> findByStatusIgnoreCaseOrderByTemplateNameAsc(String y);

    List<MasTemplate> findByStatusInIgnoreCaseOrderByLastChgDateDesc(List<String> y);
}
