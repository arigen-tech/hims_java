package com.hims.entity.repository;

import com.hims.entity.OtSurgeryDrug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OtSurgeryDrugRepository extends JpaRepository<OtSurgeryDrug, Long> {
    @Query("""
        SELECT d FROM OtSurgeryDrug d
        WHERE d.status = 'Y'
          AND (:otBookingId IS NULL OR d.otSurgeryExecution.otBooking.otBookingId = :otBookingId)
        ORDER BY d.otSurgeryDrugId DESC
        """)
    List<OtSurgeryDrug> findActive(@Param("otBookingId") Long otBookingId);

    @Query(value = """
        SELECT d FROM OtSurgeryDrug d
        WHERE d.status = 'Y'
          AND (:otBookingId IS NULL OR d.otSurgeryExecution.otBooking.otBookingId = :otBookingId)
        """,
        countQuery = """
        SELECT COUNT(d) FROM OtSurgeryDrug d
        WHERE d.status = 'Y'
          AND (:otBookingId IS NULL OR d.otSurgeryExecution.otBooking.otBookingId = :otBookingId)
        """)
    Page<OtSurgeryDrug> findActive(@Param("otBookingId") Long otBookingId, Pageable pageable);
}
