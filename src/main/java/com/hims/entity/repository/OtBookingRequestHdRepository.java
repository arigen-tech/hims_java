package com.hims.entity.repository;

import com.hims.entity.OtBookingRequestHd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtBookingRequestHdRepository extends JpaRepository<OtBookingRequestHd, Long> {

    Optional<OtBookingRequestHd> findByVisitId(Long visitId);
}