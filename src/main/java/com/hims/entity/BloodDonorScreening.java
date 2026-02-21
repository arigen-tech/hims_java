package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_donor_screening")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodDonorScreening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "screening_id")
    private Long screeningId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private BloodDonor donor;

    @Column(name = "screening_date", nullable = false)
    private LocalDate screeningDate;

    @Column(name = "hemoglobin", precision = 4, scale = 2, nullable = false)
    private BigDecimal hemoglobin;

    @Column(name = "weight_kg", precision = 5, scale = 2, nullable = false)
    private BigDecimal weightKg;

    @Column(name = "height_cm", precision = 5, scale = 2, nullable = false)
    private BigDecimal heightCm;

    @Column(name = "blood_pressure", length = 20)
    private String bloodPressure;

    @Column(name = "pulse_rate")
    private Integer pulseRate;

    @Column(name = "temperature", precision = 4, scale = 2)
    private BigDecimal temperature;

    @Column(name = "screening_result", columnDefinition = "char(1)", nullable = false)
    private String screeningResult;

    @Column(name = "deferral_type", columnDefinition = "char(1)")
    private String deferralType;

    @Column(name = "deferral_reason", length = 300)
    private String deferralReason;

    @Column(name = "deferral_upto_date")
    private LocalDate deferralUptoDate;

    @Column(name = "remarks", length = 300)
    private String remarks;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;
}
