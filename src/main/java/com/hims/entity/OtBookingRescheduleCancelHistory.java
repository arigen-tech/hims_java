package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "ot_booking_reschedule_cancel_history", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtBookingRescheduleCancelHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ot_booking_resc_cancel_history_id")
    private Long otBookingRescCancelHistoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ot_booking_id", nullable = false)
    private OtBooking otBooking;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "old_ot_id", nullable = false)
    private MasOperationTheatre oldOt;

    @Column(name = "old_scheduled_date", nullable = false)
    private LocalDate oldScheduledDate;

    @Column(name = "old_start_time", nullable = false)
    private LocalTime oldStartTime;

    @Column(name = "old_end_time", nullable = false)
    private LocalTime oldEndTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_ot_id")
    private MasOperationTheatre newOt;

    @Column(name = "new_scheduled_date")
    private LocalDate newScheduledDate;

    @Column(name = "new_start_time")
    private LocalTime newStartTime;

    @Column(name = "new_end_time")
    private LocalTime newEndTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reason_id", nullable = false)
    private MasOtScheduleChangeReason reason;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "requested_by", nullable = false, length = 200)
    private String requestedBy;

    @Column(name = "requested_date", nullable = false)
    private LocalDateTime requestedDate;

    @Column(name = "review_status", length = 20)
    private String reviewStatus;

    @Column(name = "reviewed_by", length = 200)
    private String reviewedBy;

    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;

    @Column(name = "review_remarks", length = 1000)
    private String reviewRemarks;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;
}
