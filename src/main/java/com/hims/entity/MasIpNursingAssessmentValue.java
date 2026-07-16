package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_ip_nursing_assessment_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasIpNursingAssessmentValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assessment_value_id")
    private Long assessmentValueId;

    @Column(name = "category_code", nullable = false, length = 50)
    private String categoryCode;

    @Column(name = "value_code", nullable = false, length = 50)
    private String valueCode;

    @Column(name = "value_name", nullable = false, length = 100)
    private String valueName;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_by", length = 200)
    private String updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}