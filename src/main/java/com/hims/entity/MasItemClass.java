package com.hims.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name="mas_item_class")
public class MasItemClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_class_id", nullable = false)
    private Integer itemClassId;

    @Column(name = "item_class_code", length = 8)
    private String itemClassCode;

    @Column(name = "item_class_name", length = 32)
    private String itemClassName;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by")
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDate lastChgDate;

    @Column(name = "last_chg_time", length = 8)
    private String lastChgTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private MasStoreSection masStoreSection;


}
