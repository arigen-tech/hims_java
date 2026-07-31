package com.hims.entity.repository;

import com.hims.entity.MasIpdProcedureConsumableTemplateDetail;
import com.hims.entity.MasIpdProcedureSurgeryConsumableTemplate;
import com.hims.projection.BillingTemplateDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Repository
public interface MasIpdProcedureConsumableTemplateDetailRepository extends JpaRepository<MasIpdProcedureConsumableTemplateDetail,Long> {
    @Query("SELECT d FROM MasIpdProcedureConsumableTemplateDetail d WHERE d.template = :template")
    List<MasIpdProcedureConsumableTemplateDetail> findByTemplate(
            @Param("template") MasIpdProcedureSurgeryConsumableTemplate template);

    @Query("SELECT d FROM MasIpdProcedureConsumableTemplateDetail d " +
            "WHERE d.template.templateId = :templateId " +
            "AND d.templateDetailId IN :templateDetailId")
    List<MasIpdProcedureConsumableTemplateDetail> findValidDetails(
            @Param("templateId") Long templateId,
            @Param("templateDetailId") List<Long> ids);

    @Query(value = """
    SELECT 
        d.template_detail_id AS templateDetailsId,
        d.item_id AS itemId,
        i.nomenclature AS itemName,
        i.disp_unit AS unit,
        mit.item_type_name AS type,
        d.default_qty AS qty
    FROM mas_ipd_procedure_consumable_template_detail d
    JOIN mas_store_item i ON d.item_id = i.item_id
    JOIN mas_item_type mit ON mit.item_type_id = i.item_type_id
    WHERE d.template_id = :templateId
""", nativeQuery = true)
    List<BillingTemplateDetailProjection> getTemplateDetails(@Param("templateId") Long templateId);


}
