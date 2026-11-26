package com.hims.entity.repository;

import com.hims.entity.SurgeryDrugConsumableTemplateDt;
import com.hims.entity.SurgeryDrugConsumableTemplateHd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurgeryDrugConsumableTemplateDtRepository
        extends JpaRepository<SurgeryDrugConsumableTemplateDt, Long> {
    List<SurgeryDrugConsumableTemplateDt> findByTemplateAndStatusIgnoreCaseOrderByTemplateDetailIdAsc(
            SurgeryDrugConsumableTemplateHd template, String status);
}
