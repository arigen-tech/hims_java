package com.hims.entity.repository;

import com.hims.entity.MasOtScheduleChangeReason;
import com.hims.projection.OtScheduleChangeReasonProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface MasOtScheduleChangeReasonRepository extends JpaRepository<MasOtScheduleChangeReason, Long> {

    @Query(value = "SELECT reason_id AS reasonId, reason AS reason, applicable_for AS applicableFor FROM mas_ot_schedule_change_reason WHERE reason_id = :reasonId AND status = 'Y'", nativeQuery = true)
    java.util.Optional<OtScheduleChangeReasonProjection> findActiveReasonDetails(@Param("reasonId") Long reasonId);

    List<MasOtScheduleChangeReason> findByStatusIgnoreCaseOrderByApplicableForAscReasonAsc(String status);

    List<MasOtScheduleChangeReason> findAllByOrderByStatusDescLastChgDateDesc();
}
