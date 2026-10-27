package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        name = "patient_abha_mapping",
        indexes = {
                @Index(
                        name = "idx_pam_abha_number",
                        columnList = "abha_number"
                ),
                @Index(
                        name = "idx_pam_patient_hospital_id",
                        columnList = "patient_hospital_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "patient_abha_mapping_patient_hospital_id_key",
                        columnNames = "patient_hospital_id"
                )
        }
)
public class PatientAbhaMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "patient_hospital_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "abha_hospital_fk")
    )
    private MasHospital patientHospitalId;

    @Column(name = "abha_number", nullable = false, length = 50)
    private String abhaNumber;

    @Column(name = "abha_address", length = 100)
    private String abhaAddress;

    @Column(name = "abha_profile_photo", columnDefinition = "TEXT")
    private String abhaProfilePhoto;

    @Column(name = "x_token", length = 500)
    private String xToken;

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

    @Column(name = "registration_date")
    @CreationTimestamp
    private LocalDateTime registrationDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "patient_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "abha_patient_fk")
    )
    private Patient patient;


}
