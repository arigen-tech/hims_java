package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "mas_country")
public class MasCountry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "country_id", nullable = false)
    private Long id;

    @Size(max = 8)
    @Column(name = "country_code", length = 8)
    private String countryCode;

    @Size(max = 30)
    @Column(name = "country_name", length = 30)
    private String countryName;

    @Size(max = 1)
    @Column(name = "status", length = 1)
    private String status;

    @Size(max = 12)
    @Column(name = "last_chg_by", length = 12)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @Size(max = 10)
    @Column(name = "last_chg_time", length = 10)
    private String lastChgTime;

}
