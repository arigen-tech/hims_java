package com.hims.entity.repository;

import com.hims.entity.IpDischargeMedication;
import com.hims.projection.IpDischargeMedicationProjection;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jdk.jfr.Registered;
import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Registered
public interface IpDischargeMedicationRepository extends JpaRepository<IpDischargeMedication,Long> {
    @Modifying
    @Transactional
    @Query("""
    DELETE FROM IpDischargeMedication m
    WHERE m.dischargeMedicationId IN :medicationIds
      AND m.inpatient.inpatientId = :inpatientId
    """)
    void deleteSelectedMedications(@Param("medicationIds") List<Long> medicationIds,
                                   @Param("inpatientId") Long inpatientId);

    @Query("""
            SELECT
                m.dischargeMedicationId AS medicationId,
                m.medicineName AS medicineName,
                m.dosage AS dosage,
                m.frequency AS frequency,
                m.totalDoses AS totalDoses,
                m.route AS route,
                m.instruction AS instruction
            FROM IpDischargeMedication m
            WHERE m.dischargeSummary.dischargeId = :dischargeId
            """)
    List<IpDischargeMedicationProjection> getMedicationList(
            @Param("dischargeId") Long dischargeId);
}
