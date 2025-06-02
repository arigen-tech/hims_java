package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "investigation_price_details")
public class InvestigationPriceDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investigation_id")
    private DgMasInvestigation dgMasInvestigation;

    @Column(name = "from_dt")
    private LocalDate fromDate;

    @Column(name = "to_dt")
    private LocalDate toDate;

    @Column(name = "status", length=1)
    private String status;

    @Column(name = "price")
    private Double price;

}


