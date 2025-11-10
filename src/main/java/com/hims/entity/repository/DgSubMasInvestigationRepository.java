package com.hims.entity.repository;

import com.hims.entity.DgSubMasInvestigation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DgSubMasInvestigationRepository extends JpaRepository<DgSubMasInvestigation, Long> {
    @Query("SELECT s FROM DgSubMasInvestigation s " +
            "WHERE s.investigationId.investigationId = :investigationId " +
            "AND s.status = 'y'")
    List<DgSubMasInvestigation> findByInvestigationId(@Param("investigationId") Long investigationId);

    List<DgSubMasInvestigation> findByInvestigationIdIn(List<Long> investigationIds);

    List<DgSubMasInvestigation> findByInvestigationId_InvestigationIdIn(List<Long> investigationIds);
}
