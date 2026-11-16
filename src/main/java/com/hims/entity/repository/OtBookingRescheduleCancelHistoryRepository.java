package com.hims.entity.repository;

import com.hims.entity.OtBookingRescheduleCancelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface OtBookingRescheduleCancelHistoryRepository extends JpaRepository<OtBookingRescheduleCancelHistory, Long> {
    @Query(value = "SELECT EXISTS (SELECT 1 FROM ot_booking_reschedule_cancel_history WHERE ot_booking_id = :otBookingId AND UPPER(action_type) = UPPER(:actionType) AND UPPER(review_status) = UPPER(:reviewStatus) AND status = :status)", nativeQuery = true)
    boolean hasPendingAction(@Param("otBookingId") Long otBookingId, @Param("actionType") String actionType,
                             @Param("reviewStatus") String reviewStatus, @Param("status") String status);

    Optional<OtBookingRescheduleCancelHistory> findFirstByOtBooking_OtBookingRequest_OtBookingRequestIdAndActionTypeIgnoreCaseAndReviewStatusIgnoreCaseAndStatusIgnoreCaseOrderByRequestedDateDesc(
            Long otBookingRequestId, String actionType, String reviewStatus, String status);

    Optional<OtBookingRescheduleCancelHistory> findFirstByOtBooking_OtBookingIdAndActionTypeIgnoreCaseAndReviewStatusIgnoreCaseAndStatusIgnoreCaseOrderByRequestedDateDesc(
            Long otBookingId, String actionType, String reviewStatus, String status);
}
