package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
@Entity
@Data
public class MasItemType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_type_id")
    private int id;

    @Column(name = "item_type_code",length=8)
    private String code;

    @Column(name = "item_type_name",length=30)
    private String name;

    @Column(name = "status", length=1)
    private String status;

    @Column(name = "last_chg_by",length=12)
    private String lastChgBy;

    @Column(name = "last_chg_Date")
    private Instant lastChgDate;

    @Column(name = "last_chg_time",length=10)
    private String lastChgTime;

    @ManyToOne(fetch = FetchType.EAGER  )
    @JoinColumn(name = "group_id")
    private  MasStoreGroup masStoreGroupId;


}
