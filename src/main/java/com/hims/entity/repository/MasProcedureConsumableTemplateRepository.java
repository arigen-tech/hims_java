package com.hims.entity.repository;

import com.hims.entity.MasProcedureConsumableTemplate;
import com.hims.response.ProcedureConsumableTemplateHeaderResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MasProcedureConsumableTemplateRepository extends JpaRepository<MasProcedureConsumableTemplate,Long> {
    boolean existsByTemplateCode(@NotBlank(message = "template_code is required") @Size(max = 20, message = "template_code max length is 20") String templateCode);

    boolean existsByProcedure_ProcedureIdAndTemplateName(@NotNull(message = "procedure_id is required") Long procedureId, @NotBlank(message = "template_name is required") @Size(max = 200, message = "template_name max length is 200") String templateName);

    @Query("""
    SELECT new com.hims.response.ProcedureConsumableTemplateHeaderResponse(
        t.templateId,
        t.templateCode,
        t.templateName
    )
    FROM MasProcedureConsumableTemplate t
    WHERE (
        :search IS NULL
        OR LOWER(t.templateName) LIKE LOWER(CONCAT('%', :search, '%'))
    )
    """)
    Page<ProcedureConsumableTemplateHeaderResponse> getTemplates(
            @Param("search") String search,
            Pageable pageable
    );

}
