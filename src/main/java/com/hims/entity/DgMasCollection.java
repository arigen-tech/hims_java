package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="dg_mas_collection")
@Data
public class DgMasCollection {

    @Id
    @Column(name = "collection_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long collectionId;

    @Column(name = "collection_code", length = 8)
    private String collectionCode;

    @Column(name = "collection_name", length = 30)
    private String collectionName;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 12)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDate lastChgDate;

    @Column(name = "last_chg_time", length = 10)
    private String lastChgTime;
}
