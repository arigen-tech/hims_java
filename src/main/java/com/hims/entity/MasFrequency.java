package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
@Getter
@Setter
@Entity
@Table(name="mas_frequency")

public class MasFrequency {
    @Id
    @Column(name = "frequency_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long frequency_id;


//    @Column(name="frequency_code",length=8)
//    private  String frequencyCode;

    @Column(name="frequency_name",length=30)
    private String frequencyName;

    @Column(name="status",nullable=false,length=1)
    private String status;

    @Column(name="last_chg_by",length=12)
    private String lastChgBy;

    @Column(name="last_chg_date")
    private Instant lastChgDate;

    @Column(name="last_chg_time",length=10)
    private String lastChgTime;

    @Column(name = "feq")
    private Double feq;

//    @Column(name="frequency",length=30)
//    private String frequency;

    @Column(name="order_no")
    private Long orderNo;



}
