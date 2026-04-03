package com.hims.entity.repository;

import com.hims.entity.DgFixedValue;
import com.hims.entity.DgSubMasInvestigation;
import com.hims.entity.DgMasInvestigation;
import com.hims.response.FixedValueResultResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DgFixedValueRepository extends JpaRepository<DgFixedValue,Long> {

   // DgFixedValue findFirstBySubInvestigationId(DgSubMasInvestigation subInvest);
    List<DgFixedValue> findBySubInvestigationId_InvestigationId(DgMasInvestigation masInvest);

    List<DgFixedValue> findBySubInvestigationId(DgSubMasInvestigation subInvest);


    List<DgFixedValue> findBySubInvestigationId_InvestigationId_InvestigationIdIn(List<Long> investigationIds);

    @Query("""
SELECT new com.hims.response.FixedValueResultResponse(
    fv.fixedId,
    fv.fixedValue
)
FROM DgFixedValue fv
WHERE fv.subInvestigationId.subInvestigationId = :subInvestigationId
""")
    List<FixedValueResultResponse> findFixedValuesBySubInvestigationId(Long subInvestigationId);
}
