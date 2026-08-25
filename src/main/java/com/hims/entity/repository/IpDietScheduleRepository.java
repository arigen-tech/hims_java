package com.hims.entity.repository;

import com.hims.entity.IpDietSchedule;
import com.hims.response.CurrentActiveDietScheduleResponse;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IpDietScheduleRepository extends JpaRepository<IpDietSchedule,Long> {
    @Query("""
        SELECT new com.hims.response.CurrentActiveDietScheduleResponse(
            ids.inpatient.inpatientId,
            ids.dietScheduleId,
            ids.dietDate,
            ids.servingTime,
            ids.actualTime,
            ids.mealType.mealTypeId,
            ids.mealType.mealTypeName,
            ids.dietScheduleStatus.dietScheduleStatusId,
            ids.dietScheduleStatus.statusName,
            ids.consumedPercentage,
            ids.remarks,
            ids.administeredBy
        )
        FROM IpDietSchedule ids
        WHERE ids.inpatient.inpatientId = :inpatientId
          AND ids.dietOrder.dietOrderId = :dietOrderId
        ORDER BY ids.dietDate, ids.servingTime
        """)
    List<CurrentActiveDietScheduleResponse> findCurrentActiveDietSchedule(
            @Param("inpatientId") Long inpatientId,
            @Param("dietOrderId") Long dietOrderId
    );
}
