package com.hims.entity.repository;

import com.hims.entity.MasIpdProcedureSurgeryConsumableTemplate;
import com.hims.projection.BillingTemplateMainProjection;
import com.hims.projection.BillingTemplateProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MasIpdProcedureSurgeryConsumableTemplateRepository extends JpaRepository<MasIpdProcedureSurgeryConsumableTemplate,Long> {
    @Query(value = """
    SELECT 
        t.template_id AS templateId,
        t.template_type AS templateType,
        t.template_name AS templateName,
        CASE 
            WHEN Upper(t.template_type) = :procedure: THEN p.procedure_name
            WHEN Upper(t.template_type) = :surgery THEN s.surgery_name
        END AS procedureName
    FROM mas_ipd_procedure_surgey_consmble_template t
    LEFT JOIN mas_procedure p ON t.procedure_id = p.procedure_id
    LEFT JOIN mas_surgery s ON t.surgery_id = s.surgery_id
    WHERE t.template_id = :templateId
""", nativeQuery = true)
    BillingTemplateProjection getTemplateById(@Param("templateId") Long id,
                                              @Param("procedure") String procedure,
                                              @Param("surgery") String surgery);

    @Query(value = """
    SELECT 
        t.template_id AS templateId,
        t.template_type AS templateType,
        t.template_name AS templateName,
        CASE 
            WHEN Upper(t.template_type) = :procedure THEN p.procedure_name
            WHEN Upper(t.template_type )=  :surgery THEN s.surgery_name
        END AS procedure,
        COUNT(d.template_detail_id) AS itemCount
    FROM mas_ipd_procedure_surgey_consmble_template t
    LEFT JOIN mas_procedure p ON t.procedure_id = p.procedure_id
    LEFT JOIN mas_surgery s ON t.surgery_id = s.surgery_id
    LEFT JOIN mas_ipd_procedure_consumable_template_detail d 
        ON d.template_id = t.template_id
    WHERE 
        (:templateType IS NULL OR t.template_type = :templateType)
        AND (:templateName IS NULL OR LOWER(t.template_name) LIKE LOWER(CONCAT('%', :templateName, '%')))
    GROUP BY 
        t.template_id, t.template_type, t.template_name, 
        p.procedure_name, s.surgery_name
""",
            countQuery = """
    SELECT COUNT(DISTINCT t.template_id)
    FROM mas_ipd_procedure_surgey_consmble_template t
    LEFT JOIN mas_procedure p ON t.procedure_id = p.procedure_id
    LEFT JOIN mas_surgery s ON t.surgery_id = s.surgery_id
    WHERE 
        (:templateType IS NULL OR t.template_type = :templateType)
        AND (:templateName IS NULL OR LOWER(t.template_name) LIKE LOWER(CONCAT('%', :templateName, '%')))
""",
            nativeQuery = true)
    Page<BillingTemplateMainProjection> searchTemplates(
            @Param("templateType") String templateType,
            @Param("templateName") String templateName,
            @Param("procedure") String procedure,
            @Param("surgery") String surgery,
            Pageable pageable);
}
