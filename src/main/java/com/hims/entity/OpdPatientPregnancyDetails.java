package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "opd_patient_pregnancy_details")
public class OpdPatientPregnancyDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pregnancy_details_id", nullable = false)
    private Long pregnancyDetailsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "opd_patient_details_id", insertable = false, updatable = false)
    private OpdPatientDetail opdPatientDetail;

    @Column(name = "opd_patient_details_id", nullable = false)
    private Long opdPatientDetailsId;

    @Column(name = "visit_id")
    private Long visitId;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "is_pregnant")
    private Boolean isPregnant;

    @Column(name = "lmp_date")
    private LocalDate lmpDate;

    @Column(name = "edd")
    private LocalDate edd;

    @Column(name = "current_edd")
    private LocalDate currentEdd;

    @Size(max = 50)
    @Column(name = "gestation_period", length = 50)
    private String gestationPeriod;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @Size(max = 200)
    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;
}
