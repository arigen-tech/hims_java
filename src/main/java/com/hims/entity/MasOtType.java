package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_ot_type", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasOtType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ot_type_id", nullable = false)
    private Long otTypeId;

    @Column(name = "ot_type_code", nullable = false, length = 30)
    private String otTypeCode;

    @Column(name = "ot_type_name", nullable = false, length = 100)
    private String otTypeName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "last_updated_by", length = 100)
    private String lastUpdatedBy;
}
