package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.*;

@Getter
@Setter
@Entity
@Table(name = "mas_store_unit")
public class MasStoreUnit {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unit_id", nullable = false)
    private long unitId;

    @NotNull
    @Size(max = 30)
    @Column(name = "unit_name", length = 30)
    private String unitName;

    @NotNull
    @Size(max = 1)
    @Column(name = "status",nullable = false, length = 1)
    private String status;

    @Size(max = 15)
    @Column(name = "last_chg_by", length = 15)
    private String lastChgBy;

    @CreationTimestamp
    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @Column(name = "last_chg_time")
    private String lastChangeTime;

}
