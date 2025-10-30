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
    @Query("UPDATE DgSampleCollectionDetails d SET d.validated = :validated WHERE d.sampleCollectionDetailsId = :detailId")
    int updateValidationStatus(@Param("detailId") Long detailId, @Param("validated") String validated);

    @Query("SELECT d.sampleCollectionHeader.sampleCollectionHeaderId FROM DgSampleCollectionDetails d WHERE d.sampleCollectionDetailsId IN :detailIds")
    Set<Long> findHeaderIdsByDetailIds(@Param("detailIds") List<Long> detailIds);

    @Query("SELECT COUNT(d) FROM DgSampleCollectionDetails d WHERE d.sampleCollectionHeader.sampleCollectionHeaderId = :headerId AND d.validated = 'y'")
    long countAcceptedByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COUNT(d) FROM DgSampleCollectionDetails d WHERE d.sampleCollectionHeader.sampleCollectionHeaderId = :headerId")
    long countTotalByHeaderId(@Param("headerId") Long headerId);

    @Query("""
    SELECT d FROM DgSampleCollectionDetails d
    JOIN FETCH d.sampleCollectionHeader h
    JOIN FETCH h.patientId p
    LEFT JOIN FETCH h.subChargeCode sc
    LEFT JOIN FETCH d.investigationId inv
    LEFT JOIN FETCH inv.sampleId s
    WHERE 
        (h.validated = 'n' AND d.validated = 'n')
       
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
""")
List<DgSampleCollectionDetails> findAllByHeaderResultEntryAndValidationStatusLogic();



    List<DgSampleCollectionDetails> findBySampleCollectionHeader_SampleCollectionHeaderIdAndSampleCollectionHeader_SubChargeCode_SubId(Long sampleHeaderId, Long subChargeCodeId);

  //  List<DgSampleCollectionDetails> findBySampleCollectionHeader_SampleCollectionHeaderIdAndSubChargeCode_SubId(Long sampleHeaderId, Long subChargeCodeId);
}
