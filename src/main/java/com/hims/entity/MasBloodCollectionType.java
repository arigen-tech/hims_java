package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_blood_collection_type")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasBloodCollectionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collection_type_id")
    private Long collectionTypeId;

    @Column(name = "collection_type_code", length = 10, nullable = false)
    private String collectionTypeCode;

    @Column(name = "collection_type_name", length = 50, nullable = false)
    private String collectionTypeName;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}
