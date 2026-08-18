package com.hims.entity.repository;

import com.hims.entity.DgMasInvestigation;
import com.hims.entity.DgResultEntryDetail;
import com.hims.entity.DgResultEntryHeader;
import com.hims.entity.DgSubMasInvestigation;
import com.hims.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
  @Query("""
    select d
    from DgResultEntryDetail d
    where d.resultEntryId.resultEntryId in :resultEntryIds
      and lower(d.validated) = lower(:validated)
""")
  List<DgResultEntryDetail> findByResultEntryIdsAndValidated(@Param("resultEntryIds") List<Long> resultEntryIds,
                                                             @Param("validated") String validated);

    @Query("""
SELECT new com.hims.response.InvestigationsForResultValidation(

    d.resultEntryDetailId,
    i.investigationId,
    i.investigationName,

    u.name,
    s.sampleDescription,

    d.remarks,
    d.result,

    d.normalRange,
    d.generatedSampleId,
    i.investigationType

)

FROM DgResultEntryDetail d

LEFT JOIN d.investigationId i
LEFT JOIN d.uomId u
LEFT JOIN d.sampleId s

WHERE d.resultEntryId.resultEntryId = :resultEntryHeaderId
AND d.validated = :validationStatus
""")
    List<InvestigationsForResultValidation> getInvestigationsForResultValidationWrtHeader(
            @Param("resultEntryHeaderId") Long resultEntryHeaderId,
            @Param("validationStatus")  String validationStatus
    );

    @Query("""
SELECT new com.hims.response.SubInvestigationsForResultValidationResponse(

    sub.subInvestigationId,
    sub.subInvestigationName,

    d.normalRange,

    sub.comparisonType,
    u.name,
    d.resultType,
    f.fixedId,
    d.generatedSampleId,
    d.result,
    d.remarks

)

FROM DgResultEntryDetail d

LEFT JOIN d.subInvestigationId sub
LEFT JOIN sub.investigationId inv
LEFT JOIN d.uomId u
LEFT JOIN d.fixedId f

WHERE d.resultEntryDetailId = :resultEntryDetailId
AND inv.investigationId = :investigationId
AND d.validated = :validationStatus
AND sub IS NOT NULL
""")
    List<SubInvestigationsForResultValidationResponse> findSubInvestigationsByDetailId(
            @Param("resultEntryDetailId") Long resultEntryDetailId,
            @Param("investigationId") Long investigationId,
            @Param("validationStatus") String validationStatus
    );

    @Query("""
SELECT new com.hims.response.InvestigationsForResultUpdateResponse(

    d.resultEntryId.resultEntryId,
    d.resultEntryDetailId,
    i.investigationId,
    i.investigationName,

    u.name,
    s.sampleDescription,

    d.remarks,
    d.result,

    d.normalRange,
    d.generatedSampleId,
    i.investigationType

)

FROM DgResultEntryDetail d

LEFT JOIN d.investigationId i
LEFT JOIN d.uomId u
LEFT JOIN d.sampleId s

WHERE d.resultEntryId.orderHd.id = :orderHdId
AND d.validated = :validationStatus
""")
    List<InvestigationsForResultUpdateResponse> getInvestigationsForResultUpdateWrtOrderHd(
            @Param("orderHdId") Long orderHdId,
            @Param("validationStatus")  String validationStatus
    );

    @Query("""
SELECT new com.hims.response.LabInvestigationsReportResponse(
    h.resultEntryId,
    d.resultEntryDetailId,
    oh.id,
    COALESCE(inv.investigationName, ''),
    CONCAT(
        COALESCE(p.patientFn, ''), ' ',
        COALESCE(p.patientMn, ''), ' ',
        COALESCE(p.patientLn, '')
    ),
    p.patientMobileNumber,
    g.genderName,
    p.patientAge,
    uom.name,
    d.result,
    d.normalRange,
    h.resultEnteredBy,
    CONCAT(
        COALESCE(u.firstName, ''), ' ',
        COALESCE(u.middleName, ''), ' ',
        COALESCE(u.lastName, '')
    ),
    h.resultDate
)
FROM DgResultEntryDetail d
JOIN d.resultEntryId h
LEFT JOIN h.hinId p
LEFT JOIN p.patientGender g
LEFT JOIN d.investigationId inv
LEFT JOIN d.uomId uom
LEFT JOIN h.orderHd oh
LEFT JOIN User u ON u.userId = h.resultVerifiedBy
WHERE h.hospitalId.id= :hospitalId
AND (:patientId IS NULL OR p.id = :patientId)
AND LOWER(d.validated) = LOWER(:resultValidationStatus)
            AND h.resultDate >= COALESCE(:fromDate, h.resultDate)
            AND h.resultDate <= COALESCE(:toDate, h.resultDate)

AND ( :mobileNo IS NULL OR p.patientMobileNumber LIKE :mobileNo )

AND ( :patientName IS NULL OR LOWER(CONCAT(
        COALESCE(p.patientFn, ''), ' ',
        COALESCE(p.patientMn, ''), ' ',
        COALESCE(p.patientLn, '')
    )) LIKE :patientName )
""")
    Page<LabInvestigationsReportResponse> getLabInvestigationsReport(
            @Param("hospitalId") Long hospitalId,
            @Param("mobileNo") String mobileNo,
            @Param("patientName") String patientName,
            @Param("patientId") Long patientId,

            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("resultValidationStatus") String resultValidationStatus,
            Pageable pageable
    );
       @Query("""
        SELECT new com.hims.response.LabInvestigationsReportResponse(
            h.resultEntryId,
            d.resultEntryDetailId,
            h.orderHd.id,
            i.investigationName,

             CONCAT(
        COALESCE(p.patientFn, ''), ' ',
        COALESCE(p.patientMn, ''), ' ',
        COALESCE(p.patientLn, '')
    ),

            p.patientMobileNumber,
            g.genderName,
            p.patientAge,
            u.name,
            d.result,
            d.normalRange,
            h.resultEnteredBy,
            CAST(h.resultVerifiedBy AS string),
            h.resultDate
        )
        FROM DgResultEntryDetail d

        JOIN d.resultEntryId h
        
        LEFT JOIN h.orderHd oh

        LEFT JOIN d.investigationId i

        LEFT JOIN d.uomId u

        LEFT JOIN h.hinId p

        LEFT JOIN p.patientGender g

        WHERE h.hospitalId.id = :hospitalId

          AND (
                :departmentId IS NULL
                OR oh.departmentId = :departmentId
          )

          AND ( :patientName IS NULL OR LOWER(CONCAT(
        COALESCE(p.patientFn, ''), ' ',
        COALESCE(p.patientMn, ''), ' ',
        COALESCE(p.patientLn, '')
    )) LIKE :patientName )

          AND ( :patientMobileNo IS NULL OR p.patientMobileNumber LIKE :patientMobileNo )

          AND (
                :fromDate IS NULL
                OR h.resultDate >= :fromDate
          )

          AND (
                :toDate IS NULL
                OR h.resultDate <= :toDate
          )

          AND d.result IS NOT NULL

        """)
        Page<LabInvestigationsReportResponse> getOutOfRangeInvestigationResults(
                @Param("hospitalId") Long hospitalId,
                @Param("departmentId") Long departmentId,
                @Param("patientName") String patientName,
                @Param("patientMobileNo") String patientMobileNo,
                @Param("fromDate") LocalDate fromDate,
                @Param("toDate") LocalDate toDate,
                Pageable pageable
        );

}
