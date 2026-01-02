package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "emp_specialty_interest")
@Getter
@Setter
public class EmployeeSpecialtyInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_id", nullable = false)
    private Long interestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private MasEmployee employee;

    @Column(name = "interest_summary", length = 300, nullable = false)
    private String interestSummary;

    @Column(name = "last_update_date")
    private Instant lastUpdateDate;
}

