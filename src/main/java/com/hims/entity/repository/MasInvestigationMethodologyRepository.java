package com.hims.entity.repository;

import com.hims.entity.MasInvestigationMethodology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasInvestigationMethodologyRepository extends JpaRepository<MasInvestigationMethodology,Long> {
}
