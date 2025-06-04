package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="mas_store_section")
@Data
public class MasStoreSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id",nullable = false)
    private Integer sectionId;

    @Column(name = "section_code", length = 10)
    private String sectionCode;

    @Column(name = "section_name", length = 200)
    private String sectionName;

    @Column(name = "last_chg_date")
    private LocalDate lastChgDate;

    @Column(name = "last_chg_time", length = 10)
    private String lastChgTime;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 12)
    private String lastChgBy;

    @Column(name = "hospital_id")
    private Integer hospitalId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_type_id")
    private MasItemType masItemType;

}
