package com.hims.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ot_booking_status_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtBookingStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ot_booking_status_log_id")
    private Long otBookingStatusLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_booking_id", referencedColumnName = "ot_booking_id", nullable = false)
    private OtBooking otBooking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_status_id", referencedColumnName = "booking_status_id")
    private MasOtBookingStatus fromStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_status_id", referencedColumnName = "booking_status_id", nullable = false)
    private MasOtBookingStatus toStatus;

    @Column(name = "action_remarks", length = 1000)
    private String actionRemarks;

    @Column(name = "changed_by", length = 200, nullable = false)
    private String changedBy;

    @Column(name = "changed_date", nullable = false)
    private LocalDateTime changedDate;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;


}