package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ot_surgery_drug", schema = "public", indexes =
        @Index(name = "idx_ot_surgery_drug_execution", columnList = "ot_surgery_execution_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtSurgeryDrug {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ot_surgery_drug_id", nullable = false)
    private Long otSurgeryDrugId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ot_surgery_execution_id", nullable = false)
    private OtSurgeryExecution otSurgeryExecution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private MasStoreItem item;

    @Column(name = "item_name", nullable = false, length = 250)
    private String itemName;

    @Column(name = "quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "batch_no", length = 100)
    private String batchNo;

    @Column(name = "used_by")
    private Long usedBy;

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
