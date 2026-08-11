package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "mas_procedure_consumable_template_detail",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_proc_template_item",
                        columnNames = {"template_id", "item_id"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasProcedureConsumableTemplateDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_detail_id")
    private Long templateDetailId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false, foreignKey = @ForeignKey(name = "fk_proc_temp_detail"))
    private MasProcedureConsumableTemplate template;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_proc_temp_item"))
    private MasStoreItem item;

    @Column(name = "default_qty", precision = 10, scale = 2)
    private BigDecimal defaultQty = BigDecimal.ONE;

    @Column(name = "is_mandatory", length = 1)
    private String isMandatory;

    @Column(name = "remarks", length = 200)
    private String remarks;

    @Column(name = "display_order")
    private Integer displayOrder;
}