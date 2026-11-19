package com.hims.entity.repository;

import com.hims.entity.OtBookingRequestDt;
import com.hims.entity.OtBookingRequestHd;
import com.hims.projection.PendingForOtSurgeryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
            s.surgery_name AS surgeryName,
            COALESCE(d.surgery_type_id, s.surgery_type_id) AS "surgeryTypeId",
            st.surgery_type_name AS "surgeryTypeName",
            s.surgery_level AS "surgeryLevel",
            s.is_anesthesia_required AS anesthesiaRequired

        FROM ot_booking_request_dt d

        LEFT JOIN mas_surgery s
            ON s.surgery_id = d.surgery_id
        LEFT JOIN mas_surgery_type st
            ON st.surgery_type_id = COALESCE(d.surgery_type_id, s.surgery_type_id)

        WHERE d.ot_booking_request_id IN (:requestIds)
         ORDER BY d.sequence_no
        """,
            nativeQuery = true)
    List<PendingForOtSurgeryProjection> findSurgeriesByRequestIds(
            @Param("requestIds") List<Long> requestIds
    );

    @Query(value = """
        SELECT d.ot_booking_request_dt_id AS otBookingDtId,
               d.ot_booking_request_id AS otBookingRequestId,
               current_booking.ot_booking_id AS otBookingId,
               d.surgery_id AS surgeryId,
               s.surgery_name AS surgeryName,
               COALESCE(d.surgery_type_id, s.surgery_type_id) AS "surgeryTypeId",
               st.surgery_type_name AS "surgeryTypeName",
               s.surgery_level AS "surgeryLevel",
               s.is_anesthesia_required AS anesthesiaRequired
        FROM public.ot_booking_request_hd h
        JOIN public.ot_booking_request_dt d
          ON d.ot_booking_request_id = h.ot_booking_request_id
        LEFT JOIN public.mas_surgery s ON s.surgery_id = d.surgery_id
        LEFT JOIN public.mas_surgery_type st
          ON st.surgery_type_id = COALESCE(d.surgery_type_id, s.surgery_type_id)
        LEFT JOIN LATERAL (
            SELECT ob.ot_booking_id, ob.booking_status_id
            FROM public.ot_booking ob
            WHERE ob.ot_booking_request_id = h.ot_booking_request_id
              AND UPPER(COALESCE(ob.status, 'Y')) = 'Y'
            ORDER BY ob.ot_booking_id DESC
            LIMIT 1
        ) current_booking ON TRUE
        JOIN public.mas_ot_booking_status bs
          ON bs.booking_status_id = COALESCE(current_booking.booking_status_id, h.booking_status_id)
        WHERE h.admission_id = :inpatientId
          AND UPPER(bs.status_code) IN ('REQUESTED', 'SCHEDULED', 'SENT_TO_OT', 'RECEIVED_IN_OT')
        ORDER BY h.ot_booking_request_id DESC, d.sequence_no ASC, d.ot_booking_request_dt_id ASC
        """, nativeQuery = true)
    List<PendingForOtSurgeryProjection> findSurgeriesByInpatientIdAndAllowedBookingStatus(
            @Param("inpatientId") Long inpatientId);

    @Modifying
    @Query("""
            DELETE FROM OtBookingRequestDt dt
            WHERE dt.otBookingRequest.otBookingRequestId = :bookingId
            """)
    void deleteByBookingId(
            @Param("bookingId") Long bookingId
    );
}

