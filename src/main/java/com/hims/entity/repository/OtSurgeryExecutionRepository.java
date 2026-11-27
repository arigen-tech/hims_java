package com.hims.entity.repository;

import com.hims.entity.OtSurgeryExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtSurgeryExecutionRepository extends JpaRepository<OtSurgeryExecution, Long> {
    Optional<OtSurgeryExecution> findByOtBooking_OtBookingId(Long otBookingId);
}
