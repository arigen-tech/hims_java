package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mas_ipd_service_subcategory")
public class MasIpdServiceSubcategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subcategory_id")
    private Long subcategoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "mas_ipd_service_subcategory_category_id_fkey"))
    private MasIpdServiceCategory category;

    @Column(name = "subcategory_code", length = 50)
    private String subcategoryCode;

    @Column(name = "subcategory_name", length = 150)
    private String subcategoryName;


    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 100)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;


}