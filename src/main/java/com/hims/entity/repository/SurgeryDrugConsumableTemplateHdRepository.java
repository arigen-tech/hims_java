package com.hims.entity.repository;

import com.hims.entity.SurgeryDrugConsumableTemplateHd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SurgeryDrugConsumableTemplateHdRepository
        extends JpaRepository<SurgeryDrugConsumableTemplateHd, Long> {

    @Query(value = """
            SELECT DISTINCT h.*
            FROM public.surgery_drug_consumable_template_hd h
            JOIN public.surgery_drug_consumable_template_dt d ON d.template_id = h.template_id
            JOIN public.mas_store_item i ON i.item_id = d.item_id
            JOIN public.mas_store_section s ON s.section_id = i.section_id
            WHERE h.surgery_id = :surgeryId
              AND UPPER(h.status) = UPPER(:activeStatus)
              AND UPPER(d.status) = UPPER(:activeStatus)
              AND ((UPPER(:sectionCode) = UPPER(:drugSectionCode)
                    AND UPPER(s.section_code) = UPPER(:drugSectionCode))
                   OR (UPPER(:sectionCode) = UPPER(:nonDrugSectionCode)
                    AND UPPER(s.section_code) <> UPPER(:drugSectionCode)))
            ORDER BY h.template_id DESC
            """, nativeQuery = true)
    List<SurgeryDrugConsumableTemplateHd> findActiveBySurgeryAndSectionCode(
            @Param("surgeryId") Long surgeryId,
            @Param("sectionCode") String sectionCode,
            @Param("activeStatus") String activeStatus,
            @Param("drugSectionCode") String drugSectionCode,
            @Param("nonDrugSectionCode") String nonDrugSectionCode);
}
