package com.hims.entity.repository;

import com.hims.entity.DgNormalValue;
import com.hims.entity.DgSubMasInvestigation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DgNormalValueRepository extends JpaRepository<DgNormalValue, Long> {

    DgNormalValue findBySubInvestigationId(DgSubMasInvestigation subInvestigationId);
}
