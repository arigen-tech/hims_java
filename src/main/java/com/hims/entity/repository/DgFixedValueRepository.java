package com.hims.entity.repository;

import com.hims.entity.DgFixedValue;
import com.hims.entity.DgSubMasInvestigation;
import com.hims.entity.DgMasInvestigation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DgFixedValueRepository extends JpaRepository<DgFixedValue,Long> {

    DgFixedValue findFirstBySubInvestigationId(DgSubMasInvestigation subInvest);
    List<DgFixedValue> findBySubInvestigationId_InvestigationId(DgMasInvestigation masInvest);
}
