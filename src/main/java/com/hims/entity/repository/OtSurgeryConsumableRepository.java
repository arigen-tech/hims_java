package com.hims.entity.repository;

import com.hims.entity.OtSurgeryConsumable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OtSurgeryConsumableRepository extends JpaRepository<OtSurgeryConsumable, Long> {
    @Query("""
        SELECT c FROM OtSurgeryConsumable c
        WHERE c.status = 'Y'
          AND (:otBookingId IS NULL OR c.otSurgeryExecution.otBooking.otBookingId = :otBookingId)
        ORDER BY c.otSurgeryConsumableId DESC
        """)
    List<OtSurgeryConsumable> findActive(@Param("otBookingId") Long otBookingId);

    @Query(value = """
        SELECT c FROM OtSurgeryConsumable c
        WHERE c.status = 'Y'
          AND (:otBookingId IS NULL OR c.otSurgeryExecution.otBooking.otBookingId = :otBookingId)
        """,
        countQuery = """
        SELECT COUNT(c) FROM OtSurgeryConsumable c
        WHERE c.status = 'Y'
          AND (:otBookingId IS NULL OR c.otSurgeryExecution.otBooking.otBookingId = :otBookingId)
        """)
    Page<OtSurgeryConsumable> findActive(@Param("otBookingId") Long otBookingId, Pageable pageable);
}
