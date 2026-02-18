package com.hims.entity.repository;

import com.hims.entity.DgMasInvestigation;
import com.hims.entity.DgResultEntryDetail;
import com.hims.entity.DgResultEntryHeader;
import com.hims.entity.DgSubMasInvestigation;
import com.hims.response.ResultForInvestigationResponse;
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


    List<DgResultEntryDetail> findByResultEntryId_ResultEntryIdAndValidated(DgResultEntryHeader dgResultEntryHeader, String y);

    List<DgResultEntryDetail> findByResultEntryId_ResultEntryIdAndValidatedIgnoreCase(Long resultEntryId, String y);
    @Query("""
        select new  com.hims.response.ResultForInvestigationResponse(
            p.id,
            p.patientFn,
            p.patientAge,
            inv.investigationName,
            d.normalRange,
            d.result
        )
        from DgResultEntryDetail d
        join d.resultEntryId h
        join h.orderHd oh
        join oh.patientId p
        join d.investigationId inv
        where p.id = :patientId
          and lower(d.validated) = 'y'
    """)
    List<ResultForInvestigationResponse> findValidatedResultsByPatient(@Param("patientId") Long patientId);
  //  List<ResultForInvestigationResponse> findValidatedResultsByPatient(Long patientId);
}
