package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "mas_bed_type")
public class MasBedType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bed_type_id")
    private Long bedTypeId;

    @Column(name = "bed_type_name", length = 50)
    private String bedTypeName;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "status", length = 1)
    private String status;

    @UpdateTimestamp
    @Column(name = "last_update_date")
    private LocalDate lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}
