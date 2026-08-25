package com.hims.entity.repository;

import com.hims.entity.ShiftHandover;
import com.hims.response.ShiftHandoverResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftHandoverRepository extends JpaRepository<ShiftHandover,Long> {
    @Query("""
        SELECT new com.hims.response.ShiftHandoverResponse(
             sh.id,
            sh.inpatient.inpatientId,
            sh.lastUpdateDate,
            sh.handoverNotes,
           sh.createdBy)
        FROM ShiftHandover sh
        WHERE sh.inpatient.inpatientId = :inpatientId
        ORDER BY sh.lastUpdateDate DESC
        """)

    List<ShiftHandoverResponse> getShiftHandover(@Param("inpatientId") Long inpatientId);
}
