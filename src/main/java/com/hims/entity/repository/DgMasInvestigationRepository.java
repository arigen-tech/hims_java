package com.hims.entity.repository;

import com.hims.entity.DgMasInvestigation;
import com.hims.response.DgMasInvestigationResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DgMasInvestigationRepository extends JpaRepository<DgMasInvestigation,Long> {




@Query("SELECT new com.hims.response.DgMasInvestigationResponse( " +
              "d.investigationName, d.status, d.genderApplicable, ipd.price) " +
              "FROM DgMasInvestigation d " +
              "JOIN d.priceDetails ipd " +
              "WHERE d.investigationName = :investigationName " +
              "AND d.status = 'y' " +
              "AND d.genderApplicable = :genderApplicable " +
              "AND :currentDate BETWEEN ipd.fromDate AND ipd.toDate")
//@Query("SELECT new .DgMasInvestigationResponse(" +
//        "d.investigationName, d.status, d.investigationId, d.genderApplicable, ipd.price) " +
//        "FROM DgMasInvestigation d " +
//        "LEFT JOIN InvestigationPriceDetails ipd " +
//        "ON d.investigationId = ipd.investigationId " +
//        "AND CURRENT_DATE BETWEEN ipd.fromDt AND ipd.toDt " +
//        "WHERE d.status = 'y' " +
//        "AND d.genderApplicable = 'c' " +
//        "AND d.investigationName LIKE %:searchTerm%")
    List<DgMasInvestigationResponse> findByPriceDetails(@Param("investigationName") String investigationName,
                                                        @Param("genderApplicable") String genderApplicable,
                                                        @Param("currentDate") LocalDate currentDate);
}