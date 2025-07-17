package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "store_stock_taking_m", schema = "public")
public class StoreStockTakingM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taking_m_id")
    private Long takingMId;

    @Column(name = "physical_date")
    private LocalDateTime physicalDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "stock_taking_no", length = 200)
    private String stockTakingNo;

    @Column(name = "approved_by", length = 200)
    private String approvedBy;

    @Column(name = "approved_dt")
    private LocalDateTime approvedDt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hospital_id")
    private MasHospital hospitalId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department")
    private MasDepartment departmentId;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "created_by", length = 200)
    private String createdBy;
}
