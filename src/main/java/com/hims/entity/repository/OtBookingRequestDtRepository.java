package com.hims.entity.repository;

import com.hims.entity.OtBookingRequestDt;
import com.hims.entity.OtBookingRequestHd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OtBookingRequestDtRepository extends JpaRepository<OtBookingRequestDt, Long> {

    List<OtBookingRequestDt> findByOtBookingRequest(
            OtBookingRequestHd otBookingRequest
    );

}
