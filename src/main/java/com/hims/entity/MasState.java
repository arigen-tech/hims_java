package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "mas_state")
public class MasState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "state_id", nullable = false)
    private Long id;

    @Size(max = 8)
    @Column(name = "state_code", length = 8)
    private String stateCode;

    @Size(max = 30)
    @Column(name = "state_name", length = 30)
    private String stateName;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private MasCountry country;

}
