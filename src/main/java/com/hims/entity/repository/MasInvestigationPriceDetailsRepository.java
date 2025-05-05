package com.hims.entity.repository;

import com.hims.entity.DgMasInvestigation;
import com.hims.entity.MasInvestigationPriceDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MasInvestigationPriceDetailsRepository extends JpaRepository<MasInvestigationPriceDetails, Long> {

    List<MasInvestigationPriceDetails> findByInvestigation_investigationId(Long investigationId);

    List<MasInvestigationPriceDetails> findByStatus(String status);

    @Query("SELECT m FROM MasInvestigationPriceDetails m WHERE m.status = :status ORDER BY m.id ASC")
    List<MasInvestigationPriceDetails> getAllPriceDetailsByStatus(String status);

    List<MasInvestigationPriceDetails> findByStatusIgnoreCase(String status);

    // Find records with multiple status values
    List<MasInvestigationPriceDetails> findByStatusInIgnoreCase(List<String> statuses);



    List<MasInvestigationPriceDetails> findByInvestigation_InvestigationId(Long investigationId);

    // Add this method to your repository interface
    @Query("SELECT p FROM MasInvestigationPriceDetails p " +
            "WHERE p.investigation.investigationId = :investigationId " +
            "AND p.id != :excludeId " +
            "AND ((p.fromDate <= :toDate) AND (p.toDate >= :fromDate))")
    List<MasInvestigationPriceDetails> findOverlappingDateRanges(
            @Param("investigationId") Long investigationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("excludeId") Long excludeId);

}