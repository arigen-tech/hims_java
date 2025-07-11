package com.hims.entity.repository;

import com.hims.entity.DgMasInvestigation;
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
        COALESCE(ipd.price, 0),
        d.main_chargecode_id
    FROM 
        dg_mas_investigation d
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



    List<DgMasInvestigation> findByStatusIgnoreCase(String status);
    List<DgMasInvestigation> findByStatusInIgnoreCase(List<String> statuses);

    DgMasInvestigation findByinvestigationId(Long investigationId);

}