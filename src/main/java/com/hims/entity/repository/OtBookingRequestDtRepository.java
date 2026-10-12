package com.hims.entity.repository;

import com.hims.entity.OtBookingRequestDt;
import com.hims.entity.OtBookingRequestHd;
import com.hims.projection.PendingForOtSurgeryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OtBookingRequestDtRepository extends JpaRepository<OtBookingRequestDt, Long> {

    List<OtBookingRequestDt> findByOtBookingRequest(
            OtBookingRequestHd otBookingRequest
    );

    @Query(value = """
        SELECT
            d.ot_booking_request_dt_id AS otBookingDtId,
            d.ot_booking_request_id AS otBookingRequestId,
            d.surgery_id AS surgeryId,
            s.surgery_name AS surgeryName

        FROM ot_booking_request_dt d

        LEFT JOIN mas_surgery s
            ON s.surgery_id = d.surgery_id

        WHERE d.ot_booking_request_id IN (:requestIds)
         ORDER BY d.sequence_no
        """,
            nativeQuery = true)
    List<PendingForOtSurgeryProjection> findSurgeriesByRequestIds(
            @Param("requestIds") List<Long> requestIds
    );
}

