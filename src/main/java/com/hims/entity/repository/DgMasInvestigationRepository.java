package com.hims.entity.repository;

import com.hims.entity.DgMasInvestigation;
import com.hims.projection.InvestigationProjection;
import com.hims.response.MasInvestigationByMainChargeCodeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DgMasInvestigationRepository extends JpaRepository<DgMasInvestigation,Long> {


    @Query(value = """
    SELECT 
        d.investigation_id,
        d.investigation_name,
        d.status,
        d.gender_applicable,
        d.discount_applicable,
        d.discount,
        COALESCE(ipd.price, 0),
        d.main_chargecode_id,
        mmc.main_chargecode_name
    FROM 
        dg_mas_investigation d
    LEFT JOIN
        mas_main_chargecode mmc
        ON d.main_chargecode_id = mmc.main_chargecode_id
    LEFT JOIN
        investigation_price_details ipd
        ON d.investigation_id = ipd.investigation_id
        AND CURRENT_DATE BETWEEN ipd.from_dt AND ipd.to_dt
    WHERE 
        d.status = 'y'
        AND (d.gender_applicable = :genderApplicable 
        OR d.gender_applicable = 'c')
        AND d.main_chargecode_id = :mainChargecodeId
""", nativeQuery = true)
    List<Object[]> findByPriceDetails(
            @Param("genderApplicable") String genderApplicable,
            @Param("mainChargecodeId") Long mainChargecodeId
    );



    List<DgMasInvestigation> findByStatusIgnoreCaseOrderByLastChgDateDesc(String status);
    List<DgMasInvestigation> findByStatusInIgnoreCaseOrderByLastChgDateDesc(List<String> statuses);

    DgMasInvestigation findByinvestigationId(Long investigationId);

    Page<DgMasInvestigation> findByStatusIgnoreCase(String status, Pageable pageable);

    Page<DgMasInvestigation> findByStatusInIgnoreCase(List<String> status, Pageable pageable);

    @Query("""
    SELECT m FROM DgMasInvestigation m
    WHERE
    (
      (:flag = 0 AND LOWER(m.status) IN ('y','n'))
      OR
      (:flag = 1 AND LOWER(m.status) = 'y')
    )
    AND (:mainChargeCodeId IS NULL
         OR m.mainChargeCodeId.chargecodeId = :mainChargeCodeId)
    AND (
        LOWER(m.investigationName) LIKE %:search%
        OR LOWER(m.mainChargeCodeId.chargecodeName) LIKE %:search%
    )
    """)
    Page<DgMasInvestigation> searchInvestigations(
            @Param("flag") int flag,
            @Param("search") String search,
            @Param("mainChargeCodeId") Long mainChargeCodeId,
            Pageable pageable
    );



    @Query("""
    SELECT m FROM DgMasInvestigation m
    WHERE
    (
      (:flag = 0 AND LOWER(m.status) IN ('y','n'))
      OR
      (:flag = 1 AND LOWER(m.status) = 'y')
    )
    AND (:mainChargeCodeId IS NULL
         OR m.mainChargeCodeId.chargecodeId = :mainChargeCodeId)
    """)
    Page<DgMasInvestigation> findAllWithFilter(
            @Param("flag") int flag,
            @Param("mainChargeCodeId") Long mainChargeCodeId,
            Pageable pageable
    );


    @Query("""
    SELECT DISTINCT 
        m.chargecodeId AS id,
        m.chargecodeName AS name
    FROM DgMasInvestigation d
    JOIN d.mainChargeCodeId m
    WHERE LOWER(m.status) = 'y'
    """)
    List<InvestigationTypeProjection> findUniqueInvestigationTypes();

    @Query("""
    SELECT new com.hims.response.MasInvestigationByMainChargeCodeResponse(
        m.investigationId,
        m.investigationName,
        m.mainChargeCodeId.chargecodeId
    )
    FROM DgMasInvestigation m
    WHERE LOWER(m.status) = :status
    AND (:mainChargeCodeId IS NULL 
         OR m.mainChargeCodeId.chargecodeId = :mainChargeCodeId)
""")
    List<MasInvestigationByMainChargeCodeResponse>
    dgMasInvestigationByMainChargeCodeId(@Param("mainChargeCodeId") Long mainChargeCodeId,
                                         @Param("status") String status              );

    List<DgMasInvestigation> findByMainChargeCodeIdChargecodeIdAndStatusInIgnoreCaseOrderByLastChgDateDesc(Long mainChargeCodeId, List<String> y);

    @Query(value = """
    SELECT 
        inv.investigation_id AS investigationId,
        inv.investigation_name AS investigationName
    FROM dg_mas_investigation inv
    WHERE inv.main_chargecode_id =:mainChargeCodeId AND inv.investigation_id <> :investigationId
    AND (
        :search IS NULL OR :search = '' OR
        LOWER(inv.investigation_name) LIKE LOWER(CONCAT('%', :search, '%'))
    )
    ORDER BY inv.investigation_name ASC
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM dg_mas_investigation inv
    WHERE  inv.main_chargecode_id =:mainChargeCodeId AND inv.investigation_id <> :investigationId
    AND (
        :search IS NULL OR :search = '' OR
        LOWER(inv.investigation_name) LIKE LOWER(CONCAT('%', :search, '%'))
    )
    """,
            nativeQuery = true)
    Page<InvestigationProjection> getDgMasInvestigation(
            @Param("mainChargeCodeId") Long mainChargeCodeId,
            @Param("investigationId") Long investigationId,
            @Param("search") String search,
            Pageable pageable
    );
}
