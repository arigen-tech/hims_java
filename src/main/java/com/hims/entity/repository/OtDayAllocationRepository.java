package com.hims.entity.repository;

import com.hims.entity.OtDayAllocation;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface OtDayAllocationRepository extends JpaRepository<OtDayAllocation, Long> {

    @Query("""
     SELECT o FROM OtDayAllocation o
     WHERE o.operationTheatre.otId = :otId
     AND o.department.id = :departmentId
     AND LOWER(o.dayOfWeek) = LOWER(:dayOfWeek)
     AND LOWER(o.status) = LOWER(:status)
 """)
    Optional<OtDayAllocation> findByOtAndDepartmentAndDay(
            @Param("otId") Long otId,
            @Param("departmentId") Long departmentId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("status") String status);

    List<OtDayAllocation> findByStatusIgnoreCaseOrderByDayOfWeekAsc(
            String status
    );

    List<OtDayAllocation> findAllByOrderByStatusDescLastChgDateDesc();
}