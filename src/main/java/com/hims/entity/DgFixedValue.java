package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name="dg_fixed_value")
@Entity
public class DgFixedValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fixed_id", nullable = false)
    private Long id;
    @Column(name = "fixed_value", length=500)
    private String fixedValue;
//    @Column(name = "sub_investigation_id")
//    private DgSubMasInvestigation subInvestigationId;
}
