package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ot_checklist", schema = "public",
        uniqueConstraints = @UniqueConstraint(name = "uk_ot_checklist_booking_stage", columnNames = {"ot_booking_id", "checklist_stage"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtChecklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ot_checklist_id")
    private Long otChecklistId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ot_booking_id", nullable = false)
    private OtBooking otBooking;

    @Column(name = "checklist_stage", nullable = false, length = 20)
    private String checklistStage;

    @Column(name = "checklist_status", nullable = false, length = 20)
    private String checklistStatus;

    @Column(name = "remarks", columnDefinition = "text")
    private String remarks;

    @Column(name = "verified_by", length = 200)
    private String verifiedBy;

    @Column(name = "verified_datetime")
    private LocalDateTime verifiedDatetime;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;
}
