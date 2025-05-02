package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "mas_sub_chargecode")
public class MasSubChargeCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_chargecode_id", nullable = false)
    private Long subId;

    @Size(max = 10)
    @Column(name = "sub_chargecode_code", length = 10)
    private String subCode;

    @Size(max = 100)
    @Column(name = "sub_chargecode_name", length = 100)
    private String subName;

    @Size(max = 1)
    @Column(name = "status", nullable = false, length = 1 )
    private String status;

    @Size(max = 15)
    @Column(name = "last_chg_by", nullable = false, length = 15)
    private String lastChgBy;

    @Column(name = "last_chg_date", nullable = false)
    private LocalDate lastChgDate;

    @Size(max = 10)
    @Column(name = "last_chg_time", nullable = false, length = 10)
    private String lastChgTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_chargecode_id")
    private MasMainChargeCode mainChargeId;

}
