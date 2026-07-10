package com.hims.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "mas_ipd_procedure_consumable_template_detail", schema = "public")
public class MasIpdProcedureConsumableTemplateDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_detail_id")
    private Long templateDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private MasIpdProcedureSurgeryConsumableTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private MasStoreItem item;

    @Column(name = "default_qty", precision = 10, scale = 2)
    private BigDecimal defaultQty;

    @Column(name = "is_mandatory")
    private Boolean isMandatory;

    @Column(name = "display_order")
    private Integer displayOrder;
}