package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "emp_award")
@Getter
@Setter
public class EmployeeAward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "award_id", nullable = false)
    private Long awardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private MasEmployee employee;

    @Column(name = "award_summary", length = 500, nullable = false)
    private String awardSummary;

    @UpdateTimestamp
    @Column(name = "last_update_date")
    private Instant lastUpdateDate;
}
