package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "surgery_drug_consumable_template_dt", schema = "public", indexes = {
        @Index(name = "idx_surgery_drug_consumable_template_dt_hd", columnList = "template_id"),
        @Index(name = "idx_surgery_drug_consumable_template_dt_item", columnList = "item_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurgeryDrugConsumableTemplateDt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_detail_id", nullable = false)
    private Long templateDetailId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private SurgeryDrugConsumableTemplateHd template;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private MasStoreItem item;

    @Column(name = "planned_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal plannedQuantity;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 1)
    private String status = "Y";

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @UpdateTimestamp
    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;
}
