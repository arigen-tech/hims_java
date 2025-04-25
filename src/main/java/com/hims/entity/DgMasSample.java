package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
@Entity
@Table(name = "dg_mas_sample")
@Data
public class DgMasSample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sample_id", nullable = false)
    private Long id;

    @Column(name = "sample_code", length = 7)
    private String sampleCode;

    @Column(name = "sample_description", length = 30)
    private String sampleDescription;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 100)
    private String lastChgBy;

    @Column(name = "last_chg_date", length = 8 )
    private Instant lastChgDate;


}
