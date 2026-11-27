package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ot_surgery_execution", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ot_surgery_execution_booking", columnNames = "ot_booking_id")
}, indexes = {
        @Index(name = "idx_ot_surgery_execution_booking", columnList = "ot_booking_id"),
        @Index(name = "idx_ot_surgery_execution_request_dt", columnList = "ot_booking_request_dt_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtSurgeryExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ot_surgery_execution_id", nullable = false)
    private Long otSurgeryExecutionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ot_booking_id", nullable = false)
    private OtBooking otBooking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ot_booking_request_dt_id", nullable = false)
    private OtBookingRequestDt otBookingRequestDt;

    @Column(name = "surgery_start_datetime")
    private LocalDateTime surgeryStartDatetime;

    @Column(name = "surgery_end_datetime")
    private LocalDateTime surgeryEndDatetime;

    @Builder.Default
    @Column(name = "execution_status", nullable = false, length = 20)
    private String executionStatus = "NOT_STARTED";

    @Column(name = "procedure_confirmed", length = 1)
    private String procedureConfirmed;

    @Column(name = "instrument_count_correct", length = 1)
    private String instrumentCountCorrect;

    @Column(name = "specimen_label_verified", length = 1)
    private String specimenLabelVerified;

    @Column(name = "equipment_issues_reviewed", length = 1)
    private String equipmentIssuesReviewed;

    @Column(name = "recovery_concerns_reviewed", length = 1)
    private String recoveryConcernsReviewed;

    @Column(name = "signout_remarks", columnDefinition = "text")
    private String signoutRemarks;

    @Column(name = "signout_verified_by", length = 200)
    private String signoutVerifiedBy;

    @Column(name = "signout_verified_datetime")
    private LocalDateTime signoutVerifiedDatetime;

    @Column(name = "remarks", columnDefinition = "text")
    private String remarks;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 1)
    private String status = "Y";

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @UpdateTimestamp
    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;
}
