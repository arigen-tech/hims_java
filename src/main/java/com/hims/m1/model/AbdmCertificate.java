package com.hims.m1.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * Entity to store ABDM certificates/public keys fetched from ABDM API.
 * <p>This entity stores certificates fetched from ABDM's certificate endpoint.
 * Certificates are cached in the database to avoid repeated API calls.
 * The certificate is associated with the environment (Sandbox/Gateway) and keyId.</p>
 */
@Entity
@Table(name = "abdm_certificates", indexes = {
    @Index(name = "idx_cert_3month_key_id", columnList = "keyId"),
    @Index(name = "idx_cert_3month_environment", columnList = "environment"),
    @Index(name = "idx_cert_3month_expiry", columnList = "expiry")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbdmCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Key identifier for the certificate
     */
    @Column(name = "key_id", length = 100)
    private String keyId;

    /**
     * Environment where the certificate was fetched from (SANDBOX or GATEWAY)
     */
    @Column(name = "environment", length = 20, nullable = false)
    private String environment;

    /**
     * Certificate content in PEM format or as string
     */
    @Lob
    @Column(name = "certificate", nullable = false)
    private String certificate;

    /**
     * Algorithm used (e.g., RSA, EC)
     */
    @Column(name = "algorithm", length = 50)
    private String algorithm;

    /**
     * Certificate expiry date/time
     */
    @Column(name = "expiry")
    private Instant expiry;

    /**
     * Timestamp when this certificate was fetched from ABDM
     */
    @Column(name = "fetched_at", nullable = false, updatable = false)
    private Instant fetchedAt;

    /**
     * Timestamp when this record was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /**
     * Timestamp when this record was last updated
     */
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (fetchedAt == null) {
            fetchedAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Check if the certificate is still valid (not expired)
     */
    public boolean isValid() {
        if (expiry == null) {
            return true; // If no expiry, consider it valid
        }
        return expiry.isAfter(Instant.now());
    }
}

