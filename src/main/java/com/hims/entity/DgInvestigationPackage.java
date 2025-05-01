package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "dg_investigation_package")
public class DgInvestigationPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long packId;

    @Column(name = "name", nullable = false)
    private String packName;

    @Column(name = "description")
    private String descrp;

    @Column (name = "base_cost")
    private double baseCost;

    @Column (name = "flat_discount")
    private double disc;

    @Column (name = "discount_per")
    private double discPer;

    @Column (name = "actual_cost")
    private double actualCost;

    @Size(max = 1)
    @Column (name = "status", length = 1)
    private String status;

    @Size(max = 200)
    @Column (name = "created_by", length = 200)
    private String createdBy;

    @Column (name = "created_dt")
    private LocalDateTime createdDt;

    @Size(max = 200)
    @Column (name = "updated_by", length = 200)
    private String updatedBy;

    @Column (name = "updated_dt")
    private LocalDateTime updatedDt;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @Column (name = "from_dt")
    private LocalDate fromDt;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @Column (name = "to_dt")
    private LocalDate toDt;

    @Column(name = "category")
    private String category;

    @Size(max = 1)
    @Column (name = "discount_flag", length = 1)
    private String discFlag;

}
