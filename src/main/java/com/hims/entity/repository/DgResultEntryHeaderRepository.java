package com.hims.entity.repository;

import com.hims.entity.DgResultEntryHeader;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DgResultEntryHeaderRepository extends JpaRepository<DgResultEntryHeader,Long> {

Optional<DgResultEntryHeader> findBySampleCollectionHeaderId_SampleCollectionHeaderIdAndSubChargeCodeId_SubId(Long sampleCollectionHeaderId, Long subChargeCodeId);


    @Query("SELECT DISTINCT h FROM DgResultEntryHeader h " +
            "JOIN DgResultEntryDetail d ON d.resultEntryId = h " +
            "WHERE h.resultStatus = 'n' AND d.validated = 'n'")
    List<DgResultEntryHeader> findAllUnvalidatedHeaders();

//    @Modifying
//    @Transactional
//    @Query("UPDATE DgResultEntryHeader h SET h.resultStatus = :status WHERE h.resultEntryId = :headerId")
//    void updateResultStatus(@Param("headerId") Long headerId, @Param("status") String status);
//    @Query("""
//        SELECT h FROM DgResultEntryHeader h
//        JOIN FETCH h.sampleCollectionHeaderId sch
//        JOIN FETCH sch.patientId p
//        WHERE p.patientId = :patientId AND h.subChargecodeId.subChargecodeId = :subChargeCodeId
//    """)
//    Optional<DgResultEntryHeader> findExistingHeader(
//            @Param("subChargeCodeId") Long subChargeCodeId);
//
//
//    Optional<DgResultEntryHeader> findByPatientAndSubChargeCodeId( Long subChargeCodeId);
}
