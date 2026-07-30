package com.hims.entity.repository;

import com.hims.entity.OpdTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OpdTemplateRepository extends JpaRepository<OpdTemplate, Long> {
    List<OpdTemplate> findByStatusIgnoreCase(String status);

    List<OpdTemplate> findByStatusInIgnoreCase(List<String> statuses);

    @Query("""
            SELECT t
            FROM OpdTemplate t
            WHERE UPPER(t.opdTemplateType) = UPPER(:templateType)
            AND (
                    (:flag = 1 AND UPPER(t.status) = UPPER(:activeStatus))
                 OR (:flag = 0 AND UPPER(t.status) IN (:statuses))
            )
            AND (:doctorId IS NULL OR t.doctorId.userId = :doctorId)
            """)
    List<OpdTemplate> findTemplates(
            @Param("flag") int flag,
            @Param("doctorId") Long doctorId,
            @Param("templateType") String templateType,
            @Param("activeStatus") String activeStatus,
            @Param("statuses") List<String> statuses);

    List<OpdTemplate> findByOpdTemplateType(String opdTemplateType);
}
