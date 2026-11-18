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
        name = "blood_component_discard",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_blood_discard_inventory",
                        columnNames = "inventory_id"
                )
        }
)
public class BloodComponentDiscard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discard_id")
    private Long discardId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "inventory_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_blood_discard_inventory")
    )
    private BloodComponentInventory inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "discard_reason_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_blood_discard_reason")
    )
    private MasBloodDiscardReason discardReason;

    @Column(name = "discard_datetime", nullable = false)
    private LocalDateTime discardDatetime;

    @Column(name = "discarded_by", nullable = false, length = 200)
    private String discardedBy;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "status", length = 1)
    private String status = "Y";

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        if (discardDatetime == null) {
            discardDatetime = LocalDateTime.now();
        }

        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }

        if (status == null) {
            status = "Y";
        }
    }
}