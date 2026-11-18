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
@Table(
        name = "mas_blood_discard_reason",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_blood_discard_reason_code",
                        columnNames = "discard_reason_code"
                )
        }
)
public class MasBloodDiscardReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discard_reason_id")
    private Long discardReasonId;

    @Column(name = "discard_reason_code", nullable = false, length = 50)
    private String discardReasonCode;

    @Column(name = "discard_reason_name", nullable = false, length = 100)
    private String discardReasonName;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "status", length = 1)
    private String status = "Y";

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @PrePersist
    protected void onCreate() {
        if (lastUpdateDate == null) {
            lastUpdateDate = LocalDateTime.now();
        }
        if (status == null) {
            status = "Y";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdateDate = LocalDateTime.now();
    }
}