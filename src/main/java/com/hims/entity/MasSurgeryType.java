package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_surgery_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasSurgeryType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "surgery_type_id")
    private Long surgeryTypeId;

    @Column(name = "surgery_type_code", length = 10)
    private String surgeryTypeCode;

    @Column(name = "surgery_type_name", length = 100)
    private String surgeryTypeName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdatedDate;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "last_updated_by", length = 100)
    private String lastUpdatedBy;
}