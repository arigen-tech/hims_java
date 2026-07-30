package com.hims.entity.repository;

import com.hims.entity.IpDischargeMedication;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jdk.jfr.Registered;
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
}
