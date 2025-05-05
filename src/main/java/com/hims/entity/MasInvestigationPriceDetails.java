package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "investigation_price_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasInvestigationPriceDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "investigation_id")
    private DgMasInvestigation investigation;

    @Column(name = "from_dt")
    private LocalDate fromDate;

    @Column(name = "to_dt")
    private LocalDate toDate;

    @Column(name = "last_chg_dt")
    private LocalTime lastChgDt;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;
}
