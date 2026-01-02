package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "emp_membership")
@Getter
@Setter
public class EmployeeMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_id", nullable = false)
    private Long membershipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private MasEmployee employee;

    @Column(name = "membership_summary", length = 300, nullable = false)
    private String membershipSummary;

    @Column(name = "last_update_date")
    private Instant lastUpdateDate;

    @Column(name = "order_level")
    private Integer orderLevel;
}

