package com.hims.entity.repository;

import com.hims.entity.DgSampleCollectionDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DgSampleCollectionDetailsRepository extends JpaRepository<DgSampleCollectionDetails,Long> {





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

    DgSampleCollectionDetails findBySampleCollectionDetailsIdAndInvestigationId_InvestigationId(Long sampleCollectionDetailsId,Long investigationId);

}
