package com.hims.entity.repository;

import com.hims.entity.BloodCrossmatchFailedHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BloodCrossmatchFailedHistoryRepository
        extends JpaRepository<BloodCrossmatchFailedHistory, Long> {

    @Query("""
        SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END
        FROM BloodCrossmatchFailedHistory h
        LEFT JOIN h.inventory i
        LEFT JOIN h.inpatient ip
        LEFT JOIN ip.patient p
        WHERE p.id = :patientId
          AND i.inventoryId = :inventoryId
    """)
    Boolean existsByPatientIdAndInventoryId(
            @Param("patientId") Long patientId,
            @Param("inventoryId") Long inventoryId
    );
}
