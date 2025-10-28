package com.hims.entity.repository;

import com.hims.entity.DgMasInvestigation;
import com.hims.entity.DgNormalValue;
import com.hims.entity.DgSubMasInvestigation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DgNormalValueRepository extends JpaRepository<DgNormalValue, Long> {

    DgNormalValue findBySubInvestigationId(DgSubMasInvestigation subInvestigationId);

    List<DgNormalValue> findBySubInvestigationId_InvestigationId(DgMasInvestigation masInvest);

}
