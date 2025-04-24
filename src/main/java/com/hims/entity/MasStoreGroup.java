package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name="mas_store_group")
public class MasStoreGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="group_id",nullable = false)
    private Integer id;

    @Column(name="group_code",length = 8)
    private String groupCode;

    @Column(name="group_name",length = 30)
   private String groupName;

    @Column(name="last_chg_by",length = 12)
    private String lastChgBy;

    @Column(name="last_chg_date")
   private Instant lastChgDate;

    @Column(name="last_chg_time",length = 10)
   private String lastChgTime;

    @Column(name="status",length = 1)
    private String status;


}
