package com.hims.entity.repository;

import com.hims.entity.IpDailyCaseSheetEntry;
import com.hims.projection.DailyCaseSheetEntryProjectionResponse;
import com.hims.response.DailyCaseSheetEntryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IpDailyCaseSheetEntryRepository extends JpaRepository<IpDailyCaseSheetEntry,Long> {
    @Query("""
        SELECT
            entry.caseSheetEntryId AS caseSheetEntryId,
            entry.inpatient.inpatientId AS inpatient,
            entry.doctorNotes AS notes,
            entry.investigationSummary AS investigation,
            entry.medicineSummary AS medicines,
            entry.procedureSummary AS procedure,
            entry.carePlanChanges AS plan,
            entry.nextFollowUpPlan AS followUp,
            entry.visitDatetime AS visitDateTime,
                     entry.doctor.userId AS doctorId,
                      entry.doctorName AS doctorName,
                      entry.visitDepartment.id AS departmentId,
                      entry.visitDepartment.departmentName AS departmentName
        FROM IpDailyCaseSheetEntry entry
        WHERE entry.inpatient.inpatientId = :inpatientId
        ORDER BY entry.visitDatetime DESC
        """)
    List<DailyCaseSheetEntryProjectionResponse> findDailyCaseSheetEntries(
            @Param("inpatientId") Long inpatientId
    );
}
