package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mas_treatment_advise")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasTreatmentAdvise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "treatment_advise_id")
    private Long treatmentAdviseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private MasDepartment department;

    @Column(name = "treatment_advice", length = 500, nullable = false)
    private String treatmentAdvice;

    @Column(name = "status", length = 1, nullable = false)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}
