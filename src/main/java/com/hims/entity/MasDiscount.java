package com.hims.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "mas_discount")
public class MasDiscount {
    @Id
    @Column(name = "schema_id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "schema_name", nullable = false, length = 100)
    private String schemaName;

    @NotNull
    @Column(name = "dis_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal disPercentage;

    @Column(name = "max_discount", precision = 10, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "min_amount", precision = 10, scale = 2)
    private BigDecimal minAmount;

    @NotNull
    @Column(name = "from_dt", nullable = false)
    private LocalDate fromDt;

    @NotNull
    @Column(name = "to_dt", nullable = false)
    private LocalDate toDt;

    @Size(max = 50)
    @NotNull
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Size(max = 20)
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_dt")
    private Instant createdDt;

    @Column(name = "updated_dt")
    private Instant updatedDt;

    @Column(name = "notes", length = Integer.MAX_VALUE)
    private String notes;

}
