package com.hims.entity.repository;

import com.hims.entity.OpdToothPatientCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OpdToothPatientConditionRepository extends JpaRepository<OpdToothPatientCondition, Long> {

    @Modifying
    @Query("""
        DELETE FROM OpdToothPatientCondition t
        WHERE t.patientId = :patientId
        AND t.visitId = :visitId
    """)
    void deleteByPatientIdAndVisitId(
            @Param("patientId") Long patientId,
            @Param("visitId") Long visitId
    );
}