package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name="mas_ward_category")
@Data
public class MasWardCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ward_category_id")
    private Long id;

    @Column(name = "ward_category_name",length=50)
    private String categoryName;

    @Column(name = "description")
    private  String description;

    @Column(name = "status",length=1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDate lastUpdateDate;

    @Column(name = "created_by",length=200)
    private String createdBy;

    @Column(name = "last_updated_by",length=200)
    private String lastUpdatedBY;

}
