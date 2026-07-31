package com.hims.entity.repository;

import com.hims.entity.IpDiagnosisEntry;
import com.hims.projection.IpDiagnosisEntryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IpDiagnosisEntryRepository extends JpaRepository<IpDiagnosisEntry,Long> {
    @Query(value = """
    SELECT
        ide.inpatient_id AS inpatientId,
        ic.icd_id AS icdId,
        ic.icd_code AS icdCode,
        ic.icd_name AS icdName,
        ide.diagnosis_text AS remark,
        ide.diagnosis_type AS diagnosisType,
        ide.status AS status,
        ide.diagnosis_text AS diagnosis,
        ide.diagnosis_datetime AS dateTime
    FROM ip_diagnosis_entry ide
    LEFT JOIN mas_icd ic ON ic.icd_id = ide.icd_id
    WHERE ide.inpatient_id = :inpatientId
    ORDER BY ide.diagnosis_datetime DESC
    """, nativeQuery = true)
    List<IpDiagnosisEntryProjection> getIpDiagnosisEntry(@Param("inpatientId") Long inpatientId);
}
