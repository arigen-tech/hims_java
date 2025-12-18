package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "patient_login")
public class PatientLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_login_id", nullable = false)
    private Long patientLoginId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "mobile_no", length = 15, nullable = false)
    private String mobileNo;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "last_login")
    private Instant lastLogin;

    @Column(name = "status", length = 1)
    private String status;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private Instant createdDate;

    @UpdateTimestamp
    @Column(name = "last_updated_date")
    private Instant lastUpdatedDate;
}

