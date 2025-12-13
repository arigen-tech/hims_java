package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mas_procedure_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasProcedureType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "procedure_type_id")
    private Long procedureTypeId;

    @Column(name = "procedure_type_name", length = 100, nullable = false)
    private String procedureTypeName;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "status", length = 1, nullable = false)
    private String status = "Y";

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}
