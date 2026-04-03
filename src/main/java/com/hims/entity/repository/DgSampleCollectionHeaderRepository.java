package com.hims.entity.repository;

import com.hims.entity.DgSampleCollectionHeader;
import com.hims.projection.SampleHeaderForValidationProjection;
import com.hims.response.SampleHeaderForResultEntryResponse;
import com.hims.response.SampleHeaderForValidationResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DgSampleCollectionHeaderRepository extends JpaRepository<DgSampleCollectionHeader,Long> {

    @Query("SELECT h FROM DgSampleCollectionHeader h   WHERE h.visitId.id = :visitId   AND h.subChargeCode.id = :subChargeCodeId   AND h. validated = 'n'")
    Optional<DgSampleCollectionHeader> findByVisitIdAndSubChargeCodeAndValidateStatusN(
            Long visitId, Long subChargeCodeId);
 //   Optional<DgSampleCollectionHeader> findByVisitIdAndSubChargeCodeAndValidateStatusN(long visitId, Long aLong);
//    @Modifying
//    @Transactional
//    @Query("UPDATE DgSampleCollectionHeader h SET h.validated = :status WHERE h.sampleCollectionHeaderId = :headerId")
//    int updateOrderStatus(@Param("headerId") Long headerId, @Param("status") String status);
//
//    @Modifying
//    @Transactional
//    @Query("UPDATE DgSampleCollectionHeader h SET h.sampleOrderStatus = :status WHERE h.sampleCollectionHeaderId = :hdId")
//    void updateCollectionStatus(@Param("hdId") Long hdId, @Param("status") String status);

    @Query("""
SELECT new com.hims.response.SampleHeaderForValidationResponse(
    h.sampleCollectionHeaderId,
    h.collection_time,
    oh.orderNo,
    oh.orderDate,
    CONCAT(
        COALESCE(p.patientFn, ''), ' ',
        COALESCE(p.patientMn, ''), ' ',
        COALESCE(p.patientLn, '')
    ),
    p.patientMobileNumber,
    g.genderName,
    p.patientAge,
    sc.subId,
    sc.subName,
    v.doctorName,
    r.relationName,
    h.collection_by
)
FROM DgSampleCollectionHeader h
LEFT JOIN h.patientId p
LEFT JOIN p.patientGender g
LEFT JOIN p.patientRelation r
LEFT JOIN h.subChargeCode sc
LEFT JOIN h.visitId v
LEFT JOIN DgOrderHd oh
    ON oh.patientId.id = p.id
    AND oh.visitId.id = v.id
WHERE h.hospitalId.id=:hospitalId
AND LOWER(h.validated) = LOWER(:validationStatus)

AND (
    :patientName IS NULL OR
    LOWER(CONCAT(
        COALESCE(p.patientFn, ''), ' ',
        COALESCE(p.patientMn, ''), ' ',
        COALESCE(p.patientLn, '')
    )) LIKE :patientName
)

AND (
    :patientMobileNumber IS NULL OR p.patientMobileNumber = :patientMobileNumber
)
""")
    Page<SampleHeaderForValidationResponse> findSampleHeadersForValidation(
            @Param("hospitalId") Long hospitalId,
            @Param("validationStatus") String validationStatus,
            @Param("patientName") String patientName,
            @Param("patientMobileNumber") String patientMobileNumber,
            Pageable pageable
    );

    @Query("""
SELECT new com.hims.response.SampleHeaderForResultEntryResponse(
    h.sampleCollectionHeaderId,
    p.id,
    oh.visitId.id,
    CONCAT(
        COALESCE(p.patientFn, ''), ' ',
        COALESCE(p.patientMn, ''), ' ',
        COALESCE(p.patientLn, '')
    ),
    p.patientMobileNumber,
    rel.id,
    rel.relationName,
    g.id,
    g.genderName,
    p.patientAge,

    oh.orderDate,
    oh.orderTime,

    h.collection_time,
    oh.orderNo,

    h.collection_time,
    h.collection_by,
    h.validation_date,
    h.validationTime,
    h.validatedBy,
    
    d.departmentName,
    hosp.hospitalName,

    mc.chargecodeId,
    mc.chargecodeName,
    sc.subId,
    sc.subName
)

FROM DgSampleCollectionHeader h

LEFT JOIN h.patientId p
LEFT JOIN p.patientRelation rel
LEFT JOIN p.patientGender g

LEFT JOIN h.departmentId d
LEFT JOIN h.hospitalId hosp

LEFT JOIN h.subChargeCode sc
LEFT JOIN sc.mainChargeId mc

LEFT JOIN DgOrderHd oh ON oh.visitId = h.visitId
WHERE h.hospitalId.id=:hospitalId
AND h.result_entry_status = :resultEntryStatus
AND h.validated = :validationStatus

AND (
    :patientName IS NULL OR
    LOWER(CONCAT(
        COALESCE(p.patientFn, ''), ' ',
        COALESCE(p.patientMn, ''), ' ',
        COALESCE(p.patientLn, '')
    )) LIKE :patientName
)

AND (
    :patientMobileNumber IS NULL OR p.patientMobileNumber = :patientMobileNumber
)
""")
    Page<SampleHeaderForResultEntryResponse> findHeadersForResultEntry(
            @Param("hospitalId") Long hospitalId,
            @Param("resultEntryStatus") String resultEntryStatus,
            @Param("validationStatus") String validationStatus,
            @Param("patientName") String patientName,
            @Param("patientMobileNumber") String patientMobileNumber,
            Pageable pageable
    );

}
