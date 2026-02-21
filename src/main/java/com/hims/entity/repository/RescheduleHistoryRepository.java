package com.hims.entity.repository;

import com.hims.entity.VisitRescheduleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RescheduleHistoryRepository extends JpaRepository<VisitRescheduleHistory,Long> {


}
