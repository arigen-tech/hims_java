package com.hims.entity.repository;

import com.hims.entity.OtBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtBookingRepository extends JpaRepository<OtBooking,Long> {
}
