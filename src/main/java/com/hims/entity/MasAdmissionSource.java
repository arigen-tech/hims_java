package com.hims.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mas_admission_source")
public class MasAdmissionSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admission_source_id")
    private Long id;

    @Column(name = "admission_source_name", length = 50)
    private String admissionSourceName;

    @Column(name = "description", length = 200)
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