package com.hims.entity.repository;

import com.hims.entity.OtBookingStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtBookingStatusLogRepository extends JpaRepository<OtBookingStatusLog,Long> {
}
