package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Table(name = "dg_uom")
@Entity
public class DgUom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uom_id", nullable = false)
    private Long id;

    @Column(name = "uom_code", length = 8)
    private String uomCode;

    @Column(name = "uom_name", length = 30)
    private String name;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 12)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @Column(name = "last_chg_time", length = 10)
    private String  lastChgTime;
}
