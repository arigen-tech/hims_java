package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ot_surgery_consumable", schema = "public", indexes =
        @Index(name = "idx_ot_surgery_consumable_execution", columnList = "ot_surgery_execution_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtSurgeryConsumable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ot_surgery_consumable_id", nullable = false)
    private Long otSurgeryConsumableId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ot_surgery_execution_id", nullable = false)
    private OtSurgeryExecution otSurgeryExecution;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "item_name", nullable = false, length = 250)
    private String itemName;

    @Column(name = "planned_quantity", precision = 12, scale = 2)
    private BigDecimal plannedQuantity;

    @Column(name = "used_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal usedQuantity;

    @Column(name = "unit", length = 30)
    private String unit;

    @Column(name = "batch_no", length = 100)
    private String batchNo;

    @Column(name = "remarks", columnDefinition = "text")
    private String remarks;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 1)
    private String status = "Y";

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @UpdateTimestamp
    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;
}
