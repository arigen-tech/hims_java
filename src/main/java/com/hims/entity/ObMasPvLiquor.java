package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ob_mas_pv_liquor", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObMasPvLiquor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "liquor_value", length = 50, nullable = false)
    private String liquorValue;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}
