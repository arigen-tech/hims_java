package com.hims.entity.repository;

import com.hims.entity.MasPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasPaymentStatusRepository extends JpaRepository<MasPaymentStatus, Long> {
    Optional<MasPaymentStatus> findByStatusCode(String statusCode);
}