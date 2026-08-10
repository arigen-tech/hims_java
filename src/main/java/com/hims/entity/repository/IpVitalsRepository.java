package com.hims.entity.repository;

import com.hims.entity.IpVitals;
import com.hims.projection.IpVitalsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IpVitalsRepository extends JpaRepository<IpVitals,Long> {

    @Query(value = """
            SELECT
                iv.ip_vitals_id AS vitalId,
                iv.inpatient_id AS inpatientId,
                iv.observation_datetime AS observationDatetime,
                iv.temperature AS temperature,
                iv.pulse AS pulse,
                iv.bp_systolic AS bpSystolic,
                iv.bp_diastolic AS bpDiastolic,
                iv.respiration AS respiration,
                iv.spo2 AS spo2,
                iv.pain_score AS painScore
               
            FROM ip_vitals iv
            WHERE iv.inpatient_id = :inpatientId
            ORDER BY iv.observation_datetime DESC
            """, nativeQuery = true)
    List<IpVitalsProjection> findAllVitalsByInpatientId(
            @Param("inpatientId") Long inpatientId
    );
}
