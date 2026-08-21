package com.hims.entity.repository;

import com.hims.entity.MasOtBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasOtBookingStatusRepository extends JpaRepository<MasOtBookingStatus, Long> {

    Optional<MasOtBookingStatus> findByStatusCode(String statusCode);
}
