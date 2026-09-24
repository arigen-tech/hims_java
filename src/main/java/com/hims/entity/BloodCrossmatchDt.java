package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_crossmatch_dt")
@Data
public class BloodCrossmatchDt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crossmatch_dt_id")
    private Long crossmatchDtId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crossmatch_hd_id", nullable = false)
    private BloodCrossmatchHd crossmatchHd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private BloodComponentInventory inventory;

    @Column(name = "unit_no", nullable = false, length = 50)
    private String unitNo;

    @Column(name = "compatibility_result", nullable = false, length = 20)
    private String compatibilityResult;

    @Column(name = "test_date", nullable = false)
    private LocalDate testDate;

    @Column(name = "remarks", length = 300)
    private String remarks;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;
}