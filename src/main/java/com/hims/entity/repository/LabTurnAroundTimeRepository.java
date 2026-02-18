package com.hims.entity.repository;

import com.hims.entity.LabTurnAroundTime;
import com.hims.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LabTurnAroundTimeRepository  extends JpaRepository<LabTurnAroundTime,Long>, JpaSpecificationExecutor<LabTurnAroundTime> {

    LabTurnAroundTime findByOrderHd_IdAndInvestigation_InvestigationIdAndPatient_IdAndIsReject(int id, Long investigationId, Long id1, Boolean s );

    LabTurnAroundTime findByOrderHd_IdAndInvestigation_InvestigationIdAndPatient_IdAndGeneratedSampleId(int orderHdId, Long investigationId, Long id, String sampleGeneratedId);
}
