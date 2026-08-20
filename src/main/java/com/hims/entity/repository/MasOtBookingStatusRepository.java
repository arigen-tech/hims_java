package com.hims.entity.repository;

import com.hims.entity.MasOtBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MasOtBookingStatusRepository extends JpaRepository<MasOtBookingStatus, Long> {

    // Active records only, alphabetical by status name (flag = 1)
    List<MasOtBookingStatus> findByStatusIgnoreCaseOrderByStatusNameAsc(String status);

    // All records, active first, most recently changed first (flag = 0)
    List<MasOtBookingStatus> findAllByOrderByStatusDescLastChgDateDesc();

    // For uniqueness validation on statusCode (create / update)
    Optional<MasOtBookingStatus> findByStatusCodeIgnoreCase(String statusCode);
}