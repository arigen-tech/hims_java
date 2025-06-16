package com.hims.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name="mas_hsn")
public class MasHSN {

    @Id
    @Column(name = "hsn_code", length = 10)
    private String hsnCode;

    @Column(name = "gst_rate", precision = 5, scale = 2)
    private BigDecimal gstRate;

    @Column(name = "is_medicine")
    private Boolean isMedicine;

    @Column(name = "hsn_category", length = 100)
    private String hsnCategory;

    @Column(name = "hsn_subcategory", length = 100)
    private String hsnSubcategory;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "created_by", length = 150)
    private String createdBy;

    @Column(name = "last_updated_dt")
    private LocalDateTime lastUpdatedDt;
}
