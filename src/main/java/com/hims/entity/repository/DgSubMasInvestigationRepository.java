package com.hims.entity.repository;

import com.hims.entity.DgSubMasInvestigation;
import com.hims.response.SubInvestigationResultResponse;
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

    @Query("""
SELECT new com.hims.response.SubInvestigationResultResponse(
    s.subInvestigationId,
    s.subInvestigationName,

    CASE
        WHEN s.comparisonType = 'f' THEN s.fixedValueExpectedValue

        WHEN s.comparisonType = 'n' THEN CONCAT(nv.minNormalValue, ' - ', nv.maxNormalValue)
                
        ELSE NULL
    END,
    CASE
        WHEN s.comparisonType = 'n' THEN nv.normalId
        
        ELSE NULL
    END,

    s.comparisonType,
    u.name,
    s.resultType
)

FROM DgSubMasInvestigation s

LEFT JOIN s.uomId u

LEFT JOIN DgNormalValue nv
    ON nv.subInvestigationId = s
    AND nv.sex = :genderCode
    AND :age BETWEEN nv.fromAge AND nv.toAge

WHERE s.investigationId.investigationId = :investigationId
""")
    List<SubInvestigationResultResponse> findSubInvestigationsWrtInvAndGenderAndAge(
            Long investigationId,
            String genderCode,
            Long age
    );
}
