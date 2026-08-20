package com.hims.entity.repository;

import com.hims.entity.OtDayAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtDayAllocationRepository
        extends JpaRepository<OtDayAllocation, Long> {

    List<OtDayAllocation> findByStatusIgnoreCaseOrderByDayOfWeekAsc(
            String status
    );

    List<OtDayAllocation> findAllByOrderByStatusDescLastChgDateDesc();
}