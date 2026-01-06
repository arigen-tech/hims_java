package com.hims.entity.repository;

import com.hims.entity.DgMasInvestigation;
import com.hims.entity.DgResultEntryDetail;
import com.hims.entity.DgResultEntryHeader;
import com.hims.entity.DgSubMasInvestigation;
import io.netty.resolver.dns.DnsServerAddresses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DgResultEntryDetailRepository extends JpaRepository<DgResultEntryDetail,Long> , JpaSpecificationExecutor<DgResultEntryDetail> {
    Optional<DgResultEntryDetail> findByResultEntryIdAndInvestigationIdAndSubInvestigationId(DgResultEntryHeader header, DgMasInvestigation investigation, DgSubMasInvestigation subInvestigation);

    List<DgResultEntryDetail> findByResultEntryIdAndValidated(DgResultEntryHeader header, String n);


    List<DgResultEntryDetail> findByResultEntryId(DgResultEntryHeader resultEntryId);
    @Query("SELECT d FROM DgResultEntryDetail d " +
            "WHERE d.resultEntryId = :header " +
            "AND LOWER(d.validated) = 'y'")
    List<DgResultEntryDetail> findValidatedDetailsByHeader(@Param("header") DgResultEntryHeader header);



}
