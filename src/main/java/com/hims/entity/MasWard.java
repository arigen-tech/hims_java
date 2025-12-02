package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "mas_ward")
@Data
public class MasWard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ward_id")
    private Long wardId;

    @Column(name = "ward_name", length = 100)
    private String wardName;

     @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_category_id")
    private MasWardCategory wardCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "care_level_id")
    private MasCareLevel careLevel;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDate lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}
