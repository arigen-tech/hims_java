package com.hims.entity.repository;

import com.hims.entity.LabTurnAroundTime;
import com.hims.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabTurnAroundTimeRepository  extends JpaRepository<LabTurnAroundTime,Long> {

    LabTurnAroundTime findByOrderHd_IdAndInvestigation_InvestigationIdAndPatient_Id(
            int orderHdId,
            Long investigationId,
            Long patientId
    );
}
