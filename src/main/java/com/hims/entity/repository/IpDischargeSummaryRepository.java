package com.hims.entity.repository;

import com.hims.entity.IpDischargeSummary;
import com.hims.projection.DischargeSummaryProjection;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IpDischargeSummaryRepository extends JpaRepository<IpDischargeSummary,Long> {
    Optional<IpDischargeSummary> findByInpatient_InpatientId(@NotNull(message = "Inpatient Id is required") Long inpatientId);

    @Query("""
            SELECT
                ds.dischargeId AS dischargeSummaryId,
                ds.inpatient.inpatientId AS inpatientId,
                ds.dischargeDate AS dischargeDate,
                ds.primaryDiagnosis AS primaryDiagnosis,
                ds.secondaryDiagnosis AS secondaryDiagnosis,
                ds.presentingComplaints AS presentingComplaints,
                ds.historyOfIllness AS historyOfIllness,
                ds.pastHistory AS pastHistory,
                ds.examinationFindings AS examinationFindings,
                ds.procedureDetails AS procedureDetails,
                ds.hospitalCourse AS hospitalCourse,
                ds.condition.conditionId AS conditionId,
                ds.condition.conditionName AS conditionName,
                ds.dischargeReason.dischargeReasonId AS dischargeReasonId,
                ds.dischargeReason.reasonName AS dischargeReasonName,
                ds.dischargedTo AS dischargedTo,
                ds.referredHospitalName AS referredHospitalName,
                ds.dischargeAdvice AS dischargeAdvice,
                ds.followUpAdvice AS followUpAdvice,
                ds.status AS status,
                bh.billStatus.billStatusId AS billStatusId,
                bh.billStatus.statusName AS billStatus,
                bh.paymentStatus.paymentStatusId AS paymentStatusId,
                bh.paymentStatus.statusName AS paymentStatus
            FROM IpDischargeSummary ds
            LEFT JOIN IpdBillingHeader bh
                ON bh.inpatientId.inpatientId = ds.inpatient.inpatientId
            WHERE ds.inpatient.inpatientId = :inpatientId
            """)
    Optional<DischargeSummaryProjection> getDischargeSummary(
            @Param("inpatientId") Long inpatientId);
}
