package com.hims.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "visit_reschedule_history")
public class VisitRescheduleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    public Long historyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visit_id", nullable = false)
    public Visit visitId;

    @Column(name = "new_visit_datetime")
    public Instant newVisitDatetime;

    @Column(name = "old_visit_datetime")
    public Instant oldVisitDatetime;

    @Column(name = "old_token_no")
    public Integer oldTokenNo;

    @Column(name = "new_token_no")
    public Integer newTokenNo;

    @Column(name = "reschedule_reason", length = 200)
    public String rescheduleReason;

    @Column(name = "reschedule_by", length = 100)
    public String rescheduleBy;

    @Column(name = "reschedule_datetime")
    public Instant rescheduleDatetime;
}