package com.hims.entity.repository;

import com.hims.entity.MasPacsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RadiologyTemplateRepository extends JpaRepository<MasPacsTemplate,Long> {
    List<MasPacsTemplate> findByStatusIgnoreCaseOrderByTemplateNameAsc(String y);

    List<MasPacsTemplate> findAllByOrderByPacsTemplateIdAsc();

    List<MasPacsTemplate> findAllByOrderByLastUpdateDateDesc();

    @Query("""
        select t
        from MasPacsTemplate t
        where t.subChargecodeId.subId = :subChargecodeId
          and lower(t.status) = lower(:status)
        order by t.templateName asc
    """)
    List<MasPacsTemplate> findBySubChargecodeId_SubIdAndStatusIgnoreCaseOrderByTemplateNameAsc(@Param("subChargecodeId") Long subChargecodeId,
                                                @Param("status") String status);
}
