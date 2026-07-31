package com.hims.entity.repository;

import com.hims.entity.DgMasInvestigation;
import com.hims.entity.MasInvestigationPriceDetails;
import com.hims.projection.MasInvestigationPriceDetailsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MasInvestigationPriceDetailsRepository extends JpaRepository<MasInvestigationPriceDetails, Long> {

    List<MasInvestigationPriceDetails> findByInvestigation_investigationId(Long investigationId);

    List<MasInvestigationPriceDetails> findByStatus(String status);

    @Query("SELECT m FROM MasInvestigationPriceDetails m WHERE m.status = :status ORDER BY m.id ASC")
    List<MasInvestigationPriceDetails> getAllPriceDetailsByStatus(String status);

    List<MasInvestigationPriceDetails> findByStatusIgnoreCase(String priceStatus);

    // Find records with multiple status values
    List<MasInvestigationPriceDetails> findByStatusInIgnoreCaseAndInvestigation_StatusIgnoreCase(List<String> statuses,String invStatus);


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

    @Query("SELECT p FROM MasInvestigationPriceDetails p WHERE p.investigation = :investigation AND :date BETWEEN p.fromDate AND COALESCE(p.toDate, :date)")
    Optional<MasInvestigationPriceDetails> findActivePriceByInvestigationAndDate(
            @Param("investigation") DgMasInvestigation investigation,
            @Param("date") LocalDate date);

    // Simple method for latest price
    Optional<MasInvestigationPriceDetails> findTopByInvestigationOrderByFromDateDesc(DgMasInvestigation investigation);

    @Query("""
    SELECT
        m.id AS id,
        i.id AS investigationId,
        i.investigationName AS investigationName,
        m.fromDate AS fromDt,
        m.toDate AS toDt,
        m.price AS price,
        m.status AS status
    FROM MasInvestigationPriceDetails m
    LEFT JOIN m.investigation i
    WHERE LOWER(m.status) = LOWER(:status)
      AND (:investigationName IS NULL OR LOWER(i.investigationName) LIKE :investigationName)
""")
    Page<MasInvestigationPriceDetailsProjection> getAllPriceDetails(
            @Param("status") String status,
            @Param("investigationName") String investigationName,
            Pageable pageable
    );

    @Query("""
    SELECT
        m.id AS id,
        i.id AS investigationId,
        i.investigationName AS investigationName,
        m.fromDate AS fromDt,
        m.toDate AS toDt,
        m.price AS price,
        m.status AS status
    FROM MasInvestigationPriceDetails m
    LEFT JOIN m.investigation i
    WHERE LOWER(m.status) IN (:statuses)
      AND LOWER(i.status) = LOWER(:investigationStatus)
      AND (:investigationName IS NULL OR LOWER(i.investigationName) LIKE :investigationName)
""")
    Page<MasInvestigationPriceDetailsProjection> getAllPriceDetail(
            @Param("statuses") List<String> statuses,
            @Param("investigationStatus") String investigationStatus,
            @Param("investigationName") String investigationName,
            Pageable pageable
    );

}
