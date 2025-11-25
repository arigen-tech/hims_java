package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Data
@Table(name="mas_investigation_category")
public class MasInvestigationCategory {

    @Id
    @Column(name="category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(name="category_name",length=100)
    private String categoryName;



    @Column(name="last_chg_by",length=100)
    private String lastChgBy;

    @Column(name="last_chg_date")
    private LocalDate lastChgDate;



}
