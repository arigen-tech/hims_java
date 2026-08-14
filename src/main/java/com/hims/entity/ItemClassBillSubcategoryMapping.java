package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "item_class_bill_subcategory_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemClassBillSubcategoryMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_class_id", nullable = false)
    private MasItemClass itemClass;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ipd_bill_subcategory_id", nullable = false)
    private MasIpdServiceSubcategory ipdBillSubcategoryId;

    @Column(name = "last_changed_by")
    private String lastChangedBy;

    @Column(name = "last_change_datetime")
    private LocalDateTime lastChangeDatetime;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "create_datetime")
    private LocalDateTime createDatetime;
}