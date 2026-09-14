package com.hims.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Immutable audit table - every distinct Razorpay webhook event is a new row.
 * Never update/overwrite an existing row's payload; only processing_status,
 * processed_at and error_message change after the row is first inserted.
 *
 * NOTE on JSONB mapping: this uses hypersistence-utils' JsonType, which is the
 * standard way to map JSONB with Hibernate 6 / Spring Boot 3. Add this dependency
 * to pom.xml if not already present:
 *
 * <dependency>
 *     <groupId>io.hypersistence</groupId>
 *     <artifactId>hypersistence-utils-hibernate-63</artifactId>
 *     <version>3.7.3</version>
 * </dependency>
 *
 * If you'd rather not add that dependency, store payload as a plain String
 * (columnDefinition = "jsonb") and serialize/deserialize with ObjectMapper
 * in the service layer instead - see the commented alternative field below.
 */
@Getter
@Setter
@Entity
@Table(name = "payment_webhook_event")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PaymentWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "webhook_event_id", nullable = false)
    private Long webhookEventId;

    // Nullable at insert time in case the payment can't be resolved yet;
    // populate once identified during processing.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private PaymentDetailsV2 payment;

    @NotNull
    @Size(max = 20)
    @Column(name = "gateway", nullable = false, length = 20)
    private String gateway = "RAZORPAY";

    @NotNull
    @Size(max = 100)
    @Column(name = "event_id", nullable = false, length = 100, unique = true)
    private String eventId;

    @NotNull
    @Size(max = 100)
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Size(max = 100)
    @Column(name = "gateway_order_id", length = 100)
    private String gatewayOrderId;

    @Size(max = 100)
    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    @NotNull
    @Type(JsonType.class)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload; // raw JSON string of the full Razorpay webhook body

    // Alternative without hypersistence-utils dependency:
    // @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    // private String payload;

    @NotNull
    @Size(max = 20)
    @Column(name = "processing_status", nullable = false, length = 20)
    private String processingStatus = "RECEIVED"; // RECEIVED, PROCESSED, FAILED

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
//
//    @PrePersist
//    protected void onCreate() {
//        Instant now = Instant.now();
//        this.createdAt = now;
//        this.updatedAt = now;
//        if (this.receivedAt == null) {
//            this.receivedAt = now;
//        }
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        this.updatedAt = Instant.now();
//    }
}