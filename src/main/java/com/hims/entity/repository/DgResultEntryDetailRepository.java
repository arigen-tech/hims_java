package com.hims.entity.repository;

import com.hims.entity.DgMasInvestigation;
import com.hims.entity.DgResultEntryDetail;
import com.hims.entity.DgResultEntryHeader;
import com.hims.entity.DgSubMasInvestigation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DgResultEntryDetailRepository extends JpaRepository<DgResultEntryDetail,Long> {
    Optional<DgResultEntryDetail> findByResultEntryIdAndInvestigationIdAndSubInvestigationId(DgResultEntryHeader header, DgMasInvestigation investigation, DgSubMasInvestigation subInvestigation);
}
