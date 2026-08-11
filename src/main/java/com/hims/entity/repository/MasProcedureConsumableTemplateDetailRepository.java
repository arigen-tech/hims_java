package com.hims.entity.repository;

import com.hims.entity.MasProcedureConsumableTemplateDetail;
import com.hims.response.ProcedureConsumableTemplateDetailsResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasProcedureConsumableTemplateDetailRepository extends JpaRepository<MasProcedureConsumableTemplateDetail,Long> {
    @Query("""
        SELECT new com.hims.response.ProcedureConsumableTemplateDetailsResponse(
            d.templateDetailId,
            d.item.itemId,
            d.defaultQty,
            d.item.nomenclature
        )
        FROM MasProcedureConsumableTemplateDetail d
        WHERE d.template.templateId = :templateId
        ORDER BY d.displayOrder ASC
        """)
    List<ProcedureConsumableTemplateDetailsResponse> getTemplateDetails(
            @Param("templateId") Long templateId
    );
}
