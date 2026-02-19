package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="store_balance_m")
@Data
public class StoreBalanceHd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_m_id")
    private Long balanceMId;

    @Column(name = "balance_no", length = 50)
    private String balanceNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private MasHospital hospitalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private MasDepartment departmentId;

    @Column(name = "entered_by", length = 150)
    private String enteredBy;

    @Column(name = "entered_dt")
    private LocalDateTime enteredDt;

    @Column(name = "approved_by", length = 150)
    private String approvedBy;

    @Column(name = "approval_dt")
    private LocalDateTime approvalDt;

    @Column(name = "remarks", columnDefinition = "text")
    private String remarks;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_updated_dt")
    private LocalDateTime lastUpdatedDt;
}
