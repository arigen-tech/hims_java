package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_brand")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MasBrand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id",nullable=false)
    private Long brandId;

    @Column(name = "brand_name", length = 100)
    private String brandName;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_updated_by", length = 150)
    private String lastUpdatedBy;

    @Column(name = "last_updated_dt")
    private LocalDateTime lastUpdatedDt;

}
