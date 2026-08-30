package com.hims.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "ot_booking", schema = "public", uniqueConstraints = {@UniqueConstraint(
                        name = "uk_ot_booking_no",
                        columnNames = "booking_no"), @UniqueConstraint(name = "uk_ot_booking_request", columnNames = "ot_booking_request_id")}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ot_booking_id")
    private Long otBookingId;

    @Column(name = "booking_no", length = 50, nullable = false, unique = true)
    private String bookingNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ot_booking_request_id", nullable = false, unique = true)
    private OtBookingRequestHd otBookingRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ot_id", nullable = false)
    private MasOperationTheatre operationTheatre;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "scheduled_start_time", nullable = false)
    private LocalTime scheduledStartTime;

    @Column(name = "scheduled_end_time", nullable = false)
    private LocalTime scheduledEndTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_status_id", nullable = false)
    private MasOtBookingStatus bookingStatus;

    @Column(name = "booked_by", length = 200, nullable = false)
    private String bookedBy;

    @Column(name = "booked_date", nullable = false)
    private LocalDateTime bookedDate;

    @Column(name = "cancelled_by", length = 200)
    private String cancelledBy;

    @Column(name = "cancelled_date")
    private LocalDateTime cancelledDate;

    @Column(name = "status", length = 1, nullable = false)
    private String status;

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;

    @Column(name = "is_pac_required", length = 1, nullable = false)
    private String isPacRequired;

    @Column(name = "is_pac_done", length = 1, nullable = false)
    private String isPacDone;
}