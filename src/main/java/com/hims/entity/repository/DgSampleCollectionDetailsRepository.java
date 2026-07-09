package com.hims.entity.repository;

import com.hims.entity.DgSampleCollectionDetails;
import com.hims.projection.SampleHeaderForValidationProjection;
import com.hims.response.InvestigationResultResponse;
import com.hims.response.SampleDetailsForValidationResponse;
import com.hims.response.SampleHeaderForValidationResponse;
import com.hims.response.SampleRejectionInvestigationReportResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface DgSampleCollectionDetailsRepository extends JpaRepository<DgSampleCollectionDetails,Long>,JpaSpecificationExecutor<DgSampleCollectionDetails> {





    @Modifying
    @Transactional
    @Query("UPDATE DgSampleCollectionDetails d SET d.validated = :status WHERE d.sampleCollectionDetailsId = :id")
    void updateValidation(@Param("id") Long id, @Param("status") String status);



//    @Query("""
//    SELECT d FROM DgSampleCollectionDetails d
//    JOIN FETCH d.sampleCollectionHeader h
//    JOIN FETCH h.patientId p
//    LEFT JOIN FETCH h.subChargeCode sc
//    LEFT JOIN FETCH d.investigationId inv
//    LEFT JOIN FETCH inv.sampleId s
//    WHERE
//        (h.validated = 'n' AND d.validated = 'n')
//
//""")
//    List<DgSampleCollectionDetails> findAllByHeaderValidatedStatusLogic();

    @Query("""
    SELECT d 
    FROM DgSampleCollectionDetails d
    JOIN FETCH d.sampleCollectionHeader h
    JOIN FETCH h.patientId p
    LEFT JOIN FETCH h.subChargeCode sc
    LEFT JOIN FETCH d.investigationId inv
    LEFT JOIN FETCH inv.sampleId s
    WHERE h.validated = 'n' 
      AND d.validated = 'n'
    ORDER BY h.lastChgDate DESC
""")
    List<DgSampleCollectionDetails> findAllByHeaderValidatedStatusLogic();

    //    @Query("""
//        SELECT d FROM DgSampleCollectionDetails d
//        JOIN FETCH d.sampleCollectionHeader h
//        JOIN FETCH h.patientId p
//        LEFT JOIN FETCH h.subChargeCode sc
//        LEFT JOIN FETCH d.investigationId inv
//        LEFT JOIN FETCH inv.sampleId s
//        WHERE
//            (h.result_entry_status= 'n' AND d.result_status= 'n')
//            OR
//            (h.result_entry_status = 'p' AND d.result_status = 'y')
//    """)
//    List<DgSampleCollectionDetails> findAllByHeaderValidatedStatusLogic2();
//@Query("""
//    SELECT d FROM DgSampleCollectionDetails d
//    JOIN FETCH d.sampleCollectionHeader h
//    JOIN FETCH h.patientId p
//    LEFT JOIN FETCH h.subChargeCode sc
//    LEFT JOIN FETCH d.investigationId inv
//    LEFT JOIN FETCH inv.sampleId s
//    WHERE
//        h.result_entry_status = 'n'
//        AND h.validated = 'y'
//        AND d.validated = 'y'
//        AND d.result_status = 'n'
//""")
//List<DgSampleCollectionDetails> findAllByHeaderResultEntryAndValidationStatusLogic();

    @Query("""
    SELECT d FROM DgSampleCollectionDetails d
    JOIN FETCH d.sampleCollectionHeader h
    JOIN FETCH h.patientId p
    LEFT JOIN FETCH h.subChargeCode sc
    LEFT JOIN FETCH d.investigationId inv
    LEFT JOIN FETCH inv.sampleId s
    WHERE 
        h.result_entry_status = 'n'
        AND h.validated = 'y'
        AND d.validated = 'y'
        AND d.result_status = 'n'
    ORDER BY h.lastChgDate DESC
""")
    List<DgSampleCollectionDetails> findAllByHeaderResultEntryAndValidationStatusLogic();



List<DgSampleCollectionDetails> findBySampleCollectionHeader_SampleCollectionHeaderIdAndSampleCollectionHeader_SubChargeCode_SubId(Long sampleHeaderId, Long subChargeCodeId);



    @Query("SELECT d.validated FROM DgSampleCollectionDetails d WHERE d.sampleCollectionHeader.sampleCollectionHeaderId = :headerId")
    List<String> getValidationStatusOfHeader(Long headerId);


    //Create this sequence first
//    CREATE SEQUENCE sample_id_seq
//    START WITH 1
//    INCREMENT BY 1
//    NO CYCLE;
    @Query(value = "SELECT nextval('sample_id_seq')", nativeQuery = true)
    Long getNextSequenceValue();

    List<DgSampleCollectionDetails> findByInvestigationId_InvestigationIdAndSampleCollectionHeader_visitId_Id(Long investigationId,Long visitId);

    @Query("""
SELECT new com.hims.response.SampleDetailsForValidationResponse(
    d.sampleCollectionDetailsId,
    inv.investigationId,
    d.sampleGeneratedId,
    inv.investigationName,
    s.id,
    s.sampleDescription,
    inv.quantity,
    c.collectionId,
    c.collectionName,
    d.empanelledStatus,
    d.sampleCollDatetime,
    d.rejected_reason,
    d.remarks
)
FROM DgSampleCollectionDetails d
LEFT JOIN d.investigationId inv
LEFT JOIN d.sampleId s
LEFT JOIN inv.collectionId c
WHERE d.result_status IN (:resultStatuses) 
AND d.sampleCollectionHeader.sampleCollectionHeaderId = :headerId
""")
    List<SampleDetailsForValidationResponse> findDetailsByHeaderId(@Param("headerId")  Long headerId,  @Param("resultStatuses")List<String> resultStatuses);


    @Query("""
SELECT new com.hims.response.InvestigationResultResponse(
    d.sampleCollectionDetailsId,
    i.investigationId,
    i.investigationName,
    s.id,
    s.sampleDescription,
    u.name,
    CONCAT(i.minNormalValue,' - ',i.maxNormalValue),
    i.investigationType,
    d.sampleGeneratedId
)
FROM DgSampleCollectionDetails d
JOIN d.investigationId i
LEFT JOIN i.sampleId s
LEFT JOIN i.uomId u
WHERE d.sampleCollectionHeader.sampleCollectionHeaderId = :sampleCollectionHeaderId
AND d.validated='y'
AND d.result_status='n'
""")
    List<InvestigationResultResponse> getInvestigationsForResultEntry(@Param("sampleCollectionHeaderId") Long sampleCollectionHeaderId);


    @Query("""
SELECT new com.hims.response.SampleRejectionInvestigationReportResponse(
    oh.orderNo,
    oh.orderDate,
    TRIM(CONCAT(
        COALESCE(p.patientFn,''),' ',
        COALESCE(p.patientMn,''),' ',
        COALESCE(p.patientLn,'')
    )),
    p.patientAge,
    g.genderName,
    p.patientMobileNumber,
    inv.investigationName,
    scd.sampleGeneratedId,
    'Rejected',
    scd.rejected_reason,
    scc.subName
)
FROM DgSampleCollectionDetails scd
JOIN scd.sampleCollectionHeader sch
JOIN sch.visitId v
JOIN v.billingHd bh
JOIN bh.hdorder oh
JOIN sch.patientId p
LEFT JOIN p.patientGender g
LEFT JOIN scd.investigationId inv
LEFT JOIN sch.subChargeCode scc
WHERE oh.hospitalId = :hospitalId
AND scd.oldSampleCollectionHdIdForReject IS NOT NULL
AND oh.orderDate BETWEEN :fromDate AND :toDate
AND (:subChargeCodeId IS NULL OR scc.subId = :subChargeCodeId)
ORDER BY oh.orderDate DESC
""")
    Page<SampleRejectionInvestigationReportResponse> getRejectedInvestigations(
            Long hospitalId,
            Long subChargeCodeId,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    );


}
