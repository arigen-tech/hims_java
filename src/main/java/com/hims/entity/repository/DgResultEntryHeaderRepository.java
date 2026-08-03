package com.hims.entity.repository;

import com.hims.entity.DgResultEntryHeader;
import com.hims.projection.ResultEntryHeaderForResultValidation;
import com.hims.response.SampleHeaderForResultValidationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DgResultEntryHeaderRepository extends JpaRepository<DgResultEntryHeader,Long> {

Optional<DgResultEntryHeader> findBySampleCollectionHeaderId_SampleCollectionHeaderIdAndSubChargeCodeId_SubId(Long sampleCollectionHeaderId, Long subChargeCodeId);

//
//    @Query("SELECT DISTINCT h FROM DgResultEntryHeader h " +
//            "JOIN DgResultEntryDetail d ON d.resultEntryId = h " +
//            "WHERE h.resultStatus = 'n' AND d.validated = 'n'")
//    List<DgResultEntryHeader> findAllUnvalidatedHeaders();

    @Query("""
    SELECT DISTINCT h 
    FROM DgResultEntryHeader h
    JOIN DgResultEntryDetail d ON d.resultEntryId = h
    WHERE h.resultStatus = 'n' 
      AND d.validated = 'n'
    ORDER BY h.lastChgdDate DESC, h.lastChgdTime DESC
""")
    List<DgResultEntryHeader> findAllUnvalidatedHeaders();


    List<DgResultEntryHeader> findAllByOrderByLastChgdDateDescLastChgdTimeDesc();

    Optional<DgResultEntryHeader> findByOrderHd_Id(Long aLong);

    Optional<DgResultEntryHeader> findByOrderHd_IdAndHospitalId_Id(long id, Long hospitalId);

    @Query("""
    select h.resultEntryId
    from DgResultEntryHeader h
    where h.orderHd.id in :orderHdIds
      and h.hospitalId.id = :hospitalId
""")
    List<Long> findResultEntryIdsByOrderHdIds(@Param("orderHdIds") List<Long> orderHdIds,
                                              @Param("hospitalId") Long hospitalId);


    @Query("""
SELECT new com.hims.response.SampleHeaderForResultValidationResponse(

    h.resultEntryId,
    h.resultTime,
    h.resultDate,

    
    CONCAT(
        COALESCE(p.patientFn, ''), ' ',
        COALESCE(p.patientMn, ''), ' ',
        COALESCE(p.patientLn, '')
    ),
    g.genderName,
    p.patientAge,
    p.patientMobileNumber,
    r.relationName,
    d.departmentName,
    CONCAT(
        COALESCE(doc.firstName, ''), ' ',
        COALESCE(doc.middleName, ''), ' ',
        COALESCE(doc.lastName, '')
    ),

    sc.subId,
    sc.subName,

    h.resultEnteredBy,
    oh.id,
    oh.orderNo
)

FROM DgResultEntryHeader h


LEFT JOIN h.hinId p
LEFT JOIN h.orderHd  oh
LEFT JOIN p.patientGender g
LEFT JOIN h.relationId r
LEFT JOIN h.subChargeCodeId sc
LEFT JOIN h.mainChargecodeId mc
LEFT JOIN h.sampleCollectionHeaderId sch
LEFT JOIN MasDepartment d ON d.id=oh.departmentId
LEFT JOIN sch.visitId v
LEFT JOIN v.doctor doc

WHERE h.hospitalId.id=:hospitalId
AND h.resultStatus = 'n'
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
    Page<SampleHeaderForResultValidationResponse> getSampleHeaderForResultValidation(
            @Param("hospitalId") Long hospitalId,
            @Param("patientName") String patientName,
            @Param("patientMobileNumber") String patientMobileNumber,
            Pageable pageable
    );


    @Query(value = """
    SELECT DISTINCT ON (oh.orderhd_id)
        dreh.result_entry_id AS resultEntryId,

        CONCAT(
            COALESCE(p.p_fn, ''), ' ',
            COALESCE(p.p_mn, ''), ' ',
            COALESCE(p.p_ln, '')
        ) AS patientName,

        g.gender_name AS genderName,
        p.p_age AS patientAge,
        p.p_mobile_number AS patientMobileNumber,
        r.relation_name AS relationName,

        CONCAT(
            COALESCE(doc.first_name, ''), ' ',
            COALESCE(doc.middle_name, ''), ' ',
            COALESCE(doc.last_name, '')
        ) AS doctorName,

        oh.orderhd_id AS orderhdId,
        oh.order_no AS orderNo,
        oh.order_date AS orderDate,
        oh.order_time AS orderTime

    FROM dg_result_entry_header dreh
    JOIN patient p ON p.patient_id = dreh.hin_id
    LEFT JOIN mas_gender g ON g.id = p.p_gender_id
    JOIN dg_orderhd oh ON oh.orderhd_id = dreh.order_hd_id
    LEFT JOIN mas_relation r ON r.relation_id = dreh.relation_id
    LEFT JOIN dg_sample_collection_header sch ON sch.sample_collection_header_id = dreh.sample_collection_header_id
    LEFT JOIN visit v ON v.visit_id = sch.visit_id
    LEFT JOIN users doc ON doc.user_id = v.doctor_id

    WHERE dreh.hospital_id = :hospitalId
      AND dreh.result_status IN ('n','y')

      AND (
        CAST(:patientName AS text) IS NULL OR
        (COALESCE(p.p_fn, '') || ' ' || COALESCE(p.p_mn, '') || ' ' || COALESCE(p.p_ln, ''))
        ILIKE '%' || CAST(:patientName AS text) || '%'
      )

      AND (
        CAST(:patientMobileNo AS text) IS NULL OR
        p.p_mobile_number LIKE '%' || CAST(:patientMobileNo AS text) || '%'
      )

    ORDER BY oh.orderhd_id DESC
    """,

            countQuery = """
        SELECT COUNT(DISTINCT dreh.order_hd_id)
        FROM dg_result_entry_header dreh
        JOIN patient p ON p.patient_id = dreh.hin_id
        JOIN dg_orderhd oh ON oh.orderhd_id = dreh.order_hd_id

        WHERE dreh.hospital_id = :hospitalId
          AND dreh.result_status IN ('n','y')

          AND (
            CAST(:patientName AS text) IS NULL OR
            (COALESCE(p.p_fn, '') || ' ' || COALESCE(p.p_mn, '') || ' ' || COALESCE(p.p_ln, ''))
            ILIKE '%' || CAST(:patientName AS text) || '%'
          )

          AND (
            CAST(:patientMobileNo AS text) IS NULL OR
            p.p_mobile_number LIKE '%' || CAST(:patientMobileNo AS text) || '%'
          )
    """,

            nativeQuery = true
    )
    Page<ResultEntryHeaderForResultValidation> getResultHeaderForUpdate(
            @Param("hospitalId") Long hospitalId,
            @Param("patientName") String patientName,
            @Param("patientMobileNo") String patientMobileNo,
            Pageable pageable
    );


}
